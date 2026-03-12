import request from '../utils/request'

/**
 * 获取评论列表（管理员）
 */
export function getComments(params) {
  return request({
    url: '/api/admin/comments',
    method: 'get',
    params
  })
}

/**
 * 删除评论（管理员）
 */
export function deleteComment(id) {
  return request({
    url: `/api/admin/comments/${id}`,
    method: 'delete'
  })
}
