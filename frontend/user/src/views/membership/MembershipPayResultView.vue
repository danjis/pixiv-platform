<template>
  <div class="pay-result-page">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-spinner"></div>
      <p>正在确认支付结果...</p>
    </div>

    <div class="result-card" v-else>
      <!-- 成功 -->
      <template v-if="payResult === 'success'">
        <div class="result-icon success">
          <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <path d="M8 12l3 3 5-5" />
          </svg>
        </div>
        <h2>会员开通成功</h2>
        <p class="result-desc">恭喜！你已成功开通会员，所有特权现已生效</p>
        <div class="result-info" v-if="paymentInfo">
          <div class="info-item">
            <span class="info-label">会员类型</span>
            <span class="info-value highlight">{{ parseMemberLevel(paymentInfo.subject) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">开通时长</span>
            <span class="info-value">{{ parseDuration(paymentInfo.subject) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付金额</span>
            <span class="info-value price">¥{{ paymentInfo.amount }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value mono">{{ paymentInfo.orderNo }}</span>
          </div>
          <div class="info-item" v-if="paymentInfo.paidAt">
            <span class="info-label">支付时间</span>
            <span class="info-value">{{ formatTime(paymentInfo.paidAt) }}</span>
          </div>
        </div>
      </template>

      <!-- 失败 -->
      <template v-else-if="payResult === 'fail'">
        <div class="result-icon fail">
          <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <path d="M15 9l-6 6M9 9l6 6" />
          </svg>
        </div>
        <h2>支付未完成</h2>
        <p class="result-desc">{{ errorMsg || '支付可能未成功，请返回会员中心重试' }}</p>
      </template>

      <div class="result-actions">
        <button class="btn primary" @click="$router.push('/membership')">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L4 6v5c0 5.25 3.4 10.2 8 11.4 4.6-1.2 8-6.15 8-11.4V6l-8-4z"/></svg>
          返回会员中心
        </button>
        <button class="btn outline" @click="$router.push('/')">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMembershipPaymentStatus, getMyMembership } from '@/api/membership'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const payResult = ref('')
const paymentInfo = ref(null)
const errorMsg = ref('')

onMounted(async () => {
  const orderNo = route.query.out_trade_no
  if (!orderNo) {
    payResult.value = 'fail'
    errorMsg.value = '缺少订单信息'
    loading.value = false
    return
  }

  // 轮询查询支付结果，最多重试 10 次（间隔 2 秒，总计约 20 秒）
  const maxRetries = 10
  const retryInterval = 2000

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      const res = await getMembershipPaymentStatus(orderNo)
      if (res.code === 200 && res.data) {
        paymentInfo.value = res.data
        if (res.data.status === 'PAID') {
          payResult.value = 'success'
          // 支付成功后，刷新用户会员信息到本地缓存
          try {
            const memberRes = await getMyMembership()
            if (memberRes.code === 200 && memberRes.data) {
              userStore.updateUser({
                membershipLevel: memberRes.data.level,
                membershipExpireTime: memberRes.data.expireTime
              })
            }
          } catch (e) {
            console.error('刷新会员信息失败:', e)
          }
          loading.value = false
          return
        }
        // 最后一次仍非 PAID，判定为失败
        if (attempt === maxRetries) {
          payResult.value = 'fail'
          errorMsg.value = '支付未完成，请返回会员中心重试'
          loading.value = false
          return
        }
      } else {
        // 接口返回错误
        if (attempt === maxRetries) {
          payResult.value = 'fail'
          errorMsg.value = res.message || '查询支付结果失败'
          loading.value = false
          return
        }
      }
    } catch {
      if (attempt === maxRetries) {
        payResult.value = 'fail'
        errorMsg.value = '查询支付结果失败，请返回会员中心查看'
        loading.value = false
        return
      }
    }
    // 等待后重试
    await new Promise(resolve => setTimeout(resolve, retryInterval))
  }
})

function parseMemberLevel(subject) {
  if (!subject) return '会员'
  if (subject.includes('SVIP')) return 'SVIP 超级会员'
  if (subject.includes('VIP')) return 'VIP 会员'
  return '会员'
}

function parseDuration(subject) {
  if (!subject) return ''
  if (subject.includes('1年')) return '1年'
  if (subject.includes('半年')) return '半年'
  if (subject.includes('3个月')) return '3个月'
  if (subject.includes('1个月')) return '1个月'
  return ''
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped>
.pay-result-page {
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  padding: 24px;
}

.loading-wrap {
  text-align: center;
  color: #999;
}
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #0096FA;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.loading-wrap p {
  font-size: 14px;
}

.result-card {
  background: #fff;
  border-radius: 20px;
  padding: 56px 48px 40px;
  text-align: center;
  max-width: 480px;
  width: 100%;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

.result-icon { margin-bottom: 24px; }
.result-icon.success { color: #34c759; }
.result-icon.fail { color: #ff6b6b; }

.result-card h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.result-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 28px;
  line-height: 1.6;
}

.result-info {
  background: #f9fafb;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 28px;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.info-item:last-child { border-bottom: none; }

.info-label {
  font-size: 13px;
  color: #999;
}
.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.info-value.highlight {
  color: #0096FA;
  font-weight: 700;
}
.info-value.price {
  color: #f0a030;
  font-weight: 700;
  font-size: 16px;
}
.info-value.mono {
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  font-size: 12px;
  color: #999;
}

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 13px 24px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}
.btn:hover { opacity: 0.9; transform: translateY(-1px); }
.btn.primary { background: #0096FA; color: #fff; }
.btn.outline { background: #fff; color: #666; border: 1px solid #e0e0e0; }
</style>
