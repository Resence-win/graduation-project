<template>
  <div class="user-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ userInfo.role === 'student' ? '学生个人中心' : '教师个人中心' }}</span>
          <el-button type="danger" @click="logout">退出登录</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userInfo" label-width="100px">
            <el-form-item label="姓名">
              <el-input v-model="userInfo.name" disabled />
            </el-form-item>
            <el-form-item label="学号/工号">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="性别" v-if="userInfo.role === 'student'">
              <el-select v-model="userInfo.gender" placeholder="请选择性别">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="性别" v-else>
              <el-input v-model="userInfo.gender" disabled />
            </el-form-item>
            <el-form-item label="联系电话" v-if="userInfo.role === 'student'">
              <el-input v-model="userInfo.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="联系电话" v-else>
              <el-input v-model="userInfo.phone" disabled />
            </el-form-item>
            <el-form-item v-if="userInfo.role === 'student'">
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="校园卡信息" name="card">
          <el-form :model="cardInfo" label-width="100px">
            <el-form-item label="卡号">
              <el-input v-model="cardInfo.cardNo" disabled />
            </el-form-item>
            <el-form-item label="卡状态">
              <el-tag :type="cardInfo.status === 1 ? 'success' : cardInfo.status === 2 ? 'warning' : 'danger'">
                {{ cardInfo.status === 1 ? '正常' : cardInfo.status === 2 ? '挂失' : '注销' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="余额">
              <el-input v-model="cardInfo.balance" disabled />
            </el-form-item>
            <el-form-item label="开卡时间">
              <el-input v-model="cardInfo.createTime" disabled />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="消费记录" name="consume">
          <el-form :inline="true" :model="consumeForm" class="mb-4">
            <el-form-item label="开始日期">
              <el-date-picker v-model="consumeForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker v-model="consumeForm.endDate" type="date" placeholder="选择结束日期" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadConsumeData">查询</el-button>
              <el-button @click="resetConsumeForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="consumeList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantName" label="商户名称" width="180" />
            <el-table-column prop="amount" label="消费金额" width="120" />
            <el-table-column prop="balanceAfter" label="消费后余额" width="120" />
            <el-table-column prop="consumeTime" label="消费时间" width="180" />
          </el-table>
          
          <el-pagination
            v-model:current-page="consumePagination.page"
            v-model:page-size="consumePagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="consumePagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleConsumeSizeChange"
            @current-change="handleConsumeCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="图书借阅" name="book" v-if="userInfo.role === 'student'">
          <el-form :inline="true" :model="bookSearchForm" class="mb-4">
            <el-form-item label="书名">
              <el-input v-model="bookSearchForm.bookName" placeholder="请输入书名" clearable />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="bookSearchForm.author" placeholder="请输入作者" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadBookData">查询</el-button>
              <el-button @click="resetBookSearchForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="bookList" border style="width: 100%">
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
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <template v-if="hasPendingApplication(row.id)">
                  <el-button size="small" type="info" disabled>申请中</el-button>
                </template>
                <template v-else>
                  <el-button size="small" type="primary" @click="handleBorrow(row)" :disabled="row.status !== 1">借阅</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="bookPagination.page"
            v-model:page-size="bookPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="bookPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleBookSizeChange"
            @current-change="handleBookCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="借阅记录" name="borrow" v-if="userInfo.role === 'student'">
          <el-table :data="borrowList" border style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="bookName" label="书名" width="200" />
            <el-table-column prop="borrowTime" label="借阅时间" width="180" />
            <el-table-column prop="dueTime" label="到期时间" width="180" />
            <el-table-column prop="returnTime" label="归还时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'warning' : 'success'">
                  {{ row.status === 1 ? '借阅中' : '已归还' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleReturn(row)" :disabled="row.status !== 1">归还</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="borrowPagination.page"
            v-model:page-size="borrowPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="borrowPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleBorrowSizeChange"
            @current-change="handleBorrowCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
        
        <el-tab-pane label="门禁记录" name="access">
          <el-form :inline="true" :model="accessForm" class="mb-4">
            <el-form-item label="开始日期">
              <el-date-picker v-model="accessForm.startDate" type="date" placeholder="选择开始日期" />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker v-model="accessForm.endDate" type="date" placeholder="选择结束日期" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAccessData">查询</el-button>
              <el-button @click="resetAccessForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="processedAccessList" border style="width: 100%">
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
          
          <el-pagination
            v-model:current-page="accessPagination.page"
            v-model:page-size="accessPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="accessPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleAccessSizeChange"
            @current-change="handleAccessCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
          
          <div style="margin-top: 20px; text-align: center">
            <el-button type="primary" @click="showQRCodeDialog = true">二维码开门</el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="考勤打卡" name="attendance">
          <div class="attendance-section">
            <el-card shadow="hover" class="attendance-card">
              <template #header>
                <div class="card-header">
                  <span>打卡区域</span>
                </div>
              </template>
              <div class="checkin-container">
                <div class="location-info" v-if="currentLocation">
                  <el-icon><Position /></el-icon>
                  <span>当前位置: {{ currentLocation }}</span>
                </div>
                <div class="location-error" v-else-if="locationError">
                  <el-icon><Warning /></el-icon>
                  <span>{{ locationError }}</span>
                </div>
                <div class="location-loading" v-else>
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>正在获取位置信息...</span>
                </div>
                <el-form-item label="打卡位置" style="margin-top: 20px; width: 100%">
                  <el-select v-model="selectedLocation" placeholder="请选择打卡位置" style="width: 100%">
                    <el-option 
                      v-for="location in checkinLocations" 
                      :key="location.id" 
                      :label="location.locationName" 
                      :value="location" 
                    />
                  </el-select>
                </el-form-item>
                <el-button 
                  type="primary" 
                  @click="handleCheckin" 
                  :disabled="!currentLocation || !selectedLocation || checkingIn"
                  :loading="checkingIn"
                  style="margin-top: 20px; width: 200px"
                >
                  {{ checkingIn ? '打卡中...' : '立即打卡' }}
                </el-button>
                <p class="checkin-tip" v-if="currentLocation && selectedLocation">点击按钮完成考勤打卡</p>
              </div>
            </el-card>
            
            <el-card shadow="hover" style="margin-top: 20px">
              <template #header>
                <div class="card-header">
                  <span>考勤记录</span>
                </div>
              </template>
              <el-form :inline="true" :model="attendanceForm" class="mb-4">
                <el-form-item label="开始日期">
                  <el-date-picker v-model="attendanceForm.startDate" type="date" placeholder="选择开始日期" />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker v-model="attendanceForm.endDate" type="date" placeholder="选择结束日期" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadAttendanceData">查询</el-button>
                  <el-button @click="resetAttendanceForm">重置</el-button>
                </el-form-item>
              </el-form>
              
              <el-table :data="attendanceList" border style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="status" label="考勤状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getAttendanceStatusType(row.status)">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="打卡地点">
                  <template #default="{ row }">
                    {{ row.actualLocation || '未知地点' }}
                  </template>
                </el-table-column>
                <el-table-column prop="recordTime" label="打卡时间" width="180" />
                <el-table-column prop="deviceInfo" label="设备信息" />
              </el-table>
              
              <el-pagination
                v-model:current-page="attendancePagination.page"
                v-model:page-size="attendancePagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="attendancePagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleAttendanceSizeChange"
                @current-change="handleAttendanceCurrentChange"
                style="margin-top: 20px; justify-content: flex-end"
              />
            </el-card>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="充值管理" name="recharge">
          <div class="recharge-section">
            <el-card shadow="hover" class="recharge-card">
              <template #header>
                <div class="card-header">
                  <span>充值操作</span>
                  <el-button type="primary" @click="showRechargeDialog = true">立即充值</el-button>
                </div>
              </template>
              <div class="recharge-info">
                <div class="balance-info">
                  <el-tag type="info" size="large">当前余额: {{ cardInfo.balance }} 元</el-tag>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" style="margin-top: 20px">
              <template #header>
                <div class="card-header">
                  <span>充值记录</span>
                </div>
              </template>
              <el-form :inline="true" :model="rechargeForm" class="mb-4">
                <el-form-item label="开始日期">
                  <el-date-picker v-model="rechargeForm.startDate" type="date" placeholder="选择开始日期" />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker v-model="rechargeForm.endDate" type="date" placeholder="选择结束日期" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadRechargeData">查询</el-button>
                  <el-button @click="resetRechargeForm">重置</el-button>
                </el-form-item>
              </el-form>
              
              <el-table :data="rechargeList" border style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="amount" label="充值金额" width="120">
                  <template #default="{ row }">
                    <span style="color: #67c23a">+{{ row.amount }} 元</span>
                  </template>
                </el-table-column>
                <el-table-column prop="rechargeType" label="充值方式" width="120" />
                <el-table-column prop="createTime" label="充值时间" width="180" />
              </el-table>
              
              <el-pagination
                v-model:current-page="rechargePagination.page"
                v-model:page-size="rechargePagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="rechargePagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleRechargeSizeChange"
                @current-change="handleRechargeCurrentChange"
                style="margin-top: 20px; justify-content: flex-end"
              />
            </el-card>
          </div>
        </el-tab-pane>
        
        <!-- 二维码开门对话框 -->
        <el-dialog
          v-model="showQRCodeDialog"
          title="二维码开门"
          width="400px"
        >
          <div class="qr-code-container">
            <el-form :model="qrForm" label-width="100px">
              <el-form-item label="门禁点" required>
                <el-select v-model="qrForm.access_point_id" placeholder="请选择门禁点" @change="handleAccessPointChange">
                  <el-option 
                    v-for="point in accessPoints" 
                    :key="point.id" 
                    :label="point.name" 
                    :value="point.id" 
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <!-- 进出状态提示 -->
            <div v-if="qrForm.access_point_id" class="direction-tip" :class="nextDirection === '进' ? 'direction-in' : 'direction-out'">
              <el-icon :size="20">
                <component :is="nextDirection === '进' ? 'ArrowRightBold' : 'ArrowLeftBold'" />
              </el-icon>
              <span class="direction-text">本次扫码将{{ nextDirection === '进' ? '进入' : '离开' }}该门禁点</span>
            </div>
            <div class="qr-code" v-if="qrCodeUrl">
              <img :src="qrCodeUrl" alt="二维码" />
              <p class="qr-tip">请将二维码对准门禁设备扫描</p>
              <p class="qr-direction" :class="nextDirection === '进' ? 'direction-in' : 'direction-out'">
                扫码后{{ nextDirection === '进' ? '进入' : '离开' }}
              </p>
              <!-- 二维码状态 -->
              <div class="qr-status" :class="qrCodeStatus">
                <el-icon :size="16">
                  <component :is="qrCodeStatus === 'waiting' ? 'Loading' : qrCodeStatus === 'success' ? 'Check' : 'Warning'" />
                </el-icon>
                <span>
                  {{ qrCodeStatus === 'waiting' ? '等待扫码...' : 
                     qrCodeStatus === 'success' ? '扫码成功！' : 
                     qrCodeStatus === 'failed' ? '二维码已过期' : '' }}
                </span>
              </div>
            </div>
            <div class="qr-loading" v-else>
              <el-icon class="is-loading"><loading /></el-icon>
              <span>请选择门禁点并点击刷新二维码</span>
            </div>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showQRCodeDialog = false">关闭</el-button>
              <el-button type="primary" @click="refreshQRCode" :disabled="!qrForm.access_point_id">刷新二维码</el-button>
            </span>
          </template>
        </el-dialog>
      </el-tabs>

      <!-- 借阅对话框 -->
      <el-dialog
        v-model="borrowDialogVisible"
        title="图书借阅"
        width="400px"
      >
        <el-form :model="borrowForm" label-width="80px">
          <el-form-item label="借阅天数">
            <el-select v-model="borrowForm.borrowDays" placeholder="请选择借阅天数">
              <el-option
                v-for="option in borrowDaysOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="自定义天数" v-if="borrowForm.borrowDays === 0">
            <el-input v-model="borrowForm.customDays" type="number" placeholder="请输入借阅天数" min="1" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="borrowDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmitBorrowApplication">提交申请</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 充值对话框 -->
      <el-dialog
        v-model="showRechargeDialog"
        title="校园卡充值"
        width="400px"
      >
        <el-form :model="rechargeFormDialog" label-width="80px">
          <el-form-item label="卡号" required>
            <el-input v-model="rechargeFormDialog.cardNo" disabled />
          </el-form-item>
          <el-form-item label="充值金额" required>
            <el-input-number v-model="rechargeFormDialog.amount" :min="1" :max="1000" :step="10" :precision="2" />
          </el-form-item>
          <el-form-item label="充值方式" required>
            <el-select v-model="rechargeFormDialog.rechargeType">
              <el-option label="微信" value="微信" />
              <el-option label="支付宝" value="支付宝" />
              <el-option label="现金" value="现金" v-if="userInfo.role === 'admin'" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showRechargeDialog = false">取消</el-button>
            <el-button type="primary" @click="handleRecharge">确认充值</el-button>
          </span>
        </template>
      </el-dialog>
      
      <!-- 模拟扫码支付对话框 -->
      <el-dialog
        v-model="showScanDialog"
        title="扫码支付"
        width="400px"
      >
        <div class="scan-container">
          <div class="scan-qr-code" v-if="scanQRCodeUrl">
            <img :src="scanQRCodeUrl" alt="支付二维码" />
            <p class="scan-tip">请使用{{ rechargeFormDialog.rechargeType }}扫描二维码</p>
            <div class="scan-status" :class="scanStatus">
              <el-icon :size="16">
                <component :is="scanStatus === 'waiting' ? 'Loading' : scanStatus === 'scanning' ? 'Loading' : scanStatus === 'success' ? 'Check' : 'Warning'" />
              </el-icon>
              <span>
                {{ scanStatus === 'waiting' ? '等待扫码...' : 
                   scanStatus === 'scanning' ? '扫码中...' : 
                   scanStatus === 'success' ? '支付成功！' : 
                   scanStatus === 'failed' ? '支付超时' : '' }}
              </span>
            </div>
          </div>
          <div class="scan-loading" v-else>
            <el-icon class="is-loading"><loading /></el-icon>
            <span>正在生成支付二维码...</span>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelScan">取消支付</el-button>
            <el-button type="primary" v-if="scanStatus === 'failed'" @click="generateScanQRCode">重新生成</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElDatePicker, ElButton, ElIcon } from 'element-plus'
import { Loading, ArrowRightBold, ArrowLeftBold, Check, Warning, Position } from '@element-plus/icons-vue'
import { getStudentByNo, updateStudent } from '@/api/student'
import { getTeacherByNo } from '@/api/teacher'
import { getCardInfo, getCardByUserNo } from '@/api/card'
import { getConsumeList } from '@/api/consume'
import { getBorrowList, getBookList, submitBorrowApplication, getActiveBorrowCount, getBorrowApplications, returnBook } from '@/api/book'
import { getMyAccessRecords, getAccessPoints, createQRAccess } from '@/api/access'
import { getAttendanceList, createAttendance, getActiveLocations } from '@/api/attendance'
import { getRechargeList, recharge, rechargeByCardNo } from '@/api/recharge'

const router = useRouter()

const activeTab = ref('info')
const userInfo = reactive({
  role: '',
  name: '',
  username: '',
  gender: '',
  phone: '',
  email: ''
})

const cardInfo = reactive({
  cardNo: '',
  status: 1,
  balance: '0.00',
  createTime: ''
})

// 充值相关
const showRechargeDialog = ref(false)
const showScanDialog = ref(false)
const rechargeForm = reactive({
  startDate: '',
  endDate: ''
})
const rechargeList = ref([])
const rechargePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const rechargeFormDialog = reactive({
  cardNo: '',
  amount: 100,
  rechargeType: '微信'
})
// 模拟扫码支付相关
const scanQRCodeUrl = ref('')
const scanStatus = ref('waiting') // waiting, success, failed
const scanPolling = ref(null)

const consumeForm = reactive({
  startDate: '',
  endDate: ''
})

const consumeList = ref([])
const consumePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const borrowList = ref([])
const borrowPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const bookList = ref([])
const bookPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 借阅申请列表
const borrowApplications = ref([])
const loadBorrowApplications = async () => {
  try {
    if (!cardInfo.id) return
    
    const res = await getBorrowApplications({
      card_id: cardInfo.id
    })
    if (res.code === 0) {
      borrowApplications.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载借阅申请列表失败:', error)
  }
}

// 检查图书是否有未处理的借阅申请
const hasPendingApplication = (bookId) => {
  return borrowApplications.value.some(app => app.bookId === bookId && app.status === 1)
}

const bookSearchForm = reactive({
  bookName: '',
  author: ''
})

const borrowDialogVisible = ref(false)
const currentBook = ref(null)
const borrowForm = reactive({
  borrowDays: 7,
  customDays: ''
})
const borrowDaysOptions = [
  { value: 7, label: '7天' },
  { value: 15, label: '15天' },
  { value: 30, label: '30天' },
  { value: 0, label: '自定义' }
]

// 门禁记录相关
const accessForm = reactive({
  startDate: '',
  endDate: ''
})

const accessList = ref([])
const processedAccessList = ref([])
const accessPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 二维码开门相关
const showQRCodeDialog = ref(false)
const qrForm = reactive({
  access_point_id: ''
})
const qrCodeUrl = ref('')
const accessPoints = ref([])

// 计算下次扫码的方向
const nextDirection = computed(() => {
  if (!qrForm.access_point_id || !processedAccessList.value.length) {
    return '进'
  }
  
  // 查找该门禁点最后一次通行记录
  const lastRecord = processedAccessList.value.find(
    record => record.accessPointId === qrForm.access_point_id
  )
  
  // 如果最后一次是"进"，则下次为"出"；否则为"进"
  if (lastRecord && lastRecord.direction === '进') {
    return '出'
  }
  return '进'
})

const loadUserInfo = async () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      userInfo.role = user.role
      userInfo.username = user.username
      
      // 根据角色获取详细信息
      if (user.role === 'student') {
        const res = await getStudentByNo(user.username)
        if (res.code === 0) {
          Object.assign(userInfo, res.data)
        }
      } else if (user.role === 'teacher') {
        const res = await getTeacherByNo(user.username)
        if (res.code === 0) {
          Object.assign(userInfo, res.data)
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadCardInfo = async () => {
  try {
    // 使用新的接口根据用户编号和类型查询校园卡信息
    if (userInfo.username && userInfo.role) {
      const res = await getCardByUserNo(userInfo.username, userInfo.role)
      if (res.code === 0) {
        // 转换开卡时间格式
        if (res.data.createTime) {
          res.data.createTime = new Date(res.data.createTime).toLocaleString()
        }
        Object.assign(cardInfo, res.data)
        console.log('校园卡信息:', cardInfo)
      }
    }
  } catch (error) {
    console.error('加载校园卡信息失败:', error)
  }
}

const loadConsumeData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getConsumeList({
        card_id: cardInfo.id,
        start_date: consumeForm.startDate ? new Date(consumeForm.startDate).toISOString().split('T')[0] : '',
        end_date: consumeForm.endDate ? new Date(consumeForm.endDate).toISOString().split('T')[0] : '',
        page: consumePagination.page,
        size: consumePagination.size
      })
      if (res.code === 0) {
        consumeList.value = res.data.records || []
        consumePagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载消费记录失败:', error)
  }
}

const loadBorrowData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getBorrowList({
        card_id: cardInfo.id,
        page: borrowPagination.page,
        size: borrowPagination.size
      })
      if (res.code === 0) {
        borrowList.value = res.data.records || []
        borrowPagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载借阅记录失败:', error)
  }
}

const resetConsumeForm = () => {
  consumeForm.startDate = ''
  consumeForm.endDate = ''
  loadConsumeData()
}

const handleConsumeSizeChange = (val) => {
  consumePagination.size = val
  loadConsumeData()
}

const handleConsumeCurrentChange = (val) => {
  consumePagination.page = val
  loadConsumeData()
}

const handleBorrowSizeChange = (val) => {
  borrowPagination.size = val
  loadBorrowData()
}

const handleBorrowCurrentChange = (val) => {
  borrowPagination.page = val
  loadBorrowData()
}

const loadBookData = async () => {
  try {
    const res = await getBookList({
      bookName: bookSearchForm.bookName,
      author: bookSearchForm.author,
      page: bookPagination.page,
      size: bookPagination.size
    })
    if (res.code === 0) {
      bookList.value = res.data.records || []
      bookPagination.total = res.data.total || 0
    }
    // 加载借阅申请列表
    await loadBorrowApplications()
  } catch (error) {
    console.error('加载图书列表失败:', error)
  }
}

const resetBookSearchForm = () => {
  bookSearchForm.bookName = ''
  bookSearchForm.author = ''
  loadBookData()
}

const handleBookSizeChange = (val) => {
  bookPagination.size = val
  loadBookData()
}

const handleBookCurrentChange = (val) => {
  bookPagination.page = val
  loadBookData()
}

const handleBorrow = (book) => {
  currentBook.value = book
  borrowDialogVisible.value = true
}

const handleSubmitBorrowApplication = async () => {
  try {
    if (!cardInfo.id || !currentBook.value) {
      ElMessage.error('请先加载校园卡信息')
      return
    }

    // 检查当前借阅数量
    const countRes = await getActiveBorrowCount({ card_id: cardInfo.id })
    if (countRes.code === 0 && countRes.data >= 3) {
      ElMessage.error('您当前已借阅3本图书，无法继续借阅')
      return
    }

    let borrowDays = borrowForm.borrowDays
    if (borrowForm.borrowDays === 0) {
      if (!borrowForm.customDays || isNaN(borrowForm.customDays) || borrowForm.customDays <= 0) {
        ElMessage.error('请输入有效的自定义借阅天数')
        return
      }
      borrowDays = parseInt(borrowForm.customDays)
    }

    const res = await submitBorrowApplication({
      cardId: cardInfo.id,
      bookId: currentBook.value.id,
      borrowDays: borrowDays
    })

    if (res.code === 0) {
      ElMessage.success('借阅申请提交成功，请等待管理员审批')
      borrowDialogVisible.value = false
      // 重新加载图书列表和借阅申请列表
      await loadBookData()
    } else {
      ElMessage.error(res.message || '借阅申请提交失败')
    }
  } catch (error) {
    console.error('提交借阅申请失败:', error)
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('提交借阅申请失败，请稍后重试')
    }
  }
}

const handleReturn = async (borrowRecord) => {
  try {
    const res = await returnBook({ borrow_id: borrowRecord.id })
    if (res.code === 0) {
      ElMessage.success('图书归还成功')
      loadBorrowData()
      loadBookData() // 重新加载图书列表，更新图书状态
    } else {
      ElMessage.error(res.message || '图书归还失败')
    }
  } catch (error) {
    console.error('归还图书失败:', error)
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('归还图书失败，请稍后重试')
    }
  }
}

const loadAccessData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getMyAccessRecords({
        card_id: cardInfo.id,
        page: accessPagination.page,
        size: accessPagination.size
      })
      if (res.code === 0) {
        accessList.value = res.data.records || []
        accessPagination.total = res.data.total || 0
        
        // 处理数据，转换卡 ID 为卡号
        const processedData = await Promise.all(
          accessList.value.map(async (record) => {
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
        
        processedAccessList.value = processedData
      }
    }
  } catch (error) {
    console.error('加载门禁记录失败:', error)
  }
}

const loadAccessPoints = async () => {
  try {
    const res = await getAccessPoints({})
    if (res.code === 0) {
      accessPoints.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载门禁点列表失败:', error)
  }
}

const qrCodeStatus = ref('waiting') // waiting, scanning, success, failed
const qrCodePolling = ref(null)

const generateQRCode = () => {
  if (!qrForm.access_point_id) {
    ElMessage.warning('请选择门禁点')
    return
  }
  
  qrCodeStatus.value = 'waiting'
  
  // 生成二维码（包含必要的参数）
  const qrCode = Math.random().toString(36).substring(2, 15)
  const cardId = cardInfo.id
  const accessPointId = qrForm.access_point_id
  
  // 构建二维码内容，包含必要的参数
  const qrData = JSON.stringify({
    card_id: cardId,
    access_point_id: accessPointId,
    qr_code: qrCode,
    direction: nextDirection.value
  })
  
  qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrData)}`
  
  // 模拟二维码有效期为60秒
  setTimeout(() => {
    if (showQRCodeDialog.value && qrCodeStatus.value === 'waiting') {
      qrCodeUrl.value = ''
      qrCodeStatus.value = 'failed'
      ElMessage.warning('二维码已过期，请重新生成')
    }
  }, 60000)
  
  // 开始轮询，检查二维码是否被扫描
  startQRCodePolling(qrCode)
}

const startQRCodePolling = (qrCode) => {
  // 清除之前的轮询
  if (qrCodePolling.value) {
    clearInterval(qrCodePolling.value)
  }
  
  // 每1秒轮询一次
  qrCodePolling.value = setInterval(async () => {
    try {
      // 这里应该调用后端的扫码状态检查接口
      // 由于后端没有这个接口，我们模拟一个随机的扫描结果
      const random = Math.random()
      if (random > 0.7) {
        // 模拟扫码成功
        qrCodeStatus.value = 'success'
        clearInterval(qrCodePolling.value)
        qrCodePolling.value = null
        
        // 调用后端接口，记录门禁记录
        try {
          const response = await createQRAccess({
            card_id: cardInfo.id,
            access_point_id: qrForm.access_point_id,
            qr_code: qrCode
          })
          
          if (response.code === 0) {
            // 显示成功消息
            ElMessage.success(`开门成功！您已${nextDirection.value === '进' ? '进入' : '离开'}该门禁点`)
            
            // 重新加载门禁记录
            loadAccessData()
          } else {
            ElMessage.error('开门失败：' + response.message)
          }
        } catch (error) {
          console.error('调用开门接口失败:', error)
          ElMessage.error('开门失败，请稍后重试')
        }
        
        // 3秒后关闭对话框
        setTimeout(() => {
          showQRCodeDialog.value = false
          qrCodeUrl.value = ''
        }, 3000)
      }
    } catch (error) {
      console.error('检查扫码状态失败:', error)
    }
  }, 1000)
}

// 关闭对话框时清除轮询
watch(showQRCodeDialog, (newVal) => {
  if (!newVal && qrCodePolling.value) {
    clearInterval(qrCodePolling.value)
    qrCodePolling.value = null
    qrCodeUrl.value = ''
    qrCodeStatus.value = 'waiting'
  }
})

const refreshQRCode = () => {
  generateQRCode()
}

const handleAccessPointChange = () => {
  // 门禁点改变时，清空二维码，让用户重新生成
  qrCodeUrl.value = ''
}

const resetAccessForm = () => {
  accessForm.startDate = ''
  accessForm.endDate = ''
  loadAccessData()
}

const handleAccessSizeChange = (val) => {
  accessPagination.size = val
  loadAccessData()
}

const handleAccessCurrentChange = (val) => {
  accessPagination.page = val
  loadAccessData()
}

const getStatusType = (status) => {
  return status === '成功' ? 'success' : 'danger'
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

// 考勤相关
const attendanceForm = reactive({
  startDate: '',
  endDate: ''
})

const attendanceList = ref([])
const attendancePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 位置信息
const currentLocation = ref('')
const locationError = ref('')
const currentLatitude = ref(null)
const currentLongitude = ref(null)
const checkingIn = ref(false)

// 从后端获取的打卡位置
const checkinLocations = ref([])
// 选中的打卡位置
const selectedLocation = ref(null)

// 计算两点之间的距离（单位：米）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371e3; // 地球半径（米）
  const φ1 = lat1 * Math.PI / 180;
  const φ2 = lat2 * Math.PI / 180;
  const Δφ = (lat2 - lat1) * Math.PI / 180;
  const Δλ = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

// 检查是否在打卡范围内
const isInCheckinArea = () => {
  if (!currentLatitude.value || !currentLongitude.value || !selectedLocation.value) {
    return false;
  }

  const location = selectedLocation.value;
  const distance = calculateDistance(
    currentLatitude.value,
    currentLongitude.value,
    location.latitude,
    location.longitude
  );
  return distance <= location.radius;
}

// 获取当前位置
const getCurrentLocation = () => {
  if (navigator.geolocation) {
    locationError.value = ''
    navigator.geolocation.getCurrentPosition(
      (position) => {
        currentLatitude.value = position.coords.latitude
        currentLongitude.value = position.coords.longitude
        // 这里可以调用逆地理编码API获取具体位置名称
        currentLocation.value = `纬度: ${currentLatitude.value.toFixed(4)}, 经度: ${currentLongitude.value.toFixed(4)}`
      },
      (error) => {
        locationError.value = '获取位置失败，请检查位置权限设置'
        currentLocation.value = ''
        currentLatitude.value = null
        currentLongitude.value = null
      }
    )
  } else {
    locationError.value = '您的浏览器不支持地理定位'
  }
}

// 处理打卡
const handleCheckin = async () => {
  if (!cardInfo.id) {
    ElMessage.error('请先加载校园卡信息')
    return
  }
  
  if (!currentLatitude.value || !currentLongitude.value) {
    ElMessage.error('无法获取位置信息，请检查位置权限')
    return
  }
  
  if (!selectedLocation.value) {
    ElMessage.error('请选择打卡位置')
    return
  }
  
  // 检查是否在打卡范围内
  if (!isInCheckinArea()) {
    ElMessage.error('您不在允许的打卡范围内，请前往指定地点打卡')
    return
  }
  
  checkingIn.value = true
  try {
    const res = await createAttendance({
      card_id: cardInfo.id,
      location_id: selectedLocation.value.id,
      actual_location: currentLocation.value,
      actual_latitude: currentLatitude.value,
      actual_longitude: currentLongitude.value,
      device_info: navigator.userAgent
    })
    
    if (res.code === 0) {
      ElMessage.success('打卡成功！')
      // 重新加载考勤记录
      loadAttendanceData()
    } else {
      ElMessage.error(res.message || '打卡失败')
    }
  } catch (error) {
    console.error('打卡失败:', error)
    // 检查是否是重复打卡的错误
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error.message && error.message.includes('今日已在该位置打卡')) {
      ElMessage.error('今日已在该位置打卡，请勿重复打卡')
    } else {
      ElMessage.error('打卡失败，请稍后重试')
    }
  } finally {
    checkingIn.value = false
  }
}

// 加载有效打卡位置
const loadActiveLocations = async () => {
  try {
    const res = await getActiveLocations()
    if (res.code === 0) {
      checkinLocations.value = res.data || []
    }
  } catch (error) {
    console.error('加载打卡位置失败:', error)
  }
}

// 加载考勤记录
const loadAttendanceData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getAttendanceList({
        card_id: cardInfo.id,
        start_date: attendanceForm.startDate ? new Date(attendanceForm.startDate).toISOString().split('T')[0] : '',
        end_date: attendanceForm.endDate ? new Date(attendanceForm.endDate).toISOString().split('T')[0] : '',
        page: attendancePagination.page,
        size: attendancePagination.size
      })
      if (res.code === 0) {
        attendanceList.value = res.data.records || []
        attendancePagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  }
}

// 重置考勤表单
const resetAttendanceForm = () => {
  attendanceForm.startDate = ''
  attendanceForm.endDate = ''
  loadAttendanceData()
}

// 处理考勤分页
const handleAttendanceSizeChange = (val) => {
  attendancePagination.size = val
  loadAttendanceData()
}

const handleAttendanceCurrentChange = (val) => {
  attendancePagination.page = val
  loadAttendanceData()
}

// 获取考勤状态类型
const getAttendanceStatusType = (status) => {
  const map = {
    '正常': 'success',
    '迟到': 'warning',
    '早退': 'warning',
    '缺勤': 'danger'
  }
  return map[status] || 'info'
}

const logout = () => {
  // 清除本地存储中的用户信息和token
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('退出登录成功')
  // 跳转到登录页面
  router.push('/login')
}

const handleUpdateProfile = async () => {
  try {
    // 验证手机号格式
    if (userInfo.phone && !/^1[3-9]\d{9}$/.test(userInfo.phone)) {
      ElMessage.error('请输入正确的手机号')
      return
    }
    
    const res = await updateStudent(userInfo)
    if (res.code === 0) {
      ElMessage.success('个人信息更新成功')
      // 重新加载用户信息
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  }
}

// 充值相关方法
const loadRechargeData = async () => {
  try {
    if (cardInfo.id) {
      const res = await getRechargeList({
        card_id: cardInfo.id,
        page: rechargePagination.page,
        size: rechargePagination.size
      })
      if (res.code === 0) {
        rechargeList.value = res.data.records || []
        rechargePagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载充值记录失败:', error)
  }
}

const handleRecharge = async () => {
  try {
    if (!cardInfo.id || !rechargeFormDialog.amount || !rechargeFormDialog.rechargeType) {
      ElMessage.error('请填写完整的充值信息')
      return
    }

    // 显示扫码支付对话框
    showRechargeDialog.value = false
    showScanDialog.value = true
    
    // 生成支付二维码
    generateScanQRCode()
  } catch (error) {
    console.error('充值操作失败:', error)
    ElMessage.error('充值操作失败，请稍后重试')
  }
}

const generateScanQRCode = () => {
  scanStatus.value = 'waiting'
  
  // 生成二维码（包含必要的参数）
  const scanCode = Math.random().toString(36).substring(2, 15)
  const cardId = cardInfo.id
  const amount = rechargeFormDialog.amount
  const rechargeType = rechargeFormDialog.rechargeType
  
  // 构建二维码内容，包含必要的参数
  const scanData = JSON.stringify({
    card_id: cardId,
    amount: amount,
    recharge_type: rechargeType,
    scan_code: scanCode
  })
  
  scanQRCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(scanData)}`
  
  // 5秒后提示扫码中
  setTimeout(() => {
    if (showScanDialog.value && scanStatus.value === 'waiting') {
      scanStatus.value = 'scanning'
    }
  }, 5000)
  
  // 模拟二维码有效期为15秒
  setTimeout(() => {
    if (showScanDialog.value && (scanStatus.value === 'waiting' || scanStatus.value === 'scanning')) {
      scanQRCodeUrl.value = ''
      scanStatus.value = 'failed'
      ElMessage.warning('支付超时，请重新尝试')
    }
  }, 15000)
  
  // 开始轮询，检查支付状态
  startScanPolling(scanCode)
}

const startScanPolling = (scanCode) => {
  // 清除之前的轮询
  if (scanPolling.value) {
    clearTimeout(scanPolling.value)
  }
  
  // 10秒后模拟支付成功
  scanPolling.value = setTimeout(async () => {
    if (showScanDialog.value) {
      // 模拟支付成功
      scanStatus.value = 'success'
      
      // 调用后端充值接口
      try {
        console.log('充值参数:', {
          card_id: cardInfo.id,
          amount: rechargeFormDialog.amount,
          recharge_type: rechargeFormDialog.rechargeType
        })
        
        const response = await recharge({
          card_id: cardInfo.id,
          amount: rechargeFormDialog.amount,
          recharge_type: rechargeFormDialog.rechargeType
        })
        
        console.log('充值响应:', response)
        
        if (response.code === 0) {
          // 显示成功消息
          ElMessage.success(`充值成功！已为您的校园卡充值 ${rechargeFormDialog.amount} 元`)
          
          // 重新加载校园卡信息和充值记录
          await loadCardInfo()
          loadRechargeData()
        } else {
          ElMessage.error('充值失败：' + response.message)
        }
      } catch (error) {
        console.error('调用充值接口失败:', error)
        ElMessage.error('充值失败，请稍后重试')
      }
      
      // 2秒后关闭对话框
      setTimeout(() => {
        showScanDialog.value = false
        scanQRCodeUrl.value = ''
      }, 2000)
    }
  }, 10000)
}

const cancelScan = () => {
  if (scanPolling.value) {
    clearTimeout(scanPolling.value)
    scanPolling.value = null
  }
  showScanDialog.value = false
  scanQRCodeUrl.value = ''
  scanStatus.value = 'waiting'
}

const resetRechargeForm = () => {
  rechargeForm.startDate = ''
  rechargeForm.endDate = ''
  loadRechargeData()
}

const handleRechargeSizeChange = (val) => {
  rechargePagination.size = val
  loadRechargeData()
}

const handleRechargeCurrentChange = (val) => {
  rechargePagination.page = val
  loadRechargeData()
}

// 监听充值对话框的显示
watch(() => showRechargeDialog.value, (newVal) => {
  if (newVal) {
    rechargeFormDialog.cardNo = cardInfo.cardNo
    rechargeFormDialog.amount = 100
    rechargeFormDialog.rechargeType = '微信'
  }
})

// 监听扫码支付对话框的显示
watch(() => showScanDialog.value, (newVal) => {
  if (!newVal && scanPolling.value) {
    clearInterval(scanPolling.value)
    scanPolling.value = null
    scanQRCodeUrl.value = ''
    scanStatus.value = 'waiting'
  }
})

// 监听二维码开门对话框的显示
watch(() => showQRCodeDialog.value, (newVal) => {
  if (newVal) {
    qrCodeUrl.value = ''
    qrForm.access_point_id = ''
    loadAccessPoints()
  }
})

onMounted(async () => {
  await loadUserInfo()
  await loadCardInfo()
  loadConsumeData()
  loadAccessData()
  loadAttendanceData()
  loadRechargeData()
  loadActiveLocations()
  getCurrentLocation()
  if (userInfo.role === 'student') {
    loadBorrowData()
    loadBookData()
  }
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-4 {
  margin-bottom: 16px;
}

.qr-code-container {
  text-align: center;
  padding: 20px 0;
}

.qr-code {
  margin-top: 20px;
}

.qr-code img {
  width: 200px;
  height: 200px;
}

.qr-tip {
  margin-top: 10px;
  color: #606266;
}

.qr-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
}

.qr-loading .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 进出状态提示样式 */
.direction-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 15px 0;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: bold;
}

.direction-in {
  background-color: #f0f9ff;
  color: #409eff;
  border: 1px solid #409eff;
}

.direction-out {
  background-color: #fff0f0;
  color: #f56c6c;
  border: 1px solid #f56c6c;
}

.direction-text {
  margin-left: 8px;
}

.qr-direction {
  margin-top: 10px;
  padding: 5px 15px;
  border-radius: 4px;
  font-weight: bold;
  display: inline-block;
}

/* 二维码状态样式 */
.qr-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  padding: 5px 15px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
}

.qr-status.waiting {
  background-color: #f5f7fa;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.qr-status.success {
  background-color: #f0f9ff;
  color: #67c23a;
  border: 1px solid #e1f5d6;
}

.qr-status.failed {
  background-color: #fff0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.qr-status span {
  margin-left: 8px;
}

/* 考勤打卡相关样式 */
.attendance-section {
  padding: 20px 0;
}

.attendance-card {
  max-width: 600px;
  margin: 0 auto;
}

.checkin-container {
  text-align: center;
  padding: 30px 0;
}

.location-info {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f9ff;
  color: #409eff;
  padding: 10px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.location-error {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff0f0;
  color: #f56c6c;
  padding: 10px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.location-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  margin-bottom: 20px;
}

.checkin-tip {
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.location-info span,
.location-error span,
.location-loading span {
  margin-left: 8px;
}

/* 充值相关样式 */
.recharge-section {
  padding: 20px 0;
}

.recharge-card {
  max-width: 600px;
  margin: 0 auto;
}

.recharge-info {
  text-align: center;
  padding: 30px 0;
}

.balance-info {
  margin-bottom: 20px;
}

/* 扫码支付相关样式 */
.scan-container {
  text-align: center;
  padding: 20px 0;
}

.scan-qr-code {
  margin-top: 20px;
}

.scan-qr-code img {
  width: 200px;
  height: 200px;
}

.scan-tip {
  margin-top: 10px;
  color: #606266;
}

.scan-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
}

.scan-loading .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 扫码状态样式 */
.scan-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  padding: 5px 15px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
}

.scan-status.waiting {
  background-color: #f5f7fa;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.scan-status.scanning {
  background-color: #f5f7fa;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.scan-status.success {
  background-color: #f0f9ff;
  color: #67c23a;
  border: 1px solid #e1f5d6;
}

.scan-status.failed {
  background-color: #fff0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.scan-status span {
  margin-left: 8px;
}
</style>