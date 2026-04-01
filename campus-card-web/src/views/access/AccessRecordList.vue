<template>
  <div class="access-record-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>门禁记录管理</span>
          <el-button type="primary" @click="handleExport">导出记录</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-4">
        <el-form-item label="卡号">
          <el-input v-model="searchForm.card_id" placeholder="请输入卡号" style="width: 150px" />
        </el-form-item>
        <el-form-item label="门禁点">
          <el-select v-model="searchForm.access_point_id" placeholder="请选择门禁点" style="width: 200px">
            <el-option 
              v-for="point in accessPoints" 
              :key="point.id" 
              :label="point.name" 
              :value="point.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 120px">
            <el-option label="成功" value="成功" />
            <el-option label="失败" value="失败" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="searchForm.start_date"
            type="date"
            placeholder="选择开始日期"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="searchForm.end_date"
            type="date"
            placeholder="选择结束日期"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="processedTableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="卡号" width="120">
          <template #default="scope">
            {{ scope.row.cardNo || scope.row.cardId }}
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="80" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
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
    
    <!-- 统计图表 -->
    <el-card class="mt-4">
      <template #header>
        <div class="card-header">
          <span>门禁统计</span>
        </div>
      </template>
      <div id="stat-chart" style="height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getAccessList, getAccessPoints, getAccessStatistics, exportAccessRecords } from '@/api/access'
import { getCardInfo } from '@/api/card'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const tableData = ref([])
const processedTableData = ref([])
const accessPoints = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const searchForm = reactive({
  card_id: '',
  access_point_id: '',
  status: '',
  start_date: '',
  end_date: ''
})
const chartInstance = ref(null)

const loadData = async () => {
  const params = {
    card_id: searchForm.card_id,
    access_point_id: searchForm.access_point_id,
    status: searchForm.status,
    start_date: searchForm.start_date ? searchForm.start_date.toISOString().split('T')[0] : '',
    end_date: searchForm.end_date ? searchForm.end_date.toISOString().split('T')[0] : '',
    page: pagination.current,
    size: pagination.size
  }
  try {
    const res = await getAccessList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
    
    // 处理数据，转换卡 ID 为卡号
    const processedData = await Promise.all(
      tableData.value.map(async (record) => {
        try {
          // 检查缓存
          if (cardNoCache.value[record.cardId]) {
            return {
              ...record,
              cardNo: cardNoCache.value[record.cardId]
            }
          }
          
          // 调用接口查询卡信息
          const cardRes = await getCardInfo(record.cardId)
          if (cardRes.code === 0) {
            const cardNo = cardRes.data.cardNo
            // 缓存结果
            cardNoCache.value[record.cardId] = cardNo
            return {
              ...record,
              cardNo: cardNo
            }
          }
          return record
        } catch (error) {
          console.error('查询卡号失败:', error)
          return record
        }
      })
    )
    
    processedTableData.value = processedData
  } catch (error) {
    console.error('获取门禁记录失败:', error)
  }
}

const loadAccessPoints = () => {
  getAccessPoints({}).then(res => {
    accessPoints.value = res.data.records
  })
}

const loadStatistics = () => {
  const params = {
    start_date: searchForm.start_date ? searchForm.start_date.toISOString().split('T')[0] : '',
    end_date: searchForm.end_date ? searchForm.end_date.toISOString().split('T')[0] : ''
  }
  getAccessStatistics(params).then(res => {
    const data = res.data
    const dates = data.map(item => item.date)
    const counts = data.map(item => item.total_count)
    
    if (!chartInstance.value) {
      chartInstance.value = echarts.init(document.getElementById('stat-chart'))
    }
    
    const option = {
      title: {
        text: '门禁通行统计'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: counts,
        type: 'line',
        smooth: true
      }]
    }
    
    chartInstance.value.setOption(option)
  })
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
  loadStatistics()
}

const resetForm = () => {
  searchForm.card_id = ''
  searchForm.access_point_id = ''
  searchForm.status = ''
  searchForm.start_date = ''
  searchForm.end_date = ''
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

const handleExport = () => {
  const params = {
    card_id: searchForm.card_id,
    start_date: searchForm.start_date ? searchForm.start_date.toISOString().split('T')[0] : '',
    end_date: searchForm.end_date ? searchForm.end_date.toISOString().split('T')[0] : ''
  }
  exportAccessRecords(params).then(res => {
    const blob = res.data
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'access_records.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  })
}

// 卡号缓存，避免重复查询
const cardNoCache = ref({})

const getCardNo = async (cardId) => {
  if (!cardId) return ''
  
  // 检查缓存
  if (cardNoCache.value[cardId]) {
    return cardNoCache.value[cardId]
  }
  
  try {
    // 调用接口查询卡信息
    const res = await getCardInfo(cardId)
    if (res.code === 0) {
      const cardNo = res.data.cardNo
      // 缓存结果
      cardNoCache.value[cardId] = cardNo
      return cardNo
    }
  } catch (error) {
    console.error('查询卡号失败:', error)
  }
  
  return ''
}

const getStatusType = (status) => {
  return status === '成功' ? 'success' : 'danger'
}

onMounted(() => {
  loadData()
  loadAccessPoints()
  loadStatistics()
  
  window.addEventListener('resize', () => {
    chartInstance.value && chartInstance.value.resize()
  })
})

onUnmounted(() => {
  chartInstance.value && chartInstance.value.dispose()
  window.removeEventListener('resize', () => {
    chartInstance.value && chartInstance.value.resize()
  })
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

.mt-4 {
  margin-top: 20px;
}
</style>