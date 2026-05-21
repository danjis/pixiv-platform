<template>
  <div class="publish-page">
    <div class="page-glow glow-a"></div>
    <div class="page-glow glow-b"></div>
    <div class="page-grid"></div>

    <div class="publish-shell">
      <header class="hero">
        <div class="hero-copy">
          <span class="eyebrow">{{ isEditMode ? '编辑作品' : '创作者投稿' }}</span>
          <h1 class="title">{{ isEditMode ? '完善你的作品信息' : '发布一组新的作品' }}</h1>
          <p class="subtitle">
            {{ isEditMode ? '调整标题、描述、标签和图片顺序，让作品呈现更完整。' : '把图片、标题和标签整理好，让你的作品更容易被看见。' }}
          </p>
        </div>

        <div class="hero-metrics">
          <div class="metric-card">
            <span class="metric-value">{{ imageList.length }}</span>
            <span class="metric-label">已上传</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">10</span>
            <span class="metric-label">图片上限</span>
          </div>
          <div class="metric-card">
            <span class="metric-value">15</span>
            <span class="metric-label">标签上限</span>
          </div>
        </div>
      </header>

      <section class="workflow-strip">
        <div class="workflow-card">
          <div class="workflow-label">步骤 01</div>
          <div class="workflow-title">上传主图</div>
          <div class="workflow-text">第一张图会作为封面，建议选择最能代表作品气质的一张。</div>
        </div>
        <div class="workflow-card">
          <div class="workflow-label">步骤 02</div>
          <div class="workflow-title">补充信息</div>
          <div class="workflow-text">标题、描述和标签会影响搜索和推荐的效果。</div>
        </div>
        <div class="workflow-card accent">
          <div class="workflow-label">当前状态</div>
          <div class="workflow-title">{{ imageList.length ? '可以继续编辑' : '尚未上传图片' }}</div>
          <div class="workflow-text">{{ imageList.length ? '已经有素材了，接下来把作品内容补全即可。' : '先添加图片，页面会自动切换到编辑状态。' }}</div>
        </div>
      </section>

      <main class="editor-grid">
        <section class="panel upload-panel">
          <div class="panel-head">
            <div>
              <h2>作品图片</h2>
              <p>支持 JPG / PNG / GIF / WebP，单张最大 10MB</p>
            </div>
            <div class="panel-hint">最多 10 张</div>
          </div>

          <div v-if="imageList.length > 0" class="image-grid">
            <div class="cover-banner">
              <span class="cover-banner-tag">封面</span>
              <span>第一张会作为列表封面，可通过前移后移调整顺序。</span>
            </div>

            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="image-card"
              :class="{ cover: index === 0 }"
            >
              <img :src="img.thumbnailUrl || img.imageUrl" :alt="'图片 ' + (index + 1)" />
              <div class="image-mask">
                <span v-if="index === 0" class="cover-tag">封面</span>
                <div class="image-actions">
                  <button v-if="index > 0" type="button" class="image-btn" @click="moveImage(index, -1)" title="前移">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="15 18 9 12 15 6" />
                    </svg>
                  </button>
                  <button v-if="index < imageList.length - 1" type="button" class="image-btn" @click="moveImage(index, 1)" title="后移">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="9 18 15 12 9 6" />
                    </svg>
                  </button>
                  <button type="button" class="image-btn danger" @click="removeImage(index)" title="删除">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <button v-if="imageList.length < 10" type="button" class="image-card add-card" @click="triggerUpload">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              <span>继续添加</span>
            </button>
          </div>

          <div
            v-else
            class="upload-zone"
            :class="{ dragging: isDragging }"
            @click="triggerUpload"
            @dragenter.prevent="isDragging = true"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
          >
            <div class="upload-icon">
              <svg viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="6" y="10" width="36" height="28" rx="4" />
                <circle cx="18" cy="22" r="4" />
                <path d="M6 34 l10-10 l8 8 l8-12 l10 14" stroke-linejoin="round" />
              </svg>
            </div>
            <p class="upload-title">点击或拖拽图片到这里上传</p>
            <p class="upload-desc">建议先上传封面图，再补充其他图片和标签。</p>
          </div>

          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            multiple
            style="display: none"
            @change="handleFileSelect"
          />

          <div v-if="uploading" class="upload-progress">
            <div class="progress-text">正在上传 {{ uploadingCount }} 张图片...</div>
            <el-progress :percentage="uploadProgress" :stroke-width="4" />
          </div>
        </section>

        <section class="panel form-panel">
          <div class="panel-head">
            <div>
              <h2>作品信息</h2>
              <p>把内容整理清楚，搜索和浏览体验会更完整</p>
            </div>
          </div>

          <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="formData.title" placeholder="给你的作品起个名字" maxlength="100" show-word-limit />
            </el-form-item>

            <el-form-item label="描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                placeholder="介绍一下你的作品（可选）"
                :rows="5"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="标签">
              <div class="tags-area">
                <div class="tag-input-row">
                  <el-input
                    v-model="tagInput"
                    placeholder="输入标签后按回车"
                    clearable
                    @keyup.enter="addTag"
                  >
                    <template #append>
                      <el-button @click="addTag">添加</el-button>
                    </template>
                  </el-input>
                </div>

                <div v-if="formData.tags.length > 0" class="tags-display">
                  <el-tag
                    v-for="(tag, index) in formData.tags"
                    :key="index"
                    class="tag-item"
                    effect="plain"
                    round
                    closable
                    @close="removeTag(index)"
                  >
                    {{ tag }}
                  </el-tag>
                </div>

                <div class="suggest-tags">
                  <span class="suggest-label">推荐</span>
                  <button
                    v-for="tag in recommendedTags"
                    :key="tag"
                    type="button"
                    class="suggest-chip"
                    :class="{ selected: formData.tags.includes(tag) }"
                    @click="addRecommendedTag(tag)"
                  >
                    {{ tag }}
                  </button>
                </div>

                <div class="tags-tip">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="16" x2="12" y2="12" />
                    <line x1="12" y1="8" x2="12.01" y2="8" />
                  </svg>
                  发布后系统会自动生成智能标签
                </div>
              </div>
            </el-form-item>

            <div class="form-actions">
              <el-button size="large" round @click="handleCancel">取消</el-button>
              <el-button v-if="!isEditMode" size="large" round :loading="savingDraft" @click="handleSaveDraft">
                {{ savingDraft ? '保存中...' : '存为草稿' }}
              </el-button>
              <el-button
                type="primary"
                size="large"
                round
                :loading="submitting"
                :disabled="imageList.length === 0"
                @click="handleSubmit"
              >
                {{ submitting ? (isEditMode ? '保存中...' : '发布中...') : (isEditMode ? '保存修改' : '发布作品') }}
              </el-button>
            </div>
          </el-form>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createArtwork, getArtwork, updateArtwork } from '@/api/artwork'
import { uploadImage } from '@/api/file'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isEditMode = computed(() => !!route.params.id)
const isDraftMode = computed(() => !!route.query.draftId)
const isLoadMode = computed(() => isEditMode.value || isDraftMode.value)
const artworkId = computed(() => {
  if (route.params.id) return Number(route.params.id)
  if (route.query.draftId) return Number(route.query.draftId)
  return null
})
const loadingArtwork = ref(false)

const formRef = ref(null)
const fileInput = ref(null)
const isDragging = ref(false)
const imageList = ref([])

const formData = reactive({
  title: '',
  description: '',
  tags: []
})

const formRules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题最长 100 字符', trigger: 'blur' }
  ]
}

const uploading = ref(false)
const uploadProgress = ref(0)
const uploadingCount = ref(0)
const tagInput = ref('')
const submitting = ref(false)
const savingDraft = ref(false)

const recommendedTags = ['动漫', '少女', '风景', '插画', '原创', '同人', '二次元', '游戏', '人物', '场景']

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileSelect(event) {
  const files = Array.from(event.target.files || [])
  if (files.length > 0) uploadFiles(files)
  event.target.value = ''
}

function handleDrop(event) {
  isDragging.value = false
  const files = Array.from(event.dataTransfer.files || []).filter(file => file.type.startsWith('image/'))
  if (files.length > 0) uploadFiles(files)
}

async function uploadFiles(files) {
  const remaining = 10 - imageList.value.length
  if (remaining <= 0) {
    return ElMessage.warning('最多上传 10 张图片')
  }

  const toUpload = files.slice(0, remaining)
  if (files.length > remaining) {
    ElMessage.warning(`已选择 ${files.length} 张，仅上传前 ${remaining} 张`)
  }

  for (const file of toUpload) {
    if (!file.type.startsWith('image/')) return ElMessage.error(`"${file.name}" 不是图片文件`)
    if (file.size > 10 * 1024 * 1024) return ElMessage.error(`"${file.name}" 超过 10MB 限制`)
  }

  uploading.value = true
  uploadingCount.value = toUpload.length
  uploadProgress.value = 0

  let uploaded = 0
  try {
    for (const file of toUpload) {
      const response = await uploadImage(file)
      if (response.code === 200 && response.data) {
        imageList.value.push({
          imageUrl: response.data.imageUrl,
          thumbnailUrl: response.data.thumbnailUrl || response.data.imageUrl,
          originalImageUrl: response.data.originalImageUrl || ''
        })
      } else {
        ElMessage.error(`"${file.name}" 上传失败: ${response.message || '未知错误'}`)
      }
      uploaded++
      uploadProgress.value = Math.round((uploaded / toUpload.length) * 100)
    }
    if (uploaded > 0) ElMessage.success(`成功上传 ${uploaded} 张图片`)
  } catch {
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    uploadingCount.value = 0
  }
}

function removeImage(index) {
  imageList.value.splice(index, 1)
}

function moveImage(index, direction) {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= imageList.value.length) return
  const temp = imageList.value[index]
  imageList.value[index] = imageList.value[newIndex]
  imageList.value[newIndex] = temp
  imageList.value = [...imageList.value]
}

function addTag() {
  const tag = tagInput.value.trim()
  if (!tag) return
  if (formData.tags.length >= 15) return ElMessage.warning('最多 15 个标签')
  if (formData.tags.includes(tag)) {
    tagInput.value = ''
    return
  }
  formData.tags.push(tag)
  tagInput.value = ''
}

function removeTag(index) {
  formData.tags.splice(index, 1)
}

function addRecommendedTag(tag) {
  if (formData.tags.includes(tag)) return
  if (formData.tags.length >= 15) return ElMessage.warning('最多 15 个标签')
  formData.tags.push(tag)
}

async function handleCancel() {
  if (formData.title || formData.description || imageList.value.length > 0) {
    try {
      await ElMessageBox.confirm('确定要取消吗？已填写的内容不会保存。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      })
      router.back()
    } catch {}
  } else {
    router.back()
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (imageList.value.length === 0) return ElMessage.error('请先上传图片')

    submitting.value = true
    const payload = {
      title: formData.title,
      description: formData.description || '',
      tags: formData.tags,
      images: imageList.value.map(img => ({
        imageUrl: img.imageUrl,
        thumbnailUrl: img.thumbnailUrl,
        originalImageUrl: img.originalImageUrl || ''
      }))
    }

    if (isDraftMode.value) payload.isDraft = false

    const response = (isEditMode.value || isDraftMode.value)
      ? await updateArtwork(artworkId.value, payload)
      : await createArtwork(payload)

    if (response.code === 200 || response.code === 201) {
      ElMessage.success(isEditMode.value ? '更新成功' : '发布成功')
      const targetId = response.data?.id || artworkId.value
      if (targetId) {
        router.push({ name: 'ArtworkDetail', params: { id: targetId } })
      } else {
        router.push({ name: 'Artworks' })
      }
    } else {
      ElMessage.error(response.message || (isEditMode.value ? '更新失败' : '发布失败'))
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEditMode.value ? '更新失败，请重试' : '发布失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

async function handleSaveDraft() {
  const title = formData.title.trim() || '未命名草稿'
  savingDraft.value = true
  try {
    const payload = {
      title,
      description: formData.description || '',
      tags: formData.tags,
      isDraft: true
    }
    if (imageList.value.length > 0) {
      payload.images = imageList.value.map(img => ({
        imageUrl: img.imageUrl,
        thumbnailUrl: img.thumbnailUrl
      }))
    }
    const response = (isEditMode.value || isDraftMode.value) && artworkId.value
      ? await updateArtwork(artworkId.value, payload)
      : await createArtwork(payload)
    if (response.code === 200 || response.code === 201) {
      ElMessage.success('草稿已保存')
      router.push({ name: 'StudioArtworks' })
    } else {
      ElMessage.error(response.message || '保存草稿失败')
    }
  } catch {
    ElMessage.error('保存草稿失败，请重试')
  } finally {
    savingDraft.value = false
  }
}

onMounted(async () => {
  if (isLoadMode.value && artworkId.value) {
    loadingArtwork.value = true
    try {
      const res = await getArtwork(artworkId.value)
      if (res.code === 200 && res.data) {
        const art = res.data
        formData.title = art.title || ''
        formData.description = art.description || ''
        if (art.tags && Array.isArray(art.tags)) {
          formData.tags = art.tags.map(tag => (typeof tag === 'string' ? tag : tag.name)).filter(Boolean)
        }
        if (art.images && Array.isArray(art.images) && art.images.length > 0) {
          imageList.value = art.images.map(img => ({
            imageUrl: img.imageUrl || img.url,
            thumbnailUrl: img.thumbnailUrl || img.imageUrl || img.url,
            originalImageUrl: img.originalImageUrl || ''
          }))
        } else if (art.imageUrl) {
          imageList.value = [{
            imageUrl: art.imageUrl,
            thumbnailUrl: art.thumbnailUrl || art.imageUrl,
            originalImageUrl: art.originalImageUrl || ''
          }]
        }
      } else {
        ElMessage.error('加载作品数据失败')
        router.back()
      }
    } catch {
      ElMessage.error('加载作品数据失败')
      router.back()
    } finally {
      loadingArtwork.value = false
    }
  }
})
</script>

<style scoped>
.publish-page {
  min-height: calc(100vh - 56px);
  position: relative;
  overflow: hidden;
  padding: 32px 24px 44px;
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.12), transparent 26%),
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.10), transparent 24%),
    linear-gradient(180deg, #fcfdff 0%, #f4f7fb 100%);
}
.page-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.18), transparent 72%);
  pointer-events: none;
}
.page-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(34px);
  pointer-events: none;
  opacity: 0.7;
}
.glow-a { width: 240px; height: 240px; left: -60px; top: 80px; background: rgba(59, 130, 246, 0.14); }
.glow-b { width: 240px; height: 240px; right: -70px; top: 60px; background: rgba(236, 72, 153, 0.12); }
.publish-shell {
  position: relative;
  z-index: 1;
  max-width: 1180px;
  margin: 0 auto;
}
.hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
  margin-bottom: 20px;
}
.hero-copy { max-width: 760px; }
.eyebrow {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(226, 232, 240, 0.9);
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.title {
  margin: 12px 0 8px;
  font-size: 34px;
  line-height: 1.15;
  color: #0f172a;
  letter-spacing: -0.04em;
}
.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}
.hero-metrics {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.metric-card {
  min-width: 102px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}
.metric-value {
  display: block;
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
}
.metric-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}
.workflow-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}
.workflow-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}
.workflow-card.accent {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.12), rgba(96, 165, 250, 0.08)), rgba(255, 255, 255, 0.84);
}
.workflow-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 8px;
}
.workflow-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}
.workflow-text {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.editor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.06fr) minmax(0, 0.94fr);
  gap: 24px;
  align-items: start;
}
.panel {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 26px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(12px);
}
.upload-panel { overflow: hidden; }
.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  padding: 20px 20px 0;
}
.panel-head h2 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #0f172a;
}
.panel-head p {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}
.panel-hint {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.upload-zone {
  min-height: 420px;
  margin: 18px;
  border-radius: 22px;
  border: 1.5px dashed #d8dee9;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(241, 245, 249, 0.92));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.24s ease;
}
.upload-zone:hover,
.upload-zone.dragging {
  border-color: #4f8df7;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.98), rgba(236, 245, 255, 0.96));
  transform: translateY(-1px);
}
.upload-icon {
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.16);
  color: #94a3b8;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.06);
}
.upload-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
}
.upload-desc {
  margin: 0;
  max-width: 340px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.7;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 14px;
}
.cover-banner {
  grid-column: 1 / -1;
  padding: 14px 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08), rgba(96, 165, 250, 0.06));
  border: 1px solid rgba(191, 219, 254, 0.8);
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}
.cover-banner-tag {
  display: inline-flex;
  margin-right: 8px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}
.image-card {
  position: relative;
  aspect-ratio: 1;
  border-radius: 18px;
  overflow: hidden;
  background: #eef2f7;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  border: 2px solid transparent;
}
.image-card.cover { border-color: #4f8df7; }
.image-card img { width: 100%; height: 100%; object-fit: cover; display: block; }
.image-mask {
  position: absolute;
  inset: 0;
  opacity: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: opacity 0.2s;
}
.image-card:hover .image-mask { opacity: 1; }
.cover-tag {
  align-self: flex-start;
  margin: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}
.image-actions {
  display: flex;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.58));
}
.image-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(4px);
  color: #fff;
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease;
}
.image-btn:hover { transform: translateY(-1px); background: rgba(255, 255, 255, 0.32); }
.image-btn.danger:hover { background: rgba(255, 60, 60, 0.6); }
.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1.5px dashed #d8dee9;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(241, 245, 249, 0.95));
  color: #94a3b8;
}
.add-card:hover {
  border-color: #4f8df7;
  color: #4f8df7;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.98), rgba(236, 245, 255, 0.96));
}
.upload-progress { padding: 0 18px 18px; }
.progress-text { margin-bottom: 6px; color: #475569; font-size: 13px; }
.form-panel { padding: 24px; }
.form-panel :deep(.el-form-item__label) {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}
.form-panel :deep(.el-input__wrapper),
.form-panel :deep(.el-textarea__inner) {
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid #dbe3ef;
  box-shadow: none;
}
.form-panel :deep(.el-input__wrapper.is-focus),
.form-panel :deep(.el-textarea__inner:focus) {
  border-color: #4f8df7;
  box-shadow: 0 0 0 4px rgba(79, 141, 247, 0.12);
}
.tags-area { width: 100%; }
.tag-input-row { margin-bottom: 12px; }
.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.tag-item { font-size: 13px; border-radius: 999px; }
.suggest-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.suggest-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}
.suggest-chip {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #475569;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}
.suggest-chip:hover {
  color: #2563eb;
  border-color: #bfdbfe;
  background: #eff6ff;
  transform: translateY(-1px);
}
.suggest-chip.selected {
  color: #2563eb;
  border-color: #93c5fd;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
}
.tags-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
  font-size: 12px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid rgba(226, 232, 240, 0.92);
}
.form-actions :deep(.el-button) { min-width: 98px; }
@media (max-width: 1100px) {
  .hero,
  .editor-grid,
  .workflow-strip {
    grid-template-columns: 1fr;
  }
  .hero {
    display: grid;
  }
  .hero-metrics { justify-content: flex-start; }
}
@media (max-width: 768px) {
  .publish-page { padding: 20px 14px 32px; }
  .title { font-size: 26px; }
  .upload-zone { min-height: 280px; margin: 14px; }
  .image-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .form-panel { padding: 20px 18px 18px; }
  .form-actions { flex-direction: column-reverse; }
  .form-actions :deep(.el-button) { width: 100%; }
}
</style>
