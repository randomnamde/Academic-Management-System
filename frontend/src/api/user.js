import request from './request'
import { downloadWithAuth } from '@/utils/download'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getRuntimeMetrics() {
  return request({
    url: '/auth/runtime-metrics',
    method: 'get',
    silent: true
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

export function resetAllPasswords(operatorPassword) {
  return request({
    url: '/user/password/reset-all',
    method: 'put',
    data: { operatorPassword }
  })
}

export function resetUserPassword(id, operatorPassword) {
  return request({
    url: `/user/${id}/password/reset`,
    method: 'put',
    data: { operatorPassword }
  })
}

export function resetBatchUserPasswords(userIds, operatorPassword) {
  return request({
    url: '/user/password/reset-batch',
    method: 'put',
    data: { userIds, operatorPassword }
  })
}

export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData
  })
}

export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export function updateUserRoles(id, roleCodes) {
  return request({
    url: `/user/${id}/roles`,
    method: 'put',
    data: { roleCodes }
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function downloadUserImportTemplate(roleType, fileType = 'xlsx') {
  const targetType = fileType === 'csv' ? 'csv' : 'xlsx'
  return downloadWithAuth(
    '/user/import/template',
    { roleType, fileType: targetType },
    `user-import-template.${targetType}`
  )
}

export function importUsers(roleType, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/import',
    method: 'post',
    params: { roleType },
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
