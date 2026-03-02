<template>
  <div class="applications-page">
    <h2 class="page-title">画师审核</h2>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 申请列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table
        :data="applications"
        stripe
        v-loading="loading"
        style="width: 100%"
        @row-click="showDetail"
        row-class-name="clickable-row"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="description" label="申请理由" show-overflow-tooltip />
        <el-table-column label="作品集链接" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.portfolioUrl" :href="row.portfolioUrl" target="_blank" class="link" @click.stop>
              {{ row.portfolioUrl }}
            </a>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click.stop="showDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" text size="small" @click.stop="handleReview(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" text size="small" @click.stop="handleReview(row, 'REJECTED')">拒绝</el-button>
            </template>
            <span v-else class="reviewed-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadApplications"
          @size-change="loadApplications"
        />
      </div>
    </el-card>

    <!-- 申请详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="600px" destroy-on-close>
      <template v-if="currentApp">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ currentApp.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentApp.status)" size="small">
              {{ statusText(currentApp.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentApp.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentApp.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ formatDate(currentApp.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="作品集链接" :span="2">
            <a v-if="currentApp.portfolioUrl" :href="currentApp.portfolioUrl" target="_blank" class="link">
              {{ currentApp.portfolioUrl }}
            </a>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            <div class="detail-text">{{ currentApp.description || '-' }}</div>
          </el-descriptions-item>
          <template v-if="currentApp.status !== 'PENDING'">
            <el-descriptions-item label="审核时间" :span="2">{{ formatDate(currentApp.reviewedAt) }}</el-descriptions-item>
            <el-descriptions-item label="审核意见" :span="2">
              <div class="detail-text">{{ currentApp.reviewComment || '-' }}</div>
            </el-descriptions-item>
          </template>
        </el-descriptions>
      </template>
      <template #footer>
        <template v-if="currentApp?.status === 'PENDING'">
          <el-button type="success" @click="handleReview(currentApp, 'APPROVED')">通过</el-button>
          <el-button type="danger" @click="handleReview(currentApp, 'REJECTED')">拒绝</el-button>
        </template>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="400px">
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="reviewLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplications, getPendingApplications, reviewApplication } from '../api/artist'

const applications = ref([])
const loading = ref(false)
const reviewLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref('')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentReviewApp = ref(null)
const detailDialogVisible = ref(false)
const currentApp = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const statusText = (status) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const showDetail = (row) => {
  currentApp.value = row
  detailDialogVisible.value = true
}

const loadApplications = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (statusFilter.value) params.status = statusFilter.value

    const res = await getApplications(params)
    const data = res.data
    applications.value = data?.records || data?.items || data?.content || []
    total.value = data?.total || data?.totalElements || 0
  } catch (e) {
    console.error('加载申请列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleReview = async (app, status) => {
  if (status === 'REJECTED') {
    currentReviewApp.value = app
    rejectReason.value = ''
    rejectDialogVisible.value = true
    return
  }

  // 通过
  try {
    await ElMessageBox.confirm(`确定要通过用户 "${app.username || app.userId}" 的画师申请吗？`, '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    reviewLoading.value = true
    await reviewApplication(app.id, { approved: true })
    ElMessage.success('已通过申请')
    detailDialogVisible.value = false
    loadApplications()
  } catch (e) {
    if (e !== 'cancel') console.error('审核失败:', e)
  } finally {
    reviewLoading.value = false
  }
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  reviewLoading.value = true
  try {
    await reviewApplication(currentReviewApp.value.id, {
      approved: false,
      reviewComment: rejectReason.value.trim()
    })
    ElMessage.success('已拒绝申请')
    rejectDialogVisible.value = false
    detailDialogVisible.value = false
    loadApplications()
  } catch (e) {
    console.error('拒绝失败:', e)
  } finally {
    reviewLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadApplications()
}

onMounted(loadApplications)
</script>

<style scoped>
.page-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 22px;
}

.filter-card {
  margin-bottom: 0;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.link {
  color: #409eff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.reviewed-text {
  color: #909399;
  font-size: 13px;
}

:deep(.clickable-row) {
  cursor: pointer;
}

:deep(.clickable-row:hover > td) {
  background-color: #ecf5ff !important;
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}
</style>
