<template>
  <div class="artworks-page">
    <!-- 筛选栏（标签 + 高级筛选 + 排序） -->
    <div class="filter-bar">
      <div class="filter-inner">
        <!-- 筛选标签 -->
        <div class="filter-chips">
          <button
            v-for="tag in popularTags"
            :key="tag"
            class="chip"
            :class="{ active: selectedTags.includes(tag) }"
            @click="toggleTag(tag)"
          >{{ tag }}</button>
        </div>

        <!-- 排序 + 高级筛选按钮 -->
        <div class="sort-row">
          <div class="sort-buttons">
            <button
              v-for="opt in sortOptions"
              :key="opt.value"
              class="sort-btn"
              :class="{ active: sortBy === opt.value }"
              @click="sortBy = opt.value; handleFilterChange()"
            >{{ opt.label }}</button>
          </div>
          <button class="advanced-toggle" :class="{ active: showAdvanced }" @click="showAdvanced = !showAdvanced">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 21v-7m0-4V3m8 18v-9m0-4V3m8 18v-5m0-4V3M1 14h6M9 8h6M17 16h6" stroke-linecap="round"/>
            </svg>
            高级筛选
            <span v-if="activeFilterCount > 0" class="filter-badge">{{ activeFilterCount }}</span>
          </button>
        </div>

        <!-- 高级筛选面板 -->
        <transition name="slide">
          <div v-if="showAdvanced" class="advanced-panel">
            <div class="advanced-row">
              <label class="adv-label">作品类型</label>
              <div class="adv-options">
                <button class="adv-btn" :class="{ active: isAigc === null }" @click="isAigc = null; handleFilterChange()">全部</button>
                <button class="adv-btn" :class="{ active: isAigc === false }" @click="isAigc = false; handleFilterChange()">人工创作</button>
                <button class="adv-btn" :class="{ active: isAigc === true }" @click="isAigc = true; handleFilterChange()">AI 生成</button>
              </div>
            </div>
            <div class="advanced-row">
              <label class="adv-label">时间范围</label>
              <div class="adv-options">
                <button class="adv-btn" :class="{ active: !dateFrom && !dateTo }" @click="dateFrom = ''; dateTo = ''; handleFilterChange()">全部</button>
                <button class="adv-btn" :class="{ active: datePreset === '7d' }" @click="setDatePreset('7d')">近7天</button>
                <button class="adv-btn" :class="{ active: datePreset === '30d' }" @click="setDatePreset('30d')">近30天</button>
                <button class="adv-btn" :class="{ active: datePreset === '90d' }" @click="setDatePreset('90d')">近3个月</button>
                <input type="date" v-model="dateFrom" class="date-input" @change="datePreset = ''; handleFilterChange()" />
                <span class="date-sep">—</span>
                <input type="date" v-model="dateTo" class="date-input" @change="datePreset = ''; handleFilterChange()" />
              </div>
            </div>
            <div class="advanced-row">
              <label class="adv-label">最低点赞</label>
              <div class="adv-options">
                <button class="adv-btn" :class="{ active: !minLikes }" @click="minLikes = 0; handleFilterChange()">不限</button>
                <button class="adv-btn" :class="{ active: minLikes === 10 }" @click="minLikes = 10; handleFilterChange()">10+</button>
                <button class="adv-btn" :class="{ active: minLikes === 50 }" @click="minLikes = 50; handleFilterChange()">50+</button>
                <button class="adv-btn" :class="{ active: minLikes === 100 }" @click="minLikes = 100; handleFilterChange()">100+</button>
                <button class="adv-btn" :class="{ active: minLikes === 500 }" @click="minLikes = 500; handleFilterChange()">500+</button>
              </div>
            </div>
            <div class="advanced-row actions-row">
              <button class="reset-btn" @click="resetAdvancedFilters">重置筛选</button>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 作品网格 -->
    <div class="artworks-body">
      <!-- 骨架屏（首次加载） -->
      <template v-if="loading && artworks.length === 0">
        <div class="artwork-grid">
          <div v-for="i in 18" :key="'sk-'+i" class="skeleton-card">
            <div class="skeleton-thumb"><div class="shimmer"></div></div>
            <div class="skeleton-info">
              <div class="skeleton-line w80"><div class="shimmer"></div></div>
              <div class="skeleton-line w50"><div class="shimmer"></div></div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else-if="!loading && artworks.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#d0d0d0" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <circle cx="8.5" cy="8.5" r="1.5" fill="#d0d0d0" stroke="none" />
          <path d="M21 15l-5-5L5 21" />
        </svg>
        <p class="empty-text">暂无作品</p>
        <p class="empty-hint">换个关键词试试，或浏览推荐作品</p>
      </div>

      <!-- 作品卡片网格 -->
      <template v-else>
        <div class="artwork-grid">
          <ArtworkCard
            v-for="artwork in artworks"
            :key="artwork.id"
            :artwork="artwork"
            @click="goToDetail(artwork)"
            @author-click="goToProfile(artwork)"
          />
        </div>

        <!-- 滚动加载状态 -->
        <div ref="sentinelRef" class="scroll-sentinel">
          <div v-if="loadingMore" class="load-status">
            <svg class="spin-icon" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="var(--px-blue,#0096FA)" stroke-width="2.5"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
            <span>加载中...</span>
          </div>
          <div v-else-if="noMore" class="load-status no-more">
            <span>没有更多了</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArtworks } from '@/api/artwork'
import { ElMessage } from 'element-plus'
import ArtworkCard from '@/components/ArtworkCard.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref(route.query.keyword || '')
const selectedTags = ref(route.query.tag ? [route.query.tag] : [])
const sortBy = ref('latest')
const sortOptions = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hottest' },
  { label: '最多收藏', value: 'most_favorited' },
  { label: '最多点赞', value: 'most_liked' },
  { label: '最多浏览', value: 'most_viewed' }
]
const popularTags = ref(['动漫', '少女', '风景', '插画', '原创', '同人', '二次元', '游戏'])

// 高级筛选状态
const showAdvanced = ref(false)
const isAigc = ref(null)       // null=全部, true=AI, false=人工
const dateFrom = ref('')
const dateTo = ref('')
const datePreset = ref('')
const minLikes = ref(0)

const activeFilterCount = computed(() => {
  let count = 0
  if (isAigc.value !== null) count++
  if (dateFrom.value || dateTo.value) count++
  if (minLikes.value > 0) count++
  return count
})

const setDatePreset = (preset) => {
  datePreset.value = preset
  const now = new Date()
  const fmt = (d) => d.toISOString().slice(0, 10)
  dateTo.value = fmt(now)
  if (preset === '7d') dateFrom.value = fmt(new Date(now - 7 * 864e5))
  else if (preset === '30d') dateFrom.value = fmt(new Date(now - 30 * 864e5))
  else if (preset === '90d') dateFrom.value = fmt(new Date(now - 90 * 864e5))
  handleFilterChange()
}

const resetAdvancedFilters = () => {
  isAigc.value = null
  dateFrom.value = ''
  dateTo.value = ''
  datePreset.value = ''
  minLikes.value = 0
  handleFilterChange()
}

const currentPage = ref(1)
const pageSize = ref(30)
const total = ref(0)
const artworks = ref([])
const loading = ref(false)     // 首次加载
const loadingMore = ref(false) // 加载更多
const noMore = ref(false)      // 已全部加载
const sentinelRef = ref(null)
let observer = null

const toggleTag = (tag) => {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(tag)
  handleFilterChange()
}

const buildParams = () => {
  const params = {
    page: currentPage.value,
    size: pageSize.value,
    sortBy: sortBy.value
  }
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (selectedTags.value.length > 0) params.tags = selectedTags.value.join(',')
  if (isAigc.value !== null) params.isAigc = isAigc.value
  if (dateFrom.value) params.dateFrom = dateFrom.value
  if (dateTo.value) params.dateTo = dateTo.value
  if (minLikes.value > 0) params.minLikes = minLikes.value
  return params
}

// 首次加载 / 筛选变更时重新加载
const loadArtworks = async () => {
  loading.value = true
  noMore.value = false
  currentPage.value = 1
  try {
    const response = await getArtworks(buildParams())
    if (response.code === 200 && response.data) {
      artworks.value = response.data.records || []
      total.value = response.data.total || 0
      noMore.value = artworks.value.length >= total.value
    } else {
      ElMessage.error(response.message || '加载作品列表失败')
    }
  } catch (error) {
    console.error('加载作品列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
    await nextTick()
    setupObserver()
  }
}

// 滚动触底时追加加载
const loadMore = async () => {
  if (loadingMore.value || noMore.value || loading.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const response = await getArtworks(buildParams())
    if (response.code === 200 && response.data) {
      const newRecords = response.data.records || []
      artworks.value.push(...newRecords)
      total.value = response.data.total || 0
      if (artworks.value.length >= total.value || newRecords.length === 0) {
        noMore.value = true
      }
    }
  } catch (error) {
    console.error('加载更多失败:', error)
    currentPage.value-- // 回退页码
  } finally {
    loadingMore.value = false
  }
}

// IntersectionObserver 实现无限滚动
const setupObserver = () => {
  if (observer) observer.disconnect()
  if (!sentinelRef.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) loadMore()
    },
    { rootMargin: '200px' }
  )
  observer.observe(sentinelRef.value)
}

const handleSearch = () => { loadArtworks() }
const handleFilterChange = () => { loadArtworks() }

const goToDetail = (artwork) => {
  router.push({ name: 'ArtworkDetail', params: { id: artwork.id } })
}

const goToProfile = (artwork) => {
  if (artwork.artistId) {
    router.push(`/artist/${artwork.artistId}`)
  }
}

// 监听导航栏搜索框传入的 keyword / tag query
watch(() => route.query.keyword, (val) => {
  searchKeyword.value = val || ''
  loadArtworks()
})

watch(() => route.query.tag, (val) => {
  if (val) {
    selectedTags.value = [val]
  } else {
    selectedTags.value = []
  }
  loadArtworks()
})

onMounted(() => {
  loadArtworks()
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.artworks-page {
  min-height: calc(100vh - 56px);
  background: var(--px-bg-secondary, #f5f5f5);
}

/* ===== 筛选栏 ===== */
.filter-bar {
  background: #fff;
  border-bottom: 1px solid var(--px-border-light, #f0f0f0);
  padding: 16px 0 12px;
}

.filter-inner {
  max-width: var(--px-max-width, 1245px);
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 标签筛选 */
.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 4px 14px;
  border-radius: 16px;
  border: 1px solid var(--px-border, #e0e0e0);
  background: #fff;
  font-size: 13px;
  color: var(--px-text-secondary, #555);
  cursor: pointer;
  transition: all 0.15s;
}

.chip:hover {
  border-color: var(--px-blue, #0096FA);
  color: var(--px-blue, #0096FA);
}

.chip.active {
  background: var(--px-blue, #0096FA);
  border-color: var(--px-blue, #0096FA);
  color: #fff;
}

/* 排序 */
.sort-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}

.sort-buttons {
  display: flex;
  gap: 4px;
}

.sort-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--px-text-tertiary, #999);
  cursor: pointer;
  transition: all 0.15s;
}

.sort-btn:hover {
  color: var(--px-text-primary, #1f1f1f);
}

.sort-btn.active {
  color: var(--px-blue, #0096FA);
  background: rgba(0, 150, 250, 0.08);
}

/* 高级筛选按钮 */
.advanced-toggle {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 14px;
  border: 1px solid var(--px-border, #e0e0e0);
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  color: var(--px-text-secondary, #555);
  cursor: pointer;
  transition: all 0.15s;
  position: relative;
}

.advanced-toggle:hover, .advanced-toggle.active {
  border-color: var(--px-blue, #0096FA);
  color: var(--px-blue, #0096FA);
}

.filter-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: var(--px-blue, #0096FA);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 0 4px;
}

/* 高级筛选面板 */
.advanced-panel {
  background: var(--px-bg-secondary, #f9f9f9);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}

.slide-enter-from, .slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.slide-enter-to, .slide-leave-from {
  opacity: 1;
  max-height: 300px;
}

.advanced-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.adv-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--px-text-secondary, #555);
  min-width: 64px;
  flex-shrink: 0;
}

.adv-options {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.adv-btn {
  padding: 3px 12px;
  border: 1px solid var(--px-border, #e0e0e0);
  border-radius: 4px;
  background: #fff;
  font-size: 12px;
  color: var(--px-text-secondary, #555);
  cursor: pointer;
  transition: all 0.15s;
}

.adv-btn:hover {
  border-color: var(--px-blue, #0096FA);
  color: var(--px-blue, #0096FA);
}

.adv-btn.active {
  background: var(--px-blue, #0096FA);
  border-color: var(--px-blue, #0096FA);
  color: #fff;
}

.date-input {
  padding: 3px 8px;
  border: 1px solid var(--px-border, #e0e0e0);
  border-radius: 4px;
  font-size: 12px;
  color: var(--px-text-primary, #1f1f1f);
  background: #fff;
  outline: none;
}

.date-input:focus {
  border-color: var(--px-blue, #0096FA);
}

.date-sep {
  color: var(--px-text-tertiary, #999);
  font-size: 13px;
}

.actions-row {
  justify-content: flex-end;
}

.reset-btn {
  padding: 4px 16px;
  border: 1px solid var(--px-border, #e0e0e0);
  border-radius: 4px;
  background: #fff;
  font-size: 12px;
  color: var(--px-text-tertiary, #999);
  cursor: pointer;
  transition: all 0.15s;
}

.reset-btn:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

/* ===== 作品主体 ===== */
.artworks-body {
  max-width: var(--px-max-width, 1245px);
  margin: 0 auto;
  padding: 24px 20px 48px;
}

.artwork-grid {
  column-count: 5;
  column-gap: 16px;
}

/* ===== 骨架屏 ===== */
.skeleton-card {
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  break-inside: avoid;
  margin-bottom: 16px;
}

.skeleton-thumb {
  width: 100%;
  padding-bottom: 120%;
  position: relative;
  background: #eee;
  overflow: hidden;
}

.skeleton-info {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skeleton-line {
  height: 12px;
  border-radius: 4px;
  background: #eee;
  overflow: hidden;
}

.skeleton-line.w80 { width: 80%; }
.skeleton-line.w50 { width: 50%; }

.shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.5), transparent);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
  gap: 12px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--px-text-secondary, #555);
  margin: 0;
}

.empty-hint {
  font-size: 13px;
  color: var(--px-text-tertiary, #999);
  margin: 0;
}

/* ===== 滚动加载 ===== */
.scroll-sentinel {
  min-height: 1px;
  padding: 24px 0 16px;
}

.load-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--px-text-tertiary, #999);
  font-size: 14px;
}

.load-status.no-more {
  opacity: 0.5;
}

.spin-icon {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 响应式 ===== */
@media (max-width: 1400px) {
  .artwork-grid {
    column-count: 4;
  }
}

@media (max-width: 1100px) {
  .artwork-grid {
    column-count: 3;
  }
}

@media (max-width: 860px) {
  .artwork-grid {
    column-count: 3;
    column-gap: 12px;
  }
}

@media (max-width: 600px) {
  .artwork-grid {
    column-count: 2;
    column-gap: 8px;
  }

  .filter-chips {
    overflow-x: auto;
    flex-wrap: nowrap;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .filter-chips::-webkit-scrollbar {
    display: none;
  }

  .chip {
    flex-shrink: 0;
  }
}
</style>
