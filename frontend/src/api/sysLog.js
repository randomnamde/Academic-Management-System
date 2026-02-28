import request from './request'

export function getSysLogList(params) {
  return request({
    url: '/sys-log',
    method: 'get',
    params
  })
}
