<template>
  <div class="history-page">
    <div class="page-header">
      <h1 class="page-title">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="9"/>
          <polyline points="12 7 12 12 15.5 14"/>
          <path d="M1 12h3M4.5 5.5L6.5 7.5"/>
        </svg>
        浏览记录
      </h1>
      <div class="page-actions">
        <span class="history-count" v-if="!loading">共 {{ total }} 条记录</span>
        <button v-if="historyList.length > 0" class="clear-btn" @click="handleClearAll">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
            <path d="M10 3h4a1 1 0 0 1 1 1v2H9V4a1 1 0 0 1 1-1z"/>
          </svg>
          清空全部
        </button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="historyList.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" width="80" height="80" fill="none" stroke="#ddd" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="9"/>
        <polyline points="12 7 12 12 15.5 14"/>
      </svg>
      <p class="empty-text">暂无浏览记录</p>
      <p class="empty-hint">去发现一些精彩的作品吧</p>
      <router-link to="/artworks" class="explore-btn">发现作品</router-link>
    </div>

    <!-- 按日期分组 -->
    <div v-else class="history-content">
      <div v-for="(group, date) in groupedHistory" :key="date" class="date-group">
        <h2 class="date-label">{{ date }}</h2>
        <div class="history-grid">
          <div
            v-for="item in group"
            :key="item.id"
            class="history-card"
            :class="{ unavailable: !item.isAvailable }"
          >
            <div class="card-image" @click="goToArtwork(item)">
              <img
                v-if="item.isAvailable"
                :src="item.thumbnailUrl || item.imageUrl"
                :alt="item.title"
                loading="lazy"
              />
              <div v-else class="unavailable-placeholder">
                <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <circle cx="8.5" cy="8.5" r="1.5"/>
                  <path d="M21 15l-5-5L5 21"/>
                </svg>
              </div>
              <div class="card-overlay">
                <span class="view-time">{{ formatTime(item.viewedAt) }}</span>
              </div>
            </div>
            <div class="card-info">
              <h3 class="card-title" @click="goToArtwork(item)">{{ item.title }}</h3>
              <div class="card-meta" v-if="item.isAvailable">
                <div class="artist-info" @click.stop="goToArtist(item)">
                  <img :src="item.artistAvatar || defaultAvatar" class="mini-avatar" />
                  <span class="artist-name">{{ item.artistName }}</span>
                </div>
                <div class="stats">
                  <span class="stat">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78z"/></svg>
                    {{ item.likeCount || 0 }}
                  </span>
                  <span class="stat">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z"/><circle cx="12" cy="12" r="3"/></svg>
                    {{ item.viewCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <button class="delete-btn" @click.stop="handleDelete(item)" title="删除此记录">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <button class="load-more-btn" @click="loadMore" :disabled="loadingMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBrowsingHistory, deleteHistoryItem, clearAllHistory } from '@/api/history'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loading = ref(true)
const loadingMore = ref(false)
const historyList = ref([])
const total = ref(0)
const currentPage = ref(0)
const pageSize = 20
const hasMore = ref(false)

// 按日期分组
const groupedHistory = computed(() => {
  const groups = {}
  historyList.value.forEach(item => {
    const date = formatDate(item.viewedAt)
    if (!groups[date]) groups[date] = []
    groups[date].push(item)
  })
  return groups
})

function formatDate(dateStr) {
  if (!dateStr) return '未知日期'
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) return '今天'
  if (date.toDateString() === yesterday.toDateString()) return '昨天'

  const diff = Math.floor((today - date) / (1000 * 60 * 60 * 24))
  if (diff < 7) return `${diff} 天前`

  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function goToArtwork(item) {
  if (item.isAvailable) {
    router.push(`/artworks/${item.artworkId}`)
  }
}

function goToArtist(item) {
  if (item.artistId) {
    router.push(`/artist/${item.artistId}`)
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const res = await getBrowsingHistory({ page: 0, size: pageSize })
    if (res.code === 200 && res.data) {
      historyList.value = res.data.records || []
      total.value = res.data.total || 0
      currentPage.value = 1
      hasMore.value = historyList.value.length < total.value
    }
  } catch {
    ElMessage.error('加载浏览记录失败')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    const res = await getBrowsingHistory({ page: currentPage.value, size: pageSize })
    if (res.code === 200 && res.data) {
      const newRecords = res.data.records || []
      historyList.value.push(...newRecords)
      currentPage.value++
      hasMore.value = historyList.value.length < (res.data.total || 0)
    }
  } catch {
    ElMessage.error('加载更多失败')
  } finally {
    loadingMore.value = false
  }
}

async function handleDelete(item) {
  try {
    const res = await deleteHistoryItem(item.id)
    if (res.code === 200) {
      historyList.value = historyList.value.filter(h => h.id !== item.id)
      total.value--
      ElMessage.success('已删除')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleClearAll() {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览记录吗？此操作不可恢复。', '清空浏览记录', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await clearAllHistory()
    if (res.code === 200) {
      historyList.value = []
      total.value = 0
      ElMessage.success('已清空所有浏览记录')
    } else {
      ElMessage.error(res.message || '清空失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px;
  background: #fff;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.history-count {
  font-size: 14px;
  color: #999;
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: none;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 999px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #ff4d4f;
  color: #fff;
}

/* 加载中 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #999;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #f0f0f0;
  border-top: 3px solid #0096fa;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin: 16px 0 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin-bottom: 24px;
}

.explore-btn {
  padding: 10px 32px;
  background: #0096fa;
  color: #fff;
  border-radius: 999px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s;
}

.explore-btn:hover {
  background: #0080d4;
}

/* 日期分组 */
.date-group {
  margin-bottom: 32px;
}

.date-label {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 16px;
  padding-left: 4px;
}

/* 网格布局 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.history-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  transition: all 0.3s;
}

.history-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.10);
}

.history-card.unavailable {
  opacity: 0.6;
}

.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  cursor: pointer;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 16px 16px 0 0;
}

.card-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.history-card:hover .card-image img {
  transform: scale(1.05);
}

.unavailable-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
}

.card-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
}

.view-time {
  color: rgba(255,255,255,0.9);
  font-size: 12px;
}

.card-info {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.card-title:hover {
  color: #0096fa;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.artist-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.mini-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.artist-name {
  font-size: 12px;
  color: #666;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist-name:hover {
  color: #0096fa;
}

.stats {
  display: flex;
  gap: 10px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #999;
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.5);
  border: none;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  z-index: 2;
}

.history-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: #ff4d4f;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.load-more-btn {
  padding: 10px 40px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 999px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more-btn:hover:not(:disabled) {
  border-color: #0096fa;
  color: #0096fa;
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .history-page {
    padding: 20px 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .history-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
