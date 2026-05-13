<template>
  <div>
    <h2>借阅记录 & 预约管理</h2>
    
    <el-tabs v-model="activeTab" style="margin-top: 20px;">
      <el-tab-pane label="借阅记录" name="borrow">
        <el-table :data="borrowRecords" style="width: 100%;" border v-loading="loading">
          <el-table-column prop="userId" label="用户ID" width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="bookId" label="图书ID" width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="borrowTime" label="借阅时间" width="160"></el-table-column>
          <el-table-column prop="dueTime" label="应还时间" width="160"></el-table-column>
          <el-table-column prop="returnTime" label="归还时间" width="160"></el-table-column>
          <el-table-column prop="isReturned" label="已归还" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isReturned" type="success">是</el-tag>
              <el-tag v-else type="warning">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isOverdue" label="逾期" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isOverdue" type="danger">是</el-tag>
              <el-tag v-else type="success">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isLost" label="丢失" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isLost" type="danger">是</el-tag>
              <el-tag v-else type="success">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fineAmount" label="罚款" width="80">
            <template slot-scope="scope">
              <span v-if="scope.row.fineAmount > 0" style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.fineAmount }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button v-if="!scope.row.isReturned" size="mini" type="success" @click="returnBook(scope.row)">归还</el-button>
              <el-button v-if="!scope.row.isReturned && !scope.row.isLost" size="mini" type="danger" @click="reportLost(scope.row.id)">丢失</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="预约记录" name="reserve">
        <el-alert title="预约队列说明" type="info" show-icon style="margin-bottom: 15px;">
          <ul style="margin: 5px 0; padding-left: 20px;">
            <li><strong>等待中：</strong>用户正在排队等待图书</li>
            <li><strong>已通知待取：</strong>图书已归还，用户收到通知待取书</li>
            <li><strong>已通知并完成：</strong>预约已完成，用户可前往借阅</li>
            <li><strong>已过期：</strong>超时未取书，预约自动取消</li>
          </ul>
        </el-alert>
        <el-table :data="reservationRecords" style="width: 100%;" border>
          <el-table-column prop="queuePosition" label="排队序号" width="100" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.queuePosition === 1" type="success">第 1 位</el-tag>
              <el-tag v-else type="info">第 {{ scope.row.queuePosition }} 位</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="bookId" label="图书ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="reserveTime" label="预约时间" width="160"></el-table-column>
          <el-table-column prop="expireTime" label="过期时间" width="160"></el-table-column>
          <el-table-column prop="isNotified" label="状态" width="140">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isFulfilled" type="success">已通知并完成</el-tag>
              <el-tag v-else-if="scope.row.isNotified" type="warning">已通知待取</el-tag>
              <el-tag v-else-if="scope.row.isExpired" type="danger">已过期</el-tag>
              <el-tag v-else type="info">等待中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button v-if="!scope.row.isExpired && !scope.row.isFulfilled" size="mini" type="danger" @click="cancelReservation(scope.row.id)">取消预约</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'borrow',
      borrowRecords: [],
      reservationRecords: [],
      loading: false
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'reserve') {
        this.loadReservations()
      }
    }
  },
  mounted() {
    if (this.$root.selectedUserId) {
      this.loadBorrowRecords()
    }
  },
  methods: {
    loadBorrowRecords() {
      if (this.$root.selectedUserId) {
        this.loading = true
        this.$http.get('/borrow/user/' + this.$root.selectedUserId).then(res => {
          if (res.data.code === 200) {
            this.borrowRecords = res.data.data
          }
          this.loading = false
        })
      } else {
        this.$message.info('请先在用户管理中选择一个用户')
      }
    },
    loadReservations() {
      this.$http.get('/reserves').then(res => {
        if (res.data.code === 200) {
          this.reservationRecords = res.data.data
        }
      })
    },
    returnBook(record) {
      this.$confirm('确认归还该图书吗？归还后将自动通知下一位预约用户！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/return/' + record.id).then(res => {
          if (res.data.code === 200) {
            this.$message({
              message: '归还成功！已自动通知下一位预约用户取书！',
              type: 'success',
              duration: 5000,
              showClose: true
            })
            this.loadBorrowRecords()
            this.loadReservations()
          } else {
            this.$message.error(res.data.message)
          }
        })
      })
    },
    reportLost(recordId) {
      this.$confirm('确认登记该图书为丢失状态吗？将产生罚款并扣减库存！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/lost/' + recordId).then(res => {
          if (res.data.code === 200) {
            this.$message.success('丢失登记成功，已产生罚款并扣减库存')
            this.loadBorrowRecords()
          } else {
            this.$message.error(res.data.message)
          }
        })
      })
    },
    cancelReservation(reservationId) {
      this.$confirm('确认取消该预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/reserve/cancel/' + reservationId).then(res => {
          if (res.data.code === 200) {
            this.$message.success('取消预约成功')
            this.loadReservations()
          } else {
            this.$message.error(res.data.message)
          }
        })
      })
    }
  }
}
</script>
