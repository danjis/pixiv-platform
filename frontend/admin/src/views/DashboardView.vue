<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div
        class="stat-card"
        v-for="(item, idx) in statCards"
        :key="item.key"
        :style="{ '--delay': idx * 0.06 + 's' }"
      >
        <div class="stat-card-bg"></div>
        <div class="stat-icon" :style="{ '--icon-color': item.color, '--icon-bg': item.bg }">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ formatNum(stats[item.key] ?? 0) }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
        <div class="stat-trend" :style="{ color: item.color }">{{ item.unit }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="chart-panel">
        <div class="panel-header"><h4 class="panel-title">作品状态分布</h4></div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
      <div class="chart-panel">
        <div class="panel-header"><h4 class="panel-title">平台数据概览</h4></div>
        <div ref="barChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 详细统计 -->
    <div class="detail-grid">
      <div class="detail-card" v-for="card in detailCards" :key="card.title">
        <div class="detail-card-header">
          <span class="detail-card-dot" :style="{ background: card.dotColor }"></span>
          <h4 class="panel-title">{{ card.title }}</h4>
        </div>
        <div class="detail-items">
          <div class="detail-row" v-for="row in card.rows" :key="row.label">
            <span class="detail-label">{{ row.label }}</span>
            <span class="detail-val" :class="row.cls">{{ formatNum(row.value) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近审计日志 -->
    <div class="log-section">
      <div class="panel-header"><h4 class="panel-title">最近操作日志</h4></div>
      <div class="log-list">
        <div v-if="recentLogs.length === 0" class="empty-state">
          <span class="empty-icon">📋</span>
          <p>暂无操作日志</p>
        </div>
        <div v-for="log in recentLogs" :key="log.id" class="log-item">
          <div class="log-dot" :class="getLogDotClass(log.actionType)"></div>
          <div class="log-content">
            <el-tag :type="getLogTagType(log.actionType)" size="small" effect="plain">
              {{ formatActionType(log.actionType) }}
            </el-tag>
            <span class="log-desc">{{ cleanDesc(log.description) }}</span>
          </div>
          <div class="log-time">{{ formatDate(log.createdAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { User, Picture, Stamp, ChatDotRound, Money, Suitcase, Trophy, Medal } from '@element-plus/icons-vue'
import { getStats, getAuditLogs } from '@/api/stats'

const stats = ref({})
const recentLogs = ref([])
const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const statCards = [
  { key: 'totalUsers',       label: '注册用户', unit: '人', icon: markRaw(User),         color: '#6aabde', bg: 'rgba(106,171,222,0.12)' },
  { key: 'totalArtists',     label: '认证画师', unit: '人', icon: markRaw(Stamp),        color: '#c9a84c', bg: 'rgba(201,168,76,0.12)' },
  { key: 'totalArtworks',    label: '作品总数', unit: '幅', icon: markRaw(Picture),      color: '#2ec98a', bg: 'rgba(46,201,138,0.12)' },
  { key: 'totalComments',    label: '评论总数', unit: '条', icon: markRaw(ChatDotRound), color: '#788c5d', bg: 'rgba(120,140,93,0.12)' },
  { key: 'totalCommissions', label: '约稿订单', unit: '单', icon: markRaw(Suitcase),     color: '#f0a030', bg: 'rgba(240,160,48,0.12)' },
  { key: 'totalRevenue',     label: '平台收入', unit: '元', icon: markRaw(Money),        color: '#e8c96e', bg: 'rgba(232,201,110,0.12)' },
  { key: 'totalContests',    label: '比赛活动', unit: '场', icon: markRaw(Trophy),       color: '#d97757', bg: 'rgba(217,119,87,0.12)' },
  { key: 'totalMembers',     label: '付费会员', unit: '人', icon: markRaw(Medal),        color: '#a78bfa', bg: 'rgba(167,139,250,0.12)' },
]

const detailCards = computed(() => [
  {
    title: '用户概况', dotColor: '#6aabde',
    rows: [
      { label: '总注册用户', value: stats.value.totalUsers        ?? 0 },
      { label: '认证画师',   value: stats.value.totalArtists      ?? 0, cls: 'val-gold' },
      { label: '付费会员',   value: stats.value.totalMembers      ?? 0, cls: 'val-purple' },
      { label: '今日新增',   value: stats.value.todayNewUsers     ?? 0, cls: 'val-green' },
    ]
  },
  {
    title: '内容概况', dotColor: '#2ec98a',
    rows: [
      { label: '作品总数', value: stats.value.totalArtworks       ?? 0 },
      { label: '已发布',   value: stats.value.publishedArtworks   ?? 0, cls: 'val-green' },
      { label: '画师审核', value: stats.value.pendingApplications ?? 0, cls: 'val-warning' },
      { label: '评论总数', value: stats.value.totalComments       ?? 0 },
    ]
  },
  {
    title: '交易概况', dotColor: '#f0a030',
    rows: [
      { label: '约稿总数', value: stats.value.totalCommissions     ?? 0 },
      { label: '进行中',   value: stats.value.activeCommissions    ?? 0, cls: 'val-warning' },
      { label: '已完成',   value: stats.value.completedCommissions ?? 0, cls: 'val-green' },
      { label: '平台收入', value: stats.value.totalRevenue         ?? 0, cls: 'val-gold' },
    ]
  },
  {
    title: '活动概况', dotColor: '#d97757',
    rows: [
      { label: '比赛总数',   value: stats.value.totalContests    ?? 0 },
      { label: '进行中',     value: stats.value.activeContests   ?? 0, cls: 'val-green' },
      { label: '优惠券数',   value: stats.value.totalCoupons     ?? 0 },
      { label: '待处理反馈', value: stats.value.pendingFeedbacks ?? 0, cls: 'val-danger' },
    ]
  }
])

const formatNum = (n) => {
  const num = Number(n) || 0
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  return num.toLocaleString('zh-CN')
}

const formatDate = (str) => {
  if (!str) return ''
  const d = new Date(str)
  const diff = Date.now() - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const actionTypeMap = {
  APPROVE_ARTIST_APPLICATION: '通过申请', REJECT_ARTIST_APPLICATION: '拒绝申请',
  DELETE_ARTWORK: '删除作品', DELETE_COMMENT: '删除评论',
  BAN_USER: '封禁用户', UNBAN_USER: '解封用户', OTHER: '其他操作'
}
const formatActionType = (t) => actionTypeMap[t] || t
const cleanDesc = (desc) => desc ? desc.replace(/意见:\s*null/g, '意见: 无') : ''
const getLogTagType = (t) => {
  if (t?.includes('APPROVE') || t?.includes('UNBAN')) return 'success'
  if (t?.includes('REJECT') || t?.includes('DELETE') || t?.includes('BAN')) return 'danger'
  return 'info'
}
const getLogDotClass = (t) => {
  if (t?.includes('APPROVE') || t?.includes('UNBAN')) return 'dot-green'
  if (t?.includes('REJECT') || t?.includes('DELETE') || t?.includes('BAN')) return 'dot-red'
  return 'dot-blue'
}

const initCharts = async () => {
  if (typeof window === 'undefined') return
  try {
    const echarts = (await import('echarts')).default
    const s = stats.value
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
      pieChart.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', backgroundColor: '#162234', borderColor: 'rgba(201,168,76,0.25)', textStyle: { color: '#ddd8ce', fontSize: 12 }, formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 8, textStyle: { color: '#8a97b0', fontSize: 11 }, itemWidth: 10, itemHeight: 10 },
        series: [{ type: 'pie', radius: ['40%', '68%'], center: ['50%', '44%'],
          itemStyle: { borderRadius: 6, borderColor: '#0c1220', borderWidth: 2 },
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold', color: '#e8c96e' } },
          data: [
            { value: s.publishedArtworks ?? 0, name: '已发布', itemStyle: { color: '#2ec98a' } },
            { value: s.draftArtworks     ?? 0, name: '草稿',   itemStyle: { color: '#f0a030' } },
            { value: s.deletedArtworks   ?? 0, name: '已删除', itemStyle: { color: '#e05a7a' } },
          ]
        }]
      })
    }
    if (barChartRef.value) {
      barChart = echarts.init(barChartRef.value)
      const lg = (c1, c2) => new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:c1},{offset:1,color:c2}])
      barChart.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', backgroundColor: '#162234', borderColor: 'rgba(201,168,76,0.25)', textStyle: { color: '#ddd8ce', fontSize: 12 }, axisPointer: { type: 'shadow' } },
        grid: { top: 20, right: 12, bottom: 36, left: 48 },
        xAxis: { type: 'category', data: ['用户','画师','作品','评论','约稿','会员'], axisLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }, axisLabel: { color: '#52617a', fontSize: 11 }, axisTick: { show: false } },
        yAxis: { type: 'value', axisLabel: { color: '#52617a', fontSize: 10 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } } },
        series: [{ type: 'bar', barMaxWidth: 36, itemStyle: { borderRadius: [4,4,0,0] },
          data: [
            { value: s.totalUsers       ?? 0, itemStyle: { color: lg('#6aabde','rgba(106,171,222,0.3)') }},
            { value: s.totalArtists     ?? 0, itemStyle: { color: lg('#e8c96e','rgba(232,201,110,0.3)') }},
            { value: s.totalArtworks    ?? 0, itemStyle: { color: lg('#2ec98a','rgba(46,201,138,0.3)')  }},
            { value: s.totalComments    ?? 0, itemStyle: { color: lg('#788c5d','rgba(120,140,93,0.3)')  }},
            { value: s.totalCommissions ?? 0, itemStyle: { color: lg('#f0a030','rgba(240,160,48,0.3)')  }},
            { value: s.totalMembers     ?? 0, itemStyle: { color: lg('#a78bfa','rgba(167,139,250,0.3)') }},
          ]
        }]
      })
    }
  } catch (e) { console.warn('ECharts 不可用:', e) }
}

const resizeCharts = () => { pieChart?.resize(); barChart?.resize() }

const loadStats = async () => {
  try {
    const res = await getStats()
    if (res.code === 200 && res.data) {
      stats.value = res.data
      await nextTick()
      initCharts()
    }
  } catch (e) { console.warn('加载统计失败:', e) }
}

const loadLogs = async () => {
  try {
    const res = await getAuditLogs({ page: 1, size: 8 })
    const data = res.data
    recentLogs.value = data?.records || data?.items || data?.content || []
  } catch { /* ignore */ }
}

onMounted(() => { loadStats(); loadLogs(); window.addEventListener('resize', resizeCharts) })
onUnmounted(() => { window.removeEventListener('resize', resizeCharts); pieChart?.dispose(); barChart?.dispose() })
</script>

<style scoped>
.dashboard {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 统计卡片 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px 16px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  animation: fadeUp 0.4s ease both;
  animation-delay: var(--delay, 0s);
  transition: all var(--transition-fast);
}
.stat-card:hover {
  border-color: var(--c-border-gold);
  box-shadow: var(--shadow-sm), var(--shadow-gold);
  transform: translateY(-2px);
}
.stat-card-bg {
  position: absolute;
  top: -20px; right: -20px;
  width: 80px; height: 80px;
  border-radius: 50%;
  background: var(--icon-bg, rgba(201,168,76,0.08));
  pointer-events: none;
}
.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px; height: 46px;
  border-radius: var(--radius-sm);
  background: var(--icon-bg);
  color: var(--icon-color);
  flex-shrink: 0;
}
.stat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-text-inverse);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 12px;
  color: var(--c-text-muted);
  font-weight: 500;
}
.stat-trend {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

/* ===== 图表区 ===== */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1.6fr;
  gap: 14px;
}
.chart-panel {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
  padding: 18px 20px 14px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.panel-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  margin: 0;
}
.chart-box {
  height: 220px;
  width: 100%;
}

/* ===== 详细统计 ===== */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.detail-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
  padding: 16px 18px;
}
.detail-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.detail-card-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.detail-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-label {
  font-size: 12px;
  color: var(--c-text-muted);
}
.detail-val {
  font-size: 14px;
  font-weight: 700;
  color: var(--c-text);
  font-variant-numeric: tabular-nums;
}
.val-gold    { color: var(--c-primary-bright); }
.val-green   { color: var(--c-success); }
.val-warning { color: var(--c-warning); }
.val-danger  { color: var(--c-danger); }
.val-purple  { color: #a78bfa; }

/* ===== 日志区 ===== */
.log-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
  padding: 18px 20px;
}
.log-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 10px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}
.log-item:hover { background: var(--c-surface-2); }
.log-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-green { background: var(--c-success); box-shadow: 0 0 6px var(--c-success); }
.dot-red   { background: var(--c-danger);  box-shadow: 0 0 6px var(--c-danger); }
.dot-blue  { background: var(--c-info);    box-shadow: 0 0 6px var(--c-info); }

.log-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.log-desc {
  font-size: 13px;
  color: var(--c-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.log-time {
  font-size: 11px;
  color: var(--c-text-muted);
  flex-shrink: 0;
  white-space: nowrap;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  color: var(--c-text-muted);
  gap: 8px;
}
.empty-icon { font-size: 36px; opacity: .5; }
.empty-state p { font-size: 13px; }

/* ===== 动画 ===== */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1200px) {
  .stat-grid   { grid-template-columns: repeat(2, 1fr); }
  .detail-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-row   { grid-template-columns: 1fr; }
}
</style>
