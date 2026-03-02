<template>
  <div class="home-page">
    <!-- Hero 横幅区 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="hero-logo">pixiv</span>
            <span class="hero-subtitle-text">发现好作品</span>
          </h1>
          <p class="hero-desc">在这里发现并分享最棒的艺术创作</p>
          <div class="hero-actions">
            <button class="hero-btn hero-btn-primary" @click="$router.push('/artworks')">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
              </svg>
              探索作品
            </button>
            <button
              v-if="!userStore.isAuthenticated"
              class="hero-btn hero-btn-outline"
              @click="$router.push('/register')"
            >
              免费注册
            </button>
          </div>
        </div>
        <!-- 精选作品轮播 -->
        <div class="hero-showcase">
          <div class="showcase-grid">
            <div
              v-for="(art, i) in featuredArtworks"
              :key="art.id || i"
              class="showcase-item"
              :class="'showcase-item-' + i"
              @click="goToDetail(art.id)"
            >
              <img
                :src="art.thumbnailUrl || art.imageUrl"
                :alt="art.title"
                class="showcase-img"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 推荐作品区域 -->
    <section class="recommend-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">推荐作品</h2>
          <router-link to="/artworks" class="section-more">
            查看更多
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/>
            </svg>
          </router-link>
        </div>

        <!-- 作品网格 -->
        <div v-if="loading" class="grid-loading">
          <div v-for="n in 12" :key="n" class="skeleton-card">
            <div class="skeleton-thumb"></div>
            <div class="skeleton-info">
              <div class="skeleton-title"></div>
              <div class="skeleton-author"></div>
            </div>
          </div>
        </div>

        <div v-else-if="artworks.length > 0" class="artwork-grid">
          <ArtworkCard
            v-for="artwork in artworks"
            :key="artwork.id"
            :artwork="artwork"
            @click="goToDetail(artwork.id)"
            @author-click="(art) => goToArtist(art.artistId)"
          />
        </div>

        <div v-else class="empty-state">
          <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.5">
            <rect x="20" y="20" width="80" height="80" rx="8" />
            <circle cx="45" cy="48" r="8" />
            <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
          </svg>
          <p>暂无推荐作品</p>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="px-footer">
      <div class="footer-content">
        <span class="footer-logo">pixiv</span>
        <span class="footer-copy">&copy; 2026 Pixiv Platform</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArtworks } from '@/api/artwork'
import ArtworkCard from '@/components/ArtworkCard.vue'

const router = useRouter()
const userStore = useUserStore()

const artworks = ref([])
const featuredArtworks = ref([])
const loading = ref(true)

// 加载推荐作品
async function loadArtworks() {
  loading.value = true
  try {
    const response = await getArtworks({ page: 1, size: 18, sortBy: 'latest' })
    if (response.code === 200 && response.data) {
      const records = response.data.records || []
      artworks.value = records
      // 取前 5 个作为精选展示
      featuredArtworks.value = records.slice(0, 5)
    }
  } catch (error) {
    console.error('加载推荐作品失败:', error)
  } finally {
    loading.value = false
  }
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
  loadArtworks()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--px-bg-secondary);
}

/* ====== Hero 区域 ====== */
.hero-section {
  position: relative;
  overflow: hidden;
  padding: 0;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0096FA 0%, #0052CC 60%, #003D99 100%);
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(0, 180, 255, 0.3) 0%, transparent 70%),
    radial-gradient(ellipse at 80% 20%, rgba(100, 200, 255, 0.2) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 60px 24px 48px;
  display: flex;
  align-items: center;
  gap: 48px;
}

.hero-text {
  flex: 0 0 380px;
}

.hero-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.hero-logo {
  font-family: Arial, sans-serif;
  font-weight: 900;
  font-size: 48px;
  color: #fff;
  letter-spacing: -2px;
  font-style: italic;
  line-height: 1;
}

.hero-subtitle-text {
  font-size: 22px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 2px;
}

.hero-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 28px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: var(--px-radius-round);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--px-transition-base);
  border: none;
}

.hero-btn-primary {
  background: #fff;
  color: var(--px-blue);
}

.hero-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.hero-btn-outline {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border: 1.5px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(4px);
}

.hero-btn-outline:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

/* 精选展示网格 */
.hero-showcase {
  flex: 1;
  min-width: 0;
}

.showcase-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 120px);
  gap: 8px;
}

.showcase-item {
  border-radius: var(--px-radius-md);
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.showcase-item:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  z-index: 2;
}

/* 第一张占两行高 */
.showcase-item-0 {
  grid-row: 1 / 3;
}

.showcase-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ====== 推荐作品区域 ====== */
.recommend-section {
  padding: 40px 0 60px;
}

.section-container {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: var(--px-font-xl);
  font-weight: 700;
  color: var(--px-text-primary);
  position: relative;
  padding-left: 14px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: var(--px-blue);
}

.section-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--px-font-sm);
  font-weight: 500;
  color: var(--px-text-tertiary);
  transition: color var(--px-transition-fast);
}

.section-more:hover {
  color: var(--px-blue);
}

/* 作品网格 - 瀑布流布局 */
.artwork-grid {
  column-count: 5;
  column-gap: 16px;
}

/* 骨架屏 */
.grid-loading {
  column-count: 5;
  column-gap: 16px;
}

.skeleton-card {
  border-radius: var(--px-radius-md);
  overflow: hidden;
  background: #fff;
  break-inside: avoid;
  margin-bottom: 16px;
}

.skeleton-thumb {
  width: 100%;
  padding-bottom: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 10px 12px;
}

.skeleton-title {
  width: 70%;
  height: 14px;
  border-radius: 4px;
  background: #f0f0f0;
  margin-bottom: 8px;
}

.skeleton-author {
  width: 40%;
  height: 12px;
  border-radius: 4px;
  background: #f0f0f0;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px 0;
  color: var(--px-text-placeholder);
  font-size: var(--px-font-base);
}

/* ====== 页脚 ====== */
.px-footer {
  border-top: 1px solid var(--px-border-light);
  background: var(--px-bg-primary);
  padding: 24px 0;
}

.footer-content {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-logo {
  font-family: Arial, sans-serif;
  font-weight: 900;
  font-size: 20px;
  color: var(--px-blue);
  font-style: italic;
  letter-spacing: -1px;
}

.footer-copy {
  font-size: var(--px-font-xs);
  color: var(--px-text-placeholder);
}

/* ====== 响应式 ====== */
@media (max-width: 1200px) {
  .artwork-grid,
  .grid-loading {
    column-count: 4;
  }
}

@media (max-width: 900px) {
  .hero-content {
    flex-direction: column;
    padding: 40px 24px 32px;
    gap: 32px;
  }
  .hero-text {
    flex: none;
    text-align: center;
  }
  .hero-actions {
    justify-content: center;
  }
  .hero-logo {
    font-size: 36px;
  }
  .hero-subtitle-text {
    font-size: 18px;
  }
  .showcase-grid {
    grid-template-rows: repeat(2, 100px);
  }
  .artwork-grid,
  .grid-loading {
    column-count: 3;
  }
}

@media (max-width: 640px) {
  .hero-content {
    padding: 32px 16px 24px;
  }
  .hero-logo {
    font-size: 30px;
  }
  .showcase-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 80px);
  }
  .showcase-item-0 {
    grid-row: auto;
  }
  .showcase-item:nth-child(n+5) {
    display: none;
  }
  .artwork-grid,
  .grid-loading {
    column-count: 2;
    column-gap: 10px;
  }
  .section-container {
    padding: 0 12px;
  }
}
</style>
