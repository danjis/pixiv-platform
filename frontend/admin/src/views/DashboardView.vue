<template>
  <div class="dashboard">
    <!-- Stat cards -->
    <section class="stat-grid">
      <div v-for="card in statCards" :key="card.key" class="stat-card">
        <div class="stat-card-top">
          <div class="stat-icon-wrap" :style="{ background: card.bg }">
            <el-icon :size="20" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
          <span class="stat-trend" :class="card.trendClass">{{ card.trend }}</span>
        </div>
        <div class="stat-value">{{ formatNum(stats[card.key]) }}</div>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-bar">
          <div class="stat-bar-fill" :style="{ width: card.pct + '%', background: card.color }"></div>
        </div>
      </div>
    </section>

    <!-- Charts -->
    <section class="charts-row">
      <div class="chart-card chart-card--wide">
        <div class="card-header">
          <span class="card-title">平台数据概览</span>
          <span class="card-subtitle">各维度实时数据</span>
        </div>
        <div ref="barChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <span class="card-title">作品状态分布</span>
          <span class="card-subtitle">按状态占比</span>
        </div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
    </section>

    <!-- Bottom row -->
    <section class="bottom-row">
      <div class="detail-card">
        <div class="card-header"><span class="card-title">详细统计</span></div>
        <div class="detail-groups">
          <div class="detail-group">
            <p class="detail-group-title">用户</p>
            <div class="detail-rows">
              <div class="detail-row"><span class="dr-label">总用户数</span><span class="dr-val">{{ formatNum(stats.totalUsers) }}</span></div>
              <div class="detail-row"><span class="dr-label">认证画师</span><span class="dr-val">{{ formatNum(stats.totalArtists) }}</span></div>
              <div class="detail-row"><span class="dr-label">待审核申请</span><span class="dr-val accent">{{ stats.pendingApplications ?? 0 }}</span></div>
            </div>
          </div>
          <div class="detail-divider"></div>
          <div class="detail-group">
            <p class="detail-group-title">作品</p>
            <div class="detail-rows">
              <div class="detail-row"><span class="dr-label">已发布</span><span class="dr-val success">{{ formatNum(artworkStats.publishedCount) }}</span></div>
              <div class="detail-row"><span class="dr-label">草稿中</span><span class="dr-val">{{ artworkStats.draftCount ?? 0 }}</span></div>
              <div class="detail-row"><span class="dr-label">已删除</span><span class="dr-val danger">{{ artworkStats.deletedCount ?? 0 }}</span></div>
            </div>
          </div>
          <div class="detail-divider"></div>
          <div class="detail-group">
            <p class="detail-group-title">互动</p>
            <div class="detail-rows">
              <div class="detail-row"><span class="dr-label">总浏览量</span><span class="dr-val">{{ formatNum(artworkStats.totalViews) }}</span></div>
              <div class="detail-row"><span class="dr-label">总点赞数</span><span class="dr-val">{{ formatNum(artworkStats.totalLikes) }}</span></div>
              <div class="detail-row"><span class="dr-label">总收藏数</span><span class="dr-val">{{ formatNum(artworkStats.totalFavorites) }}</span></div>
            </div>
          </div>
        </div>
      </div>

      <div class="log-card">
        <div class="card-header">
          <span class="card-title">最近操作</span>
          <span class="card-subtitle">最新 8 条审计日志</span>
        </div>
        <div class="log-list">
          <div v-if="recentLogs.length === 0" class="log-empty">暂无日志</div>
          <div v-for="log in recentLogs" :key="log.id" class="log-item">
            <div class="log-dot" :class="getLogDotClass(log.actionType)"></div>
            <div class="log-body">
              <span class="log-action">{{ formatAction(log.actionType) }}</span>
              <span class="log-desc">{{ log.description || '—' }}</span>
            </div>
            <span class="log-time">{{ formatRelTime(log.createdAt) }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, markRaw } from 'vue'
import * as echarts from 'echarts'
import { User, Picture, Stamp, Suitcase } from '@element-plus/icons-vue'
import { getPlatformStats, getArtworkStats, getAuditLogs } from '@/api/stats'

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const stats = reactive({ totalUsers: 0, totalArtists: 0, publishedCount: 0, pendingApplications: 0 })
const artworkStats = reactive({ publishedCount: 0, draftCount: 0, deletedCount: 0, totalViews: 0, totalLikes: 0, totalFavorites: 0 })
const recentLogs = ref([])

const statCards = [
  { key: 'totalUsers',          label: '注册用户',   icon: markRaw(User),     color: '#5b5bd6', bg: 'rgba(91,91,214,0.10)',  trend: '+2.4%',  trendClass: 'up',   pct: 72 },
  { key: 'totalArtists',        label: '认证画师',   icon: markRaw(Stamp),    color: '#16a34a', bg: 'rgba(22,163,74,0.10)',  trend: '+5.1%',  trendClass: 'up',   pct: 38 },
  { key: 'publishedCount',      label: '发布作品',   icon: markRaw(Picture),  color: '#d97706', bg: 'rgba(217,119,6,0.10)', trend: '+8.3%',  trendClass: 'up',   pct: 61 },
  { key: 'pendingApplications', label: '待审核申请', icon: markRaw(Suitcase), color: '#dc2626', bg: 'rgba(220,38,38,0.10)', trend: '需处理', trendClass: 'warn', pct: 20 }
]

const actionMap = {
  APPROVE_ARTIST_APPLICATION: '通过画师申请',
  REJECT_ARTIST_APPLICATION: '拒绝画师申请',
  DELETE_ARTWORK: '删除作品',
  DELETE_COMMENT: '删除评论',
  BAN_USER: '封禁用户',
  UNBAN_USER: '解封用户'
}

function formatNum(n) {
  if (n == null) return '\u2014'
  return Number(n).toLocaleString('zh-CN')
}
function formatAction(type) { return actionMap[type] || type }
function formatRelTime(str) {
  if (!str) return ''
  const m = Math.floor((Date.now() - new Date(str).getTime()) / 60000)
  if (m < 1) return '\u521a\u521a'
  if (m < 60) return m + ' \u5206\u949f\u524d'
  const h = Math.floor(m / 60)
  if (h < 24) return h + ' \u5c0f\u65f6\u524d'
  return Math.floor(h / 24) + ' \u5929\u524d'
}
function getLogDotClass(type) {
  if (type && (type.includes('APPROVE') || type.includes('UNBAN'))) return 'dot-success'
  if (type && (type.includes('REJECT') || type.includes('DELETE') || type.includes('BAN'))) return 'dot-danger'
  return 'dot-info'
}

const COLORS = ['#5b5bd6', '#7c7ce8', '#a09cf7', '#16a34a', '#d97706', '#dc2626']

function initCharts() {
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e4e4e7', borderWidth: 1, textStyle: { color: '#111118', fontSize: 12 }, axisPointer: { type: 'shadow' } },
      grid: { left: 52, right: 16, top: 16, bottom: 40 },
      xAxis: { type: 'category', data: ['\u7528\u6237','\u753b\u5e08','\u4f5c\u54c1','\u6d4f\u89c8','\u70b9\u8d5e','\u6536\u85cf'], axisLine: { lineStyle: { color: '#e4e4e7' } }, axisTick: { show: false }, axisLabel: { color: '#a1a1aa', fontSize: 12 } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f4f5f7', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#a1a1aa', fontSize: 11 } },
      series: [{ type: 'bar', barWidth: 30,
        itemStyle: { borderRadius: [6,6,0,0], color: function(p){ return COLORS[p.dataIndex] || '#5b5bd6' } },
        data: [stats.totalUsers, stats.totalArtists, artworkStats.publishedCount, artworkStats.totalViews, artworkStats.totalLikes, artworkStats.totalFavorites]
      }]
    })
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#e4e4e7', borderWidth: 1, textStyle: { color: '#111118', fontSize: 12 } },
      legend: { bottom: 0, textStyle: { color: '#52525b', fontSize: 12 } },
      color: ['#5b5bd6', '#a1a1aa', '#dc2626'],
      series: [{ type: 'pie', radius: ['44%','70%'], center: ['50%','44%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
        label: { show: false }, emphasis: { label: { show: true, fontSize: 14, fontWeight: '600' } },
        data: [
          { value: artworkStats.publishedCount, name: '\u5df2\u53d1\u5e03' },
          { value: artworkStats.draftCount,     name: '\u8349\u7a3f' },
          { value: artworkStats.deletedCount,   name: '\u5df2\u5220\u9664' }
        ]
      }]
    })
  }
}

function handleResize() {
  pieChart && pieChart.resize()
  barChart && barChart.resize()
}

onMounted(async () => {
  try {
    const [pRes, aRes, lRes] = await Promise.all([
      getPlatformStats(), getArtworkStats(), getAuditLogs({ page: 1, size: 8 })
    ])
    if (pRes.code === 200 && pRes.data) Object.assign(stats, pRes.data)
    if (aRes.code === 200 && aRes.data) {
      Object.assign(artworkStats, aRes.data)
      stats.publishedCount = aRes.data.publishedCount || 0
    }
    if (lRes.data) {
      const d = lRes.data
      recentLogs.value = d.records || d.items || d.content || []
    }
  } catch (e) { console.error('Dashboard load error:', e) }
  initCharts()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart && pieChart.dispose()
  barChart && barChart.dispose()
})
</script>

<style scoped>
.dashboard { width: 100%; display: flex; flex-direction: column; gap: 20px; }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: var(--c-surface); border: 1px solid var(--c-border);
  border-radius: var(--radius-md); padding: 20px;
  display: flex; flex-direction: column; gap: 10px;
  transition: box-shadow var(--t-fast), transform var(--t-fast);
}
.stat-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.stat-card-top { display: flex; align-items: center; justify-content: space-between; }
.stat-icon-wrap { width: 40px; height: 40px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; }
.stat-trend { font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 100px; }
.stat-trend.up   { background: rgba(22,163,74,0.1); color: #16a34a; }
.stat-trend.warn { background: rgba(220,38,38,0.1); color: #dc2626; }
.stat-value { font-size: 30px; font-weight: 700; color: var(--c-text); letter-spacing: -0.8px; font-variant-numeric: tabular-nums; line-height: 1; }
.stat-label { font-size: 12px; color: var(--c-text-muted); font-weight: 500; }
.stat-bar { height: 3px; background: var(--c-border); border-radius: 2px; overflow: hidden; }
.stat-bar-fill { height: 100%; border-radius: 2px; opacity: 0.7; }
.charts-row { display: grid; grid-template-columns: 1fr 340px; gap: 16px; }
.chart-card { background: var(--c-surface); border: 1px solid var(--c-border); border-radius: var(--radius-md); padding: 20px; }
.card-header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px; }
.card-title { font-size: 14px; font-weight: 700; color: var(--c-text); letter-spacing: -0.2px; }
.card-subtitle { font-size: 11px; color: var(--c-text-muted); }
.chart-box { height: 240px; }
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-card, .log-card { background: var(--c-surface); border: 1px solid var(--c-border); border-radius: var(--radius-md); padding: 20px; }
.detail-groups { display: flex; flex-direction: column; }
.detail-group { padding: 12px 0; }
.detail-group:first-child { padding-top: 0; }
.detail-group-title { font-size: 10px; font-weight: 700; color: var(--c-text-muted); text-transform: uppercase; letter-spacing: 0.8px; margin-bottom: 10px; }
.detail-rows { display: flex; flex-direction: column; gap: 9px; }
.detail-row { display: flex; justify-content: space-between; align-items: center; }
.dr-label { font-size: 13px; color: var(--c-text-secondary); }
.dr-val { font-size: 16px; font-weight: 700; color: var(--c-text); font-variant-numeric: tabular-nums; }
.dr-val.accent  { color: var(--c-primary); }
.dr-val.success { color: var(--c-success); }
.dr-val.danger  { color: var(--c-danger); }
.detail-divider { height: 1px; background: var(--c-border-light); }
.log-list { display: flex; flex-direction: column; }
.log-empty { font-size: 13px; color: var(--c-text-muted); padding: 20px 0; text-align: center; }
.log-item { display: flex; align-items: flex-start; gap: 12px; padding: 11px 0; border-bottom: 1px solid var(--c-border-light); }
.log-item:last-child { border-bottom: none; }
.log-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; margin-top: 4px; }
.dot-success { background: var(--c-success); }
.dot-danger  { background: var(--c-danger); }
.dot-info    { background: var(--c-primary); }
.log-body { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.log-action { font-size: 13px; font-weight: 600; color: var(--c-text); }
.log-desc { font-size: 12px; color: var(--c-text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.log-time { font-size: 11px; color: var(--c-text-muted); flex-shrink: 0; font-family: 'DM Mono', monospace; white-space: nowrap; }
</style>
