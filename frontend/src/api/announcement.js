import request from './request'

export function getAnnouncementList(params) {
  return request({
    url: '/announcement',
    method: 'get',
    params
  })
}

export function getAnnouncementDetail(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

export function createAnnouncement(data) {
  return request({
    url: '/announcement',
    method: 'post',
    data
  })
}

export function updateAnnouncement(id, data) {
  return request({
    url: `/announcement/${id}`,
    method: 'put',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  })
}

export function updateAnnouncementStatus(id, status) {
  return request({
    url: `/announcement/${id}/status`,
    method: 'put',
    params: { status }
  })
}
