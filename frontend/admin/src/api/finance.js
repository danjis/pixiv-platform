import request from '../utils/request'

/**
 * 获取财务概览数据
 */
export function getFinanceOverview() {
  return request({
    url: '/api/payments/admin/finance/overview',
    method: 'get'
  })
}

/**
 * 获取收入趋势
 * @param {Object} params - { period: 'day'|'week'|'month', days: number }
 */
export function getFinanceTrend(params) {
  return request({
    url: '/api/payments/admin/finance/trend',
    method: 'get',
    params
  })
}

/**
 * 获取最近交易明细
 * @param {number} size - 条数
 */
export function getRecentTransactions(size = 20) {
  return request({
    url: '/api/payments/admin/finance/recent',
    method: 'get',
    params: { size }
  })
}

// =========== 提现管理 ===========

/**
 * 获取提现申请列表
 */
export function getWithdrawals(params) {
  return request({
    url: '/api/users/admin/withdrawals',
    method: 'get',
    params
  })
}

/**
 * 审批通过提现
 */
export function approveWithdrawal(id) {
  return request({
    url: `/api/users/admin/withdrawals/${id}/approve`,
    method: 'put'
  })
}

/**
 * 拒绝提现
 */
export function rejectWithdrawal(id, remark) {
  return request({
    url: `/api/users/admin/withdrawals/${id}/reject`,
    method: 'put',
    data: { remark }
  })
}

/**
 * 获取待审批提现数量
 */
export function getPendingWithdrawalCount() {
  return request({
    url: '/api/users/admin/withdrawals/pending-count',
    method: 'get'
  })
}
