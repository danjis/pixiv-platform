import request from '@/utils/request'

/**
 * 获取画师的约稿方案列表
 * @param {Object} params - { artistId, page, size }
 */
export function getCommissionPlans(params = {}) {
  return request({
    url: '/api/commission-plans',
    method: 'get',
    params
  })
}

/**
 * 获取单个约稿方案详情
 */
export function getCommissionPlan(planId) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'get'
  })
}

/**
 * 创建约稿方案（画师）
 */
export function createCommissionPlan(data) {
  return request({
    url: '/api/commission-plans',
    method: 'post',
    data
  })
}

/**
 * 更新约稿方案（画师）
 */
export function updateCommissionPlan(planId, data) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'put',
    data
  })
}

/**
 * 删除约稿方案（画师）
 */
export function deleteCommissionPlan(planId) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'delete'
  })
}
