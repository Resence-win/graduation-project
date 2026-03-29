<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>消费统计</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleDateChange"
              />
            </div>
          </template>
          <div ref="consumeChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户消费排行</span>
            </div>
          </template>
          <div ref="userRankChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>商户收入排行</span>
            </div>
          </template>
          <div ref="merchantRankChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getConsumeStat, getUserRank, getMerchantRank } from '@/api/statistics'

const consumeChartRef = ref(null)
const userRankChartRef = ref(null)
const merchantRankChartRef = ref(null)

let consumeChart = null
let userRankChart = null
let merchantRankChart = null

const dateRange = ref([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

const initConsumeChart = (data) => {
  if (consumeChart) {
    consumeChart.dispose()
  }
  
  consumeChart = echarts.init(consumeChartRef.value)
  
  const dates = data.map(item => item.date)
  const amounts = data.map(item => item.total_amount)
  
  const option = {
    title: {
      text: '消费趋势统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>消费金额: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      name: '日期'
    },
    yAxis: {
      type: 'value',
      name: '消费金额(元)'
    },
    series: [{
      name: '消费金额',
      type: 'line',
      data: amounts,
      smooth: true,
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(64, 158, 255, 0.05)'
          }]
        }
      }
    }]
  }
  
  consumeChart.setOption(option)
}

const initUserRankChart = (data) => {
  if (userRankChart) {
    userRankChart.dispose()
  }
  
  userRankChart = echarts.init(userRankChartRef.value)
  
  const names = data.map(item => item.name || `用户${item.userId}`)
  const amounts = data.map(item => item.totalAmount)
  
  const option = {
    title: {
      text: '用户消费排行',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>消费金额: ¥{c}'
    },
    xAxis: {
      type: 'value',
      name: '消费金额(元)'
    },
    yAxis: {
      type: 'category',
      data: names,
      inverse: true
    },
    series: [{
      name: '消费金额',
      type: 'bar',
      data: amounts,
      itemStyle: {
        color: '#67C23A'
      }
    }]
  }
  
  userRankChart.setOption(option)
}

const initMerchantRankChart = (data) => {
  if (merchantRankChart) {
    merchantRankChart.dispose()
  }
  
  merchantRankChart = echarts.init(merchantRankChartRef.value)
  
  const names = data.map(item => item.merchantName)
  const amounts = data.map(item => item.totalAmount)
  
  const option = {
    title: {
      text: '商户收入排行',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '收入',
      type: 'pie',
      radius: '50%',
      data: data.map(item => ({
        value: item.totalAmount,
        name: item.merchantName
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  merchantRankChart.setOption(option)
}

const loadConsumeData = async () => {
  try {
    const res = await getConsumeStat({
      start_date: dateRange.value[0],
      end_date: dateRange.value[1]
    })
    if (res.code === 0) {
      initConsumeChart(res.data || [])
    }
  } catch (error) {
    console.error('加载消费统计失败:', error)
  }
}

const loadUserRank = async () => {
  try {
    const res = await getUserRank()
    if (res.code === 0) {
      initUserRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载用户排行失败:', error)
  }
}

const loadMerchantRank = async () => {
  try {
    const res = await getMerchantRank()
    if (res.code === 0) {
      initMerchantRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载商户排行失败:', error)
  }
}

const handleDateChange = () => {
  loadConsumeData()
}

const handleResize = () => {
  if (consumeChart) consumeChart.resize()
  if (userRankChart) userRankChart.resize()
  if (merchantRankChart) merchantRankChart.resize()
}

onMounted(() => {
  loadConsumeData()
  loadUserRank()
  loadMerchantRank()
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (consumeChart) consumeChart.dispose()
  if (userRankChart) userRankChart.dispose()
  if (merchantRankChart) merchantRankChart.dispose()
  
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
