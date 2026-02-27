import request from './request'

export function getCourseList(params) {
  return request({
    url: '/course',
    method: 'get',
    params
  })
}

export function getCourseCategoryStatistics() {
  return request({
    url: '/course/statistics/category',
    method: 'get'
  })
}

export function getCourseDetail(id) {
  return request({
    url: `/course/${id}`,
    method: 'get'
  })
}

export function createCourse(data) {
  return request({
    url: '/course',
    method: 'post',
    data
  })
}

export function updateCourse(id, data) {
  return request({
    url: `/course/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}

export function updateCourseStatus(id, status) {
  return request({
    url: `/course/${id}/status`,
    method: 'put',
    params: { status }
  })
}
