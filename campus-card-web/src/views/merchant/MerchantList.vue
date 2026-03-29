<template>
  <div class="merchant-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商户管理</span>
          <div>
            <el-button type="success" @click="handleAddType">新增类型</el-button>
            <el-button type="primary" @click="handleAdd">新增商户</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.merchantName" placeholder="请输入商户名称" clearable />
        </el-form-item>
        <el-form-item label="商户类型">
          <el-select v-model="searchForm.typeId" placeholder="请选择类型" clearable>
            <el-option
              v-for="type in typeList"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="merchantName" label="商户名称" width="150" />
        <el-table-column prop="typeName" label="类型" width="120" />
        <el-table-column prop="location" label="位置" width="200" />
        <el-table-column prop="logo" label="Logo" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.logo"
              :src="row.logo"
              style="width: 50px; height: 50px"
              :preview-src-list="[row.logo]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleUpload(row)">上传Logo</el-button>
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
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商户名称" prop="merchantName">
          <el-input v-model="form.merchantName" placeholder="请输入商户名称" />
        </el-form-item>
        <el-form-item label="商户类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="type in typeList"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="typeDialogVisible"
      title="新增商户类型"
      width="400px"
    >
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-width="100px">
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="typeForm.typeName" placeholder="请输入类型名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitType">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传Logo"
      width="400px"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept="image/*"
        :on-change="handleFileChange"
      >
        <el-button type="primary">选择图片</el-button>
        <template #tip>
          <div class="el-upload__tip">
            只能上传jpg/png文件，且不超过2MB
          </div>
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
import {
  getMerchantList,
  getMerchantTypeList,
  addMerchant,
  updateMerchant,
  deleteMerchant,
  addMerchantType,
  uploadMerchantLogo
} from '@/api/merchant'

const formRef = ref(null)
const typeFormRef = ref(null)
const uploadRef = ref(null)
const dialogVisible = ref(false)
const typeDialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const dialogTitle = ref('新增商户')
const tableData = ref([])
const typeList = ref([])
const currentMerchantId = ref(null)
const uploadFile = ref(null)

const searchForm = reactive({
  merchantName: '',
  typeId: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  merchantName: '',
  typeId: null,
  location: ''
})

const typeForm = reactive({
  typeName: ''
})

const rules = {
  merchantName: [
    { required: true, message: '请输入商户名称', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择商户类型', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入位置', trigger: 'blur' }
  ]
}

const typeRules = {
  typeName: [
    { required: true, message: '请输入类型名称', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const res = await getMerchantList({
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

const loadTypeList = async () => {
  try {
    const res = await getMerchantTypeList()
    if (res.code === 0) {
      typeList.value = res.data || []
    }
  } catch (error) {
    console.error('加载类型列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.merchantName = ''
  searchForm.typeId = null
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增商户'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    merchantName: '',
    typeId: null,
    location: ''
  })
}

const handleAddType = () => {
  typeForm.typeName = ''
  typeDialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑商户'
  dialogVisible.value = true
  Object.assign(form, row)
}

const handleUpload = (row) => {
  currentMerchantId.value = row.id
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
  
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    formData.append('merchant_id', currentMerchantId.value)
    
    const res = await uploadMerchantLogo(formData)
    if (res.code === 0) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('上传失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteMerchant(row.id)
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

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    let res
    if (form.id) {
      res = await updateMerchant(form)
    } else {
      res = await addMerchant(form)
    }
    
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

const handleSubmitType = async () => {
  try {
    await typeFormRef.value.validate()
    
    const res = await addMerchantType(typeForm)
    if (res.code === 0) {
      ElMessage.success('新增成功')
      typeDialogVisible.value = false
      loadTypeList()
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
  loadTypeList()
})
</script>

<style scoped>
.merchant-list {
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
