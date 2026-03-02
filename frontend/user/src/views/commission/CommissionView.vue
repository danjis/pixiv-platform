<template>
  <div class="commission-page">
    <div class="commission-container">
      <!-- 页头 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">约稿管理</h1>
          <p class="page-desc">管理你的约稿委托与接稿</p>
        </div>
      </div>

      <!-- Tabs -->
      <div class="tabs-bar">
        <button class="tab-btn" :class="{ active: activeTab === 'client' }" @click="switchTab('client')">
          我的委托
          <span v-if="clientTotal" class="tab-count">{{ clientTotal }}</span>
        </button>
        <button v-if="isArtist" class="tab-btn" :class="{ active: activeTab === 'artist' }" @click="switchTab('artist')">
          我的接稿
          <span v-if="artistTotal" class="tab-count">{{ artistTotal }}</span>
        </button>
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
        </button>
      </div>

      <!-- 列表 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
      </div>

      <div v-else-if="commissions.length > 0" class="card-list">
        <div v-for="c in commissions" :key="c.id" class="commission-card" @click="goDetail(c)">
          <div class="card-top">
            <div class="card-meta">
              <el-avatar :size="36" :src="getOtherAvatar(c) || defaultAvatar">
                {{ getOtherName(c)?.charAt(0) }}
              </el-avatar>
              <div>
                <span class="card-role">{{ activeTab === 'client' ? '画师' : '委托方' }}</span>
                <span class="card-target">{{ getOtherName(c) }}</span>
              </div>
            </div>
            <span class="status-badge" :class="'status-' + c.status.toLowerCase()">
              {{ getStatusText(c.status) }}
            </span>
          </div>

          <h3 class="card-title">{{ c.title }}</h3>
          <p class="card-desc">{{ c.description }}</p>

          <!-- 报价信息（画师已报价） -->
          <div v-if="c.status === 'QUOTED' && activeTab === 'client'" class="quote-info">
            <div class="quote-header">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="#FF9800"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              <span>画师已报价，等待您确认</span>
            </div>
            <div class="quote-details">
              <div class="quote-item">
                <span class="quote-label">报价金额</span>
                <span class="quote-value price">¥{{ c.totalAmount }}</span>
              </div>
              <div class="quote-item">
                <span class="quote-label">需付定金</span>
                <span class="quote-value">¥{{ c.depositAmount }}</span>
              </div>
              <div v-if="c.quoteNote" class="quote-note">
                <span class="quote-label">报价说明：</span>{{ c.quoteNote }}
              </div>
            </div>
          </div>

          <div class="card-info-row">
            <!-- 待处理时显示预算 -->
            <div v-if="c.status === 'PENDING' && c.budget" class="info-item">
              <span class="info-label">预算</span>
              <span class="info-value price">¥{{ c.budget }}</span>
            </div>
            <!-- 有报价后显示总金额 -->
            <div v-if="c.totalAmount && !['PENDING','QUOTED'].includes(c.status)" class="info-item">
              <span class="info-label">总金额</span>
              <span class="info-value price">¥{{ c.totalAmount }}</span>
            </div>
            <div v-if="c.depositAmount && !['PENDING','QUOTED'].includes(c.status)" class="info-item">
              <span class="info-label">定金</span>
              <span class="info-value">¥{{ c.depositAmount }}</span>
              <span :class="['pay-status', c.depositPaid ? 'paid' : 'unpaid']">{{ c.depositPaid ? '已付' : '未付' }}</span>
            </div>
            <div v-if="c.deadline" class="info-item">
              <span class="info-label">截止</span>
              <span class="info-value">{{ formatDate(c.deadline) }}</span>
            </div>
            <div v-if="c.revisionsAllowed" class="info-item">
              <span class="info-label">修改次数</span>
              <span class="info-value">{{ c.revisionsUsed || 0 }}/{{ c.revisionsAllowed }}</span>
            </div>
          </div>

          <!-- 交付信息 -->
          <div v-if="c.deliveryUrl" class="delivery-info">
            <span class="delivery-label">交付作品:</span>
            <a :href="c.deliveryUrl" target="_blank" class="delivery-link" @click.stop>查看作品</a>
            <span v-if="c.deliveryNote" class="delivery-note">{{ c.deliveryNote }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions" @click.stop>
            <!-- 委托方操作 -->
            <template v-if="activeTab === 'client'">
              <!-- QUOTED: 接受报价 → 支付宝支付定金 -->
              <button v-if="c.status === 'QUOTED'" class="action-btn primary" @click="handlePayDeposit(c)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
                接受报价并支付定金 ¥{{ c.depositAmount }}
              </button>
              <!-- DELIVERED: 确认收货 + 支付尾款 -->
              <button v-if="c.status === 'DELIVERED'" class="action-btn primary" @click="handlePayFinal(c)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
                确认收货并支付尾款 ¥{{ (c.totalAmount - c.depositAmount).toFixed(2) }}
              </button>
              <button v-if="c.status === 'DELIVERED' && c.revisionsUsed < c.revisionsAllowed" class="action-btn outline" @click="handleRevision(c)">
                请求修改 ({{ c.revisionsAllowed - c.revisionsUsed }}次)
              </button>
              <!-- PENDING: 等待画师报价 -->
              <span v-if="c.status === 'PENDING'" class="waiting-hint">等待画师报价中...</span>
            </template>

            <!-- 画师操作 -->
            <template v-if="activeTab === 'artist'">
              <!-- PENDING: 报价 -->
              <button v-if="c.status === 'PENDING'" class="action-btn primary" @click="openQuoteDialog(c)">
                报价
              </button>
              <button v-if="c.status === 'PENDING'" class="action-btn danger-text" @click="handleReject(c)">
                拒绝
              </button>
              <button v-if="c.status === 'DEPOSIT_PAID'" class="action-btn primary" @click="handleStart(c)">
                开始创作
              </button>
              <button v-if="c.status === 'IN_PROGRESS'" class="action-btn primary" @click="showDeliverDialog(c)">
                交付作品
              </button>
            </template>

            <!-- 通用操作 -->
            <button
              v-if="!['COMPLETED','CANCELLED','REJECTED'].includes(c.status)"
              class="action-btn danger-text"
              @click="handleCancel(c)"
            >
              取消约稿
            </button>
            <button class="action-btn outline" @click="goChat(c)">
              私信沟通
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none" stroke="#ddd" stroke-width="1.5">
          <rect x="12" y="8" width="40" height="48" rx="4"/>
          <line x1="20" y1="20" x2="44" y2="20"/><line x1="20" y1="28" x2="36" y2="28"/><line x1="20" y1="36" x2="40" y2="36"/>
        </svg>
        <p>{{ activeTab === 'client' ? '暂无委托约稿' : '暂无接稿记录' }}</p>
        <span class="empty-hint">
          {{ activeTab === 'client' ? '去画师主页发起约稿委托吧' : '当有用户向你发起约稿时，会在这里显示' }}
        </span>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pager">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          small
          @current-change="loadCommissions"
        />
      </div>
    </div>

    <!-- 交付对话框 -->
    <el-dialog v-model="deliverDialogVisible" title="交付作品" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="作品链接" required>
          <el-input v-model="deliverForm.deliveryUrl" placeholder="输入作品下载/查看链接" />
        </el-form-item>
        <el-form-item label="交付说明">
          <el-input v-model="deliverForm.deliveryNote" type="textarea" :rows="3" placeholder="描述交付内容（可选）" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeliver" :loading="submitting">确认交付</el-button>
      </template>
    </el-dialog>

    <!-- 画师报价对话框 -->
    <el-dialog v-model="quoteDialogVisible" title="报价" width="520px" :close-on-click-modal="false">
      <el-form label-position="top" :model="quoteForm">
        <el-form-item label="报价金额（元）" required>
          <el-input-number v-model="quoteForm.totalAmount" :min="1" :max="999999" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="定金比例">
          <el-slider v-model="quoteForm.depositRatio" :min="0.1" :max="1" :step="0.1" :format-tooltip="v => (v * 100).toFixed(0) + '%'" />
          <div class="deposit-preview" v-if="quoteForm.totalAmount">
            定金: ¥{{ (quoteForm.totalAmount * quoteForm.depositRatio).toFixed(2) }} / 尾款: ¥{{ (quoteForm.totalAmount * (1 - quoteForm.depositRatio)).toFixed(2) }}
          </div>
        </el-form-item>
        <el-form-item label="预计完成天数">
          <el-input-number v-model="quoteForm.estimatedDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item label="允许修改次数">
          <el-input-number v-model="quoteForm.revisionsAllowed" :min="0" :max="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报价说明">
          <el-input v-model="quoteForm.quoteNote" type="textarea" :rows="3" placeholder="报价说明、注意事项等（可选）" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quoteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleQuote" :loading="submitting">确认报价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getMyCommissions, rejectCommission, quoteCommission,
  startWork, deliverWork, requestRevision, cancelCommission
} from '@/api/commission'
import { createPayment } from '@/api/payment'
import { createOrGetConversation } from '@/api/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeTab = ref('client')
const commissions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const clientTotal = ref(0)
const artistTotal = ref(0)
const currentStatus = ref('')
const submitting = ref(false)

// 交付对话框
const deliverDialogVisible = ref(false)
const deliverForm = ref({ deliveryUrl: '', deliveryNote: '' })
const deliverTarget = ref(null)

// 报价对话框
const quoteDialogVisible = ref(false)
const quoteTarget = ref(null)
const quoteForm = ref({
  totalAmount: 500,
  depositRatio: 0.3,
  estimatedDays: 14,
  revisionsAllowed: 3,
  quoteNote: ''
})

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待报价', value: 'PENDING' },
  { label: '已报价', value: 'QUOTED' },
  { label: '已付定金', value: 'DEPOSIT_PAID' },
  { label: '创作中', value: 'IN_PROGRESS' },
  { label: '已交付', value: 'DELIVERED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const statusTextMap = {
  PENDING: '待报价',
  QUOTED: '已报价',
  DEPOSIT_PAID: '已付定金',
  IN_PROGRESS: '创作中',
  DELIVERED: '已交付',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REJECTED: '已拒绝'
}

const isArtist = computed(() => {
  return userStore.user?.role === 'ARTIST'
})

onMounted(() => {
  loadCommissions()
  loadCounts()
})

async function loadCommissions() {
  loading.value = true
  try {
    const params = {
      role: activeTab.value,
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (currentStatus.value) params.status = currentStatus.value

    const res = await getMyCommissions(params)
    if (res.code === 200 && res.data) {
      commissions.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } catch (e) {
    console.error('加载约稿列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadCounts() {
  try {
    const clientRes = await getMyCommissions({ role: 'client', page: 0, size: 1 })
    if (clientRes.code === 200 && clientRes.data) {
      clientTotal.value = clientRes.data.totalElements || 0
    }
    if (isArtist.value) {
      const artistRes = await getMyCommissions({ role: 'artist', page: 0, size: 1 })
      if (artistRes.code === 200 && artistRes.data) {
        artistTotal.value = artistRes.data.totalElements || 0
      }
    }
  } catch {
    // 忽略
  }
}

function switchTab(tab) {
  activeTab.value = tab
  currentPage.value = 1
  currentStatus.value = ''
  loadCommissions()
}

function filterByStatus(status) {
  currentStatus.value = status
  currentPage.value = 1
  loadCommissions()
}

function getOtherName(c) {
  return activeTab.value === 'client' ? c.artistName : c.clientName
}
function getOtherAvatar(c) {
  return activeTab.value === 'client' ? c.artistAvatar : c.clientAvatar
}
function getStatusText(status) {
  return statusTextMap[status] || status
}

function goDetail(c) {
  router.push(`/commission/${c.id}`)
}

// === 支付相关 ===

async function handlePayDeposit(c) {
  try {
    await ElMessageBox.confirm(
      `确认接受画师报价并通过支付宝支付定金 ¥${c.depositAmount}？`,
      '支付定金',
      { confirmButtonText: '去支付', cancelButtonText: '取消', type: 'info' }
    )
    submitting.value = true
    const res = await createPayment({ commissionId: c.id, paymentType: 'DEPOSIT' })
    if (res.code === 200 && res.data) {
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '创建支付订单失败')
    }
  } catch {
    // 取消操作
  } finally {
    submitting.value = false
  }
}

async function handlePayFinal(c) {
  const finalAmount = (c.totalAmount - c.depositAmount).toFixed(2)
  try {
    await ElMessageBox.confirm(
      `确认收到作品并通过支付宝支付尾款 ¥${finalAmount}？\n支付完成后约稿将自动完成。`,
      '确认收货并支付尾款',
      { confirmButtonText: '去支付', cancelButtonText: '取消', type: 'info' }
    )
    submitting.value = true
    const res = await createPayment({ commissionId: c.id, paymentType: 'FINAL_PAYMENT' })
    if (res.code === 200 && res.data) {
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '创建支付订单失败')
    }
  } catch {
    // 取消
  } finally {
    submitting.value = false
  }
}

function submitAlipayForm(formHtml) {
  // 支付宝返回的是一段带 <form> 的 HTML，插入页面后自动提交跳转到支付宝
  const div = document.createElement('div')
  div.innerHTML = formHtml
  document.body.appendChild(div)
  const form = div.querySelector('form')
  if (form) {
    form.submit()
  } else {
    ElMessage.error('支付表单解析失败，请重试')
    document.body.removeChild(div)
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
  quoteDialogVisible.value = true
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
      ElMessage.success('报价成功，等待委托方确认')
      quoteDialogVisible.value = false
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

// === 其他操作 ===

async function handleReject(c) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝约稿', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因...',
      type: 'warning'
    })
    const res = await rejectCommission(c.id, value)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    // 取消
  }
}

async function handleStart(c) {
  try {
    await ElMessageBox.confirm('确认开始创作？', '开始创作', { type: 'info' })
    const res = await startWork(c.id)
    if (res.code === 200) {
      ElMessage.success('已开始创作')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

function showDeliverDialog(c) {
  deliverTarget.value = c
  deliverForm.value = { deliveryUrl: '', deliveryNote: '' }
  deliverDialogVisible.value = true
}

async function handleDeliver() {
  if (!deliverForm.value.deliveryUrl.trim()) {
    ElMessage.warning('请输入作品链接')
    return
  }
  submitting.value = true
  try {
    const res = await deliverWork(deliverTarget.value.id, deliverForm.value)
    if (res.code === 200) {
      ElMessage.success('作品已交付')
      deliverDialogVisible.value = false
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

async function handleRevision(c) {
  try {
    await ElMessageBox.confirm(
      `确认申请修改？剩余 ${c.revisionsAllowed - c.revisionsUsed} 次修改机会。`,
      '请求修改',
      { type: 'info' }
    )
    const res = await requestRevision(c.id)
    if (res.code === 200) {
      ElMessage.success('已申请修改')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

async function handleCancel(c) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因（可选）', '取消约稿', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '取消原因...',
      type: 'warning'
    })
    const res = await cancelCommission(c.id, value)
    if (res.code === 200) {
      ElMessage.success('约稿已取消')
      loadCommissions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

async function goChat(c) {
  const targetId = activeTab.value === 'client' ? c.artistId : c.clientId
  try {
    const res = await createOrGetConversation(targetId)
    if (res.code === 200 && res.data) {
      router.push(`/chat/${res.data.id}`)
    }
  } catch {
    ElMessage.error('打开对话失败')
  }
}

function formatDate(d) {
  if (!d) return '-'
  return d.substring(0, 10)
}
</script>

<style scoped>
.commission-page {
  min-height: calc(100vh - 64px);
  background: #f5f5f5;
  padding: 24px;
}
.commission-container {
  max-width: 900px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}
.page-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* Tabs */
.tabs-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 10px;
  padding: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.tab-btn.active {
  background: #0096FA;
  color: #fff;
}
.tab-count {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.1);
}
.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

/* 状态筛选 */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.filter-btn {
  padding: 6px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-btn.active {
  background: #0096FA;
  color: #fff;
  border-color: #0096FA;
}
.filter-btn:hover:not(.active) {
  border-color: #0096FA;
  color: #0096FA;
}

/* 卡片列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.commission-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s;
  cursor: pointer;
}
.commission-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.card-role {
  font-size: 12px;
  color: #999;
  display: block;
}
.card-target {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}
.card-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 报价信息 */
.quote-info {
  background: #FFF8E1;
  border: 1px solid #FFE082;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.quote-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #F57C00;
  margin-bottom: 10px;
}
.quote-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.quote-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.quote-label {
  font-size: 12px;
  color: #999;
}
.quote-value {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.quote-value.price {
  color: #F57C00;
  font-size: 18px;
}
.quote-note {
  flex-basis: 100%;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-top: 4px;
}

/* 信息行 */
.card-info-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding: 12px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 12px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.info-label {
  font-size: 12px;
  color: #999;
}
.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.info-value.price {
  color: #0096FA;
  font-size: 16px;
}
.pay-status {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
}
.pay-status.paid {
  background: #e8f5e9;
  color: #4caf50;
}
.pay-status.unpaid {
  background: #fff3e0;
  color: #ff9800;
}

/* 交付信息 */
.delivery-info {
  padding: 10px 14px;
  background: #f0f8ff;
  border-radius: 8px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.delivery-label {
  font-size: 13px;
  color: #666;
}
.delivery-link {
  color: #0096FA;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}
.delivery-link:hover { text-decoration: underline; }
.delivery-note {
  font-size: 13px;
  color: #999;
  flex-basis: 100%;
}

/* 等待提示 */
.waiting-hint {
  font-size: 13px;
  color: #FF9800;
  font-weight: 500;
  padding: 6px 14px;
  background: #FFF3E0;
  border-radius: 20px;
}

/* 定金预览 */
.deposit-preview {
  font-size: 13px;
  color: #666;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

/* 状态徽章 */
.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
}
.status-pending { background: #fff3e0; color: #ff9800; }
.status-quoted { background: #E8F5E9; color: #4CAF50; }
.status-deposit_paid { background: #e3f2fd; color: #2196f3; }
.status-in_progress { background: #e8f5e9; color: #4caf50; }
.status-delivered { background: #f3e5f5; color: #9c27b0; }
.status-completed { background: #e0f2f1; color: #009688; }
.status-cancelled { background: #fafafa; color: #999; }
.status-rejected { background: #ffebee; color: #f44336; }

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.action-btn {
  padding: 7px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.action-btn.primary {
  background: #0096FA;
  color: #fff;
}
.action-btn.primary:hover { background: #0080dd; }
.action-btn.outline {
  background: #fff;
  color: #0096FA;
  border: 1px solid #0096FA;
}
.action-btn.outline:hover { background: #e8f4ff; }
.action-btn.danger-text {
  background: none;
  color: #f44336;
}
.action-btn.danger-text:hover { background: #ffebee; }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
.empty-state p {
  font-size: 15px;
  margin: 16px 0 8px;
}
.empty-hint {
  font-size: 13px;
  color: #bbb;
}

/* 加载 */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 60px;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #0096FA;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 分页 */
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .commission-page { padding: 12px; }
  .card-info-row { gap: 12px; }
}
</style>
