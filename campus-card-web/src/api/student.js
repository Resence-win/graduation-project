import request from '@/utils/request'

export const getStudentList = (params) => {
  return request.get('/student/list', { params })
}

export const getStudentDetail = (id) => {
  return request.get(`/student/${id}`)
}

export const addStudent = (data) => {
  return request.post('/student', data)
}

export const updateStudent = (data) => {
  return request.put('/student', data)
}

export const deleteStudent = (id) => {
  return request.delete(`/student/${id}`)
}
