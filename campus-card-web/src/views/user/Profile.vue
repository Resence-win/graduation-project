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
        
        <el-tab-pane label="图书借阅" name="book" v-if="userInfo.role === 'student'">
          <el-form :inline="true" :model="bookSearchForm" class="mb-4">
            <el-form-item label="书名">
              <el-input v-model="bookSearchForm.bookName" placeholder="请输入书名" clearable />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="bookSearchForm.author" placeholder="请输入作者" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadBookData">查询</el-button>
              <el-button @click="resetBookSearchForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="bookList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bookName" label="书名" width="200" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="logo" label="封面" width="100">
              <template #default="{ row }">
                <el-image
                  v-if="row.logo"
                  :src="'/api' + row.logo"
                  style="width: 50px; height: 50px"
                  :preview-src-list="['/api' + row.logo]"
                />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                  {{ row.status === 1 ? '可借阅' : '已借出' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <template v-if="hasPendingApplication(row.id)">
                  <el-button size="small" type="info" disabled>申请中</el-button>
                </template>
                <template v-else>
                  <el-button size="small" type="primary" @click="handleBorrow(row)" :disabled="row.status !== 1">借阅</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="bookPagination.page"
            v-model:page-size="bookPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="bookPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleBookSizeChange"
            @current-change="handleBookCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="借阅记录" name="borrow" v-if="userInfo.role === 'student'">
          <el-table :data="borrowList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bookName" label="书名" width="200" />
            <el-table-column prop="borrowTime" label="借阅时间" width="180" />
            <el-table-column prop="dueTime" label="到期时间" width="180" />
            <el-table-column prop="returnTime" label="归还时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'warning' : 'success'">
                  {{ row.status === 1 ? '借阅中' : '已归还' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleReturn(row)" :disabled="row.status !== 1">归还</el-button>
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

      <!-- 借阅对话框 -->
      <el-dialog
        v-model="borrowDialogVisible"
        title="图书借阅"
        width="400px"
      >
        <el-form :model="borrowForm" label-width="80px">
          <el-form-item label="借阅天数">
            <el-select v-model="borrowForm.borrowDays" placeholder="请选择借阅天数">
              <el-option
                v-for="option in borrowDaysOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="自定义天数" v-if="borrowForm.borrowDays === 0">
            <el-input v-model="borrowForm.customDays" type="number" placeholder="请输入借阅天数" min="1" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="borrowDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmitBorrowApplication">提交申请</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElDatePicker, ElButton } from 'element-plus'
import { getStudentByNo } from '@/api/student'
import { getTeacherByNo } from '@/api/teacher'
import { getCardInfo, getCardByUserNo } from '@/api/card'
import { getConsumeList } from '@/api/consume'
import { getBorrowList, getBookList, submitBorrowApplication, getActiveBorrowCount, getBorrowApplications, returnBook } from '@/api/book'

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

const bookList = ref([])
const bookPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 借阅申请列表
const borrowApplications = ref([])
const loadBorrowApplications = async () => {
  try {
    if (!cardInfo.id) return
    
    const res = await getBorrowApplications({
      card_id: cardInfo.id
    })
    if (res.code === 0) {
      borrowApplications.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载借阅申请列表失败:', error)
  }
}

// 检查图书是否有未处理的借阅申请
const hasPendingApplication = (bookId) => {
  return borrowApplications.value.some(app => app.bookId === bookId && app.status === 1)
}

const bookSearchForm = reactive({
  bookName: '',
  author: ''
})

const borrowDialogVisible = ref(false)
const currentBook = ref(null)
const borrowForm = reactive({
  borrowDays: 7,
  customDays: ''
})
const borrowDaysOptions = [
  { value: 7, label: '7天' },
  { value: 15, label: '15天' },
  { value: 30, label: '30天' },
  { value: 0, label: '自定义' }
]

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
        start_date: consumeForm.startDate ? new Date(consumeForm.startDate).toISOString().split('T')[0] : '',
        end_date: consumeForm.endDate ? new Date(consumeForm.endDate).toISOString().split('T')[0] : '',
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

const loadBookData = async () => {
  try {
    const res = await getBookList({
      bookName: bookSearchForm.bookName,
      author: bookSearchForm.author,
      page: bookPagination.page,
      size: bookPagination.size
    })
    if (res.code === 0) {
      bookList.value = res.data.records || []
      bookPagination.total = res.data.total || 0
    }
    // 加载借阅申请列表
    await loadBorrowApplications()
  } catch (error) {
    console.error('加载图书列表失败:', error)
  }
}

const resetBookSearchForm = () => {
  bookSearchForm.bookName = ''
  bookSearchForm.author = ''
  loadBookData()
}

const handleBookSizeChange = (val) => {
  bookPagination.size = val
  loadBookData()
}

const handleBookCurrentChange = (val) => {
  bookPagination.page = val
  loadBookData()
}

const handleBorrow = (book) => {
  currentBook.value = book
  borrowDialogVisible.value = true
}

const handleSubmitBorrowApplication = async () => {
  try {
    if (!cardInfo.id || !currentBook.value) {
      ElMessage.error('请先加载校园卡信息')
      return
    }

    // 检查当前借阅数量
    const countRes = await getActiveBorrowCount({ card_id: cardInfo.id })
    if (countRes.code === 0 && countRes.data >= 3) {
      ElMessage.error('您当前已借阅3本图书，无法继续借阅')
      return
    }

    let borrowDays = borrowForm.borrowDays
    if (borrowForm.borrowDays === 0) {
      if (!borrowForm.customDays || isNaN(borrowForm.customDays) || borrowForm.customDays <= 0) {
        ElMessage.error('请输入有效的自定义借阅天数')
        return
      }
      borrowDays = parseInt(borrowForm.customDays)
    }

    const res = await submitBorrowApplication({
      cardId: cardInfo.id,
      bookId: currentBook.value.id,
      borrowDays: borrowDays
    })

    if (res.code === 0) {
      ElMessage.success('借阅申请提交成功，请等待管理员审批')
      borrowDialogVisible.value = false
      // 重新加载图书列表和借阅申请列表
      await loadBookData()
    } else {
      ElMessage.error(res.message || '借阅申请提交失败')
    }
  } catch (error) {
    console.error('提交借阅申请失败:', error)
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('提交借阅申请失败，请稍后重试')
    }
  }
}

const handleReturn = async (borrowRecord) => {
  try {
    const res = await returnBook({ borrow_id: borrowRecord.id })
    if (res.code === 0) {
      ElMessage.success('图书归还成功')
      loadBorrowData()
      loadBookData() // 重新加载图书列表，更新图书状态
    } else {
      ElMessage.error(res.message || '图书归还失败')
    }
  } catch (error) {
    console.error('归还图书失败:', error)
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('归还图书失败，请稍后重试')
    }
  }
}

const logout = () => {
  // 清除本地存储中的用户信息和token
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出登录成功')
  // 跳转到登录页面
  router.push('/login')
}

onMounted(async () => {
  await loadUserInfo()
  await loadCardInfo()
  loadConsumeData()
  if (userInfo.role === 'student') {
    loadBorrowData()
    loadBookData()
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