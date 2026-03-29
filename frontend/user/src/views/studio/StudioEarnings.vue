<template>
  <div class="earnings-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">收入管理</h1>
        <p class="page-desc">查看你的钱包余额、收入统计与交易明细</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p class="loading-text">加载中...</p>
    </div>

    <template v-else>
      <!-- 钱包余额卡片 -->
      <div class="wallet-hero">
        <div class="wallet-main-card">
          <div class="wallet-bg-pattern"></div>
          <div class="wallet-content">
            <div class="wallet-top">
              <span class="wallet-label">可用余额</span>
              <span class="wallet-icon"><svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>
            </div>
            <div class="wallet-balance">
              <span class="balance-symbol">¥</span>
              <span class="balance-amount">{{ formatMoney(walletData.availableBalance) }}</span>
            </div>
            <div class="wallet-actions">
              <button class="withdraw-btn" :disabled="!walletData.availableBalance || walletData.availableBalance <= 0" @click="showWithdrawDialog = true">提现</button>
            </div>
            <div class="wallet-sub-info">
              <div class="sub-item">
                <span class="sub-label">冻结中</span>
                <span class="sub-value">¥{{ formatMoney(walletData.frozenAmount) }}</span>
              </div>
              <div class="sub-divider"></div>
              <div class="sub-item">
                <span class="sub-label">已提现</span>
                <span class="sub-value">¥{{ formatMoney(walletData.withdrawnAmount) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 收入统计 -->
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-icon total-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">¥{{ formatMoney(walletData.totalIncome) }}</span>
              <span class="stat-label">累计收入</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon month-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">¥{{ formatMoney(walletData.monthIncome) }}</span>
              <span class="stat-label">本月收入</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon count-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ completedCount }}</span>
              <span class="stat-label">已完成约稿</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon pending-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ pendingCount }}</span>
              <span class="stat-label">进行中约稿</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 约稿收入明细 -->
      <div class="detail-section">
        <div class="section-header">
          <h3 class="section-title">约稿收入明细</h3>
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

        <div v-if="filteredRecords.length > 0" class="records-list">
          <div class="records-header">
            <span class="col-title">约稿标题</span>
            <span class="col-client">委托方</span>
            <span class="col-amount">金额</span>
            <span class="col-status">状态</span>
            <span class="col-date">日期</span>
          </div>
          <div v-for="r in filteredRecords" :key="r.id" class="record-row">
            <span class="col-title text-ellipsis">{{ r.title || '未命名约稿' }}</span>
            <span class="col-client">{{ r.clientName || '用户' }}</span>
            <span class="col-amount price">¥{{ (r.totalAmount || 0).toFixed(2) }}</span>
            <span class="col-status">
              <span class="status-badge" :class="'badge-' + r.status.toLowerCase()">
                {{ getStatusText(r.status) }}
              </span>
            </span>
            <span class="col-date">{{ formatDate(r.updatedAt || r.createdAt) }}</span>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></div>
          <h4>{{ activePeriod === 'all' ? '暂无约稿收入记录' : '该时间段暂无记录' }}</h4>
          <p>接到约稿并完成后，收入将自动记录在这里</p>
          <button class="empty-action" @click="$router.push('/studio/plans')">
            管理约稿方案 →
          </button>
        </div>
      </div>

      <!-- 提现记录 -->
      <div class="detail-section">
        <div class="section-header">
          <h3 class="section-title">提现记录</h3>
          <button class="period-btn active" @click="loadWithdrawalHistory">刷新</button>
        </div>
        <div v-if="withdrawalRecords.length > 0" class="records-list">
          <div class="records-header withdrawal-records-header">
            <span class="col-amount">金额</span>
            <span class="col-account">支付宝账号</span>
            <span class="col-status">状态</span>
            <span class="col-remark">备注</span>
            <span class="col-date">申请时间</span>
          </div>
          <div v-for="w in withdrawalRecords" :key="w.id" class="record-row withdrawal-records-header">
            <span class="col-amount price">¥{{ (w.amount || 0).toFixed(2) }}</span>
            <span class="col-account text-ellipsis">{{ w.alipayAccount || '-' }}</span>
            <span class="col-status">
              <span class="status-badge" :class="'badge-' + (w.status || '').toLowerCase()">
                {{ { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已拒绝' }[w.status] || w.status }}
              </span>
            </span>
            <span class="col-remark text-ellipsis">{{ w.remark || '-' }}</span>
            <span class="col-date">{{ formatDate(w.createdAt) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg></div>
          <h4>暂无提现记录</h4>
          <p>申请提现后，记录将显示在这里</p>
        </div>
      </div>

      <!-- 提示信息 -->
      <div class="info-cards">
        <div class="info-card">
          <div class="info-icon"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#0096FA" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></div>
          <div class="info-content">
            <h4>收入结算规则</h4>
            <p>委托方确认完稿后，约稿金额将在扣除平台手续费（5%）后自动入账到你的可用余额。VIP 画师享受手续费优惠。</p>
          </div>
        </div>
        <div class="info-card">
          <div class="info-icon"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#0096FA" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>
          <div class="info-content">
            <h4>资金安全</h4>
            <p>进行中的约稿金额将被冻结保管，确保交易安全。约稿完成后冻结金额将自动释放到可用余额。</p>
          </div>
        </div>
        <div class="info-card">
          <div class="info-icon"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#0096FA" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5 12 12 5 19 12"/></svg></div>
          <div class="info-content">
            <h4>提现说明</h4>
            <p>提现申请提交后需管理员审批，审批通过后将转账至你的支付宝账户。通常1-3个工作日内处理完成。</p>
          </div>
        </div>
      </div>
    </template>

    <!-- 提现弹窗 -->
    <el-dialog v-model="showWithdrawDialog" title="提现" width="420px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="可用余额">
          <div style="font-size: 20px; font-weight: 700; color: #0096FA;">¥{{ formatMoney(walletData.availableBalance) }}</div>
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input-number v-model="withdrawForm.amount" :min="0.01" :max="walletData.availableBalance || 0" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="支付宝账号">
          <el-input v-model="withdrawForm.alipayAccount" placeholder="请输入接收提现的支付宝账号" />
        </el-form-item>
        <el-form-item label="支付宝实名姓名">
          <el-input v-model="withdrawForm.alipayName" placeholder="请输入支付宝账号的真实姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWithdrawDialog = false">取消</el-button>
        <el-button type="primary" :loading="withdrawLoading" :disabled="!withdrawForm.amount || !withdrawForm.alipayAccount || !withdrawForm.alipayName" @click="handleWithdraw">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWalletOverview, withdrawFromWallet, getMyWithdrawals } from '@/api/studio'
import { getMyCommissions } from '@/api/commission'

const loading = ref(true)
const walletData = ref({
  totalIncome: 0,
  availableBalance: 0,
  frozenAmount: 0,
  withdrawnAmount: 0,
  monthIncome: 0
})
const allCommissions = ref([])
const activePeriod = ref('all')

const showWithdrawDialog = ref(false)
const withdrawLoading = ref(false)
const withdrawForm = ref({ amount: null, alipayAccount: '', alipayName: '' })
const withdrawalRecords = ref([])

const periods = [
  { label: '全部', value: 'all' },
  { label: '本月', value: 'month' },
  { label: '近三月', value: '3months' },
  { label: '今年', value: 'year' }
]

const completedCount = computed(() =>
  allCommissions.value.filter(c => c.status === 'COMPLETED').length
)

const pendingCount = computed(() =>
  allCommissions.value.filter(c =>
    ['IN_PROGRESS', 'DELIVERED', 'DEPOSIT_PAID'].includes(c.status)
  ).length
)

const filteredRecords = computed(() => {
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

function formatMoney(val) {
  const n = Number(val) || 0
  return n.toFixed(2)
}

function getStatusText(status) {
  return {
    COMPLETED: '已结算',
    IN_PROGRESS: '创作中',
    DELIVERED: '待确认',
    DEPOSIT_PAID: '已付定金'
  }[status] || status
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function loadData() {
  loading.value = true
  try {
    const [walletRes, commRes] = await Promise.allSettled([
      getWalletOverview(),
      getMyCommissions({ role: 'artist', page: 0, size: 200 })
    ])

    let walletLoaded = false
    if (walletRes.status === 'fulfilled' && walletRes.value?.code === 200 && walletRes.value.data) {
      walletData.value = { ...walletData.value, ...walletRes.value.data }
      walletLoaded = true
    }

    if (commRes.status === 'fulfilled' && commRes.value?.code === 200) {
      allCommissions.value = commRes.value.data?.content || commRes.value.data?.records || []
    }

    // 仅当钱包API未返回数据时，才从约稿推算（兜底逻辑）
    if (!walletLoaded && allCommissions.value.length > 0) {
      const completed = allCommissions.value.filter(c => c.status === 'COMPLETED')
      walletData.value.totalIncome = completed.reduce((s, c) => s + (c.totalAmount || 0), 0)
      const now = new Date()
      walletData.value.monthIncome = completed
        .filter(c => {
          const d = new Date(c.updatedAt || c.createdAt)
          return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
        })
        .reduce((s, c) => s + (c.totalAmount || 0), 0)
      const pending = allCommissions.value.filter(c => ['IN_PROGRESS', 'DELIVERED', 'DEPOSIT_PAID'].includes(c.status))
      walletData.value.frozenAmount = pending.reduce((s, c) => s + (c.depositAmount || 0), 0)
      walletData.value.availableBalance = Math.max(walletData.value.totalIncome - walletData.value.frozenAmount - (walletData.value.withdrawnAmount || 0), 0)
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

async function loadWithdrawalHistory() {
  try {
    const res = await getMyWithdrawals({ page: 0, size: 50 })
    if (res.code === 200 && res.data) {
      withdrawalRecords.value = res.data.content || []
    }
  } catch { /* ignore */ }
}

onMounted(loadWithdrawalHistory)

async function handleWithdraw() {
  if (!withdrawForm.value.amount || !withdrawForm.value.alipayAccount) return
  withdrawLoading.value = true
  try {
    const res = await withdrawFromWallet({
      amount: withdrawForm.value.amount,
      alipayAccount: withdrawForm.value.alipayAccount,
      alipayName: withdrawForm.value.alipayName
    })
    if (res.code === 200) {
      ElMessage.success('提现申请已提交，请等待管理员审批')
      showWithdrawDialog.value = false
      withdrawForm.value = { amount: null, alipayAccount: '', alipayName: '' }
      await loadData()
      await loadWithdrawalHistory()
    } else {
      ElMessage.error(res.message || '提现失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提现失败')
  } finally {
    withdrawLoading.value = false
  }
}
</script>

<style scoped>
.earnings-page { max-width: 100%; background: #fff; }

.page-header { margin-bottom: 28px; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px 0; }
.page-desc { font-size: 14px; color: #999; margin: 0; }

.loading-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 80px 0; gap: 12px;
}
.spinner {
  width: 32px; height: 32px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.loading-text { color: #bbb; font-size: 14px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* === 钱包 Hero 区域 === */
.wallet-hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}

.wallet-main-card {
  background: linear-gradient(135deg, #0096FA 0%, #0070D9 50%, #004AAD 100%);
  border-radius: 20px;
  padding: 28px 32px;
  color: #fff;
  position: relative;
  overflow: hidden;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.wallet-bg-pattern {
  position: absolute; top: -30px; right: -30px;
  width: 160px; height: 160px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  pointer-events: none;
}
.wallet-bg-pattern::after {
  content: '';
  position: absolute; bottom: -50px; left: -60px;
  width: 120px; height: 120px;
  border-radius: 50%;
  background: rgba(255,255,255,0.05);
}
.wallet-content { position: relative; z-index: 1; }
.wallet-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
}
.wallet-label { font-size: 14px; opacity: 0.85; font-weight: 500; }
.wallet-icon { font-size: 24px; }
.wallet-balance {
  display: flex; align-items: baseline; gap: 4px;
  margin-bottom: 20px;
}
.balance-symbol { font-size: 20px; font-weight: 600; opacity: 0.9; }
.balance-amount { font-size: 36px; font-weight: 800; letter-spacing: -1px; }

.wallet-actions { margin: 12px 0; }
.withdraw-btn {
  background: rgba(255,255,255,0.2);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.4);
  border-radius: 999px;
  padding: 7px 22px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.withdraw-btn:hover:not(:disabled) { background: rgba(255,255,255,0.3); }
.withdraw-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.wallet-sub-info {
  display: flex; align-items: center; gap: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.15);
}
.sub-item { display: flex; flex-direction: column; gap: 2px; }
.sub-label { font-size: 11px; opacity: 0.7; }
.sub-value { font-size: 15px; font-weight: 600; }
.sub-divider {
  width: 1px; height: 28px;
  background: rgba(255,255,255,0.2);
}

/* === 统计卡片 === */
.stats-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.stat-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.07); }
.stat-icon {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.total-icon { background: #E6F7F0; color: #00C48C; }
.month-icon { background: #EBF5FF; color: #0096FA; }
.count-icon { background: #F3E8FF; color: #9B59B6; }
.pending-icon { background: #FFF3E0; color: #FF9800; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 20px; font-weight: 700; color: #1a1a1a; line-height: 1.2; }
.stat-label { font-size: 12px; color: #999; margin-top: 2px; }

/* === 明细区 === */
.detail-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px 28px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 18px;
}
.section-title { font-size: 20px; font-weight: 700; color: #1a1a1a; margin: 0; }
.period-filter { display: flex; gap: 6px; }
.period-btn {
  padding: 6px 16px; border: none; background: #f5f5f5;
  border-radius: 999px; font-size: 12px; color: #666;
  cursor: pointer; transition: all 0.15s;
}
.period-btn:hover { color: #333; background: #ebebeb; }
.period-btn.active { background: #0096FA; color: #fff; }

/* 表格 */
.records-header, .record-row {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1fr 1fr 1fr;
  gap: 12px;
  padding: 12px 0;
  align-items: center;
  font-size: 13px;
}
.records-header {
  color: #bbb; font-weight: 600; font-size: 12px;
  border-bottom: 1px solid #f0f0f0;
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

.status-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}
.badge-completed { background: #E6F7F0; color: #00C48C; }
.badge-in_progress { background: #EBF5FF; color: #0096FA; }
.badge-delivered { background: #F3E8FF; color: #9B59B6; }
.badge-deposit_paid { background: #FFF3E0; color: #FF9800; }
.badge-pending { background: #FFF3E0; color: #FF9800; }
.badge-approved { background: #E6F7F0; color: #00C48C; }
.badge-rejected { background: #FEE2E2; color: #DC2626; }

/* 提现记录表格 */
.withdrawal-records-header {
  grid-template-columns: 1fr 1.5fr 1fr 1.5fr 1fr !important;
}

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 48px 0; text-align: center;
}
.empty-icon { margin-bottom: 12px; opacity: 0.6; }
.empty-state h4 {
  font-size: 16px; font-weight: 600; color: #999; margin: 0 0 6px 0;
}
.empty-state p {
  font-size: 13px; color: #bbb; margin: 0 0 18px 0;
}
.empty-action {
  padding: 8px 22px; border: 1px solid #0096FA; background: transparent;
  color: #0096FA; border-radius: 999px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
}
.empty-action:hover { background: #0096FA; color: #fff; }

/* === 提示信息 === */
.info-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.info-card {
  background: #fff;
  border-radius: 20px;
  padding: 22px 24px;
  display: flex;
  gap: 14px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.info-icon { flex-shrink: 0; margin-top: 2px; }
.info-content h4 {
  font-size: 14px; font-weight: 600; color: #1a1a1a; margin: 0 0 6px 0;
}
.info-content p {
  font-size: 13px; color: #888; margin: 0; line-height: 1.6;
}

/* 响应式 */
@media (max-width: 1024px) {
  .wallet-hero { grid-template-columns: 1fr; }
  .stats-cards { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .info-cards { grid-template-columns: 1fr; }
  .records-header, .record-row {
    grid-template-columns: 2fr 1fr 1fr;
  }
  .col-client, .col-date { display: none; }
}
</style>
