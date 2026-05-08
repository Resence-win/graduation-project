<template>
  <div class="student-product">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品消费</span>
          <el-tag type="success">余额：¥{{ cardInfo.balance || '0.00' }}</el-tag>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商户">
          <el-select v-model="searchForm.merchantId" placeholder="请选择商户" clearable style="width: 180px">
            <el-option v-for="merchant in merchantList" :key="merchant.id" :label="merchant.merchantName" :value="merchant.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="16">
        <el-col v-for="product in productList" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="product-card" shadow="hover">
            <div class="product-image">
              <el-image v-if="product.image" :src="'/api' + product.image" fit="cover" />
              <div v-else class="image-placeholder">{{ product.productName.slice(0, 1) }}</div>
            </div>
            <div class="product-name">{{ product.productName }}</div>
            <div class="product-meta">{{ product.merchantName || '校内商户' }}</div>
            <div class="product-desc">{{ product.description || '暂无描述' }}</div>
            <div class="product-footer">
              <span class="price">¥{{ product.price }}</span>
              <span class="stock">库存 {{ product.stock }}</span>
            </div>
            <el-button type="primary" style="width: 100%; margin-top: 12px" :disabled="product.stock <= 0" @click="handleBuy(product)">
              {{ product.stock > 0 ? '购买' : '已售罄' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!productList.length" description="暂无可购买商品" />

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[8, 16, 32]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="orderDialogVisible" title="确认消费" width="420px">
      <el-form label-width="90px">
        <el-form-item label="商品">
          <span>{{ currentProduct?.productName }}</span>
        </el-form-item>
        <el-form-item label="商户">
          <span>{{ currentProduct?.merchantName }}</span>
        </el-form-item>
        <el-form-item label="单价">
          <span>¥{{ currentProduct?.price }}</span>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="currentProduct?.stock || 1" :precision="0" />
        </el-form-item>
        <el-form-item label="合计">
          <strong>¥{{ orderAmount }}</strong>
        </el-form-item>
        <el-form-item label="卡号">
          <span>{{ cardInfo.cardNo }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="handleOrderSubmit">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantList } from '@/api/merchant'
import { getProductList, orderProduct } from '@/api/product'
import { getCardByUserNo } from '@/api/card'

const productList = ref([])
const merchantList = ref([])
const orderDialogVisible = ref(false)
const currentProduct = ref(null)
const ordering = ref(false)

const searchForm = reactive({
  productName: '',
  merchantId: null
})

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0
})

const cardInfo = reactive({
  id: null,
  cardNo: '',
  balance: '0.00',
  status: 1
})

const orderForm = reactive({
  quantity: 1
})

const orderAmount = computed(() => {
  if (!currentProduct.value) return '0.00'
  return (Number(currentProduct.value.price) * orderForm.quantity).toFixed(2)
})

const loadCardInfo = async () => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  if (!user) return
  const res = await getCardByUserNo(user.username, user.role)
  if (res.code === 0 && res.data) {
    Object.assign(cardInfo, res.data)
  }
}

const loadProducts = async () => {
  const res = await getProductList({
    page: pagination.page,
    size: pagination.size,
    status: 1,
    productName: searchForm.productName,
    merchantId: searchForm.merchantId
  })
  if (res.code === 0) {
    productList.value = res.data.records || []
    pagination.total = res.data.total || 0
  }
}

const loadMerchants = async () => {
  const res = await getMerchantList({ page: 1, size: 1000 })
  if (res.code === 0) {
    merchantList.value = res.data.records || []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadProducts()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.merchantId = null
  loadProducts()
}

const handleBuy = (product) => {
  if (!cardInfo.cardNo) {
    ElMessage.warning('当前账号未绑定校园卡')
    return
  }
  if (cardInfo.status !== 1) {
    ElMessage.warning('校园卡状态异常，无法消费')
    return
  }
  currentProduct.value = product
  orderForm.quantity = 1
  orderDialogVisible.value = true
}

const handleOrderSubmit = async () => {
  if (!currentProduct.value) return
  ordering.value = true
  try {
    const res = await orderProduct({
      cardNo: cardInfo.cardNo,
      productId: currentProduct.value.id,
      quantity: orderForm.quantity
    })
    if (res.code === 0) {
      ElMessage.success('消费成功')
      orderDialogVisible.value = false
      await loadCardInfo()
      loadProducts()
    }
  } finally {
    ordering.value = false
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadProducts()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadProducts()
}

onMounted(() => {
  loadCardInfo()
  loadMerchants()
  loadProducts()
})
</script>

<style scoped>
.student-product {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-card {
  margin-bottom: 16px;
}

.product-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
}

.product-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 34px;
  font-weight: 600;
}

.product-name {
  margin-top: 12px;
  font-weight: 600;
  color: #303133;
}

.product-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

.product-desc {
  margin-top: 8px;
  height: 38px;
  color: #909399;
  font-size: 13px;
  line-height: 19px;
  overflow: hidden;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

.stock {
  color: #909399;
  font-size: 13px;
}

.el-pagination {
  display: flex;
}
</style>
