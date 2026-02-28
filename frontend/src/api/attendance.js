import request from './request'
import { downloadWithAuth } from '@/utils/download'

export function getAttendanceList(params) {
  return request({
    url: '/attendance',
    method: 'get',
    params
  })
}

export function getAttendanceDetail(id) {
  return request({
    url: `/attendance/${id}`,
    method: 'get'
  })
}

export function recordAttendance(data) {
  return request({
    url: '/attendance',
    method: 'post',
    data
  })
}

export function updateAttendance(id, data) {
  return request({
    url: `/attendance/${id}`,
    method: 'put',
    data
  })
}

export function deleteAttendance(id) {
  return request({
    url: `/attendance/${id}`,
    method: 'delete'
  })
}

export function getStudentAttendance(studentId, params) {
  return request({
    url: `/attendance/student/${studentId}`,
    method: 'get',
    params
  })
}

export function getAttendanceStatistics(studentId, params) {
  return request({
    url: `/attendance/statistics/${studentId}`,
    method: 'get',
    params
  })
}

export function batchRecordAttendance(data) {
  return request({
    url: '/attendance/batch',
    method: 'post',
    data
  })
}

export function checkIn(courseArrangementId) {
  return request({
    url: '/attendance/check-in',
    method: 'post',
    params: { courseArrangementId }
  })
}

export function checkOut(courseArrangementId) {
  return request({
    url: '/attendance/check-out',
    method: 'post',
    params: { courseArrangementId }
  })
}

export function exportAttendanceReport(params) {
  return downloadWithAuth('/report/attendance', params, 'attendance.csv')
}
