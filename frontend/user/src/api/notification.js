import request from '@/utils/request'

/**
 * 获取通知列表
 * @param {Object} params - 查询参数 { page, size }
 */
export function getNotifications(params) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get'
  })
}

/**
 * 标记通知为已读
 * @param {number} notificationId - 通知 ID
 */
export function markAsRead(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put'
  })
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return request({
    url: '/api/notifications/read-all',
    method: 'put'
  })
}

/**
 * 删除通知
 * @param {number} notificationId - 通知 ID
 */
export function deleteNotification(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'delete'
  })
}
