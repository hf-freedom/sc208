<template>
  <div>
    <h2>用户管理</h2>
    <el-button type="primary" @click="showAddDialog">添加用户</el-button>
    <el-table :data="users" style="width: 100%; margin-top: 20px;" border @row-click="selectUser">
      <el-table-column label="选择" width="60">
        <template slot-scope="scope">
          <el-radio v-model="selectedUserId" :label="scope.row.id"></el-radio>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="120"></el-table-column>
      <el-table-column prop="phone" label="电话" width="120"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
      <el-table-column prop="level" label="等级" width="80"></el-table-column>
      <el-table-column prop="maxBorrowCount" label="最大借阅" width="80"></el-table-column>
      <el-table-column prop="totalFine" label="总罚款" width="80"></el-table-column>
      <el-table-column prop="unpaidFine" label="未缴罚款" width="80"></el-table-column>
      <el-table-column prop="isBlacklisted" label="黑名单" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isBlacklisted" type="danger">是</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="warning" @click.stop="showPayFineDialog(scope.row)">缴罚款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="添加用户" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="newUser" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="newUser.name"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="newUser.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newUser.email"></el-input>
        </el-form-item>
        <el-form-item label="等级">
          <el-input-number v-model="newUser.level" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="最大借阅">
          <el-input-number v-model="newUser.maxBorrowCount" :min="1"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addUser">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="缴纳罚款" :visible.sync="payFineDialogVisible" width="400px">
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>{{ selectedUser ? selectedUser.name : '' }}</span>
        </el-form-item>
        <el-form-item label="未缴罚款">
          <span>{{ selectedUser ? selectedUser.unpaidFine : 0 }} 元</span>
        </el-form-item>
        <el-form-item label="缴纳金额">
          <el-input-number v-model="payAmount" :min="0" :max="selectedUser ? selectedUser.unpaidFine : 0"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="payFineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="payFine">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      users: [],
      selectedUserId: '',
      selectedUser: null,
      addDialogVisible: false,
      payFineDialogVisible: false,
      payAmount: 0,
      newUser: {
        name: '',
        phone: '',
        email: '',
        level: 1,
        maxBorrowCount: 5
      }
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    loadUsers() {
      this.$http.get('/users').then(res => {
        if (res.data.code === 200) {
          this.users = res.data.data
        }
      })
    },
    selectUser(row) {
      this.selectedUserId = row.id
      this.$root.selectedUserId = row.id
    },
    showAddDialog() {
      this.newUser = {
        name: '',
        phone: '',
        email: '',
        level: 1,
        maxBorrowCount: 5
      }
      this.addDialogVisible = true
    },
    addUser() {
      this.$http.post('/users', this.newUser).then(res => {
        if (res.data.code === 200) {
          this.$message.success('添加成功')
          this.addDialogVisible = false
          this.loadUsers()
        } else {
          this.$message.error(res.data.message)
        }
      })
    },
    showPayFineDialog(user) {
      if (user.unpaidFine <= 0) {
        this.$message.info('该用户没有未缴罚款')
        return
      }
      this.selectedUser = user
      this.payAmount = user.unpaidFine
      this.payFineDialogVisible = true
    },
    payFine() {
      this.$http.post('/fine/pay', null, {
        params: {
          userId: this.selectedUser.id,
          amount: this.payAmount
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.$message.success('缴纳成功')
          this.payFineDialogVisible = false
          this.loadUsers()
        } else {
          this.$message.error(res.data.message)
        }
      })
    }
  }
}
</script>
