import request from '@/utils/request'

export const getAccessList = (params) => {
  return request.get('/access/list', { params })
}
