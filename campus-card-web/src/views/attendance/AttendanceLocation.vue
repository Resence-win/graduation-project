<template>
  <div class="attendance-location">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>打卡位置管理</span>
          <el-button v-if="currentUser.role === 'teacher'" type="primary" @click="handleAddLocation">添加打卡位置</el-button>
        </div>
      </template>
      
      <el-table :data="locationList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="locationName" label="位置名称" />
        <el-table-column v-if="currentUser.role === 'admin'" prop="teacherName" label="发布老师" width="120">
          <template #default="{ row }">
            {{ row.teacherName || row.teacherId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置描述" />
        <el-table-column prop="latitude" label="纬度" width="120" />
        <el-table-column prop="longitude" label="经度" width="120" />
        <el-table-column prop="radius" label="打卡半径(米)" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getLocationStatus(row).type">
              {{ getLocationStatus(row).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditLocation(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteLocation(row.id)">删除</el-button>
            <el-button size="small" @click="handleViewRecords(row)">查看记录</el-button>
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
    
    <!-- 打卡位置编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
    >
      <el-form :model="locationForm" label-width="100px" class="location-form">
        <el-form-item label="位置名称" prop="locationName">
          <el-input v-model="locationForm.locationName" placeholder="请输入位置名称" />
        </el-form-item>
        <el-form-item label="位置描述" prop="location">
          <el-input v-model="locationForm.location" placeholder="请输入位置描述" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input type="number" v-model="locationForm.latitude" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input type="number" v-model="locationForm.longitude" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="打卡半径" prop="radius">
          <el-input type="number" v-model="locationForm.radius" placeholder="请输入打卡半径(米)" />
        </el-form-item>
        <el-form-item label="签到日期" prop="attendanceDate">
          <el-date-picker
            v-model="timeForm.attendanceDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择签到日期"
            :disabled-date="disabledAttendanceDate"
            :disabled="isRunningLocation"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="签到时间" prop="startTime">
          <div class="time-window-row">
            <el-time-picker
              v-model="timeForm.startClock"
              format="HH:mm"
              value-format="HH:mm:ss"
              placeholder="开始"
              :disabled="isRunningLocation"
            />
            <span class="time-separator">至</span>
            <el-time-picker
              v-model="timeForm.endClock"
              format="HH:mm"
              value-format="HH:mm:ss"
              placeholder="结束"
            />
          </div>
          <div class="quick-time-list">
            <el-button
              v-for="range in quickTimeRanges"
              :key="range.label"
              size="small"
              :type="isQuickRangeActive(range) ? 'primary' : 'default'"
              :disabled="isRunningLocation"
              @click="applyQuickTimeRange(range)"
            >
              {{ range.label }}
            </el-button>
          </div>
          <div class="time-rule-tip">
            开始后15分钟内为正常，之后到结束前为迟到；结束后未打卡将补记缺勤。
          </div>
        </el-form-item>
        <el-alert
          v-if="isRunningLocation"
          title="该打卡位置已开始，仅允许延长结束时间或调整地点信息，开始时间保持不变。"
          type="warning"
          show-icon
          :closable="false"
          class="form-alert"
        />
        <el-alert
          v-if="isEndedLocation"
          title="该打卡位置已结束，不允许继续修改，以免影响历史考勤记录。"
          type="error"
          show-icon
          :closable="false"
          class="form-alert"
        />
        <el-form-item label="状态" prop="status">
          <el-select v-model="locationForm.status" placeholder="请选择状态">
            <el-option label="有效" :value="1" />
            <el-option label="无效" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="info" @click="getCurrentLocation" :loading="gettingLocation">
            {{ gettingLocation ? '定位中...' : '获取当前位置' }}
          </el-button>
          <span v-if="locationError" class="location-error">{{ locationError }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveLocation">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 打卡记录对话框 -->
    <el-dialog
      v-model="recordDialogVisible"
      :title="recordDialogTitle"
      width="1200px"
      class="record-stat-dialog"
      @closed="handleRecordDialogClosed"
    >
      <div v-loading="recordStatLoading" class="record-stat-content">
        <div class="stat-grid">
          <div class="stat-card">
            <div class="stat-label">应打卡数</div>
            <div class="stat-value">{{ recordStatData.expected }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">实际打卡数</div>
            <div class="stat-value">{{ recordStatData.actual }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">正常</div>
            <div class="stat-value success">{{ recordStatData.normal }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">迟到</div>
            <div class="stat-value warning">{{ recordStatData.late }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">早退</div>
            <div class="stat-value warning">{{ recordStatData.early }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">缺勤</div>
            <div class="stat-value danger">{{ recordStatData.absent }}</div>
          </div>
        </div>
        <div ref="recordChartRef" class="record-chart"></div>
      </div>

      <el-table
        v-loading="recordLoading"
        :data="recordList"
        border
        table-layout="fixed"
        class="record-table"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="cardNo" label="卡号" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考勤类型" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getAttendanceTypeLabel(row.attendanceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualLocation" label="实际打卡地点" min-width="160" show-overflow-tooltip />
        <el-table-column prop="actualLatitude" label="实际纬度" width="110" show-overflow-tooltip />
        <el-table-column prop="actualLongitude" label="实际经度" width="110" show-overflow-tooltip />
        <el-table-column prop="deviceInfo" label="设备信息" min-width="140" show-overflow-tooltip />
        <el-table-column prop="recordTime" label="记录时间" width="170" show-overflow-tooltip />
      </el-table>
      
      <el-pagination
        v-model:current-page="recordPagination.page"
        v-model:page-size="recordPagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="recordPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleRecordSizeChange"
        @current-change="handleRecordCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recordDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { 
  createLocation, 
  updateLocation, 
  deleteLocation, 
  getAllLocations,
  getLocationsByTeacherId,
  getAttendanceRecordsByLocationId,
  getAttendanceSummary
} from '@/api/attendance'
import { getWeeklyAttendanceStat } from '@/api/statistics'

const locationList = ref([])
const recordList = ref([])
const dialogVisible = ref(false)
const recordDialogVisible = ref(false)
const dialogTitle = ref('添加打卡位置')
const currentLocationId = ref(null)
const currentRecordLocation = ref(null)
const currentUser = ref({})
const currentTeacherId = ref(null)
const originalStartTime = ref(null)
const originalEndTime = ref(null)
const recordLoading = ref(false)
const recordStatLoading = ref(false)
const recordChartRef = ref(null)
let recordChart = null

const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss'

const quickTimeRanges = [
  { label: '08:00-08:15', start: '08:00:00', end: '08:15:00' },
  { label: '10:00-10:15', start: '10:00:00', end: '10:15:00' },
  { label: '14:00-14:15', start: '14:00:00', end: '14:15:00' },
  { label: '19:00-19:15', start: '19:00:00', end: '19:15:00' }
]

const loadCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      currentUser.value = user
      currentTeacherId.value = user.role === 'teacher' ? user.businessUserId : null
    } catch (error) {
      currentUser.value = {}
      currentTeacherId.value = null
    }
  }
}

const gettingLocation = ref(false)
const locationError = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const recordPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const recordStatData = reactive({
  total: 0,
  expected: 0,
  actual: 0,
  normal: 0,
  late: 0,
  early: 0,
  absent: 0
})

const recordWeeklyData = reactive({
  startDate: '',
  endDate: '',
  daily: []
})

const locationForm = reactive({
  locationName: '',
  location: '',
  latitude: null,
  longitude: null,
  radius: 50,
  startTime: null,
  endTime: null,
  status: 1,
  teacherId: currentTeacherId.value
})

const timeForm = reactive({
  attendanceDate: '',
  startClock: '',
  endClock: ''
})

const recordDialogTitle = computed(() => {
  const name = currentRecordLocation.value?.locationName
  return name ? `${name} - 打卡记录` : '打卡记录'
})

const parseDateTime = (value) => {
  if (!value) return null
  if (value instanceof Date) return value
  const normalized = String(value).replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

const getTimestamp = (value) => {
  const date = parseDateTime(value)
  return date ? date.getTime() : null
}

const getStatisticRangeStart = () => {
  const date = new Date()
  date.setMonth(date.getMonth() - 1)
  return date
}

const isLocationInStatisticRange = (location) => {
  const endTime = parseDateTime(location?.endTime)
  return endTime ? endTime.getTime() >= getStatisticRangeStart().getTime() : false
}

const formatDateTime = (value) => {
  const date = parseDateTime(value)
  return date ? dayjs(date).format(DATE_TIME_FORMAT) : ''
}

const formatDate = (value) => {
  const date = parseDateTime(value)
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const formatClock = (value) => {
  const date = parseDateTime(value)
  return date ? dayjs(date).format('HH:mm:ss') : ''
}

const normalizeClock = (value) => {
  if (!value) return ''
  return value.length === 5 ? `${value}:00` : value
}

const buildDateTime = (date, clock) => {
  if (!date || !clock) return ''
  return `${date} ${normalizeClock(clock)}`
}

const locationEditPhase = computed(() => {
  if (!currentLocationId.value) return 'new'
  const startTimestamp = getTimestamp(originalStartTime.value)
  const endTimestamp = getTimestamp(originalEndTime.value)
  const now = Date.now()
  if (!startTimestamp || !endTimestamp) return 'new'
  if (now < startTimestamp) return 'pending'
  if (now <= endTimestamp) return 'running'
  return 'ended'
})

const isRunningLocation = computed(() => locationEditPhase.value === 'running')
const isEndedLocation = computed(() => locationEditPhase.value === 'ended')

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

const getLocationStatus = (row) => {
  if (Number(row.status) !== 1) {
    return { type: 'danger', text: '已停用' }
  }
  const now = Date.now()
  const startTime = getTimestamp(row.startTime)
  const endTime = getTimestamp(row.endTime)
  if (!startTime || !endTime) {
    return { type: 'info', text: '未配置' }
  }
  if (now < startTime) {
    return { type: 'warning', text: '未开始' }
  }
  if (now > endTime) {
    return { type: 'info', text: '已过期' }
  }
  return { type: 'success', text: '有效中' }
}

const getTodayStart = () => {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), now.getDate())
}

const disabledPastDate = (time) => {
  return time.getTime() < getTodayStart().getTime()
}

const disabledAttendanceDate = (time) => {
  if (currentLocationId.value) {
    return false
  }
  return disabledPastDate(time)
}

const resetTimeForm = () => {
  timeForm.attendanceDate = ''
  timeForm.startClock = ''
  timeForm.endClock = ''
}

const fillTimeForm = (startTime, endTime) => {
  timeForm.attendanceDate = formatDate(startTime)
  timeForm.startClock = formatClock(startTime)
  timeForm.endClock = formatClock(endTime)
}

const applyQuickTimeRange = (range) => {
  timeForm.startClock = range.start
  timeForm.endClock = range.end
}

const isQuickRangeActive = (range) => {
  return timeForm.startClock === range.start && timeForm.endClock === range.end
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    locationError.value = '您的浏览器不支持地理定位功能'
    return
  }
  
  gettingLocation.value = true
  locationError.value = ''
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      locationForm.latitude = position.coords.latitude
      locationForm.longitude = position.coords.longitude
      // 尝试根据经纬度获取位置描述
      getLocationDescription(position.coords.latitude, position.coords.longitude)
      gettingLocation.value = false
    },
    (error) => {
      gettingLocation.value = false
      switch (error.code) {
        case error.PERMISSION_DENIED:
          locationError.value = '用户拒绝了地理定位请求'
          break
        case error.POSITION_UNAVAILABLE:
          locationError.value = '位置信息不可用'
          break
        case error.TIMEOUT:
          locationError.value = '获取位置超时'
          break
        case error.UNKNOWN_ERROR:
          locationError.value = '发生未知错误'
          break
        default:
          locationError.value = '获取位置失败'
      }
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

// 根据经纬度获取位置描述（这里使用模拟数据，实际可以调用地理编码API）
const getLocationDescription = (latitude, longitude) => {
  // 实际项目中可以调用百度地图、高德地图等地理编码API
  // 这里使用模拟数据
  locationForm.location = `当前位置 (${latitude.toFixed(6)}, ${longitude.toFixed(6)})`
}

const loadLocations = async () => {
  try {
    let res
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (currentUser.value.role === 'teacher') {
      if (!currentTeacherId.value) {
        ElMessage.error('未获取到老师信息')
        return
      }
      res = await getLocationsByTeacherId(currentTeacherId.value, params)
    } else {
      res = await getAllLocations(params)
    }
    if (res.code === 0) {
      locationList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载打卡位置失败:', error)
    ElMessage.error('加载失败')
  }
}

const loadRecords = async (locationId) => {
  try {
    recordLoading.value = true
    const res = await getAttendanceRecordsByLocationId(locationId, {
      page: recordPagination.page,
      size: recordPagination.size
    })
    if (res.code === 0) {
      recordList.value = res.data.records || []
      recordPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载打卡记录失败:', error)
    ElMessage.error('加载失败')
  } finally {
    recordLoading.value = false
  }
}

const initRecordChart = () => {
  if (!recordChartRef.value) return
  if (recordChart) {
    recordChart.dispose()
  }
  recordChart = echarts.init(recordChartRef.value)
  const rows = recordWeeklyData.daily || []
  recordChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0 },
    grid: { top: 45, left: 45, right: 20, bottom: 40 },
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
}

const loadRecordStatistics = async (locationId) => {
  if (!locationId || !currentRecordLocation.value) return
  try {
    recordStatLoading.value = true
    const params = {
      location_id: locationId,
      start_date: formatDate(currentRecordLocation.value.startTime),
      end_date: formatDate(currentRecordLocation.value.endTime)
    }
    const summaryRes = await getAttendanceSummary(params)
    if (summaryRes.code === 0) {
      recordStatData.total = summaryRes.data?.total || 0
      recordStatData.expected = summaryRes.data?.expected ?? recordStatData.total
      recordStatData.actual = summaryRes.data?.actual ?? recordStatData.total
      recordStatData.normal = summaryRes.data?.normal || 0
      recordStatData.late = summaryRes.data?.late || 0
      recordStatData.early = summaryRes.data?.early || 0
      recordStatData.absent = summaryRes.data?.absent || 0
    }
    const weeklyRes = await getWeeklyAttendanceStat(params)
    if (weeklyRes.code === 0) {
      recordWeeklyData.startDate = weeklyRes.data?.startDate || ''
      recordWeeklyData.endDate = weeklyRes.data?.endDate || ''
      recordWeeklyData.daily = weeklyRes.data?.daily || []
      await nextTick()
      initRecordChart()
    }
  } catch (error) {
    console.error('加载考勤统计失败:', error)
    ElMessage.error('加载考勤统计失败')
  } finally {
    recordStatLoading.value = false
  }
}

const resetRecordStatistics = () => {
  recordStatData.total = 0
  recordStatData.expected = 0
  recordStatData.actual = 0
  recordStatData.normal = 0
  recordStatData.late = 0
  recordStatData.early = 0
  recordStatData.absent = 0
  recordWeeklyData.startDate = ''
  recordWeeklyData.endDate = ''
  recordWeeklyData.daily = []
}

const handleAddLocation = () => {
  if (currentUser.value.role !== 'teacher' || !currentTeacherId.value) {
    ElMessage.warning('仅老师可以发布打卡位置')
    return
  }
  dialogTitle.value = '添加打卡位置'
  currentLocationId.value = null
  originalStartTime.value = null
  originalEndTime.value = null
  Object.assign(locationForm, {
    locationName: '',
    location: '',
    latitude: null,
    longitude: null,
    radius: 50,
    startTime: '',
    endTime: '',
    status: 1,
    teacherId: currentTeacherId.value
  })
  resetTimeForm()
  dialogVisible.value = true
}

const handleEditLocation = (row) => {
  dialogTitle.value = '编辑打卡位置'
  currentLocationId.value = row.id
  originalStartTime.value = formatDateTime(row.startTime)
  originalEndTime.value = formatDateTime(row.endTime)
  Object.assign(locationForm, {
    locationName: row.locationName,
    location: row.location,
    latitude: row.latitude,
    longitude: row.longitude,
    radius: row.radius,
    startTime: originalStartTime.value,
    endTime: originalEndTime.value,
    status: row.status,
    teacherId: row.teacherId || currentTeacherId.value
  })
  fillTimeForm(originalStartTime.value, originalEndTime.value)
  dialogVisible.value = true
}

const handleSaveLocation = async () => {
  if (!locationForm.locationName) {
    ElMessage.error('请输入位置名称')
    return
  }
  if (!locationForm.latitude || !locationForm.longitude) {
    ElMessage.error('请输入经纬度')
    return
  }
  if (!locationForm.radius || Number(locationForm.radius) <= 0) {
    ElMessage.error('打卡半径必须大于0')
    return
  }
  if (!timeForm.attendanceDate || !timeForm.startClock || !timeForm.endClock) {
    ElMessage.error('请选择签到日期、开始时间和结束时间')
    return
  }

  const startTime = buildDateTime(timeForm.attendanceDate, timeForm.startClock)
  const endTime = buildDateTime(timeForm.attendanceDate, timeForm.endClock)
  const startTimestamp = getTimestamp(startTime)
  const endTimestamp = getTimestamp(endTime)
  const now = Date.now()

  if (!startTimestamp || !endTimestamp) {
    ElMessage.error('签到时间格式不正确')
    return
  }
  if (isEndedLocation.value) {
    ElMessage.error('已结束的打卡位置不允许修改')
    return
  }
  if (!currentLocationId.value && startTimestamp < now) {
    ElMessage.error('开始时间不能早于当前时间')
    return
  }
  if (endTimestamp <= startTimestamp) {
    ElMessage.error('结束时间必须晚于开始时间')
    return
  }
  if (isRunningLocation.value) {
    const originalStartTimestamp = getTimestamp(originalStartTime.value)
    if (originalStartTimestamp && startTimestamp !== originalStartTimestamp) {
      ElMessage.error('已开始的打卡位置不能修改开始时间')
      return
    }
    if (endTimestamp <= now) {
      ElMessage.error('进行中的打卡位置结束时间必须晚于当前时间')
      return
    }
  }

  const payload = {
    ...locationForm,
    startTime,
    endTime
  }

  try {
    let res
    if (currentLocationId.value) {
      // 更新
      res = await updateLocation({
        id: currentLocationId.value,
        ...payload
      })
    } else {
      // 添加
      res = await createLocation(payload)
    }
    if (res.code === 0) {
      ElMessage.success(currentLocationId.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadLocations()
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const handleDeleteLocation = async (id) => {
  try {
    const res = await deleteLocation(id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadLocations()
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const handleViewRecords = async (row) => {
  if (!isLocationInStatisticRange(row)) {
    ElMessage.warning('该考勤点已过期超过1个月，暂不支持统计查看')
    return
  }
  currentLocationId.value = row.id
  currentRecordLocation.value = row
  recordPagination.page = 1
  resetRecordStatistics()
  recordDialogVisible.value = true
  await nextTick()
  loadRecordStatistics(row.id)
  loadRecords(row.id)
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadLocations()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadLocations()
}

const handleRecordSizeChange = (val) => {
  recordPagination.size = val
  recordPagination.page = 1
  loadRecords(currentLocationId.value)
}

const handleRecordCurrentChange = (val) => {
  recordPagination.page = val
  loadRecords(currentLocationId.value)
}

const handleRecordDialogClosed = () => {
  if (recordChart) {
    recordChart.dispose()
    recordChart = null
  }
  currentRecordLocation.value = null
  recordList.value = []
}

const handleRecordChartResize = () => {
  recordChart?.resize()
}

onMounted(() => {
  loadCurrentUser()
  loadLocations()
  window.addEventListener('resize', handleRecordChartResize)
})

onBeforeUnmount(() => {
  if (recordChart) {
    recordChart.dispose()
  }
  window.removeEventListener('resize', handleRecordChartResize)
})
</script>

<style scoped>
.attendance-location {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-form :deep(.el-date-editor),
.location-form :deep(.el-select) {
  width: 100%;
}

.time-window-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  width: 100%;
}

.time-separator {
  color: #606266;
  white-space: nowrap;
}

.quick-time-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.quick-time-list .el-button {
  margin-left: 0;
}

.time-rule-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.form-alert {
  margin-bottom: 18px;
}

.location-error {
  margin-left: 12px;
  color: #f56c6c;
}

.record-stat-content {
  margin-bottom: 20px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 16px;
  text-align: center;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
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

.record-chart {
  width: 100%;
  height: 320px;
  margin-top: 20px;
}

.record-table :deep(.el-table__cell) {
  padding: 8px 0;
}

.record-table :deep(.cell) {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dialog-footer {
  text-align: right;
}
</style>
