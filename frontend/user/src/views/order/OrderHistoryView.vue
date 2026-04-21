<template>
  <div class="order-page">
    <!-- Hero 头部 -->
    <section class="order-hero">
      <div class="hero-bg-orbs">
        <div class="orb orb-a"></div>
        <div class="orb orb-b"></div>
      </div>
      <div class="hero-content">
        <p class="hero-eyebrow">交易中心</p>
        <h1 class="hero-title">我的订单</h1>
        <p class="hero-desc">查看你的全部支付记录和交易详情</p>
        <div class="hero-stats" v-if="orderStats.total > 0">
          <div class="hero-stat">
            <span class="stat-num">{{ orderStats.total }}</span>
            <span class="stat-label">总订单</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">¥{{ orderStats.totalAmount }}</span>
            <span class="stat-label">总消费</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">{{ orderStats.pendingCount }}</span>
            <span class="stat-label">待支付</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 主区域 -->
    <div class="order-main">
      <!-- 筛选栏 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <button
            class="tab-btn"
            :class="{ active: statusFilter === '' }"
            @click="setStatusFilter('')"
          >全部</button>
          <button
            class="tab-btn"
            :class="{ active: statusFilter === 'PENDING' }"
            @click="setStatusFilter('PENDING')"
          >待支付</button>
          <button
            class="tab-btn"
            :class="{ active: statusFilter === 'PAID' }"
            @click="setStatusFilter('PAID')"
          >已支付</button>
          <button
            class="tab-btn"
            :class="{ active: statusFilter === 'REFUNDED' }"
            @click="setStatusFilter('REFUNDED')"
          >已退款</button>
          <button
            class="tab-btn"
            :class="{ active: statusFilter === 'CLOSED' }"
            @click="setStatusFilter('CLOSED')"
          >已关闭</button>
        </div>
        <el-select
          v-model="typeFilter"
          placeholder="所有类型"
          clearable
          @change="handleFilter"
          class="type-select"
        >
          <el-option label="会员充值" value="MEMBERSHIP" />
          <el-option label="定金" value="DEPOSIT" />
          <el-option label="尾款" value="FINAL_PAYMENT" />
        </el-select>
      </div>

    <!-- 订单列表 -->
    <div class="orders-list" v-loading="loading">
      <template v-if="orders.length > 0">
        <div
          class="order-item"
          :class="{ clickable: !!order.commissionId }"
          v-for="order in orders"
          :key="order.id"
          @click="openOrderDetail(order)"
        >
          <!-- 左侧图标 -->
          <div class="order-type-icon" :class="typeClass(order.paymentType)">
            <svg v-if="order.paymentType === 'MEMBERSHIP'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="4" width="22" height="16" rx="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </div>

          <!-- 中间信息 -->
          <div class="order-detail">
            <div class="order-main-row">
              <span class="order-subject">{{ order.subject || '—' }}</span>
              <span class="order-amount" :class="{ refunded: order.status === 'REFUNDED' }">
                {{ order.status === 'REFUNDED' ? '-' : '' }}¥{{ order.amount }}
              </span>
            </div>
            <div class="order-meta-row">
              <span class="order-type-badge" :class="typeClass(order.paymentType)">
                {{ typeLabel(order.paymentType) }}
              </span>
              <span class="order-status-dot" :class="statusClass(order.status)"></span>
              <span class="order-status-text" :class="statusClass(order.status)">
                {{ statusLabel(order.status) }}
              </span>
              <span class="meta-divider">·</span>
              <span class="order-time">{{ formatTime(order.createdAt) }}</span>
            </div>
            <div class="order-extra" v-if="order.discountAmount || order.platformFee">
              <span v-if="order.discountAmount" class="extra-tag discount">
                优惠 -¥{{ order.discountAmount }}
              </span>
              <span v-if="order.platformFee" class="extra-tag fee">
                服务费 ¥{{ order.platformFee }}
              </span>
            </div>
            <div class="order-no">{{ order.orderNo }}</div>
            <div v-if="getAfterSaleSummary(order)" class="after-sale-note">
              {{ getAfterSaleSummary(order) }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="order-actions">
            <button v-if="order.commissionId" class="order-action-btn detail" @click.stop="openOrderDetail(order)">
              查看详情
            </button>
            <button
              v-if="canRequestAfterSale(order)"
              class="order-action-btn support"
              :disabled="hasOpenAfterSale(order)"
              @click.stop="handleAfterSaleRequest(order)"
            >
              申请售后
            </button>
            <button v-if="order.status === 'PENDING'" class="order-action-btn pay" @click.stop="handleContinuePay(order)">
              继续支付
            </button>
            <button v-if="order.status === 'PENDING'" class="order-action-btn cancel" @click.stop="handleCancelOrder(order)">
              取消订单
            </button>
            <button v-if="order.status !== 'PENDING'" class="order-action-btn delete" @click.stop="handleDeleteOrder(order)">
              删除
            </button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-illustration">
          <svg viewBox="0 0 120 120" width="100" height="100" fill="none">
            <rect x="25" y="20" width="70" height="85" rx="12" stroke="#d9d9d9" stroke-width="1.8"/>
            <line x1="38" y1="38" x2="82" y2="38" stroke="#e8e8e8" stroke-width="1.8" stroke-linecap="round"/>
            <line x1="38" y1="51" x2="70" y2="51" stroke="#e8e8e8" stroke-width="1.8" stroke-linecap="round"/>
            <line x1="38" y1="64" x2="78" y2="64" stroke="#e8e8e8" stroke-width="1.8" stroke-linecap="round"/>
            <line x1="38" y1="77" x2="60" y2="77" stroke="#e8e8e8" stroke-width="1.8" stroke-linecap="round"/>
            <circle cx="85" cy="85" r="18" stroke="#d9d9d9" stroke-width="1.8"/>
            <path d="M79 85h12M85 79v12" stroke="#bbb" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">暂无订单记录</p>
        <p class="empty-desc">完成支付后，订单会出现在这里</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyOrders, continuePay, cancelOrder, deleteOrder } from '@/api/payment'
import { getMyFeedbacks, submitAfterSale, submitFeedback } from '@/api/feedback'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const statusFilter = ref('')
const typeFilter = ref('')
const afterSaleMap = ref({})

const OPEN_AFTER_SALE_STATUSES = ['PENDING', 'PROCESSING']
const afterSaleStatusLabelMap = {
  PENDING: '待审核',
  PROCESSING: '处理中',
  RESOLVED: '已处理',
  CLOSED: '已关闭'
}
const afterSaleResolutionLabelMap = {
  REFUND_EXECUTED: '已执行退款',
  REFUND_REJECTED: '未通过退款审核',
  INTERVENTION_CLOSED: '售后已关闭'
}
const afterSaleActionLabelMap = {
  PLATFORM_INTERVENTION: '平台介入',
  REFUND_REVIEW: '退款审核'
}

const orderStats = computed(() => {
  const all = orders.value
  return {
    total: total.value,
    totalAmount: all.reduce((sum, o) => o.status === 'PAID' ? sum + (o.amount || 0) : sum, 0).toFixed(2),
    pendingCount: all.filter(o => o.status === 'PENDING').length
  }
})

const statusLabel = (s) => {
  const map = { PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款', CLOSED: '已关闭' }
  return map[s] || s
}

const typeLabel = (t) => {
  const map = { DEPOSIT: '定金', FINAL_PAYMENT: '尾款', MEMBERSHIP: '会员充值' }
  return map[t] || t
}

const statusClass = (s) => {
  const map = { PENDING: 'warning', PAID: 'success', REFUNDED: 'refund', CLOSED: 'closed' }
  return map[s] || ''
}

const typeClass = (t) => {
  const map = { MEMBERSHIP: 'membership', DEPOSIT: 'deposit', FINAL_PAYMENT: 'final' }
  return map[t] || ''
}

const canRequestAfterSale = (order) => {
  return !!order?.commissionId && order.status === 'PAID'
}

const getOrderAfterSale = (order) => {
  return afterSaleMap.value[`payment:${order.id}`] || null
}

const hasOpenAfterSale = (order) => {
  const record = getOrderAfterSale(order)
  return !!record && OPEN_AFTER_SALE_STATUSES.includes(record.status)
}

const getAfterSaleButtonText = (order) => {
  if (hasOpenAfterSale(order)) {
    return '售后处理中'
  }
  return order.paymentType === 'DEPOSIT' ? '申请平台介入' : '申请售后'
}

const getAfterSaleSummary = (order) => {
  const record = getOrderAfterSale(order)
  if (!record) {
    return ''
  }
  const actionLabel = afterSaleActionLabelMap[record.requestedAction] || '售后申请'
  const resolutionLabel = afterSaleResolutionLabelMap[record.resolution]
  const statusLabel = afterSaleStatusLabelMap[record.status] || record.status
  return resolutionLabel ? `${actionLabel}：${resolutionLabel}` : `${actionLabel}：${statusLabel}`
}

const setStatusFilter = (val) => {
  statusFilter.value = val
  handleFilter()
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000 && d.getDate() === now.getDate()) {
    return '今天 ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
  }
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (d.getDate() === yesterday.getDate() && d.getMonth() === yesterday.getMonth()) {
    return '昨天 ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
  }
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadAfterSales = async () => {
  const paidOrderIds = orders.value
    .filter((item) => item.status === 'PAID' && item.commissionId)
    .map((item) => item.id)

  if (!paidOrderIds.length) {
    afterSaleMap.value = {}
    return
  }

  try {
    const res = await getMyFeedbacks({ page: 0, size: 100, type: 'AFTER_SALE' })
    const records = res.code === 200 ? (res.data?.records || []) : []
    const nextMap = {}

    for (const item of records) {
      if (!item?.paymentId || !paidOrderIds.includes(item.paymentId)) {
        continue
      }
      const key = `payment:${item.paymentId}`
      if (!nextMap[key]) {
        nextMap[key] = item
      }
    }

    afterSaleMap.value = nextMap
  } catch {
    afterSaleMap.value = {}
  }
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (statusFilter.value) params.status = statusFilter.value
    if (typeFilter.value) params.paymentType = typeFilter.value
    const res = await getMyOrders(params)
    if (res.code === 200 && res.data) {
      orders.value = res.data.records || []
      total.value = res.data.total || 0
    }
    await loadAfterSales()
  } catch (e) {
    console.error('加载订单失败', e)
    orders.value = []
    total.value = 0
    afterSaleMap.value = {}
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  currentPage.value = 1
  loadOrders()
}

const openOrderDetail = (order) => {
  if (!order?.commissionId) return
  router.push({ name: 'CommissionDetail', params: { id: order.commissionId } })
}

const buildAfterSaleNotice = (order) => {
  if (order.paymentType === 'DEPOSIT') {
    return [
      '定金默认不退。',
      '只有在画师失联、拒不创作、长期拖延或其它明显画师违约情形下，平台才会审核是否退还定金。',
      '提交后不会自动退款，而是进入管理端人工审核。'
    ].join('\n')
  }

  return [
    '尾款支付通常代表你已确认收货。',
    '用户端不会直接退尾款；如存在抄袭、欺诈、严重不符或其它重大违约，可提交售后申请。',
    '提交后由平台人工审核，再决定是否退款。'
  ].join('\n')
}

const handleAfterSale = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `${buildAfterSaleNotice(order)}\n\n请填写详细原因：`,
      order.paymentType === 'DEPOSIT' ? '申请平台介入' : '申请售后/退款审核',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请尽量写清楚问题经过、证据线索、你的诉求',
        inputValidator: (val) => val && val.trim().length >= 10 ? true : '请至少填写 10 个字的申请说明'
      }
    )

    const content = [
      `约稿ID：${order.commissionId}`,
      `订单号：${order.orderNo}`,
      `支付类型：${typeLabel(order.paymentType)}`,
      `支付金额：¥${order.amount}`,
      `订单标题：${order.subject || '—'}`,
      `申请原因：${value.trim()}`
    ].join('\n')

    const res = await submitFeedback({
      type: 'COMPLAINT',
      title: `约稿售后申请 #${order.commissionId} - ${typeLabel(order.paymentType)}`,
      content,
      contactInfo: userStore.user?.email || userStore.user?.username || ''
    })

    if (res.code === 200) {
      ElMessage.success('售后申请已提交，管理员审核后会处理')
    } else {
      ElMessage.error(res.message || '提交售后申请失败')
    }
  } catch {
    // user cancelled
  }
}

const buildAfterSaleConfig = (order) => {
  if (order.paymentType === 'DEPOSIT') {
    return {
      requestedAction: 'PLATFORM_INTERVENTION',
      dialogTitle: '申请平台介入',
      title: `约稿平台介入申请 #${order.commissionId}`,
      notice: [
        '定金阶段默认不支持用户直接退款。',
        '如画师存在失联、长期不推进、拒绝创作或明显违约等情况，可提交平台介入申请。',
        '提交后会由管理员人工审核，并决定是否取消约稿及处理退款。'
      ].join('\n')
    }
  }

  return {
    requestedAction: 'REFUND_REVIEW',
    dialogTitle: '申请售后/申诉',
    title: `约稿售后申请 #${order.commissionId} - 尾款审核`,
    notice: [
      '尾款支付通常代表你已确认交付结果。',
      '如存在抄袭、欺诈、严重不符或其他重大违约情形，可提交售后申诉。',
      '平台不会自动退款，需由管理员审核后处理。'
    ].join('\n')
  }
}

const handleAfterSaleRequest = async (order) => {
  if (hasOpenAfterSale(order)) {
    ElMessage.info('该订单已有处理中售后，请等待管理员审核')
    return
  }

  const config = buildAfterSaleConfig(order)

  try {
    const { value } = await ElMessageBox.prompt(
      `${config.notice}\n\n请尽量详细说明问题经过、证据线索和你的诉求：`,
      config.dialogTitle,
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '至少填写 10 个字，便于管理员判断事实经过',
        inputValidator: (input) => input && input.trim().length >= 10 ? true : '请至少填写 10 个字的申请说明'
      }
    )

    const res = await submitAfterSale({
      commissionId: order.commissionId,
      paymentId: order.id,
      requestedAction: config.requestedAction,
      title: config.title,
      content: value.trim(),
      contactInfo: userStore.user?.email || userStore.user?.phone || userStore.user?.username || ''
    })

    if (res.code === 200) {
      ElMessage.success('售后申请已提交，平台审核后会通过站内通知反馈结果')
      await loadAfterSales()
    } else {
      ElMessage.error(res.message || '提交售后申请失败')
    }
  } catch {
    // user cancelled
  }
}

const handleContinuePay = async (order) => {
  try {
    const res = await continuePay(order.orderNo)
    if (res.code === 200 && res.data) {
      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        ElMessage.error('支付表单解析失败')
        document.body.removeChild(div)
      }
    } else {
      ElMessage.error(res.message || '发起支付失败')
    }
  } catch {
    ElMessage.error('发起支付失败')
  }
}

const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？取消后订单将关闭。', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    const res = await cancelOrder(order.orderNo)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    // user cancelled
  }
}

const handleDeleteOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要删除此订单记录吗？删除后不可恢复。', '删除订单', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteOrder(order.orderNo)
    if (res.code === 200) {
      ElMessage.success('订单已删除')
      loadOrders()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // user cancelled
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-page {
  min-height: calc(100vh - 56px);
  background: #f8f9fc;
}

/* ===== Hero ===== */
.order-hero {
  position: relative;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #3a7bd5 100%);
  padding: 40px 32px 56px;
  overflow: hidden;
}

.hero-bg-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.orb-a {
  width: 180px;
  height: 180px;
  background: #fff;
  top: -40px;
  right: -20px;
}

.orb-b {
  width: 100px;
  height: 100px;
  background: #60a5fa;
  bottom: -20px;
  left: 15%;
}

.hero-content {
  max-width: 720px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255,255,255,0.6);
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0 0 6px;
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
}

.hero-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.75);
  margin: 0 0 20px;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  padding: 14px 24px;
  width: fit-content;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
}

.stat-label {
  font-size: 11px;
  color: rgba(255,255,255,0.7);
}

.hero-stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.2);
}

/* ===== 主区域 ===== */
.order-main {
  max-width: 720px;
  margin: -24px auto 0;
  padding: 0 20px 40px;
  position: relative;
  z-index: 2;
}

/* 筛选区 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 10px 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.filter-tabs {
  display: flex;
  gap: 4px;
  background: #f5f6f8;
  border-radius: 999px;
  padding: 3px;
}
.tab-btn {
  padding: 7px 16px;
  border: none;
  background: transparent;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.tab-btn:hover {
  color: #333;
}
.tab-btn.active {
  background: #fff;
  color: #1a1a2e;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.type-select {
  width: 130px;
  flex-shrink: 0;
}

/* 订单列表 */
.orders-list {
  min-height: 200px;
}

.order-item {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 16px;
  margin-bottom: 12px;
  transition: all 0.25s;
  border: 1.5px solid #f0f0f3;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.order-item.clickable {
  cursor: pointer;
}
.order-item:hover {
  border-color: #e0e0ee;
  box-shadow: 0 6px 20px rgba(0,0,0,0.07);
  transform: translateY(-2px);
}

/* 左侧类型图标 */
.order-type-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.order-type-icon.membership {
  background: linear-gradient(135deg, #e8f0fe, #d4e4fd);
  color: #0096FA;
}
.order-type-icon.deposit {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #f0a030;
}
.order-type-icon.final {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #52c41a;
}

/* 中间详情 */
.order-detail {
  flex: 1;
  min-width: 0;
}
.order-main-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.order-subject {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 16px;
}
.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.5px;
}
.order-amount.refunded {
  color: #52c41a;
  text-decoration: line-through;
  opacity: 0.7;
}

.order-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.order-type-badge {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 999px;
  font-weight: 500;
}
.order-type-badge.membership {
  background: #e8f0fe;
  color: #0096FA;
}
.order-type-badge.deposit {
  background: #fff3e0;
  color: #e68a00;
}
.order-type-badge.final {
  background: #e8f5e9;
  color: #389e0d;
}

.order-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.order-status-dot.warning { background: #faad14; }
.order-status-dot.success { background: #52c41a; }
.order-status-dot.refund { background: #1890ff; }
.order-status-dot.closed { background: #d9d9d9; }

.order-status-text {
  font-size: 12px;
  font-weight: 500;
}
.order-status-text.warning { color: #faad14; }
.order-status-text.success { color: #52c41a; }
.order-status-text.refund { color: #1890ff; }
.order-status-text.closed { color: #999; }

.meta-divider {
  color: #ddd;
  font-size: 12px;
}
.order-time {
  font-size: 12px;
  color: #bbb;
}

.order-extra {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}
.extra-tag {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 999px;
}
.extra-tag.discount {
  background: #f6ffed;
  color: #52c41a;
}
.extra-tag.fee {
  background: #fff7e6;
  color: #d4880f;
}

.order-no {
  font-size: 11px;
  color: #ccc;
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', monospace;
  user-select: all;
}

.after-sale-note {
  margin-top: 8px;
  font-size: 12px;
  color: #ea580c;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
  justify-content: center;
  flex-shrink: 0;
  margin-left: 12px;
  min-width: 80px;
}
.order-action-btn {
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  white-space: nowrap;
  transition: all 0.2s;
}
.order-action-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.order-action-btn.pay {
  background: #6366f1;
  color: #fff;
}
.order-action-btn.detail {
  background: #eef6ff;
  color: #2563eb;
  border-color: #cfe2ff;
}
.order-action-btn.detail:hover {
  background: #dbeafe;
}
.order-action-btn.support {
  background: #fff7ed;
  color: #ea580c;
  border-color: #fed7aa;
}
.order-action-btn.support:hover {
  background: #ffedd5;
}
.order-action-btn.pay:hover {
  background: #4f46e5;
}
.order-action-btn.cancel {
  background: #fff;
  color: #666;
  border-color: #ddd;
}
.order-action-btn.cancel:hover {
  color: #333;
  border-color: #bbb;
}
.order-action-btn.delete {
  background: #fff;
  color: #999;
  border-color: #eee;
}
.order-action-btn.delete:hover {
  color: #ef4444;
  border-color: #fca5a5;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 0 60px;
}
.empty-illustration {
  margin-bottom: 20px;
  opacity: 0.7;
}
.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #999;
  margin: 0 0 6px;
}
.empty-desc {
  font-size: 13px;
  color: #ccc;
  margin: 0;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式 */
@media (max-width: 640px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-tabs {
    overflow-x: auto;
  }
  .type-select {
    width: 100%;
  }
  .order-type-icon {
    display: none;
  }
}
</style>
