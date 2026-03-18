import request from './request'

export function getRewardPunishmentList(params) {
  return request({
    url: '/reward-punishment',
    method: 'get',
    params
  })
}

export function createRewardPunishment(data) {
  return request({
    url: '/reward-punishment',
    method: 'post',
    data
  })
}

export function updateRewardPunishment(id, data) {
  return request({
    url: `/reward-punishment/${id}`,
    method: 'put',
    data
  })
}

export function deleteRewardPunishment(id) {
  return request({
    url: `/reward-punishment/${id}`,
    method: 'delete'
  })
}

export function approveRewardPunishment(id) {
  return request({
    url: `/reward-punishment/${id}/approve`,
    method: 'put'
  })
}

export function rejectRewardPunishment(id, reason) {
  return request({
    url: `/reward-punishment/${id}/reject`,
    method: 'put',
    params: { reason }
  })
}
