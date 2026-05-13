<template>
  <main class="login-page">
    <div class="campus-backdrop" aria-hidden="true">
      <span class="campus-grid"></span>
      <span class="campus-band band-one"></span>
      <span class="campus-band band-two"></span>
      <span class="campus-line line-one"></span>
      <span class="campus-line line-two"></span>
    </div>

    <section class="login-shell" aria-label="校园一卡通登录">
      <section class="brand-panel" aria-labelledby="loginTitle">
        <div class="brand-top">
          <div class="brand-badge">
            <el-icon><CreditCard /></el-icon>
            <span>Campus Card</span>
          </div>
          <div class="service-state">
            <span></span>
            服务在线
          </div>
        </div>

        <div class="brand-copy">
          <p class="eyebrow">智慧校园服务入口</p>
          <h1 id="loginTitle">校园一卡通管理系统</h1>
          <p class="brand-desc">
            覆盖开卡、充值、消费、门禁、考勤与图书借阅，让校园生活数据更清晰、业务办理更高效。
          </p>
        </div>

        <dl class="brand-metrics" aria-label="系统概览">
          <div>
            <dt>6+</dt>
            <dd>业务模块</dd>
          </div>
          <div>
            <dt>3</dt>
            <dd>角色入口</dd>
          </div>
          <div>
            <dt>24h</dt>
            <dd>校园服务</dd>
          </div>
        </dl>

        <div class="campus-card-preview" aria-label="校园一卡通示意卡片">
          <div class="preview-top">
            <div>
              <span class="preview-label">学生校园卡</span>
              <strong>2026 · ACTIVE</strong>
            </div>
            <el-icon><School /></el-icon>
          </div>
          <div class="preview-number">NO. 2026051301</div>
          <div class="preview-bottom">
            <span>余额 ¥128.60</span>
            <span>门禁 · 消费 · 借阅</span>
          </div>
        </div>

        <div class="service-list" aria-label="系统能力">
          <div class="service-item">
            <el-icon><Wallet /></el-icon>
            <div>
              <strong>账户充值</strong>
              <span>余额记录清晰可追溯</span>
            </div>
          </div>
          <div class="service-item">
            <el-icon><Location /></el-icon>
            <div>
              <strong>门禁通行</strong>
              <span>进出记录统一管理</span>
            </div>
          </div>
          <div class="service-item">
            <el-icon><DataAnalysis /></el-icon>
            <div>
              <strong>数据统计</strong>
              <span>运营数据实时汇总</span>
            </div>
          </div>
          <div class="service-item">
            <el-icon><Reading /></el-icon>
            <div>
              <strong>图书借阅</strong>
              <span>借还流程高效闭环</span>
            </div>
          </div>
        </div>
      </section>

      <section class="login-panel" aria-labelledby="formTitle">
        <div class="panel-header">
          <div class="panel-icon" aria-hidden="true">
            <el-icon><Key /></el-icon>
          </div>
          <span class="panel-kicker">安全登录</span>
          <h2 id="formTitle">欢迎回来</h2>
          <p id="loginHint">请使用管理员、教师或学生账号进入系统，系统将根据角色自动分配工作台。</p>
        </div>

        <div class="role-strip" aria-label="支持的登录角色">
          <span>管理员</span>
          <span>教师</span>
          <span>学生</span>
        </div>

        <el-form
          ref="loginFormRef"
          class="login-form"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          aria-describedby="loginHint"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              autocomplete="username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              autocomplete="current-password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            class="login-button"
            type="primary"
            size="large"
            :loading="loading"
            :disabled="loading"
            @click="handleLogin"
          >
            <span>登录系统</span>
            <el-icon v-if="!loading"><ArrowRight /></el-icon>
          </el-button>
        </el-form>

        <div class="login-note">
          <el-icon><Key /></el-icon>
          <span>登录后将根据角色自动进入对应工作台</span>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 登录时不传递角色，由后端根据账号判断角色
    const res = await login({ 
      username: loginForm.username, 
      password: loginForm.password
    })
    console.log('登录响应:', res)
    
    if (res.code === 0) {
      // 获取后端返回的角色
      const backendRole = res.data.user?.role
      
      // 转换后端角色格式（后端可能返回SUPER_ADMIN/admin/student/teacher）
      let userRole = backendRole
      if (backendRole === 'SUPER_ADMIN') {
        userRole = 'admin'
      } else if (backendRole === 'admin') {
        userRole = 'admin'
      } else if (backendRole === 'student') {
        userRole = 'student'
      } else if (backendRole === 'teacher') {
        userRole = 'teacher'
      }
      
      const userInfo = {
        ...res.data.user,
        role: userRole,
        businessUserId: res.data.businessUserId,
        cardId: res.data.cardId,
        cardNo: res.data.cardNo
      }
      console.log('用户信息:', userInfo)
      localStorage.setItem('token', 'mock-token-' + Date.now())
      localStorage.setItem('user', JSON.stringify(userInfo))
      ElMessage.success('登录成功')
      
      // 根据后端返回的角色跳转到不同页面
      console.log('角色:', userRole)
      try {
        if (userRole === 'admin') {
          console.log('准备跳转到首页')
          router.push('/')
          console.log('跳转完成')
        } else if (userRole === 'teacher') {
          console.log('准备跳转到个人中心')
          router.push('/profile')
          console.log('跳转完成')
        } else if (userRole === 'student') {
          console.log('准备跳转到个人中心')
          router.push('/profile')
          console.log('跳转完成')
        } else {
          console.log('未知角色，默认跳转到个人中心')
          router.push('/profile')
        }
      } catch (error) {
        console.error('跳转失败:', error)
        ElMessage.error('跳转失败，请刷新页面重试')
      }
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --login-primary: #0f766e;
  --login-primary-dark: #0b4f4a;
  --login-primary-soft: #d9f2ee;
  --login-accent: #c77918;
  --login-accent-soft: #fff1d6;
  --login-text: #12313a;
  --login-muted: #5b7076;
  --login-border: #d7e7e3;
  --login-surface: #ffffff;
  --login-panel: rgba(255, 255, 255, 0.94);
  --login-shadow: 0 28px 70px rgba(17, 76, 74, 0.18);
  --login-radius: 26px;
  --login-ease: 180ms ease;

  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 320px;
  min-height: 100vh;
  min-height: 100dvh;
  padding: 40px 24px;
  overflow: hidden;
  color: var(--login-text);
  background:
    linear-gradient(115deg, rgba(15, 118, 110, 0.1), transparent 36%),
    linear-gradient(245deg, rgba(199, 121, 24, 0.12), transparent 32%),
    linear-gradient(135deg, #f6fbfa 0%, #eef8f5 48%, #fff8ec 100%);
  isolation: isolate;
}

.campus-backdrop {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.campus-grid,
.campus-band,
.campus-line {
  position: absolute;
  display: block;
}

.campus-grid {
  inset: 0;
  background:
    linear-gradient(90deg, rgba(15, 118, 110, 0.08) 1px, transparent 1px),
    linear-gradient(0deg, rgba(15, 118, 110, 0.08) 1px, transparent 1px);
  background-size: 76px 76px;
  mask-image: linear-gradient(135deg, transparent 0%, #000 24%, #000 70%, transparent 100%);
  opacity: 0.5;
}

.campus-backdrop::before {
  position: absolute;
  right: -7vw;
  bottom: -110px;
  width: 58vw;
  min-width: 620px;
  height: 350px;
  content: "";
  background:
    linear-gradient(90deg, rgba(12, 83, 78, 0.12) 1px, transparent 1px) 0 100% / 72px 74px,
    linear-gradient(0deg, rgba(12, 83, 78, 0.12) 1px, transparent 1px) 0 100% / 72px 74px,
    linear-gradient(135deg, transparent 18%, rgba(15, 118, 110, 0.16) 18% 22%, transparent 22% 42%, rgba(15, 118, 110, 0.1) 42% 46%, transparent 46%);
  clip-path: polygon(0 45%, 12% 45%, 12% 30%, 21% 30%, 21% 45%, 34% 45%, 34% 18%, 48% 18%, 48% 45%, 62% 45%, 62% 26%, 74% 26%, 74% 45%, 100% 45%, 100% 100%, 0 100%);
  opacity: 0.72;
}

.campus-backdrop::after {
  position: absolute;
  top: 14%;
  left: 8%;
  width: 240px;
  height: 240px;
  content: "";
  border: 1px solid rgba(15, 118, 110, 0.14);
  border-radius: 28px;
  transform: rotate(12deg);
}

.campus-band {
  height: 110px;
  background: linear-gradient(90deg, transparent, rgba(15, 118, 110, 0.11), rgba(199, 121, 24, 0.1), transparent);
  transform: rotate(-16deg);
}

.band-one {
  top: 18%;
  left: -8%;
  width: 62vw;
}

.band-two {
  right: -14%;
  bottom: 12%;
  width: 54vw;
}

.campus-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(15, 118, 110, 0.24), transparent);
  transform: rotate(-16deg);
}

.line-one {
  top: 26%;
  left: 38%;
  width: 340px;
}

.line-two {
  right: 18%;
  bottom: 20%;
  width: 420px;
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.04fr) minmax(360px, 440px);
  width: min(1120px, 100%);
  min-height: 640px;
  overflow: hidden;
  background: var(--login-panel);
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: var(--login-radius);
  box-shadow: var(--login-shadow);
  backdrop-filter: blur(18px);
}

.brand-panel,
.login-panel {
  position: relative;
  padding: 46px;
}

.brand-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 28px;
  overflow: hidden;
  color: #f7fffd;
  background:
    linear-gradient(145deg, rgba(10, 76, 76, 0.98), rgba(15, 118, 110, 0.96) 58%, rgba(178, 99, 18, 0.92));
}

.brand-panel::before {
  position: absolute;
  inset: 26px;
  content: "";
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 20px;
  pointer-events: none;
}

.brand-panel::after {
  position: absolute;
  right: -120px;
  bottom: -48px;
  width: 420px;
  height: 220px;
  content: "";
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.12) 1px, transparent 1px),
    linear-gradient(0deg, rgba(255, 255, 255, 0.12) 1px, transparent 1px);
  background-size: 34px 34px;
  transform: rotate(-12deg);
}

.brand-top,
.brand-copy,
.brand-metrics,
.campus-card-preview,
.service-list {
  position: relative;
  z-index: 1;
}

.brand-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.brand-badge,
.service-state,
.role-strip span {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
}

.brand-badge {
  width: fit-content;
  gap: 10px;
  padding: 9px 14px;
  color: #ecfffb;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
}

.brand-badge .el-icon {
  font-size: 20px;
}

.service-state {
  gap: 8px;
  padding: 0 12px;
  color: #fef7e8;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.service-state span {
  width: 8px;
  height: 8px;
  background: #a7f3d0;
  border-radius: 999px;
  box-shadow: 0 0 0 4px rgba(167, 243, 208, 0.18);
}

.eyebrow,
.panel-kicker {
  margin: 0 0 12px;
  color: var(--login-accent);
  font-size: 13px;
  font-weight: 800;
}

.eyebrow {
  color: #ffe0a8;
}

.brand-copy h1 {
  max-width: 520px;
  margin: 0;
  color: #ffffff;
  font-size: 42px;
  line-height: 1.18;
  font-weight: 800;
}

.brand-desc {
  max-width: 560px;
  margin: 18px 0 0;
  color: rgba(247, 255, 253, 0.9);
  font-size: 17px;
  line-height: 1.75;
}

.brand-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  width: min(460px, 100%);
  margin: 0;
}

.brand-metrics div {
  padding: 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.17);
  border-radius: 14px;
}

.brand-metrics dt {
  color: #ffffff;
  font-size: 24px;
  line-height: 1.1;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.brand-metrics dd {
  margin: 7px 0 0;
  color: rgba(247, 255, 253, 0.78);
  font-size: 13px;
}

.campus-card-preview {
  width: min(390px, 100%);
  padding: 22px;
  color: var(--login-text);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(235, 250, 246, 0.94)),
    linear-gradient(90deg, rgba(255, 241, 214, 0.85), transparent);
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 18px;
  box-shadow: 0 18px 42px rgba(9, 46, 49, 0.22);
}

.preview-top,
.preview-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.preview-top .el-icon {
  color: var(--login-primary);
  font-size: 34px;
}

.preview-label,
.preview-bottom {
  color: #506a70;
  font-size: 13px;
}

.preview-top strong {
  display: block;
  margin-top: 4px;
  color: var(--login-text);
  font-size: 15px;
}

.preview-number {
  margin: 34px 0 22px;
  color: #102f36;
  font-size: 22px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  max-width: 520px;
}

.service-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-height: 72px;
  padding: 12px 14px;
  color: #f5fffd;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
}

.service-item .el-icon {
  display: inline-flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  color: #ffe1a8;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  font-size: 20px;
}

.service-item strong,
.service-item span {
  display: block;
}

.service-item strong {
  font-size: 14px;
  line-height: 1.4;
}

.service-item span {
  margin-top: 3px;
  color: rgba(247, 255, 253, 0.72);
  font-size: 12px;
  line-height: 1.45;
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 252, 251, 0.94));
}

.panel-header {
  position: relative;
  padding-top: 2px;
}

.panel-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  color: var(--login-primary);
  background: var(--login-primary-soft);
  border-radius: 14px;
  font-size: 22px;
}

.panel-header h2 {
  margin: 0;
  color: var(--login-text);
  font-size: 30px;
  line-height: 1.25;
  font-weight: 800;
}

.panel-header p {
  margin: 12px 0 0;
  color: var(--login-muted);
  font-size: 15px;
  line-height: 1.7;
}

.role-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 22px;
}

.role-strip span {
  justify-content: center;
  color: #31565c;
  background: #f3f8f7;
  border: 1px solid var(--login-border);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #233f45;
  font-size: 14px;
  font-weight: 800;
  line-height: 1.4;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 50px;
  padding: 0 12px 0 14px;
  background: #f8fbfa;
  border: 1px solid var(--login-border);
  border-radius: 14px;
  box-shadow: none;
  transition: border-color var(--login-ease), box-shadow var(--login-ease), background var(--login-ease);
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #83c5bb;
  background: #ffffff;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--login-primary);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.14);
}

.login-form :deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: var(--login-primary);
}

.login-form :deep(.el-input__inner) {
  min-height: 46px;
  color: var(--login-text);
  font-size: 16px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #8ba0a4;
}

.login-form :deep(.el-input__prefix),
.login-form :deep(.el-input__suffix) {
  color: #789096;
}

.login-form :deep(.el-input__password),
.login-form :deep(.el-input__clear) {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
}

.login-form :deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #d03050;
  background: #fffafa;
}

.login-form :deep(.el-form-item__error) {
  padding-top: 6px;
  color: #b4233a;
  font-weight: 700;
}

.login-button {
  width: 100%;
  min-height: 50px;
  margin-top: 6px;
  border: 0;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--login-primary-dark), var(--login-primary));
  box-shadow: 0 14px 30px rgba(15, 118, 110, 0.26);
  font-size: 16px;
  font-weight: 800;
  transition: transform var(--login-ease), box-shadow var(--login-ease), filter var(--login-ease);
  touch-action: manipulation;
}

.login-button :deep(span) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-button:hover,
.login-button:focus {
  background: linear-gradient(135deg, #0a4744, #0d665f);
  box-shadow: 0 16px 34px rgba(15, 118, 110, 0.34);
  filter: saturate(1.04);
}

.login-button:focus-visible {
  outline: 3px solid rgba(15, 118, 110, 0.28);
  outline-offset: 3px;
}

.login-button:active {
  transform: translateY(1px);
}

.login-button.is-disabled {
  opacity: 0.72;
  transform: none;
}

.login-note {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  margin-top: 22px;
  padding: 13px 14px;
  color: #526a70;
  background: var(--login-accent-soft);
  border: 1px solid #f3d8a9;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
}

.login-note .el-icon {
  flex: 0 0 auto;
  margin-top: 2px;
  color: var(--login-accent);
}

@media (max-width: 980px) {
  .login-page {
    align-items: flex-start;
    padding: 28px 18px;
    overflow-y: auto;
  }

  .login-shell {
    grid-template-columns: 1fr;
    min-height: 0;
    border-radius: 22px;
  }

  .brand-panel {
    padding: 34px;
  }

  .brand-panel::before {
    inset: 18px;
  }

  .brand-copy h1 {
    font-size: 34px;
  }

  .brand-metrics,
  .campus-card-preview,
  .service-list {
    width: 100%;
    max-width: none;
  }

  .login-panel {
    padding: 34px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 12px;
  }

  .campus-backdrop::before,
  .campus-backdrop::after,
  .campus-band,
  .campus-line {
    display: none;
  }

  .login-shell {
    border-radius: 18px;
  }

  .brand-panel,
  .login-panel {
    padding: 22px 18px;
  }

  .brand-panel {
    gap: 18px;
  }

  .brand-panel::before,
  .brand-panel::after {
    display: none;
  }

  .brand-top {
    align-items: flex-start;
  }

  .service-state {
    min-height: 40px;
    padding: 0 10px;
  }

  .brand-copy h1 {
    font-size: 27px;
  }

  .brand-desc {
    margin-top: 10px;
    font-size: 15px;
    line-height: 1.65;
  }

  .brand-metrics,
  .campus-card-preview,
  .service-list {
    display: none;
  }

  .panel-icon {
    width: 44px;
    height: 44px;
    margin-bottom: 14px;
  }

  .panel-header h2 {
    font-size: 24px;
  }

  .role-strip {
    margin-top: 18px;
  }

  .role-strip span {
    min-height: 40px;
  }

  .login-form {
    margin-top: 22px;
  }

  .login-form :deep(.el-form-item) {
    margin-bottom: 17px;
  }

  .login-note {
    margin-top: 18px;
  }
}

@media (max-width: 390px) {
  .brand-copy h1 {
    font-size: 25px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-button,
  .login-form :deep(.el-input__wrapper) {
    transition: none;
  }
}
</style>
