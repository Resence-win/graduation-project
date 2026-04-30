<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>校园一卡通管理系统</h2>
          <p>用户登录</p>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
