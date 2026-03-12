<template>
  <div class="history-page">
    <div class="page-header">
      <h1 class="page-title">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
          <path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
        </svg>
        浏览记录
      </h1>
      <div class="page-actions">
        <span class="history-count" v-if="!loading">共 {{ total }} 条记录</span>
        <button v-if="historyList.length > 0" class="clear-btn" @click="handleClearAll">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
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
      <svg viewBox="0 0 24 24" width="80" height="80" fill="#ddd">
        <path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
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
                <svg viewBox="0 0 24 24" width="32" height="32" fill="#ccc">
                  <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
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
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                    {{ item.likeCount || 0 }}
                  </span>
                  <span class="stat">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                    {{ item.viewCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
            <button class="delete-btn" @click.stop="handleDelete(item)" title="删除此记录">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
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
  font-size: 24px;
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
  padding: 8px 16px;
  background: none;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 20px;
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
  border-radius: 24px;
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
  font-size: 16px;
  font-weight: 600;
  color: #333;
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
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

.history-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
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
  color: #333;
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
  border-radius: 24px;
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
