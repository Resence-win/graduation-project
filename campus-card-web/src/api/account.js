import request from '@/utils/request'

export const getAccount = (cardId) => {
  return request.get(`/account/${cardId}`)
}

export const getBalance = (cardId) => {
  return request.get(`/account/balance/${cardId}`)
}

export const getAccountFlow = (params) => {
  return request.get('/account/flow', { params })
}
