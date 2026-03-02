<template>
  <div class="create-commission-page">
    <div class="page-container">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
        返回
      </button>

      <div class="form-card">
        <h1 class="form-title">发起约稿委托</h1>
        <p class="form-desc">向画师提交你的约稿需求，画师会给出报价</p>

        <!-- 画师信息 -->
        <div v-if="artist" class="artist-preview">
          <el-avatar :size="48" :src="artist.avatarUrl || defaultAvatar">
            {{ (artist.nickname || '?').charAt(0) }}
          </el-avatar>
          <div class="artist-info">
            <span class="artist-name">{{ artist.nickname }}</span>
            <span class="artist-label">目标画师</span>
          </div>
        </div>

        <!-- 约稿方案选择 -->
        <div v-if="plans.length > 0" class="section">
          <label class="section-label">选择约稿方案（可选）</label>
          <div class="plan-list">
            <div
              v-for="p in plans"
              :key="p.id"
              class="plan-card"
              :class="{ selected: form.commissionPlanId === p.id }"
              @click="selectPlan(p)"
            >
              <div class="plan-head">
                <span class="plan-title">{{ p.title }}</span>
                <span class="plan-price">¥{{ p.priceStart }}<span v-if="p.priceEnd"> - ¥{{ p.priceEnd }}</span></span>
              </div>
              <p class="plan-desc">{{ p.description }}</p>
              <div class="plan-meta">
                <span v-if="p.estimatedDays">{{ p.estimatedDays }}天</span>
                <span v-if="p.revisionsIncluded">{{ p.revisionsIncluded }}次修改</span>
                <span v-if="p.category">{{ p.category }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 表单 -->
        <div class="section">
          <div class="form-group">
            <label>约稿标题 <span class="required">*</span></label>
            <input v-model="form.title" class="form-input" placeholder="简要描述你的需求，如「原创角色立绘」" maxlength="100" />
          </div>

          <div class="form-group">
            <label>详细描述 <span class="required">*</span></label>
            <textarea v-model="form.description" class="form-textarea" rows="5" placeholder="详细描述你想要的画面内容、风格要求、用途等（至少10字）..." maxlength="5000"></textarea>
            <span class="char-count" :class="{ warn: form.description.length > 0 && form.description.length < 10 }">{{ form.description.length }} / 5000（最少10字）</span>
          </div>

          <div class="form-row">
            <div class="form-group half">
              <label>预算金额（元）</label>
              <input v-model.number="form.budget" type="number" class="form-input" placeholder="你的预算参考" min="0" />
            </div>
            <div class="form-group half">
              <label>期望完成日期</label>
              <input v-model="form.deadline" type="date" class="form-input" :min="minDate" />
            </div>
          </div>

          <div class="form-group">
            <label>参考图片/链接</label>
            <textarea v-model="form.referenceUrls" class="form-textarea" rows="2" placeholder="提供参考图片链接，多个链接用换行分隔"></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn-cancel" @click="$router.back()">取消</button>
          <button class="btn-submit" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交约稿请求' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createCommission } from '@/api/commission'
import { getArtistPlans } from '@/api/payment'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const artistId = Number(route.params.artistId)
const artist = ref(null)
const plans = ref([])
const submitting = ref(false)

const form = ref({
  targetUserId: artistId,
  title: '',
  description: '',
  budget: null,
  deadline: '',
  referenceUrls: '',
  commissionPlanId: null
})

const minDate = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().substring(0, 10)
})

onMounted(async () => {
  // 加载画师信息
  try {
    const res = await request({ url: `/api/users/${artistId}`, method: 'get' })
    if (res.code === 200) {
      artist.value = res.data
    }
  } catch {}

  // 加载画师约稿方案
  try {
    const res = await getArtistPlans(artistId)
    if (res.code === 200) {
      plans.value = res.data || []
    }
  } catch {}
})

function selectPlan(p) {
  if (form.value.commissionPlanId === p.id) {
    form.value.commissionPlanId = null
    return
  }
  form.value.commissionPlanId = p.id
  if (!form.value.title && p.title) {
    form.value.title = p.title
  }
  if (p.priceStart && !form.value.budget) {
    form.value.budget = p.priceStart
  }
}

async function handleSubmit() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请输入约稿标题')
    return
  }
  if (!form.value.description?.trim()) {
    ElMessage.warning('请输入详细描述')
    return
  }
  if (form.value.description.trim().length < 10) {
    ElMessage.warning('详细描述至少需要 10 个字符')
    return
  }

  submitting.value = true
  try {
    const data = {
      targetUserId: artistId,
      title: form.value.title.trim(),
      description: form.value.description.trim(),
    }
    if (form.value.budget) data.budget = form.value.budget
    if (form.value.deadline) data.deadline = form.value.deadline + 'T23:59:59'
    if (form.value.referenceUrls?.trim()) data.referenceUrls = form.value.referenceUrls.trim()
    if (form.value.commissionPlanId) data.commissionPlanId = form.value.commissionPlanId

    const res = await createCommission(data)
    if (res.code === 200) {
      ElMessage.success('约稿请求已提交，等待画师报价')
      router.push('/commissions')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交约稿请求失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-commission-page {
  min-height: calc(100vh - 64px);
  background: #f5f5f5;
  padding: 24px;
}
.page-container {
  max-width: 700px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s;
}
.back-btn:hover { color: #333; background: #f0f0f0; }

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px;
}
.form-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 24px;
}

/* 画师预览 */
.artist-preview {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;
}
.artist-info {
  display: flex;
  flex-direction: column;
}
.artist-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}
.artist-label {
  font-size: 12px;
  color: #999;
}

/* 约稿方案 */
.section {
  margin-bottom: 24px;
}
.section-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.plan-card {
  padding: 14px 18px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.plan-card:hover {
  border-color: #b3dcff;
}
.plan-card.selected {
  border-color: #0096FA;
  background: #f0f8ff;
}
.plan-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.plan-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.plan-price {
  font-size: 15px;
  font-weight: 700;
  color: #0096FA;
}
.plan-desc {
  font-size: 13px;
  color: #888;
  margin: 0 0 8px;
  line-height: 1.4;
}
.plan-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #aaa;
}

/* 表单 */
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.required { color: #FF4D4F; }

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #0096FA;
  box-shadow: 0 0 0 3px rgba(0,150,250,0.1);
}
.form-textarea {
  resize: vertical;
  font-family: inherit;
  line-height: 1.5;
}
.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.char-count.warn {
  color: #FF4D4F;
}
.form-row {
  display: flex;
  gap: 16px;
}
.form-group.half {
  flex: 1;
}

/* 操作 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit {
  padding: 10px 32px;
  border: none;
  background: #0096FA;
  color: #fff;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-submit:hover { background: #0080d5; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .create-commission-page { padding: 12px; }
  .form-card { padding: 20px; }
  .form-row { flex-direction: column; gap: 0; }
}
</style>
