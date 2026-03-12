<template>
  <div class="audit-logs-page">
    <div class="page-header-bar">
      <h2>审计日志</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="actionFilter" placeholder="操作类型筛选" clearable style="width: 200px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="通过画师申请" value="APPROVE_ARTIST_APPLICATION" />
        <el-option label="拒绝画师申请" value="REJECT_ARTIST_APPLICATION" />
        <el-option label="删除作品" value="DELETE_ARTWORK" />
        <el-option label="删除评论" value="DELETE_COMMENT" />
        <el-option label="封禁用户" value="BAN_USER" />
        <el-option label="解封用户" value="UNBAN_USER" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- 日志列表 -->
    <div class="table-wrapper">
      <el-table :data="logs" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="管理员" width="110">
          <template #default="{ row }">
            <span>{{ row.adminUsername || ('管理员' + row.adminId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.actionType)" size="small">{{ formatActionType(row.actionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ cleanDescription(row.description) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="110">
          <template #default="{ row }">
            <span>{{ formatTargetType(row.targetType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuditLogs } from '../api/stats'

const logs = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const actionFilter = ref('')

const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '—'

const actionTypeMap = {
  APPROVE_ARTIST_APPLICATION: '通过画师申请',
  REJECT_ARTIST_APPLICATION: '拒绝画师申请',
  DELETE_ARTWORK: '删除作品',
  DELETE_COMMENT: '删除评论',
  BAN_USER: '封禁用户',
  UNBAN_USER: '解封用户',
  OTHER: '其他操作'
}

const targetTypeMap = {
  ArtistApplication: '画师申请',
  Artwork: '作品',
  Comment: '评论',
  User: '用户',
  Commission: '约稿',
  Feedback: '反馈',
  Contest: '比赛',
  Coupon: '优惠券'
}

const formatActionType = (type) => actionTypeMap[type] || type
const formatTargetType = (type) => targetTypeMap[type] || type || '—'

const cleanDescription = (desc) => {
  if (!desc) return '—'
  // 替换意见: null 为 无
  return desc.replace(/意见:\s*null/g, '意见: 无').replace(/意见：\s*null/g, '意见：无')
}

const getActionTagType = (type) => {
  if (type?.includes('APPROVE')) return 'success'
  if (type?.includes('REJECT') || type?.includes('DELETE') || type?.includes('BAN')) return 'danger'
  if (type?.includes('UNBAN')) return 'warning'
  return 'info'
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (actionFilter.value) params.actionType = actionFilter.value
    const res = await getAuditLogs(params)
    const data = res.data
    logs.value = data?.records || data?.items || data?.content || []
    total.value = data?.total || data?.totalElements || 0
  } catch (e) { console.error('加载审计日志失败:', e) }
  finally { loading.value = false }
}

const handleSearch = () => { currentPage.value = 1; loadLogs() }

onMounted(loadLogs)
</script>

<style scoped>
.audit-logs-page { width: 100%; }
.text-muted { color: var(--c-text-muted); font-size: 13px; }
</style>
