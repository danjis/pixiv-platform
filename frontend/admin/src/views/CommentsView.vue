<template>
  <div class="comments-page">
    <div class="page-header-bar">
      <h2>评论管理</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索评论内容"
        style="width: 260px"
        clearable
        @keyup.enter="loadComments"
        @clear="loadComments"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="table-wrapper">
      <el-table :data="comments" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="artworkId" label="作品ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="content" label="评论内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="父评论" width="90">
          <template #default="{ row }">
            <span class="text-muted">{{ row.parentId || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComments, deleteComment } from '@/api/comment'

const loading = ref(false)
const comments = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)

function formatDate(str) {
  return str ? new Date(str).toLocaleString('zh-CN') : ''
}

async function loadComments() {
  loading.value = true
  try {
    const res = await getComments({
      page: currentPage.value,
      size: pageSize,
      keyword: keyword.value || undefined
    })
    if (res.code === 200 && res.data) {
      comments.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch { ElMessage.error('加载评论失败') }
  finally { loading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除该评论？\n"${row.content.substring(0, 50)}..."`,
      '删除评论',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await deleteComment(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadComments() }
    else { ElMessage.error(res.message || '删除失败') }
  } catch { /* cancelled */ }
}

onMounted(() => { loadComments() })
</script>

<style scoped>
.comments-page { width: 100%; }
.text-muted { color: var(--c-text-muted); font-size: 13px; }
</style>
