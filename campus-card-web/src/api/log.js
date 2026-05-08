import request from '@/utils/request'

export const getLogList = (params) => {
  return request.get('/log/list', { params })
}

export const exportLogs = (params) => {
  return request.get('/log/export', {
    params,
    responseType: 'blob'
  })
}
