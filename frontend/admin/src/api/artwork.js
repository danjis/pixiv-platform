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
 * 获取审计日志
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @returns {Promise}
 */
export function getAuditLogs(params) {
  return request({
    url: '/api/admin/audit-logs',
    method: 'get',
    params
  })
}
