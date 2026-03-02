import request from '@/utils/request'

/**
 * 获取作品评价列表
 * @param {number} artworkId - 作品 ID
 * @param {Object} params - 查询参数 { page, size }
 */
export function getComments(artworkId, params) {
  return request({
    url: `/api/artworks/${artworkId}/comments`,
    method: 'get',
    params
  })
}

/**
 * 发表评价
 * @param {number} artworkId - 作品 ID
 * @param {Object} data - 评价内容 { content, parentId? }
 */
export function addComment(artworkId, data) {
  return request({
    url: `/api/artworks/${artworkId}/comments`,
    method: 'post',
    data
  })
}

/**
 * 删除评价
 * @param {number} artworkId - 作品 ID
 * @param {number} commentId - 评价 ID
 */
export function deleteComment(artworkId, commentId) {
  return request({
    url: `/api/artworks/${artworkId}/comments/${commentId}`,
    method: 'delete'
  })
}
