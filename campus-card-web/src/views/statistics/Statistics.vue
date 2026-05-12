<template>
  <div class="statistics">
    <section class="statistics-header">
      <div class="header-copy">
        <div class="header-kicker">
          <el-icon><TrendCharts /></el-icon>
          <span>运营数据中心</span>
        </div>
        <h1>数据统计报表</h1>
        <p>按时间范围查看校园一卡通、消费、门禁、考勤与图书业务分析</p>
      </div>
      <div class="header-tools">
        <el-date-picker
          v-model="dateRange"
          class="date-range-picker"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateChange"
        />
        <div class="header-range">
          <el-icon><Calendar /></el-icon>
          <span>{{ dateRangeText }}</span>
        </div>
      </div>
    </section>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="overview-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>综合数据概览</span>
                <div class="sub-title">按所选时间范围自动汇总系统业务数据</div>
              </div>
              <el-tag effect="plain" type="primary">实时汇总</el-tag>
            </div>
          </template>
          <div class="overview-grid">
            <div
              v-for="item in overviewCards"
              :key="item.label"
              :class="['stat-card', item.tone]"
            >
              <div class="stat-main">
                <span class="stat-icon">
                  <el-icon><component :is="item.icon" /></el-icon>
                </span>
                <div class="stat-copy">
                  <span class="stat-label">{{ item.label }}</span>
                  <span class="stat-desc">{{ item.desc }}</span>
                </div>
              </div>
              <div class="stat-value">
                <el-statistic
                  :value="item.value"
                  :precision="item.precision || 0"
                  :prefix="item.prefix || ''"
                  :suffix="item.suffix || ''"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="14">
        <el-card class="chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>业务量概览</span>
                <div class="sub-title">消费、充值、借阅、考勤与门禁数量对比</div>
              </div>
            </div>
          </template>
          <div ref="businessChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card class="chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>校园卡状态</span>
                <div class="sub-title">正常、挂失与注销状态分布</div>
              </div>
              <el-button type="primary" plain size="small" @click="openCancelDialog">注销明细</el-button>
            </div>
          </template>
          <div ref="cardStatusChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>消费统计</span>
                <div class="sub-title">按日期展示所选时间范围内的消费金额趋势</div>
              </div>
            </div>
          </template>
          <div ref="consumeChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>用户消费排行</span>
                <div class="sub-title">展示消费金额靠前的用户</div>
              </div>
            </div>
          </template>
          <div ref="userRankChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>商户收入排行</span>
                <div class="sub-title">展示收入占比较高的校内商户</div>
              </div>
            </div>
          </template>
          <div ref="merchantRankChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="library-panel chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>图书馆统计</span>
                <div class="sub-title">馆藏状态与借阅风险概览</div>
              </div>
            </div>
          </template>
          <div class="library-stat-grid">
            <div
              v-for="item in libraryStatCards"
              :key="item.type"
              :class="['library-stat-card', `is-${item.tone}`]"
              @click="openBookDialog(item.type)"
            >
              <div class="library-stat-main">
                <span class="library-stat-icon">
                  <el-icon><component :is="item.icon" /></el-icon>
                </span>
                <div class="library-stat-copy">
                  <span class="library-stat-label">{{ item.label }}</span>
                  <span class="library-stat-desc">{{ item.desc }}</span>
                </div>
              </div>
              <div class="library-stat-value">
                <el-statistic :value="item.value" suffix="本" />
                <span class="library-stat-rate">{{ item.rateLabel }}</span>
              </div>
              <div class="library-stat-track">
                <span :style="{ width: `${item.percent}%` }"></span>
              </div>
            </div>
          </div>
          <div class="library-summary">
            <div>
              <span class="summary-label">统计馆藏</span>
              <strong>{{ libraryBookTotal }}</strong>
              <span>本</span>
            </div>
            <div>
              <span class="summary-label">借出率</span>
              <strong>{{ borrowingRate }}%</strong>
            </div>
            <div>
              <span class="summary-label">可借率</span>
              <strong>{{ availableRate }}%</strong>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="16">
        <el-card class="restriction-panel chart-panel" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span>禁借剩余天数排行</span>
                <div class="sub-title">剩余天数越高，越需要重点提醒</div>
              </div>
            </div>
          </template>
          <div
            ref="restrictionRankChartRef"
            class="chart-box restriction-chart"
            :style="{ height: restrictionRankChartHeight }"
          ></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="cancelDialogVisible" title="注销校园卡明细" width="min(900px, 92vw)">
      <el-table v-loading="cancelDetailLoading" :data="cancelDetails" border empty-text="暂无注销明细">
        <el-table-column prop="card_no" label="卡号" width="150" />
        <el-table-column prop="user_type_label" label="人员类型" width="100" />
        <el-table-column prop="user_no" label="人员编号" width="140" />
        <el-table-column prop="user_name" label="姓名" width="120" />
        <el-table-column prop="cancel_reason" label="注销原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cancel_time" label="注销时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.cancel_time) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="cancelPagination.page"
        v-model:page-size="cancelPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="cancelPagination.total"
        layout="total, sizes, prev, pager, next"
        class="dialog-pagination"
        @size-change="handleCancelSizeChange"
        @current-change="loadCancelDetails"
      />
    </el-dialog>

    <el-dialog v-model="bookDialogVisible" :title="bookDialogTitle" width="min(980px, 92vw)">
      <el-table v-loading="bookDetailLoading" :data="bookDetails" border empty-text="暂无图书明细">
        <el-table-column prop="book_name" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip />
        <el-table-column prop="collection_location" label="馆藏位置" width="160" show-overflow-tooltip />
        <el-table-column v-if="bookDetailType !== 'available'" prop="card_no" label="借阅卡号" width="150" />
        <el-table-column v-if="bookDetailType !== 'available'" prop="user_name" label="借阅人" width="160" show-overflow-tooltip />
        <el-table-column v-if="bookDetailType !== 'available'" prop="borrow_time" label="借出时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.borrow_time) }}</template>
        </el-table-column>
        <el-table-column v-if="bookDetailType !== 'available'" prop="due_time" label="应还时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.due_time) }}</template>
        </el-table-column>
        <el-table-column v-if="bookDetailType === 'overdue'" prop="overdue_days" label="逾期天数" width="100" />
        <el-table-column v-if="bookDetailType === 'available'" prop="create_time" label="入库时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.create_time) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="bookPagination.page"
        v-model:page-size="bookPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="bookPagination.total"
        layout="total, sizes, prev, pager, next"
        class="dialog-pagination"
        @size-change="handleBookSizeChange"
        @current-change="loadBookDetails"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  Calendar,
  CreditCard,
  Finished,
  Goods,
  Money,
  Reading,
  School,
  Shop,
  Tickets,
  TrendCharts,
  UserFilled,
  Wallet,
  WarningFilled
} from '@element-plus/icons-vue'
import {
  getOverview,
  getConsumeStat,
  getUserRank,
  getMerchantRank,
  getLibraryOverdueStat,
  getCardCancelDetails,
  getLibraryBookDetails
} from '@/api/statistics'

const businessChartRef = ref(null)
const cardStatusChartRef = ref(null)
const consumeChartRef = ref(null)
const userRankChartRef = ref(null)
const merchantRankChartRef = ref(null)
const restrictionRankChartRef = ref(null)

let businessChart = null
let cardStatusChart = null
let consumeChart = null
let userRankChart = null
let merchantRankChart = null
let restrictionRankChart = null

const dateRange = ref([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

const overviewData = ref({})
const libraryOverdueData = ref({})
const cancelDialogVisible = ref(false)
const cancelDetailLoading = ref(false)
const cancelDetails = ref([])
const cancelPagination = reactive({ page: 1, size: 10, total: 0 })
const bookDialogVisible = ref(false)
const bookDetailLoading = ref(false)
const bookDetailType = ref('overdue')
const bookDetails = ref([])
const bookPagination = reactive({ page: 1, size: 10, total: 0 })

const toNumber = (value) => Number(value || 0)
const formatRate = (value, total) => {
  if (!total) return 0
  return Math.min(100, Math.round((toNumber(value) / total) * 100))
}

const chartTextStyle = {
  color: '#606266',
  fontSize: 12
}

const chartAxisLine = {
  lineStyle: { color: '#dcdfe6' }
}

const chartSplitLine = {
  lineStyle: { color: '#edf1f7' }
}

const chartTooltip = {
  backgroundColor: 'rgba(255, 255, 255, 0.96)',
  borderColor: '#dbeafe',
  borderWidth: 1,
  padding: [10, 12],
  textStyle: { color: '#1f2937', fontSize: 12 },
  extraCssText: 'box-shadow: 0 10px 24px rgba(31, 78, 121, 0.12); border-radius: 8px;'
}

const emptyGraphic = (text = '暂无统计数据') => ({
  type: 'text',
  left: 'center',
  top: 'middle',
  style: {
    text,
    fill: '#909399',
    fontSize: 14,
    fontWeight: 500
  }
})

const hasChartData = (data = []) => data.some(item => toNumber(item) > 0)

const overviewCards = computed(() => [
  { label: '学生总数', desc: '学生档案', value: toNumber(overviewData.value.studentCount), icon: School, tone: 'tone-blue' },
  { label: '教师总数', desc: '教师档案', value: toNumber(overviewData.value.teacherCount), icon: UserFilled, tone: 'tone-green' },
  { label: '商户总数', desc: '校内商户', value: toNumber(overviewData.value.merchantCount), icon: Shop, tone: 'tone-orange' },
  { label: '商品总数', desc: '在售商品', value: toNumber(overviewData.value.productCount), icon: Goods, tone: 'tone-cyan' },
  { label: '图书总数', desc: '馆藏图书', value: toNumber(overviewData.value.bookCount), icon: Reading, tone: 'tone-indigo' },
  { label: '正常卡数', desc: '可用校园卡', value: toNumber(overviewData.value.activeCardCount), icon: CreditCard, tone: 'tone-teal' },
  { label: '消费金额', desc: '区间消费额', value: toNumber(overviewData.value.consumeAmount), precision: 2, prefix: '¥', icon: Money, tone: 'tone-red' },
  { label: '充值金额', desc: '区间充值额', value: toNumber(overviewData.value.rechargeAmount), precision: 2, prefix: '¥', icon: Wallet, tone: 'tone-purple' },
  { label: '消费笔数', desc: '消费记录', value: toNumber(overviewData.value.consumeCount), icon: Tickets, tone: 'tone-red' },
  { label: '充值笔数', desc: '充值记录', value: toNumber(overviewData.value.rechargeCount), icon: Wallet, tone: 'tone-teal' },
  { label: '借阅次数', desc: '图书借阅', value: toNumber(overviewData.value.borrowCount), icon: Reading, tone: 'tone-orange' },
  { label: '考勤记录', desc: '考勤流水', value: toNumber(overviewData.value.attendanceCount), icon: Calendar, tone: 'tone-green' }
])

const dateRangeText = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return '请选择统计时间范围'
  }
  return `${dateRange.value[0]} 至 ${dateRange.value[1]}`
})

const bookDialogTitle = computed(() => {
  const map = {
    overdue: '当前逾期图书',
    borrowing: '已借出图书',
    available: '可借出图书'
  }
  return map[bookDetailType.value] || '图书明细'
})

const libraryCounts = computed(() => ({
  overdue: toNumber(libraryOverdueData.value.currentOverdueCount),
  borrowing: toNumber(libraryOverdueData.value.borrowingCount),
  available: toNumber(libraryOverdueData.value.availableBookCount)
}))

const libraryBookTotal = computed(() => {
  const activeTotal = libraryCounts.value.borrowing + libraryCounts.value.available
  return Math.max(activeTotal, toNumber(overviewData.value.bookCount))
})

const borrowingRate = computed(() => formatRate(libraryCounts.value.borrowing, libraryBookTotal.value))
const availableRate = computed(() => formatRate(libraryCounts.value.available, libraryBookTotal.value))

const libraryStatCards = computed(() => {
  const overdueBase = Math.max(libraryCounts.value.borrowing, libraryCounts.value.overdue)
  return [
    {
      type: 'overdue',
      label: '当前逾期',
      desc: '需提醒归还',
      value: libraryCounts.value.overdue,
      percent: formatRate(libraryCounts.value.overdue, overdueBase),
      rateLabel: overdueBase ? `占借出 ${formatRate(libraryCounts.value.overdue, overdueBase)}%` : '暂无逾期',
      tone: 'warning',
      icon: WarningFilled
    },
    {
      type: 'borrowing',
      label: '已借出',
      desc: '正在流转',
      value: libraryCounts.value.borrowing,
      percent: borrowingRate.value,
      rateLabel: `借出率 ${borrowingRate.value}%`,
      tone: 'primary',
      icon: Reading
    },
    {
      type: 'available',
      label: '可借出',
      desc: '可继续借阅',
      value: libraryCounts.value.available,
      percent: availableRate.value,
      rateLabel: `可借率 ${availableRate.value}%`,
      tone: 'success',
      icon: Finished
    }
  ]
})

const restrictionRankRows = computed(() => (libraryOverdueData.value.borrowRestrictionRank || []).slice(0, 8))
const restrictionRankChartHeight = computed(() => {
  const rowCount = restrictionRankRows.value.length
  return `${Math.max(260, rowCount * 62 + 120)}px`
})

const resetChart = (chart, refEl) => {
  if (chart) {
    chart.dispose()
  }
  return refEl.value ? echarts.init(refEl.value) : null
}

const initBusinessChart = (data) => {
  businessChart = resetChart(businessChart, businessChartRef)
  if (!businessChart) return

  const values = [
    toNumber(data.consumeCount),
    toNumber(data.rechargeCount),
    toNumber(data.borrowCount),
    toNumber(data.attendanceCount),
    toNumber(data.accessCount)
  ]

  businessChart.setOption({
    tooltip: { ...chartTooltip, trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(64, 158, 255, 0.08)' } } },
    grid: { top: 34, left: 48, right: 24, bottom: 42 },
    xAxis: { type: 'category', data: ['消费', '充值', '借阅', '考勤', '门禁'], axisLine: chartAxisLine, axisLabel: chartTextStyle },
    yAxis: { type: 'value', name: '数量', splitLine: chartSplitLine, axisLabel: chartTextStyle },
    graphic: hasChartData(values) ? [] : [emptyGraphic()],
    series: [{
      name: '业务量',
      type: 'bar',
      barWidth: 34,
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#60a5fa' },
          { offset: 1, color: '#2563eb' }
        ]),
        borderRadius: [8, 8, 0, 0]
      },
      label: { show: true, position: 'top', color: '#475569', fontWeight: 600 }
    }]
  })
}

const initCardStatusChart = (data) => {
  cardStatusChart = resetChart(cardStatusChart, cardStatusChartRef)
  if (!cardStatusChart) return

  const values = [
    toNumber(data.activeCardCount),
    toNumber(data.lostCardCount),
    toNumber(data.cancelledCardCount)
  ]

  cardStatusChart.setOption({
    tooltip: { ...chartTooltip, trigger: 'item', formatter: '{b}: {c} 张 ({d}%)' },
    legend: { bottom: 2, icon: 'circle', itemWidth: 9, itemHeight: 9, textStyle: chartTextStyle },
    color: ['#10b981', '#f59e0b', '#ef4444'],
    graphic: hasChartData(values) ? [] : [emptyGraphic('暂无校园卡状态数据')],
    series: [{
      name: '校园卡状态',
      type: 'pie',
      radius: ['48%', '70%'],
      center: ['50%', '44%'],
      data: [
        { name: '正常', value: values[0] },
        { name: '挂失', value: values[1] },
        { name: '注销', value: values[2] }
      ],
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      emphasis: { scaleSize: 8 },
      label: { formatter: '{b}\n{c}张', color: '#475569' }
    }]
  })
  cardStatusChart.off('click')
  cardStatusChart.on('click', (params) => {
    if (params.name === '注销') {
      openCancelDialog()
    }
  })
}

const initConsumeChart = (data) => {
  consumeChart = resetChart(consumeChart, consumeChartRef)
  if (!consumeChart) return

  const values = data.map(item => toNumber(item.total_amount))

  consumeChart.setOption({
    tooltip: { ...chartTooltip, trigger: 'axis', formatter: '{b}<br/>消费金额: ¥{c}' },
    grid: { top: 30, left: 58, right: 30, bottom: 46 },
    xAxis: { type: 'category', data: data.map(item => item.date), name: '日期', boundaryGap: false, axisLine: chartAxisLine, axisLabel: chartTextStyle },
    yAxis: { type: 'value', name: '消费金额(元)', splitLine: chartSplitLine, axisLabel: chartTextStyle },
    graphic: values.length ? [] : [emptyGraphic('暂无消费趋势数据')],
    series: [{
      name: '消费金额',
      type: 'line',
      data: values,
      smooth: true,
      itemStyle: { color: '#2563eb' },
      lineStyle: { width: 3 },
      symbolSize: 7,
      symbol: 'circle',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(37, 99, 235, 0.24)' },
          { offset: 1, color: 'rgba(37, 99, 235, 0.03)' }
        ])
      }
    }]
  })
}

const initUserRankChart = (data) => {
  userRankChart = resetChart(userRankChart, userRankChartRef)
  if (!userRankChart) return

  const values = data.map(item => toNumber(item.total_amount))

  userRankChart.setOption({
    tooltip: { ...chartTooltip, trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(16, 185, 129, 0.08)' } }, formatter: '{b}<br/>消费金额: ¥{c}' },
    grid: { top: 30, left: 96, right: 34, bottom: 40 },
    xAxis: { type: 'value', name: '消费金额(元)', splitLine: chartSplitLine, axisLabel: chartTextStyle },
    yAxis: { type: 'category', data: data.map(item => item.user_name || `用户${item.userId}`), inverse: true, axisLine: chartAxisLine, axisLabel: chartTextStyle },
    graphic: values.length ? [] : [emptyGraphic('暂无用户消费排行')],
    series: [{
      name: '消费金额',
      type: 'bar',
      barMaxWidth: 24,
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#10b981' },
          { offset: 1, color: '#bbf7d0' }
        ]),
        borderRadius: [0, 8, 8, 0]
      }
    }]
  })
}

const initMerchantRankChart = (data) => {
  merchantRankChart = resetChart(merchantRankChart, merchantRankChartRef)
  if (!merchantRankChart) return

  const values = data.map(item => toNumber(item.total_amount))

  merchantRankChart.setOption({
    tooltip: { ...chartTooltip, trigger: 'item', formatter: '{a} <br/>{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', left: 8, top: 'middle', icon: 'circle', itemWidth: 9, itemHeight: 9, textStyle: chartTextStyle },
    color: ['#2563eb', '#10b981', '#f59e0b', '#ef4444', '#64748b', '#06b6d4'],
    graphic: values.length ? [] : [emptyGraphic('暂无商户收入排行')],
    series: [{
      name: '收入',
      type: 'pie',
      radius: ['40%', '64%'],
      center: ['58%', '50%'],
      data: data.map(item => ({ value: toNumber(item.total_amount), name: item.merchant_name })),
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n¥{c}', color: '#475569' },
      emphasis: { itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(37, 99, 235, 0.22)' } }
    }]
  })
}

const initRestrictionRankChart = (data) => {
  restrictionRankChart = resetChart(restrictionRankChart, restrictionRankChartRef)
  if (!restrictionRankChart) return

  const rows = (data || []).slice(0, 8)
  const labels = rows.map(item => {
    const userNo = item.user_no || item.card_no
    const userName = item.user_name || '未知用户'
    return userNo ? `${userNo} - ${userName}` : userName
  })
  restrictionRankChart.setOption({
    tooltip: {
      ...chartTooltip,
      trigger: 'axis',
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(245, 158, 11, 0.08)' } },
      formatter: (params) => {
        const item = params[0]
        return `${item.name}<br/>剩余禁借天数：${item.value} 天`
      }
    },
    grid: { top: 24, left: 150, right: 54, bottom: 44 },
    xAxis: { type: 'value', name: '天', splitLine: chartSplitLine, axisLabel: chartTextStyle },
    yAxis: {
      type: 'category',
      data: labels,
      inverse: true,
      axisLine: chartAxisLine,
      axisLabel: {
        ...chartTextStyle,
        width: 128,
        overflow: 'truncate'
      }
    },
    graphic: rows.length ? [] : [emptyGraphic('暂无禁借排行数据')],
    series: [{
      name: '剩余禁借天数',
      type: 'bar',
      barMaxWidth: 28,
      data: rows.map(item => toNumber(item.remaining_restricted_days)),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#f59e0b' },
          { offset: 1, color: '#fde68a' }
        ]),
        borderRadius: [0, 9, 9, 0]
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}天',
        color: '#475569',
        fontWeight: 600
      }
    }]
  })
}

const rangeParams = () => ({
  start_date: dateRange.value?.[0],
  end_date: dateRange.value?.[1]
})

const formatDateTime = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const loadOverview = async () => {
  try {
    const res = await getOverview(rangeParams())
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

const loadConsumeData = async () => {
  try {
    const res = await getConsumeStat(rangeParams())
    if (res.code === 0) {
      initConsumeChart(res.data || [])
    }
  } catch (error) {
    console.error('加载消费统计失败:', error)
  }
}

const loadUserRank = async () => {
  try {
    const res = await getUserRank(rangeParams())
    if (res.code === 0) {
      initUserRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载用户排行失败:', error)
  }
}

const loadMerchantRank = async () => {
  try {
    const res = await getMerchantRank(rangeParams())
    if (res.code === 0) {
      initMerchantRankChart(res.data || [])
    }
  } catch (error) {
    console.error('加载商户排行失败:', error)
  }
}

const loadLibraryOverdue = async () => {
  try {
    const res = await getLibraryOverdueStat(rangeParams())
    if (res.code === 0) {
      libraryOverdueData.value = res.data || {}
      await nextTick()
      initRestrictionRankChart(libraryOverdueData.value.borrowRestrictionRank || [])
    }
  } catch (error) {
    console.error('加载图书统计失败:', error)
  }
}

const loadCancelDetails = async () => {
  try {
    cancelDetailLoading.value = true
    const res = await getCardCancelDetails({
      ...rangeParams(),
      page: cancelPagination.page,
      size: cancelPagination.size
    })
    if (res.code === 0) {
      cancelDetails.value = res.data?.records || []
      cancelPagination.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载注销明细失败:', error)
  } finally {
    cancelDetailLoading.value = false
  }
}

const openCancelDialog = () => {
  cancelDialogVisible.value = true
  cancelPagination.page = 1
  loadCancelDetails()
}

const handleCancelSizeChange = (size) => {
  cancelPagination.size = size
  cancelPagination.page = 1
  loadCancelDetails()
}

const loadBookDetails = async () => {
  try {
    bookDetailLoading.value = true
    const res = await getLibraryBookDetails({
      type: bookDetailType.value,
      page: bookPagination.page,
      size: bookPagination.size
    })
    if (res.code === 0) {
      bookDetails.value = res.data?.records || []
      bookPagination.total = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载图书明细失败:', error)
  } finally {
    bookDetailLoading.value = false
  }
}

const openBookDialog = (type) => {
  bookDetailType.value = type
  bookDialogVisible.value = true
  bookPagination.page = 1
  loadBookDetails()
}

const handleBookSizeChange = (size) => {
  bookPagination.size = size
  bookPagination.page = 1
  loadBookDetails()
}

const loadAll = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return
  }
  loadOverview()
  loadConsumeData()
  loadUserRank()
  loadMerchantRank()
  loadLibraryOverdue()
}

const handleDateChange = () => {
  loadAll()
}

const handleResize = () => {
  [
    businessChart,
    cardStatusChart,
    consumeChart,
    userRankChart,
    merchantRankChart,
    restrictionRankChart
  ].forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  [
    businessChart,
    cardStatusChart,
    consumeChart,
    userRankChart,
    merchantRankChart,
    restrictionRankChart
  ].forEach(chart => {
    if (chart) chart.dispose()
  })
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.statistics {
  min-height: 100%;
  padding: 20px;
  color: #1f2937;
  background:
    linear-gradient(180deg, #f7fbff 0%, #f5f7fb 260px),
    #f5f7fb;
}

.statistics-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  overflow: hidden;
  margin-bottom: 20px;
  padding: 24px 26px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(239, 246, 255, 0.96) 56%, rgba(240, 253, 250, 0.92) 100%);
  box-shadow: 0 16px 34px rgba(31, 78, 121, 0.08);
}

.header-copy {
  min-width: 0;
}

.header-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
}

.statistics-header h1 {
  margin: 0;
  color: #1f2937;
  font-size: 28px;
  line-height: 1.25;
  font-weight: 700;
}

.statistics-header p {
  margin: 10px 0 0;
  color: #5f6f85;
  font-size: 14px;
}

.header-tools {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 10px;
  flex: 0 0 auto;
}

.date-range-picker {
  width: 320px;
}

.header-range {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 320px;
  padding: 8px 12px;
  color: #475569;
  font-size: 12px;
  border: 1px solid rgba(219, 234, 254, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 18px rgba(31, 78, 121, 0.06);
}

.el-row + .el-row {
  margin-top: 20px;
}

.statistics :deep(.el-card) {
  overflow: hidden;
  border: 1px solid #e5edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(25, 35, 55, 0.05);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.statistics :deep(.el-card:hover) {
  border-color: #cfe2ff;
  box-shadow: 0 16px 34px rgba(31, 78, 121, 0.09);
  transform: translateY(-1px);
}

.statistics :deep(.el-card__header) {
  padding: 18px 22px;
  border-bottom: 1px solid #eef1f6;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
}

.statistics :deep(.el-card__body) {
  padding: 22px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.card-header > span,
.card-header > div > span {
  color: #1f2937;
  font-size: 16px;
  line-height: 1.35;
  font-weight: 700;
}

.sub-title {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  position: relative;
  min-width: 0;
  min-height: 132px;
  padding: 16px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  box-shadow: 0 8px 20px rgba(25, 35, 55, 0.04);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.stat-card::before {
  position: absolute;
  top: 0;
  right: 14px;
  left: 14px;
  height: 3px;
  border-radius: 0 0 999px 999px;
  background: var(--tone-main, #2563eb);
  content: "";
}

.stat-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 14px 26px rgba(31, 78, 121, 0.09);
  transform: translateY(-2px);
}

.stat-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  color: var(--tone-main, #2563eb);
  font-size: 19px;
  border-radius: 8px;
  background: var(--tone-bg, #eff6ff);
}

.stat-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.stat-label {
  overflow: hidden;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-desc {
  overflow: hidden;
  color: #94a3b8;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-value {
  margin-top: 20px;
}

.stat-value :deep(.el-statistic__content) {
  overflow: hidden;
  color: #111827;
  font-size: 24px;
  line-height: 1.15;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tone-blue {
  --tone-main: #2563eb;
  --tone-bg: #eff6ff;
}

.tone-green {
  --tone-main: #16a34a;
  --tone-bg: #f0fdf4;
}

.tone-orange {
  --tone-main: #f59e0b;
  --tone-bg: #fffbeb;
}

.tone-cyan {
  --tone-main: #0891b2;
  --tone-bg: #ecfeff;
}

.tone-indigo {
  --tone-main: #4f46e5;
  --tone-bg: #eef2ff;
}

.tone-teal {
  --tone-main: #0f766e;
  --tone-bg: #f0fdfa;
}

.tone-red {
  --tone-main: #dc2626;
  --tone-bg: #fef2f2;
}

.tone-purple {
  --tone-main: #7c3aed;
  --tone-bg: #f5f3ff;
}

.chart-panel {
  height: 100%;
}

.chart-box {
  width: 100%;
  height: 390px;
}

.chart-panel :deep(.el-button) {
  margin-left: 0;
}

.library-panel :deep(.el-card__body) {
  padding: 20px;
}

.library-stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.library-stat-card {
  position: relative;
  min-height: 142px;
  padding: 16px;
  overflow: hidden;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.library-stat-card:first-child {
  grid-column: 1 / -1;
}

.library-stat-card::before {
  position: absolute;
  top: 0;
  right: 14px;
  left: 14px;
  height: 3px;
  border-radius: 0 0 999px 999px;
  background: #2563eb;
  content: "";
}

.library-stat-card:hover {
  border-color: rgba(37, 99, 235, 0.34);
  box-shadow: 0 12px 24px rgba(31, 78, 121, 0.1);
  transform: translateY(-2px);
}

.library-stat-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.library-stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  color: #2563eb;
  background: #eff6ff;
  font-size: 20px;
}

.library-stat-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.library-stat-label {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.library-stat-desc {
  overflow: hidden;
  color: #909399;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.library-stat-value {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-top: 18px;
}

.library-stat-value :deep(.el-statistic__content) {
  color: #111827;
  font-size: 30px;
  line-height: 1;
  font-weight: 800;
}

.library-stat-rate {
  flex: 0 0 auto;
  color: #606266;
  font-size: 12px;
}

.library-stat-track {
  position: relative;
  z-index: 1;
  height: 7px;
  margin-top: 16px;
  overflow: hidden;
  border-radius: 999px;
  background: #edf1f7;
}

.library-stat-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #93c5fd 0%, #2563eb 100%);
  transition: width 0.28s ease;
}

.library-stat-card.is-warning {
  background: linear-gradient(180deg, #ffffff 0%, #fffbeb 100%);
  border-color: #faecd8;
}

.library-stat-card.is-warning::before {
  background: #f59e0b;
}

.library-stat-card.is-warning .library-stat-icon {
  color: #f59e0b;
  background: #fffbeb;
}

.library-stat-card.is-warning .library-stat-track span {
  background: linear-gradient(90deg, #f3d19e 0%, #e6a23c 100%);
}

.library-stat-card.is-success {
  background: linear-gradient(180deg, #ffffff 0%, #f0fdf4 100%);
  border-color: #e1f3d8;
}

.library-stat-card.is-success::before {
  background: #16a34a;
}

.library-stat-card.is-success .library-stat-icon {
  color: #16a34a;
  background: #f0fdf4;
}

.library-stat-card.is-success .library-stat-track span {
  background: linear-gradient(90deg, #b3e19d 0%, #67c23a 100%);
}

.library-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
  padding: 12px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}

.library-summary > div {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  min-width: 0;
  color: #606266;
  font-size: 12px;
}

.library-summary strong {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.summary-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.restriction-panel :deep(.el-card__body) {
  padding-top: 18px;
}

.restriction-chart {
  min-height: 260px;
  transition: height 0.24s ease;
}

.dialog-pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 16px;
}

.statistics :deep(.el-dialog__body) {
  overflow-x: auto;
}

.statistics :deep(.el-table) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .statistics {
    padding: 14px;
  }

  .statistics-header {
    align-items: flex-start;
    flex-direction: column;
    padding: 18px;
  }

  .statistics-header h1 {
    font-size: 24px;
  }

  .header-tools,
  .date-range-picker,
  .header-range {
    width: 100%;
    max-width: none;
  }

  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .stat-card {
    min-height: 126px;
    padding: 14px;
  }

  .stat-value :deep(.el-statistic__content) {
    font-size: 21px;
  }

  .chart-box {
    height: 320px;
  }

  .statistics :deep(.el-card__body) {
    padding: 18px;
  }

  .library-stat-grid,
  .library-summary {
    grid-template-columns: 1fr;
  }

  .library-stat-card:first-child {
    grid-column: auto;
  }

  .library-stat-value {
    align-items: flex-start;
    flex-direction: column;
  }

  .dialog-pagination {
    justify-content: flex-start;
  }
}

@media (min-width: 769px) and (max-width: 1180px) {
  .overview-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 420px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
