import request from './request'
import { downloadWithAuth } from '@/utils/download'

export function getLeaveRequestList(params) {
  return request({
    url: '/leave-request',
    method: 'get',
    params
  })
}

export function getLeaveRequestDetail(id) {
  return request({
    url: `/leave-request/${id}`,
    method: 'get'
  })
}

export function submitLeaveRequest(data) {
  return request({
    url: '/leave-request',
    method: 'post',
    data
  })
}

export function updateLeaveRequest(id, data) {
  return request({
    url: `/leave-request/${id}`,
    method: 'put',
    data
  })
}

export function cancelLeaveRequest(id) {
  return request({
    url: `/leave-request/${id}`,
    method: 'delete'
  })
}

export function approveLeaveRequest(id, approved, remark) {
  return request({
    url: `/leave-request/${id}/approve`,
    method: 'post',
    params: { approved, remark }
  })
}

export function getStudentLeaveRequests(studentId) {
  return request({
    url: `/leave-request/student/${studentId}`,
    method: 'get'
  })
}

export function getPendingLeaveRequests() {
  return request({
    url: '/leave-request/pending',
    method: 'get'
  })
}

export function exportLeaveRequestReport(params) {
  return downloadWithAuth('/report/leave-request', params, 'leave-request.csv')
}
