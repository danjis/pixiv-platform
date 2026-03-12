<template>
  <div class="payments-page">
    <div class="page-header-bar">
      <h2>支付管理</h2>
    </div>

    <!-- 退款规则说明卡片 -->
    <div class="refund-rules-card" v-if="showRules">
      <div class="rules-header">
        <span class="rules-title">退款规则说明</span>
        <el-button text size="small" @click="showRules = false">收起</el-button>
      </div>
      <div class="rules-content">
        <p><el-tag type="danger" size="small">核心规则</el-tag> 定金（DEPOSIT）一旦支付，默认不予退还</p>
        <p><el-tag type="warning" size="small">委托方取消</el-tag> 定金不退（委托方违约），已付尾款退还</p>
        <p><el-tag type="success" size="small">画师取消</el-tag> 全额退款（画师违约，含定金）</p>
        <p><el-tag type="primary" size="small">管理员介入</el-tag> 可选择性退款任意一笔支付（用于纠纷处理）</p>
      </div>
    </div>
    <div v-else style="margin-bottom: 12px">
      <el-button text type="info" size="small" @click="showRules = true">📋 查看退款规则</el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="payments" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="commissionId" label="约稿" width="80" />
        <el-table-column prop="payerId" label="付款方" width="80" />
        <el-table-column prop="payeeId" label="收款方" width="80" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="money">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.paymentType === 'DEPOSIT' ? 'primary' : 'warning'" size="small">
              {{ row.paymentType === 'DEPOSIT' ? '定金' : '尾款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="payStatusType(row.status)" size="small">{{ payStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column label="退款信息" width="160">
          <template #default="{ row }">
            <template v-if="row.status === 'REFUNDED'">
              <div class="text-muted" style="font-size: 12px">{{ formatDate(row.refundedAt) }}</div>
              <div class="text-muted" style="font-size: 12px" v-if="row.refundReason">{{ row.refundReason }}</div>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.paidAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PAID'"
              size="small"
              type="danger"
              text
              @click="handleRefund(row)"
            >退款</el-button>
            <el-tag v-if="row.status === 'REFUNDED'" type="info" size="small">已退</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="simple-pagination">
        <el-button :disabled="page <= 0" size="small" @click="page--; loadList()">上一页</el-button>
        <span class="page-info">第 {{ page + 1 }} 页</span>
        <el-button :disabled="payments.length < pageSize" size="small" @click="page++; loadList()">下一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPayments, adminRefund } from '@/api/commission'

const loading = ref(false)
const payments = ref([])
const page = ref(0)
const pageSize = 20
const showRules = ref(false)

function payStatusLabel(s) {
  return { PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款', CLOSED: '已关闭', FAILED: '支付失败' }[s] || s
}
function payStatusType(s) {
  return { PAID: 'success', PENDING: 'warning', REFUNDED: 'danger', CLOSED: 'info', FAILED: 'danger' }[s] || 'info'
}
function formatDate(str) {
  return str ? new Date(str).toLocaleString('zh-CN') : '—'
}

async function loadList() {
  loading.value = true
  try {
    const res = await getPayments({ page: page.value, size: pageSize })
    if (res.code === 200 && res.data) {
      payments.value = res.data
    }
  } catch {
    ElMessage.error('加载支付记录失败')
  } finally {
    loading.value = false
  }
}

async function handleRefund(row) {
  const isDeposit = row.paymentType === 'DEPOSIT'
  const typeLabel = isDeposit ? '定金' : '尾款'
  const warningMsg = isDeposit
    ? `⚠️ 您正在退还【定金】！按平台规则，定金通常不予退还。\n仅在画师违约等特殊情况下才应退还定金。\n\n约稿 #${row.commissionId} · ${typeLabel} · ¥${row.amount}`
    : `确认退还约稿 #${row.commissionId} 的【${typeLabel}】¥${row.amount}？`

  try {
    const { value: reason } = await ElMessageBox.prompt(
      warningMsg,
      isDeposit ? '⚠️ 定金退款（特殊操作）' : '退款操作',
      {
        confirmButtonText: '确认退款',
        cancelButtonText: '取消',
        inputPlaceholder: '退款原因（必填）',
        inputValidator: (val) => val && val.trim() ? true : '请填写退款原因',
        type: isDeposit ? 'error' : 'warning'
      }
    )
    const res = await adminRefund({ paymentId: row.id, reason: reason.trim() })
    if (res.code === 200) {
      ElMessage.success(`${typeLabel}退款成功`)
      loadList()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch { /* cancelled */ }
}

onMounted(() => { loadList() })
</script>

<style scoped>
.payments-page { width: 100%; }
.money { font-weight: 600; font-variant-numeric: tabular-nums; }
.text-muted { color: var(--c-text-secondary); font-size: 13px; }

.refund-rules-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  padding: 16px 20px;
  margin-bottom: 16px;
  border-left: 3px solid var(--c-primary);
}
.rules-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.rules-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--c-text);
}
.rules-content p {
  font-size: 13px;
  color: var(--c-text-secondary);
  margin: 6px 0;
  line-height: 1.6;
}
.rules-content .el-tag {
  margin-right: 6px;
}
</style>
