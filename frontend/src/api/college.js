import request from './request'

export function getCollegeList(params) {
  return request({
    url: '/college',
    method: 'get',
    params
  })
}

export function getCollegeDetail(id) {
  return request({
    url: `/college/${id}`,
    method: 'get'
  })
}

export function createCollege(data) {
  return request({
    url: '/college',
    method: 'post',
    data
  })
}

export function updateCollege(id, data) {
  return request({
    url: `/college/${id}`,
    method: 'put',
    data
  })
}

export function updateCollegeStatus(id, status) {
  return request({
    url: `/college/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function bindCollegeAdmin(id, adminUserId) {
  return request({
    url: `/college/${id}/admin`,
    method: 'put',
    params: { adminUserId }
  })
}

