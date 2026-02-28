import request from './request'

export function getCourseArrangementList(params) {
  return request({
    url: '/course-arrangement',
    method: 'get',
    params
  })
}

export function getCourseArrangementDetail(id) {
  return request({
    url: `/course-arrangement/${id}`,
    method: 'get'
  })
}

export function createCourseArrangement(data) {
  return request({
    url: '/course-arrangement',
    method: 'post',
    data
  })
}

export function updateCourseArrangement(id, data) {
  return request({
    url: `/course-arrangement/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourseArrangement(id) {
  return request({
    url: `/course-arrangement/${id}`,
    method: 'delete'
  })
}

export function getCourseArrangementOptions(params) {
  return request({
    url: '/course-arrangement/options',
    method: 'get',
    params
  })
}
