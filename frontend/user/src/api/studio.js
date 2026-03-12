import request from '@/utils/request'

/**
 * 获取画师仪表盘统计数据
 * 聚合调用：作品数、粉丝数、约稿统计等
 */
export async function getDashboardStats() {
  // 此接口可后端扩展，目前前端通过组合现有API实现
  return request({
    url: '/api/users/me',
    method: 'get'
  })
}

/**
 * 获取画师的作品列表
 * @param {Object} params - { page, size, keyword, sortBy }
 */
export function getMyArtworks(params = {}) {
  return request({
    url: '/api/artworks',
    method: 'get',
    params
  })
}

/**
 * 获取画师接稿列表
 * @param {Object} params - { status, page, size }
 */
export function getArtistCommissions(params = {}) {
  return request({
    url: '/api/commissions',
    method: 'get',
    params: { role: 'artist', page: 0, size: 50, ...params }
  })
}

/**
 * 更新画师设置
 * @param {Object} data - { bio, specialties, minPrice, maxPrice, commissionOpen, contactPreference, contactInfo }
 */
export function updateArtistSettings(data) {
  return request({
    url: '/api/users/me/artist-settings',
    method: 'put',
    data
  })
}

/**
 * 获取画师设置
 */
export function getArtistSettings() {
  return request({
    url: '/api/users/me/artist-settings',
    method: 'get'
  })
}

/**
 * 获取钱包概览
 * 返回：totalIncome, availableBalance, frozenAmount, withdrawnAmount, monthIncome
 */
export function getWalletOverview() {
  return request({
    url: '/api/users/me/wallet',
    method: 'get'
  })
}

/**
 * 获取钱包交易记录
 * @param {Object} params - { page, size }
 */
export function getWalletTransactions(params = {}) {
  return request({
    url: '/api/users/me/wallet/transactions',
    method: 'get',
    params: { page: 0, size: 50, ...params }
  })
}

/**
 * 提现
 * @param {Object} data - { amount, alipayAccount }
 */
export function withdrawFromWallet(data) {
  return request({
    url: '/api/users/me/wallet/withdraw',
    method: 'post',
    data
  })
}

/**
 * 获取我的提现记录
 */
export function getMyWithdrawals(params) {
  return request({
    url: '/api/users/me/wallet/withdrawals',
    method: 'get',
    params
  })
}
