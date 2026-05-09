<template>
  <div class="borrow-record-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借阅记录管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="借阅中" :value="1" />
            <el-option label="已归还" :value="2" />
            <el-option label="已逾期" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡ID">
          <el-input v-model="searchForm.cardId" placeholder="请输入卡ID" clearable />
        </el-form-item>
        <el-form-item label="图书ID">
          <el-input v-model="searchForm.bookId" placeholder="请输入图书ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column type="index" label="记录ID" width="90" :index="indexMethod" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" />
        <el-table-column prop="collectionLocation" label="馆藏地" min-width="160" />
        <el-table-column prop="cardNo" label="校园卡号" width="150" />
        <el-table-column prop="userName" label="借阅人" min-width="180" />
        <el-table-column prop="borrowTime" label="借阅时间" width="180" />
        <el-table-column prop="dueTime" label="到期时间" width="180" />
        <el-table-column prop="returnTime" label="归还时间" width="180">
          <template #default="{ row }">
            {{ row.returnTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="归还状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="逾期天数" width="100">
          <template #default="{ row }">
            <span :class="{ overdue: row.overdueDays > 0 }">{{ row.overdueDays || 0 }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleReturn(row)"
              :disabled="row.status !== 1 && row.status !== 3"
            >
              登记归还
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBorrowList, returnBook } from '@/api/book'

const tableData = ref([])

const searchForm = reactive({
  status: null,
  cardId: '',
  bookId: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  try {
    const res = await getBorrowList({
      page: pagination.page,
      size: pagination.size,
      status: searchForm.status,
      card_id: searchForm.cardId || undefined,
      book_id: searchForm.bookId || undefined
    })
    if (res.code === 0) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载借阅记录失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.status = null
  searchForm.cardId = ''
  searchForm.bookId = ''
  loadData()
}

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm('确定要登记该图书已归还吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await returnBook({ borrow_id: row.id })
    if (res.code === 0) {
      ElMessage.success('登记归还成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('登记归还失败:', error)
    }
  }
}

const getStatusType = (row) => {
  if (row.status === 3) {
    return 'danger'
  }
  if (row.status === 2 && row.overdueDays > 0) {
    return 'warning'
  }
  if (row.status === 2) {
    return 'success'
  }
  return 'info'
}

const getStatusText = (row) => {
  if (row.status === 3) {
    return '未归还/已逾期'
  }
  if (row.status === 2 && row.overdueDays > 0) {
    return '逾期归还'
  }
  if (row.status === 2) {
    return '已归还'
  }
  return '未归还'
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadData()
}

const indexMethod = (index) => {
  return (pagination.page - 1) * pagination.size + index + 1
}


onMounted(() => {
  loadData()
})
</script>

<style scoped>
.borrow-record-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overdue {
  color: #f56c6c;
  font-weight: 600;
}

.el-pagination {
  display: flex;
}
</style>
