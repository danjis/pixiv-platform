<template>
  <div class="detail-page">
    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-content skeleton-layout">
      <!-- 左列 -->
      <div class="image-viewer">
        <SkeletonBlock height="480px" radius="12px" />
      </div>
      <div class="below-image">
        <div class="image-stats-card">
          <SkeletonBlock width="60px" height="14px" />
          <SkeletonBlock width="60px" height="14px" />
          <SkeletonBlock width="60px" height="14px" />
        </div>
      </div>
      <!-- 右列 -->
      <aside class="info-panel" style="overflow:hidden">
        <div style="padding:20px;display:flex;flex-direction:column;gap:16px">
          <SkeletonBlock width="70%" height="22px" />
          <SkeletonBlock width="100%" height="14px" />
          <SkeletonBlock width="85%" height="14px" />
          <div style="display:flex;align-items:center;gap:10px;margin-top:8px">
            <SkeletonBlock height="40px" circle />
            <div style="flex:1;display:flex;flex-direction:column;gap:6px">
              <SkeletonBlock width="100px" height="14px" />
              <SkeletonBlock width="60px" height="12px" />
            </div>
          </div>
          <div style="display:flex;gap:8px;margin-top:12px">
            <SkeletonBlock width="30%" height="14px" />
            <SkeletonBlock width="30%" height="14px" />
            <SkeletonBlock width="30%" height="14px" />
          </div>
          <SkeletonBlock width="100%" height="1px" radius="0" />
          <SkeletonBlock width="45%" height="16px" />
          <SkeletonBlock width="100%" height="60px" radius="8px" />
          <SkeletonBlock width="100%" height="60px" radius="8px" />
        </div>
      </aside>
    </div>

    <!-- 作品详情 -->
    <div v-else-if="artwork" class="detail-content">
      <!-- 图片查看区 -->
      <div class="image-viewer">
        <!-- 多图模式 -->
        <template v-if="artworkImages.length > 1">
          <div class="image-wrapper">
            <el-image
              :src="getDisplayImageUrl(artworkImages[currentImageIndex]) || artwork.imageUrl"
              :alt="artwork.title"
              fit="contain"
              :preview-src-list="artworkImages.map(i => getDisplayImageUrl(i))"
              :initial-index="currentImageIndex"
              class="main-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
          <!-- 图片导航 -->
          <div class="image-nav">
            <button class="nav-arrow" :disabled="currentImageIndex === 0" @click="currentImageIndex--">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
            </button>
            <div class="image-dots">
              <span
                v-for="(img, idx) in artworkImages"
                :key="idx"
                class="dot"
                :class="{ active: idx === currentImageIndex }"
                @click="currentImageIndex = idx"
              />
            </div>
            <span class="image-counter">{{ currentImageIndex + 1 }} / {{ artworkImages.length }}</span>
            <button class="nav-arrow" :disabled="currentImageIndex >= artworkImages.length - 1" @click="currentImageIndex++">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
          </div>
          <!-- 缩略图条 -->
          <div class="thumbnail-strip" v-if="artworkImages.length > 1">
            <div
              v-for="(img, idx) in artworkImages"
              :key="idx"
              class="thumb-item"
              :class="{ active: idx === currentImageIndex }"
              @click="currentImageIndex = idx"
            >
              <img :src="img.thumbnailUrl || img.imageUrl" :alt="'图片 ' + (idx + 1)" />
            </div>
          </div>
        </template>
        <!-- 单图模式 -->
        <template v-else>
          <div class="image-wrapper">
            <el-image
              :src="artworkImages.length > 0 ? getDisplayImageUrl(artworkImages[0]) : artwork.imageUrl"
              :alt="artwork.title"
              fit="contain"
              :preview-src-list="artworkImages.length > 0 ? [getDisplayImageUrl(artworkImages[0])] : [artwork.imageUrl]"
              class="main-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </template>
      </div>

      <!-- 图片下方信息区 -->
      <div class="below-image">
        <!-- 作品数据卡片 -->
        <div class="image-stats-card">
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            <span>{{ formatNumber(artwork.viewCount) }} 浏览</span>
          </div>
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#FF4060" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            <span>{{ formatNumber(artwork.likeCount) }} 喜欢</span>
          </div>
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#FFB800" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
            <span>{{ formatNumber(artwork.favoriteCount) }} 收藏</span>
          </div>
          <div class="stat-item" v-if="artwork.isAigc">
            <span class="aigc-tag">AIGC</span>
          </div>
        </div>

        <!-- 画师其他作品 -->
        <div class="more-works" v-if="artistWorks.length > 0">
          <div class="more-works-header">
            <h3 class="more-works-title">{{ artwork.artistName }} 的其他作品</h3>
            <button class="more-works-link" @click="goToArtistProfile">查看全部</button>
          </div>
          <div class="more-works-grid">
            <div
              v-for="item in artistWorks"
              :key="item.id"
              class="mini-card"
              @click="goToArtwork(item.id)"
            >
              <div class="mini-card-thumb">
                <img :src="item.thumbnailUrl || item.imageUrl" :alt="item.title" />
              </div>
              <div class="mini-card-info">
                <span class="mini-card-title">{{ item.title }}</span>
                <span class="mini-card-stats">
                  <svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="#FF4060" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                  {{ formatNumber(item.likeCount) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 侧边信息面板 -->
      <aside class="info-panel">
        <!-- 互动按钮栏 -->
        <div class="action-bar">
          <button
            class="action-btn"
            :class="{ active: artwork.isLiked }"
            @click="handleLike"
            :disabled="!userStore.isAuthenticated"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" :fill="artwork.isLiked ? '#FF4060' : 'none'" :stroke="artwork.isLiked ? '#FF4060' : 'currentColor'" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span>{{ formatNumber(artwork.likeCount) }}</span>
          </button>
          <button
            class="action-btn"
            :class="{ active: artwork.isFavorited }"
            @click="handleFavorite"
            :disabled="!userStore.isAuthenticated"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" :fill="artwork.isFavorited ? '#FFB800' : 'none'" :stroke="artwork.isFavorited ? '#FFB800' : 'currentColor'" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
            </svg>
            <span>{{ formatNumber(artwork.favoriteCount) }}</span>
          </button>
          <button class="action-btn" disabled>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
            </svg>
            <span>{{ formatNumber(artwork.viewCount) }}</span>
          </button>
        </div>

        <!-- 作品标题 -->
        <h1 class="artwork-title">{{ artwork.title }}</h1>

        <!-- 发布时间 -->
        <div class="publish-time">{{ formatDate(artwork.createdAt) }}</div>

        <!-- 作品描述 -->
        <div v-if="artwork.description" class="artwork-desc">
          <p>{{ artwork.description }}</p>
        </div>


        <!-- 作品标签（AI自动打标） -->
        <div class="artwork-tags">
          <template v-if="artwork.tags && artwork.tags.length > 0">
            <span
              v-for="tag in artwork.tags"
              :key="tag.name"
              class="tag-chip"
              @click="searchByTag(tag.name)"
            >
              #{{ tag.name }}
              <span v-if="tag.source === 'AUTO'" class="ai-badge">AI</span>
            </span>
          </template>
          <template v-else-if="aiTagLoading">
            <span class="ai-tag-loading"><el-icon class="is-loading" :size="16"><Loading /></el-icon> AI标签生成中...</span>
          </template>
        </div>

        <!-- 画师卡片 -->
        <div class="artist-card">
          <div class="artist-left" @click="goToArtistProfile">
            <el-avatar :size="48" :src="artwork.artistAvatar || defaultAvatar">
              {{ artwork.artistName?.charAt(0) }}
            </el-avatar>
            <div class="artist-meta">
              <div class="artist-name">{{ artwork.artistName }}</div>
            </div>
          </div>
          <div class="artist-actions" v-if="userStore.isAuthenticated && artwork.artistId !== userStore.user?.id">
            <button
              class="follow-btn"
              :class="{ following: isFollowing }"
              @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>

        <!-- 评论区域 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3 class="comment-title">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: -3px; margin-right: 4px; color: #0096FA;">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              评论
            </h3>
            <span class="comment-count-badge" v-if="artwork.commentCount">{{ artwork.commentCount }}</span>
          </div>

          <!-- 发表评论 -->
          <div v-if="userStore.isAuthenticated" class="comment-composer" :class="{ 'is-replying': replyTo }">
            <div class="reply-indicator" v-if="replyTo">
              <span class="reply-indicator-text">回复 <strong>@{{ replyTo.username }}</strong></span>
              <button class="reply-indicator-close" @click="cancelReply">&times;</button>
            </div>
            <div class="composer-row">
              <el-avatar :size="32" :src="userStore.user?.avatarUrl || defaultAvatar" />
              <div class="composer-input-wrap">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 4 }"
                  :placeholder="replyTo ? `说点什么...` : '写下你的评论...'"
                  maxlength="500"
                  resize="none"
                  ref="commentInputRef"
                />
              </div>
              <button
                class="composer-send"
                :disabled="!commentContent.trim() || submittingComment"
                @click="handleAddComment"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
              </button>
            </div>
          </div>
          <div v-else class="login-hint">
            <router-link to="/login">登录</router-link>后发表评论
          </div>

          <!-- 评论列表 -->
          <div v-if="loadingComments" class="comments-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载评论中...</span>
          </div>
          <div v-else-if="comments.length === 0" class="comments-empty">
            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#d5d5d5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span>还没有评论，来说两句吧</span>
          </div>
          <div v-else class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-thread">
              <!-- 顶级评论 -->
              <div class="comment-item" :class="{ 'vip-comment': comment.membershipLevel === 'VIP', 'svip-comment': comment.membershipLevel === 'SVIP' }">
                <el-avatar :size="36" :src="comment.avatarUrl || defaultAvatar" class="comment-avatar">
                  {{ comment.username?.charAt(0) }}
                </el-avatar>
                <div class="comment-main">
                  <div class="comment-bubble">
                    <span class="comment-author">{{ comment.username }}</span>
                    <span v-if="comment.membershipLevel === 'VIP'" class="vip-badge">VIP</span>
                    <span v-if="comment.membershipLevel === 'SVIP'" class="vip-badge svip">SVIP</span>
                    <p class="comment-content">{{ comment.content }}</p>
                  </div>
                  <div class="comment-footer">
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                    <button v-if="userStore.isAuthenticated" class="comment-action-btn" @click="startReply(comment)">
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                      回复
                    </button>
                    <button v-if="comment.userId === userStore.user?.id" class="comment-action-btn comment-delete-btn" @click="handleDeleteComment(comment.id)">
                      删除
                    </button>
                  </div>
                </div>
              </div>

              <!-- 子回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="replies-container">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <el-avatar :size="28" :src="reply.avatarUrl || defaultAvatar" class="reply-avatar">
                    {{ reply.username?.charAt(0) }}
                  </el-avatar>
                  <div class="reply-main">
                    <div class="reply-bubble">
                      <span class="reply-author">{{ reply.username }}</span>
                      <span v-if="reply.membershipLevel === 'VIP'" class="vip-badge small">VIP</span>
                      <span v-if="reply.membershipLevel === 'SVIP'" class="vip-badge svip small">SVIP</span>
                      <span v-if="reply.replyToUsername" class="reply-to-tag">
                        <svg viewBox="0 0 24 24" width="10" height="10" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
                        <span class="reply-to-name">{{ reply.replyToUsername }}</span>
                      </span>
                      <p class="reply-content">{{ reply.content }}</p>
                    </div>
                    <div class="comment-footer">
                      <span class="comment-time">{{ formatDate(reply.createdAt) }}</span>
                      <button v-if="userStore.isAuthenticated" class="comment-action-btn" @click="startReply(comment, reply)">
                        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        回复
                      </button>
                      <button v-if="reply.userId === userStore.user?.id" class="comment-action-btn comment-delete-btn" @click="handleDeleteComment(reply.id)">
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 评论分页 -->
          <div v-if="commentTotal > commentPageSize" class="comment-pager">
            <el-pagination
              v-model:current-page="commentPage"
              :page-size="commentPageSize"
              :total="commentTotal"
              layout="prev, pager, next"
              small
              @current-change="loadComments"
            />
          </div>
        </div>
      </aside>
    </div>

    <!-- 404 状态 -->
    <div v-else class="error-state">
      <svg viewBox="0 0 120 120" width="80" height="80" fill="none" stroke="#d0d0d0" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <rect x="20" y="20" width="80" height="80" rx="8" />
        <circle cx="45" cy="48" r="8" />
        <path d="M20 80 l25-25 l15 15 l20-20 l20 20 v10 a8 8 0 0 1 -8 8 H28 a8 8 0 0 1 -8-8z" fill="#f0f0f0" stroke="none"/>
      </svg>
      <p class="error-text">作品不存在或已被删除</p>
      <el-button type="primary" round @click="$router.back()">返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArtwork, getArtworks, likeArtwork, unlikeArtwork, favoriteArtwork, unfavoriteArtwork } from '@/api/artwork'
import { getComments, addComment, deleteComment } from '@/api/comment'
import { followArtist, unfollowArtist, checkFollowStatus } from '@/api/follow'
import { getMyMembership } from '@/api/membership'
import SkeletonBlock from '@/components/SkeletonBlock.vue'
import { ElMessage } from 'element-plus'
import { Picture, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 作品信息

const artwork = ref(null)
const loading = ref(true)
const isFollowing = ref(false)
const aiTagLoading = ref(false)
let aiTagTimer = null

// 多图相关
const currentImageIndex = ref(0)
const artworkImages = ref([]) // [{ imageUrl, thumbnailUrl, originalImageUrl, sortOrder }]

// 会员状态
const isVip = ref(false)

// 评论相关
const comments = ref([])
const commentContent = ref('')
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const loadingComments = ref(false)
const submittingComment = ref(false)
const replyTo = ref(null) // { commentId, username } — 当前回复的目标
const commentInputRef = ref(null)

// 画师其他作品
const artistWorks = ref([])

// 获取当前图片的显示URL（VIP看原图，非VIP看水印图）
function getDisplayImageUrl(img) {
  if (isVip.value && img.originalImageUrl) {
    return img.originalImageUrl
  }
  return img.imageUrl
}

// 加载会员状态
async function loadMembership() {
  try {
    const res = await getMyMembership()
    if (res.code === 200 && res.data) {
      const level = res.data.level
      isVip.value = (level === 'VIP' || level === 'SVIP')
    }
  } catch (e) {
    // 非登录用户或获取失败，默认非VIP
    isVip.value = false
  }
}


// 加载作品详情（支持AI标签自动刷新）
async function loadArtwork({ pollForTags = false } = {}) {
  if (!route.params.id) return
  loading.value = true
  try {
    const response = await getArtwork(route.params.id)
    if (response.code === 200 && response.data) {
      artwork.value = response.data
      // 初始化多图数据
      currentImageIndex.value = 0
      if (response.data.images && response.data.images.length > 0) {
        artworkImages.value = response.data.images
      } else {
        // 兼容旧数据：使用单图字段
        artworkImages.value = [{ imageUrl: response.data.imageUrl, thumbnailUrl: response.data.thumbnailUrl }]
      }
      loadComments()
      loadArtistWorks()
      if (userStore.isAuthenticated && artwork.value.artistId) {
        checkFollow()
        // 加载会员状态以决定是否显示无水印原图
        loadMembership()
      }
      // AI标签自动轮询逻辑
      if (pollForTags && (!artwork.value.tags || artwork.value.tags.length === 0)) {
        aiTagLoading.value = true
        startAiTagPolling()
      } else {
        aiTagLoading.value = false
        stopAiTagPolling()
      }
    } else {
      artwork.value = null
      ElMessage.error(response.message || '作品不存在或已被删除')
      setTimeout(() => { router.back() }, 3000)
    }
  } catch (error) {
    console.error('加载作品详情失败:', error)
    artwork.value = null
    ElMessage.error('作品不存在或已被删除')
    setTimeout(() => { router.back() }, 3000)
  } finally {
    loading.value = false
  }
}

// 轮询AI标签生成
function startAiTagPolling() {
  stopAiTagPolling()
  let pollCount = 0
  aiTagTimer = setInterval(async () => {
    pollCount++
    try {
      const response = await getArtwork(route.params.id)
      if (response.code === 200 && response.data) {
        artwork.value = response.data
        if (artwork.value.tags && artwork.value.tags.length > 0) {
          aiTagLoading.value = false
          stopAiTagPolling()
        }
      }
    } catch {}
    if (pollCount >= 15) { // 最多轮询30秒
      aiTagLoading.value = false
      stopAiTagPolling()
    }
  }, 2000)
}
function stopAiTagPolling() {
  if (aiTagTimer) {
    clearInterval(aiTagTimer)
    aiTagTimer = null
  }
}

// 检查关注状态
async function checkFollow() {
  try {
    const res = await checkFollowStatus(artwork.value.artistId)
    if (res.code === 200) {
      isFollowing.value = res.data?.following || res.data === true
    }
  } catch {
    // 忽略
  }
}

// 关注/取关
async function handleFollow() {
  try {
    if (isFollowing.value) {
      await unfollowArtist(artwork.value.artistId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await followArtist(artwork.value.artistId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// 跳转到画师主页
function goToArtistProfile() {
  if (artwork.value?.artistId) {
    router.push(`/artist/${artwork.value.artistId}`)
  }
}

// 加载画师其他作品
async function loadArtistWorks() {
  if (!artwork.value?.artistId) return
  try {
    const res = await getArtworks({
      artistId: artwork.value.artistId,
      page: 1,
      size: 7,
      sortBy: 'latest'
    })
    if (res.code === 200 && res.data) {
      const items = res.data.records || res.data.items || []
      // 排除当前作品
      artistWorks.value = items.filter(a => a.id !== artwork.value.id).slice(0, 6)
    }
  } catch {
    // 非关键功能，静默失败
  }
}

// 跳转到作品详情
function goToArtwork(id) {
  if (!id) return
  router.push(`/artworks/${id}`)
}

// 加载评论
async function loadComments() {
  if (!artwork.value) return
  loadingComments.value = true
  try {
    const response = await getComments(artwork.value.id, {
      page: commentPage.value,
      size: commentPageSize.value
    })
    if (response.code === 200 && response.data) {
      comments.value = response.data.records || response.data.items || []
      commentTotal.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

// 点赞/取消
async function handleLike() {
  if (!userStore.isAuthenticated) return ElMessage.warning('请先登录')
  try {
    if (artwork.value.isLiked) {
      await unlikeArtwork(artwork.value.id)
      artwork.value.isLiked = false
      artwork.value.likeCount--
    } else {
      await likeArtwork(artwork.value.id)
      artwork.value.isLiked = true
      artwork.value.likeCount++
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// 收藏/取消
async function handleFavorite() {
  if (!userStore.isAuthenticated) return ElMessage.warning('请先登录')
  try {
    if (artwork.value.isFavorited) {
      await unfavoriteArtwork(artwork.value.id)
      artwork.value.isFavorited = false
      artwork.value.favoriteCount--
    } else {
      await favoriteArtwork(artwork.value.id)
      artwork.value.isFavorited = true
      artwork.value.favoriteCount++
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

// 发表评论
async function handleAddComment() {
  if (!commentContent.value.trim()) return
  submittingComment.value = true
  try {
    const data = { content: commentContent.value.trim() }
    if (replyTo.value) {
      data.parentId = replyTo.value.commentId
    }
    const response = await addComment(artwork.value.id, data)
    if (response.code === 200) {
      ElMessage.success(replyTo.value ? '回复成功' : '评论成功')
      commentContent.value = ''
      replyTo.value = null
      artwork.value.commentCount++
      commentPage.value = 1
      loadComments()
    } else {
      ElMessage.error(response.message || '评论失败')
    }
  } catch {
    ElMessage.error('评论失败')
  } finally {
    submittingComment.value = false
  }
}

// 开始回复
function startReply(parentComment, replyTarget = null) {
  replyTo.value = {
    commentId: parentComment.id,
    username: replyTarget ? replyTarget.username : parentComment.username
  }
  commentContent.value = ''
  nextTick(() => {
    commentInputRef.value?.focus?.()
    // 滚动到输入框
    const inputEl = document.querySelector('.comment-input')
    if (inputEl) inputEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

// 取消回复
function cancelReply() {
  replyTo.value = null
  commentContent.value = ''
}

// 删除评论
async function handleDeleteComment(commentId) {
  try {
    const response = await deleteComment(artwork.value.id, commentId)
    if (response.code === 200) {
      ElMessage.success('已删除')
      artwork.value.commentCount--
      loadComments()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

// 按标签搜索
function searchByTag(tagName) {
  router.push({ name: 'Artworks', query: { tags: tagName } })
}

// 格式化
function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function formatNumber(num) {
  if (!num) return 0
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num
}

onMounted(() => loadArtwork({ pollForTags: true }))

onBeforeUnmount(() => {
  stopAiTagPolling()
})
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 56px);
}
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 120px 0;
}
.error-text { color: #999; font-size: 15px; }

/* 主布局 */
.detail-content {
  display: grid;
  grid-template-columns: 1fr 360px;
  grid-template-rows: auto 1fr;
  gap: 24px;
  align-items: start;
}

/* 图片区 */
.image-viewer {
  grid-column: 1;
  grid-row: 1;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.image-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fafafa;
}
.main-image { max-width: 100%; max-height: 80vh; }
.main-image :deep(.el-image__inner) { max-height: 80vh; }
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #bbb;
  padding: 80px 0;
}

/* 多图导航 */
.image-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}
.nav-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: #e8e8e8;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.nav-arrow:hover:not(:disabled) { background: #0096FA; color: #fff; }
.nav-arrow:disabled { opacity: 0.3; cursor: default; }
.image-dots {
  display: flex;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.2s;
}
.dot.active { background: #0096FA; transform: scale(1.2); }
.image-counter {
  font-size: 13px;
  color: #999;
  min-width: 40px;
  text-align: center;
}

/* 缩略图条 */
.thumbnail-strip {
  display: flex;
  gap: 6px;
  padding: 8px 12px;
  overflow-x: auto;
  background: #f8f8f8;
  border-top: 1px solid #f0f0f0;
}
.thumbnail-strip::-webkit-scrollbar { height: 4px; }
.thumbnail-strip::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }
.thumb-item {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  opacity: 0.6;
  transition: all 0.2s;
}
.thumb-item:hover { opacity: 0.9; }
.thumb-item.active { border-color: #0096FA; opacity: 1; }
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 图片下方区域 */
.below-image {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 数据卡片 */
.image-stats-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}
.aigc-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 画师其他作品 */
.more-works {
  background: #fff;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.more-works-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.more-works-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.more-works-link {
  background: none;
  border: none;
  color: #0096FA;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
}
.more-works-link:hover { opacity: 0.7; }

.more-works-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.mini-card {
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  background: #fafafa;
  transition: transform 0.2s, box-shadow 0.2s;
}
.mini-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.mini-card-thumb {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
}
.mini-card-thumb img {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.mini-card:hover .mini-card-thumb img { transform: scale(1.05); }
.mini-card-info {
  padding: 6px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}
.mini-card-title {
  font-size: 11px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.mini-card-stats {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  color: #999;
  flex-shrink: 0;
}

/* 侧边面板 */
.info-panel {
  grid-column: 2;
  grid-row: 1 / -1;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.info-panel::-webkit-scrollbar { width: 4px; }
.info-panel::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 2px; }

/* 互动栏 */
.action-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 0;
  border: none;
  background: #f8f8f8;
  border-radius: 16px;
  cursor: pointer;
  color: #666;
  font-size: 13px;
  transition: all 0.2s;
}
.action-btn:hover:not(:disabled) { background: #f0f0f0; }
.action-btn.active { background: #fff0f3; color: #FF4060; }
.action-btn:disabled { opacity: 0.5; cursor: default; }

.artwork-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
  line-height: 1.4;
}
.publish-time { font-size: 13px; color: #999; margin-bottom: 16px; }
.artwork-desc { margin-bottom: 16px; }
.artwork-desc p {
  font-size: 14px; color: #555; line-height: 1.7; margin: 0; white-space: pre-wrap;
}

/* 标签 */
.artwork-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.tag-chip {
  font-size: 13px;
  color: #0096FA;
  cursor: pointer;
  transition: opacity 0.2s;
}
.tag-chip:hover { opacity: 0.7; }
.ai-badge {
  font-size: 10px;
  background: #e8f4ff;
  color: #0096fa;
  padding: 1px 4px;
  border-radius: 999px;
  margin-left: 2px;
  vertical-align: super;
}

/* 画师卡片 */
.artist-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fafafa;
  border-radius: 16px;
  margin-bottom: 20px;
}
.artist-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-radius: 12px;
  padding: 4px;
  margin: -4px;
  transition: background 0.2s;
}
.artist-left:hover {
  background: #f5f5f5;
}
.artist-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.artist-name { font-size: 15px; font-weight: 600; color: #1a1a1a; }
.follow-btn {
  padding: 6px 20px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background: #0096FA;
  color: #fff;
  border: 2px solid #0096FA;
}
.follow-btn:hover { background: #0080dd; border-color: #0080dd; }
.follow-btn.following {
  background: #fff; color: #999; border-color: #ddd;
}
.follow-btn.following:hover { color: #FF4060; border-color: #FF4060; }
.message-btn {
  padding: 6px 20px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  color: #0096FA;
  border: 2px solid #0096FA;
}
.message-btn:hover { background: #0096FA; color: #fff; }

/* 评论区 */
.comment-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.comment-title {
  font-size: 16px; font-weight: 700; color: #1a1a1a;
  margin: 0;
  display: flex; align-items: center;
}
.comment-count-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 20px; padding: 0 6px;
  border-radius: 999px; background: #e8f4ff; color: #0096FA;
  font-size: 11px; font-weight: 700;
}

/* 评论输入框 */
.comment-composer {
  margin-bottom: 20px;
  border-radius: 16px;
  background: #f8f9fa;
  padding: 12px;
  transition: all 0.25s;
  border: 1.5px solid transparent;
}
.comment-composer.is-replying {
  border-color: #0096FA;
  background: #f0f8ff;
}
.reply-indicator {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 4px 10px;
  margin-bottom: 2px;
  border-bottom: 1px solid rgba(0,150,250,0.1);
}
.reply-indicator-text {
  font-size: 12px; color: #666;
}
.reply-indicator-text strong { color: #0096FA; }
.reply-indicator-close {
  background: none; border: none; font-size: 18px; color: #999;
  cursor: pointer; padding: 0 4px; line-height: 1; transition: color 0.15s;
}
.reply-indicator-close:hover { color: #FF4060; }
.composer-row {
  display: flex; align-items: flex-end; gap: 10px;
}
.composer-input-wrap {
  flex: 1;
}
.composer-input-wrap :deep(.el-textarea__inner) {
  border-radius: 16px; padding: 8px 12px; font-size: 13px;
  background: #fff; border-color: #e8e8e8; line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.composer-input-wrap :deep(.el-textarea__inner:focus) {
  border-color: #0096FA; box-shadow: 0 0 0 2px rgba(0,150,250,0.1);
}
.composer-send {
  width: 36px; height: 36px; border-radius: 50%;
  background: #0096FA; color: #fff; border: none;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; flex-shrink: 0;
}
.composer-send:hover:not(:disabled) { background: #0080dd; transform: scale(1.05); }
.composer-send:disabled { opacity: 0.35; cursor: default; }

.login-hint {
  text-align: center; padding: 16px; font-size: 13px;
  color: #999; background: #fafafa; border-radius: 16px; margin-bottom: 16px;
}
.login-hint a { color: #0096FA; text-decoration: none; font-weight: 600; }

/* 评论列表 */
.comments-loading {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 32px 0; color: #bbb; font-size: 13px;
}
.comments-empty {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 40px 0; color: #bbb; font-size: 13px;
}

/* 评论线程 */
.comment-thread {
  padding: 0;
}
.comment-thread + .comment-thread {
  border-top: 1px solid #f3f3f3;
}

/* 顶级评论 */
.comment-item {
  display: flex; gap: 10px; padding: 14px 0;
}
.comment-avatar { flex-shrink: 0; }
.comment-main { flex: 1; min-width: 0; }
.comment-bubble {
  background: #f5f6f7; border-radius: 0 16px 16px 16px;
  padding: 10px 14px;
}
.comment-author {
  font-size: 13px; font-weight: 600; color: #1a1a1a;
  display: inline; margin-bottom: 3px;
}

/* VIP 徽章 */
.vip-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: #fff;
  margin-left: 6px;
  vertical-align: middle;
  line-height: 1.4;
  letter-spacing: 0.5px;
}
.vip-badge.svip {
  background: linear-gradient(135deg, #e040fb, #7c4dff);
}
.vip-badge.small {
  font-size: 9px;
  padding: 0 4px;
}

/* VIP/SVIP 评论高亮 */
.vip-comment > .comment-main > .comment-bubble {
  background: linear-gradient(135deg, #fff8e1, #fff3e0);
  border-left: 3px solid #ff9800;
}
.svip-comment > .comment-main > .comment-bubble {
  background: linear-gradient(135deg, #f3e5f5, #ede7f6);
  border-left: 3px solid #ab47bc;
}
.comment-content {
  font-size: 13px; color: #444; line-height: 1.6; margin: 0;
  word-break: break-word; white-space: pre-wrap;
}

/* 评论底部操作 */
.comment-footer {
  display: flex; align-items: center; gap: 12px;
  padding: 5px 4px 0;
}
.comment-time {
  font-size: 11px; color: #bbb;
}
.comment-action-btn {
  background: none; border: none; font-size: 11px; color: #999;
  cursor: pointer; padding: 2px 0; transition: color 0.15s;
  display: flex; align-items: center; gap: 3px;
}
.comment-action-btn:hover { color: #0096FA; }
.comment-delete-btn:hover { color: #FF4060; }

/* 子回复容器 */
.replies-container {
  margin-left: 46px;
  padding-left: 14px;
  border-left: 2px solid #e8ecf0;
  position: relative;
}
.replies-container::before {
  content: '';
  position: absolute; left: -2px; top: 0; bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #0096FA 0%, #e8ecf0 100%);
  border-radius: 1px;
}

/* 子回复项 */
.reply-item {
  display: flex; gap: 8px; padding: 10px 0;
}
.reply-item + .reply-item {
  border-top: 1px solid #f5f6f7;
}
.reply-avatar { flex-shrink: 0; }
.reply-main { flex: 1; min-width: 0; }
.reply-bubble {
  background: #fafbfc; border-radius: 0 16px 16px 16px;
  padding: 8px 12px;
}
.reply-author {
  font-size: 12px; font-weight: 600; color: #1a1a1a;
}
.reply-to-tag {
  font-size: 11px; color: #999;
  display: inline-flex; align-items: center; gap: 2px;
  margin-left: 4px;
}
.reply-to-name {
  color: #0096FA; font-weight: 500;
}
.reply-content {
  font-size: 12.5px; color: #555; line-height: 1.55; margin: 3px 0 0;
  word-break: break-word; white-space: pre-wrap;
}

.comment-pager {
  display: flex; justify-content: center; padding-top: 16px;
}

@media (max-width: 900px) {
  .detail-content { grid-template-columns: 1fr; grid-template-rows: auto; }
  .image-viewer, .below-image, .info-panel { grid-column: 1; grid-row: auto; }
  .info-panel { position: static; max-height: none; }
  .more-works-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
