<template>
  <div class="home-container" v-loading="loading">
    <section class="hero-panel">
      <div class="hero-content">
        <div class="hero-kicker">
          <el-icon><Calendar /></el-icon>
          <span>{{ todayText }}</span>
        </div>
        <h1>{{ greeting }}，{{ username }}</h1>
        <p>校园一卡通管理系统运营驾驶舱</p>
        <div class="hero-actions">
          <el-button type="primary" @click="goTo('/statistics')">
            <el-icon><TrendCharts /></el-icon>
            数据统计
          </el-button>
          <el-button plain @click="reloadDashboard">
            <el-icon><RefreshRight /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
      <div class="hero-summary">
        <div class="summary-label">今日消费</div>
        <div class="summary-value">¥{{ formatAmount(todayConsume) }}</div>
        <div class="summary-sub">近 7 日消费 ¥{{ formatAmount(toNumber(overviewData.consumeAmount)) }}</div>
        <div class="summary-progress">
          <span>校园卡正常率</span>
          <strong>{{ activeCardRate }}%</strong>
        </div>
        <el-progress :percentage="activeCardRate" :stroke-width="8" :show-text="false" />
      </div>
    </section>

    <el-row :gutter="18" class="metric-grid">
      <el-col v-for="item in metricCards" :key="item.label" :xs="12" :sm="12" :md="8" :lg="6">
        <div class="metric-card" :class="item.tone">
          <div class="metric-icon">
            <component :is="item.icon" />
          </div>
          <div class="metric-body">
            <span>{{ item.label }}</span>
            <strong>{{ item.prefix || '' }}{{ item.value }}{{ item.suffix || '' }}</strong>
            <em>{{ item.desc }}</em>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="18" class="dashboard-row">
      <el-col :xs="24" :lg="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <span>近 7 日消费趋势</span>
                <small>消费金额按日期汇总</small>
              </div>
              <el-tag effect="plain" type="success">实时统计</el-tag>
            </div>
          </template>
          <div ref="consumeChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="9">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <span>校园卡状态</span>
                <small>正常、挂失、注销分布</small>
              </div>
            </div>
          </template>
          <div ref="cardStatusChartRef" class="chart-box compact"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" class="dashboard-row">
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <span>业务量概览</span>
                <small>近 7 日高频业务对比</small>
              </div>
            </div>
          </template>
          <div ref="businessChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <span>待关注事项</span>
                <small>从运营数据中自动汇总</small>
              </div>
              <el-badge :value="operationAlerts.length" :hidden="operationAlerts.length === 0" />
            </div>
          </template>
          <div v-if="operationAlerts.length" class="alert-list">
            <div v-for="item in operationAlerts" :key="item.label" class="alert-item" :class="item.type">
              <div class="alert-icon">
                <component :is="item.icon" />
              </div>
              <div class="alert-text">
                <strong>{{ item.label }}</strong>
                <span>{{ item.desc }}</span>
              </div>
              <b>{{ item.value }}</b>
            </div>
          </div>
          <el-empty v-else description="暂无需要重点关注的事项" :image-size="90" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card quick-panel" shadow="never">
      <template #header>
        <div class="panel-header">
          <div>
            <span>快捷操作</span>
            <small>常用管理入口</small>
          </div>
        </div>
      </template>
      <div class="quick-grid">
        <button v-for="item in quickActions" :key="item.path" class="quick-action" @click="goTo(item.path)">
          <span class="quick-icon" :class="item.tone">
            <component :is="item.icon" />
          </span>
          <span>{{ item.label }}</span>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  ArrowRight,
  Bell,
  Calendar,
  CreditCard,
  Goods,
  Histogram,
  Lock,
  Management,
  Money,
  Reading,
  RefreshRight,
  School,
  Shop,
  Tickets,
  TrendCharts,
  User,
  Wallet,
  WarningFilled
} from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/dashboard'
import { getConsumeStat, getLibraryOverdueStat, getOverview } from '@/api/statistics'

const router = useRouter()

const loading = ref(false)
const username = ref('管理员')
const dashboardData = ref({})
const overviewData = ref({})
const consumeTrendData = ref([])
const libraryData = ref({})

const consumeChartRef = ref(null)
const cardStatusChartRef = ref(null)
const businessChartRef = ref(null)

let consumeChart = null
let cardStatusChart = null
let businessChart = null

const rangeParams = () => ({
  start_date: dayjs().subtract(6, 'day').format('YYYY-MM-DD'),
  end_date: dayjs().format('YYYY-MM-DD')
})

const toNumber = (value) => Number(value || 0)

const formatAmount = (value) => toNumber(value).toFixed(2)

const todayText = computed(() => dayjs().format('YYYY年MM月DD日'))

const greeting = computed(() => {
  const hour = dayjs().hour()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayConsume = computed(() => {
  const dashboardValue = dashboardData.value.todayConsume
  if (dashboardValue !== undefined && dashboardValue !== null) {
    return toNumber(dashboardValue)
  }
  const today = dayjs().format('YYYY-MM-DD')
  const todayRow = consumeTrendData.value.find(item => item.date === today)
  return toNumber(todayRow?.total_amount)
})

const cardTotal = computed(() => toNumber(overviewData.value.cardTotal))

const activeCardRate = computed(() => {
  if (!cardTotal.value) return 0
  return Math.round((toNumber(overviewData.value.activeCardCount) / cardTotal.value) * 100)
})

const metricCards = computed(() => [
  {
    label: '学生总数',
    value: toNumber(overviewData.value.studentCount || dashboardData.value.studentCount),
    desc: '在校学生档案',
    icon: School,
    tone: 'tone-blue'
  },
  {
    label: '教师总数',
    value: toNumber(overviewData.value.teacherCount || dashboardData.value.teacherCount),
    desc: '教师用户档案',
    icon: User,
    tone: 'tone-green'
  },
  {
    label: '商户总数',
    value: toNumber(overviewData.value.merchantCount || dashboardData.value.merchantCount),
    desc: '校内消费场景',
    icon: Shop,
    tone: 'tone-orange'
  },
  {
    label: '商品总数',
    value: toNumber(overviewData.value.productCount),
    desc: '可消费商品',
    icon: Goods,
    tone: 'tone-purple'
  },
  {
    label: '正常卡数',
    value: toNumber(overviewData.value.activeCardCount),
    desc: `总卡数 ${cardTotal.value}`,
    icon: CreditCard,
    tone: 'tone-cyan'
  },
  {
    label: '消费金额',
    value: formatAmount(overviewData.value.consumeAmount),
    desc: '近 7 日累计',
    icon: Money,
    prefix: '¥',
    tone: 'tone-red'
  },
  {
    label: '充值金额',
    value: formatAmount(overviewData.value.rechargeAmount),
    desc: '近 7 日累计',
    icon: Wallet,
    prefix: '¥',
    tone: 'tone-teal'
  },
  {
    label: '考勤记录',
    value: toNumber(overviewData.value.attendanceCount),
    desc: '近 7 日记录',
    icon: Tickets,
    tone: 'tone-indigo'
  }
])

const operationAlerts = computed(() => {
  const accessFailedCount = Math.max(
    toNumber(overviewData.value.accessCount) - toNumber(overviewData.value.accessSuccessCount),
    0
  )
  const items = [
    {
      label: '挂失校园卡',
      value: toNumber(overviewData.value.lostCardCount),
      desc: '请关注补卡与账户安全',
      icon: WarningFilled,
      type: 'warning'
    },
    {
      label: '注销校园卡',
      value: toNumber(overviewData.value.cancelledCardCount),
      desc: '可在数据统计中查看明细',
      icon: CreditCard,
      type: 'danger'
    },
    {
      label: '当前逾期图书',
      value: toNumber(libraryData.value.currentOverdueCount || overviewData.value.overdueBorrowCount),
      desc: '建议提醒借阅人归还',
      icon: Reading,
      type: 'warning'
    },
    {
      label: '异常考勤',
      value: toNumber(overviewData.value.abnormalAttendanceCount),
      desc: '包含迟到、早退或缺勤',
      icon: Bell,
      type: 'info'
    },
    {
      label: '门禁异常',
      value: accessFailedCount,
      desc: '近 7 日未成功通行记录',
      icon: Lock,
      type: 'danger'
    }
  ]
  return items.filter(item => item.value > 0)
})

const quickActions = [
  { label: '学生管理', path: '/student', icon: User, tone: 'tone-blue' },
  { label: '校园卡管理', path: '/card', icon: CreditCard, tone: 'tone-cyan' },
  { label: '充值管理', path: '/recharge', icon: Wallet, tone: 'tone-teal' },
  { label: '消费管理', path: '/consume', icon: Money, tone: 'tone-red' },
  { label: '数据统计', path: '/statistics', icon: Histogram, tone: 'tone-purple' },
  { label: '图书管理', path: '/book', icon: Reading, tone: 'tone-orange' },
  { label: '门禁管理', path: '/access', icon: Lock, tone: 'tone-indigo' },
  { label: '考勤管理', path: '/attendance', icon: Management, tone: 'tone-green' }
]

const goTo = (path) => {
  router.push(path)
}

const normalizeConsumeTrend = (rows = []) => {
  const rowMap = new Map(rows.map(item => [item.date, item]))
  return Array.from({ length: 7 }).map((_, index) => {
    const date = dayjs().subtract(6 - index, 'day').format('YYYY-MM-DD')
    const row = rowMap.get(date) || {}
    return {
      date,
      total_amount: toNumber(row.total_amount),
      consume_count: toNumber(row.consume_count)
    }
  })
}

const resetChart = (chart, el) => {
  if (chart) {
    chart.dispose()
  }
  return el.value ? echarts.init(el.value) : null
}

const initConsumeChart = () => {
  consumeChart = resetChart(consumeChart, consumeChartRef)
  if (!consumeChart) return

  const rows = normalizeConsumeTrend(consumeTrendData.value)
  consumeChart.setOption({
    color: ['#1677ff'],
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const item = params[0]
        return `${item.axisValue}<br/>消费金额：¥${formatAmount(item.value)}`
      }
    },
    grid: { top: 26, left: 44, right: 22, bottom: 36 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: rows.map(item => dayjs(item.date).format('MM/DD')),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      name: '元',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eef1f6' } }
    },
    series: [{
      name: '消费金额',
      type: 'line',
      smooth: true,
      symbolSize: 8,
      data: rows.map(item => item.total_amount),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(22, 119, 255, 0.22)' },
          { offset: 1, color: 'rgba(22, 119, 255, 0.02)' }
        ])
      },
      lineStyle: { width: 3 }
    }]
  })
}

const initCardStatusChart = () => {
  cardStatusChart = resetChart(cardStatusChart, cardStatusChartRef)
  if (!cardStatusChart) return

  const rows = [
    { name: '正常', value: toNumber(overviewData.value.activeCardCount), itemStyle: { color: '#22c55e' } },
    { name: '挂失', value: toNumber(overviewData.value.lostCardCount), itemStyle: { color: '#f59e0b' } },
    { name: '注销', value: toNumber(overviewData.value.cancelledCardCount), itemStyle: { color: '#ef4444' } }
  ]

  cardStatusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c} 张 ({d}%)' },
    legend: { bottom: 2, itemWidth: 10, itemHeight: 10 },
    series: [{
      name: '校园卡状态',
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['50%', '43%'],
      avoidLabelOverlap: true,
      data: rows,
      itemStyle: { borderColor: '#fff', borderWidth: 3 },
      label: { formatter: '{b}\n{c}张' }
    }]
  })
}

const initBusinessChart = () => {
  businessChart = resetChart(businessChart, businessChartRef)
  if (!businessChart) return

  const rows = [
    { name: '消费', value: toNumber(overviewData.value.consumeCount) },
    { name: '充值', value: toNumber(overviewData.value.rechargeCount) },
    { name: '借阅', value: toNumber(overviewData.value.borrowCount) },
    { name: '考勤', value: toNumber(overviewData.value.attendanceCount) },
    { name: '门禁', value: toNumber(overviewData.value.accessCount) },
    { name: '通勤', value: toNumber(overviewData.value.commuteRideCount) }
  ]

  businessChart.setOption({
    color: ['#409eff'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 26, left: 40, right: 20, bottom: 34 },
    xAxis: {
      type: 'category',
      data: rows.map(item => item.name),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#dcdfe6' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eef1f6' } }
    },
    series: [{
      name: '业务量',
      type: 'bar',
      barWidth: 28,
      data: rows.map(item => item.value),
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#67c23a' }
        ])
      },
      label: { show: true, position: 'top', color: '#606266' }
    }]
  })
}

const initCharts = async () => {
  await nextTick()
  initConsumeChart()
  initCardStatusChart()
  initBusinessChart()
}

const loadUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  username.value = user.username || '管理员'
}

const reloadDashboard = async () => {
  try {
    loading.value = true
    const params = rangeParams()
    const [dashboardRes, overviewRes, consumeRes, libraryRes] = await Promise.allSettled([
      getDashboardData(),
      getOverview(params),
      getConsumeStat(params),
      getLibraryOverdueStat(params)
    ])

    if (dashboardRes.status === 'fulfilled' && dashboardRes.value.code === 0) {
      dashboardData.value = dashboardRes.value.data || {}
    }
    if (overviewRes.status === 'fulfilled' && overviewRes.value.code === 0) {
      overviewData.value = overviewRes.value.data || {}
    }
    if (consumeRes.status === 'fulfilled' && consumeRes.value.code === 0) {
      consumeTrendData.value = consumeRes.value.data || []
    }
    if (libraryRes.status === 'fulfilled' && libraryRes.value.code === 0) {
      libraryData.value = libraryRes.value.data || {}
    }

    await initCharts()
  } catch (error) {
    console.error('加载首页数据失败:', error)
    await initCharts()
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  ;[consumeChart, cardStatusChart, businessChart].forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(() => {
  loadUserInfo()
  reloadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  ;[consumeChart, cardStatusChart, businessChart].forEach(chart => {
    if (chart) chart.dispose()
  })
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  color: #1f2937;
}

.hero-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
  overflow: hidden;
  min-height: 210px;
  padding: 26px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background:
    radial-gradient(circle at 82% 18%, rgba(45, 212, 191, 0.26), transparent 30%),
    linear-gradient(135deg, #f8fbff 0%, #eef6ff 48%, #f6fbf4 100%);
  box-shadow: 0 16px 34px rgba(31, 78, 121, 0.08);
}

.hero-panel::after {
  position: absolute;
  right: -70px;
  bottom: -110px;
  width: 280px;
  height: 280px;
  border: 42px solid rgba(64, 158, 255, 0.12);
  border-radius: 50%;
  content: '';
}

.hero-content,
.hero-summary {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.hero-content h1 {
  margin: 0;
  color: #1f2937;
  font-size: 30px;
  line-height: 1.25;
  font-weight: 700;
}

.hero-content p {
  margin: 10px 0 22px;
  color: #5f6f85;
  font-size: 15px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-actions .el-button {
  margin-left: 0;
}

.hero-summary {
  align-self: stretch;
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 12px 24px rgba(31, 78, 121, 0.08);
}

.summary-label,
.summary-sub,
.summary-progress {
  color: #6b7280;
  font-size: 13px;
}

.summary-value {
  margin: 8px 0 6px;
  color: #0f766e;
  font-size: 34px;
  line-height: 1.2;
  font-weight: 800;
}

.summary-progress {
  display: flex;
  justify-content: space-between;
  margin: 22px 0 8px;
}

.summary-progress strong {
  color: #1f2937;
}

.metric-grid,
.dashboard-row {
  margin-top: 18px;
}

.metric-grid .el-col {
  margin-bottom: 18px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 116px;
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(25, 35, 55, 0.04);
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  border-color: #b7d9ff;
  box-shadow: 0 14px 28px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.metric-icon,
.quick-icon,
.alert-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.metric-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  color: #fff;
  font-size: 23px;
}

.metric-body {
  min-width: 0;
}

.metric-body span,
.metric-body em {
  display: block;
  color: #6b7280;
  font-style: normal;
  font-size: 13px;
}

.metric-body strong {
  display: block;
  margin: 4px 0;
  color: #111827;
  font-size: 24px;
  line-height: 1.22;
  font-weight: 800;
  word-break: break-all;
}

.panel-card {
  height: 100%;
  border-radius: 8px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.panel-header span {
  display: block;
  color: #1f2937;
  font-weight: 700;
}

.panel-header small {
  display: block;
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.chart-box {
  width: 100%;
  height: 330px;
}

.chart-box.compact {
  height: 330px;
}

.alert-list {
  display: grid;
  gap: 12px;
}

.alert-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f8fafc;
}

.alert-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  font-size: 19px;
}

.alert-text strong,
.alert-text span {
  display: block;
}

.alert-text strong {
  color: #1f2937;
  font-size: 14px;
}

.alert-text span {
  margin-top: 3px;
  color: #7b8794;
  font-size: 12px;
}

.alert-item b {
  color: #111827;
  font-size: 22px;
}

.alert-item.warning .alert-icon {
  background: #f59e0b;
}

.alert-item.danger .alert-icon {
  background: #ef4444;
}

.alert-item.info .alert-icon {
  background: #409eff;
}

.quick-panel {
  margin-top: 18px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.quick-action {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 68px;
  padding: 13px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  color: #1f2937;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, background 0.2s;
}

.quick-action:hover {
  border-color: #b7d9ff;
  background: #f8fbff;
  transform: translateY(-1px);
}

.quick-action > span:nth-child(2) {
  overflow: hidden;
  font-size: 14px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  color: #fff;
  font-size: 19px;
}

.tone-blue .metric-icon,
.quick-icon.tone-blue {
  background: #409eff;
}

.tone-green .metric-icon,
.quick-icon.tone-green {
  background: #67c23a;
}

.tone-orange .metric-icon,
.quick-icon.tone-orange {
  background: #e6a23c;
}

.tone-purple .metric-icon,
.quick-icon.tone-purple {
  background: #8b5cf6;
}

.tone-cyan .metric-icon,
.quick-icon.tone-cyan {
  background: #06b6d4;
}

.tone-red .metric-icon,
.quick-icon.tone-red {
  background: #f56c6c;
}

.tone-teal .metric-icon,
.quick-icon.tone-teal {
  background: #14b8a6;
}

.tone-indigo .metric-icon,
.quick-icon.tone-indigo {
  background: #6366f1;
}

:deep(.el-card__header) {
  padding: 16px 18px;
}

:deep(.el-card__body) {
  padding: 18px;
}

@media (max-width: 1200px) {
  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .dashboard-row .el-col + .el-col {
    margin-top: 18px;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 12px;
  }

  .hero-panel {
    padding: 20px;
  }

  .hero-content h1 {
    font-size: 24px;
  }

  .summary-value {
    font-size: 28px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-box,
  .chart-box.compact {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .metric-card {
    align-items: flex-start;
    height: auto;
    min-height: 124px;
    flex-direction: column;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
