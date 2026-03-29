<template>
  <div class="contests-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">创作比赛</h1>
        <p class="page-desc">参与幻画空间创作比赛，展示你的才华</p>
      </div>

      <!-- 状态筛选 -->
      <div class="filter-bar">
        <div class="filter-tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: currentStatus === tab.value }"
            @click="filterByStatus(tab.value)"
          >{{ tab.label }}</button>
        </div>
      </div>

      <!-- 比赛列表 -->
      <div v-loading="loading" class="contest-list">
        <div v-if="contests.length === 0 && !loading" class="empty-tip">
          <el-empty description="暂无比赛" />
        </div>

        <div v-for="contest in contests" :key="contest.id" class="contest-card" @click="goToDetail(contest.id)">
          <div class="contest-cover">
            <img v-if="contest.coverImage" :src="contest.coverImage" alt="" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.3">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
            </div>
            <div class="contest-status-badge" :class="'status-' + contest.status">
              {{ statusLabel(contest.status) }}
            </div>
          </div>
          <div class="contest-info">
            <h3 class="contest-name">{{ contest.title }}</h3>
            <p class="contest-desc">{{ contest.description || '暂无描述' }}</p>
            <div class="contest-meta">
              <div class="meta-item">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                <span>{{ contest.entryCount || 0 }} 件参赛</span>
              </div>
              <div class="meta-item">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                <span v-if="contest.status === 'ACTIVE'">投稿截止: {{ formatDate(contest.endTime) }}</span>
                <span v-else-if="contest.status === 'VOTING'">投票截止: {{ formatDate(contest.votingEndTime) }}</span>
                <span v-else-if="contest.status === 'UPCOMING'">开始: {{ formatDate(contest.startTime) }}</span>
                <span v-else>已结束</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.size" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="loadContests"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getContests } from '@/api/contest'

const router = useRouter()
const loading = ref(false)
const contests = ref([])
const currentStatus = ref('')
const pagination = ref({ page: 1, size: 12, total: 0 })

const statusTabs = [
  { label: '全部', value: '' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '投票中', value: 'VOTING' },
  { label: '即将开始', value: 'UPCOMING' },
  { label: '已结束', value: 'ENDED' }
]

const statusLabel = (s) => {
  const map = { UPCOMING: '即将开始', ACTIVE: '投稿中', VOTING: '投票中', ENDED: '已结束', CANCELLED: '已取消' }
  return map[s] || s
}

const formatDate = (dt) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 16)
}

const filterByStatus = (status) => {
  currentStatus.value = status
  pagination.value.page = 1
  loadContests()
}

const loadContests = async () => {
  loading.value = true
  try {
    const params = { page: pagination.value.page, size: pagination.value.size }
    if (currentStatus.value) params.status = currentStatus.value
    const res = await getContests(params)
    const data = res.data
    contests.value = data.records || []
    pagination.value.total = data.total || 0
  } catch (e) {
    console.error('加载比赛列表失败:', e)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/contests/${id}`)
}

onMounted(() => {
  loadContests()
})
</script>

<style scoped>
.contests-page {
  min-height: 100vh;
  background: #fff;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 8px;
}

.page-desc {
  font-size: 15px;
  color: var(--px-text-tertiary, #8c8c8c);
  margin: 0;
}

.filter-bar {
  margin-bottom: 24px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  background: #fff;
  border: 1px solid var(--px-border, #e8e8e8);
  color: var(--px-text-secondary, #595959);
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab:hover {
  border-color: var(--px-blue, #0096FA);
  color: var(--px-blue, #0096FA);
}

.filter-tab.active {
  background: var(--px-blue, #0096FA);
  border-color: var(--px-blue, #0096FA);
  color: #fff;
}

.contest-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.contest-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.contest-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
}

.contest-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #EFF6FF, #ECFDF5, #F5F3FF);
}

.contest-cover img {
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
  color: #999;
}

.contest-status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.status-UPCOMING { background: rgba(144,147,153,0.85); }
.status-ACTIVE { background: rgba(103,194,58,0.9); }
.status-VOTING { background: rgba(230,162,60,0.9); }
.status-ENDED { background: rgba(96,98,102,0.85); }
.status-CANCELLED { background: rgba(245,108,108,0.9); }

.contest-info {
  padding: 16px 20px 20px;
}

.contest-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contest-desc {
  font-size: 13px;
  color: var(--px-text-tertiary, #8c8c8c);
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contest-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--px-text-tertiary, #8c8c8c);
}

.meta-item svg {
  opacity: 0.6;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.empty-tip {
  grid-column: 1 / -1;
  padding: 60px 0;
}

@media (max-width: 768px) {
  .contest-list {
    grid-template-columns: 1fr;
  }
  .page-title {
    font-size: 18px;
  }
}
</style>
