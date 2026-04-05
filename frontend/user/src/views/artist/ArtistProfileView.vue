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
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"/></svg>
              认证画师
            </div>
            <div v-else class="role-tag user">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-7 8-7s8 3 8 7"/></svg>
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

            <div v-if="artist.role === 'ARTIST'" class="commission-state" :class="artist.acceptingCommissions ? 'open' : 'closed'">
              <span class="state-dot"></span>
              {{ artist.acceptingCommissions ? '当前可约稿' : '当前暂停接稿' }}
            </div>

            <!-- 操作按钮（非本人才显示） -->
            <div class="action-row" v-if="!isSelf">
              <button
                class="follow-btn"
                :class="{ following: isFollowing }"
                @click="handleFollow"
                v-if="userStore.isAuthenticated"
              >
                <svg v-if="!isFollowing" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-7 8-7s8 3 8 7"/><line x1="19" y1="4" x2="19" y2="10"/><line x1="16" y1="7" x2="22" y2="7"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                {{ isFollowing ? '已关注' : '关注' }}
              </button>
              <button
                class="message-btn"
                @click="handleMessage"
                v-if="userStore.isAuthenticated"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                私信
              </button>
              <button
                class="commission-btn"
                @click="$router.push({ name: 'CreateCommission', query: { artistId: artist.id } })"
                v-if="userStore.isAuthenticated && artist.role === 'ARTIST'"
                :disabled="artist.acceptingCommissions === false"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                {{ artist.acceptingCommissions === false ? '暂不接稿' : '发起约稿' }}
              </button>
            </div>
            <!-- 自己的主页 -->
            <div class="action-row" v-else>
              <button class="edit-btn" @click="$router.push('/profile')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑资料
              </button>
            </div>
          </div>
        </div>

        <div v-if="artist.role === 'ARTIST'" class="plans-section">
          <div class="plans-header">
            <h3>约稿方案</h3>
            <span class="plans-hint">{{ artist.acceptingCommissions === false ? '画师当前暂停接稿' : '可选择方案快速发起约稿' }}</span>
          </div>
          <div v-if="loadingPlans" class="plans-loading">
            <div class="spinner small"></div>
          </div>
          <div v-else-if="commissionPlans.length > 0" class="plans-grid">
            <div v-for="plan in commissionPlans" :key="plan.id" class="plan-card" :class="{ inactive: !plan.active }">
              <div class="plan-row">
                <span class="plan-title">{{ plan.title }}</span>
                <span class="plan-price">¥{{ plan.priceStart }}<span v-if="plan.priceEnd"> ~ ¥{{ plan.priceEnd }}</span></span>
              </div>
              <p class="plan-desc">{{ plan.description || '暂无描述' }}</p>
              <div class="plan-tags">
                <span v-if="plan.estimatedDays">{{ plan.estimatedDays }}天交付</span>
                <span v-if="plan.revisionsIncluded">{{ plan.revisionsIncluded }}次修改</span>
                <span v-if="plan.category">{{ plan.category }}</span>
              </div>
              <button
                class="plan-commission-btn"
                :disabled="artist.acceptingCommissions === false || !plan.active"
                @click="$router.push({ name: 'CreateCommission', query: { artistId: artist.id, planId: plan.id } })"
              >
                {{ artist.acceptingCommissions === false ? '暂停接稿' : (plan.active ? '按此方案约稿' : '方案已停用') }}
              </button>
            </div>
          </div>
          <div v-else class="plans-empty">该画师暂未设置约稿方案</div>
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
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              作品
            </button>
            <button
              v-if="!artist.hideFavorites || isSelf"
              class="tab-item"
              :class="{ active: activeTab === 'favorites' }"
              @click="switchTab('favorites')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78z"/></svg>
              收藏
            </button>
            <button
              v-if="!artist.hideFollowing || isSelf"
              class="tab-item"
              :class="{ active: activeTab === 'following' }"
              @click="switchTab('following')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-7 8-7s8 3 8 7"/></svg>
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
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
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
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
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
import { getCommissionPlans } from '@/api/commissionPlan'
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
const commissionPlans = ref([])
const loadingPlans = ref(false)

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
        loadCommissionPlans()
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

async function loadCommissionPlans() {
  if (!route.params.id) return
  loadingPlans.value = true
  try {
    const res = await getCommissionPlans({ artistId: route.params.id })
    if (res.code === 200) {
      commissionPlans.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    } else {
      commissionPlans.value = []
    }
  } catch {
    commissionPlans.value = []
  } finally {
    loadingPlans.value = false
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
    ElMessage.error('加载作品失败')
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
    ElMessage.error('加载收藏失败')
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
    ElMessage.error('加载关注列表失败')
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
  background: #fff;
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
  border-top-color: #1a1a1a;
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
  border-radius: 999px;
  background: #1a1a1a;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.back-btn:hover { background: #333; }

/* Banner */
.profile-banner {
  height: 200px;
  position: relative;
  background: linear-gradient(135deg, #e8e0f0 0%, #d4e4f7 50%, #e0ecf5 100%);
  overflow: hidden;
}
.banner-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.2) 0%, transparent 40%);
}
.banner-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(to top, #fff, transparent);
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
  border-radius: 20px;
  padding: 28px 32px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.avatar-ring {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  padding: 3px;
  background: linear-gradient(135deg, #c8b6e2, #a8d0e6);
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
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.role-tag.artist {
  background: #fef7ed;
  color: #c2710c;
}
.role-tag.user {
  background: #f5f5f5;
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

.commission-state {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 14px;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 10px;
  border-radius: 999px;
}
.commission-state .state-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.commission-state.open {
  background: #ecfdf5;
  color: #15803d;
}
.commission-state.open .state-dot {
  background: #22c55e;
}
.commission-state.closed {
  background: #fef2f2;
  color: #b91c1c;
}
.commission-state.closed .state-dot {
  background: #ef4444;
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

.commission-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.plans-section {
  background: #fff;
  border-radius: 20px;
  padding: 22px 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}
.plans-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.plans-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
}
.plans-hint {
  font-size: 12px;
  color: #888;
}
.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}
.plan-card {
  border: 1px solid #ececec;
  border-radius: 14px;
  padding: 14px;
  background: #fff;
}
.plan-card.inactive {
  opacity: 0.65;
}
.plan-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}
.plan-title {
  font-size: 14px;
  font-weight: 700;
  color: #222;
}
.plan-price {
  font-size: 13px;
  font-weight: 700;
  color: #0b78d1;
}
.plan-desc {
  margin: 0 0 8px;
  font-size: 12px;
  color: #777;
  line-height: 1.5;
}
.plan-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.plan-tags span {
  padding: 2px 8px;
  border-radius: 999px;
  background: #f4f4f5;
  color: #666;
  font-size: 11px;
}
.plan-commission-btn {
  width: 100%;
  border: 1px solid #0096fa;
  background: #fff;
  color: #0096fa;
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.plan-commission-btn:disabled {
  border-color: #ddd;
  color: #aaa;
  cursor: not-allowed;
}
.plans-empty {
  font-size: 13px;
  color: #999;
  padding: 8px 2px;
}
.plans-loading {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
.follow-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border: none;
  border-radius: 999px;
  background: #1a1a1a;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.follow-btn:hover { background: #333; }
.follow-btn.following {
  background: #f5f5f5;
  color: #1a1a1a;
}
.follow-btn.following:hover {
  background: #fee2e2;
  color: #ef4444;
}

.message-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border: 1.5px solid #e0e0e0;
  border-radius: 999px;
  background: #fff;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.message-btn:hover {
  border-color: #1a1a1a;
  color: #1a1a1a;
}

.commission-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border: 1.5px solid #1a1a1a;
  border-radius: 999px;
  background: #1a1a1a;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.commission-btn:hover {
  background: #333;
  border-color: #333;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  border: 1.5px solid #e0e0e0;
  border-radius: 999px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.edit-btn:hover { border-color: #1a1a1a; color: #1a1a1a; }

/* Tabs */
.tabs-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  overflow: hidden;
}
.tabs-nav {
  display: flex;
  gap: 8px;
  padding: 16px 24px 12px;
  border-bottom: 1px solid #f0f0f0;
}
.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  background: #f5f5f5;
  color: #999;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 999px;
  border-bottom: none;
  transition: all 0.2s;
}
.tab-item:hover { color: #333; background: #eee; }
.tab-item.active {
  color: #fff;
  background: #1a1a1a;
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
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.following-card:hover { background: #f9f9f9; }
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
