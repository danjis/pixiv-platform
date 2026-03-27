<template>
  <div class="finance-page">
    <!-- 收入概览卡片 -->
    <div class="overview-section">
      <h2 class="section-title">收入概览</h2>
      <div class="stat-cards">
        <div class="stat-card platform">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.94s4.18 1.36 4.18 3.85c-.01 1.83-1.39 2.83-3.12 3.19z"/></svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">¥{{ formatMoney(overview.platformTotalIncome) }}</span>
            <span class="stat-label">平台总收入</span>
            <span class="stat-sub">会员 + 服务费 - 优惠券补贴</span>
          </div>
        </div>
        <div class="stat-card artist">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">¥{{ formatMoney(overview.artistTotalIncome) }}</span>
            <span class="stat-label">画师总收入</span>
            <span class="stat-sub">约稿收款</span>
          </div>
        </div>
        <div class="stat-card membership">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/></svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">¥{{ formatMoney(overview.membershipIncome) }}</span>
            <span class="stat-label">会员充值收入</span>
            <span class="stat-sub">{{ overview.membershipOrders || 0 }} 笔</span>
          </div>
        </div>
        <div class="stat-card fee">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M19 14V6c0-1.1-.9-2-2-2H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zm-2 0H3V6h14v8zm-7-7c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3zm13 0v11c0 1.1-.9 2-2 2H4v-2h17V7h2z"/></svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">¥{{ formatMoney(overview.platformFeeIncome) }}</span>
            <span class="stat-label">约稿服务费</span>
            <span class="stat-sub">VIP减免 ¥{{ formatMoney(overview.feeDiscountTotal) }} | 优惠券补贴 ¥{{ formatMoney(overview.couponSubsidy) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日数据 -->
    <div class="today-section">
      <h2 class="section-title">今日数据</h2>
      <div class="today-cards">
        <div class="today-card">
          <span class="today-value">¥{{ formatMoney(overview.todayIncome) }}</span>
          <span class="today-label">今日总交易额</span>
        </div>
        <div class="today-card">
          <span class="today-value">¥{{ formatMoney(overview.todayMembership) }}</span>
          <span class="today-label">今日会员收入</span>
        </div>
        <div class="today-card">
          <span class="today-value">¥{{ formatMoney(overview.todayPlatformFee) }}</span>
          <span class="today-label">今日服务费</span>
        </div>
        <div class="today-card">
          <span class="today-value">{{ overview.todayOrders || 0 }}</span>
          <span class="today-label">今日订单数</span>
        </div>
      </div>
    </div>

    <!-- 收入构成 -->
    <div class="composition-section">
      <div class="comp-row">
        <!-- 平台收入构成 -->
        <div class="comp-card">
          <h3 class="comp-title">平台收入构成</h3>
          <div class="comp-chart">
            <div class="donut-wrapper">
              <svg viewBox="0 0 120 120" class="donut-svg">
                <circle cx="60" cy="60" r="50" fill="none" stroke="#f0f0f0" stroke-width="12" />
                <circle cx="60" cy="60" r="50" fill="none"
                  :stroke="'#6366f1'" stroke-width="12"
                  :stroke-dasharray="membershipArc + ' ' + (314.16 - membershipArc)"
                  stroke-dashoffset="78.54"
                  stroke-linecap="round" />
                <circle cx="60" cy="60" r="50" fill="none"
                  :stroke="'#f59e0b'" stroke-width="12"
                  :stroke-dasharray="feeArc + ' ' + (314.16 - feeArc)"
                  :stroke-dashoffset="78.54 - membershipArc"
                  stroke-linecap="round" />
              </svg>
              <div class="donut-center">
                <span class="donut-total">¥{{ formatMoney(overview.platformTotalIncome) }}</span>
                <span class="donut-label">总计</span>
              </div>
            </div>
            <div class="comp-legend">
              <div class="legend-item">
                <span class="legend-dot" style="background:#6366f1"></span>
                <span class="legend-text">会员充值</span>
                <span class="legend-val">¥{{ formatMoney(overview.membershipIncome) }}</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background:#f59e0b"></span>
                <span class="legend-text">约稿服务费</span>
                <span class="legend-val">¥{{ formatMoney(overview.platformFeeIncome) }}</span>
              </div>
              <div v-if="Number(overview.couponSubsidy) > 0" class="legend-item">
                <span class="legend-dot" style="background:#ef4444"></span>
                <span class="legend-text">优惠券补贴</span>
                <span class="legend-val" style="color:#ef4444">-¥{{ formatMoney(overview.couponSubsidy) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 交易分布 -->
        <div class="comp-card">
          <h3 class="comp-title">交易类型分布</h3>
          <div class="comp-stats">
            <div class="comp-stat-item">
              <div class="comp-stat-bar">
                <div class="bar-fill membership" :style="{ width: membershipPct + '%' }"></div>
              </div>
              <div class="comp-stat-info">
                <span class="comp-stat-label">会员充值</span>
                <span class="comp-stat-count">{{ overview.membershipOrders || 0 }} 笔</span>
                <span class="comp-stat-amount">¥{{ formatMoney(overview.membershipIncome) }}</span>
              </div>
            </div>
            <div class="comp-stat-item">
              <div class="comp-stat-bar">
                <div class="bar-fill commission" :style="{ width: commissionPct + '%' }"></div>
              </div>
              <div class="comp-stat-info">
                <span class="comp-stat-label">约稿交易</span>
                <span class="comp-stat-count">{{ overview.commissionOrders || 0 }} 笔</span>
                <span class="comp-stat-amount">¥{{ formatMoney(overview.commissionTotal) }}</span>
              </div>
            </div>
            <div class="comp-stat-item">
              <div class="comp-stat-bar">
                <div class="bar-fill refund" :style="{ width: refundPct + '%' }"></div>
              </div>
              <div class="comp-stat-info">
                <span class="comp-stat-label">已退款</span>
                <span class="comp-stat-count">{{ overview.totalRefundedOrders || 0 }} 笔</span>
                <span class="comp-stat-amount">¥{{ formatMoney(overview.refundedAmount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收入趋势 -->
    <div class="trend-section">
      <div class="trend-header">
        <h2 class="section-title">收入趋势</h2>
        <div class="trend-tabs">
          <button v-for="t in trendTabs" :key="t.value" class="trend-tab"
            :class="{ active: trendPeriod === t.value }" @click="changeTrendPeriod(t.value)">
            {{ t.label }}
          </button>
        </div>
      </div>
      <div class="trend-chart">
        <div v-if="trendLoading" class="chart-loading">
          <div class="spinner"></div>
        </div>
        <div v-else class="chart-bars">
          <div class="chart-y-axis">
            <span v-for="(v, i) in yAxisLabels" :key="i" class="y-label">{{ v }}</span>
          </div>
          <div class="chart-body">
            <div class="chart-grid">
              <div v-for="i in 5" :key="i" class="grid-line"></div>
            </div>
            <div class="bar-container">
              <div v-for="(d, i) in displayTrendData" :key="i" class="bar-group"
                @mouseenter="showTooltip($event, d)" @mouseleave="hideTooltip">
                <div class="bar platform-bar" :style="{ height: getBarHeight(d.platformIncome) }"></div>
                <div class="bar artist-bar" :style="{ height: getBarHeight(d.artistIncome) }"></div>
                <span class="bar-label">{{ formatDateLabel(d.date) }}</span>
              </div>
            </div>
            <!-- 悬浮提示 -->
            <div v-if="tooltipVisible" class="chart-tooltip" :style="tooltipStyle">
              <div class="tooltip-date">{{ tooltipData.date }}</div>
              <div class="tooltip-row"><span class="tooltip-dot" style="background:#6366f1"></span>平台收入：¥{{ tooltipData.platformIncome }}</div>
              <div class="tooltip-row"><span class="tooltip-dot" style="background:#10b981"></span>画师收入：¥{{ tooltipData.artistIncome }}</div>
            </div>
          </div>
        </div>
        <div class="chart-legend">
          <span class="chart-legend-item"><span class="legend-dot" style="background: #6366f1"></span>平台收入</span>
          <span class="chart-legend-item"><span class="legend-dot" style="background: #10b981"></span>画师收入</span>
        </div>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="recent-section">
      <div class="section-header">
        <h2 class="section-title">最近交易</h2>
      </div>
      <div class="tx-table">
        <div class="tx-header">
          <span class="col-no">订单号</span>
          <span class="col-type">类型</span>
          <span class="col-amount">交易金额</span>
          <span class="col-fee">平台服务费</span>
          <span class="col-artist">画师收入</span>
          <span class="col-status">状态</span>
          <span class="col-time">支付时间</span>
        </div>
        <div v-if="recentLoading" class="table-loading">
          <div class="spinner"></div>
        </div>
        <template v-else>
          <div v-for="tx in recentTx" :key="tx.id" class="tx-row">
            <span class="col-no mono">{{ tx.orderNo?.slice(-12) || '-' }}</span>
            <span class="col-type">
              <span class="type-badge" :class="getTypeBadgeClass(tx.paymentType)">
                {{ getTypeLabel(tx.paymentType) }}
              </span>
            </span>
            <span class="col-amount price">¥{{ formatMoney(tx.amount) }}</span>
            <span class="col-fee">{{ tx.platformFee ? '¥' + formatMoney(tx.platformFee) : '-' }}</span>
            <span class="col-artist">
              {{ tx.paymentType === 'MEMBERSHIP' ? '-' : '¥' + formatMoney(tx.originalAmount != null ? tx.originalAmount : tx.amount - (tx.platformFee || 0)) }}
            </span>
            <span class="col-status">
              <span class="status-dot" :class="'dot-' + (tx.status || '').toLowerCase()"></span>
              {{ getStatusLabel(tx.status) }}
            </span>
            <span class="col-time">{{ formatTime(tx.paidAt) }}</span>
          </div>
          <div v-if="recentTx.length === 0" class="empty-table">暂无交易记录</div>
        </template>
      </div>
    </div>

    <!-- 提现管理 -->
    <div class="withdrawal-section">
      <div class="section-header">
        <h2 class="section-title">
          提现管理
          <span v-if="pendingWithdrawalCount > 0" class="pending-badge">{{ pendingWithdrawalCount }} 待审批</span>
        </h2>
        <div class="withdrawal-tabs">
          <button v-for="t in withdrawalStatusTabs" :key="t.value" class="trend-tab"
            :class="{ active: withdrawalStatusFilter === t.value }" @click="changeWithdrawalFilter(t.value)">
            {{ t.label }}
          </button>
        </div>
      </div>
      <div class="tx-table">
        <div class="tx-header withdrawal-header">
          <span class="col-id">ID</span>
          <span class="col-user">用户ID</span>
          <span class="col-amount">提现金额</span>
          <span class="col-account">支付宝账号</span>
          <span class="col-wstatus">状态</span>
          <span class="col-time">申请时间</span>
          <span class="col-action">操作</span>
        </div>
        <div v-if="withdrawalLoading" class="table-loading">
          <div class="spinner"></div>
        </div>
        <template v-else>
          <div v-for="w in withdrawals" :key="w.id" class="tx-row withdrawal-row">
            <span class="col-id mono">{{ w.id }}</span>
            <span class="col-user">{{ w.userId }}</span>
            <span class="col-amount price">¥{{ formatMoney(w.amount) }}</span>
            <span class="col-account mono">{{ w.alipayAccount || '-' }}</span>
            <span class="col-wstatus">
              <span class="type-badge" :class="getWithdrawalBadgeClass(w.status)">
                {{ getWithdrawalStatusLabel(w.status) }}
              </span>
            </span>
            <span class="col-time">{{ formatTime(w.createdAt) }}</span>
            <span class="col-action">
              <template v-if="w.status === 'PENDING'">
                <button class="action-btn approve-btn" @click="handleApprove(w)">通过</button>
                <button class="action-btn reject-btn" @click="handleReject(w)">拒绝</button>
              </template>
              <template v-else>
                <span class="processed-info">
                  {{ w.processedAt ? formatTime(w.processedAt) : '' }}
                  <span v-if="w.remark" class="remark-text" :title="w.remark">{{ w.remark }}</span>
                </span>
              </template>
            </span>
          </div>
          <div v-if="withdrawals.length === 0" class="empty-table">暂无提现记录</div>
        </template>
      </div>
      <!-- 分页 -->
      <div v-if="withdrawalTotal > withdrawalPageSize" class="pagination">
        <button class="page-btn" :disabled="withdrawalPage === 0" @click="withdrawalPage--; loadWithdrawals()">上一页</button>
        <span class="page-info">{{ withdrawalPage + 1 }} / {{ Math.ceil(withdrawalTotal / withdrawalPageSize) }}</span>
        <button class="page-btn" :disabled="(withdrawalPage + 1) * withdrawalPageSize >= withdrawalTotal" @click="withdrawalPage++; loadWithdrawals()">下一页</button>
      </div>
    </div>

    <!-- 拒绝弹窗 -->
    <div v-if="rejectDialogVisible" class="dialog-overlay" @click.self="rejectDialogVisible = false">
      <div class="dialog-box">
        <h3 class="dialog-title">拒绝提现 #{{ rejectTarget?.id }}</h3>
        <p class="dialog-desc">拒绝后金额将退回画师可用余额</p>
        <textarea v-model="rejectRemark" class="dialog-input" placeholder="拒绝原因（选填）" rows="3"></textarea>
        <div class="dialog-actions">
          <button class="action-btn" @click="rejectDialogVisible = false">取消</button>
          <button class="action-btn reject-btn" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFinanceOverview, getFinanceTrend, getRecentTransactions, getWithdrawals, approveWithdrawal, rejectWithdrawal, getPendingWithdrawalCount } from '@/api/finance'

const overview = ref({
  platformTotalIncome: 0, artistTotalIncome: 0,
  membershipIncome: 0, platformFeeIncome: 0,
  feeDiscountTotal: 0, refundedAmount: 0,
  couponSubsidy: 0,
  commissionTotal: 0, totalPaidOrders: 0,
  totalRefundedOrders: 0, membershipOrders: 0,
  commissionOrders: 0,
  todayIncome: 0, todayPlatformFee: 0,
  todayMembership: 0, todayOrders: 0
})

const trendData = ref([])
const trendLoading = ref(false)
const trendPeriod = ref('7')
const recentTx = ref([])
const recentLoading = ref(false)

const trendTabs = [
  { label: '近7天', value: '7' },
  { label: '近30天', value: '30' },
  { label: '近90天', value: '90' }
]

// 计算饼图弧度
const membershipArc = computed(() => {
  const total = Number(overview.value.platformTotalIncome) || 1
  const membership = Number(overview.value.membershipIncome) || 0
  return (membership / total) * 314.16
})
const feeArc = computed(() => {
  const total = Number(overview.value.platformTotalIncome) || 1
  const fee = Number(overview.value.platformFeeIncome) || 0
  return (fee / total) * 314.16
})

// 交易类型百分比
const totalOrders = computed(() => (overview.value.membershipOrders || 0) + (overview.value.commissionOrders || 0) + (overview.value.totalRefundedOrders || 0) || 1)
const membershipPct = computed(() => ((overview.value.membershipOrders || 0) / totalOrders.value * 100).toFixed(0))
const commissionPct = computed(() => ((overview.value.commissionOrders || 0) / totalOrders.value * 100).toFixed(0))
const refundPct = computed(() => ((overview.value.totalRefundedOrders || 0) / totalOrders.value * 100).toFixed(0))

// 趋势图数据
const displayTrendData = computed(() => {
  if (trendData.value.length <= 15) return trendData.value
  // 如果数据点太多，采样展示
  const step = Math.ceil(trendData.value.length / 15)
  return trendData.value.filter((_, i) => i % step === 0 || i === trendData.value.length - 1)
})

const maxTrendValue = computed(() => {
  let max = 0
  trendData.value.forEach(d => {
    const v = Math.max(Number(d.platformIncome) || 0, Number(d.artistIncome) || 0)
    if (v > max) max = v
  })
  return max || 100
})

const yAxisLabels = computed(() => {
  const m = maxTrendValue.value
  return [formatShortMoney(m), formatShortMoney(m * 0.75), formatShortMoney(m * 0.5), formatShortMoney(m * 0.25), '0']
})

function getBarHeight(value) {
  const v = Number(value) || 0
  const pct = (v / maxTrendValue.value) * 100
  return Math.max(pct, 1) + '%'
}

// 柱状图 tooltip
const tooltipVisible = ref(false)
const tooltipData = ref({ date: '', platformIncome: 0, artistIncome: 0 })
const tooltipStyle = ref({})

function showTooltip(event, d) {
  tooltipData.value = d
  tooltipVisible.value = true
  const rect = event.currentTarget.closest('.chart-body').getBoundingClientRect()
  const barRect = event.currentTarget.getBoundingClientRect()
  const x = barRect.left - rect.left + barRect.width / 2
  const y = barRect.top - rect.top
  tooltipStyle.value = {
    left: x + 'px',
    top: Math.max(y - 10, 0) + 'px',
    transform: 'translate(-50%, -100%)'
  }
}

function hideTooltip() {
  tooltipVisible.value = false
}

function formatMoney(v) {
  const n = Number(v) || 0
  return n.toFixed(2)
}

function formatShortMoney(v) {
  const n = Number(v) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n.toFixed(0)
}

function formatDateLabel(dateStr) {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return (parts[1] || '') + '/' + (parts[2] || '')
}

function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function getTypeLabel(type) {
  return { DEPOSIT: '定金', FINAL_PAYMENT: '尾款', MEMBERSHIP: '会员' }[type] || type
}

function getTypeBadgeClass(type) {
  return {
    DEPOSIT: 'badge-deposit',
    FINAL_PAYMENT: 'badge-final',
    MEMBERSHIP: 'badge-membership'
  }[type] || ''
}

function getStatusLabel(status) {
  return { PAID: '已支付', REFUNDED: '已退款', PENDING: '待支付', CLOSED: '已关闭' }[status] || status
}

async function loadOverview() {
  try {
    const res = await getFinanceOverview()
    if (res.code === 200 && res.data) {
      overview.value = { ...overview.value, ...res.data }
    }
  } catch {
    ElMessage.error('加载财务概览失败')
  }
}

async function loadTrend(days) {
  trendLoading.value = true
  try {
    const res = await getFinanceTrend({ period: 'day', days })
    if (res.code === 200) {
      trendData.value = res.data || []
    }
  } catch {
    ElMessage.error('加载趋势数据失败')
  } finally {
    trendLoading.value = false
  }
}

async function loadRecent() {
  recentLoading.value = true
  try {
    const res = await getRecentTransactions(30)
    if (res.code === 200) {
      recentTx.value = res.data || []
    }
  } catch {
    ElMessage.error('加载交易记录失败')
  } finally {
    recentLoading.value = false
  }
}

function changeTrendPeriod(val) {
  trendPeriod.value = val
  loadTrend(Number(val))
}

// =========== 提现管理 ===========
const withdrawals = ref([])
const withdrawalLoading = ref(false)
const withdrawalPage = ref(0)
const withdrawalPageSize = 20
const withdrawalTotal = ref(0)
const withdrawalStatusFilter = ref('')
const pendingWithdrawalCount = ref(0)
const rejectDialogVisible = ref(false)
const rejectTarget = ref(null)
const rejectRemark = ref('')

const withdrawalStatusTabs = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

function getWithdrawalStatusLabel(status) {
  return { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status
}

function getWithdrawalBadgeClass(status) {
  return { PENDING: 'badge-deposit', APPROVED: 'badge-final', REJECTED: 'badge-rejected' }[status] || ''
}

function changeWithdrawalFilter(val) {
  withdrawalStatusFilter.value = val
  withdrawalPage.value = 0
  loadWithdrawals()
}

async function loadWithdrawals() {
  withdrawalLoading.value = true
  try {
    const params = { page: withdrawalPage.value, size: withdrawalPageSize }
    if (withdrawalStatusFilter.value) params.status = withdrawalStatusFilter.value
    const res = await getWithdrawals(params)
    if (res.code === 200 && res.data) {
      withdrawals.value = res.data.content || []
      withdrawalTotal.value = res.data.totalElements || 0
    }
  } catch {
    ElMessage.error('加载提现记录失败')
  } finally {
    withdrawalLoading.value = false
  }
}

async function loadPendingCount() {
  try {
    const res = await getPendingWithdrawalCount()
    if (res.code === 200) pendingWithdrawalCount.value = res.data || 0
  } catch { /* ignore */ }
}

async function handleApprove(w) {
  try {
    const res = await approveWithdrawal(w.id)
    if (res.code === 200) {
      ElMessage.success('审批通过')
      loadWithdrawals()
      loadPendingCount()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleReject(w) {
  rejectTarget.value = w
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  try {
    const res = await rejectWithdrawal(rejectTarget.value.id, rejectRemark.value)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      rejectDialogVisible.value = false
      loadWithdrawals()
      loadPendingCount()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadOverview()
  loadTrend(7)
  loadRecent()
  loadWithdrawals()
  loadPendingCount()
})
</script>

<style scoped>
.finance-page {
  max-width: 100%;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-text-inverse);
  margin: 0 0 16px;
}

/* 概览卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.stat-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--c-border);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  border-color: var(--c-border-gold);
  box-shadow: var(--shadow-sm), var(--shadow-gold);
}
.stat-icon {
  width: 56px; height: 56px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-card.platform .stat-icon  { background: rgba(106,171,222,0.12); color: #6aabde; }
.stat-card.artist .stat-icon    { background: rgba(46,201,138,0.12);  color: #2ec98a; }
.stat-card.membership .stat-icon{ background: rgba(167,139,250,0.12); color: #a78bfa; }
.stat-card.fee .stat-icon       { background: rgba(240,160,48,0.12);  color: #f0a030; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 800; color: var(--c-text-inverse); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--c-text-muted); margin-top: 2px; font-weight: 500; }
.stat-sub { font-size: 11px; color: var(--c-text-muted); margin-top: 2px; opacity: 0.7; }

/* 今日数据 */
.today-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 28px;
}
.today-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  border: 1px solid var(--c-border);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-card);
}
.today-value { font-size: 20px; font-weight: 700; color: var(--c-text-inverse); }
.today-label { font-size: 12px; color: var(--c-text-muted); margin-top: 4px; }

/* 收入构成 */
.comp-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}
.comp-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
}
.comp-title { font-size: 15px; font-weight: 600; color: var(--c-text-inverse); margin: 0 0 20px; }

.comp-chart {
  display: flex;
  align-items: center;
  gap: 32px;
}
.donut-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  flex-shrink: 0;
}
.donut-svg { width: 100%; height: 100%; transform: rotate(-90deg); }
.donut-center {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  display: flex;
  flex-direction: column;
}
.donut-total { font-size: 14px; font-weight: 700; color: var(--c-text-inverse); }
.donut-label { font-size: 11px; color: var(--c-text-muted); }

.comp-legend { display: flex; flex-direction: column; gap: 14px; }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.legend-dot { width: 10px; height: 10px; border-radius: 3px; flex-shrink: 0; }
.legend-text { color: var(--c-text-secondary); flex: 1; }
.legend-val { font-weight: 600; color: var(--c-text); }

/* 交易分布 */
.comp-stats { display: flex; flex-direction: column; gap: 20px; }
.comp-stat-item { display: flex; flex-direction: column; gap: 8px; }
.comp-stat-bar { height: 8px; background: var(--c-surface-3); border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.bar-fill.membership { background: #a78bfa; }
.bar-fill.commission { background: #2ec98a; }
.bar-fill.refund     { background: var(--c-danger); }
.comp-stat-info { display: flex; align-items: center; gap: 12px; font-size: 13px; }
.comp-stat-label { color: var(--c-text-secondary); min-width: 70px; }
.comp-stat-count { color: var(--c-text-muted); }
.comp-stat-amount { font-weight: 600; color: var(--c-text); margin-left: auto; }

/* 趋势图 */
.trend-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 28px;
  border: 1px solid var(--c-border);
  box-shadow: var(--shadow-card);
}
.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.trend-header .section-title { margin: 0; }
.trend-tabs { display: flex; gap: 4px; }
.trend-tab {
  padding: 6px 14px; border: none; background: var(--c-surface-2);
  border-radius: var(--radius-sm); font-size: 12px; color: var(--c-text-muted);
  cursor: pointer; transition: all 0.2s; font-weight: 500;
}
.trend-tab:hover { color: var(--c-text); background: var(--c-surface-3); }
.trend-tab.active { background: var(--c-primary); color: #0a1018; font-weight: 700; }

.chart-bars {
  display: flex;
  height: 220px;
  gap: 8px;
}
.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 50px;
  text-align: right;
  padding-right: 8px;
}
.y-label { font-size: 10px; color: var(--c-text-muted); }
.chart-body {
  flex: 1;
  position: relative;
}
.chart-grid {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.grid-line { border-bottom: 1px solid var(--c-border-light); }
.bar-container {
  display: flex;
  align-items: flex-end;
  height: calc(100% - 24px);
  gap: 2px;
  position: relative;
  z-index: 1;
}
.bar-group {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 2px;
  min-width: 0;
  position: relative;
  height: 100%;
}
.bar { flex: 1; border-radius: 3px 3px 0 0; min-height: 2px; transition: height 0.3s ease; }
.platform-bar { background: #a78bfa; }
.artist-bar   { background: #2ec98a; }
.bar-group:hover { background: rgba(201,168,76,0.06); border-radius: 4px; }
.bar-label {
  position: absolute; bottom: -20px; left: 50%; transform: translateX(-50%);
  font-size: 9px; color: var(--c-text-muted); white-space: nowrap;
}
.chart-legend { display: flex; gap: 20px; justify-content: center; margin-top: 12px; }
.chart-tooltip {
  position: absolute; z-index: 10;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border-gold);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-lg);
  padding: 10px 14px;
  pointer-events: none; white-space: nowrap; font-size: 12px;
}
.tooltip-date { font-weight: 600; margin-bottom: 6px; color: var(--c-text-inverse); }
.tooltip-row { display: flex; align-items: center; gap: 6px; color: var(--c-text-secondary); line-height: 1.8; }
.tooltip-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.chart-legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--c-text-muted); }

.chart-loading, .table-loading {
  display: flex; justify-content: center; align-items: center; height: 200px;
}
.spinner {
  width: 28px; height: 28px; border: 3px solid var(--c-border);
  border-top-color: var(--c-primary); border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 最近交易 */
.recent-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--c-border);
  margin-bottom: 28px;
  box-shadow: var(--shadow-card);
}
.section-header { margin-bottom: 16px; }
.tx-header, .tx-row {
  display: grid;
  grid-template-columns: 1.5fr 0.8fr 1fr 1fr 1fr 0.8fr 1.2fr;
  gap: 8px; padding: 10px 0; align-items: center; font-size: 13px;
}
.tx-header {
  color: var(--c-text-muted); font-size: 11px; font-weight: 700;
  border-bottom: 1px solid var(--c-border);
  text-transform: uppercase; letter-spacing: 0.5px;
}
.tx-row {
  color: var(--c-text-secondary);
  border-bottom: 1px solid var(--c-border-light);
  transition: background 0.15s;
}
.tx-row:hover { background: var(--c-primary-bg); }
.tx-row:last-child { border-bottom: none; }

.mono { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--c-text-muted); }
.price { color: var(--c-text-inverse); font-weight: 600; }

.type-badge { padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 600; }
.badge-deposit    { background: var(--c-warning-bg);  color: var(--c-warning); }
.badge-final      { background: var(--c-success-bg);  color: var(--c-success); }
.badge-membership { background: var(--c-primary-bg);  color: var(--c-primary); }
.badge-rejected   { background: var(--c-danger-bg);   color: var(--c-danger);  }

.status-dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-right: 4px; }
.dot-paid     { background: var(--c-success); }
.dot-refunded { background: var(--c-danger);  }
.dot-pending  { background: var(--c-warning); }
.dot-closed   { background: var(--c-text-muted); }

.empty-table { text-align: center; padding: 40px; color: var(--c-text-muted); font-size: 14px; }

@media (max-width: 900px) {
  .stat-cards, .today-cards { grid-template-columns: repeat(2, 1fr); }
  .comp-row { grid-template-columns: 1fr; }
  .tx-header, .tx-row { grid-template-columns: 1.5fr 0.8fr 1fr 0.8fr; }
  .col-fee, .col-artist, .col-time { display: none; }
}

/* 提现管理 */
.withdrawal-section {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--c-border);
  margin-bottom: 28px;
  box-shadow: var(--shadow-card);
}
.withdrawal-section .section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.pending-badge {
  background: var(--c-warning-bg); color: var(--c-warning);
  font-size: 12px; font-weight: 600;
  padding: 2px 10px; border-radius: 10px; margin-left: 10px;
}
.withdrawal-header, .withdrawal-row {
  grid-template-columns: 0.5fr 0.8fr 1fr 1.5fr 0.8fr 1.2fr 1.2fr !important;
}

.action-btn {
  padding: 4px 12px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-xs);
  font-size: 12px; cursor: pointer;
  background: transparent;
  color: var(--c-text-secondary);
  transition: all 0.15s; margin-right: 6px;
}
.approve-btn { color: var(--c-success); border-color: var(--c-success-border); }
.approve-btn:hover { background: var(--c-success-bg); }
.reject-btn  { color: var(--c-danger);  border-color: var(--c-danger-border); }
.reject-btn:hover  { background: var(--c-danger-bg);  }

.processed-info { font-size: 11px; color: var(--c-text-muted); }
.remark-text {
  display: inline-block; max-width: 100px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  vertical-align: middle; margin-left: 4px; color: var(--c-warning);
}

.pagination {
  display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 16px;
}
.page-btn {
  padding: 6px 16px; border: 1px solid var(--c-border);
  border-radius: var(--radius-sm); background: var(--c-surface-2);
  color: var(--c-text); transition: all var(--transition-fast);
}
.page-btn:hover:not(:disabled) { border-color: var(--c-primary); color: var(--c-primary); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 13px; color: var(--c-text-muted); }
</style> 
