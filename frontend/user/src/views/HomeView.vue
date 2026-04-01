<template>
  <div class="home-page">

    <!-- ===== Hero: 左文字 + 右轮播 斜切分割 ===== -->
    <section class="hero-section">
      <!-- 右侧轮播区（绝对定位，充满右侧） -->
      <div class="hero-carousel" v-if="featuredArtworks.length">
        <div class="carousel-viewport">
          <div
            v-for="(art, i) in featuredArtworks.slice(0, 5)"
            :key="art.id || i"
            class="carousel-slide"
            :class="{ active: heroIndex === i }"
            @click="goToDetail(art.id)"
          >
            <img :src="art.imageUrl || art.thumbnailUrl" :alt="art.title" loading="eager" />
            <div class="carousel-caption">
              <span class="carousel-title">{{ art.title }}</span>
              <span class="carousel-artist" v-if="art.artistName">by {{ art.artistName }}</span>
            </div>
          </div>
        </div>
        <button class="carousel-arrow carousel-prev" @click="heroPrev" aria-label="上一张">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </button>
        <button class="carousel-arrow carousel-next" @click="heroNext" aria-label="下一张">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>
        </button>
        <div class="carousel-dots">
          <button
            v-for="(_, i) in featuredArtworks.slice(0, 5)"
            :key="i"
            class="carousel-dot"
            :class="{ active: heroIndex === i }"
            @click="heroIndex = i"
          ></button>
        </div>
      </div>
      <!-- 左侧文字区（z-index 在深色斜切上方） -->
      <div class="hero-inner">
        <div class="hero-text">
          <h1 class="hero-title">幻画空间</h1>
          <p class="hero-subtitle">发现好作品，连接创作者</p>
          <p class="hero-desc">百万插画 · 创意约稿 · 社区互动<br/>属于每一个创作者的舞台</p>
          <div class="hero-stats">
            <div class="hs-item">
              <span class="hs-num">10万+</span>
              <span class="hs-label">作品总数</span>
            </div>
            <div class="hs-divider"></div>
            <div class="hs-item">
              <span class="hs-num">5万+</span>
              <span class="hs-label">创意画师</span>
            </div>
            <div class="hs-divider"></div>
            <div class="hs-item">
              <span class="hs-num">1万+</span>
              <span class="hs-label">约稿成功</span>
            </div>
          </div>
          <div class="hero-btns">
            <router-link to="/artworks" class="hero-btn-primary">探索作品</router-link>
            <router-link to="/commissions" class="hero-btn-secondary">发布约稿</router-link>
          </div>
        </div>
      </div>
      <!-- 斜切装饰 -->
      <div class="hero-diagonal"></div>
    </section>

    <!-- ===== 快捷导航 ===== -->
    <section class="platform-overview">
      <div class="overview-inner">
        <nav class="quick-nav">
          <router-link to="/artworks" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#60A5FA,#3B82F6)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
            </div>
            <span class="qnav-label">最新作品</span>
            <span class="qnav-desc">发现新灵感</span>
          </router-link>
          <router-link to="/ranking" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#FBBF24,#F59E0B)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            </div>
            <span class="qnav-label">排行榜</span>
            <span class="qnav-desc">热门作品榜</span>
          </router-link>
          <router-link to="/contests" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#F472B6,#EC4899)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M6 9H4.5a2.5 2.5 0 010-5H6"/><path d="M18 9h1.5a2.5 2.5 0 000-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0012 0V2z"/></svg>
            </div>
            <span class="qnav-label">比赛专区</span>
            <span class="qnav-desc">参赛赢大奖</span>
          </router-link>
          <router-link to="/following" class="qnav-card" v-if="userStore.isAuthenticated">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#34D399,#10B981)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            </div>
            <span class="qnav-label">关注动态</span>
            <span class="qnav-desc">画师新作品</span>
          </router-link>
          <router-link to="/commissions" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#A78BFA,#8B5CF6)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
            </div>
            <span class="qnav-label">约稿大厅</span>
            <span class="qnav-desc">定制你的专属</span>
          </router-link>
          <router-link :to="{ path: '/artworks', query: { sortBy: 'hottest' } }" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#FB923C,#F97316)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
            </div>
            <span class="qnav-label">热门推荐</span>
            <span class="qnav-desc">大家都在看</span>
          </router-link>
          <router-link v-if="userStore.isAuthenticated" to="/chat" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#6EE7B7,#059669)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <span class="qnav-label">我的私信</span>
            <span class="qnav-desc">实时聊天</span>
          </router-link>
          <router-link to="/membership" class="qnav-card">
            <div class="qnav-icon-wrap" style="background:linear-gradient(135deg,#FDA4AF,#E11D48)">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#fff" stroke-width="1.8"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </div>
            <span class="qnav-label">会员中心</span>
            <span class="qnav-desc">签到领福利</span>
          </router-link>
        </nav>
      </div>
    </section>

    <!-- ===== 推荐作品 ===== -->
    <section class="section-block">
      <div class="section-inner">
        <div class="section-head">
          <h2 class="section-title">推荐作品</h2>
          <div class="sort-tabs">
            <button
              v-for="tab in sortTabs"
              :key="tab.value"
              class="sort-tab"
              :class="{ active: activeSortTab === tab.value }"
              @click="switchSort(tab.value)"
            >{{ tab.label }}</button>
          </div>
          <router-link to="/artworks" class="section-more">
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
                  <button class="mi-btn" @click.stop="onLike(artwork)" aria-label="点赞">
                    <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                    {{ formatCount(artwork.likeCount) }}
                  </button>
                  <button class="mi-btn" @click.stop="onFavorite(artwork)" aria-label="收藏">
                    <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
                  </button>
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

        <div v-else class="empty-block">
          <svg viewBox="0 0 120 80" width="120" height="80">
            <rect x="10" y="10" width="100" height="60" rx="6" fill="none" stroke="#ddd" stroke-width="1.5"/>
            <circle cx="38" cy="35" r="8" fill="none" stroke="#ddd" stroke-width="1.5"/>
            <path d="M10 55 l28-18 l16 12 l20-14 l46 15" fill="none" stroke="#ddd" stroke-width="1.5"/>
          </svg>
          <p>暂无推荐作品</p>
        </div>
      </div>
    </section>

    <!-- ===== 比赛精选 ===== -->
    <section v-if="contestEntries.length > 0" class="section-block">
      <div class="section-inner">
        <div class="section-head">
          <h2 class="section-title">比赛精选</h2>
          <router-link to="/contests" class="section-more">
            查看全部
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
          </router-link>
        </div>
        <div class="contest-grid">
          <div
            v-for="entry in contestEntries"
            :key="entry.id"
            class="contest-card"
            @click="goToContestEntry(entry)"
          >
            <div class="cc-thumb">
              <img :src="entry.imageUrl" :alt="entry.title" />
              <span class="cc-tag">参赛作品</span>
              <span v-if="entry.averageScore > 0" class="cc-score">
                <svg viewBox="0 0 24 24" width="11" height="11" fill="#ffc040"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                {{ entry.averageScore.toFixed(1) }}
              </span>
            </div>
            <div class="cc-info">
              <p class="cc-title">{{ entry.title }}</p>
              <p class="cc-meta">
                <span class="cc-artist">{{ entry.artistName }}</span>
                <span class="cc-contest">{{ entry.contestTitle }}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 约稿大厅 ===== -->
    <section class="section-block">
      <div class="section-inner">
        <div class="section-head">
          <h2 class="section-title">约稿大厅</h2>
          <router-link to="/commissions" class="section-more">
            浏览更多
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
          </router-link>
        </div>
        <div class="commission-layout">
          <div class="commission-list">
            <div v-for="artist in displayArtists.slice(0, 4)" :key="'comm-'+artist.artistId" class="commission-card" @click="goToArtist(artist.artistId)">
              <div class="comm-cover">
                <img :src="artist.thumbnailUrl || artist.imageUrl" :alt="artist.artistName" />
              </div>
              <div class="comm-body">
                <p class="comm-title">{{ artist.artistName }} 的约稿企划</p>
                <p class="comm-desc">提供高质量插画定制服务，风格多样，欢迎咨询</p>
                <div class="comm-footer">
                  <span class="comm-status status-open">进行中</span>
                  <span class="comm-price">预算面议</span>
                </div>
              </div>
            </div>
            <div v-if="displayArtists.length === 0" class="empty-block" style="padding:32px 0">
              <p>暂无约稿企划</p>
            </div>
          </div>
          <!-- 画师推荐侧栏 -->
          <div class="commission-sidebar">
            <h3 class="sidebar-title">推荐画师</h3>
            <div class="artist-recommend-list">
              <div
                v-for="artist in displayArtists.slice(0, 6)"
                :key="'rec-'+artist.artistId"
                class="artist-rec-item"
                @click="goToArtist(artist.artistId)"
              >
                <el-avatar :size="36" :src="artist.artistAvatar || undefined" class="rec-avatar">{{ artist.artistName?.charAt(0) }}</el-avatar>
                <div class="rec-info">
                  <p class="rec-name">{{ artist.artistName }}</p>
                  <span class="rec-status status-open">开放中</span>
                </div>
                <button class="rec-btn" @click.stop="$router.push(`/commission/create/${artist.artistId}`)">约稿</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 社区热榜 ===== -->
    <section class="section-block">
      <div class="section-inner">
        <div class="section-head">
          <h2 class="section-title">社区热榜</h2>
          <router-link to="/ranking" class="section-more">
            完整榜单
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
          </router-link>
        </div>
        <div class="ranking-grid">
          <!-- 今日作品榜 -->
          <div class="ranking-panel">
            <h3 class="ranking-panel-title">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
              今日作品
            </h3>
            <div class="ranking-list">
              <div v-for="(aw, idx) in artworks.slice(0, 5)" :key="'rank-aw-'+aw.id" class="ranking-item" @click="goToDetail(aw.id)">
                <span class="rank-num" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
                <img :src="aw.thumbnailUrl || aw.imageUrl" :alt="aw.title" class="rank-thumb" />
                <div class="rank-info">
                  <p class="rank-title">{{ aw.title }}</p>
                  <p class="rank-artist">{{ aw.artistName }}</p>
                </div>
                <span class="rank-likes">{{ formatCount(aw.likeCount) }} 赞</span>
              </div>
            </div>
          </div>
          <!-- 画师榜 -->
          <div class="ranking-panel">
            <h3 class="ranking-panel-title">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
              人气画师
            </h3>
            <div class="ranking-list">
              <div v-for="(artist, idx) in displayArtists.slice(0, 5)" :key="'rank-ar-'+artist.artistId" class="ranking-item" @click="goToArtist(artist.artistId)">
                <span class="rank-num" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
                <el-avatar :size="32" :src="artist.artistAvatar || undefined" class="rank-avatar">{{ artist.artistName?.charAt(0) }}</el-avatar>
                <div class="rank-info">
                  <p class="rank-title">{{ artist.artistName }}</p>
                  <p class="rank-artist">{{ formatCount(artist.viewCount) }} 浏览</p>
                </div>
              </div>
            </div>
          </div>
          <!-- 热门标签 -->
          <div class="ranking-panel">
            <h3 class="ranking-panel-title">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
              热门标签
            </h3>
            <div class="tag-cloud">
              <button
                v-for="tag in trendingTags.slice(0, 15)"
                :key="tag.name"
                class="tag-chip"
                @click="$router.push({ name: 'Artworks', query: { tag: tag.name } })"
              >
                #{{ tag.name }}
                <span class="tag-count">{{ formatCount(tag.count) }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 底部栏 ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/>
          </svg>
          <span>幻画空间</span>
        </div>
        <div class="footer-links">
          <a href="javascript:void(0)">关于我们</a>
          <a href="javascript:void(0)">用户协议</a>
          <a href="javascript:void(0)">隐私政策</a>
          <a href="javascript:void(0)">帮助中心</a>
          <a href="javascript:void(0)">联系我们</a>
        </div>
        <p class="footer-copy">&copy; 2026 幻画空间 版权所有 · 仅供学习交流使用</p>
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

const artworks = ref([])
const featuredArtworks = ref([])
const contestEntries = ref([])
const loading = ref(true)

const heroIndex = ref(0)
let heroTimer = null

const sortTabs = [
  { label: '热门', value: 'hottest' },
  { label: '最新', value: 'latest' },
  { label: '最多收藏', value: 'most_favorited' },
  { label: '最多点赞', value: 'most_liked' },
]
const activeSortTab = ref('hottest')

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

const displayArtists = computed(() => {
  const seen = new Set()
  return artworks.value
    .filter(aw => aw.artistId && !seen.has(aw.artistId) && seen.add(aw.artistId))
    .slice(0, 8)
})

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

async function onLike(artwork) {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await likeArtwork(artwork.id)
    artwork.likeCount = (artwork.likeCount || 0) + 1
  } catch (e) { /* ignore */ }
}

async function onFavorite(artwork) {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await favoriteArtwork(artwork.id)
    ElMessage.success('已收藏')
  } catch (e) { /* ignore */ }
}

function switchSort(val) {
  activeSortTab.value = val
  loadArtworks()
}

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

function heroPrev() {
  const len = Math.min(5, featuredArtworks.value.length)
  if (len) heroIndex.value = (heroIndex.value - 1 + len) % len
}

function heroNext() {
  const len = Math.min(5, featuredArtworks.value.length)
  if (len) heroIndex.value = (heroIndex.value + 1) % len
}

function formatCount(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + '千'
  return String(num)
}

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
   首页 — 清爽清新 ACG 风格
   ============================================================ */
.home-page {
  min-height: 100vh;
  background: #FFFFFF;
}

/* ===================== Hero 斜切分割布局 ===================== */
.hero-section {
  position: relative;
  width: 100%;
  height: 480px;
  overflow: hidden;
}
.hero-inner {
  position: relative;
  z-index: 2;
  max-width: var(--px-max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 48px;
  gap: 0;
}
/* 左侧文字 — 深色背景通过伪元素实现斜切 */
.hero-text {
  flex: 0 0 440px;
  color: #fff;
  position: relative;
  z-index: 3;
  padding: 48px 48px 48px 0;
}
.hero-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 8px;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #fff 0%, #93c5fd 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.hero-subtitle {
  font-size: 16px;
  color: rgba(255,255,255,0.85);
  margin: 0 0 16px;
  font-weight: 500;
}
.hero-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.55);
  margin: 0 0 24px;
  line-height: 1.8;
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 28px;
}
.hs-item { text-align: center; }
.hs-num {
  display: block;
  font-size: 24px;
  font-weight: 800;
  color: #60a5fa;
}
.hs-label {
  display: block;
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  margin-top: 2px;
}
.hs-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.15);
}
.hero-btns {
  display: flex;
  gap: 12px;
}
.hero-btn-primary {
  padding: 10px 28px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s;
  box-shadow: 0 4px 16px rgba(59,130,246,0.3);
}
.hero-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(59,130,246,0.45);
}
.hero-btn-secondary {
  padding: 10px 28px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.25);
  color: #fff;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s;
  backdrop-filter: blur(4px);
}
.hero-btn-secondary:hover {
  background: rgba(255,255,255,0.2);
  border-color: rgba(255,255,255,0.4);
}

/* 左侧深色背景斜切 */
.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 55%;
  height: 100%;
  background: linear-gradient(135deg, #1e293b 0%, #334155 60%, #0f172a 100%);
  clip-path: polygon(0 0, 100% 0, 82% 100%, 0 100%);
  z-index: 1;
}

/* 右侧轮播 — 充满右侧 */
.hero-carousel {
  position: absolute;
  top: 0;
  right: 0;
  width: 58%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  background: #1a1a2e;
}
.carousel-viewport {
  position: absolute;
  inset: 0;
}
.carousel-slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.8s ease;
  cursor: pointer;
}
.carousel-slide.active { opacity: 1; }
.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 36px 24px 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 100%);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.carousel-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0,0,0,0.4);
}
.carousel-artist {
  font-size: 12px;
  color: rgba(255,255,255,0.75);
}
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255,255,255,0.2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.2s;
  z-index: 2;
}
.carousel-arrow:hover { background: rgba(255,255,255,0.3); }
.carousel-prev { left: 12px; }
.carousel-next { right: 12px; }
.carousel-dots {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 2;
}
.carousel-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.35);
  cursor: pointer;
  transition: all 0.3s;
  padding: 0;
}
.carousel-dot.active {
  background: #fff;
  width: 22px;
  border-radius: 4px;
}

/* 斜切装饰 — 底部白色过渡 */
.hero-diagonal {
  display: none;
}

/* ===================== 快捷导航 ===================== */
.platform-overview {
  background: #fff;
  padding: 28px 0 20px;
  border-bottom: 1px solid #f0f0f0;
}
.overview-inner {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 32px;
}
/* 快捷入口 — 卡片式 */
.quick-nav {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
}
.qnav-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 8px 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  text-decoration: none;
  transition: all 0.25s ease;
  cursor: pointer;
}
.qnav-card:hover {
  background: #fff;
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.08);
  border-color: transparent;
}
.qnav-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.qnav-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-text-primary);
  white-space: nowrap;
}
.qnav-desc {
  font-size: 11px;
  color: var(--px-text-placeholder);
  white-space: nowrap;
}

/* ===================== 通用区块 ===================== */
.section-block {
  padding: 40px 0;
}
.section-block + .section-block {
  border-top: 1px solid #f5f5f5;
}
.section-inner {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 32px;
}
.section-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--px-text-primary);
}
.section-more {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--px-text-tertiary);
  text-decoration: none;
  transition: color 0.2s;
}
.section-more:hover {
  color: var(--px-blue);
}

/* 排序标签 */
.sort-tabs {
  display: flex;
  gap: 4px;
  background: #F7F8FA;
  border-radius: 20px;
  padding: 3px;
}
.sort-tab {
  padding: 6px 16px;
  border: none;
  background: transparent;
  border-radius: 18px;
  font-size: 13px;
  color: var(--px-text-tertiary);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}
.sort-tab.active {
  background: #fff;
  color: var(--px-blue);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
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
  color: var(--px-text-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
}
.mi-btn:hover {
  background: #fff;
  color: var(--px-like);
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
  color: var(--px-text-primary);
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
  color: var(--px-text-tertiary);
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
  color: var(--px-text-placeholder);
}

/* 骨架屏 */
.skel-card {
  break-inside: avoid;
  margin-bottom: 16px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
}
.skel-thumb {
  background: linear-gradient(110deg, #f0f0f0 30%, #fafafa 50%, #f0f0f0 70%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skel-body {
  padding: 14px;
}
.skel-line {
  height: 10px;
  border-radius: 5px;
  background: #f0f0f0;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-block {
  text-align: center;
  padding: 48px 0;
  color: var(--px-text-placeholder);
}
.empty-block p {
  margin-top: 12px;
  font-size: 14px;
}

/* ===================== 比赛精选 ===================== */
.contest-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.contest-card {
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.25s ease;
}
.contest-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}
.cc-thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
}
.cc-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.cc-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);
  color: var(--px-blue);
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}
.cc-score {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 3px;
}
.cc-info {
  padding: 12px 14px;
}
.cc-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cc-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--px-text-tertiary);
}
.cc-artist {
  font-weight: 500;
}
.cc-contest {
  opacity: 0.7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===================== 约稿大厅 ===================== */
.commission-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 24px;
}
.commission-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.commission-card {
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.25s ease;
}
.commission-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}
.comm-cover {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}
.comm-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.comm-body {
  padding: 14px 16px;
}
.comm-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--px-text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.comm-desc {
  font-size: 12px;
  color: var(--px-text-tertiary);
  margin-bottom: 10px;
  line-height: 1.5;
}
.comm-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.comm-status {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
}
.status-open {
  background: #ECFDF5;
  color: #059669;
}
.comm-price {
  font-size: 12px;
  color: var(--px-text-tertiary);
}

/* 画师推荐侧栏 */
.commission-sidebar {
  background: #F9FAFB;
  border-radius: 20px;
  padding: 20px;
}
.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--px-text-primary);
  margin-bottom: 16px;
}
.artist-recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.artist-rec-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px;
  border-radius: 14px;
  transition: background 0.15s ease;
}
.artist-rec-item:hover {
  background: #fff;
}
.rec-avatar {
  flex-shrink: 0;
}
.rec-info {
  flex: 1;
  min-width: 0;
}
.rec-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-status {
  font-size: 11px;
  font-weight: 500;
}
.rec-btn {
  padding: 5px 14px;
  border: 1.5px solid var(--px-blue);
  background: transparent;
  color: var(--px-blue);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.rec-btn:hover {
  background: var(--px-blue);
  color: #fff;
}

/* ===================== 社区热榜 ===================== */
.ranking-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 280px;
  gap: 24px;
}
.ranking-panel {
  background: #F9FAFB;
  border-radius: 20px;
  padding: 20px;
}
.ranking-panel-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--px-text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.15s ease;
}
.ranking-item:hover {
  background: #fff;
}
.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #E5E7EB;
  color: var(--px-text-tertiary);
  flex-shrink: 0;
}
.rank-num.top {
  background: linear-gradient(135deg, #60A5FA, #818CF8);
  color: #fff;
}
.rank-thumb {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}
.rank-info {
  flex: 1;
  min-width: 0;
}
.rank-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-artist {
  font-size: 11px;
  color: var(--px-text-tertiary);
}
.rank-likes {
  font-size: 12px;
  font-weight: 600;
  color: var(--px-blue);
  flex-shrink: 0;
}

/* 标签云 */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 999px;
  font-size: 12px;
  color: var(--px-text-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
}
.tag-chip:hover {
  border-color: var(--px-blue);
  color: var(--px-blue);
  background: var(--px-blue-light);
}
.tag-count {
  font-size: 10px;
  color: var(--px-text-placeholder);
  margin-left: 2px;
}

/* ===================== 底部栏 ===================== */
.site-footer {
  background: #F9FAFB;
  border-top: 1px solid #f0f0f0;
  padding: 40px 0 32px;
  margin-top: 20px;
}
.footer-inner {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 32px;
  text-align: center;
}
.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  color: var(--px-text-primary);
  margin-bottom: 16px;
}
.footer-brand svg {
  color: var(--px-blue);
}
.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}
.footer-links a {
  font-size: 13px;
  color: var(--px-text-tertiary);
  text-decoration: none;
  transition: color 0.15s ease;
}
.footer-links a:hover {
  color: var(--px-blue);
}
.footer-copy {
  font-size: 12px;
  color: var(--px-text-placeholder);
}

/* ===================== 响应式 ===================== */
@media (max-width: 1100px) {
  .hero-inner { padding: 0 32px; }
  .hero-text { flex: 0 0 340px; }
  .hero-section::before { width: 50%; }
  .hero-carousel { width: 60%; }
  .masonry-grid { column-count: 3; }
  .contest-grid { grid-template-columns: repeat(3, 1fr); }
  .commission-layout { grid-template-columns: 1fr 240px; }
  .ranking-grid { grid-template-columns: 1fr 1fr; }
  .ranking-grid > .ranking-panel:last-child { display: none; }
  .quick-nav { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 900px) {
  .hero-section { height: 520px; }
  .hero-section::before { width: 100%; height: 55%; clip-path: polygon(0 0, 100% 0, 100% 85%, 0 100%); }
  .hero-carousel { width: 100%; height: 50%; top: auto; bottom: 0; }
  .hero-inner { flex-direction: column; text-align: center; padding: 32px 24px; height: auto; padding-top: 40px; }
  .hero-text { flex: none; width: 100%; padding: 0; }
  .hero-stats { justify-content: center; }
  .hero-btns { justify-content: center; }
  .masonry-grid { column-count: 3; }
  .contest-grid { grid-template-columns: repeat(2, 1fr); }
  .commission-layout { grid-template-columns: 1fr; }
  .commission-sidebar { display: none; }
  .ranking-grid { grid-template-columns: 1fr; }
  .quick-nav { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .hero-section { height: 480px; }
  .hero-title { font-size: 28px; }
  .quick-nav { grid-template-columns: repeat(4, 1fr); gap: 8px; }
  .qnav-card { padding: 12px 4px 10px; }
  .masonry-grid { column-count: 2; }
  .contest-grid { grid-template-columns: repeat(2, 1fr); }
  .commission-list { grid-template-columns: 1fr; }
  .section-inner { padding: 0 16px; }
  .overview-inner { padding: 0 16px; }
}
@media (max-width: 480px) {
  .hero-section { height: 420px; }
  .hero-inner { padding: 24px 16px; }
  .hero-title { font-size: 24px; }
  .hero-subtitle { font-size: 14px; }
  .carousel-arrow { display: none; }
  .masonry-grid { column-count: 2; column-gap: 10px; }
  .contest-grid { grid-template-columns: 1fr; }
  .footer-links { flex-wrap: wrap; gap: 12px; }
  .quick-nav { grid-template-columns: repeat(4, 1fr); gap: 6px; }
  .qnav-icon-wrap { width: 36px; height: 36px; border-radius: 10px; }
  .qnav-icon-wrap svg { width: 18px; height: 18px; }
}
</style>