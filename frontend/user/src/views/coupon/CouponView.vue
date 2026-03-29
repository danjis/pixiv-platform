<template>
  <div class="coupon-page">
    <!-- 标签切换 -->
    <div class="page-header">
      <h1 class="page-title">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
          <circle cx="7" cy="7" r="1"/>
        </svg>
        优惠券
      </h1>
      <div class="code-input-area">
        <input
          v-model="redeemCode"
          class="code-input"
          placeholder="输入优惠码兑换"
          @keyup.enter="handleRedeem"
        />
        <button class="redeem-btn" @click="handleRedeem" :disabled="!redeemCode.trim()">兑换</button>
      </div>
    </div>

    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</span>
      </button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
    </div>

    <!-- 领券中心 -->
    <div v-else-if="activeTab === 'available'" class="coupon-list">
      <div v-if="availableCoupons.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
          <circle cx="7" cy="7" r="1"/>
        </svg>
        <p>暂无可领取的优惠券</p>
      </div>
      <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card" :class="{ claimed: coupon.claimed }">
        <div class="coupon-left" :class="coupon.type === 'PERCENTAGE' ? 'type-percent' : 'type-fixed'">
          <div class="discount-value">
            <template v-if="coupon.type === 'PERCENTAGE'">
              <span class="value">{{ coupon.discountValue }}</span>
              <span class="unit">%</span>
            </template>
            <template v-else>
              <span class="symbol">¥</span>
              <span class="value">{{ coupon.discountValue }}</span>
            </template>
          </div>
          <div class="discount-label">{{ coupon.type === 'PERCENTAGE' ? '折扣' : '立减' }}</div>
        </div>
        <div class="coupon-right">
          <h3 class="coupon-name">{{ coupon.name }}</h3>
          <p class="coupon-desc" v-if="coupon.description">{{ coupon.description }}</p>
          <div class="coupon-conditions">
            <span v-if="coupon.minOrderAmount > 0">满 ¥{{ coupon.minOrderAmount }} 可用</span>
            <span v-else>无门槛</span>
            <span v-if="coupon.maxDiscountAmount" class="max-discount">最高减 ¥{{ coupon.maxDiscountAmount }}</span>
          </div>
          <div class="coupon-time">
            {{ formatTime(coupon.startTime) }} ~ {{ formatTime(coupon.endTime) }}
          </div>
          <div class="coupon-stock">
            剩余 {{ coupon.totalQuantity - coupon.claimedQuantity }} / {{ coupon.totalQuantity }}
          </div>
        </div>
        <button
          class="claim-btn"
          :class="{ disabled: coupon.claimed || !coupon.claimable }"
          :disabled="coupon.claimed || !coupon.claimable"
          @click="handleClaim(coupon)"
        >
          {{ coupon.claimed ? '已领取' : !coupon.claimable ? '已领完' : '立即领取' }}
        </button>
      </div>
    </div>

    <!-- 我的优惠券 -->
    <div v-else class="coupon-list">
      <div class="filter-bar">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          class="filter-btn"
          :class="{ active: statusFilter === f.value }"
          @click="filterByStatus(f.value)"
        >{{ f.label }}</button>
      </div>

      <div v-if="myCoupons.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
          <circle cx="7" cy="7" r="1"/>
        </svg>
        <p>{{ statusFilter === 'all' ? '还没有优惠券' : '没有相关优惠券' }}</p>
        <button class="go-claim-btn" @click="switchTab('available')">去领券</button>
      </div>
      <div
        v-for="coupon in myCoupons"
        :key="coupon.id"
        class="coupon-card my-coupon"
        :class="{
          used: coupon.status === 'USED',
          expired: coupon.status === 'EXPIRED'
        }"
      >
        <div class="coupon-left" :class="coupon.type === 'PERCENTAGE' ? 'type-percent' : 'type-fixed'">
          <div class="discount-value">
            <template v-if="coupon.type === 'PERCENTAGE'">
              <span class="value">{{ coupon.discountValue }}</span>
              <span class="unit">%</span>
            </template>
            <template v-else>
              <span class="symbol">¥</span>
              <span class="value">{{ coupon.discountValue }}</span>
            </template>
          </div>
          <div class="discount-label">{{ coupon.type === 'PERCENTAGE' ? '折扣' : '立减' }}</div>
        </div>
        <div class="coupon-right">
          <h3 class="coupon-name">{{ coupon.name }}</h3>
          <p class="coupon-desc" v-if="coupon.description">{{ coupon.description }}</p>
          <div class="coupon-conditions">
            <span v-if="coupon.minOrderAmount > 0">满 ¥{{ coupon.minOrderAmount }} 可用</span>
            <span v-else>无门槛</span>
          </div>
          <div class="coupon-time">
            有效期至 {{ formatTime(coupon.endTime) }}
          </div>
        </div>
        <div class="status-badge">
          <span v-if="coupon.status === 'UNUSED'" class="badge unused">未使用</span>
          <span v-else-if="coupon.status === 'USED'" class="badge used">已使用</span>
          <span v-else class="badge expired">已过期</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, claimCoupon, claimByCode, getMyCoupons } from '@/api/coupon'

const activeTab = ref('available')
const loading = ref(true)
const redeemCode = ref('')

// 领券中心
const availableCoupons = ref([])

// 我的优惠券
const myCoupons = ref([])
const statusFilter = ref('all')
const unusedCount = ref(0)

const tabs = ref([
  { label: '领券中心', value: 'available' },
  { label: '我的优惠券', value: 'mine', count: 0 }
])

const statusFilters = [
  { label: '全部', value: 'all' },
  { label: '未使用', value: 'UNUSED' },
  { label: '已使用', value: 'USED' },
  { label: '已过期', value: 'EXPIRED' }
]

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'available') {
    loadAvailable()
  } else {
    loadMyCoupons()
  }
}

function filterByStatus(status) {
  statusFilter.value = status
  loadMyCoupons()
}

async function loadAvailable() {
  loading.value = true
  try {
    const res = await getAvailableCoupons()
    if (res.code === 200 && res.data) {
      availableCoupons.value = res.data.records || []
    }
  } catch {
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

async function loadMyCoupons() {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value !== 'all') params.status = statusFilter.value
    const res = await getMyCoupons(params)
    if (res.code === 200 && res.data) {
      myCoupons.value = res.data.records || []
      // 更新 tab 计数（未使用的数量）
      if (statusFilter.value === 'all') {
        unusedCount.value = myCoupons.value.filter(c => c.status === 'UNUSED').length
        tabs.value[1].count = unusedCount.value
      }
    }
  } catch {
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

async function handleClaim(coupon) {
  try {
    const res = await claimCoupon(coupon.id)
    if (res.code === 200) {
      ElMessage.success('领取成功！')
      coupon.claimed = true
      coupon.claimedQuantity++
      loadMyCouponCount()
    } else {
      ElMessage.error(res.message || '领取失败')
    }
  } catch {
    ElMessage.error('领取失败')
  }
}

async function handleRedeem() {
  const code = redeemCode.value.trim()
  if (!code) return
  try {
    const res = await claimByCode(code)
    if (res.code === 200) {
      ElMessage.success('兑换成功！')
      redeemCode.value = ''
      loadAvailable()
      loadMyCouponCount()
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch {
    ElMessage.error('兑换失败')
  }
}

// 预加载我的优惠券数量（不影响当前列表展示）
async function loadMyCouponCount() {
  try {
    const res = await getMyCoupons({})
    if (res.code === 200 && res.data) {
      const list = res.data.records || []
      unusedCount.value = list.filter(c => c.status === 'UNUSED').length
      tabs.value[1].count = unusedCount.value
    }
  } catch {
    // 静默失败，不影响主流程
  }
}

onMounted(() => {
  loadAvailable()
  // 预加载"我的优惠券"数量，用于 Tab 徽标显示
  loadMyCouponCount()
})
</script>

<style scoped>
.coupon-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
  background: #fff;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.code-input-area {
  display: flex;
  gap: 8px;
}

.code-input {
  width: 200px;
  padding: 9px 16px;
  border: 1.5px solid #e8e8e8;
  border-radius: 999px;
  font-size: 14px;
  outline: none;
  background: #fafafa;
  transition: border-color 0.2s, background 0.2s;
}

.code-input:focus {
  border-color: #165DFF;
  background: #fff;
}

.redeem-btn {
  padding: 9px 24px;
  background: #165DFF;
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.redeem-btn:hover:not(:disabled) {
  background: #1249d6;
}

.redeem-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* Tab 栏 - pill shaped */
.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: none;
  background: #f5f5f5;
  border-radius: 999px;
  padding: 4px;
  width: fit-content;
}

.tab-item {
  padding: 8px 24px;
  background: none;
  border: none;
  color: #888;
  font-size: 14px;
  cursor: pointer;
  border-radius: 999px;
  transition: all 0.2s;
  margin-bottom: 0;
  border-bottom: none;
  font-weight: 500;
}

.tab-item.active {
  color: #fff;
  background: #165DFF;
  font-weight: 600;
  border-bottom-color: transparent;
}

.tab-count {
  font-size: 12px;
  color: inherit;
  opacity: 0.75;
}

/* 加载中 */
.loading-container {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #f0f0f0;
  border-top: 3px solid #165DFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 72px 0;
  color: #bbb;
}

.empty-state p {
  margin-top: 16px;
  font-size: 15px;
  color: #aaa;
}

.go-claim-btn {
  margin-top: 16px;
  padding: 9px 28px;
  background: #165DFF;
  color: #fff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.go-claim-btn:hover {
  background: #1249d6;
}

/* 筛选栏 - pill shaped */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.filter-btn {
  padding: 6px 18px;
  background: #f5f5f5;
  border: none;
  border-radius: 999px;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  background: #eee;
}

.filter-btn.active {
  background: #165DFF;
  color: #fff;
}

/* 优惠券卡片 */
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  transition: box-shadow 0.3s;
  position: relative;
}

.coupon-card:hover {
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}

.coupon-card.used,
.coupon-card.expired {
  opacity: 0.55;
}

.coupon-card.claimed {
  opacity: 0.65;
}

/* 左侧金额区 */
.coupon-left {
  width: 140px;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  position: relative;
  border-radius: 20px 0 0 20px;
}

.coupon-left.type-fixed {
  background: linear-gradient(135deg, #ff7a45, #ff4d6a);
}

.coupon-left.type-percent {
  background: linear-gradient(135deg, #165DFF, #7c5cfc);
}

/* 锯齿边缘效果 */
.coupon-left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 0;
  bottom: 0;
  width: 12px;
  background: radial-gradient(circle at 6px 50%, #fff 5px, transparent 5.5px);
  background-size: 12px 20px;
}

.discount-value {
  display: flex;
  align-items: baseline;
}

.discount-value .symbol {
  font-size: 18px;
  font-weight: 500;
}

.discount-value .value {
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}

.discount-value .unit {
  font-size: 16px;
  font-weight: 600;
  margin-left: 2px;
}

.discount-label {
  font-size: 12px;
  margin-top: 6px;
  opacity: 0.85;
}

/* 右侧信息 */
.coupon-right {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.coupon-desc {
  font-size: 13px;
  color: #888;
  margin: 0;
}

.coupon-conditions {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #aaa;
}

.max-discount {
  color: #ff7a45;
}

.coupon-time {
  font-size: 12px;
  color: #ccc;
}

.coupon-stock {
  font-size: 12px;
  color: #ccc;
}

/* 领取按钮 - pill */
.claim-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px 24px;
  background: #165DFF;
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.claim-btn:hover:not(.disabled) {
  background: #1249d6;
}

.claim-btn.disabled {
  background: #d9d9d9;
  color: #fff;
  cursor: not-allowed;
}

/* 状态标志 - pill */
.status-badge {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

.badge {
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.badge.unused {
  background: #e8f3ff;
  color: #165DFF;
}

.badge.used {
  background: #f5f5f5;
  color: #aaa;
}

.badge.expired {
  background: #fff1f0;
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .coupon-page {
    padding: 20px 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .coupon-left {
    width: 100px;
  }

  .discount-value .value {
    font-size: 28px;
  }

  .claim-btn,
  .status-badge {
    position: static;
    transform: none;
    margin: 12px 16px;
  }

  .coupon-card {
    flex-wrap: wrap;
  }
}
</style>
