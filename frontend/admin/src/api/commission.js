import request from '../utils/request'

/**
 * 获取所有约稿列表（管理员）
 */
export function getCommissions(params) {
  return request({
    url: '/api/payments/admin/commissions',
    method: 'get',
    params
  })
}

/**
 * 获取所有支付记录（管理员）
 */
export function getPayments(params) {
  return request({
    url: '/api/payments/admin/list',
    method: 'get',
    params
  })
}

/**
 * 管理员退款 - 按单笔支付退款
 * @param {Object} data - { paymentId, reason } 或 { commissionId, reason, refundDeposit }
 */
export function adminRefund(data) {
  return request({
    url: '/api/payments/admin/refund',
    method: 'post',
    data
  })
}

/**
 * 管理员取消约稿
 * @param {number} commissionId
 * @param {Object} data - { reason, refundDeposit }
 */
export function adminCancelCommission(commissionId, data) {
  return request({
    url: `/api/commissions/admin/${commissionId}/cancel`,
    method: 'put',
    data
  })
}
