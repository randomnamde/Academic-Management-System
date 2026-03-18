import request from './request'

export function getExamList(params) {
  return request({
    url: '/exam',
    method: 'get',
    params
  })
}

export function createExam(data) {
  return request({
    url: '/exam',
    method: 'post',
    data
  })
}

export function updateExam(id, data) {
  return request({
    url: `/exam/${id}`,
    method: 'put',
    data
  })
}

export function deleteExam(id) {
  return request({
    url: `/exam/${id}`,
    method: 'delete'
  })
}

export function updateExamStatus(id, status) {
  return request({
    url: `/exam/${id}/status`,
    method: 'put',
    params: { status }
  })
}
