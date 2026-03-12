<template>
  <div class="membership-page">
    <div class="page-header-bar">
      <h2>会员管理</h2>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-tag type="warning" effect="plain" size="large">VIP/SVIP 会员：{{ vipCount }} 人</el-tag>
      </div>
    </div>

    <!-- 筛选与搜索区 -->
    <div class="filter-bar">
      <el-select v-model="filterLevel" placeholder="会员等级筛选" clearable style="width: 160px" @change="loadList">
        <el-option label="全部会员" value="ALL" />
        <el-option label="VIP 会员" value="VIP" />
        <el-option label="SVIP 超级会员" value="SVIP" />
      </el-select>
      <el-input v-model="searchUserId" placeholder="按用户ID搜索" clearable style="width: 180px" @keyup.enter="searchUser" />
      <el-button type="primary" @click="searchUser" :loading="searchLoading">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 会员列表表格 -->
    <div class="table-wrapper" v-if="!showDetail">
      <el-table :data="memberList" v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.username || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="130">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ levelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" width="180">
          <template #default="{ row }">
            <template v-if="row.expireTime">
              <span :class="{ 'expired-text': row.expired }">{{ row.expireTime }}</span>
              <el-tag v-if="row.expired" type="danger" size="small" style="margin-left: 6px">已过期</el-tag>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.expired" type="danger" size="small">已过期</el-tag>
            <el-tag v-else type="success" size="small">有效</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="warning" text @click="openUpgrade(row)">升级</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 会员详情 -->
    <div v-if="showDetail && memberInfo" class="member-info-section">
      <div style="margin-bottom: 16px">
        <el-button @click="showDetail = false">← 返回列表</el-button>
      </div>

      <div class="info-card">
        <div class="info-card-header">
          <span class="info-card-title">会员信息</span>
          <el-tag :type="levelTagType(memberInfo.level)" size="large">{{ levelLabel(memberInfo.level) }}</el-tag>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ memberInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ memberInfo.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">
            <el-tag :type="levelTagType(memberInfo.level)" size="small">{{ levelLabel(memberInfo.level) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="到期时间">
            <template v-if="memberInfo.expireTime">
              <span :class="{ 'expired-text': memberInfo.expired }">
                {{ memberInfo.expireTime }}
                <el-tag v-if="memberInfo.expired" type="danger" size="small" style="margin-left: 8px">已过期</el-tag>
              </span>
            </template>
            <span v-else class="text-muted">永久 / 未开通</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="info-card" style="margin-top: 20px">
        <div class="info-card-header">
          <span class="info-card-title">升级会员</span>
        </div>
        <el-form :model="upgradeForm" label-width="100px" style="max-width: 480px">
          <el-form-item label="目标等级">
            <el-select v-model="upgradeForm.level" placeholder="选择会员等级">
              <el-option label="VIP 会员" value="VIP" />
              <el-option label="SVIP 超级会员" value="SVIP" />
            </el-select>
          </el-form-item>
          <el-form-item label="时长(天)">
            <el-input-number v-model="upgradeForm.durationDays" :min="1" :max="3650" />
          </el-form-item>
          <el-form-item label="快捷选择">
            <div class="quick-btns">
              <el-button size="small" @click="upgradeForm.durationDays = 30">30天</el-button>
              <el-button size="small" @click="upgradeForm.durationDays = 90">90天</el-button>
              <el-button size="small" @click="upgradeForm.durationDays = 180">180天</el-button>
              <el-button size="small" @click="upgradeForm.durationDays = 365">365天</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUpgrade" :loading="upgradeLoading">确认升级</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 升级弹窗（从列表直接升级） -->
    <el-dialog v-model="upgradeDialogVisible" title="升级会员" width="460px">
      <el-form :model="upgradeForm" label-width="100px">
        <el-form-item label="用户ID">
          <span>{{ upgradeForm.userId }}</span>
        </el-form-item>
        <el-form-item label="目标等级">
          <el-select v-model="upgradeForm.level" placeholder="选择会员等级">
            <el-option label="VIP 会员" value="VIP" />
            <el-option label="SVIP 超级会员" value="SVIP" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(天)">
          <el-input-number v-model="upgradeForm.durationDays" :min="1" :max="3650" />
        </el-form-item>
        <el-form-item label="快捷选择">
          <div class="quick-btns">
            <el-button size="small" @click="upgradeForm.durationDays = 30">30天</el-button>
            <el-button size="small" @click="upgradeForm.durationDays = 90">90天</el-button>
            <el-button size="small" @click="upgradeForm.durationDays = 180">180天</el-button>
            <el-button size="small" @click="upgradeForm.durationDays = 365">365天</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="upgradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpgradeFromDialog" :loading="upgradeLoading">确认升级</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberList, getUserMembership, upgradeMembership } from '../api/membership'

const loading = ref(false)
const searchLoading = ref(false)
const memberList = ref([])
const total = ref(0)
const vipCount = ref(0)
const currentPage = ref(1)
const pageSize = 20
const filterLevel = ref('')
const searchUserId = ref('')

const showDetail = ref(false)
const memberInfo = ref(null)

const upgradeDialogVisible = ref(false)
const upgradeForm = ref({ userId: null, level: 'VIP', durationDays: 30 })
const upgradeLoading = ref(false)

const levelLabel = (level) => ({ NORMAL: '普通用户', VIP: 'VIP 会员', SVIP: 'SVIP 超级会员' }[level] || level)
const levelTagType = (level) => ({ NORMAL: 'info', VIP: 'warning', SVIP: 'danger' }[level] || 'info')

async function loadList() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize }
    if (filterLevel.value && filterLevel.value !== 'ALL') {
      params.level = filterLevel.value
    }
    const res = await getMemberList(params)
    if (res.code === 200 && res.data) {
      memberList.value = res.data.records || []
      total.value = res.data.total || 0
      vipCount.value = res.data.vipCount || 0
    }
  } catch {
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  loadList()
}

async function searchUser() {
  if (!searchUserId.value) {
    loadList()
    return
  }
  searchLoading.value = true
  try {
    const res = await getUserMembership(searchUserId.value)
    if (res.code === 200 && res.data) {
      memberList.value = [res.data]
      total.value = 1
    } else {
      memberList.value = []
      total.value = 0
      ElMessage.warning('未找到该用户的会员信息')
    }
  } catch {
    memberList.value = []
    total.value = 0
    ElMessage.error('查询失败，请检查用户ID是否正确')
  } finally {
    searchLoading.value = false
  }
}

function resetSearch() {
  searchUserId.value = ''
  filterLevel.value = ''
  currentPage.value = 1
  loadList()
}

function viewDetail(row) {
  memberInfo.value = row
  showDetail.value = true
}

function openUpgrade(row) {
  upgradeForm.value = { userId: row.userId, level: 'VIP', durationDays: 30 }
  upgradeDialogVisible.value = true
}

async function handleUpgrade() {
  try {
    await ElMessageBox.confirm(
      `确认将用户 ${memberInfo.value.userId} 升级为 ${levelLabel(upgradeForm.value.level)}，时长 ${upgradeForm.value.durationDays} 天？`,
      '确认升级', { type: 'warning' }
    )
  } catch { return }
  upgradeLoading.value = true
  try {
    const res = await upgradeMembership({
      userId: memberInfo.value.userId,
      level: upgradeForm.value.level,
      durationDays: upgradeForm.value.durationDays
    })
    if (res.code === 200) {
      ElMessage.success('升级成功')
      memberInfo.value = res.data
      loadList()
    } else {
      ElMessage.error(res.message || '升级失败')
    }
  } catch { ElMessage.error('升级失败') }
  finally { upgradeLoading.value = false }
}

async function handleUpgradeFromDialog() {
  upgradeLoading.value = true
  try {
    const res = await upgradeMembership({
      userId: upgradeForm.value.userId,
      level: upgradeForm.value.level,
      durationDays: upgradeForm.value.durationDays
    })
    if (res.code === 200) {
      ElMessage.success('升级成功')
      upgradeDialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '升级失败')
    }
  } catch { ElMessage.error('升级失败') }
  finally { upgradeLoading.value = false }
}

onMounted(() => { loadList() })
</script>

<style scoped>
.membership-page {
  width: 100%;
}

.member-info-section {
  margin-top: 8px;
}

.info-card {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

.info-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.info-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.expired-text { color: var(--c-danger); }
.text-muted { color: var(--c-text-muted); }

.quick-btns {
  display: flex;
  gap: 8px;
}
</style>
