<template>
  <div class="artist-profile-page">
    <!-- 加载 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <!-- 用户不存在 -->
    <div v-else-if="!artist" class="error-state">
      <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.5">
        <circle cx="60" cy="45" r="20" />
        <path d="M30 95 c0-16 13-28 30-28 s30 12 30 28" />
      </svg>
      <p class="error-text">用户不存在</p>
      <button class="back-btn" @click="$router.back()">返回</button>
    </div>

    <template v-else>
      <!-- Banner 背景 -->
      <div class="profile-banner">
        <div class="banner-pattern"></div>
        <div class="banner-fade"></div>
      </div>

      <!-- 主内容区 -->
      <div class="profile-main">
        <!-- 用户卡片 -->
        <div class="user-card">
          <!-- 头像 -->
          <div class="avatar-area">
            <div class="avatar-ring">
              <img :src="artist.avatarUrl || defaultAvatar" alt="avatar" class="avatar-img" />
            </div>
            <div v-if="artist.role === 'ARTIST'" class="role-tag artist">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
              认证画师
            </div>
            <div v-else class="role-tag user">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
              普通用户
            </div>
          </div>

          <!-- 信息区 -->
          <div class="info-area">
            <div class="name-row">
              <h1 class="display-name">{{ artist.username }}</h1>
            </div>

            <p class="bio-text" :class="{ empty: !artist.bio }">
              {{ artist.bio || '这个人很懒，还没有写个人简介...' }}
            </p>

            <!-- 数据统计 -->
            <div class="stat-strip">
              <div v-if="artist.role === 'ARTIST'" class="stat-chip">
                <span class="stat-num">{{ artist.artworkCount || 0 }}</span>
                <span class="stat-lbl">作品</span>
              </div>
              <div v-if="artist.role === 'ARTIST'" class="stat-chip">
                <span class="stat-num">{{ artist.followerCount || 0 }}</span>
                <span class="stat-lbl">粉丝</span>
              </div>
              <div v-if="!artist.hideFollowing" class="stat-chip">
                <span class="stat-num">{{ artist.followingCount || 0 }}</span>
                <span class="stat-lbl">关注</span>
              </div>
            </div>

            <!-- 操作按钮（非本人才显示） -->
            <div class="action-row" v-if="!isSelf">
              <button
                class="follow-btn"
                :class="{ following: isFollowing }"
                @click="handleFollow"
                v-if="userStore.isAuthenticated"
              >
                <svg v-if="!isFollowing" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                </svg>
                {{ isFollowing ? '已关注' : '关注' }}
              </button>
              <button
                class="message-btn"
                @click="handleMessage"
                v-if="userStore.isAuthenticated"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                </svg>
                私信
              </button>
              <button
                class="commission-btn"
                @click="$router.push({ name: 'CreateCommission', query: { artistId: artist.id } })"
                v-if="userStore.isAuthenticated && artist.role === 'ARTIST'"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                </svg>
                发起约稿
              </button>
            </div>
            <!-- 自己的主页 -->
            <div class="action-row" v-else>
              <button class="edit-btn" @click="$router.push('/profile')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
                编辑资料
              </button>
            </div>
          </div>
        </div>

        <!-- Tab 区域 -->
        <div class="tabs-section">
          <div class="tabs-nav">
            <button
              v-if="artist.role === 'ARTIST'"
              class="tab-item"
              :class="{ active: activeTab === 'artworks' }"
              @click="switchTab('artworks')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
              作品
            </button>
            <button
              v-if="!artist.hideFavorites || isSelf"
              class="tab-item"
              :class="{ active: activeTab === 'favorites' }"
              @click="switchTab('favorites')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              收藏
            </button>
            <button
              v-if="!artist.hideFollowing || isSelf"
              class="tab-item"
              :class="{ active: activeTab === 'following' }"
              @click="switchTab('following')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
              关注
            </button>
          </div>

          <!-- 作品 Tab -->
          <div v-show="activeTab === 'artworks'" class="tab-panel">
            <div v-if="artworks.length > 0" class="artwork-grid">
              <ArtworkCard
                v-for="art in artworks"
                :key="art.id"
                :artwork="art"
                @click="goToArtwork(art.id)"
                @author-click="() => {}"
              />
            </div>
            <div v-else-if="!loadingArtworks" class="empty-state">
              <svg viewBox="0 0 120 120" width="64" height="64" fill="none" stroke="#d0d0d0" stroke-width="1.5">
                <rect x="20" y="20" width="80" height="80" rx="8" />
                <circle cx="45" cy="48" r="8" />
                <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
              </svg>
              <p>暂无作品</p>
            </div>
            <div v-if="loadingArtworks" class="tab-loading">
              <div class="spinner small"></div>
            </div>
            <!-- 分页 -->
            <div v-if="artworkTotal > artworkPageSize" class="pager">
              <el-pagination
                v-model:current-page="artworkPage"
                :page-size="artworkPageSize"
                :total="artworkTotal"
                layout="prev, pager, next"
                @current-change="loadArtworks"
              />
            </div>
          </div>

          <!-- 收藏 Tab（隐私受限） -->
          <div v-show="activeTab === 'favorites'" class="tab-panel">
            <div v-if="artist.hideFavorites && !isSelf" class="privacy-hint">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="#ccc">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
              </svg>
              <p>该用户已隐藏收藏列表</p>
            </div>
            <template v-else>
              <div v-if="favorites.length > 0" class="artwork-grid">
                <ArtworkCard
                  v-for="art in favorites"
                  :key="art.id"
                  :artwork="art"
                  @click="goToArtwork(art.id)"
                  @author-click="(a) => goToArtist(a.artistId)"
                />
              </div>
              <div v-else-if="!loadingFavorites" class="empty-state">
                <p>暂无收藏</p>
              </div>
              <div v-if="loadingFavorites" class="tab-loading">
                <div class="spinner small"></div>
              </div>
            </template>
          </div>

          <!-- 关注 Tab（隐私受限） -->
          <div v-show="activeTab === 'following'" class="tab-panel">
            <div v-if="artist.hideFollowing && !isSelf" class="privacy-hint">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="#ccc">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
              </svg>
              <p>该用户已隐藏关注列表</p>
            </div>
            <template v-else>
              <div v-if="followingList.length > 0" class="following-grid">
                <div
                  v-for="user in followingList"
                  :key="user.id"
                  class="following-card"
                  @click="goToArtist(user.id)"
                >
                  <el-avatar :size="56" :src="user.avatarUrl || defaultAvatar">
                    {{ user.username?.charAt(0) }}
                  </el-avatar>
                  <span class="following-name">{{ user.username }}</span>
                  <span class="following-meta">{{ user.artworkCount || 0 }} 作品 · {{ user.followerCount || 0 }} 粉丝</span>
                </div>
              </div>
              <div v-else-if="!loadingFollowing" class="empty-state">
                <p>暂无关注</p>
              </div>
              <div v-if="loadingFollowing" class="tab-loading">
                <div class="spinner small"></div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/api/user'
import { getArtworks, getUserFavorites } from '@/api/artwork'
import { followArtist, unfollowArtist, checkFollowStatus, getUserFollowing } from '@/api/follow'
import { createOrGetConversation } from '@/api/chat'
import { ElMessage } from 'element-plus'
import ArtworkCard from '@/components/ArtworkCard.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 画师信息
const artist = ref(null)
const loading = ref(true)
const isFollowing = ref(false)
const activeTab = ref('artworks')

// 是否自己的主页
const isSelf = computed(() => {
  return userStore.isAuthenticated && userStore.user?.id == route.params.id
})

// 作品 tab
const artworks = ref([])
const artworkPage = ref(1)
const artworkPageSize = ref(20)
const artworkTotal = ref(0)
const loadingArtworks = ref(false)

// 收藏 tab
const favorites = ref([])
const loadingFavorites = ref(false)

// 关注 tab
const followingList = ref([])
const loadingFollowing = ref(false)

// 加载画师主页信息
async function loadArtistProfile() {
  loading.value = true
  try {
    const res = await getUserProfile(route.params.id)
    if (res.code === 200 && res.data) {
      artist.value = res.data
      // 默认tab: 画师显示作品，普通用户且未隐藏收藏才显示收藏
      if (artist.value.role === 'ARTIST') {
        activeTab.value = 'artworks'
        loadArtworks()
      } else if (!artist.value.hideFavorites || isSelf.value) {
        activeTab.value = 'favorites'
        loadFavorites()
      } else if (!artist.value.hideFollowing || isSelf.value) {
        activeTab.value = 'following'
        loadFollowing()
      }
      // 检查关注状态
      if (userStore.isAuthenticated && !isSelf.value) {
        checkFollow()
      }
    } else {
      artist.value = null
    }
  } catch (err) {
    console.error('加载用户信息失败:', err)
    artist.value = null
  } finally {
    loading.value = false
  }
}

// 加载作品列表
async function loadArtworks() {
  loadingArtworks.value = true
  try {
    const res = await getArtworks({
      artistId: route.params.id,
      page: artworkPage.value,
      size: artworkPageSize.value,
      sortBy: 'latest'
    })
    if (res.code === 200 && res.data) {
      artworks.value = res.data.records || res.data.items || []
      artworkTotal.value = res.data.total || 0
    }
  } catch (err) {
    console.error('加载作品失败:', err)
  } finally {
    loadingArtworks.value = false
  }
}

// 加载收藏（注意：这里需要目标用户的收藏，但当前API只支持当前用户）
// 如果是自己的主页，可以用 getUserFavorites
// 如果不是自己的，暂不支持（需要后端支持）
async function loadFavorites() {
  if (!isSelf.value) {
    // 非本人暂无对应API，显示为空
    favorites.value = []
    return
  }
  loadingFavorites.value = true
  try {
    const res = await getUserFavorites({ page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      favorites.value = res.data.records || res.data.items || []
    }
  } catch (err) {
    console.error('加载收藏失败:', err)
  } finally {
    loadingFavorites.value = false
  }
}

// 加载关注列表
async function loadFollowing() {
  loadingFollowing.value = true
  try {
    const res = await getUserFollowing(route.params.id, { page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      followingList.value = res.data.records || res.data.items || res.data || []
    }
  } catch (err) {
    console.error('加载关注失败:', err)
  } finally {
    loadingFollowing.value = false
  }
}

// 切换tab
function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'artworks' && artworks.value.length === 0) loadArtworks()
  if (tab === 'favorites' && favorites.value.length === 0) loadFavorites()
  if (tab === 'following' && followingList.value.length === 0) loadFollowing()
}

// 检查关注状态
async function checkFollow() {
  try {
    const res = await checkFollowStatus(route.params.id)
    if (res.code === 200) {
      isFollowing.value = res.data?.following || res.data === true
    }
  } catch {}
}

// 关注/取关
async function handleFollow() {
  if (!userStore.isAuthenticated) return ElMessage.warning('请先登录')
  try {
    if (isFollowing.value) {
      await unfollowArtist(route.params.id)
      isFollowing.value = false
      if (artist.value) artist.value.followerCount = Math.max(0, (artist.value.followerCount || 1) - 1)
      ElMessage.success('已取消关注')
    } else {
      await followArtist(route.params.id)
      isFollowing.value = true
      if (artist.value) artist.value.followerCount = (artist.value.followerCount || 0) + 1
      ElMessage.success('关注成功')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// 私信
async function handleMessage() {
  if (!userStore.isAuthenticated) return ElMessage.warning('请先登录')
  try {
    const res = await createOrGetConversation(route.params.id)
    if (res.code === 200 && res.data) {
      router.push(`/chat/${res.data.id}`)
    } else {
      ElMessage.error(res.message || '创建对话失败')
    }
  } catch {
    ElMessage.error('创建对话失败')
  }
}

// 跳转
function goToArtwork(id) {
  if (!id) return
  router.push(`/artworks/${id}`)
}

function goToArtist(id) {
  if (id != route.params.id) {
    router.push(`/artist/${id}`)
  }
}

// 监听路由参数变化（同一组件不同画师）
watch(() => route.params.id, (newId) => {
  if (newId) {
    artworks.value = []
    favorites.value = []
    followingList.value = []
    loadArtistProfile()
  }
})

onMounted(() => {
  loadArtistProfile()
})
</script>

<style scoped>
.artist-profile-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 加载状态 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e0e0e0;
  border-top-color: #0096fa;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.spinner.small {
  width: 24px;
  height: 24px;
  border-width: 2px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
  color: #999;
}
.error-text { font-size: 16px; color: #666; }
.back-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 20px;
  background: #0096fa;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}

/* Banner */
.profile-banner {
  height: 200px;
  position: relative;
  background: linear-gradient(135deg, #0096fa 0%, #0057b8 50%, #003d7a 100%);
  overflow: hidden;
}
.banner-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.06) 0%, transparent 40%);
}
.banner-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(to top, #f5f5f5, transparent);
}

/* 主内容区 */
.profile-main {
  max-width: 960px;
  margin: -60px auto 40px;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

/* 用户卡片 */
.user-card {
  display: flex;
  gap: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.avatar-ring {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  padding: 3px;
  background: linear-gradient(135deg, #0096fa, #00d4ff);
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.role-tag.artist {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
}
.role-tag.user {
  background: #f0f0f0;
  color: #666;
}

.info-area {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.display-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.bio-text {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin: 0 0 16px;
}
.bio-text.empty { color: #bbb; font-style: italic; }

.stat-strip {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}
.stat-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}
.stat-lbl {
  font-size: 12px;
  color: #999;
}

/* 操作按钮 */
.action-row {
  display: flex;
  gap: 12px;
}
.follow-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: #0096fa;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.follow-btn:hover { background: #0080d8; }
.follow-btn.following {
  background: #e8f4fd;
  color: #0096fa;
}
.follow-btn.following:hover {
  background: #fee2e2;
  color: #ef4444;
}

.message-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1.5px solid #ddd;
  border-radius: 20px;
  background: #fff;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.message-btn:hover {
  border-color: #0096fa;
  color: #0096fa;
}

.commission-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1.5px solid #0096fa;
  border-radius: 20px;
  background: #0096fa;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.commission-btn:hover {
  background: #0080d5;
  border-color: #0080d5;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: 1.5px solid #ddd;
  border-radius: 20px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
}
.edit-btn:hover { border-color: #0096fa; color: #0096fa; }

/* Tabs */
.tabs-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  overflow: hidden;
}
.tabs-nav {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
}
.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 20px;
  border: none;
  background: none;
  color: #999;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item:hover { color: #333; }
.tab-item.active {
  color: #0096fa;
  border-bottom-color: #0096fa;
}

.tab-panel {
  padding: 24px;
  min-height: 200px;
}

/* 作品网格 - 瀑布流 */
.artwork-grid {
  column-count: 4;
  column-gap: 16px;
}

/* 关注网格 */
.following-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
}
.following-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.following-card:hover { background: #f5f5f5; }
.following-name {
  font-size: 13px;
  color: #333;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 90px;
}
.following-meta {
  font-size: 11px;
  color: #999;
  text-align: center;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #bbb;
  gap: 12px;
}

/* 隐私提示 */
.privacy-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  gap: 12px;
  color: #999;
}
.privacy-hint p {
  font-size: 14px;
}

/* 加载 */
.tab-loading {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* 分页 */
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式 */
@media (max-width: 640px) {
  .user-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 24px 16px;
  }
  .stat-strip { justify-content: center; }
  .action-row { justify-content: center; }
  .artwork-grid {
    column-count: 2;
  }
}
</style>
