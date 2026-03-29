import request from '@/utils/request'

export const getAttendanceList = (params) => {
  return request.get('/attendance/list', { params })
}
