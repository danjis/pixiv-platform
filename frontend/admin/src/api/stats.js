import request from '../utils/request'

// 获取平台统计（用户服务）
export function getPlatformStats() {
  return request({
    url: '/api/admin/stats',
    method: 'get'
  })
}

// 获取作品统计
export function getArtworkStats() {
  return request({
    url: '/api/admin/artworks/stats',
    method: 'get'
  })
}

// 获取审计日志
export function getAuditLogs(params) {
  return request({
    url: '/api/admin/audit-logs',
    method: 'get',
    params
  })
}
