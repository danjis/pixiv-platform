<template>
  <div class="create-page">
    <div class="page-wrap">
      <button class="back-btn" @click="$router.back()">返回</button>
      <div class="step-header">
        <div v-for="(s, i) in steps" :key="i" class="step-item" :class="{ active: currentStep === i, done: currentStep > i }">
          <div class="step-circle">
            <svg v-if="currentStep > i" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s.label }}</span>
          <div v-if="i < steps.length - 1" class="step-connector" :class="{ done: currentStep > i }"></div>
        </div>
      </div>
      <div class="form-card">
        <div v-if="artist" class="artist-banner">
          <el-avatar :size="52" :src="artist.avatarUrl || defaultAvatar"></el-avatar>
          <div class="ab-info">
            <span class="ab-label">目标画师</span>
            <span class="ab-name">{{ artist.nickname }}</span>
          </div>
          <div class="ab-badge">约稿中</div>
        </div>
        <transition name="step-fade" mode="out-in">
        <div v-if="currentStep === 0" key="step0">
          <div class="step-title-wrap">
            <span class="step-num">01</span>
            <div><h2 class="step-title">选择约稿方案</h2><p class="step-sub">选择画师提供的方案，或直接跳过自定义需求</p></div>
          </div>
          <div v-if="plans.length > 0" class="plan-grid">
            <div v-for="p in plans" :key="p.id" class="plan-card" :class="{ selected: form.commissionPlanId === p.id }" @click="selectPlan(p)">
              <div class="pc-top">
                <span class="pc-title">{{ p.title }}</span>
                <span class="pc-price">¥{{ p.priceStart }}<span v-if="p.priceEnd"> ~ ¥{{ p.priceEnd }}</span></span>
              </div>
              <p class="pc-desc">{{ p.description }}</p>
              <div class="pc-tags">
                <span v-if="p.estimatedDays" class="pc-tag">{{ p.estimatedDays }}天</span>
                <span v-if="p.revisionsIncluded" class="pc-tag">{{ p.revisionsIncluded }}次修改</span>
              </div>
            </div>
          </div>
          <div v-else class="no-plans"><p>该画师暂未设置方案，请直接填写需求</p></div>
          <div class="step-actions">
            <button class="btn-skip" @click="currentStep = 1">跳过，直接填写</button>
            <button class="btn-next" @click="currentStep = 1">下一步</button>
          </div>
        </div>
        </transition>
        <transition name="step-fade" mode="out-in">
        <div v-if="currentStep === 1" key="step1">
          <div class="step-title-wrap">
            <span class="step-num">02</span>
            <div><h2 class="step-title">描述需求</h2><p class="step-sub">详细说明你想要的画面、风格与用途</p></div>
          </div>
          <div class="tips-panel">
            <div class="tips-title">灵感模板</div>
            <div class="tips-actions">
              <button class="tip-pill" @click="applyTemplate('character')">角色立绘</button>
              <button class="tip-pill" @click="applyTemplate('scene')">场景插画</button>
              <button class="tip-pill" @click="applyTemplate('avatar')">头像定制</button>
            </div>
          </div>
          <div class="fields">
            <div class="field-group" :class="{ focused: focusedField === 'title', 'has-error': titleError }">
              <label class="field-label">约稿标题 <span class="req">*</span></label>
              <input v-model="form.title" class="field-input" placeholder="简要描述需求" maxlength="100" @focus="focusedField = 'title'" @blur="focusedField = ''; validateTitle()" />
              <div class="field-hint-row">
                <span class="field-error" v-if="titleError">{{ titleError }}</span>
                <span class="char-hint">{{ form.title.length }} / 100</span>
              </div>
            </div>
            <div class="field-group textarea-group" :class="{ focused: focusedField === 'desc', 'has-error': descError }">
              <label class="field-label">需求描述 <span class="req">*</span></label>
              <textarea v-model="form.description" class="field-textarea" placeholder="请详细描述你的需求：角色特征、场景设定、风格偏好、用途等..." rows="5" maxlength="2000" @focus="focusedField = 'desc'" @blur="focusedField = ''; validateDesc()"></textarea>
              <div class="field-hint-row">
                <span class="field-error" v-if="descError">{{ descError }}</span>
                <span class="char-hint">{{ form.description.length }} / 2000</span>
              </div>
            </div>
            <div class="field-group">
              <label class="field-label">风格偏好</label>
              <div class="style-tags">
                <button v-for="tag in styleTags" :key="tag" class="style-tag" :class="{ active: form.styleTag === tag }" @click="form.styleTag = form.styleTag === tag ? '' : tag">{{ tag }}</button>
              </div>
            </div>
            <div class="field-group">
              <label class="field-label">预算范围（元）</label>
              <div class="budget-row">
                <input v-model.number="form.budgetMin" class="field-input budget-input" type="number" min="0" placeholder="最低" />
                <span class="budget-sep">~</span>
                <input v-model.number="form.budgetMax" class="field-input budget-input" type="number" min="0" placeholder="最高" />
              </div>
            </div>
            <div class="field-group">
              <label class="field-label">期望完成时间</label>
              <input v-model="form.deadline" class="field-input" type="date" :min="minDate" />
            </div>
            <div class="field-group">
              <label class="field-label">参考图片</label>
              <div class="ref-upload-area" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
                <input ref="fileInput" type="file" accept="image/*" multiple class="hidden-input" @change="handleFileChange" />
                <div v-if="form.referenceImages.length === 0" class="upload-placeholder">
                  <p>点击或拖拽上传参考图片</p>
                  <span>支持 JPG、PNG，最多5张</span>
                </div>
                <div v-else class="ref-images">
                  <div v-for="(img, idx) in form.referenceImages" :key="idx" class="ref-img-wrap">
                    <img :src="img.preview" class="ref-img" />
                    <button class="ref-remove" @click.stop="removeImage(idx)">x</button>
                  </div>
                  <div v-if="form.referenceImages.length < 5" class="ref-add" @click.stop="triggerUpload">+</div>
                </div>
              </div>
            </div>
            <div class="field-group">
              <label class="field-label">其他备注</label>
              <textarea v-model="form.remark" class="field-textarea" placeholder="其他特别说明或要求..." rows="2" maxlength="500"></textarea>
            </div>
          </div>
          <div class="step-actions">
            <button class="btn-back" @click="currentStep = 0">上一步</button>
            <button class="btn-next" @click="goStep2">下一步</button>
          </div>
        </div>
        </transition>
        <transition name="step-fade" mode="out-in">
        <div v-if="currentStep === 2" key="step2">
          <div class="step-title-wrap">
            <span class="step-num">03</span>
            <div><h2 class="step-title">确认提交</h2><p class="step-sub">请核对以下信息，确认无误后提交</p></div>
          </div>
          <div class="confirm-card">
            <div v-if="selectedPlan" class="confirm-section">
              <p class="cs-label">选择的方案</p>
              <p class="cs-value">{{ selectedPlan.title }} · ¥{{ selectedPlan.priceStart }}</p>
            </div>
            <div class="confirm-section"><p class="cs-label">约稿标题</p><p class="cs-value">{{ form.title }}</p></div>
            <div class="confirm-section"><p class="cs-label">需求描述</p><p class="cs-value desc-value">{{ form.description }}</p></div>
            <div v-if="form.styleTag" class="confirm-section"><p class="cs-label">风格偏好</p><p class="cs-value"><span class="style-badge">{{ form.styleTag }}</span></p></div>
            <div v-if="form.deadline" class="confirm-section"><p class="cs-label">期望完成时间</p><p class="cs-value">{{ form.deadline }}</p></div>
            <div v-if="artist" class="confirm-section">
              <p class="cs-label">目标画师</p>
              <div class="confirm-artist">
                <el-avatar :size="32" :src="artist.avatarUrl || defaultAvatar"></el-avatar>
                <span class="ca-name">{{ artist.nickname }}</span>
              </div>
            </div>
          </div>
          <div class="step-actions">
            <button class="btn-back" @click="currentStep = 1">上一步</button>
            <button class="btn-submit" @click="submitCommission" :disabled="submitting">{{ submitting ? '提交中...' : '确认提交' }}</button>
          </div>
        </div>
        </transition>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCommissionPlans } from '@/api/commissionPlan'
import { createCommission } from '@/api/commission'
import { getUserProfile } from '@/api/user'
import { uploadImages } from '@/api/file'

const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const steps = [{ label: '选择方案' }, { label: '填写需求' }, { label: '确认提交' }]
const currentStep = ref(0)
const submitting = ref(false)
const focusedField = ref('')
const titleError = ref('')
const descError = ref('')
const artist = ref(null)
const plans = ref([])
const selectedPlan = ref(null)
const fileInput = ref(null)

const form = ref({
  title: '',
  description: '',
  styleTag: '',
  budgetMin: '',
  budgetMax: '',
  deadline: '',
  remark: '',
  referenceImages: [],
  commissionPlanId: null
})

const styleTags = ['写实', '动漫', '像素风', '水彩', '油画', '赛博朋克', '国风', '扁平', '萌系', '黑白']

const minDate = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

function validateTitle() {
  if (!form.value.title.trim()) { titleError.value = '请填写约稿标题'; return false }
  if (form.value.title.length < 2) { titleError.value = '标题至少2个字符'; return false }
  titleError.value = ''; return true
}

function validateDesc() {
  if (!form.value.description.trim()) { descError.value = '请填写需求描述'; return false }
  if (form.value.description.length < 10) { descError.value = '描述至少10个字符'; return false }
  descError.value = ''; return true
}

function goStep2() {
  if (!validateTitle() || !validateDesc()) return
  currentStep.value = 2
}

function selectPlan(p) {
  form.value.commissionPlanId = p.id
  selectedPlan.value = p
  // 预填充方案信息到表单
  if (p.title) form.value.title = p.title
  if (p.description) form.value.description = p.description
  if (p.priceStart) form.value.budgetMin = p.priceStart
  if (p.priceEnd) form.value.budgetMax = p.priceEnd
  if (p.estimatedDays) {
    const d = new Date()
    d.setDate(d.getDate() + p.estimatedDays)
    form.value.deadline = d.toISOString().split('T')[0]
  }
}

function triggerUpload() { fileInput.value && fileInput.value.click() }

function handleFileChange(e) { addFiles(Array.from(e.target.files)); e.target.value = '' }

function handleDrop(e) { addFiles(Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))) }

function addFiles(files) {
  const remaining = 5 - form.value.referenceImages.length
  files.slice(0, remaining).forEach(file => {
    const reader = new FileReader()
    reader.onload = ev => form.value.referenceImages.push({ file, preview: ev.target.result })
    reader.readAsDataURL(file)
  })
}

function removeImage(idx) { form.value.referenceImages.splice(idx, 1) }

function applyTemplate(type) {
  const templates = {
    character: '角色设定：\n- 人物性别/年龄：\n- 发型发色：\n- 服装风格：\n- 动作表情：\n- 画面比例：',
    scene: '场景设定：\n- 场景主题：\n- 时间天气：\n- 主体元素：\n- 色调氛围：\n- 用途：',
    avatar: '头像需求：\n- 人设关键词：\n- 背景元素：\n- 风格参考：\n- 是否需要透明底：'
  }
  form.value.description = templates[type] || form.value.description
}

async function submitCommission() {
  if (!validateTitle() || !validateDesc()) { currentStep.value = 1; return }
  submitting.value = true
  try {
    const artistId = route.query.artistId
    let referenceUrls = null

    if (form.value.referenceImages.length > 0) {
      const files = form.value.referenceImages.map(item => item.file).filter(Boolean)
      if (files.length > 0) {
        const uploadRes = await uploadImages(files)
        if (uploadRes.code === 200 && Array.isArray(uploadRes.data)) {
          const urls = uploadRes.data
            .map(item => item.imageUrl || item.originalImageUrl || item.thumbnailUrl)
            .filter(Boolean)
          referenceUrls = urls.length > 0 ? urls.join('\n') : null
        } else {
          throw new Error(uploadRes.message || '参考图上传失败')
        }
      }
    }

    const payload = {
      targetUserId: Number(artistId),
      title: form.value.title,
      description: form.value.description,
      styleTag: form.value.styleTag || null,
      budget: form.value.budgetMin || null,
      budgetMax: form.value.budgetMax || null,
      deadline: form.value.deadline || null,
      remark: form.value.remark || null,
      referenceUrls,
      commissionPlanId: form.value.commissionPlanId || null
    }
    const res = await createCommission(payload)
    if (res.code === 200) {
      ElMessage.success('约稿提交成功！等待画师报价')
      router.push({ name: 'CommissionDetail', params: { id: res.data.id } })
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch { ElMessage.error('网络错误，请稍后重试') } finally { submitting.value = false }
}

onMounted(async () => {
  const artistId = route.query.artistId
  if (artistId) {
    try { const r = await getUserProfile(artistId); if (r.code === 200) artist.value = r.data } catch {}
    try {
      const r = await getCommissionPlans({ artistId, page: 1, size: 20 })
      if (r.code === 200) {
        plans.value = Array.isArray(r.data) ? r.data : (r.data?.records || [])
      }
    } catch {}
  }
})
</script>

<style scoped>
.create-page { min-height: calc(100vh - 64px); background: radial-gradient(1200px 450px at 10% 0%, #eef5ff 0%, transparent 60%), radial-gradient(1000px 350px at 90% 0%, #f8f0ff 0%, transparent 55%), #fff; padding: 40px 0 80px; }
.page-wrap { max-width: 780px; margin: 0 auto; padding: 0 24px; }
.back-btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1.5px solid #e8e8e8; border-radius: 999px; background: #fff; font-size: 14px; font-weight: 600; color: #888; cursor: pointer; margin-bottom: 28px; transition: all .18s; }
.back-btn:hover { border-color: #b0b0b0; color: #333; background: #fafafa; }
.step-header { display: flex; align-items: center; margin-bottom: 28px; }
.step-item { display: flex; align-items: center; }
.step-circle { width: 32px; height: 32px; border-radius: 50%; border: 1.5px solid #e0e0e0; background: #fafafa; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; color: #aaa; transition: all .3s; flex-shrink: 0; }
.step-item.active .step-circle { border-color: #333; background: #333; color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.10); }
.step-item.done .step-circle { border-color: #333; background: #333; color: #fff; }
.step-label { font-size: 13px; font-weight: 600; color: #aaa; margin-left: 8px; white-space: nowrap; }
.step-item.active .step-label { color: #333; }
.step-item.done .step-label { color: #333; }
.step-connector { flex: 1; height: 1.5px; background: #e0e0e0; margin: 0 12px; min-width: 24px; transition: background .3s; }
.step-connector.done { background: #333; }
.form-card { background: #fff; border-radius: 20px; padding: 36px; box-shadow: 0 14px 40px rgba(32, 54, 106, 0.08); border: 1px solid #eef2ff; }
.artist-banner { display: flex; align-items: center; gap: 14px; background: #fafafa; border: 1px solid #eee; border-radius: 20px; padding: 16px 20px; margin-bottom: 28px; }
.ab-info { flex: 1; display: flex; flex-direction: column; gap: 3px; }
.ab-label { font-size: 11px; color: #999; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; }
.ab-name { font-size: 15px; font-weight: 800; color: #1a1a1a; }
.ab-badge { padding: 4px 12px; background: linear-gradient(135deg, #6c63ff, #4f46e5); color: #fff; border-radius: 999px; font-size: 12px; font-weight: 700; }
.step-title-wrap { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 28px; }
.step-num { font-size: 40px; font-weight: 900; color: #eee; line-height: 1; flex-shrink: 0; }
.step-title { font-size: 22px; font-weight: 900; color: #1a1a1a; margin: 0 0 4px; }
.step-sub { font-size: 14px; color: #999; margin: 0; }
.tips-panel { margin: -10px 0 18px; padding: 12px 14px; border: 1px solid #e9edff; background: #f8f9ff; border-radius: 14px; }
.tips-title { font-size: 12px; font-weight: 700; color: #667; margin-bottom: 8px; }
.tips-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.tip-pill { padding: 5px 12px; border: 1px solid #d9deff; border-radius: 999px; background: #fff; color: #4f46e5; font-size: 12px; font-weight: 600; cursor: pointer; transition: all .18s; }
.tip-pill:hover { border-color: #4f46e5; background: #f2f4ff; }
.plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; margin-bottom: 28px; }
.plan-card { border: 1.5px solid #eee; border-radius: 20px; padding: 18px; cursor: pointer; transition: all .22s; position: relative; }
.plan-card:hover { border-color: #ccc; box-shadow: 0 2px 16px rgba(0,0,0,0.04); }
.plan-card.selected { border-color: #4f46e5; background: #f7f7ff; box-shadow: 0 2px 16px rgba(79,70,229,0.12); }
.pc-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.pc-title { font-size: 15px; font-weight: 800; color: #1a1a1a; }
.pc-price { font-size: 14px; font-weight: 700; color: #4f46e5; }
.pc-desc { font-size: 13px; color: #888; margin: 0 0 10px; line-height: 1.5; }
.pc-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.pc-tag { padding: 3px 10px; background: #f5f5f5; border-radius: 999px; font-size: 12px; color: #888; }
.no-plans { text-align: center; padding: 40px 0; color: #999; }
.fields { display: flex; flex-direction: column; gap: 22px; margin-bottom: 28px; }
.field-group { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 13px; font-weight: 700; color: #444; }
.req { color: #e74c3c; }
.field-input { width: 100%; padding: 11px 14px; border: 1.5px solid #e8e8e8; border-radius: 12px; font-size: 14px; color: #1a1a1a; background: #f8f8f8; transition: all .18s; box-sizing: border-box; outline: none; }
.field-input:focus { border-color: #bbb; background: #fff; box-shadow: 0 0 0 3px rgba(0,0,0,.03); }
.field-group.focused .field-input, .field-group.focused .field-textarea { border-color: #bbb; background: #fff; box-shadow: 0 0 0 3px rgba(0,0,0,.03); }
.field-group.has-error .field-input, .field-group.has-error .field-textarea { border-color: #e74c3c; }
.field-textarea { width: 100%; padding: 11px 14px; border: 1.5px solid #e8e8e8; border-radius: 12px; font-size: 14px; color: #1a1a1a; background: #f8f8f8; resize: vertical; transition: all .18s; box-sizing: border-box; outline: none; font-family: inherit; }
.field-hint-row { display: flex; justify-content: space-between; align-items: center; }
.field-error { font-size: 12px; color: #e74c3c; font-weight: 600; }
.char-hint { font-size: 12px; color: #aaa; margin-left: auto; }
.style-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.style-tag { padding: 6px 14px; border: 1.5px solid #e8e8e8; border-radius: 999px; background: #fff; font-size: 13px; color: #888; cursor: pointer; transition: all .18s; }
.style-tag:hover { border-color: #bbb; color: #333; }
.style-tag.active { background: #333; border-color: #333; color: #fff; }
.budget-row { display: flex; align-items: center; gap: 10px; }
.budget-input { flex: 1; }
.budget-sep { font-size: 16px; color: #aaa; flex-shrink: 0; }
.ref-upload-area { border: 1.5px dashed #ddd; border-radius: 16px; padding: 24px; cursor: pointer; transition: all .18s; background: #fafafa; }
.ref-upload-area:hover { border-color: #bbb; background: #f5f5f5; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #aaa; font-size: 14px; }
.upload-placeholder span { font-size: 12px; }
.hidden-input { display: none; }
.ref-images { display: flex; flex-wrap: wrap; gap: 10px; }
.ref-img-wrap { position: relative; width: 80px; height: 80px; border-radius: 12px; overflow: hidden; }
.ref-img { width: 100%; height: 100%; object-fit: cover; }
.ref-remove { position: absolute; top: 3px; right: 3px; width: 20px; height: 20px; border-radius: 50%; background: rgba(0,0,0,.5); color: #fff; border: none; cursor: pointer; font-size: 14px; display: flex; align-items: center; justify-content: center; line-height: 1; }
.ref-add { width: 80px; height: 80px; border: 1.5px dashed #ddd; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #bbb; cursor: pointer; transition: all .18s; }
.ref-add:hover { background: #f5f5f5; border-color: #bbb; }
.confirm-card { background: #fafafa; border: 1px solid #eee; border-radius: 20px; padding: 24px; margin-bottom: 28px; display: flex; flex-direction: column; gap: 16px; }
.confirm-section { display: flex; flex-direction: column; gap: 4px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.confirm-section:last-child { border-bottom: none; padding-bottom: 0; }
.cs-label { font-size: 11px; font-weight: 700; color: #aaa; letter-spacing: 1px; text-transform: uppercase; margin: 0; }
.cs-value { font-size: 15px; font-weight: 600; color: #1a1a1a; margin: 0; }
.desc-value { font-size: 14px; line-height: 1.6; color: #666; white-space: pre-wrap; }
.style-badge { padding: 3px 12px; background: #f0f0f0; color: #333; border-radius: 999px; font-size: 13px; font-weight: 700; }
.confirm-artist { display: flex; align-items: center; gap: 10px; }
.ca-name { font-size: 15px; font-weight: 700; color: #1a1a1a; }
.step-actions { display: flex; justify-content: flex-end; gap: 12px; padding-top: 24px; border-top: 1px solid #f0f0f0; margin-top: 8px; }
.btn-skip, .btn-back { padding: 10px 22px; border: 1.5px solid #e8e8e8; border-radius: 999px; background: #fff; font-size: 14px; font-weight: 600; color: #888; cursor: pointer; transition: all .18s; }
.btn-skip:hover, .btn-back:hover { border-color: #bbb; color: #333; }
.btn-next { padding: 10px 28px; background: linear-gradient(135deg, #4f46e5, #7c3aed); border: none; border-radius: 999px; font-size: 14px; font-weight: 700; color: #fff; cursor: pointer; box-shadow: 0 8px 22px rgba(79,70,229,.28); transition: all .2s; display: inline-flex; align-items: center; gap: 6px; }
.btn-next:hover { transform: translateY(-1px); box-shadow: 0 10px 24px rgba(79,70,229,.34); }
.btn-submit { padding: 11px 32px; background: linear-gradient(135deg, #4f46e5, #7c3aed); border: none; border-radius: 999px; font-size: 14px; font-weight: 700; color: #fff; cursor: pointer; box-shadow: 0 8px 22px rgba(79,70,229,.28); transition: all .2s; }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 10px 24px rgba(79,70,229,.34); }
.btn-submit:disabled { opacity: .5; cursor: not-allowed; }
.step-fade-enter-active, .step-fade-leave-active { transition: all .25s ease; }
.step-fade-enter-from { opacity: 0; transform: translateX(20px); }
.step-fade-leave-to { opacity: 0; transform: translateX(-20px); }
@media (max-width: 600px) {
  .form-card { padding: 20px; }
  .plan-grid { grid-template-columns: 1fr; }
  .step-actions { flex-direction: column; }
  .btn-next, .btn-submit { width: 100%; justify-content: center; }
}
</style>
