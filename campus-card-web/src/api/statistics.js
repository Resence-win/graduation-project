import request from '@/utils/request'
import dayjs from 'dayjs'

export const getConsumeStat = (params) => {
  return request.get('/stat/consume', { params })
}

export const getUserRank = (params = {}) => {
  const defaultParams = {
    start_date: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    end_date: dayjs().format('YYYY-MM-DD')
  }
  return request.get('/stat/user-rank', { params: { ...defaultParams, ...params } })
}

export const getMerchantRank = (params = {}) => {
  const defaultParams = {
    start_date: dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    end_date: dayjs().format('YYYY-MM-DD')
  }
  return request.get('/stat/merchant-rank', { params: { ...defaultParams, ...params } })
}
