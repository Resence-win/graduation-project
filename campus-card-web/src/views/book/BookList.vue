<template>
  <div class="book-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图书管理</span>
          <el-button type="primary" @click="handleAdd">新增图书</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="书名">
          <el-input v-model="searchForm.bookName" placeholder="请输入书名" clearable />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="searchForm.author" placeholder="请输入作者" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="可借阅" :value="1" />
            <el-option label="已借出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="bookName" label="书名" width="200" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="logo" label="封面" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.logo"
              :src="'/api' + row.logo"
              style="width: 50px; height: 50px"
              :preview-src-list="['/api' + row.logo]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '可借阅' : '已借出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleUpload(row)">上传封面</el-button>
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="书名" prop="bookName">
          <el-input v-model="form.bookName" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传封面"
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
  getBookList,
  addBook,
  updateBook,
  deleteBook,
  uploadBookLogo
} from '@/api/book'

const formRef = ref(null)
const uploadRef = ref(null)
const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const tableData = ref([])
const currentBookId = ref(null)
const uploadFile = ref(null)

const searchForm = reactive({
  bookName: '',
  author: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  bookName: '',
  author: ''
})

const rules = {
  bookName: [
    { required: true, message: '请输入书名', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const res = await getBookList({
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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.bookName = ''
  searchForm.author = ''
  searchForm.status = null
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增图书'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    bookName: '',
    author: ''
  })
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑图书'
  dialogVisible.value = true
  Object.assign(form, row)
}

const handleUpload = (row) => {
  currentBookId.value = row.id
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
    formData.append('book_id', currentBookId.value)
    
    const res = await uploadBookLogo(formData)
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
    await ElMessageBox.confirm('确定要删除该图书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteBook(row.id)
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
      res = await updateBook(form)
    } else {
      res = await addBook(form)
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
.book-list {
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
