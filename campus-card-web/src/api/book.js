import request from '@/utils/request'

export const getBookList = (params) => {
  return request.get('/book/list', { params })
}

export const getBookDetail = (id) => {
  return request.get(`/book/${id}`)
}

export const addBook = (data) => {
  return request.post('/book', data)
}

export const updateBook = (data) => {
  return request.put('/book', data)
}

export const deleteBook = (id) => {
  return request.delete(`/book/${id}`)
}

export const uploadBookLogo = (formData) => {
  return request.post('/book/upload-logo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const borrowBook = (data) => {
  return request.post('/borrow', data)
}

export const returnBook = (params) => {
  return request.post('/borrow/return', params)
}

export const getBorrowList = (params) => {
  return request.get('/borrow/list', { params })
}
