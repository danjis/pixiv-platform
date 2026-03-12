import request from '@/utils/request'

/**
 * 获取浏览记录
 * @param {Object} params - { page, size }
 */
export function getBrowsingHistory(params = {}) {
  return request({
    url: '/api/browsing-history',
    method: 'get',
    params: { page: 0, size: 20, ...params }
  })
}

/**
 * 删除单条浏览记录
 * @param {number} historyId - 记录 ID
 */
export function deleteHistoryItem(historyId) {
  return request({
    url: `/api/browsing-history/${historyId}`,
    method: 'delete'
  })
}

/**
 * 清空所有浏览记录
 */
export function clearAllHistory() {
  return request({
    url: '/api/browsing-history/clear',
    method: 'delete'
  })
}
