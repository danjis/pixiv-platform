<template>
  <div class="contests-page">
    <div class="page-header-bar">
      <h2>比赛管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon> 创建比赛
      </el-button>
    </div>

    <!-- 比赛列表 -->
    <div class="table-wrapper">
      <el-table :data="contests" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="比赛标题" min-width="180">
          <template #default="{ row }">
            <span class="link-text" @click="showDetail(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entryCount" label="参赛数" width="80" />
        <el-table-column label="投稿时间" width="180">
          <template #default="{ row }">
            <div class="time-range">
              <span>{{ formatDate(row.startTime) }}</span>
              <span class="text-muted">至 {{ formatDate(row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="投票截止" width="150">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.votingEndTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="showDetail(row)">详情</el-button>
            <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleStatusChange(row, cmd)">
              <el-button size="small" text type="warning">
                状态 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="UPCOMING">即将开始</el-dropdown-item>
                  <el-dropdown-item command="ACTIVE">进行中</el-dropdown-item>
                  <el-dropdown-item command="VOTING">投票中</el-dropdown-item>
                  <el-dropdown-item command="ENDED">已结束</el-dropdown-item>
                  <el-dropdown-item command="CANCELLED" divided>取消</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadContests"
          @current-change="loadContests"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑比赛' : '创建比赛'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="比赛标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入比赛标题" />
        </el-form-item>
        <el-form-item label="比赛描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="比赛描述" />
        </el-form-item>
        <el-form-item label="封面图片URL">
          <el-input v-model="form.coverImage" placeholder="封面图片URL" />
        </el-form-item>
        <el-form-item label="比赛规则">
          <el-input v-model="form.rules" type="textarea" :rows="3" placeholder="比赛规则说明" />
        </el-form-item>
        <el-form-item label="奖励信息">
          <el-input v-model="form.rewardInfo" type="textarea" :rows="2" placeholder="奖励说明" />
        </el-form-item>
        <el-form-item label="投稿开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="投稿截止时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="投票截止时间" prop="votingEndTime">
          <el-date-picker v-model="form.votingEndTime" type="datetime" placeholder="选择投票截止时间" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="每人最多投稿">
          <el-input-number v-model="form.maxEntriesPerArtist" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 比赛详情对话框 -->
    <el-dialog v-model="detailVisible" title="比赛详情 — 参赛作品" width="880px" destroy-on-close>
      <div v-if="currentContest" class="contest-detail-header">
        <h3>{{ currentContest.title }}</h3>
        <el-tag :type="statusTagType(currentContest.status)" size="small">{{ statusLabel(currentContest.status) }}</el-tag>
        <span class="text-muted">参赛作品: {{ currentContest.entryCount }}</span>
        <el-button size="small" type="primary" @click="handleCalculateRankings" :loading="calculatingRank">计算排名</el-button>
      </div>

      <el-table :data="entries" v-loading="entriesLoading" size="small">
        <el-table-column prop="rankPosition" label="排名" width="70">
          <template #default="{ row }">
            <span v-if="row.rankPosition" class="rank-badge" :class="'rank-' + row.rankPosition">
              #{{ row.rankPosition }}
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="作品" width="72">
          <template #default="{ row }">
            <el-image :src="row.thumbnailUrl || row.imageUrl" style="width: 44px; height: 44px; border-radius: 6px;" fit="cover" :preview-src-list="[row.imageUrl]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="130" />
        <el-table-column prop="artistName" label="画师" width="90" />
        <el-table-column label="平均分" width="80">
          <template #default="{ row }">{{ row.averageScore ? row.averageScore.toFixed(1) : '0.0' }}</template>
        </el-table-column>
        <el-table-column prop="voteCount" label="投票数" width="70" />
        <el-table-column label="状态" width="85">
          <template #default="{ row }">
            <el-tag :type="entryStatusType(row.status)" size="small">{{ entryStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button size="small" text type="success" @click="handleReview(row, 'APPROVED')" :disabled="row.status === 'APPROVED'">通过</el-button>
            <el-button size="small" text type="danger" @click="handleReview(row, 'REJECTED')" :disabled="row.status === 'REJECTED'">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import {
  getContests, createContest, updateContest, deleteContest,
  updateContestStatus, getContestEntries, reviewEntry, calculateRankings
} from '@/api/contest'

const loading = ref(false)
const contests = ref([])
const pagination = ref({ page: 1, size: 20, total: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const form = ref({
  title: '', description: '', coverImage: '', rules: '', rewardInfo: '',
  startTime: '', endTime: '', votingEndTime: '', maxEntriesPerArtist: 1
})

const formRules = {
  title: [{ required: true, message: '请输入比赛标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  votingEndTime: [{ required: true, message: '请选择投票截止时间', trigger: 'change' }]
}

const detailVisible = ref(false)
const currentContest = ref(null)
const entries = ref([])
const entriesLoading = ref(false)
const calculatingRank = ref(false)

const statusLabel = (s) => ({ UPCOMING: '即将开始', ACTIVE: '进行中', VOTING: '投票中', ENDED: '已结束', CANCELLED: '已取消' }[s] || s)
const statusTagType = (s) => ({ UPCOMING: 'info', ACTIVE: 'success', VOTING: 'warning', ENDED: '', CANCELLED: 'danger' }[s] || '')
const entryStatusLabel = (s) => ({ SUBMITTED: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', WINNER: '获奖' }[s] || s)
const entryStatusType = (s) => ({ SUBMITTED: 'info', APPROVED: 'success', REJECTED: 'danger', WINNER: 'warning' }[s] || '')
const formatDate = (dt) => dt ? dt.replace('T', ' ').substring(0, 16) : '—'

const loadContests = async () => {
  loading.value = true
  try {
    const res = await getContests({ page: pagination.value.page, size: pagination.value.size })
    contests.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
  } catch (e) { console.error('加载比赛列表失败:', e) }
  finally { loading.value = false }
}

const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  form.value = { title: '', description: '', coverImage: '', rules: '', rewardInfo: '', startTime: '', endTime: '', votingEndTime: '', maxEntriesPerArtist: 1 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editingId.value = row.id
  form.value = {
    title: row.title, description: row.description || '', coverImage: row.coverImage || '',
    rules: row.rules || '', rewardInfo: row.rewardInfo || '', startTime: row.startTime,
    endTime: row.endTime, votingEndTime: row.votingEndTime, maxEntriesPerArtist: row.maxEntriesPerArtist || 1
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateContest(editingId.value, form.value)
        ElMessage.success('比赛已更新')
      } else {
        await createContest(form.value)
        ElMessage.success('比赛已创建')
      }
      dialogVisible.value = false
      loadContests()
    } catch (e) { console.error('操作失败:', e) }
    finally { submitting.value = false }
  })
}

const handleStatusChange = async (row, status) => {
  try {
    await updateContestStatus(row.id, status)
    ElMessage.success('状态已更新')
    loadContests()
  } catch (e) { console.error('状态更新失败:', e) }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除比赛「${row.title}」吗？`, '确认删除', { type: 'warning' })
    await deleteContest(row.id)
    ElMessage.success('比赛已删除')
    loadContests()
  } catch (e) { if (e !== 'cancel') console.error('删除失败:', e) }
}

const showDetail = async (row) => {
  currentContest.value = row
  detailVisible.value = true
  await loadEntries(row.id)
}

const loadEntries = async (contestId) => {
  entriesLoading.value = true
  try {
    const res = await getContestEntries(contestId, { page: 1, size: 100 })
    entries.value = res.data?.records || []
  } catch (e) { console.error('加载参赛作品失败:', e) }
  finally { entriesLoading.value = false }
}

const handleReview = async (entry, status) => {
  try {
    await reviewEntry(entry.id, status)
    ElMessage.success('审核状态已更新')
    if (currentContest.value) await loadEntries(currentContest.value.id)
  } catch (e) { console.error('审核失败:', e) }
}

const handleCalculateRankings = async () => {
  if (!currentContest.value) return
  calculatingRank.value = true
  try {
    await calculateRankings(currentContest.value.id)
    ElMessage.success('排名计算完成')
    await loadEntries(currentContest.value.id)
  } catch (e) { console.error('排名计算失败:', e) }
  finally { calculatingRank.value = false }
}

onMounted(() => { loadContests() })
</script>

<style scoped>
.contests-page { width: 100%; }

.link-text {
  color: var(--c-primary);
  cursor: pointer;
  font-weight: 500;
}
.link-text:hover { text-decoration: underline; }

.time-range { display: flex; flex-direction: column; font-size: 13px; line-height: 1.5; }
.text-muted { color: var(--c-text-muted); font-size: 13px; }

.contest-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.contest-detail-header h3 { margin: 0; font-size: 16px; }

.rank-badge { font-weight: 700; font-size: 14px; }
.rank-1 { color: #f59e0b; }
.rank-2 { color: #94a3b8; }
.rank-3 { color: #b87333; }
</style>
