import request from './request'

export function getClassList(params) {
  return request({
    url: '/class',
    method: 'get',
    params
  })
}

export function getClassDetail(id) {
  return request({
    url: `/class/${id}`,
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

export function updateClass(id, data) {
  return request({
    url: `/class/${id}`,
    method: 'put',
    data
  })
}

export function deleteClass(id) {
  return request({
    url: `/class/${id}`,
    method: 'delete'
  })
}

export function getClassesByTeacher(teacherId) {
  return request({
    url: `/class/teacher/${teacherId}`,
    method: 'get'
  })
}
