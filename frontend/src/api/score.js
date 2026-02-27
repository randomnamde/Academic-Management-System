import request from './request'

export function getScoreList(params) {
  return request({
    url: '/score',
    method: 'get',
    params
  })
}

export function getScoreDetail(id) {
  return request({
    url: `/score/${id}`,
    method: 'get'
  })
}

export function createScore(data) {
  return request({
    url: '/score',
    method: 'post',
    data
  })
}

export function updateScore(id, data) {
  return request({
    url: `/score/${id}`,
    method: 'put',
    data
  })
}

export function deleteScore(id) {
  return request({
    url: `/score/${id}`,
    method: 'delete'
  })
}

export function getScoresByStudent(studentId) {
  return request({
    url: `/score/student/${studentId}`,
    method: 'get'
  })
}

export function getScoresByCourse(courseArrangementId) {
  return request({
    url: `/score/course/${courseArrangementId}`,
    method: 'get'
  })
}

export function getStudentStatistics(studentId) {
  return request({
    url: `/score/statistics/${studentId}`,
    method: 'get'
  })
}

export function getScoreDistribution(courseArrangementId) {
  return request({
    url: `/score/distribution/${courseArrangementId}`,
    method: 'get'
  })
}

export function getClassRank(params) {
  return request({
    url: '/score/rank',
    method: 'get',
    params
  })
}

export function batchCreateScores(data) {
  return request({
    url: '/score/batch',
    method: 'post',
    data
  })
}
