import request from '@/utils/request'

export const getAccount = (cardNo) => {
  return request.get(`/account/by-card-no/${cardNo}`)
}

export const getBalance = (cardId) => {
  return request.get(`/account/balance/${cardId}`)
}

export const getAccountFlow = (params) => {
  return request.get('/account/flow', { params })
}
