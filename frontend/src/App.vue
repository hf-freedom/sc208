<template>
  <div id="app">
    <el-container>
      <el-header style="background: #409EFF; color: white; font-size: 24px; line-height: 60px; padding: 0 20px;">
        图书馆借阅风控系统
      </el-header>
      <el-container>
        <el-aside width="200px" style="background: #f5f5f5;">
          <el-menu router mode="vertical" default-active="/books">
            <el-menu-item index="/books">图书管理</el-menu-item>
            <el-menu-item index="/users">用户管理</el-menu-item>
            <el-menu-item index="/borrow">借阅记录</el-menu-item>
            <el-menu-item index="/statistics">统计分析</el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.el-header {
  padding: 0 !important;
}
</style>
