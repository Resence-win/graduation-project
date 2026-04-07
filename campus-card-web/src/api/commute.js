import request from '@/utils/request'

// 通勤车记录
export const getCommuteList = (params) => {
  return request.get('/commute/list', { params })
}

// 线路管理
export const getRouteList = (params) => {
  return request.get('/commute/route/list', { params })
}

export const addRoute = (data) => {
  return request.post('/commute/route', data)
}

export const getRouteDetail = (id) => {
  return request.get(`/commute/route/${id}`)
}

export const updateRoute = (data) => {
  return request.put('/commute/route', data)
}

export const deleteRoute = (id) => {
  return request.delete(`/commute/route/${id}`)
}

export const getAllRoutes = () => {
  return request.get('/commute/route/all')
}

// 车辆管理
export const getVehicleList = (params) => {
  return request.get('/commute/vehicle/list', { params })
}

export const addVehicle = (data) => {
  return request.post('/commute/vehicle', data)
}

export const getVehicleDetail = (id) => {
  return request.get(`/commute/vehicle/${id}`)
}

export const updateVehicle = (data) => {
  return request.put('/commute/vehicle', data)
}

export const deleteVehicle = (id) => {
  return request.delete(`/commute/vehicle/${id}`)
}

export const getAllVehicles = () => {
  return request.get('/commute/vehicle/all')
}

// 站点管理
export const getStationList = (params) => {
  return request.get('/commute/station/list', { params })
}

export const addStation = (data) => {
  return request.post('/commute/station', data)
}

export const getStationDetail = (id) => {
  return request.get(`/commute/station/${id}`)
}

export const updateStation = (data) => {
  return request.put('/commute/station', data)
}

export const deleteStation = (id) => {
  return request.delete(`/commute/station/${id}`)
}

// 时刻表管理
export const getScheduleList = (params) => {
  return request.get('/commute/schedule/list', { params })
}

export const addSchedule = (data) => {
  return request.post('/commute/schedule', data)
}

export const getScheduleDetail = (id) => {
  return request.get(`/commute/schedule/${id}`)
}

export const updateSchedule = (data) => {
  return request.put('/commute/schedule', data)
}

export const deleteSchedule = (id) => {
  return request.delete(`/commute/schedule/${id}`)
}

export const getAllSchedules = () => {
  return request.get('/commute/schedule/all')
}

export const getSchedulesByRouteId = (routeId) => {
  return request.get(`/commute/schedule/route/${routeId}`)
}

// 添加乘车记录
export const addCommuteRecord = (data) => {
  return request.post('/commute/record', data)
}
