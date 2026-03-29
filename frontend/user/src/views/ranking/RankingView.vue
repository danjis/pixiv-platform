<template>
  <div class="ranking-page">
    <div class="ranking-container">
      <!-- 页面头部 -->
      <div class="ranking-header">
        <h1 class="ranking-title">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="var(--px-blue, #0096FA)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="ranking-icon">
            <path d="M18 20V10M12 20V4M6 20v-6"/>
          </svg>
          排行榜
        </h1>
        <p class="ranking-desc">发现最受欢迎的高质量作品</p>
      </div>

      <!-- 筛选栏 -->
      <div class="ranking-filters">
        <!-- 时间范围 -->
        <div class="filter-group">
          <span class="filter-label">时间范围</span>
          <div class="filter-tabs">
            <button
              v-for="p in periodOptions"
              :key="p.value"
              class="filter-tab"
              :class="{ active: period === p.value }"
              @click="changePeriod(p.value)"
            >
              {{ p.label }}
            </button>
          </div>
        </div>

        <!-- 排序方式 -->
        <div class="filter-group">
          <span class="filter-label">排序方式</span>
          <div class="filter-tabs">
            <button
              v-for="s in sortOptions"
              :key="s.value"
              class="filter-tab"
              :class="{ active: sortBy === s.value }"
              @click="changeSortBy(s.value)"
            >
              <component :is="s.icon" v-if="s.icon" />
              {{ s.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading && artworks.length === 0" class="grid-loading">
        <div v-for="n in 12" :key="n" class="skeleton-card">
          <div class="skeleton-thumb"></div>
          <div class="skeleton-info">
            <div class="skeleton-title"></div>
            <div class="skeleton-author"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && artworks.length === 0" class="ranking-empty">
        <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.5">
          <rect x="20" y="20" width="80" height="80" rx="8" />
          <circle cx="45" cy="48" r="8" />
          <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
        </svg>
        <h3>当前时间段暂无作品</h3>
        <p>换个时间范围试试吧</p>
      </div>

      <!-- 排行榜列表 -->
      <template v-else>
        <div class="ranking-list">
          <div
            v-for="(artwork, index) in artworks"
            :key="artwork.id"
            class="ranking-item"
            @click="goToDetail(artwork.id)"
          >
            <!-- 排名标识 -->
            <div class="rank-badge" :class="getRankClass(index)">
              <span class="rank-number">{{ (page - 1) * size + index + 1 }}</span>
            </div>

            <!-- 作品缩略图 -->
            <div class="rank-thumb">
              <img
                :src="artwork.thumbnailUrl || artwork.imageUrl"
                :alt="artwork.title"
                @error="(e) => e.target.src = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22/>'"
              />
            </div>

            <!-- 作品信息 -->
            <div class="rank-info">
              <h3 class="rank-title">{{ artwork.title }}</h3>
              <div class="rank-author" @click.stop="goToArtist(artwork.artistId)">
                <el-avatar :size="22" :src="artwork.artistAvatar">
                  {{ artwork.artistName?.charAt(0) }}
                </el-avatar>
                <span>{{ artwork.artistName }}</span>
              </div>
            </div>

            <!-- 统计数据 -->
            <div class="rank-stats">
              <div class="stat-item" v-if="sortBy === 'most_liked' || sortBy === 'hottest'">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="#ff6b81">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                <span>{{ formatCount(artwork.likeCount) }}</span>
              </div>
              <div class="stat-item" v-if="sortBy === 'most_favorited' || sortBy === 'hottest'">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="#ffa502">
                  <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                </svg>
                <span>{{ formatCount(artwork.favoriteCount) }}</span>
              </div>
              <div class="stat-item" v-if="sortBy === 'most_viewed'">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="#2ed573">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                </svg>
                <span>{{ formatCount(artwork.viewCount) }}</span>
              </div>
              <!-- 热度分数（hover 时显示） -->
              <div class="stat-item stat-hot" v-if="sortBy === 'hottest' && artwork.hotnessScore">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="#ff4500">
                  <path d="M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z"/>
                </svg>
                <span>{{ artwork.hotnessScore?.toFixed(0) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="ranking-pagination" v-if="total > size">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getRankingArtworks } from '@/api/artwork'

const router = useRouter()
const route = useRoute()

const artworks = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(50)
const total = ref(0)

const period = ref(route.query.period || 'all')
const sortBy = ref(route.query.sortBy || 'hottest')

const periodOptions = [
  { label: '今日', value: 'day' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '总榜', value: 'all' }
]

const sortOptions = [
  { label: '综合热度', value: 'hottest' },
  { label: '最多点赞', value: 'most_liked' },
  { label: '最多收藏', value: 'most_favorited' },
  { label: '最多浏览', value: 'most_viewed' }
]

async function loadRanking() {
  loading.value = true
  try {
    const response = await getRankingArtworks({
      sortBy: sortBy.value,
      period: period.value,
      page: page.value,
      size: size.value
    })
    if (response.code === 200 && response.data) {
      artworks.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
  } finally {
    loading.value = false
  }
}

function changePeriod(val) {
  period.value = val
  page.value = 1
  updateQuery()
  loadRanking()
}

function changeSortBy(val) {
  sortBy.value = val
  page.value = 1
  updateQuery()
  loadRanking()
}

function handlePageChange(newPage) {
  page.value = newPage
  loadRanking()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function updateQuery() {
  router.replace({
    query: {
      ...route.query,
      sortBy: sortBy.value,
      period: period.value
    }
  })
}

function getRankClass(index) {
  const rank = (page.value - 1) * size.value + index
  if (rank === 0) return 'rank-gold'
  if (rank === 1) return 'rank-silver'
  if (rank === 2) return 'rank-bronze'
  return ''
}

function formatCount(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

function goToDetail(id) {
  if (!id) return
  router.push({ name: 'ArtworkDetail', params: { id } })
}

function goToArtist(id) {
  if (!id) return
  router.push(`/artist/${id}`)
}

onMounted(() => {
  loadRanking()
})
</script>

<style scoped>
.ranking-page {
  min-height: calc(100vh - 64px);
  background: #fff;
  padding: 24px 0;
}

.ranking-container {
  max-width: var(--px-max-width, 1280px);
  margin: 0 auto;
  padding: 0 24px;
}

.ranking-header {
  margin-bottom: 24px;
}

.ranking-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--px-text-primary, #1a1a2e);
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 8px;
}

.ranking-icon {
  color: var(--px-primary, #0096FA);
}

.ranking-desc {
  font-size: 14px;
  color: var(--px-text-secondary, #666);
  margin: 0;
}

/* 筛选栏 */
.ranking-filters {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--px-text-secondary, #666);
  white-space: nowrap;
}

.filter-tabs {
  display: flex;
  gap: 6px;
}

.filter-tab {
  padding: 6px 16px;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  background: var(--px-bg-secondary, #f5f5f5);
  color: var(--px-text-secondary, #666);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-tab:hover {
  background: #e8f0fe;
  color: var(--px-primary, #0096FA);
}

.filter-tab.active {
  background: var(--px-primary, #0096FA);
  color: #fff;
  font-weight: 600;
}

/* 排行榜列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.ranking-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

/* 排名标识 */
.rank-badge {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: var(--px-bg-secondary, #f0f0f0);
}

.rank-number {
  font-size: 16px;
  font-weight: 700;
  color: var(--px-text-secondary, #999);
}

.rank-gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}
.rank-gold .rank-number { color: #fff; }

.rank-silver {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
}
.rank-silver .rank-number { color: #fff; }

.rank-bronze {
  background: linear-gradient(135deg, #CD7F32, #B8860B);
}
.rank-bronze .rank-number { color: #fff; }

/* 缩略图 */
.rank-thumb {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--px-bg-tertiary, #f2f3f5);
}

.rank-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 作品信息 */
.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--px-text-primary, #1a1a2e);
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--px-text-secondary, #666);
  cursor: pointer;
}

.rank-author:hover span {
  color: var(--px-primary, #0096FA);
}

/* 统计数据 */
.rank-stats {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--px-text-secondary, #666);
}

/* 分页 */
.ranking-pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 40px;
}

/* 空状态 */
.ranking-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.ranking-empty h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: var(--px-text-primary, #333);
}

.ranking-empty p {
  font-size: 14px;
  color: var(--px-text-secondary, #999);
}

/* 骨架屏 */
.grid-loading {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.skeleton-card {
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
}

.skeleton-thumb {
  width: 100%;
  padding-bottom: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 12px;
}

.skeleton-title {
  height: 16px;
  width: 80%;
  background: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-author {
  height: 12px;
  width: 50%;
  background: #f0f0f0;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .ranking-filters {
    flex-direction: column;
    gap: 12px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .rank-stats {
    flex-direction: column;
    gap: 4px;
  }

  .rank-thumb {
    width: 56px;
    height: 56px;
  }
}
</style>
