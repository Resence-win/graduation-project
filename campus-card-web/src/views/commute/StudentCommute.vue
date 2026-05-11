<template>
  <div class="student-commute">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通勤车查询</span>
          <el-button type="primary" @click="getCurrentLocation">获取当前位置</el-button>
        </div>
      </template>

      <el-alert
        v-if="!studentProfileComplete"
        :title="studentProfileIncompleteMessage"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      
      <!-- 当前位置信息 -->
      <div class="current-location" v-if="currentLocation">
        <el-alert
          :title="`当前位置: ${currentLocation.latitude.toFixed(6)}, ${currentLocation.longitude.toFixed(6)}`"
          type="info"
          show-icon
        />
      </div>
      
      <!-- 线路列表 -->
      <div class="section">
        <h3>线路列表</h3>
        <el-table :data="routeList" border style="width: 100%">
          <el-table-column prop="id" label="线路ID" width="80" />
          <el-table-column prop="routeName" label="线路名称" width="180" />
          <el-table-column prop="startStation" label="起点站" width="150" />
          <el-table-column prop="endStation" label="终点站" width="150" />
          <el-table-column prop="totalDistance" label="全程距离(公里)" width="120" />
          <el-table-column prop="totalTime" label="全程时间(分钟)" width="120" />
          <el-table-column label="到起点距离" width="120">
            <template #default="{ row }">
              <span v-if="currentLocation && stationList.length > 0">
                {{ calculateDistanceToStartStation(row.id).toFixed(2) }} km
              </span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="到起点时间" width="120">
            <template #default="{ row }">
              <span v-if="currentLocation && stationList.length > 0">
                {{ calculateArrivalTimeToStartStation(row.id) }}
              </span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="到终点距离" width="120">
            <template #default="{ row }">
              <span v-if="currentLocation && stationList.length > 0">
                {{ calculateDistanceToEndStation(row.id).toFixed(2) }} km
              </span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="到终点时间" width="120">
            <template #default="{ row }">
              <span v-if="currentLocation && stationList.length > 0">
                {{ calculateArrivalTimeToEndStation(row.id) }}
              </span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="handleViewStations(row)">查看站点</el-button>
              <el-button size="small" @click="handleViewSchedule(row.id)">查看时刻表</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      

      
      <!-- 时刻表 -->
      <div class="section" v-if="selectedRouteId">
        <h3>{{ selectedRouteName }} - 时刻表</h3>
        <el-table :data="scheduleList" border style="width: 100%">
          <el-table-column prop="id" label="班次ID" width="80" />
          <el-table-column label="车辆" width="120">
            <template #default="{ row }">
              {{ getVehicleInfo(row.vehicleId) }}
            </template>
          </el-table-column>
          <el-table-column prop="departureTime" label="发车时间" width="120" />
          <el-table-column prop="frequency" label="班次频率" width="100" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
        </el-table>
      </div>
      
      <!-- 校园卡扫码上车 -->
      <div class="section">
        <h3>校园卡扫码上车</h3>
        <el-card shadow="hover" style="max-width: 400px; margin: 0 auto">
          <div class="scan-container">
            <el-form :model="scanForm" label-width="80px">
              <el-form-item label="选择线路">
                <el-select v-model="scanForm.routeId" placeholder="请选择线路" style="width: 100%">
                  <el-option
                    v-for="route in activeRouteList"
                    :key="route.id"
                    :label="route.routeName"
                    :value="route.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="选择车辆">
                <el-select v-model="scanForm.vehicleId" placeholder="请选择车辆" style="width: 100%">
                  <el-option
                    v-for="vehicle in filteredVehicleList"
                    :key="vehicle.id"
                    :label="vehicle.plateNumber"
                    :value="vehicle.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="选择班次">
                <el-select v-model="scanForm.scheduleId" placeholder="请选择班次" style="width: 100%">
                  <el-option
                    v-for="schedule in filteredScheduleList"
                    :key="schedule.id"
                    :label="schedule.departureTime"
                    :value="schedule.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <el-button type="primary" :disabled="!studentProfileComplete" @click="handleScanQRCode" style="margin-top: 20px">扫码上车</el-button>
            <div v-if="scanResult" class="scan-result">
              <el-alert
                :title="scanResult.success ? '扫码成功' : '扫码失败'"
                :type="scanResult.success ? 'success' : 'error'"
                show-icon
              />
              <p v-if="scanResult.success">
                线路：{{ scanResult.routeName }}<br>
                车辆：{{ scanResult.vehiclePlateNumber }}<br>
                班次：{{ scanResult.scheduleTime }}<br>
                座位号：{{ scanResult.seatNumber }}<br>
                扫码时间：{{ scanResult.scanTime }}
              </p>
              <p v-else>{{ scanResult.message }}</p>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 我的乘车记录 -->
      <div class="section">
        <h3>我的乘车记录</h3>
        <el-table :data="myRecordList" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="线路" width="150">
            <template #default="{ row }">
              {{ getRouteInfo(row.routeId) }}
            </template>
          </el-table-column>
          <el-table-column label="车辆" width="120">
            <template #default="{ row }">
              {{ getVehicleInfo(row.vehicleId) }}
            </template>
          </el-table-column>
          <el-table-column label="班次" width="100">
            <template #default="{ row }">
              {{ getScheduleInfo(row.scheduleId) }}
            </template>
          </el-table-column>
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
          v-model:current-page="recordPagination.page"
          v-model:page-size="recordPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="recordPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleRecordSizeChange"
          @current-change="handleRecordCurrentChange"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="stationDialogVisible"
      :title="`${selectedStationRouteName || '线路'} - 站点`"
      width="640px"
    >
      <el-table :data="selectedRouteStations" border style="width: 100%">
        <el-table-column prop="type" label="站点类型" width="100" />
        <el-table-column prop="stationName" label="站点名称" width="140" />
        <el-table-column prop="location" label="站点位置" min-width="160" />
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">
            <span v-if="row.latitude && row.longitude">
              {{ Number(row.latitude).toFixed(6) }}, {{ Number(row.longitude).toFixed(6) }}
            </span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="步行距离" width="110">
          <template #default="{ row }">
            {{ formatStationDistance(row) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stationDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRouteList,
  getScheduleList,
  getCommuteList,
  getVehicleList,
  getStationList,
  addCommuteRecord
} from '@/api/commute'
import { getStudentByNo } from '@/api/student'

// 线路列表
const routeList = ref([])
const selectedRouteId = ref(null)
const selectedRouteName = ref('')



// 时刻表列表
const scheduleList = ref([])
const allScheduleList = ref([])

// 车辆列表
const vehicleList = ref([])

// 站点列表
const stationList = ref([])

const activeRouteList = computed(() => routeList.value.filter(route => route.status === 1))
const activeVehicleList = computed(() => vehicleList.value.filter(vehicle => vehicle.status === 1))
const studentInfo = ref(null)
const studentProfileIncompleteMessage = '请先在个人中心完善姓名、性别、学院、专业、班级、手机号后再进行该操作'
const studentProfileComplete = computed(() => {
  if (!studentInfo.value) {
    return false
  }
  return Boolean(studentInfo.value.name && studentInfo.value.gender && studentInfo.value.college && studentInfo.value.major && studentInfo.value.className && studentInfo.value.phone && /^1[3-9]\d{9}$/.test(studentInfo.value.phone))
})

// 我的乘车记录
const myRecordList = ref([])
const recordPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 扫码结果
const scanResult = ref(null)

// 扫码上车选择
const scanForm = reactive({
  routeId: null,
  vehicleId: null,
  scheduleId: null
})

// 过滤后的车辆列表
const filteredVehicleList = ref([])
// 过滤后的班次列表
const filteredScheduleList = ref([])

// 监听线路选择变化
watch(() => scanForm.routeId, async (newRouteId) => {
  if (newRouteId) {
    // 加载该线路的时刻表
    await loadScheduleList(newRouteId)
    
    // 从时刻表中提取该线路对应的车辆ID
    const vehicleIds = [...new Set(scheduleList.value.filter(s => s.status === 1).map(s => s.vehicleId))]
    
    // 过滤车辆列表，只显示该线路对应的车辆
    filteredVehicleList.value = activeVehicleList.value.filter(v => vehicleIds.includes(v.id))
    
    // 重置车辆和班次选择
    scanForm.vehicleId = null
    scanForm.scheduleId = null
    filteredScheduleList.value = []
  } else {
    // 如果没有选择线路，显示所有车辆
    filteredVehicleList.value = activeVehicleList.value
    scheduleList.value = []
    filteredScheduleList.value = []
    scanForm.vehicleId = null
    scanForm.scheduleId = null
  }
})

// 监听车辆选择变化，过滤对应的班次
watch(() => scanForm.vehicleId, (newVehicleId) => {
  if (newVehicleId && scanForm.routeId) {
    // 过滤班次列表，只显示当前线路和车辆对应的班次
    filteredScheduleList.value = scheduleList.value.filter(s => s.vehicleId === newVehicleId && s.status === 1)
    // 重置班次选择
    scanForm.scheduleId = null
  } else {
    filteredScheduleList.value = []
    scanForm.scheduleId = null
  }
})

// 当前位置
const currentLocation = ref(null)

const stationDialogVisible = ref(false)
const selectedRouteStations = ref([])
const selectedStationRouteName = ref('')

// 加载线路列表
const loadRouteList = async () => {
  try {
    const res = await getRouteList({ page: 1, size: 100 })
    if (res.code === 0) {
      routeList.value = res.data.records || []
    } else {
      console.error('线路列表响应错误:', res)
    }
  } catch (error) {
    console.error('加载线路列表失败:', error)
    console.error('错误详情:', error.message)
    if (error.response) {
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
    }
  }
}

// 加载车辆列表
const loadVehicleList = async () => {
  try {
    const res = await getVehicleList({ page: 1, size: 100 })
    if (res.code === 0) {
      vehicleList.value = res.data.records || []
      // 更新过滤后的车辆列表
      filteredVehicleList.value = activeVehicleList.value
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
  }
}

const loadStudentInfo = async () => {
  try {
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    if (!user?.username) return
    const res = await getStudentByNo(user.username)
    if (res.code === 0) {
      studentInfo.value = res.data
    }
  } catch (error) {
    console.error('加载学生资料失败:', error)
  }
}

// 加载站点列表
const loadStationList = async () => {
  try {
    const res = await getStationList({ page: 1, size: 100 })
    if (res.code === 0) {
      stationList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载站点列表失败:', error)
  }
}



// 查看站点
const handleViewStations = async (routeOrId) => {
  const route = typeof routeOrId === 'object'
    ? routeOrId
    : routeList.value.find(r => r.id === routeOrId)
  if (!route) {
    ElMessage.warning('未找到线路信息')
    return
  }

  selectedStationRouteName.value = route.routeName
  selectedRouteStations.value = buildRouteStations(route)
  stationDialogVisible.value = true

  if (selectedRouteStations.value.length === 0) {
    ElMessage.warning('该线路暂无可展示的站点信息')
  }

  // 自动获取当前位置（如果还没有获取）
  if (!currentLocation.value) {
    getCurrentLocation()
  }
}

const handleSelectRoute = async (routeId) => {
  selectedRouteId.value = routeId
  const route = routeList.value.find(r => r.id === routeId)
  if (route) {
    selectedRouteName.value = route.routeName
  }
  
  // 自动获取当前位置（如果还没有获取）
  if (!currentLocation.value) {
    getCurrentLocation()
  }
  
  // 加载时刻表
  loadScheduleList(routeId)
}

const buildRouteStations = (route) => {
  const startStation = stationList.value.find(s => s.stationName === route.startStation)
  const endStation = stationList.value.find(s => s.stationName === route.endStation)
  const stations = []

  stations.push({
    type: '起点',
    stationName: route.startStation || '--',
    location: startStation?.location || '--',
    latitude: startStation?.latitude,
    longitude: startStation?.longitude
  })

  if (route.endStation !== route.startStation) {
    stations.push({
      type: '终点',
      stationName: route.endStation || '--',
      location: endStation?.location || '--',
      latitude: endStation?.latitude,
      longitude: endStation?.longitude
    })
  }

  return stations.filter(station => station.stationName && station.stationName !== '--')
}

// 加载时刻表
const loadScheduleList = async (routeId) => {
  try {
    const res = await getScheduleList({ routeId, page: 1, size: 100 })
    if (res.code === 0) {
      // 只存储当前线路的时刻表数据
      scheduleList.value = res.data.records || []
      mergeScheduleCache(scheduleList.value)
    }
  } catch (error) {
    console.error('加载时刻表失败:', error)
  }
}

const loadAllSchedules = async () => {
  try {
    const res = await getScheduleList({ page: 1, size: 1000 })
    if (res.code === 0) {
      allScheduleList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载全部时刻表失败:', error)
  }
}

const mergeScheduleCache = (schedules) => {
  if (!Array.isArray(schedules) || schedules.length === 0) {
    return
  }

  const scheduleMap = new Map(allScheduleList.value.map(schedule => [schedule.id, schedule]))
  schedules.forEach(schedule => {
    scheduleMap.set(schedule.id, schedule)
  })
  allScheduleList.value = Array.from(scheduleMap.values())
}

// 查看时刻表
const handleViewSchedule = (routeId) => {
  handleSelectRoute(routeId)
}

// 加载我的乘车记录
const loadMyRecordData = async () => {
  try {
    // 从登录信息中获取当前用户的卡ID
    const cardId = getStudentCardId()
    if (!cardId) {
      myRecordList.value = []
      recordPagination.total = 0
      return
    }
    
    // 先加载车辆和线路列表，确保数据就绪
    if (vehicleList.value.length === 0) {
      await loadVehicleList()
    }
    if (routeList.value.length === 0) {
      await loadRouteList()
    }
    
    // 加载全量班次索引，避免覆盖当前线路时刻表导致记录中的班次显示不全
    if (allScheduleList.value.length === 0) {
      await loadAllSchedules()
    }
    
    // 获取乘车记录
    const res = await getCommuteList({
      cardId,
      page: recordPagination.page,
      size: recordPagination.size
    })
    
    if (res.code === 0) {
      myRecordList.value = res.data.records || []
      recordPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载乘车记录失败:', error)
  }
}

// 记录分页
const handleRecordSizeChange = (val) => {
  recordPagination.size = val
  loadMyRecordData()
}

const handleRecordCurrentChange = (val) => {
  recordPagination.page = val
  loadMyRecordData()
}

// 获取当前位置
const getCurrentLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentLocation.value = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        }
      },
      (error) => {
        console.error('获取位置失败:', error)
        ElMessage.error('获取位置失败，请检查位置权限')
      }
    )
  } else {
    ElMessage.error('浏览器不支持地理定位')
  }
}

// 计算两点之间的距离（使用Haversine公式）
const calculateDistance = (point1, point2) => {
  const R = 6371 // 地球半径（公里）
  const dLat = (point2.latitude - point1.latitude) * Math.PI / 180
  const dLon = (point2.longitude - point1.longitude) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(point1.latitude * Math.PI / 180) * Math.cos(point2.latitude * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = R * c
  return distance
}

// 计算预计到达时间（分钟）
const calculateArrivalTime = (currentLocation, stationLocation) => {
  // 计算距离（公里）
  const distance = calculateDistance(currentLocation, stationLocation)
  
  // 假设步行速度为4公里/小时
  const walkingSpeed = 4 // 公里/小时
  
  // 计算预计时间（分钟）
  const estimatedTime = (distance / walkingSpeed) * 60
  
  // 返回预计分钟数
  return Math.round(estimatedTime) + ' 分钟'
}

const formatStationDistance = (station) => {
  if (!currentLocation.value || !station.latitude || !station.longitude) {
    return '--'
  }
  const distance = calculateDistance(currentLocation.value, {
    latitude: station.latitude,
    longitude: station.longitude
  })
  return `${distance.toFixed(2)} km / ${calculateArrivalTime(currentLocation.value, {
    latitude: station.latitude,
    longitude: station.longitude
  })}`
}

// 获取车辆信息
const getVehicleInfo = (vehicleId) => {
  if (!vehicleId) return ''
  const vehicle = vehicleList.value.find(v => v.id === parseInt(vehicleId))
  return vehicle ? vehicle.plateNumber : vehicleId
}

// 获取线路信息
const getRouteInfo = (routeId) => {
  if (!routeId) return ''
  const route = routeList.value.find(r => r.id === parseInt(routeId))
  return route ? route.routeName : routeId
}

// 获取班次信息
const getScheduleInfo = (scheduleId) => {
  if (!scheduleId) return ''
  const parsedId = parseInt(scheduleId)
  const schedule = allScheduleList.value.find(s => s.id === parsedId) || scheduleList.value.find(s => s.id === parsedId)
  return schedule ? schedule.departureTime : scheduleId
}

// 计算到起点站的距离
const calculateDistanceToStartStation = (routeId) => {
  if (!currentLocation.value) return 0
  // 查找该线路
  const route = routeList.value.find(r => r.id === routeId)
  if (!route) return 0
  // 查找起点站
  const startStation = stationList.value.find(s => s.stationName === route.startStation)
  if (!startStation || !startStation.latitude || !startStation.longitude) return 0
  // 计算距离
  return calculateDistance(currentLocation.value, { latitude: startStation.latitude, longitude: startStation.longitude })
}

// 计算到起点站的预计到达时间
const calculateArrivalTimeToStartStation = (routeId) => {
  if (!currentLocation.value) return '--'
  // 查找该线路
  const route = routeList.value.find(r => r.id === routeId)
  if (!route) return '--'
  // 查找起点站
  const startStation = stationList.value.find(s => s.stationName === route.startStation)
  if (!startStation || !startStation.latitude || !startStation.longitude) return '--'
  // 计算到达时间
  return calculateArrivalTime(currentLocation.value, { latitude: startStation.latitude, longitude: startStation.longitude })
}

// 计算到终点站的距离
const calculateDistanceToEndStation = (routeId) => {
  if (!currentLocation.value) return 0
  // 查找该线路
  const route = routeList.value.find(r => r.id === routeId)
  if (!route) return 0
  // 查找终点站
  const endStation = stationList.value.find(s => s.stationName === route.endStation)
  if (!endStation || !endStation.latitude || !endStation.longitude) return 0
  // 计算距离
  return calculateDistance(currentLocation.value, { latitude: endStation.latitude, longitude: endStation.longitude })
}

// 计算到终点站的预计到达时间
const calculateArrivalTimeToEndStation = (routeId) => {
  if (!currentLocation.value) return '--'
  // 查找该线路
  const route = routeList.value.find(r => r.id === routeId)
  if (!route) return '--'
  // 查找终点站
  const endStation = stationList.value.find(s => s.stationName === route.endStation)
  if (!endStation || !endStation.latitude || !endStation.longitude) return '--'
  // 计算到达时间
  return calculateArrivalTime(currentLocation.value, { latitude: endStation.latitude, longitude: endStation.longitude })
}

// 获取学生的卡ID
const getStudentCardId = () => {
  try {
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    
    if (user && user.cardId) {
      return user.cardId
    }
    
    return null
  } catch (error) {
    console.error('获取卡ID失败:', error)
    return null
  }
}

// 扫码上车
const handleScanQRCode = async () => {
  try {
    if (!studentProfileComplete.value) {
      ElMessage.warning(studentProfileIncompleteMessage)
      return
    }
    // 验证表单
    if (!scanForm.routeId) {
      ElMessage.error('请选择线路')
      return
    }
    if (!scanForm.vehicleId) {
      ElMessage.error('请选择车辆')
      return
    }
    if (!scanForm.scheduleId) {
      ElMessage.error('请选择班次')
      return
    }
    
    const vehicle = vehicleList.value.find(v => v.id === scanForm.vehicleId)
    const seatCount = vehicle?.seatCount || 30
    const seatNumber = Math.floor(Math.random() * seatCount) + 1
    
    // 获取学生的卡ID
    const cardId = getStudentCardId()
    if (!cardId) {
      ElMessage.error('未获取到校园卡信息，请重新登录或先完成开卡')
      return
    }
    
    // 调用后端接口记录乘车记录
    const recordData = {
      cardId: cardId,
      routeId: scanForm.routeId,
      vehicleId: scanForm.vehicleId,
      scheduleId: scanForm.scheduleId,
      seatNumber: seatNumber
    }
    
    const res = await addCommuteRecord(recordData)
    
    if (res.code === 0) {
      // 显示扫码结果
      const route = routeList.value.find(r => r.id === scanForm.routeId)
      const schedule = scheduleList.value.find(s => s.id === scanForm.scheduleId)
      scanResult.value = {
        success: true,
        routeName: route ? route.routeName : '未知线路',
        vehiclePlateNumber: vehicle ? vehicle.plateNumber : '未知车辆',
        scheduleTime: schedule ? schedule.departureTime : '未知班次',
        seatNumber: seatNumber,
        scanTime: new Date().toLocaleString()
      }
      
      // 重新加载车辆和班次列表
      await loadVehicleList()
      if (scanForm.routeId) {
        await loadScheduleList(scanForm.routeId)
      }
      await loadAllSchedules()
      
      // 重新加载乘车记录
      await loadMyRecordData()
      
      // 3秒后清除扫码结果
      setTimeout(() => {
        scanResult.value = null
      }, 3000)
    } else {
      scanResult.value = {
        success: false,
        message: res.msg || '扫码失败，请重试'
      }
    }
  } catch (error) {
    console.error('扫码失败:', error)
    scanResult.value = {
      success: false,
      message: error.message || '扫码失败，请检查网络连接'
    }
  }
}

onMounted(async () => {
  await loadStudentInfo()
  await Promise.all([
    loadRouteList(),
    loadVehicleList(),
    loadStationList(),
    loadAllSchedules()
  ])
  await loadMyRecordData()
  // 自动获取当前位置
  getCurrentLocation()
  
  // 初始化过滤后的车辆列表
  filteredVehicleList.value = activeVehicleList.value
})
</script>

<style scoped>
.student-commute {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.el-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.scan-container {
  text-align: center;
  padding: 20px;
}

.scan-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: left;
}

.scan-result p {
  margin: 10px 0;
  line-height: 1.5;
}
</style>
