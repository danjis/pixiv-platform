<template>
  <div class="commissions-page">
    <div class="page-header-bar">
      <h2>约稿管理</h2>
    </div>

    <div class="table-wrapper">
      <el-table :data="commissions" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="clientId" label="委托方" width="80" />
        <el-table-column prop="artistId" label="画师" width="80" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="100">
          <template #default="{ row }">
            <span class="money">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="定金" width="80">
          <template #default="{ row }">
            <el-tag :type="row.depositPaid ? 'success' : 'info'" size="small">
              {{ row.depositPaid ? '已付 ¥' + row.depositAmount : '未付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="尾款" width="80">
          <template #default="{ row }">
            <el-tag :type="row.finalPaid ? 'success' : 'info'" size="small">
              {{ row.finalPaid ? '已付' : '未付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="取消信息" width="140">
          <template #default="{ row }">
            <template v-if="row.status === 'CANCELLED'">
              <div class="text-muted" style="font-size: 12px">
                {{ cancelRoleLabel(row.cancelledByRole) }}
              </div>
              <div class="text-muted" style="font-size: 12px" v-if="row.cancelReason">
                {{ row.cancelReason.length > 20 ? row.cancelReason.substring(0, 20) + '...' : row.cancelReason }}
              </div>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            <span class="text-muted">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'CANCELLED' && row.status !== 'COMPLETED' && row.status !== 'REJECTED'"
              size="small"
              type="danger"
              text
              @click="handleAdminCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="simple-pagination">
        <el-button :disabled="page <= 0" size="small" @click="page--; loadList()">上一页</el-button>
        <span class="page-info">第 {{ page + 1 }} 页</span>
        <el-button :disabled="commissions.length < pageSize" size="small" @click="page++; loadList()">下一页</el-button>
      </div>
    </div>

    <!-- 管理员取消约稿弹窗 -->
    <el-dialog v-model="cancelDialogVisible" title="管理员取消约稿" width="500px">
      <div v-if="cancelTarget" style="margin-bottom: 16px">
        <p>约稿: <strong>#{{ cancelTarget.id }} {{ cancelTarget.title }}</strong></p>
        <p>委托方: {{ cancelTarget.clientId }} → 画师: {{ cancelTarget.artistId }}</p>
        <p>总金额: <strong>¥{{ cancelTarget.totalAmount }}</strong>
          <span v-if="cancelTarget.depositPaid"> | 定金已付: ¥{{ cancelTarget.depositAmount }}</span>
          <span v-if="cancelTarget.finalPaid"> | 尾款已付</span>
        </p>
      </div>

      <el-form label-width="100px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
        <el-form-item label="退还定金" v-if="cancelTarget && cancelTarget.depositPaid">
          <el-switch v-model="cancelForm.refundDeposit" />
          <span class="text-muted" style="margin-left: 8px">
            {{ cancelForm.refundDeposit ? '将退还定金（画师违约情况）' : '定金不退（默认规则）' }}
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="cancelDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="confirmAdminCancel" :loading="cancelLoading">确认取消约稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCommissions, adminCancelCommission } from '@/api/commission'

const loading = ref(false)
const commissions = ref([])
const page = ref(0)
const pageSize = 20

const cancelDialogVisible = ref(false)
const cancelTarget = ref(null)
const cancelForm = ref({ reason: '', refundDeposit: false })
const cancelLoading = ref(false)

const statusMap = {
  PENDING: '待接受', ACCEPTED: '已接受', QUOTED: '已报价',
  DEPOSIT_PAID: '定金已付', IN_PROGRESS: '进行中', DELIVERED: '已交付',
  REVISION_REQUESTED: '修改中', COMPLETED: '已完成',
  CANCELLED: '已取消', REJECTED: '已拒绝', REFUNDED: '已退款'
}

function statusLabel(s) { return statusMap[s] || s }
function statusType(s) {
  if (['COMPLETED'].includes(s)) return 'success'
  if (['CANCELLED', 'REFUNDED', 'REJECTED'].includes(s)) return 'danger'
  if (['IN_PROGRESS', 'DEPOSIT_PAID'].includes(s)) return 'warning'
  return 'info'
}
function cancelRoleLabel(role) {
  return { CLIENT: '委托方取消', ARTIST: '画师取消', ADMIN: '管理员取消' }[role] || '已取消'
}
function formatDate(str) {
  return str ? new Date(str).toLocaleString('zh-CN') : ''
}

async function loadList() {
  loading.value = true
  try {
    const res = await getCommissions({ page: page.value, size: pageSize })
    if (res.code === 200 && res.data) {
      commissions.value = res.data
    }
  } catch {
    ElMessage.error('加载约稿列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdminCancel(row) {
  cancelTarget.value = row
  cancelForm.value = { reason: '', refundDeposit: false }
  cancelDialogVisible.value = true
}

async function confirmAdminCancel() {
  if (!cancelForm.value.reason.trim()) {
    ElMessage.warning('请填写取消原因')
    return
  }
  cancelLoading.value = true
  try {
    const res = await adminCancelCommission(cancelTarget.value.id, {
      reason: cancelForm.value.reason.trim(),
      refundDeposit: cancelForm.value.refundDeposit
    })
    if (res.code === 200) {
      ElMessage.success('约稿已取消')
      cancelDialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    cancelLoading.value = false
  }
}

onMounted(() => { loadList() })
</script>

<style scoped>
.commissions-page { width: 100%; }
.money { font-weight: 600; font-variant-numeric: tabular-nums; }
.paid { color: var(--c-success); font-weight: 600; }
.text-muted { color: var(--c-text-secondary); font-size: 13px; }
</style>
