<template>
  <div class="users-page">
    <div class="page-header-bar">
      <h2>用户管理</h2>
      <div class="header-right">
        <div class="header-stat">
          <span class="header-stat-val">{{ total.toLocaleString('zh-CN') }}</span>
          <span class="header-stat-lbl">总用户数</span>
        </div>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名或邮箱"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 140px" @change="handleSearch">
        <el-option label="普通用户" value="USER" />
        <el-option label="画师" value="ARTIST" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <el-table :data="users" v-loading="loading" row-class-name="table-row">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }">
            <span class="id-text">#{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar-wrap">
                <el-avatar :size="36" :src="row.avatarUrl" class="user-avatar">
                  {{ row.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span v-if="row.role === 'ARTIST'" class="artist-badge-dot" title="认证画师"></span>
              </div>
              <div class="user-info">
                <span class="user-name">{{ row.username }}</span>
                <span class="user-email">{{ row.email || '—' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ARTIST' ? 'success' : 'info'" size="small">
              {{ row.role === 'ARTIST' ? '画师' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <div class="status-dot-wrap">
              <span class="status-dot" :class="row.banned ? 'dot-banned' : 'dot-active'"></span>
              <span class="status-text">{{ row.banned ? '已封禁' : '正常' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="showDetail(row)">详情</el-button>
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
          @current-change="loadUsers"
          @size-change="loadUsers"
        />
      </div>
    </div>

    <!-- Detail dialog -->
    <el-dialog v-model="detailVisible" title="用户详情" width="560px" destroy-on-close>
      <template v-if="currentUser">
        <div class="user-detail-header">
          <div class="detail-avatar-wrap">
            <el-avatar :size="60" :src="currentUser.avatarUrl" class="detail-avatar">
              {{ currentUser.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span v-if="currentUser.role === 'ARTIST'" class="detail-artist-crown" title="认证画师">★</span>
          </div>
          <div class="detail-user-meta">
            <h3>{{ currentUser.username }}</h3>
            <div class="detail-tags">
              <el-tag :type="currentUser.role === 'ARTIST' ? 'success' : 'info'" size="small">
                {{ currentUser.role === 'ARTIST' ? '认证画师' : '普通用户' }}
              </el-tag>
              <el-tag v-if="currentUser.banned" type="danger" size="small">已封禁</el-tag>
            </div>
          </div>
        </div>
        <el-descriptions :column="2" border style="margin-top: 20px">
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">{{ currentUser.bio || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getUsers } from '@/api/user'

const loading = ref(false)
const users = ref([])
const keyword = ref('')
const roleFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const detailVisible = ref(false)
const currentUser = ref(null)

function formatDate(str) {
  return str ? new Date(str).toLocaleString('zh-CN') : '—'
}

function showDetail(row) {
  currentUser.value = row
  detailVisible.value = true
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUsers({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined
    })
    if (res.code === 200 && res.data) {
      users.value = res.data.records || res.data.items || []
      total.value = res.data.total || 0
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function handleSearch() {
  currentPage.value = 1
  loadUsers()
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.users-page { width: 100%; }

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-stat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
}
.header-stat-val {
  font-family: 'JetBrains Mono', monospace;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-accent);
  line-height: 1;
}
.header-stat-lbl {
  font-size: 11px;
  color: var(--c-text-muted);
  letter-spacing: 0.3px;
}

.id-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  color: var(--c-text-muted);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  font-weight: 700;
  font-family: 'Syne', sans-serif;
}

.artist-badge-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--c-success);
  border: 2px solid var(--c-surface);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.user-name {
  font-weight: 600;
  color: var(--c-text);
  font-size: 13px;
}

.user-email {
  font-size: 12px;
  color: var(--c-text-muted);
}

.status-dot-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-active { background: var(--c-success); box-shadow: 0 0 6px rgba(52,211,153,0.5); }
.dot-banned { background: var(--c-danger); }
.status-text { font-size: 12px; color: var(--c-text-secondary); }

/* Detail dialog */
.user-detail-header {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--c-border);
  margin-bottom: 4px;
}

.detail-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.detail-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8) !important;
  color: #fff !important;
  font-size: 22px !important;
  font-weight: 700 !important;
  font-family: 'Syne', sans-serif !important;
}

.detail-artist-crown {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--c-accent);
  color: #0a0a0e;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  border: 2px solid var(--c-surface);
}

.detail-user-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.detail-user-meta h3 {
  font-family: 'Syne', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
  margin: 0;
}
.detail-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
