<template>
  <div class="feedback-page">
    <div class="page-header-bar">
      <h2>反馈与售后</h2>
      <div class="header-actions">
        <el-select
          v-model="typeFilter"
          placeholder="类型筛选"
          clearable
          style="width: 160px"
          @change="handleFilterChange"
        >
          <el-option label="全部类型" value="" />
          <el-option label="售后工单" value="AFTER_SALE" />
          <el-option label="咨询" value="CONSULTATION" />
          <el-option label="Bug反馈" value="BUG_REPORT" />
          <el-option label="功能建议" value="FEATURE_REQUEST" />
          <el-option label="投诉" value="COMPLAINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 140px"
          @change="handleFilterChange"
        >
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="feedbacks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.tagType || 'info'" size="small" effect="plain">
              {{ typeMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="售后动作" width="120">
          <template #default="{ row }">
            <span v-if="row.type === 'AFTER_SALE'" class="text-strong">
              {{ actionMap[row.requestedAction] || row.requestedAction || '--' }}
            </span>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.tagType || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理结果" width="140">
          <template #default="{ row }">
            <span v-if="row.type === 'AFTER_SALE'">
              {{ resolutionMap[row.resolution] || row.resolution || '--' }}
            </span>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openDialog(row)">
              {{ row.type === 'AFTER_SALE' ? '处理' : (row.adminReply ? '查看' : '回复') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadFeedbacks"
        />
      </div>
    </div>

    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="640px"
      :close-on-click-modal="false"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <div class="detail-row">
          <span class="detail-label">类型</span>
          <el-tag :type="typeMap[currentFeedback.type]?.tagType || 'info'" size="small" effect="plain">
            {{ typeMap[currentFeedback.type]?.label || currentFeedback.type }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">用户ID</span>
          <span>{{ currentFeedback.userId }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">标题</span>
          <span class="detail-title">{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">提交时间</span>
          <span class="text-muted">{{ formatDate(currentFeedback.createdAt) }}</span>
        </div>

        <template v-if="isAfterSale">
          <div class="detail-row">
            <span class="detail-label">售后动作</span>
            <span>{{ actionMap[currentFeedback.requestedAction] || currentFeedback.requestedAction || '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">约稿ID</span>
            <span>{{ currentFeedback.commissionId || '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付ID</span>
            <span>{{ currentFeedback.paymentId || '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付类型</span>
            <span>{{ paymentTypeMap[currentFeedback.paymentType] || currentFeedback.paymentType || '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付金额</span>
            <span>{{ currentFeedback.paymentAmount ?? '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">当前状态</span>
            <el-tag :type="statusMap[currentFeedback.status]?.tagType || 'info'" size="small">
              {{ statusMap[currentFeedback.status]?.label || currentFeedback.status }}
            </el-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">处理结果</span>
            <span>{{ resolutionMap[currentFeedback.resolution] || currentFeedback.resolution || '--' }}</span>
          </div>
        </template>

        <div class="detail-block">
          <div class="detail-label block-label">用户说明</div>
          <div class="detail-content">{{ currentFeedback.content || '暂无内容' }}</div>
        </div>

        <div v-if="currentFeedback.adminReply" class="detail-block">
          <div class="detail-label block-label">历史回复</div>
          <div class="detail-content admin-reply">{{ currentFeedback.adminReply }}</div>
        </div>

        <el-divider />

        <div v-if="isAfterSale" class="reply-section">
          <div class="detail-label section-label">处理动作</div>
          <el-select v-model="afterSaleAction" style="width: 220px">
            <el-option label="转入处理中" value="PROCESSING" />
            <el-option label="驳回申请" value="REJECT" />
            <el-option label="审核通过并处理" value="APPROVE_REFUND" />
            <el-option label="关闭工单" value="CLOSE" />
          </el-select>

          <div v-if="afterSaleAction === 'APPROVE_REFUND'" class="refund-mode">
            <div class="detail-label section-label">退款模式</div>
            <el-select v-model="refundMode" style="width: 220px">
              <el-option
                v-for="item in refundModeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>

        <div v-else class="reply-section">
          <div class="detail-label section-label">更新状态</div>
          <el-select v-model="replyStatus" style="width: 220px">
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </div>

        <div class="reply-section">
          <div class="detail-label section-label">管理员回复</div>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入处理说明，建议写清审核依据和处理结果"
            maxlength="2000"
            show-word-limit
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isAfterSale ? '提交处理' : '提交回复' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackList, processAfterSale, replyFeedback } from '@/api/feedback'

const loading = ref(false)
const feedbacks = ref([])
const currentPage = ref(1)
const pageSize = 15
const total = ref(0)
const statusFilter = ref('')
const typeFilter = ref('')

const showDialog = ref(false)
const currentFeedback = ref(null)
const replyContent = ref('')
const replyStatus = ref('RESOLVED')
const afterSaleAction = ref('PROCESSING')
const refundMode = ref('FULL')
const submitting = ref(false)

const typeMap = {
  AFTER_SALE: { label: '售后工单', tagType: 'warning' },
  CONSULTATION: { label: '咨询', tagType: '' },
  BUG_REPORT: { label: 'Bug反馈', tagType: 'danger' },
  FEATURE_REQUEST: { label: '功能建议', tagType: 'success' },
  COMPLAINT: { label: '投诉', tagType: 'warning' },
  OTHER: { label: '其他', tagType: 'info' }
}

const statusMap = {
  PENDING: { label: '待处理', tagType: 'info' },
  PROCESSING: { label: '处理中', tagType: 'warning' },
  RESOLVED: { label: '已解决', tagType: 'success' },
  CLOSED: { label: '已关闭', tagType: '' }
}

const actionMap = {
  PLATFORM_INTERVENTION: '平台介入',
  REFUND_REVIEW: '退款审核'
}

const resolutionMap = {
  NONE: '未处理',
  REFUND_EXECUTED: '已执行退款',
  REFUND_REJECTED: '未通过退款审核',
  INTERVENTION_CLOSED: '工单已关闭'
}

const paymentTypeMap = {
  DEPOSIT: '定金',
  FINAL_PAYMENT: '尾款'
}

const isAfterSale = computed(() => currentFeedback.value?.type === 'AFTER_SALE')

const dialogTitle = computed(() => {
  return isAfterSale.value ? '售后工单处理' : '反馈详情与回复'
})

const refundModeOptions = computed(() => {
  if (currentFeedback.value?.paymentType === 'FINAL_PAYMENT') {
    return [
      { label: '整单取消并退款', value: 'FULL' },
      { label: '仅退尾款', value: 'FINAL_ONLY' }
    ]
  }
  return [{ label: '整单取消并退款', value: 'FULL' }]
})

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize
    }
    if (statusFilter.value) params.status = statusFilter.value
    if (typeFilter.value) params.type = typeFilter.value

    const res = await getFeedbackList(params)
    if (res.code === 200) {
      feedbacks.value = res.data?.records || []
      total.value = res.data?.total || 0
    } else {
      feedbacks.value = []
      total.value = 0
    }
  } catch {
    feedbacks.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadFeedbacks()
}

const openDialog = (row) => {
  currentFeedback.value = row
  replyContent.value = row.adminReply || ''
  replyStatus.value = row.status === 'PENDING' ? 'RESOLVED' : (row.status || 'RESOLVED')
  afterSaleAction.value = row.status === 'PENDING' ? 'PROCESSING' : 'CLOSE'
  refundMode.value = row.paymentType === 'FINAL_PAYMENT' ? 'FINAL_ONLY' : 'FULL'
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!currentFeedback.value) {
    return
  }

  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入处理说明')
    return
  }

  submitting.value = true
  try {
    let res
    if (isAfterSale.value) {
      res = await processAfterSale(currentFeedback.value.id, {
        action: afterSaleAction.value,
        reply: replyContent.value.trim(),
        refundMode: afterSaleAction.value === 'APPROVE_REFUND' ? refundMode.value : undefined
      })
    } else {
      res = await replyFeedback(currentFeedback.value.id, {
        reply: replyContent.value.trim(),
        status: replyStatus.value
      })
    }

    if (res.code === 200) {
      ElMessage.success(isAfterSale.value ? '售后工单处理成功' : '反馈回复成功')
      showDialog.value = false
      loadFeedbacks()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error(isAfterSale.value ? '处理售后工单失败' : '回复反馈失败')
  } finally {
    submitting.value = false
  }
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadFeedbacks)
</script>

<style scoped>
.feedback-page {
  padding: 0;
}

.page-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.page-header-bar h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1a1a2e);
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.feedback-detail {
  padding: 4px 0;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.detail-label {
  min-width: 84px;
  color: #909399;
  flex-shrink: 0;
}

.detail-title,
.text-strong {
  font-weight: 600;
  color: #303133;
}

.detail-block {
  margin-bottom: 16px;
}

.block-label,
.section-label {
  display: block;
  margin-bottom: 8px;
}

.detail-content {
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
  color: #475569;
  white-space: pre-wrap;
  line-height: 1.7;
}

.admin-reply {
  background: #f0f9ff;
  color: #0f172a;
}

.reply-section {
  margin-top: 12px;
}

.refund-mode {
  margin-top: 12px;
}

.text-muted {
  color: #909399;
}

@media (max-width: 768px) {
  .page-header-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    width: 100%;
  }
}
</style>
