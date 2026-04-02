<template>
  <el-menu
    :default-active="activeMenu"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
  >
    <!-- 管理员菜单 -->
    <template v-if="userRole === 'admin'">
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      
      <el-sub-menu index="user">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/student">学生管理</el-menu-item>
        <el-menu-item index="/teacher">教师管理</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="business">
        <template #title>
          <el-icon><ShoppingCart /></el-icon>
          <span>业务管理</span>
        </template>
        <el-menu-item index="/merchant">商户管理</el-menu-item>
        <el-menu-item index="/card">校园卡管理</el-menu-item>
        <el-menu-item index="/account">账户管理</el-menu-item>
        <el-menu-item index="/recharge">充值管理</el-menu-item>
        <el-menu-item index="/consume">消费管理</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/statistics">数据统计</el-menu-item>
          <el-sub-menu index="book">
            <template #title>
              <span>图书管理</span>
            </template>
            <el-menu-item index="/book">图书列表</el-menu-item>
            <el-menu-item index="/book/application">借阅申请</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="access">
            <template #title>
              <span>门禁管理</span>
            </template>
            <el-menu-item index="/access">门禁记录</el-menu-item>
            <el-menu-item index="/access/point">门禁点管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="attendance">
            <template #title>
              <span>考勤管理</span>
            </template>
            <el-menu-item index="/attendance">考勤记录</el-menu-item>
            <el-menu-item index="/attendance/location">打卡位置管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/commute">通勤车管理</el-menu-item>
          <el-menu-item index="/log">系统日志</el-menu-item>
        </el-sub-menu>
    </template>
    
    <!-- 学生和教师菜单 -->
    <template v-else>
      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        <span>个人中心</span>
      </el-menu-item>
      <el-menu-item index="/commute/student">
        <el-icon><Van /></el-icon>
        <span>通勤车查询</span>
      </el-menu-item>
      <!-- 老师菜单 -->
      <template v-if="userRole === 'teacher'">
        <el-menu-item index="/attendance/location">
          <el-icon><Location /></el-icon>
          <span>打卡位置管理</span>
        </el-menu-item>
      </template>
    </template>
  </el-menu>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, User, ShoppingCart, Setting, Ticket, Location, Van } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const userRole = ref('admin') // 默认角色

// 获取用户角色
const getUserRole = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userRole.value = user.role || 'admin'
  }
}

onMounted(() => {
  getUserRole()
})
</script>
