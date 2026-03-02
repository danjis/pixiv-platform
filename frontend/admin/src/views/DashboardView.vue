<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card users">
          <div class="stat-icon"><el-icon :size="40"><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ platformStats.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card artists">
          <div class="stat-icon"><el-icon :size="40"><Star /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ platformStats.totalArtists || 0 }}</div>
            <div class="stat-label">认证画师</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card artworks">
          <div class="stat-icon"><el-icon :size="40"><Picture /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ artworkStats.publishedCount || 0 }}</div>
            <div class="stat-label">已发布作品</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-icon"><el-icon :size="40"><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ platformStats.pendingApplications || 0 }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">作品状态分布</span>
          </template>
          <div ref="artworkPieRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">平台数据总览</span>
          </template>
          <div ref="platformBarRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">作品统计</span>
          </template>
          <div class="detail-stats">
            <div class="detail-item">
              <span class="detail-label">总作品数</span>
              <span class="detail-value">{{ artworkStats.totalArtworks || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">已发布</span>
              <el-tag type="success" size="small">{{ artworkStats.publishedCount || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">草稿</span>
              <el-tag type="info" size="small">{{ artworkStats.draftCount || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">已删除</span>
              <el-tag type="danger" size="small">{{ artworkStats.deletedCount || 0 }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">平台信息</span>
          </template>
          <div class="detail-stats">
            <div class="detail-item">
              <span class="detail-label">总用户数</span>
              <span class="detail-value">{{ platformStats.totalUsers || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">认证画师数</span>
              <span class="detail-value">{{ platformStats.totalArtists || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">待审核申请</span>
              <el-tag type="warning" size="small">{{ platformStats.pendingApplications || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">审计日志条数</span>
              <span class="detail-value">{{ platformStats.totalAuditLogs || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近审计日志 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header-row">
          <span class="card-title">最近操作日志</span>
          <el-button type="primary" text @click="$router.push('/audit-logs')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentLogs" stripe style="width: 100%" v-loading="loadingLogs">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="actionType" label="操作类型" width="200">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.actionType)" size="small">
              {{ formatActionType(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { User, Star, Picture, Bell } from '@element-plus/icons-vue'
import { getPlatformStats, getArtworkStats, getAuditLogs } from '../api/stats'
import * as echarts from 'echarts'

const platformStats = reactive({})
const artworkStats = reactive({})
const recentLogs = ref([])
const loadingLogs = ref(false)

// Chart refs
const artworkPieRef = ref(null)
const platformBarRef = ref(null)
let pieChart = null
let barChart = null

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}

const actionTypeMap = {
  'APPROVE_ARTIST_APPLICATION': '通过画师申请',
  'REJECT_ARTIST_APPLICATION': '拒绝画师申请',
  'DELETE_ARTWORK': '删除作品',
  'DELETE_COMMENT': '删除评论',
  'BAN_USER': '封禁用户',
  'UNBAN_USER': '解封用户',
  'OTHER': '其他操作'
}

const formatActionType = (type) => actionTypeMap[type] || type

const getActionTagType = (type) => {
  if (type?.includes('APPROVE')) return 'success'
  if (type?.includes('REJECT') || type?.includes('DELETE') || type?.includes('BAN')) return 'danger'
  if (type?.includes('UNBAN')) return 'warning'
  return 'info'
}

const loadData = async () => {
  try {
    const [pRes, aRes] = await Promise.all([
      getPlatformStats(),
      getArtworkStats()
    ])
    Object.assign(platformStats, pRes.data || {})
    Object.assign(artworkStats, aRes.data || {})

    // 渲染图表
    await nextTick()
    renderArtworkPie()
    renderPlatformBar()
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }

  loadingLogs.value = true
  try {
    const logRes = await getAuditLogs({ page: 1, size: 10 })
    recentLogs.value = logRes.data?.records || logRes.data?.items || logRes.data?.content || []
  } catch (e) {
    console.error('加载审计日志失败:', e)
  } finally {
    loadingLogs.value = false
  }
}

function renderArtworkPie() {
  if (!artworkPieRef.value) return
  pieChart = echarts.init(artworkPieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    color: ['#67c23a', '#909399', '#f56c6c'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: [
        { value: artworkStats.publishedCount || 0, name: '已发布' },
        { value: artworkStats.draftCount || 0, name: '草稿' },
        { value: artworkStats.deletedCount || 0, name: '已删除' }
      ]
    }]
  })
}

function renderPlatformBar() {
  if (!platformBarRef.value) return
  barChart = echarts.init(platformBarRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: ['总用户', '认证画师', '已发布作品', '待审核申请'],
      axisLabel: { fontSize: 12 }
    },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{
      type: 'bar',
      barWidth: 36,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params) => {
          const colors = [
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f093fb' }, { offset: 1, color: '#f5576c' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#fa709a' }, { offset: 1, color: '#fee140' }])
          ]
          return colors[params.dataIndex]
        }
      },
      data: [
        platformStats.totalUsers || 0,
        platformStats.totalArtists || 0,
        artworkStats.publishedCount || 0,
        platformStats.pendingApplications || 0
      ]
    }]
  })
}

// 窗口 resize 时自适应
function handleResize() {
  pieChart?.resize()
  barChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.page-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 22px;
}

.stat-cards .stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-card.users .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card.artists .stat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-card.artworks .stat-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card.pending .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #606266;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  font-weight: 600;
  font-size: 16px;
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
