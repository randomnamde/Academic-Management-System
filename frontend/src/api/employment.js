import request from './request'

export function getEmploymentList(params) {
  return request({
    url: '/employment',
    method: 'get',
    params
  })
}

export function getEmploymentByStudentId(studentId) {
  return request({
    url: `/employment/student/${studentId}`,
    method: 'get'
  })
}

export function createEmployment(data) {
  return request({
    url: '/employment',
    method: 'post',
    data
  })
}

export function updateEmployment(id, data) {
  return request({
    url: `/employment/${id}`,
    method: 'put',
    data
  })
}

export function deleteEmployment(id) {
  return request({
    url: `/employment/${id}`,
    method: 'delete'
  })
}

export function getStatisticsByYear(params) {
  return request({
    url: '/employment/statistics/year',
    method: 'get',
    params
  })
}

export function getStatisticsByIndustry(params) {
  return request({
    url: '/employment/statistics/industry',
    method: 'get',
    params
  })
}

export function getStatisticsByCompanyType(params) {
  return request({
    url: '/employment/statistics/company-type',
    method: 'get',
    params
  })
}

export function getEmploymentRate(params) {
  return request({
    url: '/employment/statistics/employment-rate',
    method: 'get',
    params
  })
}

// 校友接口
export function getAlumniList(params) {
  return request({
    url: '/alumni',
    method: 'get',
    params
  })
}

export function getAlumniByStudentId(studentId) {
  return request({
    url: `/alumni/student/${studentId}`,
    method: 'get'
  })
}

export function createAlumni(data) {
  return request({
    url: '/alumni',
    method: 'post',
    data
  })
}

export function updateAlumni(id, data) {
  return request({
    url: `/alumni/${id}`,
    method: 'put',
    data
  })
}

export function deleteAlumni(id) {
  return request({
    url: `/alumni/${id}`,
    method: 'delete'
  })
}
