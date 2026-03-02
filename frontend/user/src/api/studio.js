import request from '@/utils/request'

/**
 * 获取画师仪表盘统计数据
 * 聚合调用：作品数、粉丝数、约稿统计等
 */
export async function getDashboardStats() {
  // 此接口可后端扩展，目前前端通过组合现有API实现
  return request({
    url: '/api/users/me',
    method: 'get'
  })
}

/**
 * 获取画师的作品列表
 * @param {Object} params - { page, size, keyword, sortBy }
 */
export function getMyArtworks(params = {}) {
  return request({
    url: '/api/artworks',
    method: 'get',
    params
  })
}

/**
 * 获取画师接稿列表
 * @param {Object} params - { status, page, size }
 */
export function getArtistCommissions(params = {}) {
  return request({
    url: '/api/commissions',
    method: 'get',
    params: { role: 'artist', page: 0, size: 50, ...params }
  })
}

/**
 * 更新画师设置
 * @param {Object} data - { bio, specialties, minPrice, maxPrice, commissionOpen, contactPreference, contactInfo }
 */
export function updateArtistSettings(data) {
  return request({
    url: '/api/users/me',
    method: 'put',
    data
  })
}
