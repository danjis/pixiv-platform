<template>
  <div class="audit-logs-page">
    <h2 class="page-title">审计日志</h2>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-select v-model="actionFilter" placeholder="操作类型筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="通过画师申请" value="APPROVE_ARTIST_APPLICATION" />
            <el-option label="拒绝画师申请" value="REJECT_ARTIST_APPLICATION" />
            <el-option label="删除作品" value="DELETE_ARTWORK" />
            <el-option label="删除评论" value="DELETE_COMMENT" />
            <el-option label="封禁用户" value="BAN_USER" />
            <el-option label="解封用户" value="UNBAN_USER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="logs" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="adminId" label="管理员ID" width="100" />
        <el-table-column prop="actionType" label="操作类型" width="200">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.actionType)" size="small">
              {{ formatActionType(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="targetType" label="目标类型" width="120" />
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </el-card>
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const actionTypeMap = {
  'APPROVE_ARTIST_APPLICATION': '通过画师申请',
  'REJECT_ARTIST_APPLICATION': '拒绝画师申请',
  'DELETE_ARTWORK': '删除作品',
  'DELETE_COMMENT': '删除评论',
  'BAN_USER': '封禁用户',
  'UNBAN_USER': '解封用户',
  'OTHER': '其他操作'
}

const formatActionType = (type) => actionTypeMap[type] || type

const getActionTagType = (type) => {
  if (type?.includes('APPROVE')) return 'success'
  if (type?.includes('REJECT') || type?.includes('DELETE') || type?.includes('BAN')) return 'danger'
  if (type?.includes('UNBAN')) return 'warning'
  return 'info'
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (actionFilter.value) params.actionType = actionFilter.value

    const res = await getAuditLogs(params)
    const data = res.data
    logs.value = data?.records || data?.items || data?.content || []
    total.value = data?.total || data?.totalElements || 0
  } catch (e) {
    console.error('加载审计日志失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadLogs()
}

onMounted(loadLogs)
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
</style>
