<template>
  <div class="ranking-page">
    <div class="bg-glow glow-a"></div>
    <div class="bg-glow glow-b"></div>
    <div class="bg-grid"></div>

    <div class="ranking-container">
      <header class="ranking-header">
        <div class="header-copy">
          <span class="eyebrow">排行榜</span>
          <h1 class="ranking-title">
            <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="ranking-icon">
              <path d="M18 20V10M12 20V4M6 20v-6"/>
            </svg>
            发现最受欢迎的高质量作品
          </h1>
          <p class="ranking-desc">按时间范围和排序方式浏览热门作品，快速找到当前最值得看的内容。</p>
        </div>

        <div class="header-metrics">
          <div class="metric-card">
            <span class="metric-value">{{ total || artworks.length }}</span>
            <span class="metric-label">总条目</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">{{ periodOptions.find(item => item.value === period)?.label || '总榜' }}</span>
            <span class="metric-label">当前榜单</span>
          </div>
        </div>
      </header>

      <section class="hero-strip">
        <div class="hero-card">
          <div class="hero-card-label">浏览建议</div>
          <div class="hero-card-title">先看综合热度，再切换时间维度</div>
          <div class="hero-card-text">综合热度适合快速发现热门作品，时间维度更适合查看趋势变化。</div>
        </div>
        <div class="hero-card">
          <div class="hero-card-label">互动指标</div>
          <div class="hero-card-title">点赞、收藏、浏览共同反映热度</div>
          <div class="hero-card-text">切换不同排序方式，可以更直观地理解作品受欢迎的原因。</div>
        </div>
        <div class="hero-card accent">
          <div class="hero-card-label">当前状态</div>
          <div class="hero-card-title">{{ loading ? '榜单加载中' : '榜单已就绪' }}</div>
          <div class="hero-card-text">你可以通过时间范围和排序方式快速切换不同榜单视图。</div>
        </div>
      </section>

      <section class="ranking-filters">
        <div class="filter-group">
          <span class="filter-label">时间范围</span>
          <div class="filter-tabs">
            <button
              v-for="item in periodOptions"
              :key="item.value"
              class="filter-tab"
              :class="{ active: period === item.value }"
              @click="changePeriod(item.value)"
              type="button"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="filter-group">
          <span class="filter-label">排序方式</span>
          <div class="filter-tabs">
            <button
              v-for="item in sortOptions"
              :key="item.value"
              class="filter-tab"
              :class="{ active: sortBy === item.value }"
              @click="changeSortBy(item.value)"
              type="button"
            >
              {{ item.label }}
            </button>
          </div>
        </div>
      </section>

      <div v-if="loading && artworks.length === 0" class="grid-loading">
        <div v-for="n in 12" :key="n" class="skeleton-card">
          <div class="skeleton-thumb"></div>
          <div class="skeleton-info">
            <div class="skeleton-title"></div>
            <div class="skeleton-author"></div>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && artworks.length === 0" class="ranking-empty">
        <div class="empty-card">
          <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.5">
            <rect x="20" y="20" width="80" height="80" rx="8" />
            <circle cx="45" cy="48" r="8" />
            <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
          </svg>
          <h3>当前时间段暂无作品</h3>
          <p>可以切换时间范围再试试。</p>
        </div>
      </div>

      <template v-else>
        <div class="ranking-list">
          <div
            v-for="(artwork, index) in artworks"
            :key="artwork.id"
            class="ranking-item"
            @click="goToDetail(artwork.id)"
          >
            <div class="rank-badge" :class="getRankClass(index)">
              <span class="rank-number">{{ (page - 1) * size + index + 1 }}</span>
            </div>

            <div class="rank-thumb">
              <img
                :src="artwork.thumbnailUrl || artwork.imageUrl"
                :alt="artwork.title"
                @error="(e) => e.target.src = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22/>'"
              />
            </div>

            <div class="rank-info">
              <h3 class="rank-title">{{ artwork.title }}</h3>
              <div class="rank-author" @click.stop="goToArtist(artwork.artistId)">
                <el-avatar :size="22" :src="artwork.artistAvatar">
                  {{ artwork.artistName?.charAt(0) }}
                </el-avatar>
                <span>{{ artwork.artistName }}</span>
              </div>
              <div class="rank-meta">
                <span v-if="artwork.isAigc" class="meta-chip aigc">AIGC</span>
                <span class="meta-chip">热度 {{ artwork.hotnessScore?.toFixed(0) || 0 }}</span>
              </div>
            </div>

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
              <div class="stat-item stat-hot" v-if="sortBy === 'hottest' && artwork.hotnessScore">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="#ff4500">
                  <path d="M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z"/>
                </svg>
                <span>{{ artwork.hotnessScore?.toFixed(0) }}</span>
              </div>
            </div>
          </div>
        </div>

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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getRankingArtworks } from '@/api/artwork'
import { ElMessage } from 'element-plus'

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
    ElMessage.error('加载排行榜失败')
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
  min-height: calc(100vh - 56px);
  position: relative;
  overflow: hidden;
  padding: 28px 0 40px;
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.10), transparent 26%),
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.10), transparent 24%),
    linear-gradient(180deg, #fcfdff 0%, #f4f7fb 100%);
}
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.15), transparent 72%);
  pointer-events: none;
}
.bg-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(36px);
  pointer-events: none;
  opacity: 0.7;
}
.glow-a { width: 220px; height: 220px; left: -50px; top: 80px; background: rgba(59, 130, 246, 0.14); }
.glow-b { width: 220px; height: 220px; right: -60px; top: 60px; background: rgba(236, 72, 153, 0.12); }
.ranking-container {
  position: relative;
  z-index: 1;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
}
.ranking-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
  margin-bottom: 22px;
}
.header-copy { max-width: 760px; }
.eyebrow {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(226, 232, 240, 0.9);
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.ranking-title {
  margin: 12px 0 8px;
  font-size: 34px;
  line-height: 1.15;
  color: #0f172a;
  letter-spacing: -0.04em;
  display: flex;
  align-items: center;
  gap: 10px;
}
.ranking-icon { color: #0096FA; }
.ranking-desc {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}
.header-metrics {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.metric-card {
  min-width: 110px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}
.metric-value {
  display: block;
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
}
.metric-label {
  display: block;
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}
.hero-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}
.hero-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}
.hero-card.accent {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.12), rgba(96, 165, 250, 0.08)), rgba(255, 255, 255, 0.84);
}
.hero-card-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 8px;
}
.hero-card-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}
.hero-card-text {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.ranking-filters {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 22px;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
  flex-wrap: wrap;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 14px;
  font-weight: 700;
  color: #475569;
  white-space: nowrap;
}
.filter-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.filter-tab {
  padding: 7px 16px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  background: #f8fafc;
  color: #475569;
  transition: all 0.2s;
}
.filter-tab:hover { background: #eff6ff; color: #2563eb; }
.filter-tab.active {
  background: linear-gradient(135deg, #0096FA, #2563eb);
  color: #fff;
  font-weight: 700;
}
.grid-loading {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.skeleton-card {
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
}
.skeleton-thumb {
  width: 100%;
  padding-bottom: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-info { padding: 12px; }
.skeleton-title,
.skeleton-author {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-title { width: 80%; margin-bottom: 8px; }
.skeleton-author { width: 50%; }
.ranking-empty {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.empty-card {
  padding: 28px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  text-align: center;
}
.empty-card h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: #0f172a;
}
.empty-card p {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ranking-item {
  display: grid;
  grid-template-columns: 54px 84px minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 22px;
  cursor: pointer;
  transition: all 0.22s;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}
.ranking-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.12);
}
.rank-badge {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #eef2f7;
}
.rank-number {
  font-size: 16px;
  font-weight: 800;
  color: #64748b;
}
.rank-gold { background: linear-gradient(135deg, #ffd700, #ffa500); }
.rank-gold .rank-number { color: #fff; }
.rank-silver { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
.rank-silver .rank-number { color: #fff; }
.rank-bronze { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.rank-bronze .rank-number { color: #fff; }
.rank-thumb {
  width: 84px;
  height: 84px;
  border-radius: 18px;
  overflow: hidden;
  background: #f2f4f8;
}
.rank-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.rank-info { min-width: 0; }
.rank-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
}
.rank-author:hover span { color: #2563eb; }
.rank-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.meta-chip {
  padding: 5px 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #64748b;
  font-size: 11px;
  border: 1px solid #e2e8f0;
}
.meta-chip.aigc {
  color: #2563eb;
  background: #eff6ff;
  border-color: #bfdbfe;
}
.rank-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid #e2e8f0;
}
.stat-hot {
  color: #ff4500;
  background: #fff5f0;
  border-color: #ffd9cc;
}
.ranking-pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 20px;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
@media (max-width: 1100px) {
  .hero-strip { grid-template-columns: 1fr; }
  .ranking-header { flex-direction: column; align-items: flex-start; }
  .header-metrics { justify-content: flex-start; }
}
@media (max-width: 900px) {
  .ranking-item {
    grid-template-columns: 42px 72px minmax(0, 1fr);
  }
  .rank-stats {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }
}
@media (max-width: 768px) {
  .ranking-page { padding: 18px 0 28px; }
  .ranking-container { padding: 0 14px; }
  .ranking-title { font-size: 26px; }
  .ranking-filters { padding: 14px; }
  .filter-group { flex-direction: column; align-items: flex-start; }
}
@media (max-width: 600px) {
  .ranking-item {
    grid-template-columns: 42px 64px minmax(0, 1fr);
    gap: 12px;
  }
  .rank-thumb {
    width: 64px;
    height: 64px;
  }
  .rank-stats { gap: 8px; }
}
</style>
