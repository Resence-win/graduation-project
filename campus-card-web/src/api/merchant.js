import request from '@/utils/request'

export const getMerchantTypeList = () => {
  return request.get('/merchant/type/list')
}

export const addMerchantType = (data) => {
  return request.post('/merchant/type', data)
}

export const deleteMerchantType = (id) => {
  return request.delete(`/merchant/type/${id}`)
}

export const getMerchantList = (params) => {
  return request.get('/merchant/list', { params })
}

export const addMerchant = (data) => {
  return request.post('/merchant', data)
}

export const updateMerchant = (data) => {
  return request.put('/merchant', data)
}

export const deleteMerchant = (id) => {
  return request.delete(`/merchant/${id}`)
}

export const uploadMerchantLogo = (formData) => {
  return request.post('/merchant/upload-logo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
