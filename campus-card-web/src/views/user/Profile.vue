<template>
  <div class="user-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ userInfo.role === 'student' ? '学生个人中心' : '教师个人中心' }}</span>
          <el-button type="danger" @click="logout">退出登录</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userInfo" label-width="100px">
            <el-form-item label="姓名">
              <el-input v-model="userInfo.name" disabled />
            </el-form-item>
            <el-form-item label="学号/工号">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="性别">
              <el-input v-model="userInfo.gender" disabled />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="userInfo.phone" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" disabled />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="校园卡信息" name="card">
          <el-form :model="cardInfo" label-width="100px">
            <el-form-item label="卡号">
              <el-input v-model="cardInfo.cardNo" disabled />
            </el-form-item>
            <el-form-item label="卡状态">
              <el-tag :type="cardInfo.status === 1 ? 'success' : cardInfo.status === 2 ? 'warning' : 'danger'">
                {{ cardInfo.status === 1 ? '正常' : cardInfo.status === 2 ? '挂失' : '注销' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="余额">
              <el-input v-model="cardInfo.balance" disabled />
            </el-form-item>
            <el-form-item label="开卡时间">
              <el-input v-model="cardInfo.createTime" disabled />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="消费记录" name="consume">
          <el-form :inline="true" :model="consumeForm" class="mb-4">
            <el-form-item label="开始日期">
              <el-date-picker v-model="consumeForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker v-model="consumeForm.endDate" type="date" placeholder="选择结束日期" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadConsumeData">查询</el-button>
              <el-button @click="resetConsumeForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="consumeList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantName" label="商户名称" width="180" />
            <el-table-column prop="amount" label="消费金额" width="120" />
            <el-table-column prop="balanceAfter" label="消费后余额" width="120" />
            <el-table-column prop="consumeTime" label="消费时间" width="180" />
          </el-table>
          
          <el-pagination
            v-model:current-page="consumePagination.page"
            v-model:page-size="consumePagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="consumePagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleConsumeSizeChange"
            @current-change="handleConsumeCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="借阅记录" name="borrow" v-if="userInfo.role === 'student'">
          <el-table :data="borrowList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bookName" label="书名" width="200" />
            <el-table-column prop="borrowTime" label="借阅时间" width="180" />
            <el-table-column prop="returnTime" label="归还时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'warning' : 'success'">
                  {{ row.status === 1 ? '已借阅' : '已归还' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="borrowPagination.page"
            v-model:page-size="borrowPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="borrowPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleBorrowSizeChange"
            @current-change="handleBorrowCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStudentByNo } from '@/api/student'
import { getTeacherByNo } from '@/api/teacher'
import { getCardInfo, getCardByUserNo } from '@/api/card'
import { getConsumeList } from '@/api/consume'
import { getBorrowList } from '@/api/book'

const router = useRouter()

const activeTab = ref('info')
const userInfo = reactive({
  role: '',
  name: '',
  username: '',
  gender: '',
  phone: '',
  email: ''
})

const cardInfo = reactive({
  cardNo: '',
  status: 1,
  balance: '0.00',
  createTime: ''
})

const consumeForm = reactive({
  startDate: '',
  endDate: ''
})

const consumeList = ref([])
const consumePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const borrowList = ref([])
const borrowPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadUserInfo = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      userInfo.role = user.role
      userInfo.username = user.username
      
      // 根据角色获取详细信息
      if (user.role === 'student') {
        const res = await getStudentByNo(user.username)
        if (res.code === 0) {
          Object.assign(userInfo, res.data)
        }
      } else if (user.role === 'teacher') {
        const res = await getTeacherByNo(user.username)
        if (res.code === 0) {
          Object.assign(userInfo, res.data)
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadCardInfo = async () => {
  try {
    // 使用新的接口根据用户编号和类型查询校园卡信息
    if (userInfo.username && userInfo.role) {
      const res = await getCardByUserNo(userInfo.username, userInfo.role)
      if (res.code === 0) {
        // 转换开卡时间格式
        if (res.data.createTime) {
          res.data.createTime = new Date(res.data.createTime).toLocaleString()
        }
        Object.assign(cardInfo, res.data)
      }
    }
  } catch (error) {
    console.error('加载校园卡信息失败:', error)
  }
}

const loadConsumeData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getConsumeList({
        card_id: cardInfo.id,
        page: consumePagination.page,
        size: consumePagination.size
      })
      if (res.code === 0) {
        consumeList.value = res.data.records || []
        consumePagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载消费记录失败:', error)
  }
}

const loadBorrowData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getBorrowList({
        card_id: cardInfo.id,
        page: borrowPagination.page,
        size: borrowPagination.size
      })
      if (res.code === 0) {
        borrowList.value = res.data.records || []
        borrowPagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载借阅记录失败:', error)
  }
}

const resetConsumeForm = () => {
  consumeForm.startDate = ''
  consumeForm.endDate = ''
  loadConsumeData()
}

const handleConsumeSizeChange = (val) => {
  consumePagination.size = val
  loadConsumeData()
}

const handleConsumeCurrentChange = (val) => {
  consumePagination.page = val
  loadConsumeData()
}

const handleBorrowSizeChange = (val) => {
  borrowPagination.size = val
  loadBorrowData()
}

const handleBorrowCurrentChange = (val) => {
  borrowPagination.page = val
  loadBorrowData()
}

const logout = () => {
  // 清除本地存储中的用户信息和token
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出登录成功')
  // 跳转到登录页面
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
  loadCardInfo()
  loadConsumeData()
  if (userInfo.role === 'student') {
    loadBorrowData()
  }
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>