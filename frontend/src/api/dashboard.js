import request from './request'

export function getDashboardOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}
