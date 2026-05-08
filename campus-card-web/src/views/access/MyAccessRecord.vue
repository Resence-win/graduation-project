<template>
  <div class="my-access-record">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的门禁记录</span>
          <el-button type="primary" @click="showQRCodeDialog = true">二维码开门</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="accessPointId" label="门禁点ID" />
        <el-table-column prop="direction" label="方向" width="80" />
        <el-table-column prop="location" label="位置" />
        <el-table-column label="定位距离" width="120">
          <template #default="scope">
            {{ scope.row.distance !== null && scope.row.distance !== undefined ? Number(scope.row.distance).toFixed(1) + '米' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accessTime" label="通行时间" width="180" />
        <el-table-column prop="deviceInfo" label="设备信息" />
      </el-table>
      
      <div class="pagination" style="margin-top: 20px">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 二维码开门对话框 -->
    <el-dialog
      v-model="showQRCodeDialog"
      title="二维码开门"
      width="400px"
    >
      <div class="qr-code-container">
        <el-form :model="qrForm" label-width="100px">
          <el-form-item label="门禁点" required>
            <el-select v-model="qrForm.access_point_id" placeholder="请选择门禁点" @change="clearAccessLocation">
              <el-option 
                v-for="point in accessPoints" 
                :key="point.id" 
                :label="point.name" 
                :value="point.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="location-info" v-if="currentLocation">
          <span>{{ currentLocation }}</span>
          <span v-if="selectedAccessPointDistance !== null">，距门禁约 {{ selectedAccessPointDistance.toFixed(1) }} 米</span>
        </div>
        <div class="location-error" v-else-if="locationError">{{ locationError }}</div>
        <div class="qr-code" v-if="qrCodeUrl">
          <img :src="qrCodeUrl" alt="二维码" />
          <p class="qr-tip">请将二维码对准门禁设备扫描</p>
        </div>
        <div class="qr-loading" v-else>
          <el-icon class="is-loading"><loading /></el-icon>
          <span>生成二维码中...</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showQRCodeDialog = false">关闭</el-button>
          <el-button type="primary" @click="refreshQRCode" :loading="locating">定位并开门</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { getMyAccessRecords, getAccessPoints, createQRAccess } from '@/api/access'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const tableData = ref([])
const accessPoints = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const showQRCodeDialog = ref(false)
const qrForm = reactive({
  access_point_id: ''
})
const qrCodeUrl = ref('')
const cardId = ref(1) // 这里应该从登录状态中获取卡号
const currentLatitude = ref(null)
const currentLongitude = ref(null)
const currentLocation = ref('')
const locationError = ref('')
const locating = ref(false)

const selectedAccessPoint = computed(() => {
  return accessPoints.value.find(point => point.id === qrForm.access_point_id) || null
})

const selectedAccessPointDistance = computed(() => {
  if (!selectedAccessPoint.value || currentLatitude.value === null || currentLongitude.value === null) {
    return null
  }
  if (selectedAccessPoint.value.latitude === null || selectedAccessPoint.value.longitude === null) {
    return null
  }
  return calculateDistance(
    currentLatitude.value,
    currentLongitude.value,
    selectedAccessPoint.value.latitude,
    selectedAccessPoint.value.longitude
  )
})

const loadData = () => {
  const params = {
    card_id: cardId.value,
    page: pagination.current,
    size: pagination.size
  }
  getMyAccessRecords(params).then(res => {
    tableData.value = res.data.records
    pagination.total = res.data.total
  })
}

const loadAccessPoints = () => {
  getAccessPoints({}).then(res => {
    accessPoints.value = res.data.records
  })
}

const generateQRCode = () => {
  if (!qrForm.access_point_id) {
    ElMessage.warning('请选择门禁点')
    return
  }
  
  // 生成二维码（这里简化处理，实际应使用JWT等方式生成）
  const qrCode = Math.random().toString(36).substring(2, 15)
  qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrCode}`
  
  // 模拟二维码有效期为10秒
  setTimeout(() => {
    if (showQRCodeDialog.value) {
      qrCodeUrl.value = ''
      ElMessage.warning('二维码已过期，请重新生成')
    }
  }, 10000)
}

const refreshQRCode = () => {
  openAccessByLocation()
}

const clearAccessLocation = () => {
  qrCodeUrl.value = ''
  currentLocation.value = ''
  locationError.value = ''
}

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const earthRadius = 6371e3
  const latDistance = (lat2 - lat1) * Math.PI / 180
  const lonDistance = (lon2 - lon1) * Math.PI / 180
  const startLat = lat1 * Math.PI / 180
  const endLat = lat2 * Math.PI / 180
  const a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
    + Math.cos(startLat) * Math.cos(endLat)
    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return earthRadius * c
}

const getBrowserLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位'))
      return
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 30000
    })
  })
}

const openAccessByLocation = async () => {
  if (!qrForm.access_point_id) {
    ElMessage.warning('请选择门禁点')
    return
  }

  locating.value = true
  locationError.value = ''
  try {
    const position = await getBrowserLocation()
    currentLatitude.value = position.coords.latitude
    currentLongitude.value = position.coords.longitude
    currentLocation.value = `当前位置: 纬度 ${currentLatitude.value.toFixed(6)}, 经度 ${currentLongitude.value.toFixed(6)}`
    const qrCode = Math.random().toString(36).substring(2, 15)
    qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrCode)}`

    const res = await createQRAccess({
      card_id: cardId.value,
      access_point_id: qrForm.access_point_id,
      qr_code: qrCode,
      actual_latitude: currentLatitude.value,
      actual_longitude: currentLongitude.value,
      device_info: navigator.userAgent
    })
    if (res.code === 0) {
      ElMessage.success(`开门成功，当前距离约 ${Number(res.data.distance || 0).toFixed(1)} 米`)
      loadData()
    }
  } catch (error) {
    locationError.value = error.message || '获取位置或开门失败'
    ElMessage.error(locationError.value)
  } finally {
    locating.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadData()
  loadAccessPoints()
})

// 监听二维码开门对话框的显示
watch(() => showQRCodeDialog.value, (newVal) => {
  if (newVal) {
    qrCodeUrl.value = ''
    qrForm.access_point_id = ''
    currentLocation.value = ''
    locationError.value = ''
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

.qr-code-container {
  text-align: center;
  padding: 20px 0;
}

.qr-code {
  margin-top: 20px;
}

.qr-code img {
  width: 200px;
  height: 200px;
}

.qr-tip {
  margin-top: 10px;
  color: #606266;
}

.location-info,
.location-error {
  margin-top: 12px;
  font-size: 13px;
}

.location-info {
  color: #409eff;
}

.location-error {
  color: #f56c6c;
}

.qr-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
}

.qr-loading .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}
</style>
