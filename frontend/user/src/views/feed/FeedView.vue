<template>
  <div class="feed-page">
    <div class="feed-container">
      <!-- 页面头部 -->
      <div class="feed-header">
        <h1 class="feed-title">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="var(--px-blue, #0096FA)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="feed-icon">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          关注动态
        </h1>
        <p class="feed-desc">来自你关注的画师的最新作品</p>
      </div>

      <!-- 未登录提示 -->
      <div v-if="!userStore.isAuthenticated" class="feed-login-hint">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#aaa" stroke-width="1.5">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
        </svg>
        <p>请先登录查看关注动态</p>
        <el-button type="primary" round @click="$router.push('/login')">
          去登录
        </el-button>
      </div>

      <!-- 加载中 -->
      <div v-else-if="loading && artworks.length === 0" class="grid-loading">
        <div v-for="n in 12" :key="n" class="skeleton-card">
          <div class="skeleton-thumb"></div>
          <div class="skeleton-info">
            <div class="skeleton-title"></div>
            <div class="skeleton-author"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && artworks.length === 0" class="feed-empty">
        <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.5">
          <rect x="20" y="20" width="80" height="80" rx="8" />
          <circle cx="45" cy="48" r="8" />
          <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
        </svg>
        <h3>暂无动态</h3>
        <p>去关注你喜欢的画师，这里将展示他们的最新作品</p>
        <el-button type="primary" round @click="$router.push('/artworks')">
          探索作品
        </el-button>
      </div>

      <!-- 作品列表 — 瀑布流布局 -->
      <template v-else>
        <div class="masonry-grid">
          <div
            v-for="artwork in artworks"
            :key="artwork.id"
            class="masonry-item"
            @click="goToDetail(artwork.id)"
          >
            <div class="mi-thumb">
              <img :src="artwork.thumbnailUrl || artwork.imageUrl" :alt="artwork.title" class="mi-img" loading="lazy" />
              <div class="mi-overlay">
                <div class="mi-actions">
                  <span class="mi-btn">
                    <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                    {{ formatCount(artwork.likeCount) }}
                  </span>
                </div>
              </div>
              <div v-if="artwork.imageCount > 1" class="mi-multi">{{ artwork.imageCount }}张</div>
            </div>
            <div class="mi-info">
              <p class="mi-title">{{ artwork.title }}</p>
              <div class="mi-meta">
                <div class="mi-author" @click.stop="goToArtist(artwork.artistId)">
                  <el-avatar :size="18" :src="artwork.artistAvatar || undefined" class="mi-avatar">{{ artwork.artistName?.charAt(0) }}</el-avatar>
                  <span class="mi-name">{{ artwork.artistName }}</span>
                </div>
                <div class="mi-stats">
                  <span class="mi-stat">
                    <svg viewBox="0 0 24 24" width="11" height="11" fill="currentColor" style="opacity:.4"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                    {{ formatCount(artwork.viewCount) }}
                  </span>
                  <span class="mi-stat">
                    <svg viewBox="0 0 24 24" width="11" height="11" fill="#FF4060" style="opacity:.75"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                    {{ formatCount(artwork.likeCount) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="feed-pagination" v-if="total > size">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="handlePageChange"
          />
        </div>

        <!-- 加载更多 loading -->
        <div v-if="loading" class="load-more-spinner">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getFeedArtworks } from '@/api/artwork'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const artworks = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(24)
const total = ref(0)

async function loadFeed() {
  if (!userStore.isAuthenticated) return

  loading.value = true
  try {
    const response = await getFeedArtworks({ page: page.value, size: size.value })
    if (response.code === 200 && response.data) {
      artworks.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载关注动态失败:', error)
  } finally {
    loading.value = false
  }
}

function handlePageChange(newPage) {
  page.value = newPage
  loadFeed()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToDetail(id) {
  if (!id) return
  router.push({ name: 'ArtworkDetail', params: { id } })
}

function goToArtist(id) {
  if (!id) return
  router.push(`/artist/${id}`)
}

function formatCount(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + '千'
  return String(num)
}

onMounted(() => {
  loadFeed()
})
</script>

<style scoped>
.feed-page {
  min-height: calc(100vh - 64px);
  background: #fff;
  padding: 24px 0;
}

.feed-container {
  max-width: var(--px-max-width, 1280px);
  margin: 0 auto;
  padding: 0 24px;
}

.feed-header {
  margin-bottom: 32px;
}

.feed-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--px-text-primary, #1a1a2e);
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 8px;
}

.feed-icon {
  color: var(--px-primary, #0096FA);
}

.feed-desc {
  font-size: 14px;
  color: var(--px-text-secondary, #666);
  margin: 0;
}

/* ===================== 瀑布流 ===================== */
.masonry-grid {
  column-count: 4;
  column-gap: 16px;
}
.masonry-item {
  break-inside: avoid;
  margin-bottom: 16px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.25s ease;
}
.masonry-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}
.mi-thumb {
  position: relative;
  overflow: hidden;
}
.mi-img {
  width: 100%;
  display: block;
}
.mi-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.25s ease;
  display: flex;
  align-items: flex-end;
  padding: 12px;
}
.masonry-item:hover .mi-overlay {
  opacity: 1;
}
.mi-actions {
  display: flex;
  gap: 8px;
}
.mi-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  border: none;
  border-radius: 999px;
  font-size: 11px;
  color: var(--px-text-secondary, #666);
}
.mi-multi {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  backdrop-filter: blur(4px);
}
.mi-info {
  padding: 12px 14px;
}
.mi-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-text-primary, #1a1a2e);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mi-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mi-author {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.mi-avatar {
  flex-shrink: 0;
}
.mi-name {
  font-size: 12px;
  color: var(--px-text-tertiary, #8c8c8c);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
}
.mi-stats {
  display: flex;
  gap: 8px;
}
.mi-stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--px-text-placeholder, #bbb);
}

/* 分页 */
.feed-pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 40px;
}

/* 空状态 */
.feed-empty,
.feed-login-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: var(--px-text-secondary, #666);
}

.feed-empty h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: var(--px-text-primary, #333);
}

.feed-empty p,
.feed-login-hint p {
  margin: 8px 0 20px;
  font-size: 14px;
  color: var(--px-text-secondary, #999);
}

/* 骨架屏加载 */
.grid-loading {
  column-count: 4;
  column-gap: 16px;
}

.skeleton-card {
  break-inside: avoid;
  margin-bottom: 16px;
  border-radius: 20px;
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

/* 加载更多 */
.load-more-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: var(--px-text-secondary, #999);
  font-size: 14px;
}

@media (max-width: 1100px) {
  .masonry-grid, .grid-loading { column-count: 3; }
}
@media (max-width: 768px) {
  .masonry-grid, .grid-loading { column-count: 2; }
  .feed-title { font-size: 22px; }
}
@media (max-width: 480px) {
  .masonry-grid, .grid-loading { column-count: 2; column-gap: 10px; }
}
</style>
