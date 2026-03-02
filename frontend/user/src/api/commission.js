import request from '@/utils/request'

/**
 * 发起约稿请求（用户向画师发起）
 * @param {Object} data - { targetUserId, conversationId?, title, description, budget?, deadline?, referenceUrls?, commissionPlanId? }
 */
export function createCommission(data) {
  return request({
    url: '/api/commissions',
    method: 'post',
    data
  })
}

/**
 * 获取约稿详情
 */
export function getCommission(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}`,
    method: 'get'
  })
}

/**
 * 获取我的约稿列表
 * @param {Object} params - { role: 'client'|'artist', status?, page, size }
 */
export function getMyCommissions(params = {}) {
  return request({
    url: '/api/commissions',
    method: 'get',
    params: { role: 'client', page: 0, size: 10, ...params }
  })
}

/**
 * 画师报价 (PENDING → QUOTED)
 * @param {number} commissionId
 * @param {Object} data - { totalAmount, depositRatio, deadline?, revisionsAllowed?, quoteNote? }
 */
export function quoteCommission(commissionId, data) {
  return request({
    url: `/api/commissions/${commissionId}/quote`,
    method: 'put',
    data
  })
}

/**
 * 接受方案（兼容接口，实际支付走 payment API）
 */
export function acceptCommission(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}/accept`,
    method: 'put'
  })
}

/**
 * 拒绝方案 (PENDING → REJECTED)
 */
export function rejectCommission(commissionId, reason) {
  return request({
    url: `/api/commissions/${commissionId}/reject`,
    method: 'put',
    data: { reason }
  })
}

/**
 * 开始创作 (DEPOSIT_PAID → IN_PROGRESS)
 */
export function startWork(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}/start`,
    method: 'put'
  })
}

/**
 * 交付作品 (IN_PROGRESS → DELIVERED)
 */
export function deliverWork(commissionId, data) {
  return request({
    url: `/api/commissions/${commissionId}/deliver`,
    method: 'put',
    data
  })
}

/**
 * 确认收货 (DELIVERED → COMPLETED)
 */
export function confirmDelivery(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}/confirm`,
    method: 'put'
  })
}

/**
 * 请求修改 (DELIVERED → IN_PROGRESS)
 */
export function requestRevision(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}/revision`,
    method: 'put'
  })
}

/**
 * 取消约稿 → CANCELLED
 */
export function cancelCommission(commissionId, reason) {
  return request({
    url: `/api/commissions/${commissionId}/cancel`,
    method: 'put',
    data: { reason }
  })
}

/**
 * 获取约稿内消息列表
 */
export function getCommissionMessages(commissionId) {
  return request({
    url: `/api/commissions/${commissionId}/messages`,
    method: 'get'
  })
}

/**
 * 发送约稿内消息
 */
export function sendCommissionMessage(commissionId, data) {
  return request({
    url: `/api/commissions/${commissionId}/messages`,
    method: 'post',
    data
  })
}
