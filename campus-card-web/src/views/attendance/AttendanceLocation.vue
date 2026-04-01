<template>
  <div class="attendance-location">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>打卡位置管理</span>
          <el-button type="primary" @click="handleAddLocation">添加打卡位置</el-button>
        </div>
      </template>
      
      <el-table :data="locationList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="locationName" label="位置名称" />
        <el-table-column prop="location" label="位置描述" />
        <el-table-column prop="latitude" label="纬度" width="120" />
        <el-table-column prop="longitude" label="经度" width="120" />
        <el-table-column prop="radius" label="打卡半径(米)" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditLocation(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteLocation(row.id)">删除</el-button>
            <el-button size="small" @click="handleViewRecords(row.id)">查看记录</el-button>
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
      width="500px"
    >
      <el-form :model="locationForm" label-width="100px">
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
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="locationForm.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="locationForm.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="locationForm.status" placeholder="请选择状态">
            <el-option label="有效" value="1" />
            <el-option label="无效" value="0" />
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
      title="打卡记录"
      width="800px"
    >
      <el-table :data="recordList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardId" label="卡ID" width="100" />
        <el-table-column prop="status" label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualLocation" label="实际打卡地点" />
        <el-table-column prop="actualLatitude" label="实际纬度" width="120" />
        <el-table-column prop="actualLongitude" label="实际经度" width="120" />
        <el-table-column prop="deviceInfo" label="设备信息" />
        <el-table-column prop="recordTime" label="记录时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  createLocation, 
  updateLocation, 
  deleteLocation, 
  getLocationsByTeacherId, 
  getAttendanceRecordsByLocationId 
} from '@/api/attendance'

const locationList = ref([])
const recordList = ref([])
const dialogVisible = ref(false)
const recordDialogVisible = ref(false)
const dialogTitle = ref('添加打卡位置')
const currentLocationId = ref(null)
const currentTeacherId = ref(1) // 从用户信息中获取老师ID

// 获取当前老师ID
const getCurrentTeacherId = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    // 假设用户信息中包含老师ID，实际需要根据后端返回的用户信息结构调整
    currentTeacherId.value = user.id || 1
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

const getStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '缺勤': 'danger'
  }
  return map[status] || 'info'
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
    const res = await getLocationsByTeacherId(currentTeacherId.value, {
      page: pagination.page,
      size: pagination.size
    })
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
  }
}

const handleAddLocation = () => {
  dialogTitle.value = '添加打卡位置'
  currentLocationId.value = null
  Object.assign(locationForm, {
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
  dialogVisible.value = true
}

const handleEditLocation = (row) => {
  dialogTitle.value = '编辑打卡位置'
  currentLocationId.value = row.id
  Object.assign(locationForm, {
    locationName: row.locationName,
    location: row.location,
    latitude: row.latitude,
    longitude: row.longitude,
    radius: row.radius,
    startTime: row.startTime ? new Date(row.startTime) : null,
    endTime: row.endTime ? new Date(row.endTime) : null,
    status: row.status,
    teacherId: currentTeacherId.value
  })
  dialogVisible.value = true
}

const handleSaveLocation = async () => {
  try {
    let res
    if (currentLocationId.value) {
      // 更新
      res = await updateLocation({
        id: currentLocationId.value,
        ...locationForm
      })
    } else {
      // 添加
      res = await createLocation(locationForm)
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

const handleViewRecords = (locationId) => {
  currentLocationId.value = locationId
  recordPagination.page = 1
  loadRecords(locationId)
  recordDialogVisible.value = true
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
  loadRecords(currentLocationId.value)
}

const handleRecordCurrentChange = (val) => {
  recordPagination.page = val
  loadRecords(currentLocationId.value)
}

onMounted(() => {
  getCurrentTeacherId()
  loadLocations()
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

.dialog-footer {
  text-align: right;
}
</style>