import request from '../utils/request'

/**
 * 创建优惠券
 */
export function createCoupon(data) {
  return request({
    url: '/api/coupons/admin/create',
    method: 'post',
    data
  })
}

/**
 * 获取所有优惠券列表（管理员）
 */
export function getCouponList() {
  return request({
    url: '/api/coupons/admin/list',
    method: 'get'
  })
}

/**
 * 切换优惠券状态
 */
export function toggleCouponStatus(couponId) {
  return request({
    url: `/api/coupons/admin/${couponId}/toggle`,
    method: 'put'
  })
}
