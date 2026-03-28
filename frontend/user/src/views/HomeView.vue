<template>
  <div class="home-page">

    <!-- ===== HERO CAROUSEL ===== -->
    <section class="hero-section">
      <!-- 高斯模糊背景层（低清缩略图充当背景，不拉伸主图） -->
      <div class="hero-blur-bg-wrap">
        <div
          v-for="(art, i) in featuredArtworks.slice(0, 5)"
          :key="'bg-'+( art.id || i)"
          class="hero-blur-bg"
          :class="{ active: heroIndex === i }"
          :style="{ backgroundImage: `url(${art.thumbnailUrl || art.imageUrl})` }"
        ></div>
      </div>
      <!-- 主图层：object-fit: cover 充满轮播框 -->
      <div class="hero-slides">
        <div
          v-for="(art, i) in featuredArtworks.slice(0, 5)"
          :key="art.id || i"
          class="slide-frame"
          :class="{ active: heroIndex === i }"
        >
          <img
            :src="art.imageUrl || art.thumbnailUrl"
            :alt="art.title"
            class="slide-img"
            loading="eager"
          />
        </div>
      </div>
      <div class="hero-dim"></div>
      <div class="hero-vignette"></div>

      <div class="hero-body">
        <div class="hero-left">
          <p class="hero-eyebrow">ACG · 插画 · 约稿平台</p>
          <h1 class="hero-headline">
            <span class="hero-brand">pixiv</span>
            <span class="hero-tagline">发现 · 创作 · 约稿</span>
          </h1>
          <p class="hero-sub">汇聚数十万插画师与爱好者，在这里找到属于你的灵感世界</p>
          <div class="hero-ctas">
            <button class="cta-primary" @click="$router.push('/artworks')">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
              探索作品
            </button>
            <button v-if="!userStore.isAuthenticated" class="cta-ghost" @click="$router.push('/register')">免费注册</button>
            <button v-else-if="userStore.isArtist" class="cta-ghost" @click="$router.push('/publish')">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
              发布作品
            </button>
          </div>
          <div class="hero-stats">
            <div class="hero-stat"><span class="hstat-num">100K+</span><span class="hstat-lbl">作品</span></div>
            <div class="hstat-div"></div>
            <div class="hero-stat"><span class="hstat-num">50K+</span><span class="hstat-lbl">画师</span></div>
            <div class="hstat-div"></div>
            <div class="hero-stat"><span class="hstat-num">10K+</span><span class="hstat-lbl">约稿成功</span></div>
          </div>
        </div>

        <div class="hero-thumbs" v-if="featuredArtworks.length">
          <div
            v-for="(art, i) in featuredArtworks.slice(0, 5)"
            :key="art.id || i"
            class="hero-thumb"
            :class="{ active: heroIndex === i }"
            @click="heroIndex = i"
          >
            <img :src="art.thumbnailUrl || art.imageUrl" :alt="art.title" />
            <div class="thumb-active-bar"></div>
          </div>
        </div>
      </div>

      <div class="hero-dots" v-if="featuredArtworks.length">
        <button
          v-for="(_, i) in featuredArtworks.slice(0, 5)"
          :key="i"
          class="hero-dot"
          :class="{ active: heroIndex === i }"
          @click="heroIndex = i"
        ></button>
      </div>
    </section>

    <!-- ===== QUICK NAV ===== -->
    <nav class="quick-nav">
      <div class="qnav-inner">
        <router-link to="/artworks" class="qnav-item" active-class="router-link-active"><span class="qi">🎨</span><span>最新作品</span></router-link>
        <router-link to="/ranking" class="qnav-item" active-class="router-link-active"><span class="qi">🏅</span><span>排行榜</span></router-link>
        <router-link to="/contests" class="qnav-item" active-class="router-link-active"><span class="qi">🏆</span><span>比赛专区</span></router-link>
        <router-link to="/following" class="qnav-item" v-if="userStore.isAuthenticated" active-class="router-link-active"><span class="qi">💫</span><span>关注动态</span></router-link>
        <router-link to="/commissions" class="qnav-item" v-if="userStore.isAuthenticated" active-class="router-link-active"><span class="qi">✏️</span><span>我的约稿</span></router-link>
        <router-link to="/membership" class="qnav-item" v-if="userStore.isAuthenticated" active-class="router-link-active"><span class="qi">💎</span><span>会员中心</span></router-link>
        <router-link
          :to="{ path: '/artworks', query: { sortBy: 'popular' } }"
          class="qnav-item"
          active-class=""
        ><span class="qi">🔥</span><span>热门推荐</span></router-link>
      </div>
    </nav>

    <!-- ===== 比赛精选 ===== -->
    <section v-if="contestEntries.length > 0" class="block-section contest-bg">
      <div class="block-inner">
        <div class="block-head">
          <div class="block-title-wrap">
            <span class="title-accent" style="background:linear-gradient(180deg,#ffd700,#f0a030)"></span>
            <h2 class="block-title">比赛精选</h2>
            <span class="block-badge badge-orange">CONTEST</span>
          </div>
          <router-link to="/contests" class="block-more">
            查看全部
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
          </router-link>
        </div>
        <div class="contest-track">
          <div
            v-for="entry in contestEntries"
            :key="entry.id"
            class="contest-card"
            @click="goToContestEntry(entry)"
          >
            <div class="cc-thumb">
              <img :src="entry.imageUrl" :alt="entry.title" />
              <div class="cc-ribbon">参赛作品</div>
              <div v-if="entry.averageScore > 0" class="cc-score">
                <svg viewBox="0 0 24 24" width="11" height="11" fill="#ffc040"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                {{ entry.averageScore.toFixed(1) }}
              </div>
              <div class="cc-overlay"><span class="cc-contest-name">{{ entry.contestTitle }}</span></div>
            </div>
            <div class="cc-info">
              <p class="cc-title">{{ entry.title }}</p>
              <p class="cc-artist">{{ entry.artistName }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 主内容区 + 右侧栏 ===== -->
    <div class="content-shell">

      <!-- 左侧主内容 -->
      <main class="content-main">

        <!-- 推荐作品 -->
        <section class="block-section">
          <div class="block-head">
            <div class="block-title-wrap">
              <span class="title-accent" style="background:var(--px-blue)"></span>
              <h2 class="block-title">推荐作品</h2>
              <span class="block-badge badge-blue">FEATURED</span>
            </div>
            <div class="sort-tabs">
              <button
                v-for="tab in sortTabs"
                :key="tab.value"
                class="sort-tab"
                :class="{ active: activeSortTab === tab.value }"
                @click="switchSort(tab.value)"
              >{{ tab.label }}</button>
            </div>
            <router-link to="/artworks" class="block-more">
              查看更多
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
            </router-link>
          </div>

          <!-- 骨架屏 -->
          <div v-if="loading" class="masonry-grid">
            <div v-for="n in 16" :key="n" class="skel-card">
              <div class="skel-thumb" :style="{ paddingBottom: (55 + (n % 5) * 10) + '%' }"></div>
              <div class="skel-body">
                <div class="skel-line" style="width:70%"></div>
                <div class="skel-line" style="width:40%;margin-top:6px"></div>
              </div>
            </div>
          </div>

          <!-- 瀑布流 -->
          <div v-else-if="artworks.length > 0" class="masonry-grid">
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
                    <button class="mi-btn like-btn" @click.stop="onLike(artwork)">
                      <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                      {{ formatCount(artwork.likeCount) }}
                    </button>
                    <button class="mi-btn bookmark-btn" @click.stop="onFavorite(artwork)">
                      <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
                    </button>
                  </div>
                </div>
                <div v-if="artwork.imageCount > 1" class="mi-multi">
                  <svg viewBox="0 0 24 24" width="10" height="10" fill="currentColor"><path d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
                  {{ artwork.imageCount }}
                </div>
                <div v-if="artwork.isR18" class="mi-r18">R-18</div>
              </div>
              <div class="mi-info">
                <p class="mi-title">{{ artwork.title }}</p>
                <div class="mi-meta-row">
                  <div class="mi-author" @click.stop="goToArtist(artwork.artistId)">
                    <el-avatar :size="18" :src="artwork.artistAvatar || undefined" class="mi-avatar">{{ artwork.artistName?.charAt(0) }}</el-avatar>
                    <span class="mi-name">{{ artwork.artistName }}</span>
                  </div>
                  <div class="mi-stats">
                    <span class="mi-stat-item">
                      <svg viewBox="0 0 24 24" width="11" height="11" fill="currentColor" style="opacity:.4"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                      {{ formatCount(artwork.viewCount) }}
                    </span>
                    <span class="mi-stat-item">
                      <svg viewBox="0 0 24 24" width="11" height="11" fill="var(--px-like)" style="opacity:.75"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                      {{ formatCount(artwork.likeCount) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-block">
            <svg viewBox="0 0 120 80" width="120" height="80">
              <rect x="10" y="10" width="100" height="60" rx="6" fill="none" stroke="#ddd" stroke-width="1.5"/>
              <circle cx="38" cy="35" r="8" fill="none" stroke="#ddd" stroke-width="1.5"/>
              <path d="M10 55 l28-18 l16 12 l20-14 l46 15" fill="none" stroke="#ddd" stroke-width="1.5"/>
            </svg>
            <p>暂无推荐作品</p>
          </div>
        </section>

        <!-- AI 热门标签 -->
        <section v-if="trendingTags.length > 0" class="tags-section">
          <div class="block-head" style="margin-bottom:16px">
            <div class="block-title-wrap">
              <span class="title-accent" style="background:linear-gradient(135deg,#7c3aed,#06b6d4)"></span>
              <h2 class="block-title">AI 热门标签</h2>
              <span class="block-badge badge-purple">AI · TAG</span>
            </div>
          </div>
          <div class="tags-scroll">
            <button
              v-for="tag in trendingTags"
              :key="tag.name || tag"
              class="tag-chip"
              @click="$router.push({ name: 'Artworks', query: { tag: tag.name || tag } })"
            >
              <svg viewBox="0 0 24 24" width="11" height="11" fill="currentColor"><path d="M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z"/></svg>
              #{{ tag.name || tag }}
              <span v-if="tag.count" class="tag-count">{{ formatCount(tag.count) }}</span>
            </button>
          </div>
        </section>

      </main>

      <!-- ===== 右侧边栏 ===== -->
      <aside class="content-sidebar">

        <!-- 约稿画师推荐 -->
        <div class="sidebar-widget">
          <div class="sw-head">
            <span class="title-accent" style="background:linear-gradient(135deg,#ec4899,#f97316)"></span>
            <h3 class="sw-title">约稿画师推荐</h3>
            <span class="sw-badge badge-pink">OPEN</span>
          </div>
          <div class="artist-list">
            <div
              v-for="artist in displayArtists"
              :key="artist.artistId"
              class="artist-row"
              @click="goToArtist(artist.artistId)"
            >
              <div class="ar-cover">
                <img :src="artist.thumbnailUrl || artist.imageUrl" :alt="artist.artistName" />
              </div>
              <el-avatar :size="36" :src="artist.artistAvatar || undefined" class="ar-avatar">{{ artist.artistName?.charAt(0) }}</el-avatar>
              <div class="ar-info">
                <p class="ar-name">{{ artist.artistName }}</p>
                <span class="ar-open">接稿中</span>
              </div>
              <button class="ar-btn" @click.stop="$router.push(`/commission/create/${artist.artistId}`)">
                约稿
              </button>
            </div>
          </div>
        </div>

        <!-- 社区脉动 -->
        <div class="sidebar-widget community-widget">
          <div class="sw-head">
            <span class="title-accent" style="background:linear-gradient(135deg,#06b6d4,#3b82f6)"></span>
            <h3 class="sw-title">社区脉动</h3>
            <span class="sw-badge badge-cyan">LIVE</span>
          </div>
          <div class="community-pulse">
            <div class="pulse-item">
              <span class="pulse-icon" style="background:#fff0f3;color:#ff4060">🎨</span>
              <div class="pulse-content">
                <p class="pulse-text">今日新增作品</p>
                <p class="pulse-val" style="color:#ff4060">+2,847</p>
              </div>
            </div>
            <div class="pulse-item">
              <span class="pulse-icon" style="background:#fffbe6;color:#f59e0b">✏️</span>
              <div class="pulse-content">
                <p class="pulse-text">进行中约稿</p>
                <p class="pulse-val" style="color:#f59e0b">1,203 单</p>
              </div>
            </div>
            <div class="pulse-item">
              <span class="pulse-icon" style="background:#f0fdf4;color:#10b981">🏆</span>
              <div class="pulse-content">
                <p class="pulse-text">本周比赛参赛</p>
                <p class="pulse-val" style="color:#10b981">+568 人</p>
              </div>
            </div>
            <div class="pulse-item">
              <span class="pulse-icon" style="background:#eff6ff;color:#3b82f6">💎</span>
              <div class="pulse-content">
                <p class="pulse-text">活跃会员</p>
                <p class="pulse-val" style="color:#3b82f6">12.4K</p>
              </div>
            </div>
          </div>
          <router-link to="/artworks" class="pulse-cta">进入社区广场 →</router-link>
        </div>

        <!-- 快速约稿入口 -->
        <div class="sidebar-widget commission-entry">
          <div class="ce-bg"></div>
          <div class="ce-content">
            <p class="ce-title">开始你的约稿</p>
            <p class="ce-sub">从数千位专业插画师中<br/>找到最适合你的那一位</p>
            <router-link to="/commissions" class="ce-btn">立即浏览画师</router-link>
          </div>
        </div>

      </aside>
    </div>

    <!-- ===== 页脚 ===== -->
    <footer class="px-footer">
      <div class="footer-inner">
        <span class="footer-logo">pixiv</span>
        <span class="footer-copy">&copy; 2026 Pixiv Platform. All rights reserved.</span>
      </div>
    </footer>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArtworks, likeArtwork, favoriteArtwork } from '@/api/artwork'
import { getFeaturedEntries } from '@/api/contest'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// ---- 核心状态（100% 保留原有逻辑）----
const artworks = ref([])
const featuredArtworks = ref([])
const contestEntries = ref([])
const loading = ref(true)

// ---- 新增 UI 状态（不改变任何 API 调用）----
const heroIndex = ref(0)
let heroTimer = null

// 排序 tabs（仅控制 loadArtworks 的 sortBy 参数，不新增接口）
const sortTabs = [
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'popular' },
  { label: '收藏', value: 'favorite' },
]
const activeSortTab = ref('latest')

// AI 热门标签：从已加载作品的 tags 字段聚合
const trendingTags = computed(() => {
  const map = {}
  artworks.value.forEach(aw => {
    if (Array.isArray(aw.tags)) {
      aw.tags.forEach(t => {
        const key = typeof t === 'string' ? t : t?.name
        if (key) map[key] = (map[key] || 0) + 1
      })
    }
  })
  return Object.entries(map)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 30)
    .map(([name, count]) => ({ name, count }))
})

// 约稿画师：从 artworks 聚合，去重后取前 8 个
const displayArtists = computed(() => {
  const seen = new Set()
  return artworks.value
    .filter(aw => aw.artistId && !seen.has(aw.artistId) && seen.add(aw.artistId))
    .slice(0, 8)
})

// ---- 原有 API 调用（路径/入参/出参 100% 保留）----
async function loadContestEntries() {
  try {
    const res = await getFeaturedEntries(8)
    if (res.code === 200 && res.data) {
      contestEntries.value = res.data
    }
  } catch (e) {
    console.error('加载比赛精选失败:', e)
  }
}

async function loadArtworks() {
  loading.value = true
  try {
    const response = await getArtworks({ page: 1, size: 18, sortBy: activeSortTab.value })
    if (response.code === 200 && response.data) {
      const records = response.data.records || []
      artworks.value = records
      featuredArtworks.value = records.slice(0, 5)
    }
  } catch (error) {
    console.error('加载推荐作品失败:', error)
  } finally {
    loading.value = false
  }
}

// ---- 新增交互（调用已有 API，路径完全原样）----
async function onLike(artwork) {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await likeArtwork(artwork.id)
    artwork.likeCount = (artwork.likeCount || 0) + 1
  } catch (e) {
    // ignore
  }
}

async function onFavorite(artwork) {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await favoriteArtwork(artwork.id)
    ElMessage.success('已收藏')
  } catch (e) {
    // ignore
  }
}

function switchSort(val) {
  activeSortTab.value = val
  loadArtworks()
}

// ---- 原有导航函数（100% 保留）----
function goToDetail(id) {
  if (!id) return
  router.push({ name: 'ArtworkDetail', params: { id } })
}

function goToArtist(id) {
  if (!id) return
  router.push(`/artist/${id}`)
}

function goToContestEntry(entry) {
  if (entry.contestId) {
    router.push(`/contests/${entry.contestId}`)
  }
}

function formatCount(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

// ---- 生命周期（100% 保留原有调用）----
onMounted(() => {
  loadArtworks()
  loadContestEntries()
  heroTimer = setInterval(() => {
    if (featuredArtworks.value.length) {
      heroIndex.value = (heroIndex.value + 1) % Math.min(5, featuredArtworks.value.length)
    }
  }, 4000)
})

onBeforeUnmount(() => {
  clearInterval(heroTimer)
})
</script>

<style scoped>
/* ============================================================
   HOME PAGE — 完整样式
   ============================================================ */

.home-page {
  min-height: 100vh;
  background: var(--px-bg-secondary);
}

/* ===================== HERO ===================== */
.hero-section {
  position: relative;
  height: 520px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 高斯模糊背景层 */
.hero-blur-bg-wrap {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.hero-blur-bg {
  position: absolute;
  inset: -20px;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1s ease;
  filter: blur(22px) brightness(0.55) saturate(1.2);
  transform: scale(1.08);
}
.hero-blur-bg.active { opacity: 1; }

/* 主图层：img 保持原始比例 */
.hero-slides {
  position: absolute;
  inset: 0;
  z-index: 1;
}
.slide-frame {
  position: absolute;
  inset: 0;
  display: block;
  opacity: 0;
  transition: opacity 1s ease;
}
.slide-frame.active { opacity: 1; }
.slide-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center;
    display: block;
    filter: drop-shadow(0 8px 32px rgba(0,0,0,0.3));
  }
.hero-dim {
  position: absolute; inset: 0;
  background: linear-gradient(105deg, rgba(0,0,0,0.72) 0%, rgba(0,0,0,0.45) 50%, rgba(0,0,0,0.18) 100%);
}
.hero-vignette {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 70% 50%, transparent 40%, rgba(0,0,0,0.35) 100%);
}
.hero-body {
  position: relative; z-index: 2; flex: 1;
  max-width: var(--px-max-width); margin: 0 auto; width: 100%;
  padding: 0 32px;
  display: flex; align-items: center; justify-content: space-between; gap: 32px;
}
.hero-left { max-width: 480px; }
.hero-eyebrow {
  font-size: 12px; font-weight: 700; letter-spacing: 3px;
  text-transform: uppercase; color: rgba(255,255,255,0.55); margin-bottom: 12px;
}
.hero-headline { display: flex; flex-direction: column; gap: 2px; margin-bottom: 14px; }
.hero-brand {
  font-family: Arial, sans-serif; font-style: italic; font-weight: 900;
  font-size: 56px; line-height: 1; color: #fff; letter-spacing: -2px;
  text-shadow: 0 2px 24px rgba(0,150,250,0.5);
}
.hero-tagline { font-size: 18px; font-weight: 600; color: rgba(255,255,255,0.82); letter-spacing: 4px; }
.hero-sub { font-size: 14px; color: rgba(255,255,255,0.62); line-height: 1.7; margin-bottom: 28px; }
.hero-ctas { display: flex; gap: 12px; margin-bottom: 32px; }
.cta-primary {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 11px 26px; background: var(--px-blue);
  color: #fff; border: none; border-radius: 999px;
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: all 0.22s ease; box-shadow: 0 4px 18px rgba(0,150,250,0.45);
}
.cta-primary:hover { background: var(--px-blue-hover); transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,150,250,0.55); }
.cta-ghost {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 11px 22px; background: rgba(255,255,255,0.14); color: #fff;
  border: 1.5px solid rgba(255,255,255,0.35); border-radius: 999px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  backdrop-filter: blur(6px); transition: all 0.22s ease;
}
.cta-ghost:hover { background: rgba(255,255,255,0.24); transform: translateY(-2px); }
.hero-stats { display: flex; align-items: center; gap: 20px; }
.hero-stat { display: flex; flex-direction: column; gap: 2px; }
.hstat-num { font-size: 22px; font-weight: 800; color: #fff; line-height: 1; }
.hstat-lbl { font-size: 11px; color: rgba(255,255,255,0.5); letter-spacing: 1px; }
.hstat-div { width: 1px; height: 32px; background: rgba(255,255,255,0.2); }
.hero-thumbs { display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; }
.hero-thumb {
  width: 100px; height: 72px; border-radius: 8px; overflow: hidden;
  cursor: pointer; opacity: 0.5; border: 2px solid transparent;
  transition: all 0.3s ease; position: relative;
}
.hero-thumb img { width: 100%; height: 100%; object-fit: cover; }
.hero-thumb.active { opacity: 1; border-color: var(--px-blue); box-shadow: 0 0 0 3px rgba(0,150,250,0.3); }
.thumb-active-bar {
  position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: var(--px-blue); opacity: 0; transition: opacity 0.3s;
}
.hero-thumb.active .thumb-active-bar { opacity: 1; }
.hero-dots {
  position: absolute; bottom: 18px; left: 50%; transform: translateX(-50%);
  z-index: 3; display: flex; gap: 8px;
}
.hero-dot {
  width: 8px; height: 8px; border-radius: 999px; border: none;
  background: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.3s ease; padding: 0;
}
.hero-dot.active { width: 24px; background: #fff; }

/* ===================== QUICK NAV ===================== */
.quick-nav {
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.4);
    position: sticky; top: 56px; z-index: 50;
    box-shadow: 0 4px 24px rgba(0,0,0,0.03);
  }
  .qnav-inner {
    max-width: var(--px-max-width); margin: 0 auto; padding: 12px 32px;
    display: flex; align-items: center; gap: 16px; overflow-x: auto; scrollbar-width: none;
  }
  .qnav-inner::-webkit-scrollbar { display: none; }
  .qnav-item {
    display: flex; align-items: center; gap: 8px; padding: 10px 22px;
    font-size: 14px; font-weight: 700; color: var(--px-text-secondary);
    text-decoration: none; white-space: nowrap;
    border-radius: 999px;
    background: #fff;
    border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 2px 8px rgba(0,0,0,0.02);
    transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
  }
  .qnav-item:hover {
    color: var(--px-blue);
    background: rgba(255, 255, 255, 1);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0,150,250,0.12);
    border-color: rgba(0,150,250,0.15);
  }
  .qnav-item.router-link-active {
    color: #fff;
    background: var(--px-blue);
    border-color: transparent;
    box-shadow: 0 4px 16px rgba(0,150,250,0.4);
  }
  .qi { font-size: 18px; line-height: 1; transition: transform 0.3s ease; }
  .qnav-item:hover .qi { transform: scale(1.15) rotate(-5deg); }


/* ===================== CONTEST BG ===================== */
.contest-bg {
  background: linear-gradient(135deg, #fffbeb 0%, #fff7ed 100%);
  border-top: 1px solid #fde68a;
  border-bottom: 1px solid #fed7aa;
}

/* ===================== BLOCK 公共 ===================== */
.block-section { padding: 40px 0; }
.block-inner { max-width: var(--px-max-width); margin: 0 auto; padding: 0 32px; }
.block-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 22px; gap: 12px; flex-wrap: wrap;
}
.block-title-wrap { display: flex; align-items: center; gap: 10px; }
.title-accent { display: inline-block; width: 4px; height: 22px; border-radius: 2px; flex-shrink: 0; }
.block-title { font-size: 20px; font-weight: 800; color: var(--px-text-primary); letter-spacing: -0.3px; }
.block-badge {
  font-size: 10px; font-weight: 700; letter-spacing: 1.5px;
  padding: 3px 8px; border-radius: 4px; border: 1px solid;
}
.badge-orange { color: #e88000; background: #fff7eb; border-color: #ffe0b2; }
.badge-blue   { color: var(--px-blue); background: var(--px-blue-light); border-color: #b3d9f8; }
.badge-purple { color: #7c3aed; background: #f3e8ff; border-color: #d8b4fe; }
.badge-pink   { color: #ec4899; background: #fdf2f8; border-color: #fbcfe8; }
.badge-cyan   { color: #0891b2; background: #ecfeff; border-color: #a5f3fc; }
.block-more {
  display: flex; align-items: center; gap: 3px; font-size: 13px; font-weight: 500;
  color: var(--px-text-tertiary); text-decoration: none; margin-left: auto;
  flex-shrink: 0; transition: color 0.15s;
}
.block-more:hover { color: var(--px-blue); }

/* ===================== 比赛精选 ===================== */
.contest-track {
  display: flex; gap: 16px; overflow-x: auto;
  padding-bottom: 12px; scrollbar-width: thin; scrollbar-color: #ddd transparent;
}
.contest-track::-webkit-scrollbar { height: 4px; }
.contest-track::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }
.contest-card {
  flex: 0 0 220px; background: #fff; border-radius: 12px; overflow: hidden;
  cursor: pointer; transition: all 0.28s ease; border: 1.5px solid transparent;
}
.contest-card:hover { transform: translateY(-6px); box-shadow: 0 14px 36px rgba(240,160,48,0.18); border-color: #f0c040; }
.cc-thumb {
  position: relative; width: 100%; padding-bottom: 72%;
  overflow: hidden; background: var(--px-bg-tertiary);
}
.cc-thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.35s ease; }
.contest-card:hover .cc-thumb img { transform: scale(1.06); }
.cc-ribbon {
  position: absolute; top: 10px; left: 0;
  background: linear-gradient(135deg, #f0a030, #e88000); color: #fff;
  font-size: 10px; font-weight: 700; padding: 3px 10px 3px 12px;
  border-radius: 0 4px 4px 0; letter-spacing: 1px;
}
.cc-score {
  position: absolute; bottom: 8px; right: 8px; display: flex; align-items: center; gap: 3px;
  background: rgba(0,0,0,0.62); color: #ffc040; font-size: 12px; font-weight: 700;
  padding: 3px 8px; border-radius: 20px;
}
.cc-overlay {
  position: absolute; inset: 0; background: linear-gradient(transparent 50%, rgba(0,0,0,0.6) 100%);
  display: flex; align-items: flex-end; padding: 8px 10px; opacity: 0; transition: opacity 0.25s;
}
.contest-card:hover .cc-overlay { opacity: 1; }
.cc-contest-name { font-size: 11px; color: rgba(255,255,255,0.85); font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }
.cc-info { padding: 10px 12px 12px; }
.cc-title { font-size: 13px; font-weight: 600; color: var(--px-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.cc-artist { font-size: 11px; color: var(--px-text-tertiary); }

/* ===================== 主内容 + 侧边栏 ===================== */
.content-shell {
  max-width: var(--px-max-width); margin: 0 auto; padding: 0 32px;
  display: grid; grid-template-columns: 1fr 300px; gap: 28px; align-items: start;
}
.content-main { min-width: 0; }
.content-main .block-section { padding: 32px 0; }
.content-main .block-inner { max-width: 100%; padding: 0; }

/* ===================== 排序 Tabs ===================== */
.sort-tabs { display: flex; gap: 2px; background: var(--px-bg-tertiary); border-radius: 8px; padding: 3px; }
.sort-tab {
  padding: 5px 14px; border: none; border-radius: 6px;
  font-size: 12px; font-weight: 600; color: var(--px-text-tertiary);
  background: transparent; cursor: pointer; transition: all 0.18s ease;
}
.sort-tab.active { background: #fff; color: var(--px-blue); box-shadow: 0 1px 4px rgba(0,0,0,0.1); }

/* ===================== 瀑布流 ===================== */
.masonry-grid { column-count: 3; column-gap: 14px; }
.masonry-item {
  break-inside: avoid; margin-bottom: 14px; border-radius: 10px;
  overflow: hidden; background: #fff; cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border: 1px solid var(--px-border-light);
}
.masonry-item:hover { transform: translateY(-4px); box-shadow: 0 8px 28px rgba(0,0,0,0.13); border-color: transparent; }
.mi-thumb { position: relative; line-height: 0; overflow: hidden; background: var(--px-bg-tertiary); }
.mi-img { width: 100%; height: auto; display: block; transition: transform 0.4s ease; }
.masonry-item:hover .mi-img { transform: scale(1.04); }
.mi-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(transparent 55%, rgba(0,0,0,0.6) 100%);
  opacity: 0; transition: opacity 0.22s ease;
  display: flex; flex-direction: column; justify-content: flex-end; padding: 8px;
  pointer-events: none;
}
.masonry-item:hover .mi-overlay { opacity: 1; }
.mi-actions { pointer-events: all; }
.mi-actions { display: flex; justify-content: flex-end; gap: 6px; }
.mi-btn {
  display: flex; align-items: center; gap: 4px; padding: 5px 10px;
  border: none; border-radius: 999px; font-size: 12px; font-weight: 600;
  cursor: pointer; backdrop-filter: blur(4px); transition: transform 0.15s;
}
.mi-btn:hover { transform: scale(1.1); }
.like-btn { background: rgba(255,64,96,0.85); color: #fff; }
.bookmark-btn { background: rgba(255,184,0,0.85); color: #fff; }
.mi-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.mi-tag {
  font-size: 10px; color: rgba(255,255,255,0.88); background: rgba(0,0,0,0.38);
  backdrop-filter: blur(3px); padding: 2px 7px; border-radius: 999px;
}
.mi-multi {
  position: absolute; top: 7px; right: 7px; display: flex; align-items: center; gap: 3px;
  background: rgba(0,0,0,0.58); color: #fff; font-size: 11px; font-weight: 600;
  padding: 3px 7px; border-radius: 6px;
}
.mi-r18 {
  position: absolute; top: 7px; left: 7px; background: #ff0000; color: #fff;
  font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; letter-spacing: 0.5px;
}
.mi-info { padding: 9px 11px 10px; }
.mi-title {
  font-size: 13px; font-weight: 600; color: var(--px-text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 5px; transition: color 0.15s;
}
.masonry-item:hover .mi-title { color: var(--px-blue); }
.mi-meta-row { display: flex; align-items: center; justify-content: space-between; }
.mi-author { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.mi-name {
  font-size: 11px; color: var(--px-text-tertiary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; transition: color 0.15s;
}
.mi-author:hover .mi-name { color: var(--px-blue); }
.mi-avatar { flex-shrink: 0; font-size: 9px; }
.mi-stats { display: flex; align-items: center; gap: 8px; }
.mi-stat-item {
  display: flex; align-items: center; gap: 3px;
  font-size: 11px; color: var(--px-text-placeholder);
}

/* 骨架屏 */
.skel-card { break-inside: avoid; margin-bottom: 14px; border-radius: 10px; overflow: hidden; background: #fff; }
.skel-thumb {
  width: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%; animation: shimmer 1.5s infinite;
}
.skel-body { padding: 10px 12px 12px; }
.skel-line { height: 12px; border-radius: 4px; background: #f0f0f0; }
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 空态 */
.empty-block {
  display: flex; flex-direction: column; align-items: center; gap: 14px;
  padding: 80px 0; color: var(--px-text-placeholder); font-size: 14px;
}

/* ===================== AI 标签滑块 ===================== */
.tags-section {
  padding: 28px 0 32px;
  background: linear-gradient(135deg, #faf5ff 0%, #eff6ff 100%);
  border-radius: 14px;
  border: 1px solid #e9d5ff;
  margin-bottom: 8px;
}
.tags-section .block-head { padding: 0 20px; }
.tags-scroll {
  display: flex; flex-wrap: wrap; gap: 9px;
  padding: 0 20px; overflow-x: auto; scrollbar-width: none;
}
.tags-scroll::-webkit-scrollbar { display: none; }
.tag-chip {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 7px 14px; background: #fff;
  border: 1.5px solid #e9d5ff; border-radius: 999px;
  font-size: 12px; font-weight: 600; color: #7c3aed;
  cursor: pointer; white-space: nowrap; transition: all 0.2s ease;
  box-shadow: 0 1px 4px rgba(124,58,237,0.06);
}
.tag-chip:hover {
  background: #7c3aed; color: #fff; border-color: #7c3aed;
  transform: translateY(-2px); box-shadow: 0 4px 14px rgba(124,58,237,0.28);
}
.tag-count { font-size: 10px; font-weight: 500; opacity: 0.6; margin-left: 2px; }

/* ===================== 右侧边栏 ===================== */
.content-sidebar {
  position: sticky;
  top: calc(var(--px-header-height) + 48px + 16px);
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.sidebar-widget {
  background: #fff;
  border: 1px solid var(--px-border-light);
  border-radius: 14px;
  padding: 18px 16px 20px;
  box-shadow: var(--px-shadow-sm);
}
.sw-head {
  display: flex; align-items: center; gap: 9px; margin-bottom: 16px;
}
.sw-title {
  font-size: 15px; font-weight: 800; color: var(--px-text-primary); flex: 1;
}
.sw-badge {
  font-size: 9px; font-weight: 700; letter-spacing: 1.5px;
  padding: 2px 7px; border-radius: 4px; border: 1px solid;
}

/* 约稿画师列表 */
.artist-list { display: flex; flex-direction: column; gap: 2px; }
.artist-row {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 6px; border-radius: 10px; cursor: pointer;
  transition: background 0.15s ease; position: relative;
}
.artist-row:hover { background: var(--px-bg-hover); }
.ar-cover {
  width: 36px; height: 36px; border-radius: 8px; overflow: hidden;
  flex-shrink: 0; background: var(--px-bg-tertiary);
}
.ar-cover img { width: 100%; height: 100%; object-fit: cover; }
.ar-avatar {
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 1px 6px rgba(0,0,0,0.12);
  font-size: 14px; font-weight: 700;
  margin-left: -18px;
  position: relative;
  z-index: 1;
}
.ar-info { flex: 1; min-width: 0; }
.ar-name {
  font-size: 13px; font-weight: 700; color: var(--px-text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 2px;
}
.ar-open {
  font-size: 10px; font-weight: 600; color: #10b981;
  background: #d1fae5; padding: 1px 7px; border-radius: 999px;
}
.ar-btn {
  flex-shrink: 0; padding: 4px 12px;
  background: linear-gradient(135deg, #ec4899, #f97316);
  color: #fff; border: none; border-radius: 999px;
  font-size: 11px; font-weight: 700; cursor: pointer;
  transition: all 0.2s ease; box-shadow: 0 2px 8px rgba(236,72,153,0.25);
}
.ar-btn:hover { transform: scale(1.06); box-shadow: 0 4px 14px rgba(236,72,153,0.4); }

/* 社区脉动 */
.community-widget { background: linear-gradient(135deg, #f0f9ff 0%, #f5f3ff 100%); }
.community-pulse { display: flex; flex-direction: column; gap: 10px; margin-bottom: 14px; }
.pulse-item { display: flex; align-items: center; gap: 12px; }
.pulse-icon {
  width: 36px; height: 36px; border-radius: 10px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px;
}
.pulse-content { flex: 1; min-width: 0; }
.pulse-text { font-size: 12px; color: var(--px-text-tertiary); margin-bottom: 1px; }
.pulse-val { font-size: 15px; font-weight: 800; line-height: 1; }
.pulse-cta {
  display: block; text-align: center; padding: 9px 0;
  background: rgba(99,102,241,0.08); border-radius: 8px;
  font-size: 13px; font-weight: 700; color: #6366f1;
  text-decoration: none; transition: all 0.18s ease;
  border: 1px solid rgba(99,102,241,0.15);
}
.pulse-cta:hover { background: #6366f1; color: #fff; border-color: #6366f1; }

/* 约稿入口卡片 */
.commission-entry {
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4c1d95 100%) !important;
  border-color: transparent !important;
  padding: 24px 18px !important;
}
.ce-bg {
  position: absolute; inset: 0; opacity: 0.18;
  background: radial-gradient(circle at 80% 20%, #a78bfa 0%, transparent 60%),
              radial-gradient(circle at 20% 80%, #60a5fa 0%, transparent 60%);
}
.ce-content { position: relative; z-index: 1; }
.ce-title {
  font-size: 17px; font-weight: 900; color: #fff;
  margin-bottom: 8px; letter-spacing: -0.3px;
}
.ce-sub {
  font-size: 12px; color: rgba(255,255,255,0.65);
  line-height: 1.7; margin-bottom: 18px;
}
.ce-btn {
  display: inline-block; padding: 9px 20px;
  background: linear-gradient(135deg, #ec4899, #f97316);
  color: #fff; border-radius: 999px; font-size: 13px;
  font-weight: 700; text-decoration: none;
  box-shadow: 0 4px 16px rgba(236,72,153,0.45);
  transition: all 0.22s ease;
}
.ce-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(236,72,153,0.6); }

/* ===================== 页脚 ===================== */
.px-footer { border-top: 1px solid var(--px-border-light); background: var(--px-bg-primary); padding: 24px 0; margin-top: 40px; }
.footer-inner {
  max-width: var(--px-max-width); margin: 0 auto; padding: 0 32px;
  display: flex; align-items: center; justify-content: space-between;
}
.footer-logo {
  font-family: Arial, sans-serif; font-weight: 900; font-size: 20px;
  color: var(--px-blue); font-style: italic; letter-spacing: -1px;
}
.footer-copy { font-size: 12px; color: var(--px-text-placeholder); }

/* ===================== 响应式 ===================== */
@media (max-width: 1100px) {
  .content-shell { grid-template-columns: 1fr 260px; gap: 20px; }
  .masonry-grid { column-count: 3; }
}
@media (max-width: 900px) {
  .content-shell { grid-template-columns: 1fr; }
  .content-sidebar { position: static; }
  .masonry-grid { column-count: 3; }
  .artists-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .hero-section { height: auto; min-height: 420px; }
  .hero-body { flex-direction: column; padding: 48px 24px 56px; gap: 28px; }
  .hero-thumbs { flex-direction: row; }
  .hero-thumb { width: 72px; height: 52px; }
  .masonry-grid { column-count: 2; }
  .content-shell { padding: 0 16px; }
  .block-inner { padding: 0 16px; }
  .qnav-inner { padding: 0 16px; }
}
@media (max-width: 480px) {
  .hero-brand { font-size: 40px; }
  .hero-tagline { font-size: 15px; }
  .masonry-grid { column-count: 2; column-gap: 10px; }
  .footer-inner { flex-direction: column; gap: 8px; text-align: center; }
}
</style>   