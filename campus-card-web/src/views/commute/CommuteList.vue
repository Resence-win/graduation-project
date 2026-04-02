<template>
  <div class="commute-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通勤车管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card" @tab-click="(tab) => handleTabChange(tab.props.name)">
        <!-- 线路管理 -->
        <el-tab-pane label="线路管理" name="route">
          <div class="tab-content">
            <div class="header-actions">
              <el-button type="primary" @click="handleAddRoute">新增线路</el-button>
            </div>
            
            <el-form :inline="true" :model="routeSearchForm">
              <el-form-item label="线路名称">
                <el-input v-model="routeSearchForm.routeName" placeholder="请输入线路名称" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="routeSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="正常" value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleRouteSearch">查询</el-button>
                <el-button @click="handleRouteReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table :data="routeTableData" border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="routeName" label="线路名称" width="180" />
              <el-table-column prop="startStation" label="起点站" width="150" />
              <el-table-column prop="endStation" label="终点站" width="150" />
              <el-table-column prop="totalDistance" label="全程距离(公里)" width="120" />
              <el-table-column prop="totalTime" label="全程时间(分钟)" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常' : '异常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="handleEditRoute(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteRoute(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="routePagination.page"
              v-model:page-size="routePagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="routePagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRouteSizeChange"
              @current-change="handleRouteCurrentChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>
        
        <!-- 车辆管理 -->
        <el-tab-pane label="车辆管理" name="vehicle">
          <div class="tab-content">
            <div class="header-actions">
              <el-button type="primary" @click="handleAddVehicle">新增车辆</el-button>
            </div>
            
            <el-form :inline="true" :model="vehicleSearchForm">
              <el-form-item label="车牌号">
                <el-input v-model="vehicleSearchForm.plateNumber" placeholder="请输入车牌号" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="vehicleSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="正常" value="1" />
                  <el-option label="维护" value="2" />
                  <el-option label="停用" value="3" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleVehicleSearch">查询</el-button>
                <el-button @click="handleVehicleReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table :data="vehicleTableData" border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="vehicleType" label="车辆类型" width="100" />
              <el-table-column prop="seatCount" label="座位数" width="80" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
                    {{ row.status === 1 ? '正常' : row.status === 2 ? '维护' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="handleEditVehicle(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteVehicle(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="vehiclePagination.page"
              v-model:page-size="vehiclePagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="vehiclePagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleVehicleSizeChange"
              @current-change="handleVehicleCurrentChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>
        
        <!-- 站点管理 -->
        <el-tab-pane label="站点管理" name="station">
          <div class="tab-content">
            <div class="header-actions">
              <el-button type="primary" @click="handleAddStation">新增站点</el-button>
            </div>
            
            <el-form :inline="true" :model="stationSearchForm">
              <el-form-item label="站点名称">
                <el-input v-model="stationSearchForm.stationName" placeholder="请输入站点名称" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="stationSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="正常" value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleStationSearch">查询</el-button>
                <el-button @click="handleStationReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table :data="stationTableData" border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="stationName" label="站点名称" width="150" />
              <el-table-column prop="location" label="站点位置" width="200" />
              <el-table-column prop="latitude" label="纬度" width="120" />
              <el-table-column prop="longitude" label="经度" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常' : '异常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="handleEditStation(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteStation(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="stationPagination.page"
              v-model:page-size="stationPagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="stationPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleStationSizeChange"
              @current-change="handleStationCurrentChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>
        

        
        <!-- 时刻表管理 -->
        <el-tab-pane label="时刻表管理" name="schedule">
          <div class="tab-content">
            <div class="header-actions">
              <el-button type="primary" @click="handleAddSchedule">新增时刻表</el-button>
            </div>
            
            <el-form :inline="true" :model="scheduleSearchForm">
              <el-form-item label="线路ID">
                <el-input v-model="scheduleSearchForm.routeId" placeholder="请输入线路ID" clearable />
              </el-form-item>
              <el-form-item label="车辆ID">
                <el-input v-model="scheduleSearchForm.vehicleId" placeholder="请输入车辆ID" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="scheduleSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="正常" value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleScheduleSearch">查询</el-button>
                <el-button @click="handleScheduleReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table :data="scheduleTableData" border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="线路" width="150">
                <template #default="{ row }">
                  {{ getRouteName(row.routeId) }}
                </template>
              </el-table-column>
              <el-table-column label="车辆" width="120">
                <template #default="{ row }">
                  {{ getVehiclePlateNumber(row.vehicleId) }}
                </template>
              </el-table-column>
              <el-table-column prop="departureTime" label="发车时间" width="120" />
              <el-table-column prop="frequency" label="班次频率" width="100" />
              <el-table-column prop="startDate" label="开始日期" width="120" />
              <el-table-column prop="endDate" label="结束日期" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常' : '异常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="handleEditSchedule(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteSchedule(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="schedulePagination.page"
              v-model:page-size="schedulePagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="schedulePagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleScheduleSizeChange"
              @current-change="handleScheduleCurrentChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>
        
        <!-- 乘车记录 -->
        <el-tab-pane label="乘车记录" name="record">
          <div class="tab-content">
            <el-form :inline="true" :model="recordSearchForm">
              <el-form-item label="卡号">
                <el-input v-model="recordSearchForm.cardId" placeholder="请输入卡号" clearable />
              </el-form-item>
              <el-form-item label="线路ID">
                <el-input v-model="recordSearchForm.routeId" placeholder="请输入线路ID" clearable />
              </el-form-item>
              <el-form-item label="车辆ID">
                <el-input v-model="recordSearchForm.vehicleId" placeholder="请输入车辆ID" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleRecordSearch">查询</el-button>
                <el-button @click="handleRecordReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <el-table :data="recordTableData" border style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="卡号" width="150">
                <template #default="{ row }">
                  {{ getCardInfo(row.cardId) }}
                </template>
              </el-table-column>
              <el-table-column label="线路" width="150">
                <template #default="{ row }">
                  {{ getRouteName(row.routeId) }}
                </template>
              </el-table-column>
              <el-table-column label="车辆" width="120">
                <template #default="{ row }">
                  {{ getVehiclePlateNumber(row.vehicleId) }}
                </template>
              </el-table-column>
              <el-table-column label="班次" width="100">
                <template #default="{ row }">
                  {{ getScheduleInfo(row.scheduleId) }}
                </template>
              </el-table-column>
              <el-table-column prop="seatNumber" label="座位号" width="100" />
              <el-table-column prop="rideTime" label="乘车时间" width="180" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '正常' : '异常' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            
            <el-pagination
              v-model:current-page="recordPagination.page"
              v-model:page-size="recordPagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="recordPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRecordSizeChange"
              @current-change="handleRecordCurrentChange"
              style="margin-top: 20px; justify-content: flex-end"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 新增/编辑线路对话框 -->
    <el-dialog
      v-model="routeDialogVisible"
      :title="routeDialogTitle"
      width="500px"
    >
      <el-form :model="routeForm" label-width="100px">
        <el-form-item label="线路名称" required>
          <el-input v-model="routeForm.routeName" placeholder="请输入线路名称" />
        </el-form-item>
        <el-form-item label="起点站" required>
          <el-select v-model="routeForm.startStationId" placeholder="请选择起点站">
            <el-option v-for="station in stationOptions" :key="station.id" :label="station.stationName" :value="station.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="终点站" required>
          <el-select v-model="routeForm.endStationId" placeholder="请选择终点站">
            <el-option v-for="station in stationOptions" :key="station.id" :label="station.stationName" :value="station.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="全程距离(公里)">
          <el-input v-model.number="routeForm.totalDistance" type="number" placeholder="自动计算" readonly />
        </el-form-item>
        <el-form-item label="全程时间(分钟)">
          <el-input v-model.number="routeForm.totalTime" type="number" placeholder="自动计算" readonly />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="routeForm.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="异常" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="routeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRoute">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 新增/编辑车辆对话框 -->
    <el-dialog
      v-model="vehicleDialogVisible"
      :title="vehicleDialogTitle"
      width="500px"
    >
      <el-form :model="vehicleForm" label-width="100px">
        <el-form-item label="车牌号" required>
          <el-input v-model="vehicleForm.plateNumber" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-input v-model="vehicleForm.vehicleType" placeholder="请输入车辆类型" />
        </el-form-item>
        <el-form-item label="座位数">
          <el-input v-model.number="vehicleForm.seatCount" type="number" placeholder="请输入座位数" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="vehicleForm.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="维护" value="2" />
            <el-option label="停用" value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="vehicleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveVehicle">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 新增/编辑站点对话框 -->
    <el-dialog
      v-model="stationDialogVisible"
      :title="stationDialogTitle"
      width="500px"
    >
      <el-form :model="stationForm" label-width="100px">
        <el-form-item label="站点名称" required>
          <el-input v-model="stationForm.stationName" placeholder="请输入站点名称" />
        </el-form-item>
        <el-form-item label="站点位置">
          <el-input v-model="stationForm.location" placeholder="请输入站点位置" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model.number="stationForm.latitude" type="number" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model.number="stationForm.longitude" type="number" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="stationForm.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="异常" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveStation">保存</el-button>
        </span>
      </template>
    </el-dialog>
    

    
    <!-- 新增/编辑时刻表对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      :title="scheduleDialogTitle"
      width="500px"
    >
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="线路" required>
          <el-select v-model="scheduleForm.routeId" placeholder="请选择线路">
            <el-option v-for="route in routeOptions" :key="route.id" :label="route.routeName" :value="route.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆" required>
          <el-select v-model="scheduleForm.vehicleId" placeholder="请选择车辆">
            <el-option v-for="vehicle in vehicleOptions" :key="vehicle.id" :label="vehicle.plateNumber" :value="vehicle.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发车时间" required>
          <el-time-picker v-model="scheduleForm.departureTime" placeholder="请选择发车时间" />
        </el-form-item>
        <el-form-item label="班次频率">
          <el-select v-model="scheduleForm.frequency" placeholder="请选择班次频率">
            <el-option label="每天" value="每天" />
            <el-option label="工作日" value="工作日" />
            <el-option label="周末" value="周末" />
            <el-option label="周一至周三" value="周一至周三" />
            <el-option label="周四至周五" value="周四至周五" />
            <el-option label="周六至周日" value="周六至周日" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="scheduleForm.startDate" type="date" placeholder="请选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="scheduleForm.endDate" type="date" placeholder="请选择结束日期" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="scheduleForm.status" placeholder="请选择状态">
            <el-option label="正常" value="1" />
            <el-option label="异常" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveSchedule">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getCommuteList,
  getRouteList, addRoute, updateRoute, deleteRoute,
  getVehicleList, addVehicle, updateVehicle, deleteVehicle,
  getStationList, addStation, updateStation, deleteStation,
  getScheduleList, addSchedule, updateSchedule, deleteSchedule
} from '@/api/commute'
import { getCardList } from '@/api/card'

// 标签页
const activeTab = ref('route')

// 线路管理
const routeTableData = ref([])
const routeSearchForm = reactive({
  routeName: null,
  status: null
})
const routePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const routeDialogVisible = ref(false)
const routeDialogTitle = ref('新增线路')
const routeForm = reactive({
  id: null,
  routeName: '',
  startStationId: null,
  endStationId: null,
  startStation: '',
  endStation: '',
  totalDistance: null,
  totalTime: null,
  status: 1
})

// 站点选项
const stationOptions = ref([])

// 线路选项
const routeOptions = ref([])

// 车辆选项
const vehicleOptions = ref([])

// 加载站点选项
const loadStationOptions = async () => {
  try {
    const res = await getStationList({ page: 1, size: 100 })
    if (res.code === 0) {
      stationOptions.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载站点选项失败:', error)
  }
}

// 加载线路选项
const loadRouteOptions = async () => {
  try {
    const res = await getRouteList({ page: 1, size: 100 })
    if (res.code === 0) {
      routeOptions.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载线路选项失败:', error)
  }
}

// 加载车辆选项
const loadVehicleOptions = async () => {
  try {
    const res = await getVehicleList({ page: 1, size: 100 })
    if (res.code === 0) {
      vehicleOptions.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载车辆选项失败:', error)
  }
}

// 获取线路名称
const getRouteName = (routeId) => {
  const route = routeOptions.value.find(r => r.id === routeId)
  return route ? route.routeName : routeId
}

// 获取车牌号
const getVehiclePlateNumber = (vehicleId) => {
  const vehicle = vehicleOptions.value.find(v => v.id === vehicleId)
  return vehicle ? vehicle.plateNumber : vehicleId
}

// 获取站点名称
const getStationName = (stationId) => {
  const station = stationOptions.value.find(s => s.id === stationId)
  return station ? station.stationName : stationId
}

// 获取班次信息
const getScheduleInfo = (scheduleId) => {
  const schedule = scheduleList.value.find(s => s.id === scheduleId)
  return schedule ? schedule.departureTime : scheduleId
}

// 获取校园卡信息
const getCardInfo = (cardId) => {
  const card = cardList.value.find(c => c.id === cardId)
  return card ? card.cardNo : cardId
}

// 车辆管理
const vehicleTableData = ref([])
const vehicleSearchForm = reactive({
  plateNumber: null,
  status: null
})
const vehiclePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const vehicleDialogVisible = ref(false)
const vehicleDialogTitle = ref('新增车辆')
const vehicleForm = reactive({
  id: null,
  plateNumber: '',
  vehicleType: '',
  seatCount: null,
  status: 1
})

// 站点管理
const stationTableData = ref([])
const stationSearchForm = reactive({
  stationName: null,
  status: null
})
const stationPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const stationDialogVisible = ref(false)
const stationDialogTitle = ref('新增站点')
const stationForm = reactive({
  id: null,
  stationName: '',
  location: '',
  latitude: null,
  longitude: null,
  status: 1
})



// 时刻表管理
const scheduleTableData = ref([])
const scheduleSearchForm = reactive({
  routeId: null,
  vehicleId: null,
  status: null
})
const schedulePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const scheduleDialogVisible = ref(false)
const scheduleDialogTitle = ref('新增时刻表')
const scheduleForm = reactive({
  id: null,
  routeId: null,
  vehicleId: null,
  departureTime: null,
  frequency: '',
  startDate: null,
  endDate: null,
  status: 1
})

// 乘车记录数据
const recordTableData = ref([])
const recordSearchForm = reactive({
  cardId: null,
  routeId: null,
  vehicleId: null
})
const recordPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 班次列表
const scheduleList = ref([])

// 校园卡列表
const cardList = ref([])

// 加载线路数据
const loadRouteData = async () => {
  try {
    const res = await getRouteList({
      page: routePagination.page,
      size: routePagination.size,
      ...routeSearchForm
    })
    if (res.code === 0) {
      routeTableData.value = res.data.records || []
      routePagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载线路数据失败:', error)
  }
}

// 加载车辆数据
const loadVehicleData = async () => {
  try {
    const res = await getVehicleList({
      page: vehiclePagination.page,
      size: vehiclePagination.size,
      ...vehicleSearchForm
    })
    if (res.code === 0) {
      vehicleTableData.value = res.data.records || []
      vehiclePagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载车辆数据失败:', error)
  }
}

// 加载站点数据
const loadStationData = async () => {
  try {
    const res = await getStationList({
      page: stationPagination.page,
      size: stationPagination.size,
      ...stationSearchForm
    })
    if (res.code === 0) {
      stationTableData.value = res.data.records || []
      stationPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载站点数据失败:', error)
  }
}

// 加载时刻表数据
const loadScheduleData = async () => {
  try {
    // 先加载线路和车辆选项，确保表格中能显示线路名称和车牌号
    await Promise.all([
      loadRouteOptions(),
      loadVehicleOptions()
    ])
    
    const res = await getScheduleList({
      page: schedulePagination.page,
      size: schedulePagination.size,
      ...scheduleSearchForm
    })
    if (res.code === 0) {
      scheduleTableData.value = res.data.records || []
      schedulePagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载时刻表数据失败:', error)
  }
}

// 加载校园卡列表
const loadCardList = async () => {
  try {
    const res = await getCardList({ page: 1, size: 1000 })
    if (res.code === 0) {
      cardList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载校园卡列表失败:', error)
  }
}

// 加载乘车记录数据
const loadRecordData = async () => {
  try {
    // 先加载线路、车辆、班次和校园卡列表，确保能显示名称而不是ID
    await Promise.all([
      loadRouteOptions(),
      loadVehicleOptions(),
      loadCardList()
    ])
    
    // 加载所有线路的时刻表
    if (routeOptions.value.length > 0) {
      for (const route of routeOptions.value) {
        const res = await getScheduleList({ routeId: route.id, page: 1, size: 100 })
        if (res.code === 0) {
          const newSchedules = res.data.records || []
          scheduleList.value = [...scheduleList.value, ...newSchedules]
        }
      }
      // 去重，避免重复数据
      const uniqueSchedules = [...new Map(scheduleList.value.map(item => [item.id, item])).values()]
      scheduleList.value = uniqueSchedules
    }
    
    // 从登录信息中获取当前用户信息
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const userRole = user ? user.role : 'admin'
    
    // 构建请求参数
    const requestParams = {
      page: recordPagination.page,
      size: recordPagination.size,
      ...recordSearchForm
    }
    
    // 如果不是管理员，只获取当前用户的乘车记录
    if (userRole !== 'admin' && user && user.cardId) {
      requestParams.cardId = user.cardId
    }
    
    const res = await getCommuteList(requestParams)
    if (res.code === 0) {
      recordTableData.value = res.data.records || []
      recordPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载乘车记录数据失败:', error)
  }
}

// 线路管理方法
const handleAddRoute = async () => {
  routeDialogTitle.value = '新增线路'
  Object.assign(routeForm, {
    id: null,
    routeName: '',
    startStationId: null,
    endStationId: null,
    startStation: '',
    endStation: '',
    totalDistance: null,
    totalTime: null,
    status: 1
  })
  await loadStationOptions()
  routeDialogVisible.value = true
}

const handleEditRoute = async (row) => {
  routeDialogTitle.value = '编辑线路'
  await loadStationOptions()
  // 查找起点站和终点站的ID
  const startStation = stationOptions.value.find(s => s.stationName === row.startStation)
  const endStation = stationOptions.value.find(s => s.stationName === row.endStation)
  Object.assign(routeForm, {
    ...row,
    startStationId: startStation ? startStation.id : null,
    endStationId: endStation ? endStation.id : null
  })
  routeDialogVisible.value = true
}

const handleSaveRoute = async () => {
  try {
    // 根据站点ID获取站点名称
    const startStation = stationOptions.value.find(s => s.id === routeForm.startStationId)
    const endStation = stationOptions.value.find(s => s.id === routeForm.endStationId)
    
    if (startStation) {
      routeForm.startStation = startStation.stationName
    }
    if (endStation) {
      routeForm.endStation = endStation.stationName
    }
    
    // 计算距离和时间（这里可以根据实际情况实现）
    if (startStation && endStation) {
      // 简单计算距离（实际应该使用地图API）
      const distance = Math.sqrt(
        Math.pow(startStation.latitude - endStation.latitude, 2) + 
        Math.pow(startStation.longitude - endStation.longitude, 2)
      ) * 111 // 粗略转换为公里
      routeForm.totalDistance = parseFloat(distance.toFixed(2))
      // 假设平均速度为30公里/小时
      routeForm.totalTime = Math.round(distance / 30 * 60)
    }
    
    let res
    if (routeForm.id) {
      res = await updateRoute(routeForm)
    } else {
      res = await addRoute(routeForm)
    }
    if (res.code === 0) {
      routeDialogVisible.value = false
      loadRouteData()
    }
  } catch (error) {
    console.error('保存线路失败:', error)
  }
}

const handleDeleteRoute = async (id) => {
  try {
    // 添加确认提示
    if (!confirm('确定要删除该线路吗？删除后相关的线路站点数据也会被删除。')) {
      return
    }
    const res = await deleteRoute(id)
    if (res.code === 0) {
      loadRouteData()
    }
  } catch (error) {
    console.error('删除线路失败:', error)
  }
}

const handleRouteSearch = () => {
  routePagination.page = 1
  loadRouteData()
}

const handleRouteReset = () => {
  routeSearchForm.routeName = null
  routeSearchForm.status = null
  loadRouteData()
}

const handleRouteSizeChange = (val) => {
  routePagination.size = val
  loadRouteData()
}

const handleRouteCurrentChange = (val) => {
  routePagination.page = val
  loadRouteData()
}

// 车辆管理方法
const handleAddVehicle = () => {
  vehicleDialogTitle.value = '新增车辆'
  Object.assign(vehicleForm, {
    id: null,
    plateNumber: '',
    vehicleType: '',
    seatCount: null,
    status: 1
  })
  vehicleDialogVisible.value = true
}

const handleEditVehicle = (row) => {
  vehicleDialogTitle.value = '编辑车辆'
  Object.assign(vehicleForm, row)
  vehicleDialogVisible.value = true
}

const handleSaveVehicle = async () => {
  try {
    let res
    if (vehicleForm.id) {
      res = await updateVehicle(vehicleForm)
    } else {
      res = await addVehicle(vehicleForm)
    }
    if (res.code === 0) {
      vehicleDialogVisible.value = false
      loadVehicleData()
    }
  } catch (error) {
    console.error('保存车辆失败:', error)
  }
}

const handleDeleteVehicle = async (id) => {
  try {
    // 添加确认提示
    if (!confirm('确定要删除该车辆吗？')) {
      return
    }
    const res = await deleteVehicle(id)
    if (res.code === 0) {
      loadVehicleData()
    }
  } catch (error) {
    console.error('删除车辆失败:', error)
  }
}

const handleVehicleSearch = () => {
  vehiclePagination.page = 1
  loadVehicleData()
}

const handleVehicleReset = () => {
  vehicleSearchForm.plateNumber = null
  vehicleSearchForm.status = null
  loadVehicleData()
}

const handleVehicleSizeChange = (val) => {
  vehiclePagination.size = val
  loadVehicleData()
}

const handleVehicleCurrentChange = (val) => {
  vehiclePagination.page = val
  loadVehicleData()
}

// 站点管理方法
const handleAddStation = () => {
  stationDialogTitle.value = '新增站点'
  Object.assign(stationForm, {
    id: null,
    stationName: '',
    location: '',
    latitude: null,
    longitude: null,
    status: 1
  })
  stationDialogVisible.value = true
}

const handleEditStation = (row) => {
  stationDialogTitle.value = '编辑站点'
  Object.assign(stationForm, row)
  stationDialogVisible.value = true
}

const handleSaveStation = async () => {
  try {
    let res
    if (stationForm.id) {
      res = await updateStation(stationForm)
    } else {
      res = await addStation(stationForm)
    }
    if (res.code === 0) {
      stationDialogVisible.value = false
      loadStationData()
    }
  } catch (error) {
    console.error('保存站点失败:', error)
  }
}

const handleDeleteStation = async (id) => {
  try {
    // 添加确认提示
    if (!confirm('确定要删除该站点吗？')) {
      return
    }
    const res = await deleteStation(id)
    if (res.code === 0) {
      loadStationData()
    }
  } catch (error) {
    console.error('删除站点失败:', error)
  }
}

const handleStationSearch = () => {
  stationPagination.page = 1
  loadStationData()
}

const handleStationReset = () => {
  stationSearchForm.stationName = null
  stationSearchForm.status = null
  loadStationData()
}

const handleStationSizeChange = (val) => {
  stationPagination.size = val
  loadStationData()
}

const handleStationCurrentChange = (val) => {
  stationPagination.page = val
  loadStationData()
}

// 时刻表管理方法
const handleAddSchedule = async () => {
  scheduleDialogTitle.value = '新增时刻表'
  Object.assign(scheduleForm, {
    id: null,
    routeId: null,
    vehicleId: null,
    departureTime: null,
    frequency: '',
    startDate: null,
    endDate: null,
    status: 1
  })
  // 加载线路和车辆选项
  await Promise.all([
    loadRouteOptions(),
    loadVehicleOptions()
  ])
  scheduleDialogVisible.value = true
}

const handleEditSchedule = async (row) => {
  scheduleDialogTitle.value = '编辑时刻表'
  // 加载线路和车辆选项
  await Promise.all([
    loadRouteOptions(),
    loadVehicleOptions()
  ])
  Object.assign(scheduleForm, row)
  scheduleDialogVisible.value = true
}

const handleSaveSchedule = async () => {
  try {
    // 处理时间格式，确保只发送时间部分
    const formData = {
      ...scheduleForm
    }
    // 处理发车时间，只保留时间部分
    if (formData.departureTime) {
      const time = new Date(formData.departureTime)
      formData.departureTime = time.toTimeString().substring(0, 8)
    }
    
    let res
    if (formData.id) {
      res = await updateSchedule(formData)
    } else {
      res = await addSchedule(formData)
    }
    if (res.code === 0) {
      scheduleDialogVisible.value = false
      loadScheduleData()
    }
  } catch (error) {
    console.error('保存时刻表失败:', error)
  }
}

const handleDeleteSchedule = async (id) => {
  try {
    const res = await deleteSchedule(id)
    if (res.code === 0) {
      loadScheduleData()
    }
  } catch (error) {
    console.error('删除时刻表失败:', error)
  }
}

const handleScheduleSearch = () => {
  schedulePagination.page = 1
  loadScheduleData()
}

const handleScheduleReset = () => {
  scheduleSearchForm.routeId = null
  scheduleSearchForm.vehicleId = null
  scheduleSearchForm.status = null
  loadScheduleData()
}

const handleScheduleSizeChange = (val) => {
  schedulePagination.size = val
  loadScheduleData()
}

const handleScheduleCurrentChange = (val) => {
  schedulePagination.page = val
  loadScheduleData()
}

// 乘车记录方法
const handleRecordSearch = () => {
  recordPagination.page = 1
  loadRecordData()
}

const handleRecordReset = () => {
  recordSearchForm.cardId = null
  recordSearchForm.routeId = null
  recordSearchForm.vehicleId = null
  loadRecordData()
}

const handleRecordSizeChange = (val) => {
  recordPagination.size = val
  loadRecordData()
}

const handleRecordCurrentChange = (val) => {
  recordPagination.page = val
  loadRecordData()
}

// 标签页切换时加载对应数据
const handleTabChange = (tab) => {
  activeTab.value = tab
  switch (tab) {
    case 'route':
      loadRouteData()
      break
    case 'vehicle':
      loadVehicleData()
      break
    case 'station':
      loadStationData()
      break
    case 'schedule':
      loadScheduleData()
      break
    case 'record':
      loadRecordData()
      break
  }
}

onMounted(() => {
  loadRouteData()
})
</script>

<style scoped>
.commute-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.header-actions {
  margin-bottom: 20px;
}

.el-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
