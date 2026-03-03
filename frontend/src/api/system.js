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

