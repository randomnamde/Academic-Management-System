import request from './request'

export function getCurrentSemester() {
  return request({
    url: '/system/config/current-semester',
    method: 'get'
  })
}

export function updateCurrentSemester(currentSemester) {
  return request({
    url: '/system/config/current-semester',
    method: 'put',
    params: { currentSemester }
  })
}

export function getCourseTimeSlots() {
  return request({
    url: '/system/config/course-time-slots',
    method: 'get'
  })
}

export function updateCourseTimeSlots(timeSlots) {
  return request({
    url: '/system/config/course-time-slots',
    method: 'put',
    data: { timeSlots }
  })
}
