<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="学生总数" :value="1000" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="教师总数" :value="200" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="商户总数" :value="50" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日消费" :value="12500.5" :precision="2" prefix="¥" />
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>欢迎登录校园一卡通管理系统</span>
            </div>
          </template>
          <p>系统功能模块：</p>
          <ul>
            <li>用户管理：学生、教师信息管理</li>
            <li>业务管理：商户、校园卡、账户、充值、消费管理</li>
            <li>系统管理：数据统计、图书、门禁、考勤、通勤车、日志</li>
          </ul>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <el-button type="primary" @click="$router.push('/student')">学生管理</el-button>
          <el-button type="success" @click="$router.push('/card')">校园卡管理</el-button>
          <el-button type="warning" @click="$router.push('/recharge')">充值管理</el-button>
          <el-button type="danger" @click="$router.push('/statistics')">数据统计</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}
</style>
