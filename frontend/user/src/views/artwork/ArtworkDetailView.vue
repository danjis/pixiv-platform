<template>
  <div class="detail-page">
    <div class="detail-glow glow-a"></div>
    <div class="detail-glow glow-b"></div>
    <div class="detail-grid"></div>

    <div v-if="loading" class="detail-layout skeleton-layout">
      <div class="image-stage">
        <SkeletonBlock height="520px" radius="20px" />
      </div>
      <div class="support-stack">
        <div class="info-panel">
          <SkeletonBlock width="72%" height="24px" />
          <SkeletonBlock width="42%" height="14px" />
          <SkeletonBlock width="100%" height="72px" radius="14px" />
        </div>
      </div>
    </div>

    <div v-else-if="artwork" class="detail-layout">
      <section class="image-stage">
        <div class="main-viewer">
          <template v-if="artworkImages.length > 1">
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
          </template>
          <template v-else>
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
          </template>
        </div>

        <div v-if="artworkImages.length > 1" class="image-nav">
          <button class="nav-arrow" :disabled="currentImageIndex === 0" @click="currentImageIndex--" type="button">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
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
          <button class="nav-arrow" :disabled="currentImageIndex >= artworkImages.length - 1" @click="currentImageIndex++" type="button">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </button>
        </div>

        <div v-if="artworkImages.length > 1" class="thumbnail-strip">
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

        <div class="surface-metrics">
          <div class="metric">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            <span>{{ formatNumber(artwork.viewCount) }} 浏览</span>
          </div>
          <div class="metric">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#FF4060" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
            </svg>
            <span>{{ formatNumber(artwork.likeCount) }} 喜欢</span>
          </div>
          <div class="metric">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#FFB800" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
            </svg>
            <span>{{ formatNumber(artwork.favoriteCount) }} 收藏</span>
          </div>
          <div class="metric" v-if="artwork.isAigc">
            <span class="aigc-tag">AIGC</span>
          </div>
        </div>

        <div class="related-block" v-if="artistWorks.length > 0">
          <div class="block-head">
            <div>
              <h3>{{ artwork.artistName }} 的其他作品</h3>
              <p>继续发现同一位创作者的内容</p>
            </div>
            <button class="text-link" @click="goToArtistProfile" type="button">查看全部</button>
          </div>
          <div class="related-grid">
            <div v-for="item in artistWorks" :key="item.id" class="mini-card" @click="goToArtwork(item.id)">
              <div class="mini-thumb">
                <img :src="item.thumbnailUrl || item.imageUrl" :alt="item.title" />
              </div>
              <div class="mini-info">
                <span class="mini-title">{{ item.title }}</span>
                <span class="mini-like">{{ formatNumber(item.likeCount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <aside class="support-stack">
        <section class="info-panel sticky">
          <div class="action-bar">
            <button class="action-btn" :class="{ active: artwork.isLiked }" @click="handleLike" :disabled="!userStore.isAuthenticated" type="button">
              <svg viewBox="0 0 24 24" width="20" height="20" :fill="artwork.isLiked ? '#FF4060' : 'none'" :stroke="artwork.isLiked ? '#FF4060' : 'currentColor'" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
              </svg>
              <span>{{ formatNumber(artwork.likeCount) }}</span>
            </button>
            <button class="action-btn" :class="{ active: artwork.isFavorited }" @click="handleFavorite" :disabled="!userStore.isAuthenticated" type="button">
              <svg viewBox="0 0 24 24" width="20" height="20" :fill="artwork.isFavorited ? '#FFB800' : 'none'" :stroke="artwork.isFavorited ? '#FFB800' : 'currentColor'" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
              </svg>
              <span>{{ formatNumber(artwork.favoriteCount) }}</span>
            </button>
            <button class="action-btn muted" disabled type="button">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <span>{{ formatNumber(artwork.viewCount) }}</span>
            </button>
          </div>

          <div class="header-block">
            <span class="page-kicker">作品详情</span>
            <h1 class="artwork-title">{{ artwork.title }}</h1>
            <div class="publish-time">{{ formatDate(artwork.createdAt) }}</div>
          </div>

          <div class="summary-grid">
            <div class="summary-card">
              <span class="summary-label">创作者</span>
              <span class="summary-value">{{ artwork.artistName }}</span>
            </div>
            <div class="summary-card">
              <span class="summary-label">作品类型</span>
              <span class="summary-value">{{ artwork.isAigc ? 'AIGC 作品' : '原创作品' }}</span>
            </div>
            <div class="summary-card">
              <span class="summary-label">互动</span>
              <span class="summary-value">{{ formatNumber(artwork.commentCount) }} 评论</span>
            </div>
          </div>

          <div class="artist-card">
            <div class="artist-left" @click="goToArtistProfile">
              <el-avatar :size="48" :src="artwork.artistAvatar || defaultAvatar">
                {{ artwork.artistName?.charAt(0) }}
              </el-avatar>
              <div class="artist-meta">
                <div class="artist-name">{{ artwork.artistName }}</div>
                <div class="artist-note">点击进入画师主页</div>
              </div>
            </div>
            <div class="artist-actions" v-if="userStore.isAuthenticated && artwork.artistId !== userStore.user?.id">
              <button class="follow-btn" :class="{ following: isFollowing }" @click="handleFollow" type="button">
                {{ isFollowing ? '已关注' : '+ 关注' }}
              </button>
            </div>
          </div>

          <div v-if="artwork.description" class="artwork-desc">
            <div class="section-label">作品说明</div>
            <p>{{ artwork.description }}</p>
          </div>

          <div class="tag-block">
            <div class="section-label">相关标签</div>
            <div class="artwork-tags">
              <template v-if="artwork.tags && artwork.tags.length > 0">
                <button
                  v-for="tag in artwork.tags"
                  :key="tag.name"
                  class="tag-chip"
                  @click="searchByTag(tag.name)"
                  type="button"
                >
                  #{{ tag.nameZh || tag.name_zh || tag.name }}
                  <span v-if="tag.source === 'AUTO'" class="ai-badge">AI</span>
                </button>
              </template>
              <template v-else-if="aiTagLoading">
                <span class="ai-tag-loading">
                  <el-icon class="is-loading" :size="16"><Loading /></el-icon>
                  AI 标签生成中...
                </span>
              </template>
            </div>
          </div>

          <div class="comment-section">
            <div class="comment-header">
              <h3 class="comment-title">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: -3px; margin-right: 4px; color: #0096FA;">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
                评论
              </h3>
              <span v-if="artwork.commentCount" class="comment-count-badge">{{ artwork.commentCount }}</span>
            </div>

            <div v-if="userStore.isAuthenticated" class="comment-composer" :class="{ 'is-replying': replyTo }">
              <div v-if="replyTo" class="reply-indicator">
                <span class="reply-indicator-text">回复 <strong>@{{ replyTo.username }}</strong></span>
                <button class="reply-indicator-close" @click="cancelReply" type="button">&times;</button>
              </div>
              <div class="composer-row">
                <el-avatar :size="32" :src="userStore.user?.avatarUrl || defaultAvatar" />
                <div class="composer-input-wrap">
                  <el-input
                    ref="commentInputRef"
                    v-model="commentContent"
                    type="textarea"
                    :autosize="{ minRows: 1, maxRows: 4 }"
                    :placeholder="replyTo ? '说点什么...' : '写下你的评论...'"
                    maxlength="500"
                    resize="none"
                  />
                </div>
                <button class="composer-send" :disabled="!commentContent.trim() || submittingComment" @click="handleAddComment" type="button">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="22" y1="2" x2="11" y2="13" />
                    <polygon points="22 2 15 22 11 13 2 9 22 2" />
                  </svg>
                </button>
              </div>
            </div>
            <div v-else class="login-hint">
              <router-link to="/login">登录</router-link>后发表评论
            </div>

            <div v-if="loadingComments" class="comments-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载评论中...</span>
            </div>
            <div v-else-if="comments.length === 0" class="comments-empty">
              <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#d5d5d5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
              <span>还没有评论，来留下第一条吧</span>
            </div>
            <div v-else class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-thread">
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
                      <button v-if="userStore.isAuthenticated" class="comment-action-btn" @click="startReply(comment)" type="button">
                        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                        </svg>
                        回复
                      </button>
                      <button v-if="comment.userId === userStore.user?.id" class="comment-action-btn comment-delete-btn" @click="handleDeleteComment(comment.id)" type="button">
                        删除
                      </button>
                    </div>
                  </div>
                </div>

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
                          <svg viewBox="0 0 24 24" width="10" height="10" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M9 18l6-6-6-6" />
                          </svg>
                          <span class="reply-to-name">{{ reply.replyToUsername }}</span>
                        </span>
                        <p class="reply-content">{{ reply.content }}</p>
                      </div>
                      <div class="comment-footer">
                        <span class="comment-time">{{ formatDate(reply.createdAt) }}</span>
                        <button v-if="userStore.isAuthenticated" class="comment-action-btn" @click="startReply(comment, reply)" type="button">
                          <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                          </svg>
                          回复
                        </button>
                        <button v-if="reply.userId === userStore.user?.id" class="comment-action-btn comment-delete-btn" @click="handleDeleteComment(reply.id)" type="button">
                          删除
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

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
        </section>
      </aside>
    </div>

    <div v-else class="error-state">
      <div class="error-card">
        <div class="error-icon">404</div>
        <div class="error-title">作品不存在或已被删除</div>
        <div class="error-text">页面即将返回上一页。</div>
      </div>
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

const artwork = ref(null)
const loading = ref(true)
const isFollowing = ref(false)
const aiTagLoading = ref(false)
let aiTagTimer = null

const currentImageIndex = ref(0)
const artworkImages = ref([])
const isVip = ref(false)

const comments = ref([])
const commentContent = ref('')
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const loadingComments = ref(false)
const submittingComment = ref(false)
const replyTo = ref(null)
const commentInputRef = ref(null)

const artistWorks = ref([])

function getDisplayImageUrl(img) {
  if (isVip.value && img.originalImageUrl) return img.originalImageUrl
  return img.imageUrl
}

async function loadMembership() {
  try {
    const res = await getMyMembership()
    if (res.code === 200 && res.data) {
      const level = res.data.level
      isVip.value = level === 'VIP' || level === 'SVIP'
    }
  } catch {
    isVip.value = false
  }
}

async function loadArtwork({ pollForTags = false } = {}) {
  if (!route.params.id) return
  loading.value = true
  try {
    const response = await getArtwork(route.params.id)
    if (response.code === 200 && response.data) {
      artwork.value = response.data
      currentImageIndex.value = 0
      if (response.data.images && response.data.images.length > 0) {
        artworkImages.value = response.data.images
      } else {
        artworkImages.value = [{ imageUrl: response.data.imageUrl, thumbnailUrl: response.data.thumbnailUrl }]
      }
      loadComments()
      loadArtistWorks()
      if (userStore.isAuthenticated && artwork.value.artistId) {
        checkFollow()
        loadMembership()
      }
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
    if (pollCount >= 15) {
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

async function checkFollow() {
  try {
    const res = await checkFollowStatus(artwork.value.artistId)
    if (res.code === 200) {
      isFollowing.value = res.data?.following || res.data === true
    }
  } catch {}
}

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

function goToArtistProfile() {
  if (artwork.value?.artistId) {
    router.push(`/artist/${artwork.value.artistId}`)
  }
}

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
      artistWorks.value = items.filter(a => a.id !== artwork.value.id).slice(0, 6)
    }
  } catch {}
}

function goToArtwork(id) {
  if (!id) return
  router.push(`/artworks/${id}`)
}

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

async function handleAddComment() {
  if (!commentContent.value.trim()) return
  submittingComment.value = true
  try {
    const data = { content: commentContent.value.trim() }
    if (replyTo.value) data.parentId = replyTo.value.commentId
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

function startReply(parentComment, replyTarget = null) {
  replyTo.value = {
    commentId: parentComment.id,
    username: replyTarget ? replyTarget.username : parentComment.username
  }
  commentContent.value = ''
  nextTick(() => {
    commentInputRef.value?.focus?.()
  })
}

function cancelReply() {
  replyTo.value = null
  commentContent.value = ''
}

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

function searchByTag(tagName) {
  router.push({ name: 'Artworks', query: { tags: tagName } })
}

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
  min-height: calc(100vh - 56px);
  position: relative;
  overflow: hidden;
  padding: 28px 24px 40px;
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.10), transparent 26%),
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.10), transparent 24%),
    linear-gradient(180deg, #fcfdff 0%, #f4f7fb 100%);
}
.detail-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.16), transparent 72%);
  pointer-events: none;
}
.detail-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(36px);
  pointer-events: none;
  opacity: 0.68;
}
.glow-a { width: 240px; height: 240px; left: -70px; top: 80px; background: rgba(59, 130, 246, 0.14); }
.glow-b { width: 240px; height: 240px; right: -80px; top: 50px; background: rgba(236, 72, 153, 0.12); }
.detail-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  gap: 24px;
  max-width: 1240px;
  margin: 0 auto;
  align-items: start;
}
.image-stage { display: grid; gap: 16px; }
.main-viewer {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(12px);
}
.main-image {
  width: 100%;
  min-height: 520px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}
.main-image :deep(.el-image__inner) { max-height: 80vh; }
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #c0c4cc;
  padding: 80px 0;
}
.image-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 18px;
  background: rgba(248, 250, 252, 0.95);
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
}
.nav-arrow {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #e2e8f0;
  color: #475569;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.nav-arrow:hover:not(:disabled) { background: #3b82f6; color: #fff; }
.nav-arrow:disabled { opacity: 0.35; cursor: default; }
.image-dots { display: flex; gap: 6px; }
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  cursor: pointer;
  transition: all 0.2s;
}
.dot.active { background: #3b82f6; transform: scale(1.2); }
.image-counter {
  min-width: 44px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}
.thumbnail-strip {
  display: flex;
  gap: 8px;
  padding: 10px 6px 0;
  overflow-x: auto;
}
.thumb-item {
  width: 58px;
  height: 58px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  opacity: 0.68;
  transition: all 0.2s;
}
.thumb-item.active { border-color: #3b82f6; opacity: 1; }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; display: block; }
.surface-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.metric {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  color: #475569;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.05);
}
.aigc-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}
.related-block {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 26px;
  padding: 18px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.05);
}
.block-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}
.block-head h3,
.artwork-title {
  margin: 0;
  color: #0f172a;
}
.block-head h3 { font-size: 16px; }
.block-head p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 13px;
}
.text-link {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.mini-card {
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.9);
  transition: transform 0.2s, box-shadow 0.2s;
}
.mini-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.1);
}
.mini-thumb {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
}
.mini-thumb img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.mini-card:hover .mini-thumb img { transform: scale(1.05); }
.mini-info {
  padding: 8px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.mini-title {
  font-size: 11px;
  color: #334155;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.mini-like {
  font-size: 10px;
  color: #94a3b8;
  flex-shrink: 0;
}
.support-stack { display: grid; gap: 16px; }
.info-panel {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 28px;
  padding: 22px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(12px);
}
.sticky { position: sticky; top: 80px; }
.action-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
}
.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 11px 0;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #f8fafc;
  border-radius: 18px;
  cursor: pointer;
  color: #475569;
  font-size: 13px;
  transition: all 0.2s;
}
.action-btn:hover:not(:disabled) { background: #eff6ff; color: #2563eb; }
.action-btn.active { background: #fff0f3; color: #ff4060; border-color: rgba(255, 64, 96, 0.22); }
.action-btn.muted { opacity: 0.9; }
.action-btn:disabled { opacity: 0.5; cursor: default; }
.header-block { margin-bottom: 18px; }
.page-kicker,
.section-label {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.artwork-title {
  margin-top: 10px;
  font-size: 26px;
  line-height: 1.35;
  font-weight: 800;
}
.publish-time { margin-top: 8px; color: #94a3b8; font-size: 13px; }
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}
.summary-card {
  padding: 12px 14px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  border: 1px solid rgba(226, 232, 240, 0.9);
}
.summary-label {
  display: block;
  color: #94a3b8;
  font-size: 11px;
  margin-bottom: 6px;
}
.summary-value {
  display: block;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}
.artist-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(180deg, #f8fafc, #f4f7fb);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
  margin-bottom: 18px;
}
.artist-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-radius: 14px;
  padding: 4px;
  margin: -4px;
  transition: background 0.2s;
}
.artist-left:hover { background: rgba(255, 255, 255, 0.7); }
.artist-name { font-size: 15px; font-weight: 800; color: #0f172a; }
.artist-note { margin-top: 4px; color: #94a3b8; font-size: 12px; }
.follow-btn {
  padding: 7px 20px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #fff;
  border: 1px solid #3b82f6;
}
.follow-btn.following {
  background: #fff;
  color: #94a3b8;
  border-color: #dbe3ef;
}
.follow-btn.following:hover { color: #ff4060; border-color: #ff4060; }
.artwork-desc {
  margin-bottom: 18px;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fafc, #f4f7fb);
  border: 1px solid rgba(226, 232, 240, 0.88);
}
.artwork-desc p {
  margin: 10px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
}
.tag-block {
  margin-bottom: 22px;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fafc, #f4f7fb);
  border: 1px solid rgba(226, 232, 240, 0.88);
}
.artwork-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #dbeafe;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}
.tag-chip:hover { transform: translateY(-1px); box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12); }
.ai-badge {
  font-size: 10px;
  background: rgba(255, 255, 255, 0.86);
  color: #2563eb;
  padding: 1px 5px;
  border-radius: 999px;
}
.ai-tag-loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 13px;
}
.comment-section {
  border-top: 1px solid rgba(226, 232, 240, 0.9);
  padding-top: 22px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.comment-title {
  margin: 0;
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.comment-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #e8f4ff;
  color: #0096FA;
  font-size: 11px;
  font-weight: 700;
}
.comment-composer {
  margin-bottom: 20px;
  border-radius: 18px;
  background: #f8fafc;
  padding: 14px;
  border: 1.5px solid transparent;
}
.comment-composer.is-replying {
  border-color: #0096FA;
  background: #f0f8ff;
}
.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 10px;
  margin-bottom: 2px;
  border-bottom: 1px solid rgba(0, 150, 250, 0.1);
}
.reply-indicator-text { color: #64748b; font-size: 12px; }
.reply-indicator-text strong { color: #0096FA; }
.reply-indicator-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.composer-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}
.composer-input-wrap { flex: 1; }
.composer-input-wrap :deep(.el-textarea__inner) {
  border-radius: 16px;
  padding: 10px 12px;
  font-size: 13px;
  background: #fff;
  border-color: #e2e8f0;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.composer-input-wrap :deep(.el-textarea__inner:focus) {
  border-color: #0096FA;
  box-shadow: 0 0 0 2px rgba(0, 150, 250, 0.1);
}
.composer-send {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #0096FA;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}
.composer-send:hover:not(:disabled) { background: #0080dd; transform: scale(1.05); }
.composer-send:disabled { opacity: 0.35; cursor: default; }
.login-hint {
  text-align: center;
  padding: 16px;
  font-size: 13px;
  color: #94a3b8;
  background: #fafafa;
  border-radius: 16px;
  margin-bottom: 16px;
}
.login-hint a { color: #0096FA; text-decoration: none; font-weight: 600; }
.comments-loading,
.comments-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 0;
  color: #94a3b8;
  font-size: 13px;
}
.comments-empty {
  flex-direction: column;
  padding: 40px 0;
}
.comment-thread + .comment-thread { border-top: 1px solid #f3f3f3; }
.comment-item {
  display: flex;
  gap: 10px;
  padding: 14px 0;
}
.comment-main { flex: 1; min-width: 0; }
.comment-bubble {
  background: #f5f6f7;
  border-radius: 0 16px 16px 16px;
  padding: 10px 14px;
}
.comment-author {
  display: inline;
  margin-bottom: 3px;
  color: #1a1a1a;
  font-size: 13px;
  font-weight: 700;
}
.vip-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 5px;
  border-radius: 999px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
}
.vip-badge.svip { background: linear-gradient(135deg, #e040fb, #7c4dff); }
.vip-badge.small { font-size: 9px; padding: 0 4px; }
.vip-comment > .comment-main > .comment-bubble {
  background: linear-gradient(135deg, #fff8e1, #fff3e0);
  border-left: 3px solid #ff9800;
}
.svip-comment > .comment-main > .comment-bubble {
  background: linear-gradient(135deg, #f3e5f5, #ede7f6);
  border-left: 3px solid #ab47bc;
}
.comment-content,
.reply-content {
  margin: 0;
  color: #444;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}
.comment-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 5px 4px 0;
}
.comment-time { color: #bbb; font-size: 11px; }
.comment-action-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 11px;
  cursor: pointer;
  padding: 2px 0;
  display: flex;
  align-items: center;
  gap: 3px;
}
.comment-action-btn:hover { color: #0096FA; }
.comment-delete-btn:hover { color: #FF4060; }
.replies-container {
  margin-left: 46px;
  padding-left: 14px;
  border-left: 2px solid #e8ecf0;
  position: relative;
}
.replies-container::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, #0096FA 0%, #e8ecf0 100%);
  border-radius: 1px;
}
.reply-item {
  display: flex;
  gap: 8px;
  padding: 10px 0;
}
.reply-item + .reply-item { border-top: 1px solid #f5f6f7; }
.reply-main { flex: 1; min-width: 0; }
.reply-bubble {
  background: #fafbfc;
  border-radius: 0 16px 16px 16px;
  padding: 8px 12px;
}
.reply-author { font-size: 12px; font-weight: 700; color: #1a1a1a; }
.reply-to-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-left: 4px;
  color: #999;
  font-size: 11px;
}
.reply-to-name { color: #0096FA; font-weight: 500; }
.comment-pager { display: flex; justify-content: center; padding-top: 16px; }
.error-state {
  display: flex;
  justify-content: center;
  padding: 120px 0;
}
.error-card {
  padding: 28px 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  text-align: center;
}
.error-icon {
  font-size: 34px;
  font-weight: 900;
  color: #cbd5e1;
  letter-spacing: 0.08em;
}
.error-title {
  margin-top: 10px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 800;
}
.error-text {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 13px;
}
@media (max-width: 1100px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .sticky { position: static; }
  .related-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .detail-page { padding: 18px 14px 28px; }
  .main-image { min-height: 320px; }
  .summary-grid { grid-template-columns: 1fr; }
  .related-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .action-bar { gap: 6px; }
  .artist-card { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
