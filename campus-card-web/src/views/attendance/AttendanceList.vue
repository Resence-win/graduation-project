<template>
  <div class="attendance-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.cardId" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="正常" />
            <el-option label="迟到" value="迟到" />
            <el-option label="早退" value="早退" />
            <el-option label="缺勤" value="缺勤" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardId" label="卡ID" width="100" />
        <el-table-column prop="status" label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="locationName" label="打卡位置" />
        <el-table-column prop="actualLocation" label="实际打卡地点" />
        <el-table-column prop="actualLatitude" label="实际纬度" width="120" />
        <el-table-column prop="actualLongitude" label="实际经度" width="120" />
        <el-table-column prop="deviceInfo" label="设备信息" />
        <el-table-column prop="recordTime" label="记录时间" width="180" />
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
    
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>考勤统计</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="statForm">
        <el-form-item label="开始日期">
          <el-date-picker v-model="statForm.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="statForm.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleStatistics">统计</el-button>
        </el-form-item>
      </el-form>
      
      <div class="stat-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">总打卡次数</div>
                <div class="stat-value">{{ statData.total }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">正常</div>
                <div class="stat-value">{{ statData.normal }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">迟到</div>
                <div class="stat-value">{{ statData.late }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">缺勤</div>
                <div class="stat-value">{{ statData.absent }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAttendanceList, getAttendanceStatistics } from '@/api/attendance'

const tableData = ref([])

const searchForm = reactive({
  cardId: null,
  status: null,
  startDate: null,
  endDate: null
})

const statForm = reactive({
  startDate: null,
  endDate: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statData = reactive({
  total: 0,
  normal: 0,
  late: 0,
  absent: 0
})

const getStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '缺勤': 'danger'
  }
  return map[status] || 'info'
}

const loadData = async () => {
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      card_id: searchForm.cardId,
      status: searchForm.status,
      start_date: searchForm.startDate ? searchForm.startDate.toISOString().split('T')[0] : '',
      end_date: searchForm.endDate ? searchForm.endDate.toISOString().split('T')[0] : ''
    }
    const res = await getAttendanceList(params)
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
  searchForm.status = null
  searchForm.startDate = null
  searchForm.endDate = null
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

const handleStatistics = async () => {
  try {
    const params = {
      start_date: statForm.startDate ? statForm.startDate.toISOString().split('T')[0] : '',
      end_date: statForm.endDate ? statForm.endDate.toISOString().split('T')[0] : ''
    }
    const res = await getAttendanceStatistics(params)
    if (res.code === 0) {
      const records = res.data.records || []
      statData.total = records.length
      statData.normal = records.filter(r => r.status === '正常').length
      statData.late = records.filter(r => r.status === '迟到').length
      statData.absent = records.filter(r => r.status === '缺勤').length
    }
  } catch (error) {
    console.error('统计失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.attendance-list {
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

.stat-content {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
