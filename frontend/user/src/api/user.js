import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/api/users/me',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 */
export function updateProfile(data) {
  return request({
    url: '/api/users/me',
    method: 'put',
    data
  })
}

/**
 * 获取用户主页
 * @param {number} userId - 用户 ID
 */
export function getUserProfile(userId) {
  return request({
    url: `/api/users/${userId}`,
    method: 'get'
  })
}

/**
 * 申请成为画师
 * @param {Object} data - 申请信息 { portfolioUrl, description }
 */
export function applyForArtist(data) {
  return request({
    url: '/api/users/artist-application',
    method: 'post',
    data
  })
}

/**
 * 获取画师申请状态
 */
export function getArtistApplication() {
  return request({
    url: '/api/users/artist-application',
    method: 'get'
  })
}

/**
 * 更新隐私设置
 * @param {Object} settings - { hideFollowing: boolean, hideFavorites: boolean }
 */
export function updatePrivacySettings(settings) {
  return request({
    url: '/api/users/me/privacy',
    method: 'put',
    data: settings
  })
}
