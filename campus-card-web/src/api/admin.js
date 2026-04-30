import request from '@/utils/request'

export const login = (data) => {
  return request.post('/admin/login', data)
}

export const changePassword = (data) => {
  return request.post('/admin/changePassword', data)
}

export const resetPassword = (data) => {
  return request.post('/admin/resetPassword', data)
}
