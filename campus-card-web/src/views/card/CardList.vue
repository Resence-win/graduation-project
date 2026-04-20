<template>
  <div class="card-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>校园卡管理</span>
          <el-button type="primary" @click="handleOpen">开卡</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.cardNo" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="挂失" :value="2" />
            <el-option label="注销" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardNo" label="卡号" width="150" />
        <el-table-column prop="userNo" label="学号/教师编号" width="150" />
        <el-table-column prop="userName" label="姓名" width="120" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.userType === 'student' ? 'primary' : 'success'">
              {{ row.userType === 'student' ? '学生' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueDate" label="发卡日期" width="120" />
        <el-table-column prop="expireDate" label="过期日期" width="120" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 1"
              size="small"
              type="warning"
              @click="handleLoss(row)"
            >
              挂失
            </el-button>
            <el-button
              v-if="row.status === 2"
              size="small"
              type="success"
              @click="handleUnloss(row)"
            >
              解挂
            </el-button>
            <el-button
              v-if="row.status !== 0"
              size="small"
              type="danger"
              @click="handleCancel(row)"
            >
              注销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      title="开卡"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学号/教师编号" prop="userNo">
          <el-input v-model="form.userNo" placeholder="请输入学号或教师编号" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择用户类型" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item label="开卡原因" prop="remark">
          <el-select v-model="form.remark" placeholder="请选择开卡原因" style="width: 100%">
            <el-option label="新生入校" value="新生入校" />
            <el-option label="教师入职" value="教师入职" />
            <el-option label="补办新卡" value="补办新卡" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="operationDialogVisible"
      :title="operationTitle"
      width="500px"
    >
      <el-form :model="operationForm" ref="operationFormRef" label-width="100px">
        <el-form-item label="操作原因" prop="remark">
          <el-select 
            v-if="operationType === 'loss' || operationType === 'unloss'"
            v-model="operationForm.remark" 
            placeholder="请选择操作原因" 
            style="width: 100%"
          >
            <el-option 
              v-if="operationType === 'loss'" 
              label="卡片丢失" 
              value="卡片丢失" 
            />
            <el-option 
              v-if="operationType === 'loss'" 
              label="卡片被盗" 
              value="卡片被盗" 
            />
            <el-option 
              v-if="operationType === 'loss'" 
              label="其他原因" 
              value="其他原因" 
            />
            <el-option 
              v-if="operationType === 'unloss'" 
              label="找回卡片" 
              value="找回卡片" 
            />
            <el-option 
              v-if="operationType === 'unloss'" 
              label="其他原因" 
              value="其他原因" 
            />
          </el-select>
          <el-select 
            v-if="operationType === 'cancel'"
            v-model="operationForm.remark" 
            placeholder="请选择注销原因" 
            style="width: 100%"
          >
            <el-option label="学生毕业" value="学生毕业" />
            <el-option label="教师离职" value="教师离职" />
            <el-option label="合同到期" value="合同到期" />
            <el-option label="学生转学" value="学生转学" />
            <el-option label="学生辍学" value="学生辍学" />
            <el-option label="其他特殊情况" value="其他特殊情况" />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="operationType === 'cancel'"
          title="注意：在有效期内注销校园卡需要填写注销原因"
          type="warning"
          :closable="false"
          style="margin-bottom: 10px"
        />
      </el-form>
      <template #footer>
        <el-button @click="operationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOperationSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCardList, openCard, getCardInfo, lossCard, unlossCard, cancelCard } from '@/api/card'

const formRef = ref(null)
const operationFormRef = ref(null)
const dialogVisible = ref(false)
const operationDialogVisible = ref(false)
const tableData = ref([])

const searchForm = reactive({
  cardNo: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  userNo: '',
  userType: 'student',
  remark: ''
})

const operationForm = reactive({
  cardId: null,
  remark: ''
})

const operationType = ref('')
const operationTitle = ref('')

const rules = {
  userNo: [
    { required: true, message: '请输入学号或教师编号', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请选择开卡原因', trigger: 'change' }
  ]
}

const getStatusType = (status) => {
  const map = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '注销',
    1: '正常',
    2: '挂失'
  }
  return map[status] || '未知'
}

const loadData = async () => {
  try {
    const res = await getCardList({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 0) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.cardNo = ''
  searchForm.status = null
  loadData()
}

const handleOpen = () => {
  dialogVisible.value = true
  Object.assign(form, {
    userNo: '',
    userType: 'student'
  })
}

const handleView = async (row) => {
  try {
    const res = await getCardInfo(row.id)
    if (res.code === 0) {
      ElMessageBox.alert(`
        <div style="text-align: left;">
          <p><strong>卡号：</strong>${res.data.cardNo}</p>
          <p><strong>用户编号：</strong>${res.data.userNo}</p>
          <p><strong>用户姓名：</strong>${res.data.userName}</p>
          <p><strong>用户类型：</strong>${res.data.userType === 'student' ? '学生' : '教师'}</p>
          <p><strong>状态：</strong>${getStatusText(res.data.status)}</p>
          <p><strong>发卡日期：</strong>${res.data.issueDate}</p>
          <p><strong>过期日期：</strong>${res.data.expireDate}</p>
        </div>
      `, '卡信息', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      })
    }
  } catch (error) {
    console.error('查看失败:', error)
  }
}

const handleLoss = async (row) => {
  operationType.value = 'loss'
  operationTitle.value = '挂失校园卡'
  operationForm.cardId = row.id
  operationForm.remark = ''
  operationDialogVisible.value = true
}

const handleUnloss = async (row) => {
  operationType.value = 'unloss'
  operationTitle.value = '解挂校园卡'
  operationForm.cardId = row.id
  operationForm.remark = ''
  operationDialogVisible.value = true
}

const handleCancel = async (row) => {
  operationType.value = 'cancel'
  operationTitle.value = '注销校园卡'
  operationForm.cardId = row.id
  operationForm.remark = ''
  operationDialogVisible.value = true
}

const handleOperationSubmit = async () => {
  try {
    // 验证表单
    if (!operationForm.remark) {
      ElMessage.error('请选择操作原因')
      return
    }
    
    let res
    if (operationType.value === 'loss') {
      res = await lossCard({ cardId: operationForm.cardId, remark: operationForm.remark })
      if (res.code === 0) {
        ElMessage.success('挂失成功')
      }
    } else if (operationType.value === 'unloss') {
      res = await unlossCard({ cardId: operationForm.cardId, remark: operationForm.remark })
      if (res.code === 0) {
        ElMessage.success('解挂成功')
      }
    } else if (operationType.value === 'cancel') {
      res = await cancelCard({ cardId: operationForm.cardId, remark: operationForm.remark })
      if (res.code === 0) {
        ElMessage.success('注销成功')
      }
    }
    
    if (res && res.code === 0) {
      operationDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 转换参数名，与后端保持一致
    const requestData = {
      user_no: form.userNo,
      user_type: form.userType,
      remark: form.remark
    }
    
    console.log('开卡请求参数:', requestData)
    
    const res = await openCard(requestData)
    console.log('开卡响应:', res)
    
    if (res.code === 0) {
      ElMessage.success('开卡成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  display: flex;
}
</style>
