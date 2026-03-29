<template>
  <div class="commute-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通勤车管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.cardId" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item label="线路">
          <el-input v-model="searchForm.routeId" placeholder="请输入线路ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardId" label="卡ID" width="100" />
        <el-table-column prop="routeId" label="线路ID" width="100" />
        <el-table-column prop="seatNumber" label="座位号" width="100" />
        <el-table-column prop="rideTime" label="乘车时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
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
import { getCommuteList } from '@/api/commute'

const tableData = ref([])

const searchForm = reactive({
  cardId: null,
  routeId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  try {
    const res = await getCommuteList({
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
  searchForm.cardId = null
  searchForm.routeId = null
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.commute-list {
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
