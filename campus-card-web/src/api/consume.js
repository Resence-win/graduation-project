import request from '@/utils/request'

export const consume = (data) => {
  return request.post('/consume', data)
}

export const getConsumeList = (params) => {
  return request.get('/consume/list', { params })
}
