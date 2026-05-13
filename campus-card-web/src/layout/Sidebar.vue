<template>
  <div class="sidebar-shell">
    <div class="sidebar-brand">
      <div class="brand-mark">
        <el-icon><CreditCard /></el-icon>
      </div>
      <div class="brand-copy">
        <strong>校园一卡通</strong>
        <span>Campus Card</span>
      </div>
    </div>

    <div class="role-card">
      <span class="role-pill">{{ roleMeta.label }}</span>
      <strong>{{ username }}</strong>
      <small>{{ roleMeta.desc }}</small>
    </div>

    <el-scrollbar class="sidebar-scroll">
      <el-menu
        class="sidebar-menu"
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#d7e7e3"
        active-text-color="#ffffff"
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
            <el-menu-item index="/product">商品管理</el-menu-item>
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
              <el-menu-item index="/book/borrow-record">借阅记录</el-menu-item>
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
              <el-menu-item index="/attendance/application">申报审批</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/commute">通勤车管理</el-menu-item>
            <el-menu-item index="/college-major">学院专业管理</el-menu-item>
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
          <el-menu-item v-if="userRole === 'student'" index="/product/student">
            <el-icon><ShoppingCart /></el-icon>
            <span>商品消费</span>
          </el-menu-item>
          <!-- 老师菜单 -->
          <template v-if="userRole === 'teacher'">
            <el-menu-item index="/attendance/location">
              <el-icon><Location /></el-icon>
              <span>打卡位置管理</span>
            </el-menu-item>
            <el-menu-item index="/attendance/application">
              <el-icon><Ticket /></el-icon>
              <span>申报审批</span>
            </el-menu-item>
          </template>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { CreditCard, HomeFilled, User, ShoppingCart, Setting, Ticket, Location, Van } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const userRole = ref('admin') // 默认角色
const username = ref('管理员')

const roleText = {
  admin: { label: '管理员端', desc: '校园业务运营工作台' },
  teacher: { label: '教师端', desc: '考勤与申报审批入口' },
  student: { label: '学生端', desc: '校园服务自助入口' }
}

const roleMeta = computed(() => roleText[userRole.value] || roleText.admin)

// 获取用户角色
const getUserRole = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userRole.value = user.role || 'admin'
    username.value = user.username || roleMeta.value.label
  }
}

onMounted(() => {
  getUserRole()
})
</script>

<style scoped>
.sidebar-shell {
  position: relative;
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  color: #f7fffd;
  background:
    radial-gradient(circle at 18% 8%, rgba(45, 212, 191, 0.2), transparent 28%),
    linear-gradient(180deg, #0a4c4c 0%, #123f45 54%, #0b3038 100%);
}

.sidebar-shell::after {
  position: absolute;
  right: -82px;
  bottom: -56px;
  width: 220px;
  height: 220px;
  content: "";
  border: 34px solid rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  pointer-events: none;
}

.sidebar-brand,
.role-card,
.sidebar-scroll {
  position: relative;
  z-index: 1;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 18px 16px;
}

.brand-mark {
  display: inline-flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  color: #fff3d8;
  background: rgba(255, 255, 255, 0.13);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  font-size: 22px;
}

.brand-copy {
  min-width: 0;
}

.brand-copy strong,
.brand-copy span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-copy strong {
  color: #ffffff;
  font-size: 17px;
  line-height: 1.35;
  font-weight: 800;
}

.brand-copy span {
  margin-top: 2px;
  color: rgba(247, 255, 253, 0.68);
  font-size: 12px;
}

.role-card {
  margin: 0 14px 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 16px;
  box-shadow: 0 14px 30px rgba(3, 28, 32, 0.18);
}

.role-pill {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 9px;
  color: #fff3d8;
  background: rgba(199, 121, 24, 0.2);
  border: 1px solid rgba(255, 224, 168, 0.3);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.role-card strong,
.role-card small {
  display: block;
}

.role-card strong {
  margin-top: 10px;
  color: #ffffff;
  font-size: 15px;
}

.role-card small {
  margin-top: 4px;
  color: rgba(247, 255, 253, 0.66);
  font-size: 12px;
  line-height: 1.5;
}

.sidebar-scroll {
  flex: 1;
  min-height: 0;
}

.sidebar-menu {
  padding: 2px 10px 18px;
  border-right: 0;
}

.sidebar-menu :deep(.el-menu) {
  border-right: 0;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 44px;
  margin: 4px 0;
  border-radius: 12px;
  color: rgba(247, 255, 253, 0.74);
  font-size: 14px;
  font-weight: 700;
  line-height: 44px;
  transition: color 180ms ease, background 180ms ease, box-shadow 180ms ease;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.95), rgba(20, 184, 166, 0.8));
  box-shadow: 0 10px 24px rgba(15, 118, 110, 0.28);
}

.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.11);
}

.sidebar-menu :deep(.el-icon) {
  flex: 0 0 auto;
  color: currentColor;
  font-size: 18px;
}

.sidebar-menu :deep(.el-menu--inline) {
  background: transparent;
}

.sidebar-menu :deep(.el-menu--inline .el-menu-item),
.sidebar-menu :deep(.el-menu--inline .el-sub-menu__title) {
  height: 40px;
  margin: 3px 0;
  color: rgba(247, 255, 253, 0.66);
  font-size: 13px;
  line-height: 40px;
}

.sidebar-menu :deep(.el-menu--inline .el-menu-item.is-active) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.14);
  box-shadow: inset 3px 0 0 #ffe0a8;
}

.sidebar-menu :deep(.el-sub-menu__icon-arrow) {
  color: rgba(247, 255, 253, 0.58);
}

.sidebar-menu :deep(.el-menu-item:focus),
.sidebar-menu :deep(.el-sub-menu__title:focus) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.12);
}

@media (prefers-reduced-motion: reduce) {
  .sidebar-menu :deep(.el-menu-item),
  .sidebar-menu :deep(.el-sub-menu__title) {
    transition: none;
  }
}
</style>
