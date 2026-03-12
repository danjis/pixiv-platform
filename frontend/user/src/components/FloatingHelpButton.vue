<template>
  <!-- 悬浮帮助按钮 -->
  <div class="floating-help">
    <div class="help-btn" @click="togglePanel" :class="{ active: showPanel }">
      <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/>
        <path d="M11 5h2v6h-2zm0 8h2v2h-2z"/>
      </svg>
    </div>

    <!-- 展开面板 -->
    <transition name="help-panel">
      <div v-if="showPanel" class="help-panel">
        <div class="panel-header">
          <span>帮助与反馈</span>
          <el-icon class="close-icon" @click="showPanel = false"><Close /></el-icon>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-links">
          <div class="quick-item" @click="openFeedbackDialog('CONSULTATION')">
            <div class="quick-icon consultation">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>
            </div>
            <span>在线咨询</span>
          </div>
          <div class="quick-item" @click="openFeedbackDialog('BUG_REPORT')">
            <div class="quick-icon bug">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 8h-2.81c-.45-.78-1.07-1.45-1.82-1.96L17 4.41 15.59 3l-2.17 2.17C12.96 5.06 12.49 5 12 5c-.49 0-.96.06-1.41.17L8.41 3 7 4.41l1.62 1.63C7.88 6.55 7.26 7.22 6.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81c1.04 1.79 2.97 3 5.19 3s4.15-1.21 5.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8zm-6 8h-4v-2h4v2zm0-4h-4v-2h4v2z"/></svg>
            </div>
            <span>Bug反馈</span>
          </div>
          <div class="quick-item" @click="openFeedbackDialog('FEATURE_REQUEST')">
            <div class="quick-icon feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7zm2.85 11.1l-.85.6V16h-4v-2.3l-.85-.6C7.8 12.16 7 10.63 7 9c0-2.76 2.24-5 5-5s5 2.24 5 5c0 1.63-.8 3.16-2.15 4.1z"/></svg>
            </div>
            <span>功能建议</span>
          </div>
          <div class="quick-item" @click="showHistory = true; showPanel = false">
            <div class="quick-icon history">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            </div>
            <span>反馈记录</span>
          </div>
        </div>

        <div class="panel-footer">
          <p>工作时间：周一至周五 9:00-18:00</p>
          <p>邮箱：support@pixiv-platform.com</p>
        </div>
      </div>
    </transition>
  </div>

  <!-- 反馈对话框 -->
  <el-dialog
    v-model="showFeedbackDialog"
    :title="feedbackDialogTitle"
    width="500px"
    :close-on-click-modal="false"
    class="feedback-dialog"
  >
    <el-form :model="feedbackForm" :rules="rules" ref="feedbackFormRef" label-position="top">
      <el-form-item label="反馈类型" prop="type">
        <el-select v-model="feedbackForm.type" style="width: 100%">
          <el-option label="在线咨询" value="CONSULTATION" />
          <el-option label="Bug反馈" value="BUG_REPORT" />
          <el-option label="功能建议" value="FEATURE_REQUEST" />
          <el-option label="投诉" value="COMPLAINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="feedbackForm.title" placeholder="请简要描述您的问题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="详细描述" prop="content">
        <el-input
          v-model="feedbackForm.content"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您遇到的问题或建议..."
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="联系方式（选填）">
        <el-input v-model="feedbackForm.contactInfo" placeholder="邮箱或手机号，方便我们联系您" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showFeedbackDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">提交反馈</el-button>
    </template>
  </el-dialog>

  <!-- 反馈记录抽屉 -->
  <el-drawer v-model="showHistory" title="我的反馈记录" size="420px" direction="rtl">
    <div class="feedback-history">
      <div v-if="historyLoading" class="loading-placeholder">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="feedbackList.length === 0" class="empty-state">
        <el-empty description="暂无反馈记录" />
      </div>
      <div v-else class="feedback-list">
        <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
          <div class="feedback-head">
            <el-tag :type="typeTagMap[item.type]?.type || 'info'" size="small" effect="plain">
              {{ typeTagMap[item.type]?.label || item.type }}
            </el-tag>
            <el-tag :type="statusTagMap[item.status]?.type || 'info'" size="small">
              {{ statusTagMap[item.status]?.label || item.status }}
            </el-tag>
          </div>
          <h4 class="feedback-title">{{ item.title }}</h4>
          <p class="feedback-content">{{ item.content }}</p>
          <div class="feedback-time">{{ formatTime(item.createdAt) }}</div>
          <div v-if="item.adminReply" class="admin-reply">
            <div class="reply-label">客服回复：</div>
            <div class="reply-content">{{ item.adminReply }}</div>
            <div class="reply-time">{{ formatTime(item.repliedAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { submitFeedback, getMyFeedbacks } from '@/api/feedback'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const showPanel = ref(false)
const showFeedbackDialog = ref(false)
const showHistory = ref(false)
const submitting = ref(false)
const historyLoading = ref(false)
const feedbackList = ref([])
const feedbackFormRef = ref(null)

const feedbackForm = ref({
  type: 'CONSULTATION',
  title: '',
  content: '',
  contactInfo: ''
})

const feedbackDialogTitle = ref('提交反馈')

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const typeTagMap = {
  CONSULTATION: { label: '咨询', type: '' },
  BUG_REPORT: { label: 'Bug', type: 'danger' },
  FEATURE_REQUEST: { label: '建议', type: 'success' },
  COMPLAINT: { label: '投诉', type: 'warning' },
  OTHER: { label: '其他', type: 'info' }
}

const statusTagMap = {
  PENDING: { label: '待处理', type: 'info' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: 'success' },
  CLOSED: { label: '已关闭', type: '' }
}

const togglePanel = () => {
  showPanel.value = !showPanel.value
}

const openFeedbackDialog = (type) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再提交反馈')
    return
  }
  feedbackForm.value = { type, title: '', content: '', contactInfo: '' }
  const titles = {
    CONSULTATION: '在线咨询',
    BUG_REPORT: 'Bug反馈',
    FEATURE_REQUEST: '功能建议',
    COMPLAINT: '投诉',
    OTHER: '其他反馈'
  }
  feedbackDialogTitle.value = titles[type] || '提交反馈'
  showPanel.value = false
  showFeedbackDialog.value = true
}

const handleSubmit = async () => {
  if (!feedbackFormRef.value) return
  try {
    await feedbackFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await submitFeedback(feedbackForm.value)
    if (res.code === 200) {
      ElMessage.success('反馈提交成功，我们会尽快处理')
      showFeedbackDialog.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadHistory = async () => {
  if (!userStore.isAuthenticated) return
  historyLoading.value = true
  try {
    const res = await getMyFeedbacks({ page: 0, size: 20 })
    if (res.code === 200) {
      feedbackList.value = res.data?.records || res.data?.content || []
    }
  } catch {
    feedbackList.value = []
  } finally {
    historyLoading.value = false
  }
}

watch(showHistory, (val) => {
  if (val) loadHistory()
})

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.floating-help {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 2000;
}

.help-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0096fa, #0078d4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 120, 212, 0.35);
  transition: all 0.3s ease;
}

.help-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(0, 120, 212, 0.45);
}

.help-btn.active {
  background: linear-gradient(135deg, #0078d4, #005a9e);
}

/* 展开面板 */
.help-panel {
  position: absolute;
  bottom: 64px;
  right: 0;
  width: 280px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #0096fa, #0078d4);
  color: #fff;
}

.close-icon {
  cursor: pointer;
  font-size: 16px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.close-icon:hover {
  opacity: 1;
}

.quick-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  padding: 16px 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
  color: #333;
}

.quick-item:hover {
  background: #f5f7fa;
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-icon.consultation {
  background: #e6f4ff;
  color: #0096fa;
}

.quick-icon.bug {
  background: #fff1f0;
  color: #f56c6c;
}

.quick-icon.feature {
  background: #f0f9eb;
  color: #67c23a;
}

.quick-icon.history {
  background: #fdf6ec;
  color: #e6a23c;
}

.panel-footer {
  padding: 12px 18px;
  background: #f9fafb;
  border-top: 1px solid #f0f0f0;
}

.panel-footer p {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.8;
}

/* 面板动画 */
.help-panel-enter-active,
.help-panel-leave-active {
  transition: all 0.25s ease;
}

.help-panel-enter-from,
.help-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.95);
}

/* 反馈对话框 */
:deep(.feedback-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.feedback-dialog .el-dialog__body) {
  padding: 20px;
}

/* 反馈历史记录 */
.feedback-history {
  padding: 0 4px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  padding: 16px;
  border-radius: 10px;
  background: #fafbfc;
  border: 1px solid #eef0f4;
}

.feedback-head {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.feedback-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.feedback-content {
  margin: 0 0 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feedback-time {
  font-size: 12px;
  color: #aaa;
}

.admin-reply {
  margin-top: 12px;
  padding: 12px;
  background: #e6f4ff;
  border-radius: 8px;
  border-left: 3px solid #0096fa;
}

.reply-label {
  font-size: 12px;
  color: #0078d4;
  font-weight: 600;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: #333;
  line-height: 1.6;
}

.reply-time {
  font-size: 12px;
  color: #aaa;
  margin-top: 6px;
}

.loading-placeholder {
  padding: 16px;
}

.empty-state {
  padding: 40px 0;
}
</style>
