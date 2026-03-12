import request from '@/utils/request'

/**
 * 提交反馈
 * @param {Object} data - { type, title, content, contactInfo }
 */
export function submitFeedback(data) {
  return request({
    url: '/api/feedback',
    method: 'post',
    data
  })
}

/**
 * 获取我的反馈列表
 * @param {Object} params - { page, size }
 */
export function getMyFeedbacks(params) {
  return request({
    url: '/api/feedback/mine',
    method: 'get',
    params
  })
}
