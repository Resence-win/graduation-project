import request from '@/utils/request'

export const getCardList = (params) => {
  return request.get('/card/list', { params })
}

export const openCard = (data) => {
  return request.post('/card/open', data)
}

export const getCardInfo = (cardId) => {
  return request.get(`/card/${cardId}`)
}

export const getCardByUserNo = (userNo, userType) => {
  return request.get(`/card/by-user-no/${userNo}/${userType}`)
}

export const getCardByCardNo = (cardNo) => {
  return request.get(`/card/by-card-no/${cardNo}`)
}

export const lossCard = (data) => {
  return request.post('/card/loss', data)
}

export const unlossCard = (data) => {
  return request.post('/card/unloss', data)
}

export const cancelCard = (data) => {
  return request.post('/card/cancel', data)
}
