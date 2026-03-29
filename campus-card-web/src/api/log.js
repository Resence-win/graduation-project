import request from '@/utils/request'

export const getLogList = (params) => {
  return request.get('/log/list', { params })
}
