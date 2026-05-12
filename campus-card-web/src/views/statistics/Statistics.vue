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
              <el-button type="primary" plain size="small" @click="openCancelDialog">注销明细</el-button>
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

    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>图书馆统计</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="24" class="book-stat-col">
              <div class="stat-card warning-card clickable-stat" @click="openBookDialog('overdue')">
                <div class="stat-label">当前逾期</div>
                <el-statistic :value="toNumber(libraryOverdueData.currentOverdueCount)" suffix="本" />
              </div>
            </el-col>
            <el-col :span="12" class="book-stat-col">
              <div class="stat-card clickable-stat" @click="openBookDialog('borrowing')">
                <div class="stat-label">已借出</div>
                <el-statistic :value="toNumber(libraryOverdueData.borrowingCount)" suffix="本" />
              </div>
            </el-col>
            <el-col :span="12" class="book-stat-col">
              <div class="stat-card success-card clickable-stat" @click="openBookDialog('available')">
                <div class="stat-label">可借出</div>
                <el-statistic :value="toNumber(libraryOverdueData.availableBookCount)" suffix="本" />
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>禁借剩余天数排行</span>
            </div>
          </template>
          <div ref="restrictionRankChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="cancelDialogVisible" title="注销校园卡明细" width="900px">
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

    <el-dialog v-model="bookDialogVisible" :title="bookDialogTitle" width="980px">
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

const bookDialogTitle = computed(() => {
  const map = {
    overdue: '当前逾期图书',
    borrowing: '已借出图书',
    available: '可借出图书'
  }
  return map[bookDetailType.value] || '图书明细'
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

  businessChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 30, left: 45, right: 20, bottom: 40 },
    xAxis: { type: 'category', data: ['消费', '充值', '借阅', '考勤', '门禁'] },
    yAxis: { type: 'value', name: '数量' },
    series: [{
      name: '业务量',
      type: 'bar',
      barWidth: 34,
      data: [
        toNumber(data.consumeCount),
        toNumber(data.rechargeCount),
        toNumber(data.borrowCount),
        toNumber(data.attendanceCount),
        toNumber(data.accessCount)
      ],
      itemStyle: { color: '#409EFF', borderRadius: [6, 6, 0, 0] },
      label: { show: true, position: 'top' }
    }]
  })
}

const initCardStatusChart = (data) => {
  cardStatusChart = resetChart(cardStatusChart, cardStatusChartRef)
  if (!cardStatusChart) return

  cardStatusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 张 ({d}%)' },
    legend: { bottom: 0 },
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
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{c}张' }
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

  consumeChart.setOption({
    title: { text: '消费趋势统计', left: 'center' },
    tooltip: { trigger: 'axis', formatter: '{b}<br/>消费金额: ¥{c}' },
    xAxis: { type: 'category', data: data.map(item => item.date), name: '日期' },
    yAxis: { type: 'value', name: '消费金额(元)' },
    series: [{
      name: '消费金额',
      type: 'line',
      data: data.map(item => item.total_amount),
      smooth: true,
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64, 158, 255, 0.12)' }
    }]
  })
}

const initUserRankChart = (data) => {
  userRankChart = resetChart(userRankChart, userRankChartRef)
  if (!userRankChart) return

  userRankChart.setOption({
    title: { text: '用户消费排行', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>消费金额: ¥{c}' },
    xAxis: { type: 'value', name: '消费金额(元)' },
    yAxis: { type: 'category', data: data.map(item => item.user_name || `用户${item.userId}`), inverse: true },
    series: [{ name: '消费金额', type: 'bar', data: data.map(item => item.total_amount), itemStyle: { color: '#67C23A' } }]
  })
}

const initMerchantRankChart = (data) => {
  merchantRankChart = resetChart(merchantRankChart, merchantRankChartRef)
  if (!merchantRankChart) return

  merchantRankChart.setOption({
    title: { text: '商户收入排行', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '收入',
      type: 'pie',
      radius: '50%',
      data: data.map(item => ({ value: item.total_amount, name: item.merchant_name })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

const initRestrictionRankChart = (data) => {
  restrictionRankChart = resetChart(restrictionRankChart, restrictionRankChartRef)
  if (!restrictionRankChart) return

  const rows = (data || []).slice(0, 8)
  restrictionRankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 20, left: 90, right: 24, bottom: 35 },
    xAxis: { type: 'value', name: '天' },
    yAxis: { type: 'category', data: rows.map(item => item.user_name || '未知用户'), inverse: true },
    series: [{
      name: '剩余禁借天数',
      type: 'bar',
      data: rows.map(item => toNumber(item.remaining_restricted_days)),
      itemStyle: { color: '#E6A23C', borderRadius: [0, 6, 6, 0] },
      label: { show: true, position: 'right' }
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

.stat-col,
.book-stat-col {
  margin-bottom: 16px;
}

.stat-card {
  min-height: 92px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f8fafc;
}

.warning-card {
  background: #fdf6ec;
  border-color: #faecd8;
}

.success-card {
  background: #f0f9eb;
  border-color: #e1f3d8;
}

.clickable-stat {
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.clickable-stat:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.16);
  transform: translateY(-1px);
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

.dialog-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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
