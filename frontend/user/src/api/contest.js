import request from '@/utils/request'

/**
 * 获取比赛列表
 */
export function getContests(params) {
  return request({
    url: '/api/contests',
    method: 'get',
    params
  })
}

/**
 * 获取首页精选参赛作品
 */
export function getFeaturedEntries(limit = 8) {
  return request({
    url: '/api/contests/featured-entries',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取比赛详情
 */
export function getContest(id) {
  return request({
    url: `/api/contests/${id}`,
    method: 'get'
  })
}

/**
 * 提交参赛作品
 */
export function submitEntry(contestId, data) {
  return request({
    url: `/api/contests/${contestId}/entries`,
    method: 'post',
    data
  })
}

/**
 * 获取参赛作品列表
 */
export function getContestEntries(contestId, params) {
  return request({
    url: `/api/contests/${contestId}/entries`,
    method: 'get',
    params
  })
}

/**
 * 投票评分
 */
export function voteEntry(contestId, data) {
  return request({
    url: `/api/contests/${contestId}/vote`,
    method: 'post',
    data
  })
}

/**
 * 获取投票评价列表
 */
export function getEntryVotes(entryId, params) {
  return request({
    url: `/api/contests/entries/${entryId}/votes`,
    method: 'get',
    params
  })
}
