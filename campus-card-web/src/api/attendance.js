import request from '@/utils/request'

export const getAttendanceList = (params) => {
  return request.get('/attendance/list', { params })
}

export const createAttendance = (data) => {
  return request.post('/attendance/checkin', null, {
    params: data
  })
}

export const getAttendanceStatistics = (params) => {
  return request.get('/attendance/statistics', { params })
}

// 打卡位置相关接口
export const createLocation = (data) => {
  return request.post('/attendance/location', data)
}

export const updateLocation = (data) => {
  return request.put('/attendance/location', data)
}

export const deleteLocation = (id) => {
  return request.delete(`/attendance/location/${id}`)
}

export const getLocationById = (id) => {
  return request.get(`/attendance/location/${id}`)
}

export const getLocationsByTeacherId = (teacherId, params) => {
  return request.get(`/attendance/location/teacher/${teacherId}`, { params })
}

export const getAllLocations = (params) => {
  return request.get('/attendance/location/list', { params })
}

export const getActiveLocations = () => {
  return request.get('/attendance/location/active')
}

export const getAttendanceRecordsByLocationId = (locationId, params) => {
  return request.get(`/attendance/location/${locationId}/records`, { params })
}
