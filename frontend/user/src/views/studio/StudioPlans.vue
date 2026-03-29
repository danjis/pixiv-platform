<template>
  <div class="studio-plans-page">
    <div class="page-header">
      <div>
        <h2>约稿方案管理</h2>
        <p class="header-desc">设置你的约稿方案，让用户更容易发起约稿</p>
      </div>
      <button class="btn-create" @click="openCreate">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建方案
      </button>
    </div>

    <!-- 方案列表 -->
    <div v-if="plans.length > 0" class="plan-grid">
      <div v-for="p in plans" :key="p.id" class="plan-card" :class="{ inactive: !p.active }">
        <div class="plan-head">
          <h3>{{ p.title }}</h3>
          <span class="plan-status" :class="p.active ? 'on' : 'off'">{{ p.active ? '启用' : '已关闭' }}</span>
        </div>
        <p class="plan-desc">{{ p.description || '无描述' }}</p>
        <div class="plan-price">
          ¥{{ p.priceStart }}<span v-if="p.priceEnd"> - ¥{{ p.priceEnd }}</span>
        </div>
        <div class="plan-meta">
          <span v-if="p.estimatedDays">{{ p.estimatedDays }}天交付</span>
          <span v-if="p.revisionsIncluded">{{ p.revisionsIncluded }}次修改</span>
          <span v-if="p.category">{{ p.category }}</span>
        </div>
        <div class="plan-actions">
          <button class="act-btn" @click="openEdit(p)">编辑</button>
          <button class="act-btn" @click="handleToggle(p)">{{ p.active ? '关闭' : '启用' }}</button>
          <button class="act-btn danger" @click="handleDelete(p)">删除</button>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>还没有约稿方案</p>
      <p class="empty-hint">创建方案后，用户可以选择方案快速发起约稿</p>
    </div>

    <!-- 新建/编辑弹窗 -->
    <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
      <div class="modal-body">
        <h3>{{ editingId ? '编辑方案' : '新建方案' }}</h3>
        <div class="modal-form">
          <div class="form-group">
            <label>方案名称 <span class="req">*</span></label>
            <input v-model="form.title" class="form-input" placeholder="如「角色立绘」「头像」" maxlength="50" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" class="form-input" rows="3" placeholder="描述方案内容、包含内容等" maxlength="1000"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>起始价格 (¥) <span class="req">*</span></label>
              <input v-model.number="form.priceStart" type="number" class="form-input" min="0" />
            </div>
            <div class="form-group half">
              <label>最高价格 (¥)</label>
              <input v-model.number="form.priceEnd" type="number" class="form-input" min="0" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group half">
              <label>预计天数</label>
              <input v-model.number="form.estimatedDays" type="number" class="form-input" min="1" />
            </div>
            <div class="form-group half">
              <label>包含修改次数</label>
              <input v-model.number="form.revisionsIncluded" type="number" class="form-input" min="0" />
            </div>
          </div>
          <div class="form-group">
            <label>分类</label>
            <input v-model="form.category" class="form-input" placeholder="如「插画」「头像」「Live2D」" maxlength="30" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDialog = false">取消</button>
          <button class="btn-submit" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyPlans, createPlan, updatePlan, togglePlan, deletePlan } from '@/api/payment'

const plans = ref([])
const loading = ref(true)
const showDialog = ref(false)
const editingId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  title: '',
  description: '',
  priceStart: null,
  priceEnd: null,
  estimatedDays: 7,
  revisionsIncluded: 2,
  category: ''
})
const form = ref(defaultForm())

onMounted(async () => {
  await loadPlans()
})

async function loadPlans() {
  loading.value = true
  try {
    const res = await getMyPlans()
    if (res.code === 200) plans.value = res.data || []
  } catch {}
  loading.value = false
}

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

function openEdit(p) {
  editingId.value = p.id
  form.value = {
    title: p.title,
    description: p.description || '',
    priceStart: p.priceStart,
    priceEnd: p.priceEnd,
    estimatedDays: p.estimatedDays,
    revisionsIncluded: p.revisionsIncluded,
    category: p.category || ''
  }
  showDialog.value = true
}

async function handleSave() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请输入方案名称')
    return
  }
  if (!form.value.priceStart || form.value.priceStart <= 0) {
    ElMessage.warning('请输入起始价格')
    return
  }

  saving.value = true
  try {
    const data = { ...form.value }
    if (!data.priceEnd) delete data.priceEnd
    if (!data.category) delete data.category

    let res
    if (editingId.value) {
      res = await updatePlan(editingId.value, data)
    } else {
      res = await createPlan(data)
    }
    if (res.code === 200) {
      ElMessage.success(editingId.value ? '方案已更新' : '方案已创建')
      showDialog.value = false
      await loadPlans()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleToggle(p) {
  try {
    const res = await togglePlan(p.id)
    if (res.code === 200) {
      ElMessage.success(p.active ? '方案已关闭' : '方案已启用')
      await loadPlans()
    }
  } catch {}
}

async function handleDelete(p) {
  try {
    await ElMessageBox.confirm(`确定删除方案「${p.title}」？`, '删除方案', { type: 'warning' })
    const res = await deletePlan(p.id)
    if (res.code === 200) {
      ElMessage.success('方案已删除')
      await loadPlans()
    }
  } catch {}
}
</script>

<style scoped>
.studio-plans-page {
  padding: 0;
  background: #fff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}
.header-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.btn-create {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border: none;
  background: #0096FA;
  color: #fff;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.btn-create:hover { background: #0080d5; }

/* Plan Grid */
.plan-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 18px;
}
.plan-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  transition: all 0.25s;
}
.plan-card:hover {
  box-shadow: 0 8px 28px rgba(0,0,0,0.10);
  transform: translateY(-3px);
}
.plan-card.inactive { opacity: 0.55; }

.plan-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.plan-head h3 {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.plan-status {
  font-size: 12px;
  padding: 3px 12px;
  border-radius: 999px;
  font-weight: 600;
}
.plan-status.on { background: #f0fdf4; color: #16a34a; }
.plan-status.off { background: #f5f5f5; color: #999; }

.plan-desc {
  font-size: 13px;
  color: #888;
  margin: 0 0 12px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.plan-price {
  font-size: 18px;
  font-weight: 700;
  color: #0096FA;
  margin-bottom: 10px;
}

.plan-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 16px;
}

.plan-actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;
}
.act-btn {
  padding: 7px 18px;
  border: 1px solid #e8e8e8;
  background: #fff;
  border-radius: 999px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.act-btn:hover { background: #f5f5f5; }
.act-btn.danger { color: #ff4d4f; border-color: #ffccc7; }
.act-btn.danger:hover { background: #fff1f0; }

/* Empty */
.empty-state {
  text-align: center;
  padding: 70px 0;
  color: #ccc;
}
.empty-state p { margin: 0; font-size: 16px; }
.empty-hint { font-size: 13px; color: #ddd; margin-top: 8px !important; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.modal-body {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  width: 90%;
  max-width: 520px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.13);
}
.modal-body h3 { margin: 0 0 20px; font-size: 20px; font-weight: 700; }

.modal-form { display: flex; flex-direction: column; gap: 14px; margin-bottom: 20px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #555; }
.req { color: #FF4D4F; }
.form-row { display: flex; gap: 14px; }
.form-group.half { flex: 1; }

.form-input {
  padding: 10px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
  transition: border-color 0.2s;
}
.form-input:focus { outline: none; border-color: #0096FA; box-shadow: 0 0 0 3px rgba(0,150,250,0.08); }

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 999px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit {
  padding: 10px 28px;
  border: none;
  background: #0096FA;
  color: #fff;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-submit:hover { background: #0080d5; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .plan-grid { grid-template-columns: 1fr; }
  .form-row { flex-direction: column; gap: 14px; }
}
</style>
