<template>
  <div class="commission-detail-page">
    <div class="page-container" v-if="commission">
      <!-- 返回 -->
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
        返回
      </button>

      <div class="detail-layout">
        <!-- 左侧主体 -->
        <div class="main-col">
          <!-- 标题区 -->
          <div class="card title-card">
            <div class="title-row">
              <h1>{{ commission.title }}</h1>
              <span class="status-badge" :class="'status-' + commission.status">{{ statusMap[commission.status] || commission.status }}</span>
            </div>
            <p class="create-time">创建于 {{ formatTime(commission.createdAt) }}</p>
          </div>

          <!-- 描述 -->
          <div class="card">
            <h3 class="card-title">约稿描述</h3>
            <p class="desc-text">{{ commission.description || '无描述' }}</p>
            <div v-if="commission.referenceUrls" class="ref-section">
              <h4>参考链接</h4>
              <div v-for="(url, i) in refUrlList" :key="i" class="ref-link">
                <a :href="url" target="_blank" rel="noopener">{{ url }}</a>
              </div>
            </div>
          </div>

          <!-- 报价信息（QUOTED及之后） -->
          <div v-if="hasQuote" class="card">
            <h3 class="card-title">画师报价</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">报价金额</span>
                <span class="value price">¥{{ commission.totalAmount }}</span>
              </div>
              <div class="info-item">
                <span class="label">定金比例</span>
                <span class="value">{{ Math.round((commission.depositRatio || 0.3) * 100) }}%</span>
              </div>
              <div class="info-item">
                <span class="label">定金金额</span>
                <span class="value price">¥{{ commission.depositAmount }}</span>
              </div>
              <div class="info-item" v-if="commission.revisionsAllowed">
                <span class="label">允许修改</span>
                <span class="value">{{ commission.revisionsAllowed }} 次</span>
              </div>
              <div class="info-item" v-if="commission.deadline">
                <span class="label">预计交付</span>
                <span class="value">{{ formatDate(commission.deadline) }}</span>
              </div>
            </div>
            <p v-if="commission.quoteNote" class="quote-note">
              <strong>报价说明：</strong>{{ commission.quoteNote }}
            </p>
          </div>

          <!-- 支付记录 -->
          <div v-if="payments.length" class="card">
            <h3 class="card-title">支付记录</h3>
            <div class="payment-list">
              <div v-for="p in payments" :key="p.id" class="payment-item">
                <div class="payment-main">
                  <span class="payment-type">{{ p.paymentType === 'DEPOSIT' ? '定金' : '尾款' }}</span>
                  <span class="payment-amount">¥{{ p.amount }}</span>
                  <span v-if="p.discountAmount && p.discountAmount > 0" class="payment-coupon-tag">
                    已优惠 ¥{{ p.discountAmount }}
                  </span>
                </div>
                <div class="payment-sub">
                  <span class="payment-status" :class="'ps-' + p.status">
                    <template v-if="p.status === 'PENDING' && isOrderExpired(p)">
                      已超时
                    </template>
                    <template v-else>
                      {{ paymentStatusMap[p.status] || p.status }}
                    </template>
                  </span>
                  <span v-if="p.paidAt" class="payment-time">{{ formatTime(p.paidAt) }}</span>
                  <span v-if="p.refundedAt" class="payment-time refund-time">退款于 {{ formatTime(p.refundedAt) }}</span>
                  <span v-if="p.refundReason" class="refund-reason">退款原因: {{ p.refundReason }}</span>
                  <span v-if="p.orderNo" class="payment-order">订单号: {{ p.orderNo }}</span>
                  <button
                    v-if="p.status === 'PENDING' && !isOrderExpired(p) && p.payerId === currentUserId"
                    class="continue-pay-btn"
                    @click="handleContinuePay(p.orderNo)"
                  >
                    继续支付
                  </button>
                  <span v-if="p.status === 'PENDING' && !isOrderExpired(p) && p.expireAt" class="expire-hint">
                    {{ formatCountdown(p.expireAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 取消信息 -->
          <div v-if="commission.status === 'CANCELLED' && commission.cancelledByRole" class="card cancel-info-card">
            <h3 class="card-title">取消信息</h3>
            <div class="cancel-info">
              <div class="cancel-role-badge" :class="'role-' + commission.cancelledByRole">
                {{ cancelRoleLabelMap[commission.cancelledByRole] || commission.cancelledByRole }}
              </div>
              <p v-if="commission.cancelReason" class="cancel-reason">
                <strong>取消原因：</strong>{{ commission.cancelReason }}
              </p>
              <div class="cancel-refund-hint">
                <template v-if="commission.cancelledByRole === 'ARTIST'">
                  <p class="refund-ok">✅ 画师取消，所有已支付款项（包括定金）已全额退还</p>
                </template>
                <template v-else-if="commission.cancelledByRole === 'CLIENT'">
                  <p class="refund-partial">⚠️ 委托方取消，定金不予退还，尾款（如有）已退还</p>
                </template>
                <template v-else-if="commission.cancelledByRole === 'ADMIN'">
                  <p class="refund-admin">🔧 管理员介入取消，退款由管理员处理</p>
                </template>
              </div>
            </div>
          </div>

          <!-- 消息/沟通（简化版） -->
          <div class="card">
            <h3 class="card-title">沟通记录</h3>
            <div v-if="messages.length === 0" class="empty-msg">暂无消息</div>
            <div v-else class="msg-list">
              <div v-for="m in messages" :key="m.id" class="msg-item" :class="{ mine: m.senderId === currentUserId }">
                <span class="msg-sender">{{ m.senderId === currentUserId ? '我' : '对方' }}</span>
                <span class="msg-content">{{ m.content }}</span>
                <span class="msg-time">{{ formatTime(m.createdAt) }}</span>
              </div>
            </div>
            <div class="msg-send" v-if="canSendMessage">
              <input v-model="msgText" class="msg-input" placeholder="输入消息..." @keyup.enter="sendMsg" />
              <button class="msg-btn" @click="sendMsg" :disabled="!msgText.trim()">发送</button>
            </div>
          </div>
        </div>

        <!-- 右侧操作面板 -->
        <div class="side-col">
          <!-- 对方信息 -->
          <div class="card">
            <h3 class="card-title">{{ isArtist ? '委托方' : '画师' }}</h3>
            <div class="user-row">
              <el-avatar :size="40" :src="otherUser?.avatarUrl || defaultAvatar">
                {{ (otherUser?.nickname || '?').charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ otherUser?.nickname || '用户' }}</span>
              </div>
            </div>
          </div>

          <!-- 金额概要 -->
          <div class="card">
            <h3 class="card-title">金额信息</h3>
            <div class="amount-summary">
              <div class="amount-row" v-if="commission.budget">
                <span>用户预算</span>
                <span class="amount-val">¥{{ commission.budget }}</span>
              </div>
              <div class="amount-row" v-if="commission.totalAmount">
                <span>报价总额</span>
                <span class="amount-val total">¥{{ commission.totalAmount }}</span>
              </div>
              <div class="amount-row" v-if="commission.depositAmount">
                <span>定金</span>
                <span class="amount-val">¥{{ commission.depositAmount }}</span>
              </div>
              <div class="amount-row" v-if="commission.totalAmount && commission.depositAmount">
                <span>尾款</span>
                <span class="amount-val">¥{{ (commission.totalAmount - commission.depositAmount).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card" v-if="showActions">
            <h3 class="card-title">操作</h3>
            <div class="action-list">
              <!-- 用户端：QUOTED → 接受报价并支付定金 -->
              <template v-if="!isArtist">
                <button v-if="commission.status === 'QUOTED'" class="action-btn primary" @click="payDeposit">
                  接受报价并支付定金
                </button>
                <button v-if="commission.status === 'DELIVERED'" class="action-btn primary" @click="payFinal">
                  确认收货并支付尾款
                </button>
                <button v-if="commission.status === 'DELIVERED'" class="action-btn outline" @click="doRevision">
                  请求修改
                </button>
              </template>

              <!-- 画师端 -->
              <template v-if="isArtist">
                <button v-if="commission.status === 'PENDING'" class="action-btn primary" @click="showQuoteDialog = true">
                  报价
                </button>
                <button v-if="commission.status === 'DEPOSIT_PAID'" class="action-btn primary" @click="doStart">
                  开始创作
                </button>
                <button v-if="commission.status === 'IN_PROGRESS'" class="action-btn primary" @click="showDeliverDialog = true">
                  交付作品
                </button>
              </template>

              <!-- 通用 -->
              <button v-if="canCancel" class="action-btn danger" @click="doCancel">
                取消约稿
              </button>
            </div>
          </div>

          <!-- 状态流程 -->
          <div class="card">
            <h3 class="card-title">流程</h3>
            <div class="timeline">
              <div v-for="(step, i) in timeline" :key="i" class="timeline-step" :class="{ active: step.active, done: step.done }">
                <div class="timeline-dot"></div>
                <div class="timeline-label">{{ step.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 报价弹窗 -->
      <div v-if="showQuoteDialog" class="modal-overlay" @click.self="showQuoteDialog = false">
        <div class="modal-body">
          <h3>报价</h3>
          <div class="modal-form">
            <label>报价金额 (¥)</label>
            <input v-model.number="quoteForm.totalAmount" type="number" class="form-input" />
            <label>定金比例</label>
            <input v-model.number="quoteForm.depositRatio" type="number" min="0.1" max="1" step="0.1" class="form-input" />
            <label>预计天数</label>
            <input v-model.number="quoteForm.estimatedDays" type="number" class="form-input" />
            <label>允许修改次数</label>
            <input v-model.number="quoteForm.revisionsAllowed" type="number" class="form-input" />
            <label>报价说明</label>
            <textarea v-model="quoteForm.quoteNote" rows="3" class="form-input"></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showQuoteDialog = false">取消</button>
            <button class="btn-submit" @click="doQuote">确认报价</button>
          </div>
        </div>
      </div>

      <!-- 交付弹窗 -->
      <div v-if="showDeliverDialog" class="modal-overlay" @click.self="showDeliverDialog = false">
        <div class="modal-body">
          <h3>交付作品</h3>
          <div class="modal-form">
            <label>作品链接 <span style="color:#f56c6c">*</span></label>
            <input v-model="deliverUrl" class="form-input" placeholder="输入作品下载/查看链接" />
            <label style="margin-top: 12px">交付说明</label>
            <textarea v-model="deliverNote" rows="3" class="form-input" placeholder="描述交付内容（可选）"></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showDeliverDialog = false">取消</button>
            <button class="btn-submit" @click="doDeliver">确认交付</button>
          </div>
        </div>
      </div>

      <!-- 优惠券选择弹窗 -->
      <div v-if="showCouponDialog" class="modal-overlay" @click.self="showCouponDialog = false">
        <div class="modal-body coupon-modal">
          <h3>选择优惠券</h3>
          <div v-if="couponLoading" class="coupon-loading">加载中...</div>
          <div v-else>
            <div v-if="availableCoupons.length === 0" class="empty-coupon">
              <p>暂无可用优惠券</p>
            </div>
            <div v-else class="coupon-list">
              <div
                v-for="c in availableCoupons"
                :key="c.userCouponId"
                class="coupon-item"
                :class="{ selected: selectedCouponId === c.userCouponId }"
                @click="selectedCouponId = selectedCouponId === c.userCouponId ? null : c.userCouponId"
              >
                <div class="coupon-left">
                  <span class="coupon-discount">
                    <template v-if="c.type === 'FIXED'">¥{{ c.discountValue }}</template>
                    <template v-else>{{ c.discountValue }}%</template>
                  </span>
                  <span class="coupon-type">{{ c.type === 'FIXED' ? '满减券' : '折扣券' }}</span>
                </div>
                <div class="coupon-right">
                  <span class="coupon-name">{{ c.name }}</span>
                  <span class="coupon-save">可省 ¥{{ c.discountAmount }}</span>
                  <span v-if="c.minOrderAmount > 0" class="coupon-min">满¥{{ c.minOrderAmount }}可用</span>
                  <span class="coupon-expire">{{ new Date(c.endTime).toLocaleDateString('zh-CN') }} 到期</span>
                </div>
                <div class="coupon-check">
                  <span v-if="selectedCouponId === c.userCouponId" class="check-icon">✓</span>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showCouponDialog = false">取消</button>
            <button class="btn-submit" @click="confirmPayment">
              {{ selectedCouponId ? '使用优惠券并支付' : '不使用优惠券，直接支付' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 费用明细确认弹窗 -->
      <div v-if="showFeeDialog" class="modal-overlay" @click.self="showFeeDialog = false">
        <div class="modal-body fee-modal">
          <h3>费用明细</h3>
          <div class="fee-breakdown">
            <div class="fee-row">
              <span class="fee-label">{{ feeDetail.payLabel }}</span>
              <span class="fee-value">¥{{ feeDetail.originalAmount }}</span>
            </div>
            <div v-if="feeDetail.payLabel === '约稿定金'" class="fee-tip">
              💡 定金支付不可使用优惠券，优惠券仅在支付尾款时可用
            </div>
            <div class="fee-row" v-if="feeDetail.couponDiscount > 0">
              <span class="fee-label">🎫 优惠券抵扣</span>
              <span class="fee-value green">-¥{{ feeDetail.couponDiscount }}</span>
            </div>
            <div class="fee-row">
              <span class="fee-label">平台服务费 (5%)</span>
              <span class="fee-value">+¥{{ feeDetail.platformFee }}</span>
            </div>
            <div class="fee-row" v-if="feeDetail.feeDiscount > 0">
              <span class="fee-label">🏅 {{ feeDetail.vipLevel }} 手续费减免</span>
              <span class="fee-value green">-¥{{ feeDetail.feeDiscount }}</span>
            </div>
            <div class="fee-divider"></div>
            <div class="fee-row total">
              <span class="fee-label">实付金额</span>
              <span class="fee-value accent">¥{{ feeDetail.totalAmount }}</span>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showFeeDialog = false">取消</button>
            <button class="btn-submit" @click="doFinalPayment" :disabled="paying">确认支付</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else class="loading-wrap">
      <p v-if="loadError">{{ loadError }}</p>
      <p v-else>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getCommission, quoteCommission, startWork, deliverWork,
  confirmDelivery, requestRevision, cancelCommission,
  getCommissionMessages, sendCommissionMessage
} from '@/api/commission'
import { createPayment, getCommissionPayments, continuePay, getAvailableCouponsForOrder } from '@/api/payment'
import { getMyMembership } from '@/api/membership'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const commissionId = Number(route.params.id)
const currentUserId = computed(() => userStore.user?.id)
const commission = ref(null)
const payments = ref([])
const messages = ref([])
const otherUser = ref(null)
const loadError = ref('')
const msgText = ref('')

// 优惠券相关
const showCouponDialog = ref(false)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const pendingPaymentType = ref(null)
const couponLoading = ref(false)

// 费用明细
const showFeeDialog = ref(false)
const paying = ref(false)
const feeDetail = ref({
  payLabel: '', originalAmount: '0', couponDiscount: 0,
  platformFee: '0', feeDiscount: 0, vipLevel: '',
  totalAmount: '0'
})
const pendingPayCouponId = ref(null)

const showQuoteDialog = ref(false)
const showDeliverDialog = ref(false)
const deliverUrl = ref('')
const deliverNote = ref('')
const quoteForm = ref({ totalAmount: null, depositRatio: 0.3, estimatedDays: 7, revisionsAllowed: 2, quoteNote: '' })

const statusMap = { PENDING: '待报价', QUOTED: '已报价', DEPOSIT_PAID: '已付定金', IN_PROGRESS: '创作中', DELIVERED: '已交付', COMPLETED: '已完成', CANCELLED: '已取消', REJECTED: '已拒绝' }
const cancelRoleLabelMap = { CLIENT: '委托方取消', ARTIST: '画师取消', ADMIN: '管理员取消' }
const paymentStatusMap = { PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款', FAILED: '失败', CLOSED: '已关闭' }

const isArtist = computed(() => commission.value && currentUserId.value === commission.value.artistId)
const hasQuote = computed(() => {
  const s = commission.value?.status
  return s && !['PENDING', 'CANCELLED', 'REJECTED'].includes(s)
})
const canCancel = computed(() => {
  const s = commission.value?.status
  return ['PENDING', 'QUOTED', 'DEPOSIT_PAID', 'IN_PROGRESS'].includes(s)
})
const canSendMessage = computed(() => {
  const s = commission.value?.status
  return s && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(s)
})
const showActions = computed(() => {
  const s = commission.value?.status
  return s && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(s)
})

const refUrlList = computed(() => {
  if (!commission.value?.referenceUrls) return []
  return commission.value.referenceUrls.split('\n').filter(u => u.trim())
})

const timeline = computed(() => {
  const steps = [
    { key: 'PENDING', label: '提交需求' },
    { key: 'QUOTED', label: '画师报价' },
    { key: 'DEPOSIT_PAID', label: '支付定金' },
    { key: 'IN_PROGRESS', label: '创作中' },
    { key: 'DELIVERED', label: '作品交付' },
    { key: 'COMPLETED', label: '完成' }
  ]
  const order = ['PENDING', 'QUOTED', 'DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED', 'COMPLETED']
  const idx = order.indexOf(commission.value?.status)
  return steps.map((s, i) => ({
    ...s,
    done: i < idx,
    active: i === idx
  }))
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const res = await getCommission(commissionId)
    if (res.code === 200) {
      commission.value = res.data
      quoteForm.value.totalAmount = res.data.budget || null
      // load other user info
      const otherId = isArtist.value ? res.data.clientId : res.data.artistId
      if (otherId) {
        try {
          const ur = await request({ url: `/api/users/${otherId}`, method: 'get' })
          if (ur.code === 200) otherUser.value = ur.data
        } catch {}
      }
    } else {
      loadError.value = res.message || '加载失败'
    }
  } catch {
    loadError.value = '加载约稿详情失败'
  }

  // payments
  try {
    const pr = await getCommissionPayments(commissionId)
    if (pr.code === 200) payments.value = pr.data || []
  } catch {}

  // messages
  try {
    const mr = await getCommissionMessages(commissionId)
    if (mr.code === 200) messages.value = mr.data || []
  } catch {}
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}

// ---- Actions ----
async function payDeposit() {
  await openCouponSelector('DEPOSIT')
}

async function payFinal() {
  await openCouponSelector('FINAL_PAYMENT')
}

/** 打开优惠券选择弹窗 */
async function openCouponSelector(paymentType) {
  pendingPaymentType.value = paymentType
  selectedCouponId.value = null

  // 定金不可用优惠券，直接进入费用明细
  if (paymentType === 'DEPOSIT') {
    availableCoupons.value = []
    confirmPayment()
    return
  }

  couponLoading.value = true
  showCouponDialog.value = true

  // 计算订单金额
  const c = commission.value
  let orderAmount = 0
  if (paymentType === 'DEPOSIT') {
    orderAmount = c.depositAmount || 0
  } else {
    orderAmount = (c.totalAmount || 0) - (c.depositAmount || 0)
  }

  try {
    const res = await getAvailableCouponsForOrder(orderAmount)
    if (res.code === 200) {
      availableCoupons.value = res.data || []
    } else {
      availableCoupons.value = []
    }
  } catch {
    availableCoupons.value = []
  } finally {
    couponLoading.value = false
  }
}

/** 确认支付 → 先展示费用明细 */
async function confirmPayment() {
  showCouponDialog.value = false

  const c = commission.value
  const type = pendingPaymentType.value
  const originalAmount = parseFloat(type === 'DEPOSIT' ? c.depositAmount : (c.totalAmount - c.depositAmount).toFixed(2))

  // 优惠券折扣
  let couponDiscount = 0
  if (selectedCouponId.value) {
    const coupon = availableCoupons.value.find(x => x.userCouponId === selectedCouponId.value)
    if (coupon) couponDiscount = parseFloat(coupon.discountAmount) || 0
  }
  const afterCoupon = Math.max(0, originalAmount - couponDiscount)

  // 平台服务费
  const platformFee = parseFloat((afterCoupon * 0.05).toFixed(2))

  // 会员手续费减免
  let feeDiscountAmt = 0
  let vipLevel = ''
  try {
    const mRes = await getMyMembership()
    if (mRes.code === 200 && mRes.data && !mRes.data.expired) {
      vipLevel = mRes.data.level || ''
      if (vipLevel === 'SVIP') feeDiscountAmt = parseFloat((platformFee * 0.10).toFixed(2))
      else if (vipLevel === 'VIP') feeDiscountAmt = parseFloat((platformFee * 0.05).toFixed(2))
    }
  } catch { /* ignore */ }

  const actualFee = parseFloat((platformFee - feeDiscountAmt).toFixed(2))
  const totalAmount = parseFloat((afterCoupon + actualFee).toFixed(2))

  feeDetail.value = {
    payLabel: type === 'DEPOSIT' ? '约稿定金' : '约稿尾款',
    originalAmount: originalAmount.toFixed(2),
    couponDiscount,
    platformFee: platformFee.toFixed(2),
    feeDiscount: feeDiscountAmt,
    vipLevel,
    totalAmount: totalAmount.toFixed(2)
  }
  pendingPayCouponId.value = selectedCouponId.value
  showFeeDialog.value = true
}

/** 确认费用后发起支付 */
async function doFinalPayment() {
  paying.value = true
  try {
    const data = { commissionId, paymentType: pendingPaymentType.value }
    if (pendingPayCouponId.value) data.userCouponId = pendingPayCouponId.value
    const res = await createPayment(data)
    if (res.code === 200 && res.data) {
      showFeeDialog.value = false
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '创建支付失败')
    }
  } catch { ElMessage.error('发起支付失败') }
  finally { paying.value = false }
}

function submitAlipayForm(htmlForm) {
  const div = document.createElement('div')
  div.innerHTML = htmlForm
  document.body.appendChild(div)
  const form = div.querySelector('form')
  if (form) form.submit()
}

/** 判断待支付订单是否已超时 */
function isOrderExpired(p) {
  if (!p.expireAt) return false
  return new Date(p.expireAt) < new Date()
}

/** 格式化剩余倒计时 */
function formatCountdown(expireAt) {
  const diff = new Date(expireAt) - new Date()
  if (diff <= 0) return '已超时'
  const mins = Math.floor(diff / 60000)
  const secs = Math.floor((diff % 60000) / 1000)
  return `剩余 ${mins}分${secs}秒`
}

/** 继续支付已有待支付订单 */
async function handleContinuePay(orderNo) {
  try {
    const res = await continuePay(orderNo)
    if (res.code === 200 && res.data) {
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '继续支付失败')
      await loadData()
    }
  } catch {
    ElMessage.error('继续支付失败')
    await loadData()
  }
}

async function doQuote() {
  if (!quoteForm.value.totalAmount || quoteForm.value.totalAmount <= 0) {
    ElMessage.warning('请输入报价金额')
    return
  }
  try {
    const res = await quoteCommission(commissionId, quoteForm.value)
    if (res.code === 200) {
      ElMessage.success('报价已发送')
      showQuoteDialog.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '报价失败')
    }
  } catch { ElMessage.error('报价失败') }
}

async function doStart() {
  try {
    const res = await startWork(commissionId)
    if (res.code === 200) { ElMessage.success('已开始创作'); await loadData() }
    else ElMessage.error(res.message || '操作失败')
  } catch { ElMessage.error('操作失败') }
}

async function doDeliver() {
  if (!deliverUrl.value.trim()) {
    ElMessage.warning('请输入作品链接')
    return
  }
  try {
    const res = await deliverWork(commissionId, { deliveryUrl: deliverUrl.value, deliveryNote: deliverNote.value })
    if (res.code === 200) { ElMessage.success('已交付'); showDeliverDialog.value = false; await loadData() }
    else ElMessage.error(res.message || '操作失败')
  } catch { ElMessage.error('操作失败') }
}

async function doRevision() {
  try {
    await ElMessageBox.confirm('确认请求修改？', '提示')
    const res = await requestRevision(commissionId)
    if (res.code === 200) { ElMessage.success('已请求修改'); await loadData() }
    else ElMessage.error(res.message || '操作失败')
  } catch {}
}

async function doCancel() {
  const status = commission.value?.status
  const hasPaid = ['DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED'].includes(status)

  // 构建退款提示信息
  let warningMsg = ''
  if (isArtist.value) {
    // 画师取消 → 全额退还
    if (hasPaid) {
      warningMsg = '<div style="margin-bottom:12px;padding:12px;background:#fef0e7;border-radius:8px;border-left:4px solid #e6a23c;">' +
        '<p style="margin:0 0 6px;font-weight:600;color:#e6a23c;">⚠️ 退款提示</p>' +
        '<p style="margin:0;color:#606266;font-size:13px;">画师主动取消约稿，委托方已支付的<strong>所有款项（包括定金）</strong>将<strong>全额退还</strong>。</p>' +
        '</div>'
    }
    warningMsg += '<p style="margin:8px 0 0;color:#909399;font-size:13px;">请输入取消原因：</p>'
  } else {
    // 委托方取消
    if (hasPaid) {
      warningMsg = '<div style="margin-bottom:12px;padding:12px;background:#fef0e7;border-radius:8px;border-left:4px solid #f56c6c;">' +
        '<p style="margin:0 0 6px;font-weight:600;color:#f56c6c;">⚠️ 定金不予退还</p>' +
        '<p style="margin:0;color:#606266;font-size:13px;">委托方主动取消约稿，<strong>定金将不予退还</strong>。</p>' +
        '<p style="margin:4px 0 0;color:#606266;font-size:13px;">如有已支付的尾款，尾款部分将退还。</p>' +
        '</div>' +
        '<div style="margin-bottom:12px;padding:10px;background:#f0f9ff;border-radius:8px;font-size:12px;color:#909399;">' +
        '💡 如因画师原因需取消，建议与画师协商由画师方发起取消，可获全额退款。' +
        '</div>'
      warningMsg += '<p style="margin:8px 0 0;color:#909399;font-size:13px;">请输入取消原因：</p>'
    } else {
      warningMsg = '<p style="margin:0;color:#909399;font-size:13px;">当前尚未支付，取消不涉及退款。请输入取消原因：</p>'
    }
  }

  try {
    const { value: reason } = await ElMessageBox.prompt('', '取消约稿', {
      message: warningMsg,
      dangerouslyUseHTMLString: true,
      inputPlaceholder: '取消原因（选填）',
      confirmButtonText: hasPaid ? '确认取消并退款' : '确认取消',
      cancelButtonText: '我再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
      customClass: 'cancel-commission-dialog'
    })
    const res = await cancelCommission(commissionId, reason || '')
    if (res.code === 200) {
      if (isArtist.value && hasPaid) {
        ElMessage.success('约稿已取消，款项将退还给委托方')
      } else if (!isArtist.value && hasPaid) {
        ElMessage.success('约稿已取消，定金不退还，尾款（如有）将退还')
      } else {
        ElMessage.success('约稿已取消')
      }
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

async function sendMsg() {
  if (!msgText.value.trim()) return
  try {
    const res = await sendCommissionMessage(commissionId, { content: msgText.value.trim() })
    if (res.code === 200) {
      msgText.value = ''
      await loadData()
    }
  } catch {}
}
</script>

<style scoped>
.commission-detail-page {
  min-height: calc(100vh - 64px);
  background: #f5f5f5;
  padding: 24px;
}
.page-container {
  max-width: 1100px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.back-btn:hover { color: #333; background: #f0f0f0; }

.detail-layout {
  display: flex;
  gap: 20px;
}
.main-col { flex: 1; min-width: 0; }
.side-col { width: 300px; flex-shrink: 0; }

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 16px;
}

/* 标题卡 */
.title-card h1 {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: #1a1a1a;
}
.title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.create-time { font-size: 12px; color: #999; margin: 0; }

/* 状态 badge */
.status-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.status-PENDING { background: #fff7e6; color: #d48806; }
.status-QUOTED { background: #e6f7ff; color: #1890ff; }
.status-DEPOSIT_PAID { background: #f0f5ff; color: #2f54eb; }
.status-IN_PROGRESS { background: #fff0f6; color: #eb2f96; }
.status-DELIVERED { background: #f6ffed; color: #52c41a; }
.status-COMPLETED { background: #f0fdf4; color: #16a34a; }
.status-CANCELLED { background: #f5f5f5; color: #999; }
.status-REJECTED { background: #fff1f0; color: #ff4d4f; }

/* 描述 */
.desc-text {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
}
.ref-section { margin-top: 16px; }
.ref-section h4 { font-size: 13px; color: #666; margin: 0 0 8px; }
.ref-link a {
  color: #0096FA;
  font-size: 13px;
  word-break: break-all;
}

/* info grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
}
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item .label { font-size: 12px; color: #999; }
.info-item .value { font-size: 15px; font-weight: 600; color: #333; }
.info-item .value.price { color: #0096FA; }
.quote-note {
  margin: 14px 0 0;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 支付 */
.payment-list { display: flex; flex-direction: column; gap: 10px; }
.payment-item {
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}
.payment-main { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.payment-type { font-weight: 600; font-size: 14px; color: #333; }
.payment-amount { font-weight: 700; font-size: 16px; color: #0096FA; }
.payment-sub { display: flex; gap: 12px; font-size: 12px; color: #999; flex-wrap: wrap; }
.ps-PAID { color: #52c41a; }
.ps-PENDING { color: #d48806; }
.ps-REFUNDED { color: #ff4d4f; }
.ps-CLOSED { color: #999; }

.continue-pay-btn {
  padding: 2px 12px;
  background: #0096FA;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: background .2s;
}
.continue-pay-btn:hover { background: #007ad9; }

.expire-hint {
  color: #d48806;
  font-size: 11px;
}

.refund-time { color: #ff4d4f !important; }
.refund-reason { color: #999; font-style: italic; }

/* 取消信息 */
.cancel-info-card { border-left: 4px solid #e6a23c; }
.cancel-info { display: flex; flex-direction: column; gap: 10px; }
.cancel-role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
}
.role-CLIENT { background: #fef0e7; color: #e6a23c; }
.role-ARTIST { background: #e6f7ff; color: #1890ff; }
.role-ADMIN { background: #f0f0f0; color: #666; }
.cancel-reason { margin: 0; font-size: 14px; color: #555; }
.cancel-refund-hint p { margin: 0; font-size: 13px; }
.refund-ok { color: #52c41a; }
.refund-partial { color: #e6a23c; }
.refund-admin { color: #909399; }

/* 消息 */
.empty-msg { text-align: center; color: #ccc; padding: 20px; font-size: 14px; }
.msg-list { max-height: 300px; overflow-y: auto; }
.msg-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.msg-item.mine .msg-sender { color: #0096FA; }
.msg-sender { font-weight: 600; color: #333; white-space: nowrap; }
.msg-content { flex: 1; color: #555; }
.msg-time { font-size: 11px; color: #bbb; white-space: nowrap; }
.msg-send { display: flex; gap: 8px; margin-top: 12px; }
.msg-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
}
.msg-input:focus { outline: none; border-color: #0096FA; }
.msg-btn {
  padding: 10px 20px;
  border: none;
  background: #0096FA;
  color: #fff;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
}
.msg-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 右侧 */
.user-row { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-weight: 600; font-size: 14px; color: #1a1a1a; }

.amount-summary { display: flex; flex-direction: column; gap: 10px; }
.amount-row { display: flex; justify-content: space-between; font-size: 14px; color: #666; }
.amount-val { font-weight: 700; color: #333; }
.amount-val.total { color: #0096FA; font-size: 16px; }

/* 操作 */
.action-list { display: flex; flex-direction: column; gap: 10px; }
.action-btn {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}
.action-btn:hover { opacity: 0.85; }
.action-btn.primary { background: #0096FA; color: #fff; }
.action-btn.outline { background: #fff; color: #0096FA; border: 1px solid #0096FA; }
.action-btn.danger { background: #fff; color: #ff4d4f; border: 1px solid #ff4d4f; }

/* timeline */
.timeline { display: flex; flex-direction: column; gap: 0; }
.timeline-step {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  position: relative;
}
.timeline-step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 7px;
  top: 28px;
  width: 2px;
  height: calc(100% - 12px);
  background: #e8e8e8;
}
.timeline-step.done:not(:last-child)::after { background: #52c41a; }
.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #e8e8e8;
  background: #fff;
  flex-shrink: 0;
  z-index: 1;
}
.timeline-step.done .timeline-dot { border-color: #52c41a; background: #52c41a; }
.timeline-step.active .timeline-dot { border-color: #0096FA; background: #0096FA; }
.timeline-label { font-size: 13px; color: #999; }
.timeline-step.done .timeline-label { color: #52c41a; }
.timeline-step.active .timeline-label { color: #0096FA; font-weight: 600; }

/* loading */
.loading-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  color: #999;
}

/* modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.modal-body {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  width: 90%;
  max-width: 460px;
}
.modal-body h3 { margin: 0 0 18px; font-size: 18px; }
.modal-form { display: flex; flex-direction: column; gap: 10px; margin-bottom: 18px; }
.modal-form label { font-size: 13px; font-weight: 600; color: #555; }
.form-input {
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
}
.form-input:focus { outline: none; border-color: #0096FA; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
}
.btn-submit {
  padding: 8px 24px;
  border: none;
  background: #0096FA;
  color: #fff;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 768px) {
  .commission-detail-page { padding: 12px; }
  .detail-layout { flex-direction: column; }
  .side-col { width: 100%; }
}

/* 优惠券弹窗 */
.coupon-modal { max-width: 520px; }
.coupon-loading { text-align: center; padding: 30px 0; color: #999; }
.empty-coupon { text-align: center; padding: 30px 0; color: #999; }
.coupon-list { max-height: 320px; overflow-y: auto; margin-bottom: 18px; display: flex; flex-direction: column; gap: 10px; }
.coupon-item {
  display: flex;
  align-items: center;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.coupon-item:hover { border-color: #0096FA; background: #f0f8ff; }
.coupon-item.selected { border-color: #0096FA; background: #e6f4ff; }
.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
  padding-right: 14px;
  border-right: 1px dashed #ddd;
}
.coupon-discount { font-size: 22px; font-weight: 700; color: #ff4d4f; }
.coupon-type { font-size: 11px; color: #999; margin-top: 2px; }
.coupon-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding-left: 14px;
}
.coupon-name { font-size: 14px; font-weight: 600; color: #333; }
.coupon-save { font-size: 13px; color: #ff4d4f; font-weight: 500; }
.coupon-min { font-size: 12px; color: #999; }
.coupon-expire { font-size: 11px; color: #bbb; }
.coupon-check { width: 28px; display: flex; justify-content: center; }
.check-icon { color: #0096FA; font-size: 20px; font-weight: 700; }
.payment-coupon-tag {
  display: inline-block;
  font-size: 11px;
  color: #ff4d4f;
  background: #fff1f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  padding: 1px 6px;
  margin-left: 8px;
}

/* 费用明细弹窗 */
.fee-modal { max-width: 420px; }
.fee-breakdown { padding: 8px 0; }
.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}
.fee-label { font-size: 14px; color: #555; }
.fee-value { font-size: 14px; font-weight: 600; color: #1a1a2e; font-variant-numeric: tabular-nums; }
.fee-value.green { color: #52c41a; }
.fee-value.accent { font-size: 20px; font-weight: 800; color: #0096FA; }
.fee-divider { border-top: 1px dashed #e0e0e0; margin: 8px 0; }
.fee-tip {
  background: #fef9e7;
  color: #b7791f;
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  margin: 4px 0 8px;
  line-height: 1.6;
}
.fee-row.total .fee-label { font-size: 15px; font-weight: 700; color: #1a1a2e; }
</style>
