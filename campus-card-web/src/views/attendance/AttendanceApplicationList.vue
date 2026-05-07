<template>
  <div class="attendance-application-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤申报审批</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="申报类型">
          <el-select v-model="searchForm.applicationType" placeholder="请选择类型" clearable>
            <el-option label="外出实习" value="INTERNSHIP" />
            <el-option label="请假" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="申请ID" width="90" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="cardNo" label="校园卡号" width="150" />
        <el-table-column label="申报类型" width="110">
          <template #default="{ row }">
            {{ getApplicationTypeText(row.applicationType) }}
          </template>
        </el-table-column>
        <el-table-column label="申报日期" width="210">
          <template #default="{ row }">
            {{ row.startDate || '-' }}<span v-if="row.endDate"> 至 {{ row.endDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="internshipCompany" label="实习单位" width="160" show-overflow-tooltip />
        <el-table-column prop="reason" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="审核时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleReview(row, 'APPROVED')" :disabled="row.status !== 'PENDING'">通过</el-button>
            <el-button size="small" type="danger" @click="handleReview(row, 'REJECTED')" :disabled="row.status !== 'PENDING'">拒绝</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAttendanceApplications, reviewAttendanceApplication } from '@/api/attendance'

const tableData = ref([])

const searchForm = reactive({
  applicationType: '',
  status: 'PENDING'
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  try {
    const res = await getAttendanceApplications({
      application_type: searchForm.applicationType,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    })
    if (res.code === 0) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载考勤申报失败:', error)
  }
}

const handleReview = async (row, status) => {
  const actionText = status === 'APPROVED' ? '通过' : '拒绝'
  const applicationTypeText = getApplicationTypeText(row.applicationType)
  try {
    const { value } = await ElMessageBox.prompt(`确定要${actionText}该${applicationTypeText}申报吗？`, '审核确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '可填写审核备注',
      inputType: 'textarea'
    })

    const res = await reviewAttendanceApplication({
      application_id: row.id,
      status,
      reviewer_id: getCurrentUserId(),
      review_remark: value || ''
    })
    if (res.code === 0) {
      ElMessage.success(`${actionText}成功`)
      loadData()
    } else {
      ElMessage.error(res.msg || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}失败:`, error)
      ElMessage.error(error.response?.data?.msg || `${actionText}失败`)
    }
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.applicationType = ''
  searchForm.status = 'PENDING'
  loadData()
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadData()
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return map[status] || '未知'
}

const getApplicationTypeText = (type) => {
  const map = {
    INTERNSHIP: '外出实习',
    LEAVE: '请假'
  }
  return map[type] || '考勤'
}

const getCurrentUserId = () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return undefined
  try {
    return JSON.parse(userStr).id
  } catch (error) {
    return undefined
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.attendance-application-list {
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
