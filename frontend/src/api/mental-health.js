import request from './request'

export function getRecordList(params) {
  return request({
    url: '/mental-health/record/list',
    method: 'get',
    params
  })
}

export function getStudentRecords(studentNo) {
  return request({
    url: `/mental-health/record/student/${studentNo}`,
    method: 'get'
  })
}

export function addRecord(data) {
  return request({
    url: '/mental-health/record',
    method: 'post',
    data
  })
}

export function updateRecord(id, data) {
  return request({
    url: `/mental-health/record/${id}`,
    method: 'put',
    data
  })
}

export function getInterviewList(params) {
  return request({
    url: '/mental-health/interview/list',
    method: 'get',
    params
  })
}

export function addInterview(data) {
  return request({
    url: '/mental-health/interview',
    method: 'post',
    data
  })
}

export function getCrisisList(params) {
  return request({
    url: '/mental-health/crisis/list',
    method: 'get',
    params
  })
}

export function addCrisis(data) {
  return request({
    url: '/mental-health/crisis',
    method: 'post',
    data
  })
}

export function updateCrisis(id, data) {
  return request({
    url: `/mental-health/crisis/${id}`,
    method: 'put',
    data
  })
}

export function getRiskStudents(params) {
  return request({
    url: '/mental-health/risk-students',
    method: 'get',
    params
  })
}
