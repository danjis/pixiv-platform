import request from '@/utils/request'

/**
 * 获取可领取的优惠券列表
 */
export function getAvailableCoupons(params = {}) {
  return request({
    url: '/api/coupons/available',
    method: 'get',
    params: { page: 0, size: 20, ...params }
  })
}

/**
 * 领取优惠券
 * @param {number} couponId - 优惠券 ID
 */
export function claimCoupon(couponId) {
  return request({
    url: `/api/coupons/${couponId}/claim`,
    method: 'post'
  })
}

/**
 * 通过优惠码领取
 * @param {string} code - 优惠券代码
 */
export function claimByCode(code) {
  return request({
    url: '/api/coupons/claim-by-code',
    method: 'post',
    data: { code }
  })
}

/**
 * 获取我的优惠券
 * @param {Object} params - { status, page, size }
 */
export function getMyCoupons(params = {}) {
  return request({
    url: '/api/coupons/mine',
    method: 'get',
    params: { page: 0, size: 20, ...params }
  })
}

/**
 * 获取可用于订单的优惠券
 * @param {number} orderAmount - 订单金额
 */
export function getAvailableForOrder(orderAmount) {
  return request({
    url: '/api/coupons/available-for-order',
    method: 'get',
    params: { orderAmount }
  })
}
