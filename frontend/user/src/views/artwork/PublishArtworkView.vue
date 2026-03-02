<template>
  <div class="publish-page">
    <div class="publish-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">{{ isEditMode ? '编辑作品' : '投稿作品' }}</h1>
        <p class="page-desc">{{ isEditMode ? '修改作品信息和图片' : '分享你的创作，让更多人看到（最多 10 张图片）' }}</p>
      </div>

      <div class="publish-body">
        <!-- 左侧：图片上传 -->
        <div class="upload-area">
          <!-- 已上传图片网格 -->
          <div class="image-grid" v-if="imageList.length > 0">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="image-grid-item"
              :class="{ 'is-cover': index === 0 }"
            >
              <img :src="img.thumbnailUrl || img.imageUrl" :alt="'图片 ' + (index + 1)" />
              <div class="grid-item-overlay">
                <span v-if="index === 0" class="cover-badge">封面</span>
                <div class="grid-item-actions">
                  <button v-if="index > 0" class="grid-action-btn" @click="moveImage(index, -1)" title="前移">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
                  </button>
                  <button v-if="index < imageList.length - 1" class="grid-action-btn" @click="moveImage(index, 1)" title="后移">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>
                  </button>
                  <button class="grid-action-btn delete-btn" @click="removeImage(index)" title="删除">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
                  </button>
                </div>
              </div>
            </div>

            <!-- 添加更多按钮 -->
            <div
              v-if="imageList.length < 10"
              class="image-grid-item add-more"
              @click="triggerUpload"
            >
              <svg viewBox="0 0 24 24" width="32" height="32" fill="#bbb"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
              <span class="add-more-text">添加图片</span>
            </div>
          </div>

          <!-- 空状态上传区域 -->
          <div
            v-else
            class="upload-zone"
            :class="{ 'dragging': isDragging }"
            @click="triggerUpload"
            @dragenter.prevent="isDragging = true"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
          >
            <div class="upload-placeholder">
              <svg viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="#bbb" stroke-width="1.5">
                <rect x="6" y="10" width="36" height="28" rx="4" />
                <circle cx="18" cy="22" r="4" />
                <path d="M6 34 l10-10 l8 8 l8-12 l10 14" stroke-linejoin="round"/>
              </svg>
              <p class="upload-main-text">点击或拖拽图片到这里上传</p>
              <p class="upload-sub-text">支持 JPG / PNG / GIF / WebP，单张最大 10MB，最多 10 张</p>
            </div>
          </div>

          <!-- 隐藏的文件输入 -->
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            style="display: none;"
            multiple
            @change="handleFileSelect"
          />

          <!-- 上传进度条 -->
          <div v-if="uploading" class="upload-progress">
            <div class="progress-text">正在上传 {{ uploadingCount }} 张图片...</div>
            <el-progress :percentage="uploadProgress" :stroke-width="4" />
          </div>
        </div>

        <!-- 右侧：表单 -->
        <div class="form-area">
          <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
            <!-- 标题 -->
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="formData.title"
                placeholder="给你的作品起个名字"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <!-- 描述 -->
            <el-form-item label="描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                placeholder="介绍一下你的作品（可选）"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>

            <!-- 标签 -->
            <el-form-item label="标签">
              <div class="tags-area">
                <div class="tag-input-row">
                  <el-input
                    v-model="tagInput"
                    placeholder="输入标签后按回车"
                    @keyup.enter="addTag"
                    clearable
                    size="default"
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
                    closable
                    @close="removeTag(index)"
                    class="tag-item"
                    effect="plain"
                    round
                  >
                    {{ tag }}
                  </el-tag>
                </div>

                <!-- 推荐标签 -->
                <div class="suggest-tags">
                  <span class="suggest-label">推荐：</span>
                  <span
                    v-for="tag in recommendedTags"
                    :key="tag"
                    class="suggest-chip"
                    :class="{ selected: formData.tags.includes(tag) }"
                    @click="addRecommendedTag(tag)"
                  >
                    {{ tag }}
                  </span>
                </div>

                <div class="tags-tip">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="#999">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                  </svg>
                  发布后系统会自动生成智能标签
                </div>
              </div>
            </el-form-item>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <el-button size="large" round @click="handleCancel">取消</el-button>
              <el-button
                v-if="!isEditMode"
                size="large"
                round
                @click="handleSaveDraft"
                :loading="savingDraft"
              >
                {{ savingDraft ? '保存中...' : '存为草稿' }}
              </el-button>
              <el-button
                type="primary"
                size="large"
                round
                @click="handleSubmit"
                :loading="submitting"
                :disabled="imageList.length === 0"
              >
                {{ submitting ? (isEditMode ? '保存中...' : '发布中...') : (isEditMode ? '保存修改' : '发布作品') }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
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

// 编辑模式：仅编辑已发布的作品 /artworks/:id/edit
const isEditMode = computed(() => !!route.params.id)
// 草稿模式：从草稿继续投稿 /publish?draftId=xxx（UI 与新建投稿一致）
const isDraftMode = computed(() => !!route.query.draftId)
// 是否需要加载已有数据（编辑 or 草稿）
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

// 多图列表 [{ imageUrl, thumbnailUrl }]
const imageList = ref([])

const formData = reactive({
  title: '',
  description: '',
  tags: []
})

const formRules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题最长 100 字', trigger: 'blur' }
  ]
}

const uploading = ref(false)
const uploadProgress = ref(0)
const uploadingCount = ref(0)
const tagInput = ref('')
const submitting = ref(false)
const savingDraft = ref(false)

const recommendedTags = [
  '动漫', '少女', '风景', '插画', '原创',
  '同人', '二次元', '游戏', '人物', '场景'
]

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
  const files = Array.from(event.dataTransfer.files || []).filter(f => f.type.startsWith('image/'))
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

  // 验证文件
  for (const file of toUpload) {
    if (!file.type.startsWith('image/')) {
      return ElMessage.error(`"${file.name}" 不是图片文件`)
    }
    if (file.size > 10 * 1024 * 1024) {
      return ElMessage.error(`"${file.name}" 超过 10MB 限制`)
    }
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
          thumbnailUrl: response.data.thumbnailUrl || response.data.imageUrl
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
  // 触发响应式更新
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
        thumbnailUrl: img.thumbnailUrl
      }))
    }

    // 草稿模式发布：必须传 isDraft: false，否则后端状态不会从 DRAFT 变 PUBLISHED
    if (isDraftMode.value) {
      payload.isDraft = false
    }

    let response
    if (isEditMode.value || isDraftMode.value) {
      // 编辑已发布作品 or 草稿继续发布，都用 update
      response = await updateArtwork(artworkId.value, payload)
    } else {
      response = await createArtwork(payload)
    }

    if (response.code === 200 || response.code === 201) {
      ElMessage.success(isEditMode.value ? '更新成功！' : '发布成功！')
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
    // 编辑模式或草稿模式下使用 updateArtwork，否则创建新草稿
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

// 编辑模式 / 草稿模式：加载已有作品数据
onMounted(async () => {
  if (isLoadMode.value && artworkId.value) {
    loadingArtwork.value = true
    try {
      const res = await getArtwork(artworkId.value)
      if (res.code === 200 && res.data) {
        const art = res.data
        formData.title = art.title || ''
        formData.description = art.description || ''
        // 加载标签
        if (art.tags && Array.isArray(art.tags)) {
          formData.tags = art.tags.map(t => typeof t === 'string' ? t : t.name).filter(Boolean)
        }
        // 加载图片
        if (art.images && Array.isArray(art.images) && art.images.length > 0) {
          imageList.value = art.images.map(img => ({
            imageUrl: img.imageUrl || img.url,
            thumbnailUrl: img.thumbnailUrl || img.imageUrl || img.url
          }))
        } else if (art.imageUrl) {
          imageList.value = [{
            imageUrl: art.imageUrl,
            thumbnailUrl: art.thumbnailUrl || art.imageUrl
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
  background: #f2f4f5;
  padding: 32px 24px;
}
.publish-container {
  max-width: 1000px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}
.page-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.publish-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

/* 上传区 */
.upload-area {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.upload-zone {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  margin: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}
.upload-zone:hover,
.upload-zone.dragging {
  border-color: #0096FA;
  background: #f0f8ff;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
}
.upload-main-text {
  font-size: 15px;
  color: #666;
  margin: 0;
}
.upload-sub-text {
  font-size: 13px;
  color: #bbb;
  margin: 0;
}

/* 多图网格 */
.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  padding: 12px;
}
.image-grid-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 1;
  background: #f0f0f0;
}
.image-grid-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.image-grid-item.is-cover {
  border: 2px solid #0096FA;
}
.grid-item-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  opacity: 0;
  transition: opacity 0.2s;
}
.image-grid-item:hover .grid-item-overlay {
  opacity: 1;
}
.cover-badge {
  align-self: flex-start;
  margin: 6px;
  padding: 2px 8px;
  background: #0096FA;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 4px;
}
.grid-item-actions {
  display: flex;
  justify-content: center;
  gap: 4px;
  padding: 6px;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
}
.grid-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
  color: #fff;
  cursor: pointer;
  transition: background 0.15s;
}
.grid-action-btn:hover {
  background: rgba(255,255,255,0.35);
}
.grid-action-btn.delete-btn:hover {
  background: rgba(255,60,60,0.6);
}

.add-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 2px dashed #ddd;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}
.add-more:hover {
  border-color: #0096FA;
  background: #f0f8ff;
}
.add-more-text {
  font-size: 12px;
  color: #999;
}

.upload-progress {
  padding: 12px 16px;
}
.progress-text {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

/* 表单区 */
.form-area {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.form-area :deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

/* 标签 */
.tags-area {
  width: 100%;
}
.tag-input-row {
  margin-bottom: 12px;
}
.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.tag-item {
  font-size: 13px;
}
.suggest-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.suggest-label {
  font-size: 12px;
  color: #999;
}
.suggest-chip {
  font-size: 12px;
  color: #666;
  padding: 3px 10px;
  background: #f5f5f5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.suggest-chip:hover {
  background: #e8f4ff;
  color: #0096FA;
}
.suggest-chip.selected {
  background: #e8f4ff;
  color: #0096FA;
}
.tags-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

/* 操作 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .publish-body {
    grid-template-columns: 1fr;
  }
  .upload-zone { min-height: 280px; }
  .image-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
