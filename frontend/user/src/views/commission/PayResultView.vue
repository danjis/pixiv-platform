<template>
  <div class="pay-result-page">
    <div class="result-card" v-if="!loading">
      <!-- 成功 -->
      <template v-if="payResult === 'success'">
        <div class="result-icon success">
          <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 12.5l2.5 3L16 9"/></svg>
        </div>
        <h2>支付成功</h2>
        <p class="result-text">{{ paymentTypeLabel }}已完成支付</p>
        <div class="result-info" v-if="paymentInfo">
          <div class="info-row"><span>订单号</span><span>{{ paymentInfo.orderNo }}</span></div>
          <div class="info-row"><span>金额</span><span class="price">¥{{ paymentInfo.amount }}</span></div>
          <div class="info-row"><span>支付时间</span><span>{{ formatTime(paymentInfo.paidAt) }}</span></div>
        </div>
      </template>

      <!-- 待确认 -->
      <template v-else-if="payResult === 'pending'">
        <div class="result-icon pending">
          <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        </div>
        <h2>支付处理中</h2>
        <p class="result-text">支付结果确认中，请稍后查看约稿详情</p>
      </template>

      <!-- 失败 -->
      <template v-else>
        <div class="result-icon fail">
          <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        </div>
        <h2>支付未完成</h2>
        <p class="result-text">{{ errorMsg || '支付可能未成功，请返回重试或查看约稿详情' }}</p>
      </template>

      <div class="result-actions">
        <button v-if="commissionId" class="btn primary" @click="goDetail">查看约稿详情</button>
        <button class="btn outline" @click="$router.push('/commissions')">返回约稿列表</button>
      </div>
    </div>

    <div v-else class="loading-wrap">
      <p>查询支付结果中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPaymentStatus } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const payResult = ref('')  // 'success' | 'pending' | 'fail'
const paymentInfo = ref(null)
const commissionId = ref(null)
const errorMsg = ref('')

const paymentTypeLabel = computed(() => {
  if (!paymentInfo.value) return '款项'
  return paymentInfo.value.paymentType === 'DEPOSIT' ? '定金' : '尾款'
})

onMounted(async () => {
  // 支付宝回调会带上 out_trade_no 等参数
  const orderNo = route.query.out_trade_no
  if (!orderNo) {
    payResult.value = 'fail'
    errorMsg.value = '缺少订单信息'
    loading.value = false
    return
  }

  try {
    const res = await getPaymentStatus(orderNo)
    if (res.code === 200 && res.data) {
      paymentInfo.value = res.data
      commissionId.value = res.data.commissionId
      if (res.data.status === 'PAID') {
        payResult.value = 'success'
      } else if (res.data.status === 'PENDING') {
        payResult.value = 'pending'
      } else {
        payResult.value = 'fail'
      }
    } else {
      payResult.value = 'pending'
    }
  } catch {
    payResult.value = 'pending'
  } finally {
    loading.value = false
  }
})

function goDetail() {
  router.push(`/commission/${commissionId.value}`)
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
.result-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px 40px;
  text-align: center;
  max-width: 460px;
  width: 100%;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.result-icon { margin-bottom: 20px; }
.result-icon.success { color: #34c759; }
.result-icon.pending { color: #f5a623; }
.result-icon.fail { color: #f56c6c; }
.result-card h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}
.result-text {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

.result-info {
  background: #f9fafb;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
  text-align: left;
}
.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 6px 0;
  color: #666;
}
.info-row .price { font-weight: 700; color: #0096FA; }

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn {
  padding: 12px 24px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}
.btn:hover { opacity: 0.85; }
.btn.primary { background: #0096FA; color: #fff; }
.btn.outline { background: #fff; color: #666; border: 1px solid #e0e0e0; }

.loading-wrap {
  text-align: center;
  color: #999;
  font-size: 14px;
}
</style>
