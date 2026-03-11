import request from './request'

export function getMajorList(params) {
  return request({
    url: '/major',
    method: 'get',
    params
  })
}

export function getMajorOptions(params) {
  return request({
    url: '/major/options',
    method: 'get',
    params
  })
}

export function getMajorDetail(majorCode) {
  return request({
    url: `/major/${majorCode}`,
    method: 'get'
  })
}

export function createMajor(data) {
  return request({
    url: '/major',
    method: 'post',
    data
  })
}

export function updateMajor(majorCode, data) {
  return request({
    url: `/major/${majorCode}`,
    method: 'put',
    data
  })
}

export function updateMajorStatus(majorCode, status) {
  return request({
    url: `/major/${majorCode}/status`,
    method: 'put',
    params: { status }
  })
}
