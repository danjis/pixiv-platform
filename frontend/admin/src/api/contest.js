import request from '@/utils/request'

/**
 * 获取比赛列表
 */
export function getContests(params) {
  return request({
    url: '/api/admin/contests',
    method: 'get',
    params
  })
}

/**
 * 获取比赛详情
 */
export function getContest(id) {
  return request({
    url: `/api/admin/contests/${id}`,
    method: 'get'
  })
}

/**
 * 创建比赛
 */
export function createContest(data) {
  return request({
    url: '/api/admin/contests',
    method: 'post',
    data
  })
}

/**
 * 更新比赛
 */
export function updateContest(id, data) {
  return request({
    url: `/api/admin/contests/${id}`,
    method: 'put',
    data
  })
}

/**
 * 更新比赛状态
 */
export function updateContestStatus(id, status) {
  return request({
    url: `/api/admin/contests/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 删除比赛
 */
export function deleteContest(id) {
  return request({
    url: `/api/admin/contests/${id}`,
    method: 'delete'
  })
}

/**
 * 获取参赛作品列表
 */
export function getContestEntries(contestId, params) {
  return request({
    url: `/api/admin/contests/${contestId}/entries`,
    method: 'get',
    params
  })
}

/**
 * 审核参赛作品
 */
export function reviewEntry(entryId, status) {
  return request({
    url: `/api/admin/contests/entries/${entryId}/review`,
    method: 'put',
    data: { status }
  })
}

/**
 * 计算排名
 */
export function calculateRankings(contestId) {
  return request({
    url: `/api/admin/contests/${contestId}/calculate-rankings`,
    method: 'post'
  })
}
