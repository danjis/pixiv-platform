import request from '../utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/admin/auth/login',
    method: 'post',
    data
  })
}

/**
 * 管理员登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/admin/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前管理员信息
 * @returns {Promise}
 */
export function getCurrentAdmin() {
  return request({
    url: '/api/admin/me',
    method: 'get'
  })
}
