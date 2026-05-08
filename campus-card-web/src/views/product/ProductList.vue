<template>
  <div class="product-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="所属商户">
          <el-select v-model="searchForm.merchantId" placeholder="请选择商户" clearable style="width: 180px">
            <el-option v-for="merchant in merchantList" :key="merchant.id" :label="merchant.merchantName" :value="merchant.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">
            {{ (pagination.page - 1) * pagination.size + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="merchantName" label="所属商户" width="160" />
        <el-table-column prop="price" label="价格" width="110">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="'/api' + row.image"
              style="width: 50px; height: 50px"
              :preview-src-list="['/api' + row.image]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleUpload(row)">上传图片</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="所属商户" prop="merchantId">
          <el-select v-model="form.merchantId" placeholder="请选择商户" style="width: 100%">
            <el-option v-for="merchant in merchantList" :key="merchant.id" :label="merchant.merchantName" :value="merchant.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :max="9999" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="99999" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传商品图片" width="400px">
      <el-upload ref="uploadRef" :auto-upload="false" :limit="1" accept="image/*" :on-change="handleFileChange">
        <el-button type="primary">选择图片</el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传图片文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUploadSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantList } from '@/api/merchant'
import { getProductList, addProduct, updateProduct, deleteProduct, uploadProductImage } from '@/api/product'

const formRef = ref(null)
const uploadRef = ref(null)
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const tableData = ref([])
const merchantList = ref([])
const currentProductId = ref(null)
const uploadFile = ref(null)

const searchForm = reactive({
  productName: '',
  merchantId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  productName: '',
  merchantId: null,
  price: 1,
  stock: 0,
  description: '',
  status: 1
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  merchantId: [{ required: true, message: '请选择所属商户', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const loadData = async () => {
  const res = await getProductList({
    page: pagination.page,
    size: pagination.size,
    ...searchForm
  })
  if (res.code === 0) {
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  }
}

const loadMerchantList = async () => {
  const res = await getMerchantList({ page: 1, size: 1000 })
  if (res.code === 0) {
    merchantList.value = res.data.records || []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.merchantId = null
  searchForm.status = null
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增商品'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    productName: '',
    merchantId: null,
    price: 1,
    stock: 0,
    description: '',
    status: 1
  })
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  dialogVisible.value = true
  Object.assign(form, row)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const res = form.id ? await updateProduct(form) : await addProduct(form)
    if (res.code === 0) {
      ElMessage.success(form.id ? '更新成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
}

const handleUpload = (row) => {
  currentProductId.value = row.id
  uploadFile.value = null
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleUploadSubmit = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的图片')
    return
  }
  const formData = new FormData()
  formData.append('file', uploadFile.value)
  formData.append('product_id', currentProductId.value)
  const res = await uploadProductImage(formData)
  if (res.code === 0) {
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadData()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteProduct(row.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
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
  loadMerchantList()
  loadData()
})
</script>

<style scoped>
.product-list {
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
