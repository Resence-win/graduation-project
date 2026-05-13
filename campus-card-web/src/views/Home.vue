<template>
  <div class="home-container" v-loading="loading">
    <section class="hero-panel">
      <div class="hero-content">
        <div class="hero-kicker">
          <el-icon><Calendar /></el-icon>
          <span>{{ todayText }}</span>
        </div>
        <h1>{{ greeting }}，{{ username }}</h1>
        <p>管理员工作台 · 今日工作概览</p>
        <div class="hero-actions">
          <el-button type="primary" @click="goTo('/statistics')">
            <el-icon><Histogram /></el-icon>
            查看完整统计
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
          <span>待关注事项</span>
          <strong>{{ attentionCount }} 项</strong>
        </div>
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

    <el-row :gutter="18" class="workbench-row">
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
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <span>业务动态</span>
                <small>近 7 日关键业务状态</small>
              </div>
              <el-button text type="primary" @click="goTo('/statistics')">查看完整统计</el-button>
            </div>
          </template>
          <div class="activity-grid">
            <div v-for="item in activityCards" :key="item.label" class="activity-card" :class="item.tone">
              <span class="activity-icon">
                <component :is="item.icon" />
              </span>
              <div class="activity-copy">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <em>{{ item.desc }}</em>
              </div>
            </div>
          </div>
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
          <span>
            <strong>{{ item.label }}</strong>
            <em>{{ item.desc }}</em>
          </span>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import {
  ArrowRight,
  Bell,
  Calendar,
  CreditCard,
  Histogram,
  Lock,
  Management,
  Money,
  Reading,
  RefreshRight,
  School,
  Shop,
  Tickets,
  User,
  Wallet,
  WarningFilled
} from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/dashboard'
import { getLibraryOverdueStat, getOverview } from '@/api/statistics'

const router = useRouter()

const loading = ref(false)
const username = ref('管理员')
const dashboardData = ref({})
const overviewData = ref({})
const libraryData = ref({})

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
  return dashboardValue !== undefined && dashboardValue !== null ? toNumber(dashboardValue) : 0
})

const cardTotal = computed(() => toNumber(overviewData.value.cardTotal))

const activeCardRate = computed(() => {
  if (!cardTotal.value) return 0
  return Math.round((toNumber(overviewData.value.activeCardCount) / cardTotal.value) * 100)
})

const accessFailedCount = computed(() => Math.max(
  toNumber(overviewData.value.accessCount) - toNumber(overviewData.value.accessSuccessCount),
  0
))

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
    label: '正常卡数',
    value: toNumber(overviewData.value.activeCardCount),
    desc: `总卡数 ${cardTotal.value}`,
    icon: CreditCard,
    tone: 'tone-cyan'
  },
  {
    label: '今日消费',
    value: formatAmount(todayConsume.value),
    desc: '今日消费金额',
    icon: Money,
    prefix: '¥',
    tone: 'tone-red'
  },
  {
    label: '待关注',
    value: attentionCount.value,
    desc: '需处理事项',
    icon: WarningFilled,
    suffix: '项',
    tone: 'tone-purple'
  }
])

const operationAlerts = computed(() => {
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
      value: accessFailedCount.value,
      desc: '近 7 日未成功通行记录',
      icon: Lock,
      type: 'danger'
    }
  ]
  return items.filter(item => item.value > 0)
})

const attentionCount = computed(() => operationAlerts.value.length)

const activityCards = computed(() => [
  {
    label: '近 7 日消费',
    value: `¥${formatAmount(overviewData.value.consumeAmount)}`,
    desc: `${toNumber(overviewData.value.consumeCount)} 笔消费记录`,
    icon: Money,
    tone: 'tone-red'
  },
  {
    label: '近 7 日充值',
    value: `¥${formatAmount(overviewData.value.rechargeAmount)}`,
    desc: `${toNumber(overviewData.value.rechargeCount)} 笔充值记录`,
    icon: Wallet,
    tone: 'tone-teal'
  },
  {
    label: '门禁通行',
    value: `${toNumber(overviewData.value.accessSuccessCount)} 次`,
    desc: `异常 ${accessFailedCount.value} 次`,
    icon: Lock,
    tone: 'tone-indigo'
  },
  {
    label: '考勤记录',
    value: `${toNumber(overviewData.value.attendanceCount)} 条`,
    desc: `异常 ${toNumber(overviewData.value.abnormalAttendanceCount)} 条`,
    icon: Tickets,
    tone: 'tone-green'
  },
  {
    label: '图书借阅',
    value: `${toNumber(overviewData.value.borrowCount)} 次`,
    desc: `当前逾期 ${toNumber(libraryData.value.currentOverdueCount || overviewData.value.overdueBorrowCount)} 本`,
    icon: Reading,
    tone: 'tone-orange'
  },
  {
    label: '通勤乘车',
    value: `${toNumber(overviewData.value.commuteRideCount)} 次`,
    desc: '近 7 日乘车记录',
    icon: Management,
    tone: 'tone-blue'
  }
])

const quickActions = [
  { label: '学生管理', desc: '维护学生档案', path: '/student', icon: User, tone: 'tone-blue' },
  { label: '校园卡管理', desc: '开卡、挂失、注销', path: '/card', icon: CreditCard, tone: 'tone-cyan' },
  { label: '充值管理', desc: '处理充值流水', path: '/recharge', icon: Wallet, tone: 'tone-teal' },
  { label: '消费管理', desc: '查看消费记录', path: '/consume', icon: Money, tone: 'tone-red' },
  { label: '数据统计', desc: '分析业务报表', path: '/statistics', icon: Histogram, tone: 'tone-purple' },
  { label: '图书管理', desc: '维护馆藏与借阅', path: '/book', icon: Reading, tone: 'tone-orange' },
  { label: '门禁管理', desc: '查看通行记录', path: '/access', icon: Lock, tone: 'tone-indigo' },
  { label: '考勤管理', desc: '管理考勤记录', path: '/attendance', icon: Management, tone: 'tone-green' }
]

const goTo = (path) => {
  router.push(path)
}

const loadUserInfo = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  username.value = user.username || '管理员'
}

const reloadDashboard = async () => {
  try {
    loading.value = true
    const params = rangeParams()
    const [dashboardRes, overviewRes, libraryRes] = await Promise.allSettled([
      getDashboardData(),
      getOverview(params),
      getLibraryOverdueStat(params)
    ])

    if (dashboardRes.status === 'fulfilled' && dashboardRes.value.code === 0) {
      dashboardData.value = dashboardRes.value.data || {}
    }
    if (overviewRes.status === 'fulfilled' && overviewRes.value.code === 0) {
      overviewData.value = overviewRes.value.data || {}
    }
    if (libraryRes.status === 'fulfilled' && libraryRes.value.code === 0) {
      libraryData.value = libraryRes.value.data || {}
    }
  } catch (error) {
    console.error('加载首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  reloadDashboard()
})
</script>

<style scoped>
.home-container {
  padding: 0;
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
  border: 1px solid #cfe9e5;
  border-radius: 16px;
  background:
    radial-gradient(circle at 82% 18%, rgba(45, 212, 191, 0.26), transparent 30%),
    linear-gradient(135deg, #f8fbfa 0%, #eef8f5 48%, #fff8ec 100%);
  box-shadow: 0 18px 38px rgba(17, 76, 74, 0.08);
}

.hero-panel::after {
  position: absolute;
  right: -70px;
  bottom: -110px;
  width: 280px;
  height: 280px;
  border: 42px solid rgba(15, 118, 110, 0.12);
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
  color: #0f766e;
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
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 12px 24px rgba(17, 76, 74, 0.08);
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
.workbench-row {
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
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(25, 35, 55, 0.04);
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  border-color: #b9e4dd;
  box-shadow: 0 14px 28px rgba(15, 118, 110, 0.12);
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
  border-radius: 14px;
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
  border-radius: 12px;
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
  border-radius: 12px;
  background: #f8fafc;
}

.alert-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
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

.activity-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.activity-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  min-height: 96px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fff;
}

.activity-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: #fff;
  font-size: 20px;
}

.activity-copy {
  min-width: 0;
}

.activity-copy span,
.activity-copy em {
  display: block;
  color: #6b7280;
  font-style: normal;
  font-size: 12px;
}

.activity-copy strong {
  display: block;
  margin: 4px 0;
  color: #111827;
  font-size: 22px;
  line-height: 1.22;
  font-weight: 800;
  word-break: break-all;
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
  border-radius: 12px;
  color: #1f2937;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s, border-color 0.2s, background 0.2s;
}

.quick-action:hover {
  border-color: #b9e4dd;
  background: #f8fbfa;
  transform: translateY(-1px);
}

.quick-action > span:nth-child(2) {
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-action strong,
.quick-action em {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-action strong {
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.quick-action em {
  margin-top: 4px;
  color: #8a95a3;
  font-size: 12px;
  font-style: normal;
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: #fff;
  font-size: 19px;
}

.tone-blue .metric-icon,
.tone-blue .activity-icon,
.quick-icon.tone-blue {
  background: #409eff;
}

.tone-green .metric-icon,
.tone-green .activity-icon,
.quick-icon.tone-green {
  background: #67c23a;
}

.tone-orange .metric-icon,
.tone-orange .activity-icon,
.quick-icon.tone-orange {
  background: #e6a23c;
}

.tone-purple .metric-icon,
.tone-purple .activity-icon,
.quick-icon.tone-purple {
  background: #8b5cf6;
}

.tone-cyan .metric-icon,
.tone-cyan .activity-icon,
.quick-icon.tone-cyan {
  background: #06b6d4;
}

.tone-red .metric-icon,
.tone-red .activity-icon,
.quick-icon.tone-red {
  background: #f56c6c;
}

.tone-teal .metric-icon,
.tone-teal .activity-icon,
.quick-icon.tone-teal {
  background: #14b8a6;
}

.tone-indigo .metric-icon,
.tone-indigo .activity-icon,
.quick-icon.tone-indigo {
  background: #6366f1;
}

:deep(.el-card__header) {
  padding: 16px 18px;
}

:deep(.el-card__body) {
  padding: 18px;
}

:deep(.hero-actions .el-button--primary) {
  background: linear-gradient(135deg, #0b4f4a, #0f766e);
  border-color: #0f766e;
  box-shadow: 0 10px 22px rgba(15, 118, 110, 0.18);
}

:deep(.hero-actions .el-button--primary:hover),
:deep(.hero-actions .el-button--primary:focus) {
  background: linear-gradient(135deg, #0a4744, #0d665f);
  border-color: #0d665f;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #0f766e, #14b8a6);
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

  .workbench-row .el-col + .el-col {
    margin-top: 18px;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 0;
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
}

@media (max-width: 480px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }

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
