import request from '@/utils/request'

/**
 * 获取当前用户会员信息
 */
export function getMyMembership() {
  return request({
    url: '/api/membership',
    method: 'get'
  })
}

/**
 * 获取指定用户会员信息（公开接口，用于VIP徽章显示）
 */
export function getUserMembership(userId) {
  return request({
    url: `/api/membership/${userId}`,
    method: 'get'
  })
}

/**
 * 获取签到状态
 */
export function getCheckinStatus() {
  return request({
    url: '/api/checkin/status',
    method: 'get'
  })
}

/**
 * 执行签到
 */
export function doCheckin() {
  return request({
    url: '/api/checkin',
    method: 'post'
  })
}

/**
 * 获取积分信息
 */
export function getPoints() {
  return request({
    url: '/api/checkin/points',
    method: 'get'
  })
}

/**
 * 获取积分记录
 */
export function getPointRecords(page = 1, size = 20) {
  return request({
    url: '/api/checkin/points/records',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 创建会员支付订单（返回支付宝表单HTML）
 */
export function createMembershipPayment(data) {
  return request({
    url: '/api/payments/membership',
    method: 'post',
    data
  })
}

/**
 * 查询会员支付状态
 */
export function getMembershipPaymentStatus(orderNo) {
  return request({
    url: '/api/payments/membership/status',
    method: 'get',
    params: { orderNo }
  })
}

/**
 * 获取积分商城物品列表
 */
export function getPointsShop() {
  return request({
    url: '/api/checkin/points/shop',
    method: 'get'
  })
}

/**
 * 积分兑换
 */
export function exchangePoints(itemId) {
  return request({
    url: '/api/checkin/points/exchange',
    method: 'post',
    data: { itemId }
  })
}
