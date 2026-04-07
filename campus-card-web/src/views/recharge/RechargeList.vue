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
        <el-table-column prop="id" label="ID" width="80" />
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
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="银行卡" value="银行卡" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRechargeList, rechargeByCardNo } from '@/api/recharge'

const formRef = ref(null)
const dialogVisible = ref(false)
const tableData = ref([])

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
    
    const res = await rechargeByCardNo(rechargeData)
    if (res.code === 0) {
      ElMessage.success('充值成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
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
</style>
