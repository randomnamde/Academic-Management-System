import request from './request'

export function getBackupList(params) {
  return request({
    url: '/backup/list',
    method: 'get',
    params
  })
}

export function createBackup(data) {
  return request({
    url: '/backup',
    method: 'post',
    params: data
  })
}

export function restoreBackup(backupId) {
  return request({
    url: `/backup/restore/${backupId}`,
    method: 'post'
  })
}

export function getStrategyList() {
  return request({
    url: '/backup/strategy/list',
    method: 'get'
  })
}

export function createStrategy(data) {
  return request({
    url: '/backup/strategy',
    method: 'post',
    data
  })
}

export function updateStrategy(id, data) {
  return request({
    url: `/backup/strategy/${id}`,
    method: 'put',
    data
  })
}

export function deleteStrategy(id) {
  return request({
    url: `/backup/strategy/${id}`,
    method: 'delete'
  })
}
