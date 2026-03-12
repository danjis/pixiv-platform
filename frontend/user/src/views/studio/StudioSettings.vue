<template>
  <div class="settings-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">画师设置</h1>
        <p class="page-desc">管理你的画师资料与接稿偏好</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <template v-else>
      <!-- 双栏布局 -->
      <div class="settings-grid">
        <!-- 左栏 -->
        <div class="settings-col">
          <!-- 接稿状态 -->
          <div class="setting-card">
            <div class="card-header">
              <div class="card-title-area">
                <h3 class="card-title">接稿状态</h3>
                <p class="card-desc">开启后其他用户可以向你发起约稿</p>
              </div>
              <div class="toggle-area">
                <button
                  class="toggle-btn"
                  :class="{ active: commissionOpen }"
                  @click="toggleCommissionStatus"
                >
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ commissionOpen ? '接稿中' : '暂停接稿' }}</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 画师简介 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">画师简介</h3>
            </div>
            <div class="card-body">
              <div class="form-group">
                <label>个人介绍</label>
                <textarea
                  v-model="form.bio"
                  class="form-textarea"
                  rows="4"
                  placeholder="介绍一下自己，让委托方更了解你的风格和特长..."
                  maxlength="500"
                ></textarea>
                <span class="char-count">{{ (form.bio || '').length }}/500</span>
              </div>
            </div>
          </div>

          <!-- 擅长风格 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">擅长风格</h3>
              <p class="card-desc">添加你擅长的创作风格标签，方便委托方找到你</p>
            </div>
            <div class="card-body">
              <div class="tags-area">
                <span v-for="(tag, i) in form.specialties" :key="i" class="tag">
                  {{ tag }}
                  <button class="tag-remove" @click="removeTag(i)">×</button>
                </span>
                <div class="tag-input-wrapper" v-if="form.specialties.length < 10">
                  <input
                    v-model="newTag"
                    class="tag-input"
                    placeholder="输入标签后回车"
                    @keydown.enter.prevent="addTag"
                    maxlength="20"
                  />
                </div>
              </div>
              <div class="tag-presets">
                <span class="preset-label">推荐：</span>
                <button
                  v-for="p in presetTags"
                  :key="p"
                  class="preset-btn"
                  :class="{ selected: form.specialties.includes(p) }"
                  @click="togglePresetTag(p)"
                >
                  {{ p }}
                </button>
              </div>
            </div>
          </div>

          <!-- 隐私设置 -->
          <div class="setting-card">
            <div class="card-header">
              <div class="card-title-area">
                <h3 class="card-title">隐私设置</h3>
                <p class="card-desc">控制你的主页上哪些信息对其他人可见</p>
              </div>
            </div>
            <div class="card-body">
              <div class="privacy-item">
                <div class="privacy-info">
                  <span class="privacy-label">隐藏关注列表</span>
                  <span class="privacy-desc">开启后，其他用户无法查看你的关注列表</span>
                </div>
                <button
                  class="toggle-btn"
                  :class="{ active: privacySettings.hideFollowing }"
                  @click="privacySettings.hideFollowing = !privacySettings.hideFollowing"
                >
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ privacySettings.hideFollowing ? '已隐藏' : '公开' }}</span>
                </button>
              </div>
              <div class="privacy-item">
                <div class="privacy-info">
                  <span class="privacy-label">隐藏收藏列表</span>
                  <span class="privacy-desc">开启后，其他用户无法查看你的收藏列表</span>
                </div>
                <button
                  class="toggle-btn"
                  :class="{ active: privacySettings.hideFavorites }"
                  @click="privacySettings.hideFavorites = !privacySettings.hideFavorites"
                >
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ privacySettings.hideFavorites ? '已隐藏' : '公开' }}</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 通知偏好 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">通知偏好</h3>
              <p class="card-desc">约稿相关通知设置</p>
            </div>
            <div class="card-body">
              <div class="privacy-item">
                <div class="privacy-info">
                  <span class="privacy-label">新约稿通知</span>
                  <span class="privacy-desc">收到新的约稿请求时通知我</span>
                </div>
                <button class="toggle-btn" :class="{ active: notifications.newCommission }"
                  @click="notifications.newCommission = !notifications.newCommission">
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ notifications.newCommission ? '开启' : '关闭' }}</span>
                </button>
              </div>
              <div class="privacy-item">
                <div class="privacy-info">
                  <span class="privacy-label">消息提醒</span>
                  <span class="privacy-desc">委托方发来新消息时通知我</span>
                </div>
                <button class="toggle-btn" :class="{ active: notifications.message }"
                  @click="notifications.message = !notifications.message">
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ notifications.message ? '开启' : '关闭' }}</span>
                </button>
              </div>
              <div class="privacy-item">
                <div class="privacy-info">
                  <span class="privacy-label">收款通知</span>
                  <span class="privacy-desc">收到约稿付款时通知我</span>
                </div>
                <button class="toggle-btn" :class="{ active: notifications.payment }"
                  @click="notifications.payment = !notifications.payment">
                  <span class="toggle-dot"></span>
                  <span class="toggle-text">{{ notifications.payment ? '开启' : '关闭' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右栏 -->
        <div class="settings-col">
          <!-- 价格区间 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">价格参考</h3>
              <p class="card-desc">设置你的约稿参考价格，展示在你的个人主页</p>
            </div>
            <div class="card-body">
              <div class="price-row">
                <div class="form-group half">
                  <label>最低价格 (¥)</label>
                  <input v-model.number="form.minPrice" type="number" class="form-input" placeholder="0" min="0" />
                </div>
                <span class="price-sep">—</span>
                <div class="form-group half">
                  <label>最高价格 (¥)</label>
                  <input v-model.number="form.maxPrice" type="number" class="form-input" placeholder="0" min="0" />
                </div>
              </div>
              <div class="price-preview">
                <div class="preview-label">主页展示效果</div>
                <div class="preview-box">
                  <span class="preview-price" v-if="form.minPrice || form.maxPrice">
                    ¥{{ form.minPrice || 0 }} ~ ¥{{ form.maxPrice || 0 }}
                  </span>
                  <span class="preview-empty" v-else>未设置价格范围</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 联系方式 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">联系偏好</h3>
              <p class="card-desc">委托方在约稿时可看到你的首选联系方式</p>
            </div>
            <div class="card-body">
              <div class="form-group">
                <label>首选联系方式</label>
                <div class="contact-options">
                  <label v-for="opt in contactOptions" :key="opt.value"
                    class="contact-option" :class="{ selected: form.contactPreference === opt.value }">
                    <input type="radio" :value="opt.value" v-model="form.contactPreference" class="hidden-radio" />
                    <span class="contact-icon">{{ opt.icon }}</span>
                    <span class="contact-name">{{ opt.label }}</span>
                  </label>
                </div>
              </div>
              <div v-if="form.contactPreference !== 'platform'" class="form-group">
                <label>{{ contactLabel }}</label>
                <input v-model="form.contactInfo" class="form-input" :placeholder="contactPlaceholder" />
              </div>
            </div>
          </div>

          <!-- 约稿偏好 -->
          <div class="setting-card">
            <div class="card-header">
              <h3 class="card-title">约稿偏好</h3>
              <p class="card-desc">让委托方了解你的创作偏好</p>
            </div>
            <div class="card-body">
              <div class="form-group">
                <label>工作日程</label>
                <select v-model="form.workSchedule" class="form-select">
                  <option value="fulltime">全职接稿</option>
                  <option value="parttime">兼职接稿（工作日晚间/周末）</option>
                  <option value="weekend">仅周末</option>
                  <option value="flexible">灵活安排</option>
                </select>
              </div>
              <div class="form-group">
                <label>平均交付周期</label>
                <select v-model="form.deliveryTime" class="form-select">
                  <option value="3days">3 天内</option>
                  <option value="1week">1 周内</option>
                  <option value="2weeks">2 周内</option>
                  <option value="1month">1 个月内</option>
                  <option value="custom">视复杂度而定</option>
                </select>
              </div>
              <div class="form-group">
                <label>接受的作品类型</label>
                <div class="checkbox-group">
                  <label v-for="wt in workTypes" :key="wt" class="check-item"
                    :class="{ checked: form.workTypes.includes(wt) }">
                    <input type="checkbox" :value="wt" v-model="form.workTypes" class="hidden-radio" />
                    <span class="check-text">{{ wt }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <!-- 保存 -->
      <div class="save-area">
        <button class="save-btn" @click="handleSave" :disabled="saving">
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
        <span v-if="lastSaved" class="save-hint">上次保存: {{ lastSaved }}</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser, updatePrivacySettings } from '@/api/user'
import { updateArtistSettings, getArtistSettings } from '@/api/studio'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(true)
const saving = ref(false)
const lastSaved = ref('')
const commissionOpen = ref(true)
const newTag = ref('')

const privacySettings = ref({
  hideFollowing: false,
  hideFavorites: false
})

const notifications = ref({
  newCommission: true,
  message: true,
  payment: true
})

const form = ref({
  bio: '',
  specialties: [],
  minPrice: null,
  maxPrice: null,
  contactPreference: 'platform',
  contactInfo: '',
  workSchedule: 'flexible',
  deliveryTime: 'custom',
  workTypes: ['立绘', '头像', '插画']
})

const presetTags = [
  '日系', '二次元', '写实', '水彩', '油画', '厚涂',
  'Q版', '像素', '立绘', '头像', '场景', '原创',
  '同人', '插画', 'Live2D', '漫画'
]

const workTypes = ['立绘', '头像', '插画', '漫画', '场景', 'Live2D', '表情包', '封面', 'LOGO']

const contactOptions = [
  { value: 'platform', label: '站内私信', icon: '💬' },
  { value: 'email', label: '邮箱', icon: '📧' },
  { value: 'qq', label: 'QQ', icon: '🐧' },
  { value: 'wechat', label: '微信', icon: '💚' }
]

const contactLabel = computed(() => {
  return { email: '邮箱地址', qq: 'QQ 号', wechat: '微信号' }[form.value.contactPreference] || '联系方式'
})

const contactPlaceholder = computed(() => {
  return { email: '你的邮箱', qq: '你的QQ号', wechat: '你的微信号' }[form.value.contactPreference] || ''
})

function addTag() {
  const tag = newTag.value.trim()
  if (tag && !form.value.specialties.includes(tag) && form.value.specialties.length < 10) {
    form.value.specialties.push(tag)
    newTag.value = ''
  }
}

function removeTag(index) {
  form.value.specialties.splice(index, 1)
}

function togglePresetTag(tag) {
  const idx = form.value.specialties.indexOf(tag)
  if (idx >= 0) {
    form.value.specialties.splice(idx, 1)
  } else if (form.value.specialties.length < 10) {
    form.value.specialties.push(tag)
  }
}

function toggleCommissionStatus() {
  commissionOpen.value = !commissionOpen.value
}

async function loadSettings() {
  loading.value = true
  try {
    // 并行加载用户信息和画师设置
    const [userRes, artistRes] = await Promise.all([
      getCurrentUser(),
      getArtistSettings().catch(() => null)
    ])

    if (userRes.code === 200) {
      const u = userRes.data
      form.value.bio = u.bio || ''
      privacySettings.value.hideFollowing = u.hideFollowing || false
      privacySettings.value.hideFavorites = u.hideFavorites || false
    }

    if (artistRes && artistRes.code === 200) {
      const a = artistRes.data
      form.value.specialties = a.specialties || []
      form.value.minPrice = a.minPrice || null
      form.value.maxPrice = a.maxPrice || null
      form.value.contactPreference = a.contactPreference || 'platform'
      form.value.contactInfo = a.contactInfo || ''
      commissionOpen.value = a.acceptingCommissions !== false
      // 如果画师有描述，优先使用
      if (a.description) {
        form.value.bio = a.description || form.value.bio
      }
    }
  } catch {
    ElMessage.error('加载设置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    // 保存画师设置（包括bio）
    const data = {
      bio: form.value.bio,
      specialties: form.value.specialties,
      minPrice: form.value.minPrice,
      maxPrice: form.value.maxPrice,
      contactPreference: form.value.contactPreference,
      contactInfo: form.value.contactInfo,
      commissionOpen: commissionOpen.value
    }
    const res = await updateArtistSettings(data)
    if (res.code === 200) {
      // 同时保存隐私设置
      try {
        await updatePrivacySettings({
          hideFollowing: privacySettings.value.hideFollowing,
          hideFavorites: privacySettings.value.hideFavorites
        })
      } catch {}
      ElMessage.success('设置已保存')
      lastSaved.value = new Date().toLocaleTimeString('zh-CN')
      // 更新 store
      userStore.updateUser({ bio: form.value.bio })
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<style scoped>
.settings-page { max-width: 100%; }

.page-header { margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px 0; }
.page-desc { font-size: 14px; color: #999; margin: 0; }

.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 双栏布局 */
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

/* 设置卡片 */
.setting-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f5;
  transition: box-shadow 0.2s;
}
.setting-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.card-title-area { flex: 1; }
.card-title { font-size: 16px; font-weight: 600; color: #1a1a1a; margin: 0; }
.card-desc { font-size: 13px; color: #999; margin: 4px 0 0; }
.card-body { margin-top: 16px; }

/* Toggle */
.toggle-area { flex-shrink: 0; }
.toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border: 2px solid #e0e0e0;
  background: #f5f5f5;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
  font-weight: 600;
  color: #999;
}
.toggle-btn.active {
  border-color: #00C48C;
  background: #E6F7F0;
  color: #00C48C;
}
.toggle-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ccc;
  transition: background 0.3s;
}
.toggle-btn.active .toggle-dot {
  background: #00C48C;
}

/* 表单 */
.form-group { margin-bottom: 14px; position: relative; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px;
}
.form-input, .form-textarea, .form-select {
  width: 100%; padding: 10px 14px; border: 1px solid #e0e0e0;
  border-radius: 8px; font-size: 14px; color: #333;
  transition: border-color 0.2s; box-sizing: border-box;
  background: #fff;
}
.form-input:focus, .form-textarea:focus, .form-select:focus {
  outline: none; border-color: #0096FA;
}
.form-textarea { resize: vertical; font-family: inherit; }
.form-select { appearance: auto; }
.char-count {
  position: absolute; right: 10px; bottom: 8px;
  font-size: 11px; color: #ccc;
}

/* 标签 */
.tags-area {
  display: flex; flex-wrap: wrap; gap: 8px;
  padding: 10px 14px;
  border: 1px solid #e0e0e0; border-radius: 8px;
  min-height: 44px; align-items: center;
}
.tag {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 10px; background: #EBF5FF; color: #0096FA;
  border-radius: 6px; font-size: 13px; font-weight: 500;
}
.tag-remove {
  background: none; border: none; color: #0096FA; font-size: 16px;
  cursor: pointer; padding: 0; line-height: 1; opacity: 0.6;
}
.tag-remove:hover { opacity: 1; }
.tag-input-wrapper { flex: 1; min-width: 100px; }
.tag-input {
  border: none; outline: none; font-size: 13px; color: #333;
  background: transparent; width: 100%;
}
.tag-input::placeholder { color: #ccc; }

.tag-presets {
  display: flex; flex-wrap: wrap; gap: 6px;
  margin-top: 12px; align-items: center;
}
.preset-label { font-size: 12px; color: #bbb; }
.preset-btn {
  padding: 3px 10px; border: 1px solid #e8e8e8; background: #fff;
  border-radius: 14px; font-size: 12px; color: #888;
  cursor: pointer; transition: all 0.15s;
}
.preset-btn:hover { border-color: #0096FA; color: #0096FA; }
.preset-btn.selected {
  border-color: #0096FA; background: #EBF5FF; color: #0096FA; font-weight: 500;
}

/* 价格 */
.price-row {
  display: flex; align-items: flex-end; gap: 12px;
}
.form-group.half { flex: 1; }
.price-sep {
  font-size: 18px; color: #ccc; margin-bottom: 20px;
}

.price-preview {
  margin-top: 16px;
  padding: 14px 16px;
  background: #f8f9fb;
  border-radius: 10px;
  border: 1px dashed #e0e0e0;
}
.preview-label {
  font-size: 11px;
  color: #bbb;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.preview-box {
  font-size: 18px;
  font-weight: 700;
  color: #00C48C;
}
.preview-empty {
  font-size: 14px;
  color: #ccc;
  font-weight: 400;
}

/* 联系选项卡片 */
.contact-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.contact-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 2px solid #f0f0f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.contact-option:hover {
  border-color: #d0d0d0;
}
.contact-option.selected {
  border-color: #0096FA;
  background: #f0f7ff;
}
.hidden-radio { display: none; }
.contact-icon { font-size: 20px; }
.contact-name { font-size: 13px; font-weight: 500; color: #555; }
.contact-option.selected .contact-name { color: #0096FA; font-weight: 600; }

/* 作品类型 */
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.check-item {
  padding: 5px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 13px;
  color: #888;
  cursor: pointer;
  transition: all 0.15s;
  background: #fff;
}
.check-item:hover {
  border-color: #0096FA;
  color: #0096FA;
}
.check-item.checked {
  border-color: #0096FA;
  background: #EBF5FF;
  color: #0096FA;
  font-weight: 500;
}

/* 保存 */
.save-area {
  display: flex; align-items: center; gap: 16px;
  margin-top: 8px; margin-bottom: 40px;
  justify-content: flex-end;
}
.save-btn {
  padding: 12px 36px; border: none; background: #0096FA; color: #fff;
  border-radius: 10px; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: background 0.2s;
}
.save-btn:hover { background: #0080d5; }
.save-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.save-hint { font-size: 12px; color: #bbb; }

/* 隐私设置 */
.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.privacy-item:last-child { border-bottom: none; }
.privacy-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.privacy-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.privacy-desc {
  font-size: 12px;
  color: #999;
}

@media (max-width: 900px) {
  .settings-grid { grid-template-columns: 1fr; }
  .card-header { flex-direction: column; gap: 12px; }
  .price-row { flex-direction: column; }
  .price-sep { display: none; }
  .contact-options { grid-template-columns: 1fr; }
}
</style>
