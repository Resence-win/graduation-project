import request from '@/utils/request'

// 门禁点管理
export const getAccessPoints = (params) => {
  return request.get('/access/point/list', { params })
}

export const addAccessPoint = (data) => {
  return request.post('/access/point', data)
}

export const updateAccessPoint = (data) => {
  return request.put('/access/point', data)
}

export const deleteAccessPoint = (id) => {
  return request.delete(`/access/point/${id}`)
}

// 门禁记录管理
export const getAccessList = (params) => {
  return request.get('/access/list', { params })
}

export const getMyAccessRecords = (params) => {
  return request.get('/access/my', { params })
}

export const createQRAccess = (params) => {
  return request.post('/access/qr', {}, { params })
}

export const getAccessStatistics = (params) => {
  return request.get('/access/stat', { params })
}

export const exportAccessRecords = (params) => {
  return request.get('/access/export', { 
    params, 
    responseType: 'blob' 
  })
}
