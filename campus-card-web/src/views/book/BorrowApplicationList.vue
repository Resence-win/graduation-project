<template>
  <div class="borrow-application-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借阅申请管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" :value="1" />
            <el-option label="已批准" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="cardNo" label="校园卡号" width="150" />
        <el-table-column prop="userName" label="借阅人" width="200" />
        <el-table-column prop="bookName" label="图书名称" width="200" />
        <el-table-column prop="borrowDays" label="借阅天数" width="100" />
        <el-table-column prop="applicationTime" label="申请时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleApprove(row)" :disabled="row.status !== 1">批准</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)" :disabled="row.status !== 1">拒绝</el-button>
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
import {
  getBorrowApplications,
  approveBorrowApplication
} from '@/api/book'

const tableData = ref([])

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  try {
    const res = await getBorrowApplications({
      page: pagination.page,
      size: pagination.size,
      status: searchForm.status
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
  searchForm.status = null
  loadData()
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要批准该借阅申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await approveBorrowApplication({
      application_id: row.id,
      status: 2,
      operator_id: 1 // 这里应该从登录用户信息中获取
    })
    if (res.code === 0) {
      ElMessage.success('批准成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准失败:', error)
    }
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确定要拒绝该借阅申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await approveBorrowApplication({
      application_id: row.id,
      status: 3,
      operator_id: 1 // 这里应该从登录用户信息中获取
    })
    if (res.code === 0) {
      ElMessage.success('拒绝成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
    }
  }
}

const getStatusType = (status) => {
  const statusMap = {
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    1: '待审批',
    2: '已批准',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
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
.borrow-application-list {
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