<template>
  <el-container class="main-container">
    <el-aside width="224px" class="sidebar">
      <Sidebar />
    </el-aside>
    <el-container class="content-container">
      <el-header class="header">
        <Header />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style scoped>
.main-container {
  height: 100vh;
  min-width: 0;
  background:
    radial-gradient(circle at 82% 8%, rgba(20, 184, 166, 0.12), transparent 28%),
    linear-gradient(135deg, #f6fbfa 0%, #eef5f3 48%, #fff8ec 100%);
}

.sidebar {
  position: relative;
  z-index: 2;
  color: #fff;
  box-shadow: 12px 0 32px rgba(10, 76, 76, 0.12);
}

.content-container {
  min-width: 0;
  min-height: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.86);
  border-bottom: 1px solid rgba(215, 231, 227, 0.9);
  box-shadow: 0 10px 24px rgba(18, 49, 58, 0.04);
  backdrop-filter: blur(18px);
}

.main-content {
  min-width: 0;
  min-height: 0;
  padding: 24px 28px 32px;
  overflow: auto;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.44), rgba(255, 255, 255, 0)),
    transparent;
}

@media (max-width: 1024px) {
  .header {
    padding: 0 22px;
  }

  .main-content {
    padding: 20px 22px 28px;
  }
}

@media (max-width: 768px) {
  .header {
    height: 64px;
    padding: 0 16px;
  }

  .main-content {
    padding: 16px;
  }
}
</style>
