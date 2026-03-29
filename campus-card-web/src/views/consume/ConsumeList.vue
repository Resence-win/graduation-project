<template>
  <div class="consume-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消费管理</span>
          <el-button type="primary" @click="handleConsume">消费</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.cardNo" placeholder="请输入卡号" clearable />
        </el-form-item>
        <el-form-item label="商户">
          <el-input v-model="searchForm.merchantId" placeholder="请输入商户ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="accountId" label="账户ID" width="100" />
        <el-table-column prop="merchantId" label="商户ID" width="100" />
        <el-table-column prop="amount" label="消费金额" width="120">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">-¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="消费后余额" width="120">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: bold">¥{{ row.balanceAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="consumeTime" label="消费时间" width="180" />
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
      title="消费"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="卡号" prop="cardId">
          <el-input v-model="form.cardId" placeholder="请输入卡号" />
        </el-form-item>
        <el-form-item label="商户" prop="merchantId">
          <el-select v-model="form.merchantId" placeholder="请选择商户" style="width: 100%">
            <el-option
              v-for="merchant in merchantList"
              :key="merchant.id"
              :label="merchant.merchantName"
              :value="merchant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消费金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :max="10000" :precision="2" style="width: 100%" />
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
import { getConsumeList, consume } from '@/api/consume'
import { getMerchantList } from '@/api/merchant'

const formRef = ref(null)
const dialogVisible = ref(false)
const tableData = ref([])
const merchantList = ref([])

const searchForm = reactive({
  cardNo: '',
  merchantId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  cardId: null,
  merchantId: null,
  amount: 10
})

const rules = {
  cardId: [
    { required: true, message: '请输入卡号', trigger: 'blur' }
  ],
  merchantId: [
    { required: true, message: '请选择商户', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入消费金额', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const res = await getConsumeList({
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

const loadMerchantList = async () => {
  try {
    const res = await getMerchantList({ page: 1, size: 1000 })
    if (res.code === 0) {
      merchantList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载商户列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.cardNo = ''
  searchForm.merchantId = null
  loadData()
}

const handleConsume = () => {
  dialogVisible.value = true
  Object.assign(form, {
    cardId: null,
    merchantId: null,
    amount: 10
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const res = await consume(form)
    if (res.code === 0) {
      ElMessage.success('消费成功')
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
  loadMerchantList()
})
</script>

<style scoped>
.consume-list {
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
