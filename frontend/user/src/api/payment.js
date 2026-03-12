import request from '@/utils/request'

/**
 * 创建支付订单（返回支付宝支付表单 HTML）
 * @param {Object} data - { commissionId, paymentType: 'DEPOSIT' | 'FINAL_PAYMENT' }
 */
export function createPayment(data) {
  return request({
    url: '/api/payments/create',
    method: 'post',
    data
  })
}

/**
 * 查询支付订单状态
 * @param {string} orderNo - 订单号
 */
export function getPaymentStatus(orderNo) {
  return request({
    url: '/api/payments/status',
    method: 'get',
    params: { orderNo }
  })
}

/**
 * 获取约稿的支付记录
 * @param {number} commissionId
 */
export function getCommissionPayments(commissionId) {
  return request({
    url: `/api/payments/commission/${commissionId}`,
    method: 'get'
  })
}

/**
 * 继续支付已有的待支付订单
 * @param {string} orderNo - 订单号
 */
export function continuePay(orderNo) {
  return request({
    url: '/api/payments/continue',
    method: 'post',
    data: { orderNo }
  })
}

/**
 * 获取我的支付订单列表
 * @param {Object} params - { page, size, status?, paymentType? }
 */
export function getMyOrders(params) {
  return request({
    url: '/api/payments/my',
    method: 'get',
    params
  })
}

/**
 * 取消待支付订单
 * @param {string} orderNo
 */
export function cancelOrder(orderNo) {
  return request({
    url: '/api/payments/cancel',
    method: 'post',
    data: { orderNo }
  })
}

/**
 * 删除已关闭/已退款的订单
 * @param {string} orderNo
 */
export function deleteOrder(orderNo) {
  return request({
    url: `/api/payments/${orderNo}`,
    method: 'delete'
  })
}

/**
 * 获取可用于指定订单金额的优惠券
 * @param {number} orderAmount - 订单金额
 */
export function getAvailableCouponsForOrder(orderAmount) {
  return request({
    url: '/api/coupons/available-for-order',
    method: 'get',
    params: { orderAmount }
  })
}

// ==================== 约稿方案 API ====================

/**
 * 获取画师的约稿方案列表（公开）
 */
export function getArtistPlans(artistId) {
  return request({
    url: `/api/commission-plans/artist/${artistId}`,
    method: 'get'
  })
}

/**
 * 获取方案详情
 */
export function getPlan(planId) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'get'
  })
}

/**
 * 获取我的约稿方案列表（画师）
 */
export function getMyPlans() {
  return request({
    url: '/api/commission-plans/mine',
    method: 'get'
  })
}

/**
 * 创建约稿方案（画师）
 */
export function createPlan(data) {
  return request({
    url: '/api/commission-plans',
    method: 'post',
    data
  })
}

/**
 * 更新约稿方案
 */
export function updatePlan(planId, data) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'put',
    data
  })
}

/**
 * 启用/停用约稿方案
 */
export function togglePlan(planId) {
  return request({
    url: `/api/commission-plans/${planId}/toggle`,
    method: 'put'
  })
}

/**
 * 删除约稿方案
 */
export function deletePlan(planId) {
  return request({
    url: `/api/commission-plans/${planId}`,
    method: 'delete'
  })
}
