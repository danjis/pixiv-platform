import request from '@/utils/request'

/**
 * AI 智能客服 API
 */

// 创建新会话
export function createAiChatSession() {
  return request.post('/api/ai-chat/sessions')
}

// 获取会话列表
export function getAiChatSessions(page = 0, size = 20) {
  return request.get('/api/ai-chat/sessions', { params: { page, size } })
}

// 获取会话消息记录
export function getAiChatMessages(sessionId) {
  return request.get(`/api/ai-chat/sessions/${sessionId}/messages`)
}

// 发送消息
export function sendAiChatMessage(sessionId, message) {
  return request.post('/api/ai-chat/send', { sessionId, message })
}

// 删除会话
export function deleteAiChatSession(sessionId) {
  return request.delete(`/api/ai-chat/sessions/${sessionId}`)
}
