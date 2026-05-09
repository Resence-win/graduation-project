import request from '@/utils/request'

export const getCollegeMajorList = (params) => {
  return request.get('/college-major/list', { params })
}

export const getActiveCollegeMajors = () => {
  return request.get('/college-major/active')
}

export const getCollegeMajorOptions = () => {
  return request.get('/college-major/options')
}

export const addCollegeMajor = (data) => {
  return request.post('/college-major', data)
}

export const updateCollegeMajor = (data) => {
  return request.put('/college-major', data)
}

export const deleteCollegeMajor = (id) => {
  return request.delete(`/college-major/${id}`)
}
