import request from '../utils/request'

/**
 * 获取反馈列表（管理员）
 * @param {Object} params - { page, size, status }
 */
export function getFeedbackList(params) {
  return request({
    url: '/api/feedback/admin/list',
    method: 'get',
    params
  })
}

/**
 * 回复反馈
 * @param {number} feedbackId
 * @param {Object} data - { reply, status }
 */
export function replyFeedback(feedbackId, data) {
  return request({
    url: `/api/feedback/admin/${feedbackId}/reply`,
    method: 'put',
    data
  })
}

export function processAfterSale(feedbackId, data) {
  return request({
    url: `/api/feedback/admin/${feedbackId}/after-sale`,
    method: 'put',
    data
  })
}
