import request from '@/utils/request'

export const getCommuteList = (params) => {
  return request.get('/commute/list', { params })
}
