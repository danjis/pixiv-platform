import request from '@/utils/request'

/**
 * 创建或获取与目标用户的对话
 * @param {number} targetUserId - 对方用户 ID
 */
export function createOrGetConversation(targetUserId) {
  return request({
    url: '/api/conversations',
    method: 'post',
    data: { targetUserId }
  })
}

/**
 * 获取我的对话列表
 */
export function getMyConversations() {
  return request({
    url: '/api/conversations',
    method: 'get'
  })
}

/**
 * 获取对话详情
 * @param {number} conversationId
 */
export function getConversation(conversationId) {
  return request({
    url: `/api/conversations/${conversationId}`,
    method: 'get'
  })
}

/**
 * 获取对话消息（分页）
 * @param {number} conversationId
 * @param {Object} params - { page, size }
 */
export function getMessages(conversationId, params = {}) {
  return request({
    url: `/api/conversations/${conversationId}/messages`,
    method: 'get',
    params: { page: 0, size: 50, ...params }
  })
}

/**
 * 发送私信
 * @param {number} conversationId
 * @param {Object} data - { content, messageType?, attachmentUrl? }
 */
export function sendMessage(conversationId, data) {
  return request({
    url: `/api/conversations/${conversationId}/messages`,
    method: 'post',
    data
  })
}

/**
 * 标记对话已读
 * @param {number} conversationId
 */
export function markAsRead(conversationId) {
  return request({
    url: `/api/conversations/${conversationId}/read`,
    method: 'put'
  })
}
