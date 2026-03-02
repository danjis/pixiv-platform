<template>
  <div class="earnings-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">收入管理</h1>
        <p class="page-desc">查看收入统计与约稿收款明细</p>
      </div>
    </div>

    <!-- 收入概览 -->
    <div class="overview-cards">
      <div class="overview-card total">
        <div class="ov-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
        </div>
        <div class="ov-info">
          <span class="ov-amount">¥{{ totalEarnings.toFixed(2) }}</span>
          <span class="ov-label">累计收入</span>
        </div>
      </div>
      <div class="overview-card month">
        <div class="ov-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z"/></svg>
        </div>
        <div class="ov-info">
          <span class="ov-amount">¥{{ monthEarnings.toFixed(2) }}</span>
          <span class="ov-label">本月收入</span>
        </div>
      </div>
      <div class="overview-card pending">
        <div class="ov-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
        </div>
        <div class="ov-info">
          <span class="ov-amount">¥{{ pendingAmount.toFixed(2) }}</span>
          <span class="ov-label">待结算</span>
        </div>
      </div>
      <div class="overview-card count">
        <div class="ov-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
        </div>
        <div class="ov-info">
          <span class="ov-amount">{{ completedCount }}</span>
          <span class="ov-label">已完成约稿</span>
        </div>
      </div>
    </div>

    <!-- 收入明细 -->
    <div class="detail-section">
      <div class="section-header">
        <h3 class="section-title">收入明细</h3>
        <div class="period-filter">
          <button
            v-for="p in periods"
            :key="p.value"
            class="period-btn"
            :class="{ active: activePeriod === p.value }"
            @click="activePeriod = p.value"
          >
            {{ p.label }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <div v-else-if="filteredRecords.length > 0" class="records-list">
        <div class="records-header">
          <span class="col-title">约稿</span>
          <span class="col-client">委托方</span>
          <span class="col-amount">金额</span>
          <span class="col-status">状态</span>
          <span class="col-date">日期</span>
        </div>
        <div v-for="r in filteredRecords" :key="r.id" class="record-row">
          <span class="col-title text-ellipsis">{{ r.title }}</span>
          <span class="col-client">{{ r.clientName || '用户' }}</span>
          <span class="col-amount price">¥{{ r.totalAmount.toFixed(2) }}</span>
          <span class="col-status">
            <span class="status-dot" :class="'dot-' + r.status.toLowerCase()"></span>
            {{ getStatusText(r.status) }}
          </span>
          <span class="col-date">{{ formatDate(r.updatedAt || r.createdAt) }}</span>
        </div>
      </div>

      <div v-else class="empty-records">
        <p>{{ activePeriod === 'all' ? '暂无收入记录' : '该时间段暂无记录' }}</p>
      </div>
    </div>

    <!-- 提现提示 -->
    <div class="withdraw-hint">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="#FF9800"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
      <span>提现功能暂未开放，收入将自动结算到你的账户</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyCommissions } from '@/api/commission'

const loading = ref(true)
const allCommissions = ref([])
const activePeriod = ref('all')

const periods = [
  { label: '全部', value: 'all' },
  { label: '本月', value: 'month' },
  { label: '近三月', value: '3months' },
  { label: '今年', value: 'year' }
]

// 已完成的约稿 = 收入记录
const earningsRecords = computed(() =>
  allCommissions.value.filter(c => c.status === 'COMPLETED')
)

// 待结算：已交付但未完成
const pendingRecords = computed(() =>
  allCommissions.value.filter(c => ['IN_PROGRESS', 'DELIVERED', 'DEPOSIT_PAID'].includes(c.status))
)

const totalEarnings = computed(() =>
  earningsRecords.value.reduce((s, c) => s + (c.totalAmount || 0), 0)
)

const monthEarnings = computed(() => {
  const now = new Date()
  return earningsRecords.value
    .filter(c => {
      const d = new Date(c.updatedAt || c.createdAt)
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
    })
    .reduce((s, c) => s + (c.totalAmount || 0), 0)
})

const pendingAmount = computed(() =>
  pendingRecords.value.reduce((s, c) => s + (c.totalAmount || 0), 0)
)

const completedCount = computed(() => earningsRecords.value.length)

const filteredRecords = computed(() => {
  // 显示所有约稿（有金额的），按时间筛选
  let records = allCommissions.value.filter(c =>
    ['COMPLETED', 'IN_PROGRESS', 'DELIVERED', 'DEPOSIT_PAID'].includes(c.status)
  )

  const now = new Date()
  if (activePeriod.value === 'month') {
    records = records.filter(c => {
      const d = new Date(c.updatedAt || c.createdAt)
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
    })
  } else if (activePeriod.value === '3months') {
    const three = new Date()
    three.setMonth(three.getMonth() - 3)
    records = records.filter(c => new Date(c.updatedAt || c.createdAt) >= three)
  } else if (activePeriod.value === 'year') {
    records = records.filter(c => new Date(c.updatedAt || c.createdAt).getFullYear() === now.getFullYear())
  }

  return records.sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
})

function getStatusText(status) {
  return { COMPLETED: '已结算', IN_PROGRESS: '创作中', DELIVERED: '待确认', DEPOSIT_PAID: '已付定金' }[status] || status
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMyCommissions({ role: 'artist', page: 0, size: 200 })
    if (res.code === 200) {
      // 修复：使用 content 或 records 字段获取数组数据
      allCommissions.value = res.data?.content || res.data?.records || []
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.earnings-page { max-width: 900px; }

.page-header { margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px 0; }
.page-desc { font-size: 14px; color: #999; margin: 0; }

/* 概览卡片 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.overview-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.ov-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.overview-card.total .ov-icon { background: #E6F7F0; color: #00C48C; }
.overview-card.month .ov-icon { background: #EBF5FF; color: #0096FA; }
.overview-card.pending .ov-icon { background: #FFF3E0; color: #FF9800; }
.overview-card.count .ov-icon { background: #F3E8FF; color: #9B59B6; }
.ov-info { display: flex; flex-direction: column; }
.ov-amount { font-size: 22px; font-weight: 700; color: #1a1a1a; line-height: 1.2; }
.ov-label { font-size: 12px; color: #999; margin-top: 2px; }

/* 明细区 */
.detail-section {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}
.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;
}
.section-title { font-size: 16px; font-weight: 600; color: #1a1a1a; margin: 0; }
.period-filter { display: flex; gap: 4px; }
.period-btn {
  padding: 5px 12px; border: none; background: #f5f5f5;
  border-radius: 6px; font-size: 12px; color: #666;
  cursor: pointer; transition: all 0.15s;
}
.period-btn:hover { color: #333; }
.period-btn.active { background: #0096FA; color: #fff; }

.loading-state { display: flex; justify-content: center; padding: 40px 0; }
.spinner {
  width: 28px; height: 28px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 表格 */
.records-header, .record-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 10px 0;
  align-items: center;
  font-size: 13px;
}
.records-header {
  color: #bbb; font-weight: 600; font-size: 12px;
  border-bottom: 1px solid #f0f0f0;
  text-transform: uppercase;
}
.record-row {
  color: #555;
  border-bottom: 1px solid #f8f8f8;
  transition: background 0.15s;
}
.record-row:hover { background: #fafbfc; }
.record-row:last-child { border-bottom: none; }
.text-ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #00C48C; font-weight: 600; }

.col-status {
  display: flex; align-items: center; gap: 6px;
}
.status-dot {
  width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
}
.dot-completed { background: #00C48C; }
.dot-in_progress { background: #0096FA; }
.dot-delivered { background: #9B59B6; }
.dot-deposit_paid { background: #FF9800; }

.empty-records {
  display: flex; justify-content: center; padding: 40px 0;
  color: #bbb; font-size: 14px;
}

/* 提现提示 */
.withdraw-hint {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 18px;
  background: #FFFBF0;
  border: 1px solid #FFE7B3;
  border-radius: 10px;
  font-size: 13px;
  color: #996600;
}

@media (max-width: 768px) {
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
  .records-header, .record-row {
    grid-template-columns: 2fr 1fr 1fr;
  }
  .col-client, .col-date { display: none; }
}
</style>
