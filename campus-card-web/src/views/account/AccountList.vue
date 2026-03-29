<template>
  <div class="account-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账户管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.cardNo" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardId" label="卡ID" width="100" />
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: bold">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewFlow(row)">查看流水</el-button>
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
      v-model="flowDialogVisible"
      title="账户流水"
      width="800px"
    >
      <el-table :data="flowData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="changeType" label="变动类型" width="100" />
        <el-table-column prop="amount" label="变动金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.changeType === '充值' ? '#67C23A' : '#F56C6C' }">
              {{ row.changeType === '充值' ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: bold">¥{{ row.balanceAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      
      <el-pagination
        v-model:current-page="flowPagination.page"
        v-model:page-size="flowPagination.size"
        :total="flowPagination.total"
        layout="total, prev, pager, next"
        @current-change="handleFlowPageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccount, getAccountFlow } from '@/api/account'

const flowDialogVisible = ref(false)
const tableData = ref([])
const flowData = ref([])
const currentAccountId = ref(null)

const searchForm = reactive({
  cardNo: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const flowPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  if (!searchForm.cardNo) {
    ElMessage.warning('请输入卡号')
    return
  }
  
  try {
    const res = await getAccount(searchForm.cardNo)
    if (res.code === 0) {
      tableData.value = res.data ? [res.data] : []
      pagination.total = res.data ? 1 : 0
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
  tableData.value = []
  pagination.total = 0
}

const handleViewFlow = async (row) => {
  currentAccountId.value = row.id
  flowPagination.page = 1
  await loadFlowData()
  flowDialogVisible.value = true
}

const loadFlowData = async () => {
  try {
    const res = await getAccountFlow({
      account_id: currentAccountId.value,
      page: flowPagination.page,
      size: flowPagination.size
    })
    if (res.code === 0) {
      flowData.value = res.data.records || []
      flowPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载流水失败:', error)
  }
}

const handleFlowPageChange = (val) => {
  flowPagination.page = val
  loadFlowData()
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadData()
}
</script>

<style scoped>
.account-list {
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
