import request from '@/utils/request'

export const getProductList = (params) => {
  return request.get('/product/list', { params })
}

export const addProduct = (data) => {
  return request.post('/product', data)
}

export const updateProduct = (data) => {
  return request.put('/product', data)
}

export const deleteProduct = (id) => {
  return request.delete(`/product/${id}`)
}

export const uploadProductImage = (formData) => {
  return request.post('/product/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const orderProduct = (data) => {
  return request.post('/consume/product-order', data)
}
