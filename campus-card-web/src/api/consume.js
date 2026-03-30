import request from '@/utils/request'

export const consume = (data) => {
  return request.post('/consume', data)
}

export const consumeByCardNo = (data) => {
  return request.post('/consume/by-card-no', data)
}

export const getConsumeList = (params) => {
  return request.get('/consume/list', { params })
}
