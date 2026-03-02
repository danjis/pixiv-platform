import request from '@/utils/request'

/**
 * 获取作品列表
 * @param {Object} params - 查询参数 { keyword, tags, sortBy, page, size }
 */
export function getArtworks(params) {
  return request({
    url: '/api/artworks',
    method: 'get',
    params
  })
}

/**
 * 获取作品详情
 * @param {number} artworkId - 作品 ID
 */
export function getArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}`,
    method: 'get'
  })
}

/**
 * 发布作品
 * @param {Object} data - 作品信息
 */
export function createArtwork(data) {
  return request({
    url: '/api/artworks',
    method: 'post',
    data
  })
}

/**
 * 更新作品
 * @param {number} artworkId - 作品 ID
 * @param {Object} data - 作品信息
 */
export function updateArtwork(artworkId, data) {
  return request({
    url: `/api/artworks/${artworkId}`,
    method: 'put',
    data
  })
}

/**
 * 删除作品
 * @param {number} artworkId - 作品 ID
 */
export function deleteArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}`,
    method: 'delete'
  })
}

/**
 * 点赞作品
 * @param {number} artworkId - 作品 ID
 */
export function likeArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}/like`,
    method: 'post'
  })
}

/**
 * 取消点赞
 * @param {number} artworkId - 作品 ID
 */
export function unlikeArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}/like`,
    method: 'delete'
  })
}

/**
 * 收藏作品
 * @param {number} artworkId - 作品 ID
 */
export function favoriteArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}/favorite`,
    method: 'post'
  })
}

/**
 * 取消收藏
 * @param {number} artworkId - 作品 ID
 */
export function unfavoriteArtwork(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}/favorite`,
    method: 'delete'
  })
}

/**
 * 获取用户收藏的作品列表
 * @param {Object} params - 查询参数 { page, size }
 */
export function getUserFavorites(params) {
  return request({
    url: '/api/artworks/favorites',
    method: 'get',
    params
  })
}

/**
 * 获取关注动态流
 * @param {Object} params - 查询参数 { page, size }
 */
export function getFeedArtworks(params) {
  return request({
    url: '/api/artworks/feed',
    method: 'get',
    params
  })
}

/**
 * 获取排行榜
 * @param {Object} params - 查询参数 { sortBy, period, page, size }
 */
export function getRankingArtworks(params) {
  return request({
    url: '/api/artworks/ranking',
    method: 'get',
    params
  })
}

/**
 * 获取用户草稿列表
 * @param {Object} params - { page, size }
 */
export function getDrafts(params) {
  return request({
    url: '/api/artworks/drafts',
    method: 'get',
    params
  })
}

/**
 * 发布草稿
 * @param {number} artworkId - 草稿 ID
 */
export function publishDraft(artworkId) {
  return request({
    url: `/api/artworks/${artworkId}/publish`,
    method: 'put'
  })
}

/**
 * 搜索自动补全建议
 * @param {string} keyword - 搜索关键词
 * @param {number} limit - 最大建议数
 */
export function getSearchSuggestions(keyword, limit = 8) {
  return request({
    url: '/api/artworks/suggestions',
    method: 'get',
    params: { keyword, limit }
  })
}
