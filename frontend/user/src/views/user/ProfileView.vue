<template>
  <div class="profile-page">
    <!-- 骨架屏 -->
    <div v-if="loading" class="profile-skeleton">
      <!-- Banner 骨架 -->
      <div class="profile-banner">
        <div class="banner-pattern"></div>
        <div class="banner-fade"></div>
      </div>
      <div class="profile-main">
        <div class="user-card" style="padding:24px;display:flex;flex-direction:column;align-items:center;gap:16px">
          <SkeletonBlock height="100px" circle />
          <SkeletonBlock width="80px" height="22px" radius="11px" />
          <SkeletonBlock width="120px" height="18px" />
          <SkeletonBlock width="200px" height="14px" />
          <div style="display:flex;gap:32px;margin-top:8px">
            <SkeletonBlock width="50px" height="14px" />
            <SkeletonBlock width="50px" height="14px" />
            <SkeletonBlock width="50px" height="14px" />
          </div>
        </div>
        <div style="display:flex;gap:12px;margin-top:16px;justify-content:center">
          <SkeletonBlock width="80px" height="36px" radius="18px" />
          <SkeletonBlock width="80px" height="36px" radius="18px" />
          <SkeletonBlock width="80px" height="36px" radius="18px" />
        </div>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-top:24px">
          <SkeletonBlock height="220px" radius="10px" v-for="n in 6" :key="n" />
        </div>
      </div>
    </div>

    <template v-else-if="userProfile">
      <!-- Banner -->
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
            <div class="avatar-ring" @click="handleAvatarClick">
              <img :src="userProfile.avatarUrl || defaultAvatar" alt="avatar" class="avatar-img" />
              <div class="avatar-hover">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
                <span>更换头像</span>
              </div>
              <input
                ref="avatarInput"
                type="file"
                accept="image/*"
                @change="handleAvatarChange"
                style="display:none"
              />
            </div>
            <!-- 角色标签 -->
            <div v-if="userProfile.role === 'ARTIST'" class="role-tag artist">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              认证画师
            </div>
            <div v-else class="role-tag user">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              普通用户
            </div>
          </div>

          <!-- 信息区 -->
          <div class="info-area">
            <div class="name-row">
              <h1 class="display-name">{{ userProfile.username }}</h1>
              <span v-if="userProfile.membershipLevel === 'VIP'" class="vip-badge vip">VIP</span>
              <span v-else-if="userProfile.membershipLevel === 'SVIP'" class="vip-badge svip">SVIP</span>
              <button class="edit-btn" @click="handleEditProfile">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑资料
              </button>
            </div>

            <p class="bio-text" :class="{ empty: !userProfile.bio }">
              {{ userProfile.bio || '这个人很懒，还没有写个人简介...' }}
            </p>

            <!-- 数据统计 -->
            <div class="stat-strip">
              <div v-if="userProfile.role === 'ARTIST'" class="stat-chip">
                <span class="stat-num">{{ userProfile.artworkCount || 0 }}</span>
                <span class="stat-lbl">作品</span>
              </div>
              <div v-if="userProfile.role === 'ARTIST'" class="stat-chip">
                <span class="stat-num">{{ userProfile.followerCount || 0 }}</span>
                <span class="stat-lbl">粉丝</span>
              </div>
              <div class="stat-chip">
                <span class="stat-num">{{ followingCount }}</span>
                <span class="stat-lbl">关注</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-row">
              <button v-if="userProfile.role !== 'ARTIST'" class="primary-btn" @click="handleApplyArtist">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 010-5H6"/><path d="M18 9h1.5a2.5 2.5 0 000-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0012 0V2z"/></svg>
                申请成为画师
              </button>
              <button v-if="userProfile.role === 'ARTIST' && !userProfile.acceptingCommissions" class="outline-btn" @click="handleToggleCommissions">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                开启约稿
              </button>
              <button v-if="userProfile.role === 'ARTIST' && userProfile.acceptingCommissions" class="success-btn" @click="handleToggleCommissions">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                约稿中
              </button>
            </div>
          </div>
        </div>

        <!-- Tab 区域 -->
        <div class="tabs-section">
          <div class="tabs-nav">
            <button
              v-if="userProfile.role === 'ARTIST'"
              class="tab-item"
              :class="{ active: activeTab === 'artworks' }"
              @click="switchTab('artworks')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
              作品
            </button>
            <button
              class="tab-item"
              :class="{ active: activeTab === 'favorites' }"
              @click="switchTab('favorites')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
              收藏
            </button>
            <button
              class="tab-item"
              :class="{ active: activeTab === 'following' }"
              @click="switchTab('following')"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
              关注
            </button>
          </div>

          <!-- 作品 Tab -->
          <div v-show="activeTab === 'artworks'" class="tab-panel">
            <div v-if="artworks.length > 0" class="artwork-grid">
              <div
                v-for="art in artworks"
                :key="art.id"
                class="artwork-card"
                @click="goToArtwork(art.id)"
              >
                <div class="card-cover">
                  <img :src="art.thumbnailUrl || art.imageUrl" :alt="art.title" loading="lazy" />
                  <div class="card-overlay">
                    <div class="overlay-stats">
                      <span>
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
                        {{ art.likeCount || 0 }}
                      </span>
                      <span>
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        {{ art.viewCount || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="card-title">{{ art.title }}</div>
              </div>
            </div>
            <div v-else class="empty-panel">
              <svg viewBox="0 0 64 64" width="48" height="48" fill="none" stroke="#ddd" stroke-width="1.5"><rect x="8" y="8" width="48" height="48" rx="6"/><path d="M8 40l12-12 10 10 8-8 18 18"/><circle cx="22" cy="22" r="4"/></svg>
              <p>还没有发布作品</p>
            </div>
            <div v-if="artworkTotal > artworkPageSize" class="pager">
              <el-pagination v-model:current-page="artworkPage" :page-size="artworkPageSize" :total="artworkTotal" layout="prev, pager, next" small @current-change="loadArtworks" />
            </div>
          </div>

          <!-- 收藏 Tab -->
          <div v-show="activeTab === 'favorites'" class="tab-panel">
            <div v-if="favorites.length > 0" class="artwork-grid">
              <div
                v-for="art in favorites"
                :key="art.id"
                class="artwork-card"
                @click="goToArtwork(art.id)"
              >
                <div class="card-cover">
                  <img :src="art.thumbnailUrl || art.imageUrl" :alt="art.title" loading="lazy" />
                  <div class="card-overlay">
                    <div class="overlay-stats">
                      <span>
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
                        {{ art.likeCount || 0 }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="card-title">{{ art.title }}</div>
              </div>
            </div>
            <div v-else class="empty-panel">
              <svg viewBox="0 0 64 64" width="48" height="48" fill="none" stroke="#ddd" stroke-width="1.5"><path d="M32 54l-2.2-2C16 40 8 33 8 24c0-7 5.6-12 12.5-12 3.9 0 7.6 1.8 10 4.6C33.4 13.8 37.1 12 41 12 48.4 12 54 17 54 24c0 9-8 16-21.8 28L32 54z"/></svg>
              <p>还没有收藏作品</p>
            </div>
            <div v-if="favoriteTotal > favoritePageSize" class="pager">
              <el-pagination v-model:current-page="favoritePage" :page-size="favoritePageSize" :total="favoriteTotal" layout="prev, pager, next" small @current-change="loadFavorites" />
            </div>
          </div>

          <!-- 关注 Tab -->
          <div v-show="activeTab === 'following'" class="tab-panel">
            <div v-if="following.length > 0" class="following-grid">
              <div v-for="artist in following" :key="artist.id" class="follow-card" @click="goToArtistProfile(artist.id)">
                <img :src="artist.avatarUrl || defaultAvatar" alt="" class="follow-avatar" />
                <div class="follow-info">
                  <span class="follow-name">{{ artist.username }}</span>
                  <span class="follow-meta">{{ artist.artworkCount || 0 }} 作品 · {{ artist.followerCount || 0 }} 粉丝</span>
                </div>
                <button class="unfollow-btn" @click.stop="handleUnfollow(artist.id)">已关注</button>
              </div>
            </div>
            <div v-else class="empty-panel">
              <svg viewBox="0 0 64 64" width="48" height="48" fill="none" stroke="#ddd" stroke-width="1.5"><circle cx="26" cy="24" r="8"/><path d="M10 52c0-8.8 7.2-16 16-16s16 7.2 16 16"/><line x1="48" y1="24" x2="48" y2="36"/><line x1="42" y1="30" x2="54" y2="30"/></svg>
              <p>还没有关注画师</p>
            </div>
            <div v-if="followingTotal > followingPageSize" class="pager">
              <el-pagination v-model:current-page="followingPage" :page-size="followingPageSize" :total="followingTotal" layout="prev, pager, next" small @current-change="loadFollowing" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" disabled />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="4" placeholder="介绍一下自己吧" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" round @click="handleSaveProfile" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 申请画师对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请成为画师" width="480px" :close-on-click-modal="false">
      <div class="apply-tip">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#0096FA" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <span>提交申请后，管理员将会审核你的资料</span>
      </div>
      <el-form :model="applyForm" label-position="top" style="margin-top: 16px">
        <el-form-item label="作品集链接" required>
          <el-input v-model="applyForm.portfolioUrl" placeholder="请输入作品集链接" />
        </el-form-item>
        <el-form-item label="创作经历" required>
          <el-input v-model="applyForm.description" type="textarea" :rows="4" placeholder="介绍一下你的创作经历和擅长的风格" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" round @click="handleSubmitApplication" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, updateProfile, applyForArtist } from '@/api/user'
import { getArtworks, getUserFavorites } from '@/api/artwork'
import { getFollowing, unfollowArtist } from '@/api/follow'
import { uploadAvatar } from '@/api/file'
import SkeletonBlock from '@/components/SkeletonBlock.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const userProfile = ref(null)
const activeTab = ref('artworks')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const avatarInput = ref(null)
const uploadingAvatar = ref(false)

// 作品
const artworks = ref([])
const artworkPage = ref(1)
const artworkPageSize = ref(12)
const artworkTotal = ref(0)

// 收藏
const favorites = ref([])
const favoritePage = ref(1)
const favoritePageSize = ref(12)
const favoriteTotal = ref(0)

// 关注
const following = ref([])
const followingPage = ref(1)
const followingPageSize = ref(12)
const followingTotal = ref(0)
const followingCount = computed(() => followingTotal.value)

// 编辑资料
const editDialogVisible = ref(false)
const editForm = ref({ username: '', email: '', bio: '', avatarUrl: '' })
const saving = ref(false)

// 申请画师
const applyDialogVisible = ref(false)
const applyForm = ref({ portfolioUrl: '', description: '' })
const applying = ref(false)

function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'artworks' && artworks.value.length === 0) loadArtworks()
  if (tab === 'favorites' && favorites.value.length === 0) loadFavorites()
  if (tab === 'following' && following.value.length === 0) loadFollowing()
}

async function loadUserProfile() {
  loading.value = true
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      userProfile.value = res.data
      
      // 根据用户角色设置默认标签页
      if (userProfile.value.role === 'ARTIST') {
        activeTab.value = 'artworks'
        await loadArtworks()
      } else {
        activeTab.value = 'favorites'
        await loadFavorites()
      }
      
      // 始终加载关注列表（用于显示关注数量）
      await loadFollowing()
      
      console.log('用户资料加载完成:', {
        role: userProfile.value.role,
        activeTab: activeTab.value,
        favoritesCount: favorites.value.length,
        followingCount: following.value.length
      })
    } else {
      ElMessage.error(res.message || '加载用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

async function loadArtworks() {
  try {
    const res = await getArtworks({ artistId: userProfile.value.id, page: artworkPage.value, size: artworkPageSize.value })
    if (res.code === 200) {
      artworks.value = res.data?.records || []
      artworkTotal.value = res.data?.total || 0
      console.log('作品列表加载成功:', artworks.value.length, '个作品')
    } else {
      console.error('作品列表加载失败:', res.message)
    }
  } catch (error) {
    console.error('作品列表加载异常:', error)
  }
}

async function loadFavorites() {
  try {
    console.log('开始加载收藏列表:', { page: favoritePage.value, size: favoritePageSize.value })
    const res = await getUserFavorites({ page: favoritePage.value, size: favoritePageSize.value })
    console.log('收藏列表API响应:', res)
    if (res.code === 200) {
      favorites.value = res.data?.records || []
      favoriteTotal.value = res.data?.total || 0
      console.log('收藏列表加载成功:', favorites.value.length, '个作品，总数:', favoriteTotal.value)
    } else {
      console.error('收藏列表加载失败:', res.message)
      ElMessage.error(res.message || '加载收藏列表失败')
    }
  } catch (error) {
    console.error('收藏列表加载异常:', error)
    ElMessage.error('加载收藏列表失败')
  }
}

async function loadFollowing() {
  try {
    console.log('开始加载关注列表:', { page: followingPage.value, size: followingPageSize.value })
    const res = await getFollowing({ page: followingPage.value, size: followingPageSize.value })
    console.log('关注列表API响应:', res)
    if (res.code === 200) {
      following.value = res.data?.records || []
      followingTotal.value = res.data?.total || 0
      console.log('关注列表加载成功:', following.value.length, '个画师，总数:', followingTotal.value)
    } else {
      console.error('关注列表加载失败:', res.message)
      ElMessage.error(res.message || '加载关注列表失败')
    }
  } catch (error) {
    console.error('关注列表加载异常:', error)
    ElMessage.error('加载关注列表失败')
  }
}

function handleEditProfile() {
  editForm.value = {
    username: userProfile.value.username,
    email: userProfile.value.email,
    bio: userProfile.value.bio || '',
    avatarUrl: userProfile.value.avatarUrl || ''
  }
  editDialogVisible.value = true
}

async function handleSaveProfile() {
  saving.value = true
  try {
    const res = await updateProfile(editForm.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userProfile.value = { ...userProfile.value, ...editForm.value }
      userStore.updateUser(editForm.value)
      editDialogVisible.value = false
    } else ElMessage.error(res.message || '保存失败')
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

function handleApplyArtist() {
  applyForm.value = { portfolioUrl: '', description: '' }
  applyDialogVisible.value = true
}

async function handleSubmitApplication() {
  if (!applyForm.value.portfolioUrl) return ElMessage.warning('请输入作品集链接')
  if (!applyForm.value.description) return ElMessage.warning('请输入创作经历')
  applying.value = true
  try {
    const res = await applyForArtist(applyForm.value)
    if (res.code === 200) { ElMessage.success('申请已提交，请等待审核'); applyDialogVisible.value = false }
    else ElMessage.error(res.message || '申请失败')
  } catch { ElMessage.error('申请失败') }
  finally { applying.value = false }
}

async function handleToggleCommissions() {
  ElMessage.info('功能开发中')
}

async function handleUnfollow(artistId) {
  try {
    await ElMessageBox.confirm('确定取消关注？', '确认', { type: 'warning' })
    const res = await unfollowArtist(artistId)
    if (res.code === 200) { ElMessage.success('已取消关注'); loadFollowing() }
    else ElMessage.error(res.message || '取消关注失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

function goToArtwork(id) { if (!id) return; router.push(`/artworks/${id}`) }

function goToArtistProfile(id) { router.push(`/artist/${id}`) }

function handleAvatarClick() { avatarInput.value?.click() }

async function handleAvatarChange(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.error('请选择图片文件')
  if (file.size > 5 * 1024 * 1024) return ElMessage.error('图片不能超过 5MB')
  uploadingAvatar.value = true
  try {
    ElMessage.info('正在上传头像...')
    const upRes = await uploadAvatar(file)
    if (upRes.code === 200) {
      const avatarUrl = upRes.data.imageUrl
      const saveRes = await updateProfile({
        username: userProfile.value.username,
        email: userProfile.value.email,
        bio: userProfile.value.bio,
        avatarUrl
      })
      if (saveRes.code === 200) {
        userProfile.value.avatarUrl = avatarUrl
        userStore.updateUser({ avatarUrl })
        ElMessage.success('头像已更新')
      } else ElMessage.error(saveRes.message || '更新头像失败')
    } else ElMessage.error(upRes.message || '上传失败')
  } catch { ElMessage.error('上传头像失败') }
  finally { uploadingAvatar.value = false; event.target.value = '' }
}

onMounted(() => loadUserProfile())
</script>

<style scoped>
/* ========== 页面容器 ========== */
.profile-page {
  min-height: calc(100vh - 56px);
  background: #fff;
}

/* ========== 骨架屏 ========== */
.profile-skeleton .user-card {
  margin-top: -60px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

/* ========== Banner ========== */
.profile-banner {
  height: 220px;
  background: linear-gradient(135deg, #e8f0fe 0%, #d4e4fc 40%, #c2d6f8 100%);
  position: relative;
  overflow: hidden;
}
.banner-pattern {
  position: absolute; inset: 0;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.06) 0%, transparent 40%),
    radial-gradient(circle at 60% 80%, rgba(255,255,255,0.04) 0%, transparent 60%);
}
.banner-fade {
  position: absolute; bottom: 0; left: 0; right: 0; height: 80px;
  background: linear-gradient(transparent, #fff);
}

/* ========== 主内容区 ========== */
.profile-main {
  max-width: 960px;
  margin: -80px auto 0;
  padding: 0 24px 40px;
  position: relative;
  z-index: 1;
}

/* ========== 用户卡片 ========== */
.user-card {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

/* 头像 */
.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.avatar-ring {
  width: 120px; height: 120px;
  border-radius: 50%;
  border: 3px solid #e8f0fe;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  position: relative;
  cursor: pointer;
  overflow: hidden;
}
.avatar-img {
  width: 100%; height: 100%;
  object-fit: cover;
  border-radius: 50%;
  transition: transform 0.3s;
}
.avatar-ring:hover .avatar-img { transform: scale(1.05); }
.avatar-hover {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px; opacity: 0;
  transition: opacity 0.25s;
  border-radius: 50%;
}
.avatar-hover span { font-size: 12px; color: #fff; }
.avatar-ring:hover .avatar-hover { opacity: 1; }

.role-tag {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 12px; border-radius: 999px;
  font-size: 12px; font-weight: 600;
}
.role-tag.artist { background: #FFF3E0; color: #E67E22; }
.role-tag.user { background: #F0F0F0; color: #888; }

/* 信息区 */
.info-area { flex: 1; min-width: 0; }
.name-row {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px;
}
.display-name {
  font-size: 28px; font-weight: 700; color: #1a1a1a; margin: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.vip-badge {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 700;
  letter-spacing: 1px; flex-shrink: 0;
}
.vip-badge.vip {
  background: linear-gradient(135deg, #ffd700, #ffaa00);
  color: #fff; box-shadow: 0 2px 8px rgba(255, 170, 0, 0.3);
}
.vip-badge.svip {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: #fff; box-shadow: 0 2px 8px rgba(238, 90, 36, 0.3);
}
.edit-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 20px; border: 1px solid #ddd;
  background: #fff; border-radius: 999px;
  font-size: 13px; color: #555; cursor: pointer;
  transition: all 0.2s; flex-shrink: 0;
}
.edit-btn:hover { border-color: #0096FA; color: #0096FA; }

.bio-text {
  font-size: 14px; color: #666; line-height: 1.7;
  margin: 0 0 16px; word-break: break-word;
}
.bio-text.empty { color: #bbb; font-style: italic; }

/* 数据条 */
.stat-strip {
  display: flex; gap: 32px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.stat-chip { display: flex; flex-direction: column; align-items: center; }
.stat-num {
  font-size: 24px; font-weight: 700; color: #1a1a1a;
  line-height: 1;
}
.stat-lbl { font-size: 12px; color: #999; margin-top: 4px; }

/* 操作按钮 */
.action-row { display: flex; gap: 10px; flex-wrap: wrap; }
.primary-btn, .outline-btn, .success-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 24px; border-radius: 999px;
  font-size: 14px; font-weight: 500; cursor: pointer;
  transition: all 0.2s; border: none;
}
.primary-btn {
  background: #0096FA; color: #fff;
}
.primary-btn:hover { background: #007AD4; box-shadow: 0 4px 12px rgba(0,150,250,0.35); }
.outline-btn {
  background: #fff; color: #0096FA; border: 1.5px solid #0096FA;
}
.outline-btn:hover { background: #E8F4FF; }
.success-btn {
  background: #00C48C; color: #fff;
}
.success-btn:hover { background: #00A876; }

/* ========== Tabs ========== */
.tabs-section {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  overflow: hidden;
}
.tabs-nav {
  display: flex; gap: 8px;
  padding: 16px 24px 0;
  border-bottom: none;
}
.tab-item {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 24px; border: none; background: #f5f5f5;
  font-size: 14px; font-weight: 500; color: #999;
  cursor: pointer; transition: all 0.2s;
  border-radius: 999px;
}
.tab-item:hover { color: #333; background: #eee; }
.tab-item.active {
  color: #fff; background: #0096FA;
}
.tab-item.active svg { stroke: #fff; }

.tab-panel { padding: 24px; }

/* ========== 作品网格 - 瀑布流 ========== */
.artwork-grid {
  column-count: 4;
  column-gap: 16px;
}
.artwork-card {
  cursor: pointer;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  transition: all 0.25s;
  break-inside: avoid;
  margin-bottom: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.artwork-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.card-cover {
  position: relative;
  overflow: hidden;
  line-height: 0;
}
.card-cover img {
  width: 100%; height: auto; display: block;
  transition: transform 0.4s;
}
.artwork-card:hover .card-cover img { transform: scale(1.08); }
.card-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(transparent 50%, rgba(0,0,0,0.6));
  opacity: 0; transition: opacity 0.25s;
  display: flex; align-items: flex-end;
  padding: 12px;
}
.artwork-card:hover .card-overlay { opacity: 1; }
.overlay-stats {
  display: flex; gap: 12px; font-size: 12px; color: #fff;
}
.overlay-stats span { display: flex; align-items: center; gap: 4px; }
.card-title {
  padding: 10px 12px; font-size: 13px; font-weight: 500;
  color: #333; overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 关注列表 ========== */
.following-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.follow-card {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 16px;
  border: 1px solid #f0f0f0; border-radius: 20px;
  transition: all 0.2s;
  cursor: pointer;
}
.follow-card:hover {
  border-color: #d0e8ff;
  box-shadow: 0 2px 8px rgba(0,150,250,0.06);
}
.follow-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  object-fit: cover; flex-shrink: 0;
}
.follow-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.follow-name {
  font-size: 14px; font-weight: 600; color: #333;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.follow-meta { font-size: 12px; color: #999; margin-top: 2px; }
.unfollow-btn {
  padding: 6px 16px; border: 1px solid #ddd; background: #fff;
  border-radius: 999px; font-size: 12px; color: #666;
  cursor: pointer; transition: all 0.2s; flex-shrink: 0;
}
.unfollow-btn:hover { border-color: #FF4D4F; color: #FF4D4F; background: #FFF1F0; }

/* ========== 空状态 ========== */
.empty-panel {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 60px 0; color: #ccc; font-size: 14px;
}

.pager { display: flex; justify-content: center; margin-top: 20px; }

/* ========== 对话框 ========== */
.apply-tip {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px; background: #E8F4FF;
  border-radius: 20px; font-size: 13px; color: #0078D4;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .profile-banner { height: 160px; }
  .profile-main { margin-top: -50px; padding: 0 16px 32px; }
  .user-card {
    flex-direction: column; align-items: center;
    text-align: center; padding: 24px 20px;
  }
  .name-row { flex-direction: column; gap: 10px; }
  .stat-strip { justify-content: center; }
  .action-row { justify-content: center; }
  .artwork-grid { column-count: 2; column-gap: 10px; }
  .following-grid { grid-template-columns: 1fr; }
}
</style>
