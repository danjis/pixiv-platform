<template>
  <div class="feedback-page">
    <div class="page-header-bar">
      <h2>反馈管理</h2>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="loadFeedbacks">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
      </div>
    </div>

    <div class="table-wrapper">
      <el-table :data="feedbacks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.tagType || 'info'" size="small" effect="plain">
              {{ typeMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="联系方式" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ row.contactInfo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.tagType || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openReplyDialog(row)">
              {{ row.adminReply ? '查看' : '回复' }}
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

    <!-- 回复对话框 -->
    <el-dialog v-model="showReplyDialog" title="反馈详情与回复" width="560px" :close-on-click-modal="false">
      <div class="feedback-detail" v-if="currentFeedback">
        <div class="detail-row">
          <span class="detail-label">类型：</span>
          <el-tag :type="typeMap[currentFeedback.type]?.tagType || 'info'" size="small" effect="plain">
            {{ typeMap[currentFeedback.type]?.label || currentFeedback.type }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">用户ID：</span>
          <span>{{ currentFeedback.userId }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">标题：</span>
          <span class="detail-title">{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">内容：</span>
        </div>
        <div class="detail-content">{{ currentFeedback.content }}</div>
        <div class="detail-row" v-if="currentFeedback.contactInfo">
          <span class="detail-label">联系方式：</span>
          <span>{{ currentFeedback.contactInfo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">提交时间：</span>
          <span class="text-muted">{{ formatDate(currentFeedback.createdAt) }}</span>
        </div>

        <el-divider />

        <div class="reply-section">
          <div class="detail-label" style="margin-bottom: 8px">管理员回复：</div>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容..."
            maxlength="2000"
            show-word-limit
          />
          <div style="margin-top: 12px">
            <span class="detail-label">更新状态：</span>
            <el-select v-model="replyStatus" style="width: 140px; margin-left: 8px">
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReply" :loading="replying">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackList, replyFeedback } from '@/api/feedback'

const loading = ref(false)
const feedbacks = ref([])
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)
const statusFilter = ref('')

const showReplyDialog = ref(false)
const currentFeedback = ref(null)
const replyContent = ref('')
const replyStatus = ref('RESOLVED')
const replying = ref(false)

const typeMap = {
  CONSULTATION: { label: '咨询', tagType: '' },
  BUG_REPORT: { label: 'Bug', tagType: 'danger' },
  FEATURE_REQUEST: { label: '建议', tagType: 'success' },
  COMPLAINT: { label: '投诉', tagType: 'warning' },
  OTHER: { label: '其他', tagType: 'info' }
}

const statusMap = {
  PENDING: { label: '待处理', tagType: 'info' },
  PROCESSING: { label: '处理中', tagType: 'warning' },
  RESOLVED: { label: '已解决', tagType: 'success' },
  CLOSED: { label: '已关闭', tagType: '' }
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getFeedbackList(params)
    if (res.code === 200) {
      feedbacks.value = res.data?.records || res.data?.content || []
      total.value = res.data?.total || res.data?.totalElements || 0
    }
  } catch {
    feedbacks.value = []
  } finally {
    loading.value = false
  }
}

const openReplyDialog = (row) => {
  currentFeedback.value = row
  replyContent.value = row.adminReply || ''
  replyStatus.value = row.status === 'PENDING' ? 'RESOLVED' : row.status
  showReplyDialog.value = true
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    const res = await replyFeedback(currentFeedback.value.id, {
      reply: replyContent.value,
      status: replyStatus.value
    })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      showReplyDialog.value = false
      loadFeedbacks()
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch {
    ElMessage.error('回复失败')
  } finally {
    replying.value = false
  }
}

const formatDate = (date) => {
  if (!date) return '—'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => loadFeedbacks())
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
}

.page-header-bar h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary, #1a1a2e);
}

.table-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.text-muted {
  color: #999;
  font-size: 13px;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  padding: 16px;
}

/* 反馈详情 */
.feedback-detail {
  padding: 4px 0;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.detail-label {
  color: #999;
  font-size: 13px;
  min-width: 70px;
  flex-shrink: 0;
}

.detail-title {
  font-weight: 600;
  color: #333;
}

.detail-content {
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 14px;
  color: #444;
  line-height: 1.7;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.reply-section {
  margin-top: 4px;
}
</style>
