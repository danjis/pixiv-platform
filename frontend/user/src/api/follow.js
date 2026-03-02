import request from '@/utils/request'

/**
 * 关注画师
 * @param {number} artistId - 画师 ID
 */
export function followArtist(artistId) {
  return request({
    url: `/api/follows/${artistId}`,
    method: 'post'
  })
}

/**
 * 取消关注
 * @param {number} artistId - 画师 ID
 */
export function unfollowArtist(artistId) {
  return request({
    url: `/api/follows/${artistId}`,
    method: 'delete'
  })
}

/**
 * 获取关注列表
 * @param {Object} params - 查询参数 { page, size }
 */
export function getFollowing(params) {
  return request({
    url: '/api/follows/following',
    method: 'get',
    params
  })
}

/**
 * 获取粉丝列表
 * @param {Object} params - 查询参数 { page, size }
 */
export function getFollowers(params) {
  return request({
    url: '/api/follows/followers',
    method: 'get',
    params
  })
}

/**
 * 获取指定用户的关注列表（公开接口）
 * @param {number} userId - 用户 ID
 * @param {Object} params - 查询参数 { page, size }  page 从 1 开始
 */
export function getUserFollowing(userId, params) {
  return request({
    url: `/api/follows/${userId}/following`,
    method: 'get',
    params
  })
}

/**
 * 获取指定用户的粉丝列表（公开接口）
 * @param {number} userId - 用户 ID
 * @param {Object} params - 查询参数 { page, size }  page 从 1 开始
 */
export function getUserFollowers(userId, params) {
  return request({
    url: `/api/follows/${userId}/followers`,
    method: 'get',
    params
  })
}

/**
 * 检查是否关注
 * @param {number} artistId - 画师 ID
 */
export function checkFollowStatus(artistId) {
  return request({
    url: `/api/follows/${artistId}/status`,
    method: 'get'
  })
}
