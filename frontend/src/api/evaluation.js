import request from './request'

export function getEvaluationList(params) {
  return request({
    url: '/evaluation',
    method: 'get',
    params
  })
}

export function createEvaluation(data) {
  return request({
    url: '/evaluation',
    method: 'post',
    data
  })
}

export function updateEvaluation(id, data) {
  return request({
    url: `/evaluation/${id}`,
    method: 'put',
    data
  })
}

export function deleteEvaluation(id) {
  return request({
    url: `/evaluation/${id}`,
    method: 'delete'
  })
}

export function getTeacherStatistics(teacherNo, semester) {
  return request({
    url: `/evaluation/teacher/${teacherNo}/statistics`,
    method: 'get',
    params: { semester }
  })
}

export function getCourseStatistics(courseArrangementId) {
  return request({
    url: `/evaluation/course/${courseArrangementId}/statistics`,
    method: 'get'
  })
}

export function publishEvaluation(id) {
  return request({
    url: `/evaluation/${id}/publish`,
    method: 'put'
  })
}
