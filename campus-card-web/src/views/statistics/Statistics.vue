<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <div>
                <span>综合数据概览</span>
                <div class="sub-title">按所选时间自动汇总系统业务数据</div>
              </div>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleDateChange"
              />
            </div>
          </template>
          <el-row :gutter="16">
            <el-col
              v-for="item in overviewCards"
              :key="item.label"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="4"
              class="stat-col"
            >
              <div class="stat-card">
                <div class="stat-label">{{ item.label }}</div>
                <el-statistic
                  :value="item.value"
                  :precision="item.precision || 0"
                  :prefix="item.prefix || ''"
                  :suffix="item.suffix || ''"
                />
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>业务量概览</span>
            </div>
          </template>
          <div ref="businessChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>校园卡状态</span>
            </div>
          </template>
          <div ref="cardStatusChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>消费统计</span>
            </div>
          </template>
          <div ref="consumeChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户消费排行</span>
            </div>
          </template>
          <div ref="userRankChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>商户收入排行</span>
            </div>
          </template>
          <div ref="merchantRankChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getOverview, getConsumeStat, getUserRank, getMerchantRank } from '@/api/statistics'

const businessChartRef = ref(null)
const cardStatusChartRef = ref(null)
const consumeChartRef = ref(null)
const userRankChartRef = ref(null)
const merchantRankChartRef = ref(null)

let businessChart = null
let cardStatusChart = null
let consumeChart = null
let userRankChart = null
let merchantRankChart = null

const dateRange = ref([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

const overviewData = ref({})

const toNumber = (value) => Number(value || 0)

const overviewCards = computed(() => [
  { label: '学生总数', value: toNumber(overviewData.value.studentCount) },
  { label: '教师总数', value: toNumber(overviewData.value.teacherCount) },
  { label: '商户总数', value: toNumber(overviewData.value.merchantCount) },
  { label: '商品总数', value: toNumber(overviewData.value.productCount) },
  { label: '图书总数', value: toNumber(overviewData.value.bookCount) },
  { label: '正常卡数', value: toNumber(overviewData.value.activeCardCount) },
  { label: '消费金额', value: toNumber(overviewData.value.consumeAmount), precision: 2, prefix: '¥' },
  { label: '充值金额', value: toNumber(overviewData.value.rechargeAmount), precision: 2, prefix: '¥' },
  { label: '消费笔数', value: toNumber(overviewData.value.consumeCount) },
  { label: '充值笔数', value: toNumber(overviewData.value.rechargeCount) },
  { label: '借阅次数', value: toNumber(overviewData.value.borrowCount) },
  { label: '考勤记录', value: toNumber(overviewData.value.attendanceCount) }
])

const initBusinessChart = (data) => {
  if (businessChart) {
    businessChart.dispose()
  }

  businessChart = echarts.init(businessChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      top: 30,
      left: 45,
      right: 20,
      bottom: 40
    },
    xAxis: {
      type: 'category',
      data: ['消费', '充值', '借阅', '考勤', '门禁', '乘车']
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [{
      name: '业务量',
      type: 'bar',
      barWidth: 34,
      data: [
        toNumber(data.consumeCount),
        toNumber(data.rechargeCount),
        toNumber(data.borrowCount),
        toNumber(data.attendanceCount),
        toNumber(data.accessCount),
        toNumber(data.commuteRideCount)
      ],
      itemStyle: {
        color: '#409EFF',
        borderRadius: [6, 6, 0, 0]
      },
      label: {
        show: true,
        position: 'top'
      }
    }]
  }

  businessChart.setOption(option)
}

const initCardStatusChart = (data) => {
  if (cardStatusChart) {
    cardStatusChart.dispose()
  }

  cardStatusChart = echarts.init(cardStatusChartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 张 ({d}%)'
    },
    legend: {
      bottom: 0
    },
    series: [{
      name: '校园卡状态',
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '44%'],
      data: [
        { name: '正常', value: toNumber(data.activeCardCount) },
        { name: '挂失', value: toNumber(data.lostCardCount) },
        { name: '注销', value: toNumber(data.cancelledCardCount) }
      ],
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        formatter: '{b}\n{c}张'
      }
    }]
  }

  cardStatusChart.setOption(option)
}

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

  const names = data.map(item => item.user_name || `用户${item.userId}`)
  const amounts = data.map(item => item.total_amount)

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

  const names = data.map(item => item.merchant_name)
  const amounts = data.map(item => item.total_amount)

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
        value: item.total_amount,
        name: item.merchant_name
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

const loadOverview = async () => {
  try {
    const res = await getOverview({
      start_date: dateRange.value[0],
      end_date: dateRange.value[1]
    })
    if (res.code === 0) {
      overviewData.value = res.data || {}
      await nextTick()
      initBusinessChart(overviewData.value)
      initCardStatusChart(overviewData.value)
    }
  } catch (error) {
    console.error('加载综合概览失败:', error)
  }
}

const loadUserRank = async () => {
  try {
    const res = await getUserRank({
      start_date: dateRange.value[0],
      end_date: dateRange.value[1]
    })
    if (res.code === 0) {
      initUserRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载用户排行失败:', error)
  }
}

const loadMerchantRank = async () => {
  try {
    const res = await getMerchantRank({
      start_date: dateRange.value[0],
      end_date: dateRange.value[1]
    })
    if (res.code === 0) {
      initMerchantRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载商户排行失败:', error)
  }
}

const handleDateChange = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return
  }
  loadOverview()
  loadConsumeData()
  loadUserRank()
  loadMerchantRank()
}

const handleResize = () => {
  if (businessChart) businessChart.resize()
  if (cardStatusChart) cardStatusChart.resize()
  if (consumeChart) consumeChart.resize()
  if (userRankChart) userRankChart.resize()
  if (merchantRankChart) merchantRankChart.resize()
}

onMounted(() => {
  loadOverview()
  loadConsumeData()
  loadUserRank()
  loadMerchantRank()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (businessChart) businessChart.dispose()
  if (cardStatusChart) cardStatusChart.dispose()
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

.el-row + .el-row {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.sub-title {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.stat-col {
  margin-bottom: 16px;
}

.stat-card {
  min-height: 92px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f8fafc;
}

.stat-label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.chart-box {
  width: 100%;
  height: 400px;
}

@media (max-width: 768px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .chart-box {
    height: 320px;
  }
}
</style>
