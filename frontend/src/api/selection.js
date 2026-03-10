import request from './request'

/**
 * 提交选课申请 (学生)
 */
export function selectCourse(data) {
  return request({
    url: '/selection/select',
    method: 'post',
    data
  })
}

/**
 * 获取选课结果 (学生)
 */
export function getSelectionResult(params) {
  return request({
    url: '/selection/result',
    method: 'get',
    params
  })
}

/**
 * 预热库存 (管理员)
 */
export function preloadInventory() {
  return request({
    url: '/selection/admin/preload',
    method: 'post'
  })
}
