<template>
  <div class="commissions-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">接稿管理</h1>
        <p class="page-desc">管理所有委托订单，跟踪创作进度</p>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <button
        v-for="f in statusFilters"
        :key="f.value"
        class="filter-btn"
        :class="{ active: currentStatus === f.value }"
        @click="filterByStatus(f.value)"
      >
        {{ f.label }}
        <span v-if="f.count > 0" class="filter-count">{{ f.count }}</span>
      </button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <!-- 列表 -->
    <div v-else-if="filteredCommissions.length > 0" class="commission-list">
      <div v-for="c in filteredCommissions" :key="c.id" class="commission-card">
        <!-- 顶部 -->
        <div class="card-top">
          <div class="card-client">
            <el-avatar :size="36" :src="c.clientAvatarUrl || defaultAvatar">
              {{ (c.clientName || '?').charAt(0) }}
            </el-avatar>
            <div class="client-info">
              <span class="client-label">委托方</span>
              <span class="client-name">{{ c.clientName || '用户' }}</span>
            </div>
          </div>
          <span class="status-badge" :class="'status-' + c.status.toLowerCase()">
            {{ getStatusText(c.status) }}
          </span>
        </div>

        <!-- 标题描述 -->
        <h3 class="card-title">{{ c.title }}</h3>
        <p class="card-desc">{{ c.description }}</p>

        <!-- 委托方预算和参考信息(PENDING状态) -->
        <div v-if="c.status === 'PENDING'" class="client-request-info">
          <div v-if="c.budget" class="request-item">
            <span class="request-label">委托方预算</span>
            <span class="request-value price">¥{{ c.budget }}</span>
          </div>
          <div v-if="c.referenceUrls" class="request-item full">
            <span class="request-label">参考链接</span>
            <span class="request-value">{{ c.referenceUrls }}</span>
          </div>
        </div>

        <!-- 报价信息（已报价后显示） -->
        <div v-if="c.status === 'QUOTED'" class="quote-sent-info">
          <div class="quote-sent-header">已发送报价，等待委托方确认</div>
          <div class="quote-sent-details">
            <span>报价 ¥{{ c.totalAmount }}</span>
            <span>定金 ¥{{ c.depositAmount }}</span>
            <span v-if="c.quoteNote">说明: {{ c.quoteNote }}</span>
          </div>
        </div>

        <!-- 信息栏（报价确认后显示） -->
        <div v-if="!['PENDING', 'QUOTED'].includes(c.status)" class="card-info">
          <div class="info-item">
            <span class="info-label">总金额</span>
            <span class="info-value price">¥{{ c.totalAmount }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">定金</span>
            <span class="info-value">¥{{ c.depositAmount }}</span>
            <span :class="['pay-tag', c.depositPaid ? 'paid' : 'unpaid']">{{ c.depositPaid ? '已付' : '未付' }}</span>
          </div>
          <div v-if="c.deadline" class="info-item">
            <span class="info-label">截止日期</span>
            <span class="info-value" :class="{ 'text-danger': isOverdue(c.deadline) }">
              {{ formatDate(c.deadline) }}
              <span v-if="isOverdue(c.deadline)" class="overdue-tag">已逾期</span>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">修改次数</span>
            <span class="info-value">{{ c.revisionsUsed || 0 }} / {{ c.revisionsAllowed || 3 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatDate(c.createdAt) }}</span>
          </div>
        </div>

        <!-- 交付信息 -->
        <div v-if="c.deliveryUrl" class="delivery-section">
          <div class="delivery-header">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="#00C48C"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
            <span>已交付作品</span>
          </div>
          <a :href="c.deliveryUrl" target="_blank" class="delivery-link">查看交付作品</a>
          <p v-if="c.deliveryNote" class="delivery-note">{{ c.deliveryNote }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <!-- PENDING: 报价或拒绝 -->
          <button v-if="c.status === 'PENDING'" class="action-btn primary" @click="openQuoteDialog(c)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>
            报价
          </button>
          <button v-if="c.status === 'PENDING'" class="action-btn danger-text" @click="handleReject(c)">
            拒绝
          </button>

          <!-- 开始创作: DEPOSIT_PAID → IN_PROGRESS -->
          <button v-if="c.status === 'DEPOSIT_PAID'" class="action-btn primary" @click="handleStartWork(c)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
            开始创作
          </button>

          <!-- 交付作品: IN_PROGRESS → DELIVERED -->
          <button v-if="c.status === 'IN_PROGRESS'" class="action-btn primary" @click="openDeliverDialog(c)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
            交付作品
          </button>

          <!-- 被要求修改后重新交付: IN_PROGRESS (revision) -->
          <span v-if="c.status === 'IN_PROGRESS' && c.revisionsUsed > 0" class="revision-hint">
            第 {{ c.revisionsUsed }} 次修改中
          </span>

          <!-- 联系委托方 -->
          <button v-if="!['COMPLETED','CANCELLED','REJECTED'].includes(c.status)" class="action-btn secondary" @click="handleChat(c)">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
            联系委托方
          </button>

          <!-- 取消 -->
          <button v-if="['PENDING','QUOTED','DEPOSIT_PAID'].includes(c.status)" class="action-btn danger-text" @click="handleCancel(c)">
            取消
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg viewBox="0 0 64 64" width="56" height="56" fill="none" stroke="#ddd" stroke-width="1.5">
        <rect x="10" y="8" width="44" height="48" rx="6"/>
        <path d="M22 24h20M22 32h14M22 40h18"/>
      </svg>
      <p>{{ currentStatus === 'all' ? '暂无约稿委托' : '没有该状态的约稿' }}</p>
    </div>

    <!-- 报价弹窗 -->
    <div v-if="quoteDialog" class="dialog-mask" @click.self="quoteDialog = false">
      <div class="dialog-box" style="width: 520px;">
        <div class="dialog-header">
          <h3>报价</h3>
          <button class="close-btn" @click="quoteDialog = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
        </div>
        <div class="dialog-body">
          <div v-if="quoteTarget?.budget" class="budget-hint">
            委托方预算: ¥{{ quoteTarget.budget }}
          </div>
          <div class="form-group">
            <label>报价金额 (元) <span class="required">*</span></label>
            <input v-model.number="quoteForm.totalAmount" type="number" class="form-input" placeholder="输入报价金额" min="1" />
          </div>
          <div class="form-group">
            <label>定金比例</label>
            <div class="ratio-selector">
              <button v-for="r in [0.2, 0.3, 0.5, 0.7, 1.0]" :key="r"
                class="ratio-btn" :class="{ active: quoteForm.depositRatio === r }"
                @click="quoteForm.depositRatio = r">
                {{ (r * 100).toFixed(0) }}%
              </button>
            </div>
            <div v-if="quoteForm.totalAmount" class="deposit-preview">
              定金: ¥{{ (quoteForm.totalAmount * quoteForm.depositRatio).toFixed(2) }}
              <span v-if="quoteForm.depositRatio < 1">/ 尾款: ¥{{ (quoteForm.totalAmount * (1 - quoteForm.depositRatio)).toFixed(2) }}</span>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>预计完成天数</label>
              <input v-model.number="quoteForm.estimatedDays" type="number" class="form-input" min="1" max="365" />
            </div>
            <div class="form-group half">
              <label>允许修改次数</label>
              <input v-model.number="quoteForm.revisionsAllowed" type="number" class="form-input" min="0" max="10" />
            </div>
          </div>
          <div class="form-group">
            <label>报价说明</label>
            <textarea v-model="quoteForm.quoteNote" class="form-textarea" rows="3" placeholder="补充报价说明、注意事项等（可选）"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="quoteDialog = false">取消</button>
          <button class="btn-save" @click="handleQuote" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认报价' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 交付弹窗 -->
    <div v-if="deliverDialog" class="dialog-mask" @click.self="deliverDialog = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>交付作品</h3>
          <button class="close-btn" @click="deliverDialog = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>作品链接 <span class="required">*</span></label>
            <input v-model="deliverForm.deliveryUrl" class="form-input" placeholder="作品文件 URL 或在线地址" />
          </div>
          <div class="form-group">
            <label>交付说明</label>
            <textarea v-model="deliverForm.deliveryNote" class="form-textarea" rows="3" placeholder="可以添加交付说明、注意事项等"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="deliverDialog = false">取消</button>
          <button class="btn-save" @click="handleDeliver" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认交付' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyCommissions, quoteCommission, rejectCommission, startWork, deliverWork, cancelCommission } from '@/api/commission'
import { createOrGetConversation } from '@/api/chat'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loading = ref(true)
const commissions = ref([])
const currentStatus = ref('all')
const submitting = ref(false)

// 报价对话框
const quoteDialog = ref(false)
const quoteTarget = ref(null)
const quoteForm = ref({
  totalAmount: 500,
  depositRatio: 0.3,
  estimatedDays: 14,
  revisionsAllowed: 3,
  quoteNote: ''
})

// 交付对话框
const deliverDialog = ref(false)
const deliverForm = ref({ commissionId: null, deliveryUrl: '', deliveryNote: '' })

const statusFilters = computed(() => {
  const counts = {}
  commissions.value.forEach(c => {
    counts[c.status] = (counts[c.status] || 0) + 1
  })
  return [
    { label: '全部', value: 'all', count: commissions.value.length },
    { label: '待报价', value: 'PENDING', count: counts['PENDING'] || 0 },
    { label: '已报价', value: 'QUOTED', count: counts['QUOTED'] || 0 },
    { label: '已付定金', value: 'DEPOSIT_PAID', count: counts['DEPOSIT_PAID'] || 0 },
    { label: '创作中', value: 'IN_PROGRESS', count: counts['IN_PROGRESS'] || 0 },
    { label: '已交付', value: 'DELIVERED', count: counts['DELIVERED'] || 0 },
    { label: '已完成', value: 'COMPLETED', count: counts['COMPLETED'] || 0 },
    { label: '已取消', value: 'CANCELLED', count: counts['CANCELLED'] || 0 }
  ]
})

const filteredCommissions = computed(() => {
  if (currentStatus.value === 'all') return commissions.value
  return commissions.value.filter(c => c.status === currentStatus.value)
})

function filterByStatus(status) {
  currentStatus.value = status
}

function getStatusText(status) {
  const map = {
    PENDING: '待报价',
    QUOTED: '已报价',
    DEPOSIT_PAID: '已付定金',
    IN_PROGRESS: '创作中',
    DELIVERED: '已交付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function isOverdue(deadline) {
  return deadline && new Date(deadline) < new Date()
}

async function loadCommissions() {
  loading.value = true
  try {
    const res = await getMyCommissions({ role: 'artist', page: 0, size: 50 })
    if (res.code === 200) {
      commissions.value = res.data?.content || res.data?.records || []
    }
  } catch {
    ElMessage.error('加载约稿失败')
  } finally {
    loading.value = false
  }
}

// === 报价 ===

function openQuoteDialog(c) {
  quoteTarget.value = c
  quoteForm.value = {
    totalAmount: c.budget || 500,
    depositRatio: 0.3,
    estimatedDays: 14,
    revisionsAllowed: 3,
    quoteNote: ''
  }
  quoteDialog.value = true
}

async function handleQuote() {
  if (!quoteForm.value.totalAmount || quoteForm.value.totalAmount <= 0) {
    ElMessage.warning('请输入报价金额')
    return
  }
  submitting.value = true
  try {
    const data = {
      totalAmount: quoteForm.value.totalAmount,
      depositRatio: quoteForm.value.depositRatio,
      revisionsAllowed: quoteForm.value.revisionsAllowed,
      quoteNote: quoteForm.value.quoteNote
    }
    if (quoteForm.value.estimatedDays) {
      const deadline = new Date()
      deadline.setDate(deadline.getDate() + quoteForm.value.estimatedDays)
      data.deadline = deadline.toISOString().substring(0, 19)
    }
    const res = await quoteCommission(quoteTarget.value.id, data)
    if (res.code === 200) {
      ElMessage.success('报价成功，等待委托方确认支付')
      quoteDialog.value = false
      loadCommissions()
    } else {
      ElMessage.error(res.message || '报价失败')
    }
  } catch {
    ElMessage.error('报价失败')
  } finally {
    submitting.value = false
  }
}

// === 拒绝 ===

async function handleReject(c) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝约稿', {
      inputPlaceholder: '拒绝原因...',
      type: 'warning'
    })
    const res = await rejectCommission(c.id, value || '')
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// === 开始创作 ===

async function handleStartWork(c) {
  try {
    await ElMessageBox.confirm(`确定开始创作「${c.title}」吗？`, '开始创作')
    const res = await startWork(c.id)
    if (res.code === 200) {
      ElMessage.success('已开始创作')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// === 交付 ===

function openDeliverDialog(c) {
  deliverForm.value = { commissionId: c.id, deliveryUrl: '', deliveryNote: '' }
  deliverDialog.value = true
}

async function handleDeliver() {
  if (!deliverForm.value.deliveryUrl.trim()) {
    ElMessage.warning('请填写作品链接')
    return
  }
  submitting.value = true
  try {
    const res = await deliverWork(deliverForm.value.commissionId, {
      deliveryUrl: deliverForm.value.deliveryUrl.trim(),
      deliveryNote: deliverForm.value.deliveryNote.trim()
    })
    if (res.code === 200) {
      ElMessage.success('交付成功，等待委托方确认并支付尾款')
      deliverDialog.value = false
      loadCommissions()
    } else {
      ElMessage.error(res.message || '交付失败')
    }
  } catch {
    ElMessage.error('交付失败')
  } finally {
    submitting.value = false
  }
}

async function handleChat(c) {
  try {
    const res = await createOrGetConversation(c.clientId)
    if (res.code === 200) {
      router.push(`/chat/${res.data.id}`)
    } else {
      ElMessage.error('无法创建对话')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleCancel(c) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消约稿', {
      inputPlaceholder: '取消原因（可选）',
      type: 'warning'
    })
    const res = await cancelCommission(c.id, value || '')
    if (res.code === 200) {
      ElMessage.success('已取消')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadCommissions)
</script>

<style scoped>
.commissions-page { max-width: 800px; }

.page-header { margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px 0; }
.page-desc { font-size: 14px; color: #999; margin: 0; }

/* 筛选 */
.filter-bar {
  display: flex; gap: 6px; flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 7px 14px; border: none; background: #fff;
  border-radius: 8px; font-size: 13px; color: #666;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.filter-btn:hover { color: #333; }
.filter-btn.active { background: #0096FA; color: #fff; box-shadow: 0 2px 8px rgba(0,150,250,0.3); }
.filter-count {
  font-size: 11px; background: rgba(0,0,0,0.08); padding: 1px 6px;
  border-radius: 10px; font-weight: 600;
}
.filter-btn.active .filter-count { background: rgba(255,255,255,0.3); }

/* 加载 */
.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 列表 */
.commission-list { display: flex; flex-direction: column; gap: 16px; }
.commission-card {
  background: #fff; border-radius: 14px; padding: 20px 22px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
}
.commission-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); }

.card-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 14px;
}
.card-client { display: flex; align-items: center; gap: 10px; }
.client-info { display: flex; flex-direction: column; }
.client-label { font-size: 11px; color: #bbb; }
.client-name { font-size: 14px; font-weight: 500; color: #333; }

.status-badge {
  font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 6px;
}
.status-pending { background: #FFF3E0; color: #FF9800; }
.status-quoted { background: #E8F5E9; color: #4CAF50; }
.status-deposit_paid { background: #E8F4FF; color: #0096FA; }
.status-in_progress { background: #EBF5FF; color: #0096FA; }
.status-delivered { background: #F3E8FF; color: #9B59B6; }
.status-completed { background: #E6F7F0; color: #00C48C; }
.status-cancelled { background: #f5f5f5; color: #999; }
.status-rejected { background: #FFF1F0; color: #FF4D4F; }

.card-title { font-size: 17px; font-weight: 600; color: #1a1a1a; margin: 0 0 6px 0; }
.card-desc {
  font-size: 13px; color: #888; margin: 0 0 16px 0;
  line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}

/* 委托方请求信息 */
.client-request-info {
  padding: 12px 16px; background: #FFF8E1; border: 1px solid #FFE082;
  border-radius: 10px; margin-bottom: 14px;
  display: flex; flex-wrap: wrap; gap: 16px;
}
.request-item { display: flex; flex-direction: column; gap: 2px; }
.request-item.full { flex-basis: 100%; }
.request-label { font-size: 11px; color: #999; }
.request-value { font-size: 14px; color: #333; font-weight: 500; }
.request-value.price { color: #F57C00; font-weight: 700; font-size: 16px; }

/* 已发送报价 */
.quote-sent-info {
  padding: 12px 16px; background: #E8F5E9; border: 1px solid #C8E6C9;
  border-radius: 10px; margin-bottom: 14px;
}
.quote-sent-header {
  font-size: 13px; font-weight: 600; color: #4CAF50; margin-bottom: 6px;
}
.quote-sent-details {
  display: flex; gap: 16px; font-size: 13px; color: #666; flex-wrap: wrap;
}

.card-info {
  display: flex; flex-wrap: wrap; gap: 16px;
  padding: 14px 16px; background: #f9fafb; border-radius: 10px;
  margin-bottom: 14px;
}
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 11px; color: #bbb; }
.info-value { font-size: 14px; color: #333; font-weight: 500; }
.info-value.price { color: #0096FA; font-weight: 700; }
.info-value.text-danger { color: #FF4D4F; }
.pay-tag {
  font-size: 10px; padding: 1px 6px; border-radius: 4px; font-weight: 600; margin-top: 2px;
}
.pay-tag.paid { background: #E6F7F0; color: #00C48C; }
.pay-tag.unpaid { background: #FFF1F0; color: #FF4D4F; }
.overdue-tag {
  font-size: 10px; background: #FF4D4F; color: #fff; padding: 1px 6px;
  border-radius: 4px; margin-left: 4px;
}

.delivery-section {
  padding: 12px 16px; background: #F0FAF5; border-radius: 10px;
  margin-bottom: 14px;
}
.delivery-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; font-weight: 600; color: #00C48C; margin-bottom: 6px;
}
.delivery-link {
  font-size: 13px; color: #0096FA; word-break: break-all;
}
.delivery-note { font-size: 12px; color: #888; margin: 6px 0 0; }

/* 操作 */
.card-actions {
  display: flex; gap: 10px; flex-wrap: wrap; align-items: center;
}
.action-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 8px 16px; border: none; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.15s;
}
.action-btn.primary { background: #0096FA; color: #fff; }
.action-btn.primary:hover { background: #0080d5; }
.action-btn.secondary { background: #f0f0f0; color: #555; }
.action-btn.secondary:hover { background: #e5e5e5; color: #333; }
.action-btn.danger-text { background: transparent; color: #FF4D4F; }
.action-btn.danger-text:hover { background: #FFF1F0; }
.revision-hint {
  font-size: 12px; color: #FF9800; font-weight: 500;
  padding: 4px 10px; background: #FFF3E0; border-radius: 6px;
}

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 80px 0; color: #bbb; font-size: 14px;
}

/* 预算提示 */
.budget-hint {
  padding: 10px 14px; background: #FFF8E1; border: 1px solid #FFE082;
  border-radius: 8px; margin-bottom: 16px; font-size: 14px;
  color: #F57C00; font-weight: 600;
}

/* 定金比例选择 */
.ratio-selector {
  display: flex; gap: 8px; margin-top: 6px;
}
.ratio-btn {
  padding: 6px 14px; border: 1px solid #e0e0e0; border-radius: 8px;
  background: #fff; font-size: 13px; color: #666; cursor: pointer;
  transition: all 0.15s;
}
.ratio-btn.active {
  background: #0096FA; color: #fff; border-color: #0096FA;
}
.ratio-btn:hover:not(.active) { border-color: #0096FA; color: #0096FA; }

.deposit-preview {
  font-size: 13px; color: #666; margin-top: 8px;
  padding: 8px 12px; background: #f9f9f9; border-radius: 6px;
}

.form-row { display: flex; gap: 16px; }
.form-group.half { flex: 1; }

/* 弹窗 */
.dialog-mask {
  position: fixed; inset: 0; z-index: 2000;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #fff; border-radius: 16px; width: 460px; max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.dialog-header {
  display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0;
}
.dialog-header h3 { font-size: 18px; font-weight: 600; margin: 0; }
.close-btn { background: none; border: none; color: #999; cursor: pointer; padding: 4px; border-radius: 6px; }
.close-btn:hover { background: #f0f0f0; color: #333; }
.dialog-body { padding: 20px 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.required { color: #FF4D4F; }
.form-input, .form-textarea {
  width: 100%; padding: 10px 14px; border: 1px solid #e0e0e0;
  border-radius: 8px; font-size: 14px; color: #333;
  transition: border-color 0.2s; box-sizing: border-box;
}
.form-input:focus, .form-textarea:focus { outline: none; border-color: #0096FA; }
.form-textarea { resize: vertical; font-family: inherit; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 0 24px 20px; }
.btn-cancel {
  padding: 8px 20px; border: 1px solid #e0e0e0; background: #fff;
  border-radius: 8px; font-size: 14px; color: #666; cursor: pointer;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-save {
  padding: 8px 24px; border: none; background: #0096FA; color: #fff;
  border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer;
}
.btn-save:hover { background: #0080d5; }
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
