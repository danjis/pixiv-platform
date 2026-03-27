<template>
  <div class="artworks-page">
    <div class="page-header-bar">
      <h2>作品管理</h2>
      <div class="header-stats-row">
        <div class="hstat" v-for="s in headerStats" :key="s.label">
          <span class="hstat-val" :style="{ color: s.color }">{{ s.val }}</span>
          <span class="hstat-lbl">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- Filter bar -->
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
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          :class="['status-tab', { active: statusFilter === tab.value }]"
          @click="statusFilter = tab.value; handleSearch()"
        >{{ tab.label }}</button>
      </div>
      <div style="flex:1" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <el-table :data="artworks" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-text">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column label="作品" min-width="300">
          <template #default="{ row }">
            <div class="artwork-cell">
              <div class="artwork-thumb-wrap">
                <el-image
                  :src="row.thumbnailUrl || row.imageUrl"
                  class="artwork-thumb"
                  fit="cover"
                  :preview-src-list="[row.imageUrl || row.thumbnailUrl].filter(Boolean)"
                  preview-teleported
                />
              </div>
              <div class="artwork-info">
                <span class="artwork-title">{{ row.title }}</span>
                <span class="artwork-artist">by {{ row.artistName || '未知' }}</span>
                <div class="artwork-stats" v-if="row.viewCount || row.likeCount">
                  <span class="astat"><span class="astat-icon">👁</span>{{ (row.viewCount || 0).toLocaleString() }}</span>
                  <span class="astat"><span class="astat-icon">❤</span>{{ (row.likeCount || 0).toLocaleString() }}</span>
                  <span class="astat"><span class="astat-icon">⭐</span>{{ (row.favoriteCount || 0).toLocaleString() }}</span>
                </div>
              </div>
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
            <span v-else class="deleted-label">已删除</span>
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
import { ref, computed, onMounted } from 'vue'
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

const statusTabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已删除', value: 'DELETED' }
]

const headerStats = computed(() => [
  { val: total.value.toLocaleString('zh-CN'), label: '总作品', color: 'var(--c-text)' },
  { val: artworks.value.filter(a => a.status === 'PUBLISHED').length, label: '本页已发布', color: 'var(--c-success)' },
  { val: artworks.value.filter(a => a.status === 'DELETED').length, label: '本页已删除', color: 'var(--c-danger)' }
])

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
.artworks-page { width: 100%; }

.header-stats-row {
  display: flex;
  align-items: center;
  gap: 24px;
}
.hstat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
}
.hstat-val {
  font-family: 'JetBrains Mono', monospace;
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
}
.hstat-lbl {
  font-size: 10px;
  color: var(--c-text-muted);
  letter-spacing: 0.3px;
}

.id-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  color: var(--c-text-muted);
}

.artwork-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.artwork-thumb-wrap {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: var(--c-surface-3);
  border: 1px solid var(--c-border);
}

.artwork-thumb {
  width: 100%;
  height: 100%;
  display: block;
}

.artwork-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.artwork-title {
  font-weight: 600;
  color: var(--c-text);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.artwork-artist {
  font-size: 11.5px;
  color: var(--c-text-muted);
}

.artwork-stats {
  display: flex;
  gap: 10px;
  margin-top: 2px;
}
.astat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: var(--c-text-muted);
  font-family: 'JetBrains Mono', monospace;
}
.astat-icon { font-size: 10px; }

.deleted-label {
  font-size: 11px;
  color: var(--c-text-muted);
  font-style: italic;
}
</style>
 