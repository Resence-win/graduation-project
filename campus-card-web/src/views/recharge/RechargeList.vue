<template>
  <div class="recharge-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充值管理</span>
          <el-button type="primary" @click="handleRecharge">充值</el-button>
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
        <el-table-column label="ID" width="80">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="cardNo" label="账户卡号" width="180" />
        <el-table-column prop="amount" label="充值金额" width="120">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: bold">+¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rechargeType" label="充值方式" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="充值时间" width="180" />
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
      v-model="dialogVisible"
      title="充值"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="卡号" prop="cardNo">
          <el-input v-model="form.cardNo" placeholder="请输入卡号" />
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :max="10000" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="充值方式" prop="rechargeType">
          <el-select v-model="form.rechargeType" placeholder="请选择充值方式" style="width: 100%">
            <el-option label="现金" value="现金" />
            <el-option label="支付宝" value="支付宝" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="alipayDialogVisible"
      title="支付宝沙箱支付"
      width="420px"
    >
      <div class="alipay-status">
        <p>已打开支付宝沙箱收银台，支付完成后请返回本页面查询状态。</p>
        <el-tag :type="alipayStatus === 'success' ? 'success' : alipayStatus === 'failed' ? 'danger' : 'warning'">
          {{ alipayStatus === 'success' ? '支付成功，已入账' : alipayStatus === 'querying' ? '正在查询支付结果' : alipayStatus === 'failed' ? '支付未完成或查询失败' : '等待支付结果' }}
        </el-tag>
      </div>
      <template #footer>
        <el-button @click="closeAlipayDialog">关闭</el-button>
        <el-button @click="reopenAlipayPage" :disabled="!alipayFormHtml">重新打开收银台</el-button>
        <el-button type="primary" @click="checkAlipayStatus" :loading="alipayQuerying">查询支付结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRechargeList, rechargeByCardNo, createAlipayPagePay, queryAlipayRechargeStatus } from '@/api/recharge'

const formRef = ref(null)
const dialogVisible = ref(false)
const alipayDialogVisible = ref(false)
const tableData = ref([])
const alipayOutTradeNo = ref('')
const alipayFormHtml = ref('')
const alipayStatus = ref('waiting')
const alipayPolling = ref(null)
const alipayQuerying = ref(false)

const searchForm = reactive({
  cardNo: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  cardNo: null,
  amount: 100,
  rechargeType: '现金'
})

const rules = {
  cardNo: [
    { required: true, message: '请输入卡号', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' }
  ],
  rechargeType: [
    { required: true, message: '请选择充值方式', trigger: 'change' }
  ]
}

const loadData = async () => {
  try {
    const res = await getRechargeList({
      page: pagination.page,
      size: pagination.size,
      card_no: searchForm.cardNo
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
  searchForm.cardNo = ''
  loadData()
}

const handleRecharge = () => {
  dialogVisible.value = true
  Object.assign(form, {
    cardNo: null,
    amount: 100,
    rechargeType: '现金'
  })
}

const handleSubmit = async () => {
  let payWindow = null
  try {
    await formRef.value.validate()
    
    // 获取当前登录用户信息
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    
    // 构建充值请求参数，添加操作人信息
    const rechargeData = {
      ...form,
      operatorId: user ? user.id : 1, // 默认操作为1（系统）
      operatorName: user ? user.username : '系统'
    }
    
    if (form.rechargeType === '支付宝') {
      payWindow = window.open('', '_blank')
      if (payWindow) {
        payWindow.document.write('<p style="font-family: sans-serif; padding: 24px;">正在创建支付宝沙箱订单...</p>')
      }
      const res = await createAlipayPagePay(rechargeData)
      if (res.code === 0 && res.data) {
        alipayOutTradeNo.value = res.data.outTradeNo
        alipayFormHtml.value = res.data.formHtml
        alipayStatus.value = 'waiting'
        dialogVisible.value = false
        alipayDialogVisible.value = true
        openAlipayForm(alipayFormHtml.value, payWindow)
        startAlipayPolling()
      } else if (payWindow) {
        payWindow.close()
      }
      return
    }

    const res = await rechargeByCardNo(rechargeData)
    if (res.code === 0) {
      ElMessage.success('充值成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (payWindow) {
      payWindow.close()
    }
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
}

const openAlipayForm = (formHtml, existingWindow = null) => {
  const payWindow = existingWindow || window.open('', '_blank')
  if (!payWindow) {
    ElMessage.warning('浏览器拦截了支付宝收银台窗口，请允许弹窗后点击重新打开')
    return
  }
  payWindow.document.open()
  payWindow.document.write(formHtml)
  payWindow.document.close()
}

const reopenAlipayPage = () => {
  if (!alipayFormHtml.value) {
    ElMessage.warning('暂无可打开的支付宝订单')
    return
  }
  openAlipayForm(alipayFormHtml.value)
}

const checkAlipayStatus = async (silent = false) => {
  if (!alipayOutTradeNo.value) {
    ElMessage.warning('暂无支付宝充值订单')
    return
  }
  if (alipayQuerying.value) {
    return
  }
  alipayQuerying.value = true
  alipayStatus.value = 'querying'
  try {
    const res = await queryAlipayRechargeStatus(alipayOutTradeNo.value)
    if (res.code === 0 && res.data) {
      if (res.data.settled) {
        alipayStatus.value = 'success'
        ElMessage.success(`支付宝充值成功，已入账 ${res.data.amount} 元`)
        stopAlipayPolling()
        loadData()
      } else if (res.data.status === 'CLOSED') {
        alipayStatus.value = 'failed'
        ElMessage.warning(res.data.message || '支付宝交易已关闭')
        stopAlipayPolling()
      } else {
        alipayStatus.value = 'waiting'
        if (!silent) {
          ElMessage.info(res.data.message || '支付尚未完成')
        }
      }
    }
  } catch (error) {
    alipayStatus.value = 'waiting'
    if (!silent) {
      ElMessage.warning('支付宝订单查询超时或失败，请稍后重试')
    }
    console.error('查询支付宝订单失败:', error)
  } finally {
    alipayQuerying.value = false
  }
}

const startAlipayPolling = () => {
  stopAlipayPolling()
  let pollingCount = 0
  const poll = async () => {
    if (!alipayDialogVisible.value || pollingCount >= 24 || alipayStatus.value === 'success' || alipayStatus.value === 'failed') {
      stopAlipayPolling()
      return
    }
    pollingCount += 1
    await checkAlipayStatus(true)
    if (alipayPolling.value !== null && alipayDialogVisible.value && alipayStatus.value !== 'success' && alipayStatus.value !== 'failed') {
      alipayPolling.value = setTimeout(poll, 5000)
    }
  }
  alipayPolling.value = setTimeout(poll, 5000)
}

const stopAlipayPolling = () => {
  if (alipayPolling.value) {
    clearTimeout(alipayPolling.value)
    alipayPolling.value = null
  }
}

const closeAlipayDialog = () => {
  stopAlipayPolling()
  alipayDialogVisible.value = false
  alipayStatus.value = 'waiting'
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

onUnmounted(() => {
  stopAlipayPolling()
})
</script>

<style scoped>
.recharge-list {
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

.alipay-status {
  text-align: center;
  line-height: 28px;
}
</style>
