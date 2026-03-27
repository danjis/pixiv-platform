<template>
  <div class="users-page">
    <div class="page-header-bar">
      <h2>用户管理</h2>
    </div>

    <!-- 筛选栏 -->
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

    <!-- 表格 -->
    <div class="table-wrapper">
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatarUrl" class="user-avatar">
                {{ row.username?.charAt(0) }}
              </el-avatar>
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
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="560px" destroy-on-close>
      <template v-if="currentUser">
        <div class="user-detail-header">
          <el-avatar :size="56" :src="currentUser.avatarUrl">
            {{ currentUser.username?.charAt(0) }}
          </el-avatar>
          <div>
            <h3>{{ currentUser.username }}</h3>
            <el-tag :type="currentUser.role === 'ARTIST' ? 'success' : 'info'" size="small">
              {{ currentUser.role === 'ARTIST' ? '画师' : '用户' }}
            </el-tag>
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
.users-page {
  width: 100%;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
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

.text-muted {
  color: var(--c-text-secondary);
  font-size: 13px;
}

.user-detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-detail-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
}
</style>
