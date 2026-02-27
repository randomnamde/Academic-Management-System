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
