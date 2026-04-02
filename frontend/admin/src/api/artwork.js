import request from '../utils/request'

/**
 * 获取所有作品列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function getArtworks(params) {
  return request({
    url: '/api/admin/artworks',
    method: 'get',
    params
  })
}

/**
 * 删除违规作品
 * @param {number} id - 作品 ID
 * @param {Object} data - 删除原因
 * @param {string} data.reason - 删除原因
 * @returns {Promise}
 */
export function deleteArtwork(id, data) {
  return request({
    url: `/api/admin/artworks/${id}`,
    method: 'delete',
    data
  })
}

/**
 * ES 全量同步
 */
export function esFullSync() {
  return request({
    url: '/api/admin/artworks/es/full-sync',
    method: 'post',
    timeout: 60000
  })
}

/**
 * 批量提取 AI 特征向量（以图搜图用）
 */
export function esExtractFeatures() {
  return request({
    url: '/api/admin/artworks/es/extract-features',
    method: 'post',
    timeout: 120000
  })
}

/**
 * 批量翻译标签（英文→中文）
 */
export function batchTranslateTags() {
  return request({
    url: '/api/admin/artworks/tags/translate',
    method: 'post',
    timeout: 60000
  })
}
