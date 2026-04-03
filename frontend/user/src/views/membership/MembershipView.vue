<template>
  <div class="membership-page">
    <!-- 会员信息头部 -->
    <section class="membership-header" :class="'level-' + membershipLevel.toLowerCase()">
      <div class="header-content">
        <div class="member-info">
          <div class="member-avatar">
            <el-avatar :size="72" :src="userStore.user?.avatarUrl" />
            <span v-if="membershipLevel !== 'NORMAL'" class="vip-badge" :class="membershipLevel.toLowerCase()">
              {{ membershipLevel }}
            </span>
          </div>
          <div class="member-detail">
            <h2 class="member-name">{{ userStore.user?.username }}</h2>
            <div class="member-level">
              <span class="level-tag" :class="membershipLevel.toLowerCase()">
                {{ levelName }}
              </span>
              <span v-if="membership.expireTime && membershipLevel !== 'NORMAL'" class="expire-time">
                到期时间：{{ membership.expireTime }}
              </span>
            </div>
          </div>
        </div>
        <div class="points-summary">
          <div class="points-box">
            <span class="points-number">{{ points.availablePoints || 0 }}</span>
            <span class="points-label">可用积分</span>
          </div>
          <div class="points-box">
            <span class="points-number">{{ points.totalPoints || 0 }}</span>
            <span class="points-label">累计积分</span>
          </div>
        </div>
      </div>
    </section>

    <div class="membership-body">
      <!-- 每日签到卡片 -->
      <section class="checkin-card">
        <div class="card-header">
          <h3>每日签到</h3>
          <span class="consecutive-info" v-if="checkin.consecutiveDays > 0">
            已连续签到 <strong>{{ checkin.consecutiveDays }}</strong> 天
            <span class="cycle-info">（周期第 {{ ((checkin.consecutiveDays - 1) % 7) + 1 }} 天）</span>
          </span>
        </div>

        <!-- 7天签到日历 -->
        <div class="checkin-week">
          <div
            v-for="(checked, idx) in checkin.weekCheckins || []"
            :key="idx"
            class="checkin-day"
            :class="{ 'checked': checked, 'today': idx === todayIndex }"
          >
            <span class="day-label">{{ weekLabels[idx] }}</span>
            <div class="day-circle">
              <svg v-if="checked" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <span v-else-if="idx >= todayIndex" class="day-points">+{{ getDayPoints(idx) }}</span>
              <span v-else class="day-missed">-</span>
            </div>
          </div>
        </div>

        <div class="checkin-action">
          <el-button
            v-if="!checkin.checkedInToday"
            type="primary"
            size="large"
            round
            :loading="checkinLoading"
            @click="handleCheckin"
            class="checkin-btn"
          >
            立即签到
          </el-button>
          <div v-else class="checkin-done">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#52c41a" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><polyline points="9 12 11.5 14.5 16 9.5"/>
            </svg>
            <span>今日已签到，获得 <strong>{{ checkin.pointsEarned }}</strong> 积分</span>
          </div>
          <p class="multiplier-tip" v-if="checkin.multiplier > 1">
            🎉 {{ membershipLevel }} 会员享 {{ checkin.multiplier }}x 积分加成！
          </p>
        </div>
      </section>

      <!-- VIP特权展示 -->
      <section class="benefits-card">
        <h3 class="card-title">会员特权</h3>
        <div class="benefits-grid">
          <div class="benefit-item">
            <div class="benefit-icon">🏅</div>
            <h4>VIP 专属徽章</h4>
            <p>个人主页和评论区展示专属 VIP 标识</p>
            <span class="benefit-tag vip">VIP</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">✨</div>
            <h4>签到积分加倍</h4>
            <p>VIP 双倍积分 / SVIP 三倍积分</p>
            <span class="benefit-tag vip">VIP 2x / SVIP 3x</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">💰</div>
            <h4>约稿手续费减免</h4>
            <p>VIP 享 95 折 / SVIP 享 9 折手续费优惠</p>
            <span class="benefit-tag svip">SVIP</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">🎫</div>
            <h4>每月赠券</h4>
            <p>VIP 每月赠 5 元券 / SVIP 每月赠 15 元券</p>
            <span class="benefit-tag vip">VIP</span>
          </div>

          <div class="benefit-item">
            <div class="benefit-icon">💬</div>
            <h4>评论高亮</h4>
            <p>VIP/SVIP 用户评论带有醒目高亮展示</p>
            <span class="benefit-tag vip">VIP</span>
          </div>
          <div class="benefit-item">
            <div class="benefit-icon">🖼️</div>
            <h4>查看无水印原图</h4>
            <p>VIP/SVIP 用户可查看画师作品的高清无水印原图</p>
            <span class="benefit-tag vip">VIP</span>
          </div>
        </div>
      </section>

      <!-- 开通/升级会员 -->
      <section class="subscribe-card">
        <h3 class="card-title">开通会员</h3>
        <div class="plan-tabs">
          <div
            class="plan-tab"
            :class="{ active: selectedPlan === 'VIP' }"
            @click="selectedPlan = 'VIP'"
          >
            <span class="plan-tab-badge vip">VIP</span>
            <span>会员</span>
          </div>
          <div
            class="plan-tab"
            :class="{ active: selectedPlan === 'SVIP' }"
            @click="selectedPlan = 'SVIP'"
          >
            <span class="plan-tab-badge svip">SVIP</span>
            <span>超级会员</span>
          </div>
        </div>

        <div class="duration-options">
          <div
            v-for="opt in durationOptions"
            :key="opt.days"
            class="duration-card"
            :class="{ selected: selectedDuration === opt.days, best: opt.best }"
            @click="selectedDuration = opt.days"
          >
            <div v-if="opt.best" class="best-tag">推荐</div>
            <div class="duration-period">{{ opt.label }}</div>
            <div class="duration-price">
              <span class="price-symbol">¥</span>
              <span class="price-amount">{{ selectedPlan === 'VIP' ? opt.vipPrice : opt.svipPrice }}</span>
            </div>
            <div class="duration-avg" v-if="opt.days >= 90">
              ≈ ¥{{ ((selectedPlan === 'VIP' ? opt.vipPrice : opt.svipPrice) / (opt.days / 30)).toFixed(1) }}/月
            </div>
          </div>
        </div>

        <div class="subscribe-action">
          <div class="subscribe-summary">
            <span>
              开通 <strong>{{ selectedPlan }}</strong> ·
              {{ durationOptions.find(o => o.days === selectedDuration)?.label }}
            </span>
            <span class="subscribe-total">
              ¥{{ selectedPlan === 'VIP'
                ? durationOptions.find(o => o.days === selectedDuration)?.vipPrice
                : durationOptions.find(o => o.days === selectedDuration)?.svipPrice }}
            </span>
          </div>
          <el-button
            type="warning"
            size="large"
            round
            :loading="subscribeLoading"
            @click="handleSubscribe"
            class="subscribe-btn"
          >
            {{ membershipLevel === 'NORMAL' ? '立即开通' : '续费 / 升级' }}
          </el-button>
        </div>
      </section>

      <!-- 积分商城 -->
      <section class="shop-card">
        <h3 class="card-title">积分商城</h3>
        <p class="shop-desc">使用签到积分兑换约稿优惠券，让您的创作之旅更划算！</p>
        <div class="shop-grid">
          <div v-for="item in shopItems" :key="item.id" class="shop-item">
            <div class="shop-icon">{{ item.icon }}</div>
            <h4>{{ item.name }}</h4>
            <p>{{ item.description }}</p>
            <div class="shop-cost">
              <span class="cost-points">{{ item.pointsCost }}</span>
              <span class="cost-label">积分</span>
            </div>
            <el-button
              type="primary"
              size="small"
              round
              :disabled="(points.availablePoints || 0) < item.pointsCost"
              :loading="exchangeLoading"
              @click="handleExchange(item)"
            >
              {{ (points.availablePoints || 0) >= item.pointsCost ? '立即兑换' : '积分不足' }}
            </el-button>
          </div>
        </div>
      </section>

      <!-- 积分记录 -->
      <section class="records-card">
        <h3 class="card-title">积分记录</h3>
        <div v-if="recordsLoading" class="records-loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="records.length > 0" class="records-list">
          <div v-for="record in records" :key="record.id" class="record-item">
            <div class="record-info">
              <span class="record-type" :class="record.points > 0 ? 'earn' : 'spend'">
                {{ getTypeLabel(record.type) }}
              </span>
              <span class="record-desc">{{ record.description }}</span>
            </div>
            <div class="record-right">
              <span class="record-points" :class="record.points > 0 ? 'positive' : 'negative'">
                {{ record.points > 0 ? '+' : '' }}{{ record.points }}
              </span>
              <span class="record-time">{{ formatTime(record.createdAt) }}</span>
            </div>
          </div>
          <div class="records-pagination" v-if="totalRecords > pageSize">
            <el-pagination
              small
              layout="prev, pager, next"
              :total="totalRecords"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="loadRecords"
            />
          </div>
        </div>
        <div v-else class="records-empty">
          <p>暂无积分记录</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyMembership, getCheckinStatus, doCheckin, getPoints, getPointRecords, createMembershipPayment, getPointsShop, exchangePoints } from '@/api/membership'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const membership = ref({})
const checkin = ref({})
const points = ref({})
const records = ref([])
const checkinLoading = ref(false)
const recordsLoading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const totalRecords = ref(0)

// 开通会员
const selectedPlan = ref('VIP')
const selectedDuration = ref(30)
const subscribeLoading = ref(false)

// 积分商城
const shopItems = ref([])
const exchangeLoading = ref(false)

const durationOptions = [
  { days: 30, label: '1个月', vipPrice: 15, svipPrice: 30, best: false },
  { days: 90, label: '3个月', vipPrice: 40, svipPrice: 80, best: false },
  { days: 180, label: '半年', vipPrice: 68, svipPrice: 138, best: true },
  { days: 365, label: '1年', vipPrice: 118, svipPrice: 238, best: false }
]

const weekLabels = ['一', '二', '三', '四', '五', '六', '日']

const membershipLevel = computed(() => membership.value.level || 'NORMAL')

const levelName = computed(() => {
  const map = { NORMAL: '普通用户', VIP: 'VIP 会员', SVIP: '超级 VIP' }
  return map[membershipLevel.value] || '普通用户'
})

const todayIndex = computed(() => {
  const d = new Date().getDay()
  return d === 0 ? 6 : d - 1  // 0=Sunday → index 6
})

function getDayPoints(idx) {
  const base = [10, 20, 30, 40, 50, 60, 100]
  const today = todayIndex.value
  const consecutive = checkin.value.consecutiveDays || 0
  const checked = checkin.value.checkedInToday
  const multiplier = checkin.value.multiplier || 1

  // 只对今天及未来的未签到天调用此函数
  const daysFromToday = idx - today

  // 计算该天对应的连续签到天数
  let futureConsecutive
  if (checked) {
    // 今天已签到，consecutiveDays 包含今天
    futureConsecutive = consecutive + daysFromToday
  } else {
    // 今天还没签到
    futureConsecutive = consecutive + 1 + daysFromToday
  }

  const cycleIdx = ((futureConsecutive - 1) % 7)
  return base[cycleIdx] * multiplier
}

function getTypeLabel(type) {
  const map = { CHECKIN: '签到', EXCHANGE: '兑换', VIP_REWARD: 'VIP奖励', ADMIN_GRANT: '管理员发放' }
  return map[type] || type
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadMembership() {
  try {
    const res = await getMyMembership()
    if (res.code === 200) membership.value = res.data || {}
  } catch (e) { console.error(e); ElMessage.error('加载会员信息失败') }
}

async function loadCheckinStatus() {
  try {
    const res = await getCheckinStatus()
    if (res.code === 200) checkin.value = res.data || {}
  } catch (e) { console.error(e); ElMessage.error('加载签到状态失败') }
}

async function loadPoints() {
  try {
    const res = await getPoints()
    if (res.code === 200) points.value = res.data || {}
  } catch (e) { console.error(e); ElMessage.error('加载积分信息失败') }
}

async function loadRecords(page) {
  if (page) currentPage.value = page
  recordsLoading.value = true
  try {
    const res = await getPointRecords(currentPage.value, pageSize)
    if (res.code === 200 && res.data) {
      records.value = res.data.content || res.data.records || []
      totalRecords.value = res.data.totalElements || res.data.total || 0
    }
  } catch (e) { console.error(e); ElMessage.error('加载积分记录失败') }
  finally { recordsLoading.value = false }
}

async function handleCheckin() {
  checkinLoading.value = true
  try {
    const res = await doCheckin()
    if (res.code === 200) {
      checkin.value = res.data
      ElMessage.success(`签到成功！获得 ${res.data.pointsEarned} 积分`)
      loadPoints()
      loadRecords(1)
    } else {
      ElMessage.warning(res.message || '签到失败')
    }
  } catch (e) {
    ElMessage.error('签到失败，请稍后再试')
  } finally {
    checkinLoading.value = false
  }
}

async function handleSubscribe() {
  const opt = durationOptions.find(o => o.days === selectedDuration.value)
  const price = selectedPlan.value === 'VIP' ? opt.vipPrice : opt.svipPrice
  try {
    await ElMessageBox.confirm(
      `确认开通 ${selectedPlan.value} ${opt.label}？\n需支付 ¥${price}`,
      '确认开通',
      { confirmButtonText: '前往支付', cancelButtonText: '取消', type: 'info' }
    )
  } catch { return }

  subscribeLoading.value = true
  try {
    const res = await createMembershipPayment({
      level: selectedPlan.value,
      durationDays: selectedDuration.value
    })
    if (res.code === 200 && res.data) {
      // res.data 是支付宝表单 HTML，需要提交到支付宝
      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        ElMessage.error('支付表单生成失败')
      }
    } else {
      ElMessage.error(res.message || '创建支付失败')
    }
  } catch (e) {
    ElMessage.error('创建支付失败，请稍后再试')
  } finally {
    subscribeLoading.value = false
  }
}

async function loadShopItems() {
  try {
    const res = await getPointsShop()
    if (res.code === 200) shopItems.value = res.data || []
  } catch (e) { console.error(e); ElMessage.error('加载商城失败') }
}

async function handleExchange(item) {
  try {
    await ElMessageBox.confirm(
      `确认使用 ${item.pointsCost} 积分兑换「${item.name}」？`,
      '积分兑换',
      { confirmButtonText: '确认兑换', cancelButtonText: '取消', type: 'info' }
    )
  } catch { return }

  exchangeLoading.value = true
  try {
    const res = await exchangePoints(item.id)
    if (res.code === 200 && res.data) {
      ElMessage.success(res.data.message || '兑换成功')
      loadPoints()
      loadRecords(1)
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '兑换失败')
  } finally {
    exchangeLoading.value = false
  }
}

onMounted(() => {
  loadMembership()
  loadCheckinStatus()
  loadPoints()
  loadRecords()
  loadShopItems()
})
</script>

<style scoped>
.membership-page {
  min-height: 100vh;
  background: #fff;
}

/* ====== 头部 ====== */
.membership-header {
  background: linear-gradient(135deg, #EFF6FF, #ECFDF5, #F5F3FF);
  padding: 40px 0;
  color: #1a1a1a;
}
.membership-header.level-vip {
  background: linear-gradient(135deg, #EFF6FF, #ECFDF5, #F5F3FF);
}
.membership-header.level-svip {
  background: linear-gradient(135deg, #EFF6FF, #ECFDF5, #F5F3FF);
}

.header-content {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.member-avatar {
  position: relative;
}

.vip-badge {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  font-weight: 800;
  padding: 1px 8px;
  border-radius: 999px;
  letter-spacing: 1px;
}
.vip-badge.vip { background: #f59e0b; color: #fff; }
.vip-badge.svip { background: #8b5cf6; color: #fff; }

.member-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.member-level { display: flex; align-items: center; gap: 12px; }

.level-tag {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 999px;
  background: rgba(0,0,0,0.06);
  color: #555;
}
.level-tag.vip { background: #fef3c7; color: #b45309; }
.level-tag.svip { background: #ede9fe; color: #7c3aed; }

.expire-time {
  font-size: 13px;
  color: #666;
}

.points-summary {
  display: flex;
  gap: 32px;
}

.points-box {
  text-align: center;
}

.points-number {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
}

.points-label {
  font-size: 12px;
  color: #666;
}

/* ====== 主体 ====== */
.membership-body {
  max-width: 960px;
  margin: 24px auto 40px;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ====== 签到卡片 ====== */
.checkin-card, .benefits-card, .records-card, .shop-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
}

.consecutive-info {
  font-size: 13px;
  color: #999;
}
.consecutive-info strong {
  color: #f59e0b;
  font-size: 16px;
}

/* 签到周历 */
.checkin-week {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 24px;
}

.checkin-day {
  flex: 1;
  text-align: center;
}

.day-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.day-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: #f9fafb;
  color: #bbb;
  font-size: 11px;
  transition: all 0.3s;
}

.checkin-day.checked .day-circle {
  background: #ecfdf5;
  color: #10b981;
}

.checkin-day.today .day-circle {
  border: 2px solid #6366f1;
}
.checkin-day.today.checked .day-circle {
  border-color: #10b981;
}

.day-points {
  font-weight: 600;
}

.day-missed {
  color: #ddd;
  font-size: 14px;
}

.cycle-info {
  font-size: 12px;
  color: #bbb;
  margin-left: 4px;
}

.checkin-action {
  text-align: center;
}

.checkin-btn {
  min-width: 180px;
  font-size: 16px;
  font-weight: 600;
  height: 44px;
  border-radius: 999px;
}

.checkin-done {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #10b981;
  font-size: 15px;
}
.checkin-done strong { color: #f59e0b; }

.multiplier-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #f59e0b;
}

/* ====== 特权展示 ====== */
.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.benefit-item {
  padding: 20px;
  border-radius: 20px;
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  text-align: center;
  transition: all 0.3s;
}
.benefit-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.04);
}

.benefit-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.benefit-item h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.benefit-item p {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 10px;
}

.benefit-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}
.benefit-tag.vip { background: #fef3c7; color: #b45309; }
.benefit-tag.svip { background: #ede9fe; color: #7c3aed; }

/* ====== 积分商城 ====== */
.shop-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.shop-item {
  padding: 20px 16px;
  border-radius: 20px;
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  text-align: center;
  transition: all 0.3s;
}
.shop-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.04);
  border-color: #c7d2fe;
}

.shop-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.shop-item h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.shop-item p {
  font-size: 11px;
  color: #999;
  margin-bottom: 12px;
  line-height: 1.4;
}

.shop-cost {
  margin-bottom: 12px;
}

.cost-points {
  font-size: 22px;
  font-weight: 800;
  color: #f59e0b;
}

.cost-label {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

/* ====== 开通会员卡片 ====== */
.subscribe-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}

.plan-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.plan-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border-radius: 20px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s;
}
.plan-tab:hover { border-color: #f59e0b; }
.plan-tab.active { border-color: #f59e0b; background: #fffbeb; color: #b45309; }

.plan-tab-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 999px;
  letter-spacing: 1px;
}
.plan-tab-badge.vip { background: #fef3c7; color: #b45309; }
.plan-tab-badge.svip { background: #ede9fe; color: #7c3aed; }

.duration-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.duration-card {
  position: relative;
  padding: 20px 12px;
  border-radius: 20px;
  border: 2px solid #f3f4f6;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f9fafb;
}
.duration-card:hover { border-color: #f59e0b; }
.duration-card.selected { border-color: #f59e0b; background: #fffbeb; }

.best-tag {
  position: absolute;
  top: -10px;
  right: -6px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}

.duration-period {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.price-symbol {
  font-size: 14px;
  font-weight: 600;
  color: #f59e0b;
}
.price-amount {
  font-size: 28px;
  font-weight: 800;
  color: #f59e0b;
}

.duration-avg {
  margin-top: 6px;
  font-size: 11px;
  color: #999;
}

.subscribe-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fffbeb;
  border-radius: 20px;
}

.subscribe-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.subscribe-total {
  font-size: 24px;
  font-weight: 800;
  color: #f59e0b;
}

.subscribe-btn {
  min-width: 160px;
  font-size: 16px;
  font-weight: 700;
  height: 48px;
  border-radius: 999px;
}

/* ====== 积分记录 ====== */
.records-list {
  display: flex;
  flex-direction: column;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.record-item:last-child { border-bottom: none; }

.record-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-type {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}
.record-type.earn { background: #ecfdf5; color: #059669; }
.record-type.spend { background: #fef3c7; color: #d97706; }

.record-desc {
  font-size: 14px;
  color: #333;
}

.record-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.record-points {
  font-size: 16px;
  font-weight: 700;
}
.record-points.positive { color: #10b981; }
.record-points.negative { color: #ef4444; }

.record-time {
  font-size: 12px;
  color: #bbb;
  min-width: 80px;
  text-align: right;
}

.records-pagination {
  margin-top: 16px;
  text-align: center;
}

.records-empty {
  text-align: center;
  padding: 40px 0;
  color: #ccc;
  font-size: 14px;
}

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  .member-info { flex-direction: column; }
  .benefits-grid { grid-template-columns: repeat(2, 1fr); }
  .duration-options { grid-template-columns: repeat(2, 1fr); }
  .shop-grid { grid-template-columns: repeat(2, 1fr); }
  .checkin-week { gap: 4px; }
  .day-circle { width: 36px; height: 36px; }
}

@media (max-width: 480px) {
  .benefits-grid { grid-template-columns: 1fr; }
  .points-summary { gap: 20px; }
}
</style>
