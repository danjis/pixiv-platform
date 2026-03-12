<template>
  <div class="coupons-page">
    <div class="page-header-bar">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon> 创建优惠券
      </el-button>
    </div>

    <div class="table-wrapper">
      <el-table :data="couponList" v-loading="loading">
        <el-table-column prop="code" label="优惠码" width="130">
          <template #default="{ row }">
            <code class="coupon-code">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="130" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'PERCENTAGE' ? 'primary' : 'warning'" size="small">
              {{ row.type === 'PERCENTAGE' ? '折扣' : '立减' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠" width="90">
          <template #default="{ row }">
            <span class="money">{{ row.type === 'PERCENTAGE' ? row.discountValue + '%' : '¥' + row.discountValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="100">
          <template #default="{ row }">
            {{ row.minOrderAmount > 0 ? '满¥' + row.minOrderAmount : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="领取/总量" width="100">
          <template #default="{ row }">
            <span class="text-num">{{ row.claimedQuantity }} / {{ row.totalQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已使用" width="70" prop="usedQuantity" />
        <el-table-column label="有效期" min-width="170">
          <template #default="{ row }">
            <span class="text-muted" style="font-size: 12px;">{{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发放方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.distributionType === 'CODE_ONLY' ? 'warning' : 'success'" size="small">
              {{ row.distributionType === 'CODE_ONLY' ? '仅券码' : '公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              text
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggle(row)"
            >{{ row.status === 'ACTIVE' ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建优惠券" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：新用户专享" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="可选描述" :rows="2" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="FIXED">满减</el-radio>
            <el-radio value="PERCENTAGE">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="1" :max="form.type === 'PERCENTAGE' ? 99 : 99999" :step="form.type === 'PERCENTAGE' ? 5 : 10" />
          <span class="form-hint">{{ form.type === 'PERCENTAGE' ? '% 折扣' : '元' }}</span>
        </el-form-item>
        <el-form-item label="最低金额">
          <el-input-number v-model="form.minOrderAmount" :min="0" :step="10" />
          <span class="form-hint">0 = 无门槛</span>
        </el-form-item>
        <el-form-item label="最高抵扣" v-if="form.type === 'PERCENTAGE'">
          <el-input-number v-model="form.maxDiscountAmount" :min="0" :step="10" />
          <span class="form-hint">0 = 不限</span>
        </el-form-item>
        <el-form-item label="发放数量" prop="totalQuantity">
          <el-input-number v-model="form.totalQuantity" :min="1" :max="99999" />
        </el-form-item>
        <el-form-item label="发放方式" prop="distributionType">
          <el-radio-group v-model="form.distributionType">
            <el-radio value="PUBLIC">公开领取（领券中心可见）</el-radio>
            <el-radio value="CODE_ONLY">仅券码兑换（不在领券中心展示）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponList, createCoupon, toggleCouponStatus } from '@/api/coupon'

const loading = ref(false)
const couponList = ref([])
const showCreateDialog = ref(false)
const creating = ref(false)
const formRef = ref(null)

const form = ref({
  name: '', description: '', type: 'FIXED', discountValue: 10,
  minOrderAmount: 0, maxDiscountAmount: 0, totalQuantity: 100, dateRange: null,
  distributionType: 'PUBLIC'
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

function formatDate(str) {
  if (!str) return ''
  return new Date(str).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function statusTagType(status) {
  return { ACTIVE: 'success', INACTIVE: 'info', EXPIRED: 'danger' }[status] || 'info'
}

function statusLabel(status) {
  return { ACTIVE: '生效中', INACTIVE: '已停用', EXPIRED: '已过期' }[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await getCouponList()
    if (res.code === 200 && res.data) {
      couponList.value = Array.isArray(res.data.records) ? res.data.records : []
    }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleCreate() {
  await formRef.value.validate()
  creating.value = true
  try {
    const data = {
      name: form.value.name, description: form.value.description,
      type: form.value.type, discountValue: form.value.discountValue,
      minOrderAmount: form.value.minOrderAmount,
      maxDiscountAmount: form.value.type === 'PERCENTAGE' ? form.value.maxDiscountAmount : null,
      totalQuantity: form.value.totalQuantity,
      startTime: form.value.dateRange[0], endTime: form.value.dateRange[1],
      distributionType: form.value.distributionType
    }
    const res = await createCoupon(data)
    if (res.code === 200) {
      ElMessage.success('创建成功，优惠码: ' + (res.data?.code || ''))
      showCreateDialog.value = false
      form.value = { name: '', description: '', type: 'FIXED', discountValue: 10, minOrderAmount: 0, maxDiscountAmount: 0, totalQuantity: 100, dateRange: null, distributionType: 'PUBLIC' }
      loadList()
    } else { ElMessage.error(res.message || '创建失败') }
  } catch { ElMessage.error('创建失败') }
  finally { creating.value = false }
}

async function handleToggle(row) {
  const action = row.status === 'ACTIVE' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}优惠券「${row.name}」？`, '提示', { type: 'warning' })
    const res = await toggleCouponStatus(row.id)
    if (res.code === 200) { ElMessage.success(`${action}成功`); loadList() }
    else { ElMessage.error(res.message || `${action}失败`) }
  } catch { /* cancelled */ }
}

onMounted(() => { loadList() })
</script>

<style scoped>
.coupons-page { width: 100%; }

.coupon-code {
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: 12px;
  padding: 2px 6px;
  background: var(--c-primary-bg);
  color: var(--c-primary);
  border-radius: 4px;
  font-weight: 600;
}

.money { font-weight: 600; font-variant-numeric: tabular-nums; }
.text-num { font-variant-numeric: tabular-nums; }
.text-muted { color: var(--c-text-muted); }

.form-hint {
  margin-left: 8px;
  color: var(--c-text-muted);
  font-size: 13px;
}
</style>
