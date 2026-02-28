import request from './request'

export function getAnalyticsOverview(params) {
  return request({
    url: '/analytics/overview',
    method: 'get',
    params
  })
}

export function getAttendanceTrend(params) {
  return request({
    url: '/analytics/attendance-trend',
    method: 'get',
    params
  })
}

export function getScoreTrend(params) {
  return request({
    url: '/analytics/score-trend',
    method: 'get',
    params
  })
}

export function getRiskStudents(params) {
  return request({
    url: '/analytics/risk-students',
    method: 'get',
    params
  })
}

