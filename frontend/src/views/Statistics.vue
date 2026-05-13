<template>
  <div>
    <h2>统计分析 & 定时任务监控</h2>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总借阅次数</div>
            <div class="stat-value">{{ statistics.totalBorrowCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">逾期率</div>
            <div class="stat-value" style="color: #f56c6c;">{{ statistics.overdueRate || '0%' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">活跃用户数</div>
            <div class="stat-value">{{ statistics.activeUserCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ statistics.totalUserCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider content-position="left">定时任务监控</el-divider>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="task-card">
          <div slot="header" class="card-header">
            <span>预约超时释放</span>
            <el-tag type="success" size="mini">每小时执行</el-tag>
          </div>
          <div class="task-stat">
            <div class="task-stat-label">累计释放锁定</div>
            <div class="task-stat-value">{{ taskStatus.totalExpiredReservationsReleased || 0 }} 本</div>
          </div>
          <el-button type="primary" size="small" style="width: 100%; margin-top: 15px;" @click="executeTask('expire')" :loading="executingTask === 'expire'">
            立即执行
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="task-card">
          <div slot="header" class="card-header">
            <span>逾期罚款处理</span>
            <el-tag type="warning" size="mini">每天凌晨1点</el-tag>
          </div>
          <div class="task-stat">
            <div class="task-stat-label">累计处理逾期</div>
            <div class="task-stat-value">{{ taskStatus.totalOverdueBooksProcessed || 0 }} 笔</div>
          </div>
          <el-button type="warning" size="small" style="width: 100%; margin-top: 15px;" @click="executeTask('overdue')" :loading="executingTask === 'overdue'">
            立即执行
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="task-card">
          <div slot="header" class="card-header">
            <span>借阅催还通知</span>
            <el-tag type="info" size="mini">每天早上9点</el-tag>
          </div>
          <div class="task-stat">
            <div class="task-stat-label">累计发送催还</div>
            <div class="task-stat-value">{{ taskStatus.totalRemindersSent || 0 }} 次</div>
          </div>
          <el-button type="info" size="small" style="width: 100%; margin-top: 15px;" @click="executeTask('reminder')" :loading="executingTask === 'reminder'">
            立即执行
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="task-card">
          <div slot="header" class="card-header">
            <span>任务执行统计</span>
          </div>
          <div class="task-stat">
            <div class="task-stat-label">总执行次数</div>
            <div class="task-stat-value">{{ taskStatus.totalTaskExecutions || 0 }} 次</div>
          </div>
          <el-button type="success" size="small" style="width: 100%; margin-top: 15px;" @click="refreshTaskStatus">
            刷新数据
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-divider content-position="left">最近任务执行记录</el-divider>
    
    <el-table :data="recentTaskLogs" style="width: 100%;" border>
      <el-table-column prop="executeTime" label="执行时间" width="180"></el-table-column>
      <el-table-column prop="taskName" label="任务名称" width="180">
        <template slot-scope="scope">
          <el-tag :type="getTaskTagType(scope.row.taskType)" size="small">{{ scope.row.taskName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="processedCount" label="处理数量" width="100" align="center">
        <template slot-scope="scope">
          <el-badge :value="scope.row.processedCount" class="item" :type="scope.row.processedCount > 0 ? 'success' : 'info'"></el-badge>
        </template>
      </el-table-column>
      <el-table-column prop="success" label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.success" type="success" size="mini">成功</el-tag>
          <el-tag v-else type="danger" size="mini">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="details" label="执行详情" show-overflow-tooltip></el-table-column>
    </el-table>

    <el-divider content-position="left">热门图书 Top 10</el-divider>
    
    <el-table :data="topBooks" style="width: 100%;" border>
      <el-table-column type="index" label="排名" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.$index === 0" type="danger" size="medium">TOP 1</el-tag>
          <el-tag v-else-if="scope.$index === 1" type="warning" size="medium">TOP 2</el-tag>
          <el-tag v-else-if="scope.$index === 2" type="info" size="medium">TOP 3</el-tag>
          <span v-else>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" width="200"></el-table-column>
      <el-table-column prop="author" label="作者" width="120"></el-table-column>
      <el-table-column prop="category" label="分类" width="100"></el-table-column>
      <el-table-column prop="borrowCount" label="借阅次数" width="120" sortable>
        <template slot-scope="scope">
          <el-tag type="warning">{{ scope.row.borrowCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalQuantity" label="总馆藏" width="100"></el-table-column>
      <el-table-column prop="availableQuantity" label="可借数量" width="100">
        <template slot-scope="scope">
          <span :style="{ color: scope.row.availableQuantity > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
            {{ scope.row.availableQuantity }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      statistics: {},
      taskStatus: {},
      topBooks: [],
      recentTaskLogs: [],
      executingTask: null
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadTaskStatus()
  },
  methods: {
    loadStatistics() {
      this.$http.get('/statistics').then(res => {
        if (res.data.code === 200) {
          this.statistics = res.data.data
          this.topBooks = res.data.data.topBooks || []
        }
      })
    },
    loadTaskStatus() {
      this.$http.get('/task-status').then(res => {
        if (res.data.code === 200) {
          this.taskStatus = res.data.data
          this.recentTaskLogs = res.data.data.recentTaskLogs || []
        }
      })
    },
    refreshTaskStatus() {
      this.loadTaskStatus()
      this.$message.success('数据已刷新')
    },
    getTaskTagType(taskType) {
      if (taskType === 'RESERVATION_EXPIRE') return 'success'
      if (taskType === 'OVERDUE_PROCESS') return 'warning'
      if (taskType === 'OVERDUE_REMINDER') return 'info'
      return ''
    },
    executeTask(taskType) {
      this.executingTask = taskType
      let apiUrl = ''
      let taskName = ''
      
      if (taskType === 'expire') {
        apiUrl = '/task/expire-reservations'
        taskName = '预约超时释放锁定'
      } else if (taskType === 'overdue') {
        apiUrl = '/task/process-overdue'
        taskName = '逾期罚款处理'
      } else if (taskType === 'reminder') {
        apiUrl = '/task/send-reminders'
        taskName = '借阅催还通知'
      }

      this.$http.post(apiUrl).then(res => {
        this.executingTask = null
        if (res.data.code === 200) {
          const log = res.data.data
          this.$message({
            message: `${taskName}执行完成，处理了 ${log.processedCount} 条记录`,
            type: log.processedCount > 0 ? 'success' : 'info',
            duration: 5000
          })
          this.loadTaskStatus()
          this.loadStatistics()
        } else {
          this.$message.error(res.data.message)
        }
      }).catch(() => {
        this.executingTask = null
      })
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-item {
  padding: 10px 0;
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}
.task-card {
  min-height: 200px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-stat {
  text-align: center;
  padding: 10px 0;
}
.task-stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.task-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}
</style>
