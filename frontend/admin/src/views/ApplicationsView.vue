<template>
  <div class="applications-page">
    <div class="page-header-bar">
      <h2>画师审核</h2>
      <div class="header-stats">
        <div class="stat-badge pending" v-if="pendingCount > 0">
          <span class="stat-badge-dot"></span>
          <span>{{ pendingCount }} 条待审核</span>
        </div>
        <el-tag v-else type="success" effect="plain" size="large">暂无待审核</el-tag>
      </div>
    </div>

    <!-- 状态标签页 + 筛选 -->
    <div class="filter-bar">
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs" :key="tab.value"
          :class="['status-tab', { active: statusFilter === tab.value }]"
          @click="statusFilter = tab.value; handleSearch()"
        >
          {{ tab.label }}
          <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
        </button>
      </div>
      <div style="flex: 1" />
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名..."
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 申请列表 - 卡片式 -->
    <div class="app-list" v-loading="loading">
      <div v-if="applications.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无申请记录</p>
      </div>

      <div
        v-for="app in applications"
        :key="app.id"
        :class="['app-card', { 'app-card--pending': app.status === 'PENDING' }]"
        @click="showDetail(app)"
      >
        <div class="app-card-left">
          <div class="app-avatar">
            {{ (app.username || 'U').charAt(0).toUpperCase() }}
          </div>
          <div class="app-info">
            <div class="app-info-header">
              <span class="app-username">{{ app.username || '用户' + app.userId }}</span>
              <el-tag :type="statusTagType(app.status)" size="small" class="app-status-tag">
                {{ statusText(app.status) }}
              </el-tag>
            </div>
            <div class="app-desc">{{ app.description || '暂无申请说明' }}</div>
            <div class="app-meta">
              <span class="meta-item">
                <el-icon size="13"><User /></el-icon>
                ID: {{ app.userId }}
              </span>
              <span class="meta-item" v-if="app.specialties?.length">
                擅长：{{ app.specialties.join(' / ') }}
              </span>
              <span class="meta-item">
                <el-icon size="13"><Clock /></el-icon>
                {{ formatDate(app.createdAt) }}
              </span>
              <a v-if="app.portfolioUrl" :href="app.portfolioUrl" target="_blank" class="meta-link" @click.stop>
                <el-icon size="13"><Link /></el-icon>
                查看作品集
              </a>
            </div>
          </div>
        </div>
        <div class="app-card-actions" v-if="app.status === 'PENDING'" @click.stop>
          <el-button type="success" size="small" round @click="handleReview(app, 'APPROVED')">
            <el-icon style="margin-right: 4px"><Check /></el-icon>通过
          </el-button>
          <el-button type="danger" size="small" round plain @click="handleReview(app, 'REJECTED')">
            <el-icon style="margin-right: 4px"><Close /></el-icon>拒绝
          </el-button>
        </div>
        <div class="app-card-result" v-else>
          <span v-if="app.status === 'APPROVED'" class="result-approved">✓ 已通过</span>
          <span v-else class="result-rejected">✕ 已拒绝</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadApplications"
        @size-change="loadApplications"
        background
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="" width="620px" destroy-on-close class="detail-dialog">
      <template v-if="currentApp">
        <div class="detail-header">
          <div class="detail-avatar">
            {{ (currentApp.username || 'U').charAt(0).toUpperCase() }}
          </div>
          <div class="detail-header-info">
            <h3>{{ currentApp.username || '用户' + currentApp.userId }}</h3>
            <el-tag :type="statusTagType(currentApp.status)" size="small">{{ statusText(currentApp.status) }}</el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="detail-section-title">基本信息</h4>
          <div class="detail-grid">
            <div class="detail-field">
              <span class="detail-label">申请ID</span>
              <span class="detail-value">#{{ currentApp.id }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">用户ID</span>
              <span class="detail-value">{{ currentApp.userId }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">申请时间</span>
              <span class="detail-value">{{ formatDate(currentApp.createdAt) }}</span>
            </div>
            <div class="detail-field" v-if="currentApp.portfolioUrl">
              <span class="detail-label">作品集</span>
              <a :href="currentApp.portfolioUrl" target="_blank" class="detail-link">
                {{ currentApp.portfolioUrl }}
              </a>
            </div>
            <div class="detail-field" v-if="currentApp.specialties?.length">
              <span class="detail-label">擅长领域</span>
              <span class="detail-value">{{ currentApp.specialties.join(' / ') }}</span>
            </div>
            <div class="detail-field" v-if="currentApp.contactInfo">
              <span class="detail-label">联系方式</span>
              <span class="detail-value">{{ currentApp.contactInfo }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="detail-section-title">申请理由</h4>
          <div class="detail-content-box">
            {{ currentApp.description || '暂无申请说明' }}
          </div>
        </div>

        <template v-if="currentApp.status !== 'PENDING'">
          <div class="detail-section">
            <h4 class="detail-section-title">审核结果</h4>
            <div class="detail-grid">
              <div class="detail-field">
                <span class="detail-label">审核时间</span>
                <span class="detail-value">{{ formatDate(currentApp.reviewedAt) }}</span>
              </div>
            </div>
            <div class="detail-content-box" v-if="currentApp.reviewComment" style="margin-top: 12px">
              <span class="detail-label" style="display: block; margin-bottom: 6px">审核意见</span>
              {{ currentApp.reviewComment }}
            </div>
          </div>
        </template>
      </template>
      <template #footer>
        <div class="dialog-footer">
          <template v-if="currentApp?.status === 'PENDING'">
            <el-button type="success" round @click="handleReview(currentApp, 'APPROVED')">
              <el-icon style="margin-right: 4px"><Check /></el-icon>通过申请
            </el-button>
            <el-button type="danger" round plain @click="handleReview(currentApp, 'REJECTED')">
              <el-icon style="margin-right: 4px"><Close /></el-icon>拒绝申请
            </el-button>
          </template>
          <el-button round @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝原因 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="440px">
      <div class="reject-hint">
        <el-icon size="16" color="#ef4444"><WarningFilled /></el-icon>
        <span>拒绝后，用户将收到通知并可重新提交申请</span>
      </div>
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因，便于用户了解改进方向..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" round @click="confirmReject" :loading="reviewLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User, Clock, Link, Check, Close, WarningFilled } from '@element-plus/icons-vue'
import { getApplications, getPendingApplications, reviewApplication } from '../api/artist'

const applications = ref([])
const loading = ref(false)
const reviewLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref('')
const searchKeyword = ref('')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentReviewApp = ref(null)
const detailDialogVisible = ref(false)
const currentApp = ref(null)

// 从 AdminLayout 注入共享的待审数量
const pendingCount = inject('pendingApplicationCount', ref(0))
const refreshParentPendingCount = inject('refreshPendingCount', () => {})

const statusTabs = computed(() => [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING', count: pendingCount.value },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
])

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '—'
const statusText = (status) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status)
const statusTagType = (status) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[status] || 'info')

const showDetail = (row) => { currentApp.value = row; detailDialogVisible.value = true }

const loadApplications = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getApplications(params)
    const data = res.data
    applications.value = data?.records || data?.items || data?.content || []
    total.value = data?.total || data?.totalElements || 0
  } catch (e) { console.error('加载申请列表失败:', e) }
  finally { loading.value = false }
}

const loadPendingCount = async () => {
  // 刷新共享的待审数量（更新侧栏 badge）
  await refreshParentPendingCount()
}

const handleReview = async (app, status) => {
  if (status === 'REJECTED') {
    currentReviewApp.value = app
    rejectReason.value = ''
    rejectDialogVisible.value = true
    return
  }
  try {
    await ElMessageBox.confirm(`确定要通过用户 "${app.username || app.userId}" 的画师申请吗？`, '确认通过', { type: 'success', confirmButtonText: '确认通过', cancelButtonText: '取消' })
    reviewLoading.value = true
    await reviewApplication(app.id, { approved: true })
    ElMessage.success('已通过申请')
    detailDialogVisible.value = false
    loadApplications()
    loadPendingCount()
  } catch (e) { if (e !== 'cancel') console.error('审核失败:', e) }
  finally { reviewLoading.value = false }
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入拒绝原因'); return }
  reviewLoading.value = true
  try {
    await reviewApplication(currentReviewApp.value.id, { approved: false, reviewComment: rejectReason.value.trim() })
    ElMessage.success('已拒绝申请')
    rejectDialogVisible.value = false
    detailDialogVisible.value = false
    loadApplications()
    loadPendingCount()
  } catch (e) { console.error('拒绝失败:', e) }
  finally { reviewLoading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadApplications() }

onMounted(() => {
  loadApplications()
  loadPendingCount()
})
</script>

<style scoped>
.applications-page { width: 100%; }

/* 头部统计 */
.header-stats { display: flex; align-items: center; gap: 12px; }

.stat-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
.stat-badge.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}
.stat-badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f59e0b;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* 状态标签页 */
.status-tabs {
  display: flex;
  gap: 4px;
}
.status-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--c-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.status-tab:hover {
  background: var(--c-primary-bg);
  color: var(--c-primary);
}
.status-tab.active {
  background: var(--c-primary);
  color: #fff;
}
.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 5px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255,255,255,0.25);
}
.status-tab:not(.active) .tab-count {
  background: var(--c-primary-bg);
  color: var(--c-primary);
}

/* 申请列表 */
.app-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
}

.app-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}
.app-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--c-border);
  transform: translateY(-1px);
}
.app-card--pending {
  border-left: 3px solid #f59e0b;
}

.app-card-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.app-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-hover));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.app-info {
  flex: 1;
  min-width: 0;
}

.app-info-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.app-username {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-text);
}

.app-desc {
  font-size: 13px;
  color: var(--c-text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 500px;
  margin-bottom: 8px;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--c-text-muted);
}

.meta-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--c-primary);
  text-decoration: none;
  font-weight: 500;
}
.meta-link:hover { text-decoration: underline; }

.app-card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

.app-card-result {
  flex-shrink: 0;
  margin-left: 16px;
  font-size: 13px;
  font-weight: 600;
}
.result-approved { color: var(--c-success); }
.result-rejected { color: var(--c-danger); }

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--c-text-muted);
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state p { font-size: 14px; }

/* 分页 */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 0;
  border-top: none;
}

/* 详情弹窗 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--c-border-light);
}

.detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-hover));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
}

.detail-header-info h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 6px;
}

.detail-section {
  margin-bottom: 22px;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.detail-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--c-text-muted);
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: var(--c-text);
  font-weight: 500;
}

.detail-link {
  font-size: 14px;
  color: var(--c-primary);
  text-decoration: none;
  word-break: break-all;
}
.detail-link:hover { text-decoration: underline; }

.detail-content-box {
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--c-text);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 拒绝提示 */
.reject-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(239, 68, 68, 0.06);
  border-radius: var(--radius-sm);
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--c-text-secondary);
}

/* 弹窗覆盖 */
:deep(.detail-dialog .el-dialog__header) {
  display: none;
}
</style>
