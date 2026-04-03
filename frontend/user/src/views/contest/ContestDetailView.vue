<template>
  <div class="contest-detail-page">
    <div v-loading="loading" class="page-container">
      <!-- 比赛信息 -->
      <div v-if="contest" class="contest-header">
        <div class="contest-cover-area">
          <img v-if="contest.coverImage" :src="contest.coverImage" alt="" class="cover-img" />
          <div v-else class="cover-placeholder">
            <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.3">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
          </div>
          <div class="status-badge" :class="'status-' + contest.status">{{ statusLabel(contest.status) }}</div>
        </div>
        <div class="contest-meta-area">
          <h1 class="contest-title">{{ contest.title }}</h1>
          <p class="contest-description">{{ contest.description || '暂无描述' }}</p>

          <div class="time-info">
            <div class="time-row">
              <span class="time-label">投稿时间</span>
              <span class="time-value">{{ formatDate(contest.startTime) }} ~ {{ formatDate(contest.endTime) }}</span>
            </div>
            <div class="time-row">
              <span class="time-label">投票截止</span>
              <span class="time-value">{{ formatDate(contest.votingEndTime) }}</span>
            </div>
            <div class="time-row">
              <span class="time-label">参赛数量</span>
              <span class="time-value">{{ contest.entryCount || 0 }} 件作品</span>
            </div>
            <div v-if="contest.maxEntriesPerArtist" class="time-row">
              <span class="time-label">每人限制</span>
              <span class="time-value">{{ contest.maxEntriesPerArtist }} 件</span>
            </div>
          </div>

          <!-- 规则 & 奖励 -->
          <div v-if="contest.rules" class="detail-section">
            <h3 class="section-label">比赛规则</h3>
            <p class="section-text">{{ contest.rules }}</p>
          </div>
          <div v-if="contest.rewardInfo" class="detail-section">
            <h3 class="section-label">奖励信息</h3>
            <p class="section-text">{{ contest.rewardInfo }}</p>
          </div>

          <!-- 参赛按钮（仅画师 & 投稿中可操作） -->
          <div v-if="canSubmit" class="action-area">
            <el-button type="primary" @click="showSubmitDialog = true">提交参赛作品</el-button>
          </div>
        </div>
      </div>

      <!-- 排行榜 / 参赛作品 -->
      <div v-if="contest" class="entries-section">
        <h2 class="section-title">
          {{ contest.status === 'ENDED' ? '最终排名' : '参赛作品' }}
          <span class="entry-count">({{ entriesPagination.total }} 件)</span>
        </h2>

        <div v-loading="entriesLoading" class="entries-grid">
          <div v-if="entries.length === 0 && !entriesLoading" class="empty-entries">
            <el-empty description="暂无参赛作品" />
          </div>

          <div v-for="(entry, idx) in entries" :key="entry.id" class="entry-card">
            <!-- 排名标记 -->
            <div v-if="entry.rankPosition && entry.rankPosition <= 3" class="rank-medal" :class="'rank-' + entry.rankPosition">
              {{ entry.rankPosition === 1 ? '🥇' : entry.rankPosition === 2 ? '🥈' : '🥉' }}
            </div>
            <div v-else-if="contest.status === 'ENDED' || contest.status === 'VOTING'" class="rank-number">
              #{{ entry.rankPosition || (idx + 1) }}
            </div>

            <div class="entry-image" @click="previewImage(entry.imageUrl)">
              <img :src="entry.thumbnailUrl || entry.imageUrl" :alt="entry.title" />
            </div>

            <div class="entry-info">
              <h4 class="entry-title">{{ entry.title }}</h4>
              <div class="entry-artist-info">
                <el-avatar :size="22" :src="entry.artistAvatar">{{ (entry.artistName || '画').charAt(0) }}</el-avatar>
                <router-link :to="'/artist/' + entry.artistId" class="entry-artist-link">{{ entry.artistName }}</router-link>
              </div>
              <p v-if="entry.description" class="entry-desc">{{ entry.description }}</p>

              <div class="entry-scores">
                <div class="score-display">
                  <span class="score-value">{{ (entry.averageScore || 0).toFixed(1) }}</span>
                  <span class="score-label">平均分</span>
                </div>
                <div class="vote-count-display">
                  <span class="vote-count-value">{{ entry.voteCount || 0 }}</span>
                  <span class="score-label">投票数</span>
                </div>
              </div>

              <!-- 投票区域（仅投票期间 & 未投过） -->
              <div v-if="canVote && !entry.hasVoted && !isMyEntry(entry)" class="vote-area">
                <div class="vote-score-select">
                  <span class="vote-label">评分:</span>
                  <el-rate
                    v-model="voteForm[entry.id]"
                    :max="10"
                    :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                    :low-threshold="3"
                    :high-threshold="7"
                    show-score
                    score-template="{value}分"
                  />
                </div>
                <div class="vote-comment">
                  <el-input
                    v-model="voteComments[entry.id]"
                    placeholder="写点评价（可选）"
                    size="small"
                    maxlength="200"
                    clearable
                  />
                </div>
                <el-button type="primary" size="small" :loading="votingEntryId === entry.id" @click="handleVote(entry)">投票</el-button>
              </div>

              <!-- 已投票提示 -->
              <div v-if="entry.hasVoted" class="voted-hint">
                <el-tag type="info" size="small">已评分: {{ entry.myScore }}分</el-tag>
              </div>

              <!-- 自己的作品 -->
              <div v-if="isMyEntry(entry)" class="my-entry-hint">
                <el-tag type="warning" size="small">我的参赛作品</el-tag>
              </div>

              <!-- 查看评价按钮 -->
              <div v-if="entry.voteCount > 0" class="reviews-toggle">
                <button class="toggle-reviews-btn" @click="toggleReviews(entry)">
                  {{ expandedEntries[entry.id] ? '收起评价' : '查看评价' }}
                  <span class="review-count">({{ entry.voteCount }}条)</span>
                  <svg :class="{ rotated: expandedEntries[entry.id] }" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"/>
                  </svg>
                </button>
              </div>

              <!-- 评价列表（展开时显示） -->
              <div v-if="expandedEntries[entry.id]" class="reviews-section">
                <div v-if="entryVotes[entry.id]?.loading" class="reviews-loading">
                  <el-icon class="is-loading"><svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M512 64a448 448 0 1 0 448 448 32 32 0 0 0-64 0 384 384 0 1 1-384-384 32 32 0 0 0 0-64z" fill="currentColor"/></svg></el-icon>
                  加载中...
                </div>
                <template v-else-if="entryVotes[entry.id]?.votes?.length > 0">
                  <div v-for="vote in entryVotes[entry.id].votes" :key="vote.id" class="review-item">
                    <div class="review-header">
                      <div class="reviewer-info">
                        <el-avatar :size="24" :src="vote.voterAvatar">{{ (vote.voterName || '用户').charAt(0) }}</el-avatar>
                        <span class="reviewer-name">{{ vote.voterName || ('用户' + vote.voterId) }}</span>
                      </div>
                      <div class="review-score">
                        <el-rate :model-value="vote.score" :max="10" disabled :colors="['#F56C6C', '#E6A23C', '#67C23A']" :low-threshold="3" :high-threshold="7" size="small" />
                        <span class="score-text">{{ vote.score }}分</span>
                      </div>
                    </div>
                    <p v-if="vote.comment" class="review-comment">{{ vote.comment }}</p>
                    <span class="review-time">{{ formatDate(vote.createdAt) }}</span>
                  </div>
                  <div v-if="entryVotes[entry.id].total > entryVotes[entry.id].votes.length" class="load-more-reviews">
                    <el-button text type="primary" size="small" @click="loadMoreReviews(entry)">加载更多评价</el-button>
                  </div>
                </template>
                <div v-else class="no-reviews">暂无评价内容</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="entriesPagination.total > entriesPagination.size" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="entriesPagination.page"
            :page-size="entriesPagination.size"
            :total="entriesPagination.total"
            layout="prev, pager, next"
            @current-change="loadEntries"
          />
        </div>
      </div>
    </div>

    <!-- 提交参赛作品弹窗 -->
    <el-dialog v-model="showSubmitDialog" title="提交参赛作品" width="560px" :close-on-click-modal="false">
      <el-form :model="submitForm" :rules="submitRules" ref="submitFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="submitForm.title" placeholder="为你的参赛作品起个名字" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="submitForm.description" type="textarea" :rows="3" placeholder="描述你的创作理念（可选）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="作品图片" prop="imageUrl" required>
          <div class="upload-area">
            <div v-if="submitForm.imageUrl" class="preview-box">
              <img :src="submitForm.imageUrl" alt="" />
              <div class="preview-remove" @click="submitForm.imageUrl = ''">×</div>
            </div>
            <el-upload
              v-else
              class="image-uploader"
              action="/api/files/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <div class="upload-trigger">
                <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.4">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                <span>点击上传作品图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitEntry">提交</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-image-viewer v-if="previewVisible" :url-list="[previewUrl]" @close="previewVisible = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContest, getContestEntries, submitEntry, voteEntry, getEntryVotes } from '@/api/contest'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const contestId = route.params.id

const loading = ref(true)
const contest = ref(null)
const entries = ref([])
const entriesLoading = ref(false)
const entriesPagination = ref({ page: 1, size: 20, total: 0 })

// vote form state: entryId => score (1-10)
const voteForm = ref({})
const voteComments = ref({})
const votingEntryId = ref(null)

// submit entry state
const showSubmitDialog = ref(false)
const submitFormRef = ref(null)
const submitting = ref(false)
const submitForm = ref({ title: '', description: '', imageUrl: '', thumbnailUrl: '' })
const submitRules = {
  title: [{ required: true, message: '请输入作品标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传作品图片', trigger: 'change' }]
}

// image preview
const previewVisible = ref(false)
const previewUrl = ref('')

// reviews state
const expandedEntries = ref({})
const entryVotes = ref({})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const canSubmit = computed(() => {
  return contest.value?.status === 'ACTIVE' && userStore.isArtist && userStore.isAuthenticated
})

const canVote = computed(() => {
  return contest.value?.status === 'VOTING' && userStore.isAuthenticated
})

const isMyEntry = (entry) => {
  return userStore.user?.id && entry.artistId === userStore.user.id
}

const statusLabel = (s) => {
  const map = { UPCOMING: '即将开始', ACTIVE: '投稿中', VOTING: '投票中', ENDED: '已结束', CANCELLED: '已取消' }
  return map[s] || s
}

const formatDate = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 16)
}

const loadContest = async () => {
  loading.value = true
  try {
    const res = await getContest(contestId)
    contest.value = res.data
  } catch (e) {
    ElMessage.error('加载比赛信息失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadEntries = async () => {
  entriesLoading.value = true
  try {
    const res = await getContestEntries(contestId, {
      page: entriesPagination.value.page,
      size: entriesPagination.value.size
    })
    const data = res.data
    entries.value = data.records || []
    entriesPagination.value.total = data.total || 0
  } catch (e) {
    console.error('加载参赛作品失败:', e)
    ElMessage.error('加载参赛作品失败')
  } finally {
    entriesLoading.value = false
  }
}

const handleVote = async (entry) => {
  const score = voteForm.value[entry.id]
  if (!score || score < 1) {
    ElMessage.warning('请先选择评分（1-10分）')
    return
  }
  votingEntryId.value = entry.id
  try {
    await voteEntry(contestId, {
      entryId: entry.id,
      score: Math.round(score),
      comment: voteComments.value[entry.id] || ''
    })
    ElMessage.success('投票成功')
    // 重新加载以更新投票状态
    await loadEntries()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '投票失败')
  } finally {
    votingEntryId.value = null
  }
}

const handleUploadSuccess = (response) => {
  if (response.data) {
    submitForm.value.imageUrl = response.data.imageUrl || response.data
    submitForm.value.thumbnailUrl = response.data.thumbnailUrl || response.data.imageUrl || response.data
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片格式文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const handleSubmitEntry = async () => {
  if (!submitFormRef.value) return
  await submitFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!submitForm.value.imageUrl) {
      ElMessage.warning('请上传作品图片')
      return
    }
    submitting.value = true
    try {
      await submitEntry(contestId, {
        title: submitForm.value.title,
        description: submitForm.value.description,
        imageUrl: submitForm.value.imageUrl,
        thumbnailUrl: submitForm.value.thumbnailUrl || submitForm.value.imageUrl
      })
      ElMessage.success('参赛作品提交成功！')
      showSubmitDialog.value = false
      submitForm.value = { title: '', description: '', imageUrl: '', thumbnailUrl: '' }
      // 刷新列表和比赛信息
      await Promise.all([loadContest(), loadEntries()])
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const previewImage = (url) => {
  if (url) {
    previewUrl.value = url
    previewVisible.value = true
  }
}

const toggleReviews = async (entry) => {
  const id = entry.id
  if (expandedEntries.value[id]) {
    expandedEntries.value[id] = false
    return
  }
  expandedEntries.value[id] = true
  if (!entryVotes.value[id]) {
    entryVotes.value[id] = { votes: [], total: 0, page: 1, loading: true }
    try {
      const res = await getEntryVotes(id, { page: 1, size: 10 })
      const data = res.data
      entryVotes.value[id].votes = data.records || []
      entryVotes.value[id].total = data.total || 0
    } catch (e) {
      console.error('加载评价失败:', e)
    } finally {
      entryVotes.value[id].loading = false
    }
  }
}

const loadMoreReviews = async (entry) => {
  const id = entry.id
  const state = entryVotes.value[id]
  if (!state) return
  state.page++
  try {
    const res = await getEntryVotes(id, { page: state.page, size: 10 })
    const data = res.data
    state.votes.push(...(data.records || []))
  } catch (e) {
    console.error('加载更多评价失败:', e)
    state.page--
  }
}

onMounted(async () => {
  await loadContest()
  await loadEntries()
})
</script>

<style scoped>
.contest-detail-page {
  min-height: 100vh;
  background: #fff;
}

.page-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px;
}

.contest-header {
  display: flex;
  gap: 32px;
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  margin-bottom: 32px;
}

.contest-cover-area {
  position: relative;
  flex-shrink: 0;
  width: 320px;
  height: 240px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #e8ecf4 0%, #d5dce8 100%);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #8c8c8c;
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.status-UPCOMING { background: rgba(144,147,153,0.85); }
.status-ACTIVE { background: rgba(82,196,26,0.9); }
.status-VOTING { background: rgba(250,173,20,0.9); }
.status-ENDED { background: rgba(96,98,102,0.85); }
.status-CANCELLED { background: rgba(245,108,108,0.9); }

.contest-meta-area {
  flex: 1;
  min-width: 0;
}

.contest-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 8px;
}

.contest-description {
  font-size: 14px;
  color: var(--px-text-secondary, #595959);
  margin: 0 0 16px;
  line-height: 1.6;
}

.time-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  margin-bottom: 16px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.time-label {
  color: var(--px-text-tertiary, #8c8c8c);
  font-weight: 500;
}

.time-value {
  color: var(--px-text-primary, #1f1f1f);
}

.detail-section {
  margin-bottom: 12px;
}

.section-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--px-text-secondary, #595959);
  margin: 0 0 4px;
}

.section-text {
  font-size: 13px;
  color: var(--px-text-secondary, #595959);
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.action-area {
  margin-top: 16px;
}

.action-area :deep(.el-button) {
  border-radius: 999px;
}

.vote-area :deep(.el-button) {
  border-radius: 999px;
}

/* 参赛作品区域 */
.entries-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 20px;
}

.entry-count {
  font-size: 14px;
  font-weight: 400;
  color: var(--px-text-tertiary, #8c8c8c);
}

.entries-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.entry-card {
  display: flex;
  gap: 20px;
  padding: 16px;
  border: 1px solid var(--px-border, #e8e8e8);
  border-radius: 16px;
  position: relative;
  transition: all 0.2s;
}

.entry-card:hover {
  border-color: var(--px-blue, #0096FA);
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.rank-medal {
  position: absolute;
  top: -8px;
  left: -8px;
  font-size: 28px;
  z-index: 1;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.rank-number {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--px-text-tertiary, #8c8c8c);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.entry-image {
  flex-shrink: 0;
  width: 200px;
  height: 150px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  background: #f5f5f5;
}

.entry-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.entry-image:hover img {
  transform: scale(1.05);
}

.entry-info {
  flex: 1;
  min-width: 0;
}

.entry-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 4px;
}

.entry-artist {
  font-size: 13px;
  color: var(--px-text-tertiary, #8c8c8c);
  margin: 0 0 6px;
}

.entry-artist-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 4px 0 8px;
}

.entry-artist-link {
  font-size: 13px;
  color: var(--px-primary, #1890ff);
  text-decoration: none;
  transition: color 0.2s;
}

.entry-artist-link:hover {
  color: var(--px-primary-dark, #096dd9);
  text-decoration: underline;
}

.entry-desc {
  font-size: 13px;
  color: var(--px-text-secondary, #595959);
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.entry-scores {
  display: flex;
  gap: 24px;
  margin-bottom: 10px;
}

.score-display,
.vote-count-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value {
  font-size: 22px;
  font-weight: 700;
  color: #e6a23c;
}

.vote-count-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
}

.score-label {
  font-size: 12px;
  color: var(--px-text-tertiary, #8c8c8c);
}

.vote-area {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.vote-score-select {
  display: flex;
  align-items: center;
  gap: 6px;
}

.vote-label {
  font-size: 13px;
  color: var(--px-text-secondary, #595959);
  white-space: nowrap;
}

.vote-comment {
  flex: 1;
  min-width: 120px;
  max-width: 240px;
}

.voted-hint,
.my-entry-hint {
  margin-top: 4px;
}

/* 评价区域 */
.reviews-toggle {
  margin-top: 8px;
}

.toggle-reviews-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--px-blue, #0096FA);
  font-size: 13px;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.2s;
}

.toggle-reviews-btn:hover {
  color: #0080d4;
}

.toggle-reviews-btn svg {
  transition: transform 0.2s;
}

.toggle-reviews-btn svg.rotated {
  transform: rotate(180deg);
}

.review-count {
  color: var(--px-text-tertiary, #8c8c8c);
}

.reviews-section {
  margin-top: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
}

.reviews-loading {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--px-text-tertiary, #8c8c8c);
  font-size: 13px;
  padding: 12px 0;
  justify-content: center;
}

.review-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--px-border, #e8e8e8);
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reviewer-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--px-text-primary, #1f1f1f);
}

.review-score {
  display: flex;
  align-items: center;
  gap: 6px;
}

.score-text {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.review-comment {
  font-size: 13px;
  color: var(--px-text-secondary, #595959);
  margin: 4px 0;
  line-height: 1.5;
}

.review-time {
  font-size: 11px;
  color: var(--px-text-tertiary, #8c8c8c);
}

.load-more-reviews {
  text-align: center;
  padding-top: 8px;
}

.no-reviews {
  text-align: center;
  color: var(--px-text-tertiary, #8c8c8c);
  font-size: 13px;
  padding: 12px 0;
}

/* 提交弹窗 */
.upload-area {
  width: 100%;
}

.preview-box {
  position: relative;
  width: 240px;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--px-border, #e8e8e8);
}

.preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.preview-remove:hover {
  background: rgba(245,108,108,0.9);
}

.image-uploader {
  width: 240px;
}

.upload-trigger {
  width: 240px;
  height: 180px;
  border: 2px dashed var(--px-border, #ddd);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: var(--px-text-tertiary, #8c8c8c);
  font-size: 13px;
  transition: border-color 0.2s;
}

.upload-trigger:hover {
  border-color: var(--px-blue, #0096FA);
  color: var(--px-blue, #0096FA);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-entries {
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .contest-header {
    flex-direction: column;
  }

  .contest-cover-area {
    width: 100%;
    height: 200px;
  }

  .entry-card {
    flex-direction: column;
  }

  .entry-image {
    width: 100%;
    height: 200px;
  }

  .vote-area {
    flex-direction: column;
    align-items: flex-start;
  }

  .vote-comment {
    max-width: 100%;
  }
}

/* 幻画空间 pill buttons */
:deep(.el-dialog__footer .el-button) {
  border-radius: 999px;
}

:deep(.el-tag) {
  border-radius: 999px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next),
:deep(.el-pagination .el-pager li) {
  border-radius: 999px;
}
</style>
