import request from '@/utils/request'

export const recharge = (data) => {
  return request.post('/recharge', {
    cardId: data.card_id,
    amount: data.amount,
    rechargeType: data.recharge_type,
    operatorId: 1,
    operatorName: 'user'
  })
}

export const rechargeByCardNo = (data) => {
  return request.post('/recharge/by-card-no', data)
}

export const getRechargeList = (params) => {
  return request.get('/recharge/list', { params })
}
