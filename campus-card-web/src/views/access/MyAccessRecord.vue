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
            <el-select v-model="qrForm.access_point_id" placeholder="请选择门禁点">
              <el-option 
                v-for="point in accessPoints" 
                :key="point.id" 
                :label="point.name" 
                :value="point.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
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
          <el-button type="primary" @click="refreshQRCode">刷新二维码</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
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
  generateQRCode()
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