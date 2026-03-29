import request from '@/utils/request'

export const recharge = (data) => {
  return request.post('/recharge', data)
}

export const getRechargeList = (params) => {
  return request.get('/recharge/list', { params })
}
