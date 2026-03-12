<template>
  <div class="dashboard-page">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h1 class="welcome-title">{{ greeting }}，{{ userStore.user?.username || '画师' }}</h1>
        <p class="welcome-desc">这是你的创作者工作台，管理作品和约稿</p>
      </div>
      <router-link to="/publish" class="quick-publish-btn">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        投稿新作品
      </router-link>
    </div>

    <!-- 数据概览 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon artworks">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M22 16V4c0-1.1-.9-2-2-2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2zm-11-4l2.03 2.71L16 11l4 5H8l3-4zM2 6v14c0 1.1.9 2 2 2h14v-2H4V6H2z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.artworkCount }}</span>
          <span class="stat-label">作品总数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon commissions">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.activeCommissions }}</span>
          <span class="stat-label">进行中约稿</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon earnings">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">¥{{ stats.totalEarnings.toFixed(2) }}</span>
          <span class="stat-label">累计收入</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon followers">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
      </div>
    </div>

    <!-- 数据图表 -->
    <div class="charts-grid">
      <div class="panel">
        <div class="panel-header">
          <h3 class="panel-title">作品表现排行</h3>
          <router-link to="/studio/artworks" class="panel-link">查看全部</router-link>
        </div>
        <div v-if="loadingArtworks" class="panel-loading"><div class="spinner"></div></div>
        <div v-else-if="topArtworks.length === 0" class="panel-empty"><p>暂无作品数据</p></div>
        <div v-else ref="artworkBarRef" class="chart-container"></div>
      </div>
      <div class="panel">
        <div class="panel-header">
          <h3 class="panel-title">约稿状态分布</h3>
        </div>
        <div v-if="allCommissions.length === 0 && !loadingCommissions" class="panel-empty"><p>暂无约稿数据</p></div>
        <div v-else ref="commissionPieRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content-grid">
      <!-- 最近约稿 -->
      <div class="panel">
        <div class="panel-header">
          <h3 class="panel-title">最近约稿</h3>
          <router-link to="/studio/commissions" class="panel-link">查看全部</router-link>
        </div>
        <div v-if="loadingCommissions" class="panel-loading">
          <div class="spinner"></div>
        </div>
        <div v-else-if="recentCommissions.length === 0" class="panel-empty">
          <p>暂无约稿</p>
        </div>
        <div v-else class="commission-list">
          <div
            v-for="c in recentCommissions"
            :key="c.id"
            class="commission-item"
            @click="$router.push('/studio/commissions')"
          >
            <div class="commission-info">
              <span class="commission-title">{{ c.title }}</span>
              <span class="commission-client">来自 {{ c.clientName || '用户' }}</span>
            </div>
            <div class="commission-right">
              <span class="commission-amount">¥{{ c.totalAmount }}</span>
              <span class="commission-status" :class="'s-' + c.status.toLowerCase()">
                {{ getStatusText(c.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="panel">
        <div class="panel-header">
          <h3 class="panel-title">快捷操作</h3>
        </div>
        <div class="quick-actions">
          <router-link to="/publish" class="quick-action">
            <div class="qa-icon" style="background:#EBF5FF;color:#0096FA">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            </div>
            <span>投稿作品</span>
          </router-link>
          <router-link to="/chat" class="quick-action">
            <div class="qa-icon" style="background:#E6F7F0;color:#00C48C">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
            </div>
            <span>查看私信</span>
          </router-link>
          <router-link to="/studio/settings" class="quick-action">
            <div class="qa-icon" style="background:#FFF3E0;color:#FF9800">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
            </div>
            <span>画师设置</span>
          </router-link>
          <router-link to="/studio/earnings" class="quick-action">
            <div class="qa-icon" style="background:#F3E8FF;color:#9B59B6">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>
            </div>
            <span>收入报表</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyCommissions } from '@/api/commission'
import { getCurrentUser } from '@/api/user'
import { getMyArtworks } from '@/api/studio'
import * as echarts from 'echarts'

const userStore = useUserStore()

const stats = ref({
  artworkCount: 0,
  activeCommissions: 0,
  totalEarnings: 0,
  followerCount: 0
})

const recentCommissions = ref([])
const allCommissions = ref([])
const loadingCommissions = ref(true)
const topArtworks = ref([])
const loadingArtworks = ref(true)

// 图表
const artworkBarRef = ref(null)
const commissionPieRef = ref(null)
let barChart = null
let pieChart = null

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const statusMap = {
  PENDING: '待确认',
  DEPOSIT_PAID: '已付定金',
  IN_PROGRESS: '创作中',
  DELIVERED: '已交付',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REJECTED: '已拒绝'
}

function getStatusText(status) {
  return statusMap[status] || status
}

function renderArtworkBar() {
  if (!artworkBarRef.value || topArtworks.value.length === 0) return
  barChart = echarts.init(artworkBarRef.value)
  const items = topArtworks.value.slice(0, 8).reverse()
  const names = items.map(a => {
    const t = a.title || '无标题'
    return t.length > 8 ? t.slice(0, 8) + '…' : t
  })
  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const idx = params[0].dataIndex
        const art = items[idx]
        return `<b>${art.title || '无标题'}</b><br/>
          浏览 ${art.viewCount || 0}<br/>
          点赞 ${art.likeCount || 0}<br/>
          收藏 ${art.favoriteCount || 0}`
      }
    },
    legend: { data: ['浏览', '点赞', '收藏'], bottom: 0, textStyle: { fontSize: 11, color: '#999' } },
    grid: { left: 80, right: 16, top: 8, bottom: 36 },
    xAxis: { type: 'value', axisLabel: { fontSize: 11, color: '#bbb' }, splitLine: { lineStyle: { color: '#f5f5f5' } } },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 11, color: '#666' }, axisLine: { show: false }, axisTick: { show: false } },
    series: [
      { name: '浏览', type: 'bar', stack: 'total', data: items.map(a => a.viewCount || 0), itemStyle: { color: '#0096FA', borderRadius: [0, 0, 0, 0] }, barWidth: 14 },
      { name: '点赞', type: 'bar', stack: 'total', data: items.map(a => a.likeCount || 0), itemStyle: { color: '#FF6B81' }, barWidth: 14 },
      { name: '收藏', type: 'bar', stack: 'total', data: items.map(a => a.favoriteCount || 0), itemStyle: { color: '#FFC53D', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }
    ]
  })
}

function renderCommissionPie() {
  if (!commissionPieRef.value || allCommissions.value.length === 0) return
  pieChart = echarts.init(commissionPieRef.value)
  const statusCounts = {}
  allCommissions.value.forEach(c => {
    const label = statusMap[c.status] || c.status
    statusCounts[label] = (statusCounts[label] || 0) + 1
  })
  const data = Object.entries(statusCounts).map(([name, value]) => ({ name, value }))
  const colorMap = { '待确认': '#FF9800', '已付定金': '#0096FA', '创作中': '#409EFF', '已交付': '#9B59B6', '已完成': '#00C48C', '已取消': '#bbb', '已拒绝': '#FF4D4F' }
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 11, color: '#999' } },
    series: [{
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map(d => ({ ...d, itemStyle: { color: colorMap[d.name] || '#999' } }))
    }]
  })
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

async function loadDashboard() {
  // 加载用户信息获取粉丝数和作品数
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      stats.value.artworkCount = res.data.artworkCount || 0
      stats.value.followerCount = res.data.followerCount || 0
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }

  // 加载约稿列表（画师视角）
  loadingCommissions.value = true
  try {
    const res = await getMyCommissions({ role: 'artist', page: 0, size: 20 })
    if (res.code === 200) {
      const items = res.data?.content || res.data?.items || (Array.isArray(res.data) ? res.data : [])
      allCommissions.value = items
      recentCommissions.value = items.slice(0, 5)
      // 统计进行中的约稿
      stats.value.activeCommissions = items.filter(c =>
        ['PENDING', 'DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED'].includes(c.status)
      ).length
      // 统计收入（已完成的约稿总额）
      stats.value.totalEarnings = items
        .filter(c => c.status === 'COMPLETED')
        .reduce((sum, c) => sum + (c.totalAmount || 0), 0)
    }
  } catch (e) {
    console.error('加载约稿失败', e)
  } finally {
    loadingCommissions.value = false
  }

  // 加载作品数据用于图表
  loadingArtworks.value = true
  try {
    const res = await getMyArtworks({ page: 1, size: 20 })
    if (res.code === 200) {
      const items = res.data?.records || res.data?.items || res.data?.content || (Array.isArray(res.data) ? res.data : [])
      // 按浏览量 + 点赞 + 收藏综合排序
      topArtworks.value = [...items].sort((a, b) =>
        ((b.viewCount || 0) + (b.likeCount || 0) * 2 + (b.favoriteCount || 0) * 3) -
        ((a.viewCount || 0) + (a.likeCount || 0) * 2 + (a.favoriteCount || 0) * 3)
      )
    }
  } catch (e) {
    console.error('加载作品数据失败', e)
  } finally {
    loadingArtworks.value = false
  }

  // 渲染图表
  await nextTick()
  renderArtworkBar()
  renderCommissionPie()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  max-width: 100%;
}

/* 欢迎区 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}
.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}
.welcome-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.quick-publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #0096FA;
  color: #fff;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s;
  white-space: nowrap;
}
.quick-publish-btn:hover {
  background: #0080d5;
}

/* 数据卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon.artworks { background: #EBF5FF; color: #0096FA; }
.stat-icon.commissions { background: #FFF3E0; color: #FF9800; }
.stat-icon.earnings { background: #E6F7F0; color: #00C48C; }
.stat-icon.followers { background: #F3E8FF; color: #9B59B6; }

.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-number {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* 内容区 */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
}

/* 图表区 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-container {
  width: 100%;
  height: 300px;
}

.panel {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}
.panel-link {
  font-size: 13px;
  color: #0096FA;
  text-decoration: none;
  font-weight: 500;
}
.panel-link:hover {
  text-decoration: underline;
}
.panel-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #eee;
  border-top-color: #0096FA;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.panel-empty {
  display: flex;
  justify-content: center;
  padding: 40px 0;
  color: #bbb;
  font-size: 14px;
}

/* 约稿列表 */
.commission-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.commission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
}
.commission-item:hover {
  background: #f8f9fa;
}
.commission-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.commission-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.commission-client {
  font-size: 12px;
  color: #bbb;
}
.commission-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.commission-amount {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}
.commission-status {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
}
.s-pending { background: #FFF3E0; color: #FF9800; }
.s-deposit_paid { background: #E8F4FF; color: #0096FA; }
.s-in_progress { background: #EBF5FF; color: #0096FA; }
.s-delivered { background: #F3E8FF; color: #9B59B6; }
.s-completed { background: #E6F7F0; color: #00C48C; }
.s-cancelled { background: #f5f5f5; color: #999; }
.s-rejected { background: #FFF1F0; color: #FF4D4F; }

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  border-radius: 12px;
  text-decoration: none;
  color: #555;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s;
  border: 1px solid #f0f0f0;
}
.quick-action:hover {
  border-color: #e0e0e0;
  background: #fafafa;
  transform: translateY(-1px);
}
.qa-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
