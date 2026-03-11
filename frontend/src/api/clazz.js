import request from './request'

export function getClassList(params) {
  return request({
    url: '/class',
    method: 'get',
    params
  })
}

export function getClassDetail(classCode) {
  return request({
    url: `/class/${classCode}`,
    method: 'get'
  })
}

export function createClass(data) {
  return request({
    url: '/class',
    method: 'post',
    data
  })
}

export function updateClass(classCode, data) {
  return request({
    url: `/class/${classCode}`,
    method: 'put',
    data
  })
}

export function deleteClass(classCode) {
  return request({
    url: `/class/${classCode}`,
    method: 'delete'
  })
}

export function getClassesByTeacher(teacherId) {
  return request({
    url: `/class/teacher/${teacherId}`,
    method: 'get'
  })
}
