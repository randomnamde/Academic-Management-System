import request from './request'

export function getSemesterList(params) {
  return request({
    url: '/semester',
    method: 'get',
    params
  })
}

export function createSemester(data) {
  return request({
    url: '/semester',
    method: 'post',
    data
  })
}

export function updateSemester(id, data) {
  return request({
    url: `/semester/${id}`,
    method: 'put',
    data
  })
}

export function updateSemesterStatus(id, status) {
  return request({
    url: `/semester/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function getSemesterOptions() {
  return request({
    url: '/semester/options',
    method: 'get'
  })
}
