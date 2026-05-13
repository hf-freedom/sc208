<template>
  <div>
    <h2>图书管理</h2>
    <el-button type="primary" @click="showAddDialog">添加图书</el-button>
    
    <el-table :data="books" style="width: 100%; margin-top: 20px;" border v-loading="loading">
      <el-table-column prop="title" label="书名" width="200"></el-table-column>
      <el-table-column prop="author" label="作者" width="120"></el-table-column>
      <el-table-column prop="category" label="分类" width="100"></el-table-column>
      <el-table-column prop="totalQuantity" label="总数量" width="80"></el-table-column>
      <el-table-column prop="availableQuantity" label="可借" width="80"></el-table-column>
      <el-table-column prop="borrowedQuantity" label="借出" width="80"></el-table-column>
      <el-table-column prop="reservedQuantity" label="预约数" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.reservedQuantity > 0" type="warning">{{ scope.row.reservedQuantity }}</el-tag>
          <span v-else>0</span>
        </template>
      </el-table-column>
      <el-table-column prop="borrowCount" label="借阅次数" width="100"></el-table-column>
      <el-table-column label="操作" width="280">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="borrowBook(scope.row.id)">借阅</el-button>
          <el-button size="mini" type="success" @click="reserveBook(scope.row.id)">预约</el-button>
          <el-button size="mini" type="info" @click="showReservationQueue(scope.row)">
            查看队列 ({{ scope.row.reservedQuantity }})
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="添加图书" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="newBook" label-width="100px">
        <el-form-item label="书名">
          <el-input v-model="newBook.title"></el-input>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="newBook.author"></el-input>
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="newBook.isbn"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="newBook.category"></el-input>
        </el-form-item>
        <el-form-item label="总数量">
          <el-input-number v-model="newBook.totalQuantity" :min="1"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addBook">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="预约队列" :visible.sync="queueDialogVisible" width="700px">
      <div slot="title">
        <span>{{ currentBook ? currentBook.title : '' }} - 预约队列</span>
        <el-tag style="margin-left: 10px;" type="info">共 {{ reservationQueue.length }} 人在等待</el-tag>
      </div>
      
      <el-alert v-if="hasNotifiedUser" type="success" show-icon style="margin-bottom: 15px;">
        <template slot="title">
          <strong>归还通知已发送！</strong> 下一位预约用户（排名第1）已收到取书通知，预约已完成。
        </template>
      </el-alert>

      <el-table :data="reservationQueue" border>
        <el-table-column prop="queuePosition" label="排队顺序" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.queuePosition === 1" type="success">第 1 位 (正在通知)</el-tag>
            <el-tag v-else type="info">第 {{ scope.row.queuePosition }} 位</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="reserveTime" label="预约时间" width="160"></el-table-column>
        <el-table-column prop="expireTime" label="过期时间" width="160"></el-table-column>
        <el-table-column prop="isNotified" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isFulfilled" type="success">已通知并完成</el-tag>
            <el-tag v-else-if="scope.row.isNotified" type="warning">已通知待取</el-tag>
            <el-tag v-else-if="scope.row.isExpired" type="danger">已过期</el-tag>
            <el-tag v-else type="info">等待中</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="loadReservationQueue(currentBook.id)">刷新队列</el-button>
        <el-button @click="queueDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      books: [],
      loading: false,
      addDialogVisible: false,
      queueDialogVisible: false,
      currentBook: null,
      reservationQueue: [],
      hasNotifiedUser: false,
      newBook: {
        title: '',
        author: '',
        isbn: '',
        category: '',
        totalQuantity: 1
      }
    }
  },
  mounted() {
    this.loadBooks()
  },
  methods: {
    loadBooks() {
      this.loading = true
      this.$http.get('/books').then(res => {
        if (res.data.code === 200) {
          this.books = res.data.data
        }
        this.loading = false
      })
    },
    showAddDialog() {
      this.newBook = {
        title: '',
        author: '',
        isbn: '',
        category: '',
        totalQuantity: 1
      }
      this.addDialogVisible = true
    },
    addBook() {
      this.$http.post('/books', this.newBook).then(res => {
        if (res.data.code === 200) {
          this.$message.success('添加成功')
          this.addDialogVisible = false
          this.loadBooks()
        } else {
          this.$message.error(res.data.message)
        }
      })
    },
    borrowBook(bookId) {
      if (!this.$root.selectedUserId) {
        this.$message.warning('请先在用户管理中选择一个用户')
        return
      }
      this.$http.post('/borrow', null, {
        params: {
          userId: this.$root.selectedUserId,
          bookId: bookId
        }
      }).then(res => {
        if (res.data.code === 200) {
          this.$message.success('借阅成功')
          this.loadBooks()
        } else {
          this.$message.error(res.data.message)
        }
      })
    },
    reserveBook(bookId) {
      if (!this.$root.selectedUserId) {
        this.$message.warning('请先在用户管理中选择一个用户')
        return
      }
      this.$http.post('/reserve', null, {
        params: {
          userId: this.$root.selectedUserId,
          bookId: bookId
        }
      }).then(res => {
        if (res.data.code === 200) {
          const position = res.data.data.queuePosition
          this.$message.success(`预约成功！您当前排在第 ${position} 位`)
          this.loadBooks()
        } else {
          this.$message.error(res.data.message)
        }
      })
    },
    showReservationQueue(book) {
      this.currentBook = book
      this.hasNotifiedUser = false
      this.loadReservationQueue(book.id)
      this.queueDialogVisible = true
    },
    loadReservationQueue(bookId) {
      this.$http.get(`/reserve/book/${bookId}`).then(res => {
        if (res.data.code === 200) {
          this.reservationQueue = res.data.data
          this.hasNotifiedUser = res.data.data.some(r => r.isNotified || r.isFulfilled)
        }
      })
    }
  }
}
</script>
