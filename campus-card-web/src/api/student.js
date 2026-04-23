import request from '@/utils/request'

export const getStudentList = (params) => {
  return request.get('/student/list', { params })
}

export const getStudentDetail = (id) => {
  return request.get(`/student/${id}`)
}

export const getStudentByNo = (studentNo) => {
  return request.get(`/student/by-no/${studentNo}`)
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

export const exportStudents = () => {
  return request.get('/student/export', { responseType: 'blob' })
}

export const importStudents = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/student/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const downloadImportTemplate = () => {
  return request.get('/student/template/download', { responseType: 'blob' })
}
