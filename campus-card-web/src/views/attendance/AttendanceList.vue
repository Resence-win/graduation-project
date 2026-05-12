<template>
  <div class="attendance-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤统计</span>
        </div>
      </template>

      <el-form :inline="true" :model="statForm">
        <el-form-item label="开始日期">
          <el-date-picker v-model="statForm.startDate" type="date" placeholder="选择开始日期" disabled />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="statForm.endDate" type="date" placeholder="选择结束日期" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="statLoading" @click="handleStatistics">统计</el-button>
          <el-button @click="handleStatReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="statLoading" class="stat-content">
        <div class="stat-grid">
          <div class="stat-card">
            <div class="stat-label">总打卡次数</div>
            <div class="stat-value">{{ statData.total }}</div>
          </div>
          <div class="stat-card clickable-stat-card" @click="applyStatusRangeFilter('正常')">
            <div class="stat-label">正常</div>
            <div class="stat-value success">{{ statData.normal }}</div>
          </div>
          <div class="stat-card clickable-stat-card" @click="applyStatusRangeFilter('迟到')">
            <div class="stat-label">迟到</div>
            <div class="stat-value warning">{{ statData.late }}</div>
          </div>
          <div class="stat-card clickable-stat-card" @click="applyStatusRangeFilter('早退')">
            <div class="stat-label">早退</div>
            <div class="stat-value warning">{{ statData.early }}</div>
          </div>
          <div class="stat-card clickable-stat-card" @click="applyStatusRangeFilter('缺勤')">
            <div class="stat-label">缺勤</div>
            <div class="stat-value danger">{{ statData.absent }}</div>
          </div>
        </div>
        <div ref="weeklyChartRef" class="attendance-chart"></div>
      </div>
    </el-card>

    <el-card ref="recordCardRef" style="margin-top: 20px">
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
      
      <el-table
        v-loading="listLoading"
        :data="tableData"
        border
        table-layout="fixed"
        class="attendance-record-table"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="cardNo" label="卡号" width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getAttendanceTypeLabel(row.attendanceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="locationName" label="打卡位置" width="130" show-overflow-tooltip />
        <el-table-column prop="actualLocation" label="实际地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="internshipCompany" label="实习单位" width="130" show-overflow-tooltip />
        <el-table-column prop="internshipLogDate" label="日志日期" width="115" show-overflow-tooltip />
        <el-table-column label="请假信息" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.attendanceType === 'LEAVE'">
              {{ row.leaveStartDate }} 至 {{ row.leaveEndDate }} / {{ row.leaveReason || '未填写原因' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualLatitude" label="纬度" width="110" show-overflow-tooltip />
        <el-table-column prop="actualLongitude" label="经度" width="110" show-overflow-tooltip />
        <el-table-column prop="deviceInfo" label="设备" min-width="150" show-overflow-tooltip />
        <el-table-column prop="recordTime" label="记录时间" width="170" show-overflow-tooltip />
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
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getAttendanceList, getAttendanceSummary } from '@/api/attendance'
import { getWeeklyAttendanceStat } from '@/api/statistics'

const tableData = ref([])
const listLoading = ref(false)
const statLoading = ref(false)
const weeklyChartRef = ref(null)
const recordCardRef = ref(null)
let weeklyChart = null

const getRecentSevenDayRange = () => {
  const endDate = new Date()
  endDate.setHours(0, 0, 0, 0)
  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - 6)
  return { startDate, endDate }
}

const recentSevenDayRange = getRecentSevenDayRange()

const searchForm = reactive({
  cardId: null,
  status: null,
  startDate: null,
  endDate: null
})

const statForm = reactive({
  startDate: recentSevenDayRange.startDate,
  endDate: recentSevenDayRange.endDate
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
  early: 0,
  absent: 0
})

const weeklyData = reactive({
  startDate: '',
  endDate: '',
  daily: []
})

const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'string') return date
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const setRecentSevenDayRange = () => {
  const { startDate, endDate } = getRecentSevenDayRange()
  statForm.startDate = startDate
  statForm.endDate = endDate
}

const parseDate = (value) => {
  if (!value) return null
  if (value instanceof Date) return value
  const [year, month, day] = String(value).split('-').map(Number)
  return new Date(year, month - 1, day)
}

const validateDateRange = (startDate, endDate) => {
  if (startDate && endDate && startDate.getTime() > endDate.getTime()) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return false
  }
  return true
}

const getStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '缺勤': 'danger'
  }
  return map[status] || 'info'
}

const getAttendanceTypeLabel = (type) => {
  const map = {
    CAMPUS_LOCATION: '校内位置',
    OFF_CAMPUS_LOCATION: '校外位置',
    INTERNSHIP_LOG: '实习日志',
    LEAVE: '请假'
  }
  return map[type] || '校内位置'
}

const loadData = async () => {
  if (!validateDateRange(searchForm.startDate, searchForm.endDate)) return

  try {
    listLoading.value = true
    const params = {
      page: pagination.page,
      size: pagination.size,
      card_id: searchForm.cardId,
      status: searchForm.status,
      start_date: formatDate(searchForm.startDate),
      end_date: formatDate(searchForm.endDate)
    }
    const res = await getAttendanceList(params)
    if (res.code === 0) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    listLoading.value = false
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

const scrollToRecords = () => {
  nextTick(() => {
    recordCardRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

const applyChartFilter = (status, startDate, endDate) => {
  searchForm.status = status
  searchForm.startDate = parseDate(startDate)
  searchForm.endDate = parseDate(endDate)
  pagination.page = 1
  loadData()
  scrollToRecords()
}

const applyStatusRangeFilter = (status) => {
  const startDate = formatDate(statForm.startDate) || weeklyData.startDate
  const endDate = formatDate(statForm.endDate) || weeklyData.endDate
  applyChartFilter(status, startDate, endDate)
}

const initWeeklyChart = () => {
  if (!weeklyChartRef.value) return
  if (weeklyChart) {
    weeklyChart.dispose()
  }
  weeklyChart = echarts.init(weeklyChartRef.value)
  const rows = weeklyData.daily || []
  weeklyChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0 },
    grid: { top: 45, left: 45, right: 20, bottom: 45 },
    xAxis: { type: 'category', data: rows.map(item => item.date) },
    yAxis: {
      type: 'value',
      name: '人数',
      minInterval: 1,
      axisLabel: { formatter: value => String(Math.round(value)) }
    },
    series: [
      { name: '正常', type: 'bar', data: rows.map(item => Number(item.normal || 0)), itemStyle: { color: '#67C23A' } },
      { name: '迟到', type: 'bar', data: rows.map(item => Number(item.late || 0)), itemStyle: { color: '#E6A23C' } },
      { name: '早退', type: 'bar', data: rows.map(item => Number(item.early || 0)), itemStyle: { color: '#909399' } },
      { name: '缺勤', type: 'bar', data: rows.map(item => Number(item.absent || 0)), itemStyle: { color: '#F56C6C' } }
    ]
  })
  weeklyChart.off('click')
  weeklyChart.on('click', (params) => {
    if (params.componentType === 'series' && params.seriesName && params.name) {
      applyChartFilter(params.seriesName, params.name, params.name)
    }
  })
  weeklyChart.off('legendselectchanged')
  weeklyChart.on('legendselectchanged', (params) => {
    weeklyChart.setOption({
      legend: { selected: { 正常: true, 迟到: true, 早退: true, 缺勤: true } }
    })
    applyChartFilter(params.name, weeklyData.startDate, weeklyData.endDate)
  })
}

const loadWeeklyStatistics = async () => {
  const params = {
    start_date: formatDate(statForm.startDate),
    end_date: formatDate(statForm.endDate)
  }
  const res = await getWeeklyAttendanceStat(params)
  if (res.code === 0) {
    weeklyData.startDate = res.data?.startDate || ''
    weeklyData.endDate = res.data?.endDate || ''
    weeklyData.daily = res.data?.daily || []
    await nextTick()
    initWeeklyChart()
  }
}

const handleStatistics = async () => {
  setRecentSevenDayRange()
  if (!validateDateRange(statForm.startDate, statForm.endDate)) return

  try {
    statLoading.value = true
    const params = {
      start_date: formatDate(statForm.startDate),
      end_date: formatDate(statForm.endDate)
    }
    const res = await getAttendanceSummary(params)
    if (res.code === 0) {
      statData.total = res.data?.total || 0
      statData.normal = res.data?.normal || 0
      statData.late = res.data?.late || 0
      statData.early = res.data?.early || 0
      statData.absent = res.data?.absent || 0
    }
    await loadWeeklyStatistics()
  } catch (error) {
    console.error('统计失败:', error)
  } finally {
    statLoading.value = false
  }
}

const handleStatReset = () => {
  setRecentSevenDayRange()
  handleStatistics()
}

onMounted(() => {
  loadData()
  handleStatistics()
  window.addEventListener('resize', handleChartResize)
})

const handleChartResize = () => {
  weeklyChart?.resize()
}

onBeforeUnmount(() => {
  if (weeklyChart) {
    weeklyChart.dispose()
  }
  window.removeEventListener('resize', handleChartResize)
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.attendance-chart {
  width: 100%;
  height: 360px;
  margin-top: 24px;
}

.attendance-record-table {
  width: 100%;
}

.attendance-record-table :deep(.el-table__cell) {
  padding: 8px 0;
}

.attendance-record-table :deep(.cell) {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.stat-card {
  padding: 18px 20px;
  text-align: center;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
}

.clickable-stat-card {
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.clickable-stat-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.16);
  transform: translateY(-1px);
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

.stat-value.success {
  color: #67C23A;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.danger {
  color: #F56C6C;
}
</style>
