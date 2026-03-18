import request from './request'

export function getTeacherList(params) {
  return request({
    url: '/teacher',
    method: 'get',
    params
  })
}

export function getTeacherDetail(id) {
  return request({
    url: `/teacher/${id}`,
    method: 'get'
  })
}

export function createTeacher(data) {
  return request({
    url: '/teacher',
    method: 'post',
    data
  })
}

export function updateTeacher(id, data) {
  return request({
    url: `/teacher/${id}`,
    method: 'put',
    data
  })
}

export function deleteTeacher(id) {
  return request({
    url: `/teacher/${id}`,
    method: 'delete'
  })
}

export function updateTeacherStatus(id, status) {
  return request({
    url: `/teacher/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 教师工作量
export function getMyWorkload(params) {
  return request({
    url: '/teacher/workload',
    method: 'get',
    params
  })
}

export function getMyWorkloadSummary(params) {
  return request({
    url: '/teacher/workload/summary',
    method: 'get',
    params
  })
}

export function getTeachingStatistics(params) {
  return request({
    url: '/teacher/workload/statistics',
    method: 'get',
    params
  })
}

export function getTeacherWorkload(teacherNo, params) {
  return request({
    url: `/teacher/workload/teacher/${teacherNo}`,
    method: 'get',
    params
  })
}
