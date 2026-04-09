<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="item in statCards" :key="item.key">
        <div class="stat-icon" :style="{ background: item.bg }">
          <el-icon :size="22" :color="item.color"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ stats[item.key] ?? '—' }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="chart-panel">
        <h4 class="panel-title">作品状态分布</h4>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
      <div class="chart-panel">
        <h4 class="panel-title">平台数据概览</h4>
        <div ref="barChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 详细统计 -->
    <div class="detail-grid">
      <div class="detail-card">
        <h4 class="panel-title">用户统计</h4>
        <div class="detail-items">
          <div class="detail-row">
            <span class="detail-label">总用户数</span>
            <span class="detail-val">{{ stats.totalUsers ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">认证画师</span>
            <span class="detail-val">{{ stats.totalArtists ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">待审核申请</span>
            <span class="detail-val accent">{{ stats.pendingApplications ?? 0 }}</span>
          </div>
        </div>
      </div>
      <div class="detail-card">
        <h4 class="panel-title">作品统计</h4>
        <div class="detail-items">
          <div class="detail-row">
            <span class="detail-label">已发布</span>
            <span class="detail-val">{{ artworkStats.publishedCount ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">草稿中</span>
            <span class="detail-val">{{ artworkStats.draftCount ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">已删除</span>
            <span class="detail-val danger">{{ artworkStats.deletedCount ?? 0 }}</span>
          </div>
        </div>
      </div>
      <div class="detail-card">
        <h4 class="panel-title">互动统计</h4>
        <div class="detail-items">
          <div class="detail-row">
            <span class="detail-label">总浏览量</span>
            <span class="detail-val">{{ artworkStats.totalViews ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">总点赞数</span>
            <span class="detail-val">{{ artworkStats.totalLikes ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">总收藏数</span>
            <span class="detail-val">{{ artworkStats.totalFavorites ?? 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Kibana 数据看板 -->
    <div class="kibana-section">
      <h4 class="panel-title">
        Kibana 运营看板
        <a class="kibana-link" :href="kibanaUrl" target="_blank" title="在新窗口打开 Kibana">
          <el-icon><Link /></el-icon> 打开 Kibana
        </a>
      </h4>
      <div class="kibana-embed-wrap">
        <iframe
          :src="kibanaUrl + '/app/dashboards'"
          class="kibana-iframe"
          frameborder="0"
          allow="fullscreen"
        ></iframe>
        <div class="kibana-fallback">
          <p>如 Kibana 未加载，请确认容器已启动并访问 <a :href="kibanaUrl" target="_blank">{{ kibanaUrl }}</a></p>
        </div>
      </div>
    </div>

    <!-- 最近审计日志 -->
    <div class="log-section">
      <h4 class="panel-title">最近操作日志</h4>
      <div class="table-wrapper">
        <el-table :data="recentLogs" size="small">
          <el-table-column prop="adminId" label="管理员" width="90" />
          <el-table-column prop="actionType" label="操作" width="180">
            <template #default="{ row }">
              <el-tag :type="getActionTagType(row.actionType)" size="small">
                {{ formatAction(row.actionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="时间" width="170">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, markRaw } from 'vue'
import * as echarts from 'echarts'
import { User, Picture, Stamp, Suitcase, Link } from '@element-plus/icons-vue'
import { getPlatformStats, getArtworkStats, getAuditLogs } from '@/api/stats'

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const stats = reactive({
  totalUsers: 0,
  totalArtists: 0,
  publishedCount: 0,
  pendingApplications: 0
})

const artworkStats = reactive({
  publishedCount: 0,
  draftCount: 0,
  deletedCount: 0,
  totalViews: 0,
  totalLikes: 0,
  totalFavorites: 0
})

const recentLogs = ref([])
const kibanaUrl = ref(import.meta.env.VITE_KIBANA_URL || 'http://47.120.43.236:5601')

const statCards = [
  { key: 'totalUsers', label: '总用户', icon: markRaw(User), color: '#6366f1', bg: 'rgba(99,102,241,0.1)' },
  { key: 'totalArtists', label: '认证画师', icon: markRaw(Stamp), color: '#10b981', bg: 'rgba(16,185,129,0.1)' },
  { key: 'publishedCount', label: '发布作品', icon: markRaw(Picture), color: '#f59e0b', bg: 'rgba(245,158,11,0.1)' },
  { key: 'pendingApplications', label: '待审核', icon: markRaw(Suitcase), color: '#ef4444', bg: 'rgba(239,68,68,0.1)' }
]

const actionMap = {
  APPROVE_ARTIST_APPLICATION: '通过画师申请',
  REJECT_ARTIST_APPLICATION: '拒绝画师申请',
  DELETE_ARTWORK: '删除作品',
  DELETE_COMMENT: '删除评论',
  BAN_USER: '封禁用户',
  UNBAN_USER: '解封用户'
}

function formatAction(type) { return actionMap[type] || type }
function formatDate(str) { return str ? new Date(str).toLocaleString('zh-CN') : '' }

function getActionTagType(type) {
  if (type?.includes('APPROVE')) return 'success'
  if (type?.includes('REJECT') || type?.includes('DELETE') || type?.includes('BAN')) return 'danger'
  if (type?.includes('UNBAN')) return 'warning'
  return 'info'
}

function initCharts() {
  // 饼图
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#e2e8f0', borderWidth: 1, textStyle: { color: '#334155', fontSize: 13 } },
      legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
      color: ['#6366f1', '#94a3b8', '#ef4444'],
      series: [{
        type: 'pie',
        radius: ['42%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: '600' } },
        data: [
          { value: artworkStats.publishedCount, name: '已发布' },
          { value: artworkStats.draftCount, name: '草稿' },
          { value: artworkStats.deletedCount, name: '已删除' }
        ]
      }]
    })
  }

  // 柱状图
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e2e8f0', borderWidth: 1, textStyle: { color: '#334155', fontSize: 13 } },
      grid: { left: 50, right: 20, top: 20, bottom: 36 },
      xAxis: {
        type: 'category',
        data: ['用户', '画师', '作品', '浏览', '点赞', '收藏'],
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisTick: { show: false },
        axisLabel: { color: '#64748b', fontSize: 12 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#94a3b8', fontSize: 11 }
      },
      series: [{
        type: 'bar',
        barWidth: 28,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: (params) => {
            const colors = ['#6366f1', '#818cf8', '#a5b4fc', '#10b981', '#f59e0b', '#f97316']
            return colors[params.dataIndex] || '#6366f1'
          }
        },
        data: [
          stats.totalUsers,
          stats.totalArtists,
          artworkStats.publishedCount,
          artworkStats.totalViews,
          artworkStats.totalLikes,
          artworkStats.totalFavorites
        ]
      }]
    })
  }
}

function handleResize() {
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  try {
    const [platformRes, artworkRes, logRes] = await Promise.all([
      getPlatformStats(),
      getArtworkStats(),
      getAuditLogs({ page: 1, size: 8 })
    ])

    if (platformRes.code === 200 && platformRes.data) {
      Object.assign(stats, platformRes.data)
    }
    if (artworkRes.code === 200 && artworkRes.data) {
      Object.assign(artworkStats, artworkRes.data)
      stats.publishedCount = artworkRes.data.publishedCount || 0
    }
    if (logRes.data) {
      const d = logRes.data
      recentLogs.value = d.records || d.items || d.content || []
    }
  } catch (e) {
    console.error('加载仪表盘数据失败:', e)
  }

  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 20px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-text);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 13px;
  color: var(--c-text-muted);
  margin-top: 2px;
}

/* 图表 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-bottom: 24px;
}

.chart-panel {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 16px;
}

.chart-box {
  height: 280px;
}

/* 详细统计 */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.detail-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px;
}

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: 13px;
  color: var(--c-text-secondary);
}

.detail-val {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
  font-variant-numeric: tabular-nums;
}

.detail-val.accent {
  color: var(--c-primary);
}

.detail-val.danger {
  color: var(--c-danger);
}

/* 日志 */
.log-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px;
}

.log-section .table-wrapper {
  box-shadow: none;
}

/* Kibana 看板 */
.kibana-section {
  grid-column: 1 / -1;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px;
}
.kibana-section .panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.kibana-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 400;
  color: var(--c-primary, #6366f1);
  text-decoration: none;
}
.kibana-link:hover { text-decoration: underline; }
.kibana-embed-wrap { position: relative; }
.kibana-iframe {
  width: 100%;
  height: 520px;
  border-radius: 8px;
  border: 1px solid var(--c-border, #e2e8f0);
}
.kibana-fallback {
  position: absolute;
  bottom: 8px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: var(--c-text-muted, #94a3b8);
}
.kibana-fallback a { color: var(--c-primary, #6366f1); }
</style>
