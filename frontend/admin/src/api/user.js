import request from '../utils/request'

// 获取用户列表（分页、可搜索、可按角色筛选）
export function getUsers(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(userId) {
  return request({
    url: `/api/admin/users/${userId}`,
    method: 'get'
  })
}
