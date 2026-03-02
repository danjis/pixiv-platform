import request from '@/utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息 { username, email, password }
 */
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息 { usernameOrEmail, password }
 */
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * 刷新令牌
 * @param {string} refreshToken - 刷新令牌
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/api/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}
