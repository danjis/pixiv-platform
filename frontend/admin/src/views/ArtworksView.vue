<template>
  <div class="artworks-page">
    <h2 class="page-title">作品管理</h2>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索作品标题或描述"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 作品列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="artworks" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="缩略图" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.thumbnailUrl || row.imageUrl"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
              :preview-src-list="[row.imageUrl]"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="artistName" label="画师" width="120" />
        <el-table-column label="数据" width="200">
          <template #default="{ row }">
            <div class="stats-cell">
              <span title="浏览">👁 {{ row.viewCount || 0 }}</span>
              <span title="点赞">❤ {{ row.likeCount || 0 }}</span>
              <span title="收藏">⭐ {{ row.favoriteCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="artworkStatusType(row.status)" size="small">
              {{ artworkStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'DELETED'"
              type="danger"
              text
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <span v-else class="deleted-text">已删除</span>
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
          @current-change="loadArtworks"
          @size-change="loadArtworks"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import { getArtworks, deleteArtwork } from '../api/artwork'

const artworks = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const statusFilter = ref('')

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const artworkStatusText = (status) => {
  const map = { PUBLISHED: '已发布', DRAFT: '草稿', DELETED: '已删除' }
  return map[status] || status
}

const artworkStatusType = (status) => {
  const map = { PUBLISHED: 'success', DRAFT: 'info', DELETED: 'danger' }
  return map[status] || 'info'
}

const loadArtworks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value) params.status = statusFilter.value

    const res = await getArtworks(params)
    const data = res.data
    artworks.value = data?.records || data?.items || data?.content || []
    total.value = data?.total || data?.totalElements || 0
  } catch (e) {
    console.error('加载作品列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (artwork) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除作品 "${artwork.title}" 吗？此操作将进行软删除。`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteArtwork(artwork.id)
    ElMessage.success('作品已删除')
    loadArtworks()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadArtworks()
}

onMounted(loadArtworks)
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

.stats-cell {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.image-placeholder {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  color: #c0c4cc;
}

.deleted-text {
  color: #909399;
  font-size: 13px;
}
</style>
