import request from './request'

export function getStudentList(params) {
  return request({
    url: '/student',
    method: 'get',
    params
  })
}

export function getStudentDetail(studentNo) {
  return request({
    url: `/student/${studentNo}`,
    method: 'get'
  })
}

export function createStudent(data) {
  return request({
    url: '/student',
    method: 'post',
    data
  })
}

export function updateStudent(studentNo, data) {
  return request({
    url: `/student/${studentNo}`,
    method: 'put',
    data
  })
}

export function deleteStudent(studentNo) {
  return request({
    url: `/student/${studentNo}`,
    method: 'delete'
  })
}

export function updateStudentStatus(studentNo, status) {
  return request({
    url: `/student/${studentNo}/status`,
    method: 'put',
    params: { status }
  })
}

export function getStudentsByClassId(classId) {
  return request({
    url: '/student/class/' + classId,
    method: 'get'
  })
}

export function getStudentGenderStatistics() {
  return request({
    url: '/student/statistics/gender',
    method: 'get'
  })
}

export function getNextStudentNo(params) {
  return request({
    url: '/student/next-no',
    method: 'get',
    params
  })
}
