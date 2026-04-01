<template>
  <div class="access-point-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>门禁点管理</span>
          <el-button type="primary" @click="handleAdd">新增门禁点</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-4">
        <el-form-item label="门禁点名称">
          <el-input v-model="searchForm.name" placeholder="请输入门禁点名称" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 120px">
            <el-option label="正常" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="门禁点名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="deviceId" label="设备ID" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="门禁点名称" required>
          <el-input v-model="form.name" placeholder="请输入门禁点名称" />
        </el-form-item>
        <el-form-item label="位置" required>
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="设备ID" required>
          <el-input v-model="form.deviceId" placeholder="请输入设备ID" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAccessPoints, addAccessPoint, updateAccessPoint, deleteAccessPoint } from '@/api/access'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const searchForm = reactive({
  name: '',
  status: ''
})
const dialogVisible = ref(false)
const dialogTitle = ref('新增门禁点')
const form = reactive({
  id: '',
  name: '',
  location: '',
  deviceId: '',
  status: 1
})

const loadData = () => {
  const params = {
    name: searchForm.name,
    status: searchForm.status,
    page: pagination.current,
    size: pagination.size
  }
  getAccessPoints(params).then(res => {
    tableData.value = res.data.records
    pagination.total = res.data.total
  })
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetForm = () => {
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增门禁点'
  form.id = ''
  form.name = ''
  form.location = ''
  form.deviceId = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑门禁点'
  form.id = row.id
  form.name = row.name
  form.location = row.location
  form.deviceId = row.deviceId
  form.status = row.status
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个门禁点吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAccessPoint(id).then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
  }).catch(() => {
    // 取消删除
  })
}

const handleSubmit = () => {
  if (!form.name || !form.location || !form.deviceId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (form.id) {
    // 编辑
    updateAccessPoint(form).then(() => {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      loadData()
    })
  } else {
    // 新增
    addAccessPoint(form).then(() => {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      loadData()
    })
  }
}

onMounted(() => {
  loadData()
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
</style>