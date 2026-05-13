<template>
  <div class="header-content">
    <div class="title-group">
      <span class="title-kicker">工作台</span>
      <div>
        <h1>{{ pageTitle }}</h1>
        <p>校园一卡通管理系统</p>
      </div>
    </div>
    <div class="user-info">
      <el-dropdown trigger="click">
        <button class="user-trigger" type="button">
          <span class="user-avatar">{{ userInitial }}</span>
          <span class="user-copy">
            <strong>{{ username }}</strong>
            <small>{{ roleLabel }}</small>
          </span>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const username = ref('管理员')
const role = ref('admin')

const roleMap = {
  admin: '管理员',
  teacher: '教师',
  student: '学生'
}

const pageTitle = computed(() => route.meta?.title || '首页')
const roleLabel = computed(() => roleMap[role.value] || '用户')
const userInitial = computed(() => (username.value || roleLabel.value || '用').slice(0, 1).toUpperCase())

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  username.value = user.username || '管理员'
  role.value = user.role || 'admin'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
  min-width: 0;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.title-kicker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 11px;
  color: #0f766e;
  background: #d9f2ee;
  border: 1px solid #b9e4dd;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.title-group h1,
.title-group p {
  overflow: hidden;
  margin: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-group h1 {
  color: #12313a;
  font-size: 20px;
  line-height: 1.3;
  font-weight: 800;
}

.title-group p {
  margin-top: 2px;
  color: #6b7f84;
  font-size: 12px;
}

.user-info {
  flex: 0 0 auto;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 5px 10px 5px 6px;
  color: #12313a;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid #d7e7e3;
  border-radius: 999px;
  cursor: pointer;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.user-trigger:hover,
.user-trigger:focus {
  background: #ffffff;
  border-color: #83c5bb;
  box-shadow: 0 8px 22px rgba(15, 118, 110, 0.1);
}

.user-trigger:focus-visible {
  outline: 3px solid rgba(15, 118, 110, 0.18);
  outline-offset: 2px;
}

.user-avatar {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: linear-gradient(135deg, #0b4f4a, #0f766e);
  border-radius: 999px;
  font-size: 14px;
  font-weight: 800;
}

.user-copy {
  display: grid;
  min-width: 62px;
  text-align: left;
}

.user-copy strong,
.user-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-copy strong {
  color: #12313a;
  font-size: 14px;
  line-height: 1.2;
}

.user-copy small {
  margin-top: 2px;
  color: #6b7f84;
  font-size: 12px;
}

.el-icon--right {
  color: #70868b;
}

@media (max-width: 768px) {
  .title-kicker,
  .user-copy {
    display: none;
  }

  .title-group h1 {
    font-size: 18px;
  }
}
</style>
