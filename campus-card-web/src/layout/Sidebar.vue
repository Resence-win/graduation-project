<template>
  <el-menu
    :default-active="activeMenu"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
  >
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
      <el-menu-item index="/book">图书管理</el-menu-item>
      <el-menu-item index="/access">门禁管理</el-menu-item>
      <el-menu-item index="/attendance">考勤管理</el-menu-item>
      <el-menu-item index="/commute">通勤车管理</el-menu-item>
      <el-menu-item index="/log">系统日志</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, User, ShoppingCart, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>
