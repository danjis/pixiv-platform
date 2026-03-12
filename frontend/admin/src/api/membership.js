import request from '../utils/request'

// 获取会员列表（管理员）
export function getMemberList(params) {
  return request({
    url: '/api/admin/membership/list',
    method: 'get',
    params
  })
}

// 获取用户会员信息
export function getUserMembership(userId) {
  return request({
    url: `/api/membership/${userId}`,
    method: 'get'
  })
}

// 升级用户会员
export function upgradeMembership(data) {
  return request({
    url: '/api/admin/membership/upgrade',
    method: 'post',
    data
  })
}
