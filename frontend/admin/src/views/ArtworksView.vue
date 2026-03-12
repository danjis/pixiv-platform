<template>
  <div class="artworks-page">
    <div class="page-header-bar">
      <h2>作品管理</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索作品标题"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="handleSearch">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已删除" value="DELETED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <el-table :data="artworks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="作品" min-width="280">
          <template #default="{ row }">
            <div class="artwork-cell">
              <el-image
                :src="row.thumbnailUrl || row.imageUrl"
                class="artwork-thumb"
                fit="cover"
                :preview-src-list="[row.imageUrl || row.thumbnailUrl]"
                preview-teleported
              />
              <div class="artwork-info">
                <span class="artwork-title">{{ row.title }}</span>
                <span class="artwork-artist">by {{ row.artistName || '未知' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="180">
          <template #default="{ row }">
            <div class="artwork-stats">
              <span>👁 {{ row.viewCount || 0 }}</span>
              <span>❤ {{ row.likeCount || 0 }}</span>
              <span>⭐ {{ row.favoriteCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'DELETED'"
              type="danger"
              text
              size="small"
              @click="handleDelete(row)"
            >删除</el-button>
            <span v-else class="text-muted" style="font-size: 12px;">已删除</span>
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
          @current-change="loadArtworks"
          @size-change="loadArtworks"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArtworks, deleteArtwork } from '@/api/artwork'

const loading = ref(false)
const artworks = ref([])
const keyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

function formatDate(str) {
  return str ? new Date(str).toLocaleString('zh-CN') : '—'
}

function statusLabel(s) {
  return { PUBLISHED: '已发布', DRAFT: '草稿', DELETED: '已删除' }[s] || s
}

function statusTagType(s) {
  return { PUBLISHED: 'success', DRAFT: 'warning', DELETED: 'danger' }[s] || 'info'
}

async function loadArtworks() {
  loading.value = true
  try {
    const res = await getArtworks({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined
    })
    if (res.code === 200 && res.data) {
      artworks.value = res.data.records || res.data.items || []
      total.value = res.data.total || 0
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function handleSearch() {
  currentPage.value = 1
  loadArtworks()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除作品「${row.title}」吗？此操作为软删除。`,
      '删除作品',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await deleteArtwork(row.id)
    if (res.code === 200) {
      ElMessage.success('作品已删除')
      loadArtworks()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch { /* cancelled */ }
}

onMounted(() => { loadArtworks() })
</script>

<style scoped>
.artworks-page {
  width: 100%;
}

.artwork-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.artwork-thumb {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  object-fit: cover;
}

.artwork-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.artwork-title {
  font-weight: 600;
  color: var(--c-text);
  font-size: 13px;
}

.artwork-artist {
  font-size: 12px;
  color: var(--c-text-muted);
}

.artwork-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--c-text-secondary);
}

.text-muted {
  color: var(--c-text-secondary);
  font-size: 13px;
}
</style>
