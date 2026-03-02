import request from '../utils/request'

// 获取画师申请列表（分页）
export function getApplications(params) {
  return request({
    url: '/api/admin/applications',
    method: 'get',
    params
  })
}

// 获取待审核申请列表
export function getPendingApplications(params) {
  return request({
    url: '/api/admin/applications/pending',
    method: 'get',
    params
  })
}

// 获取申请详情
export function getApplicationDetail(id) {
  return request({
    url: `/api/admin/applications/${id}`,
    method: 'get'
  })
}

// 审核申请（通过或拒绝）
export function reviewApplication(id, data) {
  return request({
    url: `/api/admin/applications/${id}/review`,
    method: 'post',
    data
  })
}

// 获取待审核数量
export function getPendingCount() {
  return request({
    url: '/api/admin/applications/pending/count',
    method: 'get'
  })
}
