import request from './request'

export function getStudentList(params) {
  return request({
    url: '/student',
    method: 'get',
    params
  })
}

export function getStudentDetail(id) {
  return request({
    url: `/student/${id}`,
    method: 'get'
  })
}

export function createStudent(data) {
  return request({
    url: '/student',
    method: 'post',
    data
  })
}

export function updateStudent(id, data) {
  return request({
    url: `/student/${id}`,
    method: 'put',
    data
  })
}

export function deleteStudent(id) {
  return request({
    url: `/student/${id}`,
    method: 'delete'
  })
}

export function updateStudentStatus(id, status) {
  return request({
    url: `/student/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getStudentsByClassId(classId) {
  return request({
    url: '/student/class/' + classId,
    method: 'get'
  })
}

export function getStudentGenderStatistics() {
  return request({
    url: '/student/statistics/gender',
    method: 'get'
  })
}
