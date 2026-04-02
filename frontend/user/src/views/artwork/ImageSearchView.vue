<template>
  <div class="image-search-page">
    <!-- 上传区域 -->
    <div class="upload-section">
      <div class="section-header">
        <div class="title-row">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="var(--px-blue, #0096FA)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
          </svg>
          <h1 class="page-title">以图搜图</h1>
          <span class="page-sub">上传一张图片，AI 将为你找到风格相似的作品</span>
        </div>
      </div>

      <div
        class="drop-zone"
        :class="{ dragging: isDragging, 'has-preview': previewUrl }"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleDrop"
        @click="triggerFileInput"
      >
        <!-- 预览图 -->
        <template v-if="previewUrl">
          <img :src="previewUrl" class="preview-img" alt="搜索图片" />
          <div class="preview-overlay">
            <span class="overlay-text">点击更换图片</span>
          </div>
        </template>
        <!-- 空白态 -->
        <template v-else>
          <div class="drop-content">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#bbb" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            <p class="drop-title">拖拽图片到这里，或点击选择</p>
            <p class="drop-hint">支持 JPG / PNG / WebP，最大 10MB</p>
          </div>
        </template>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          style="display:none"
          @change="handleFileChange"
        />
      </div>

      <div class="action-row" v-if="previewUrl">
        <button class="search-btn" :disabled="searching" @click="doSearch">
          <svg v-if="!searching" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
          <svg v-else class="spin-anim" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/>
          </svg>
          {{ searching ? '搜索中...' : '搜索相似作品' }}
        </button>
        <button class="clear-btn" @click="clearImage">清除图片</button>
      </div>
    </div>

    <!-- 结果区域 -->
    <div class="results-section" v-if="hasSearched">
      <div class="results-header">
        <h2 class="results-title">
          搜索结果
          <span class="results-count" v-if="results.length">找到 {{ results.length }} 个相似作品</span>
        </h2>
      </div>

      <!-- 空结果 -->
      <div v-if="results.length === 0 && !searching" class="empty-results">
        <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="#d0d0d0" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <p>未找到相似作品</p>
        <p class="empty-hint">尝试上传其他图片，或确保已有作品已提取特征</p>
      </div>

      <!-- 结果网格 -->
      <div v-else class="result-grid">
        <div
          v-for="item in results"
          :key="item.id"
          class="result-card"
          @click="goToDetail(item)"
        >
          <div class="result-thumb-wrap">
            <img :src="item.thumbnailUrl || item.imageUrl" :alt="item.title" class="result-thumb" loading="lazy" />
            <div class="similarity-badge">{{ item.hotnessScore?.toFixed(1) || '—' }}%</div>
          </div>
          <div class="result-info">
            <p class="result-name">{{ item.title }}</p>
            <p class="result-artist">{{ item.artistName }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { searchByImage } from '@/api/artwork'
import { ElMessage } from 'element-plus'

const router = useRouter()

const fileInputRef = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const isDragging = ref(false)
const searching = ref(false)
const hasSearched = ref(false)
const results = ref([])

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (e) => {
  const file = e.target.files?.[0]
  if (file) processFile(file)
}

const handleDrop = (e) => {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) processFile(file)
}

const processFile = (file) => {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG / PNG / WebP 格式')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

const clearImage = () => {
  selectedFile.value = null
  previewUrl.value = ''
  hasSearched.value = false
  results.value = []
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const doSearch = async () => {
  if (!selectedFile.value) return
  searching.value = true
  hasSearched.value = true
  results.value = []
  try {
    const res = await searchByImage(selectedFile.value, 20)
    if (res.code === 200 && res.data) {
      results.value = res.data
    } else {
      ElMessage.error(res.message || '搜索失败')
    }
  } catch (err) {
    console.error('以图搜图失败:', err)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    searching.value = false
  }
}

const goToDetail = (item) => {
  router.push({ name: 'ArtworkDetail', params: { id: item.id } })
}
</script>

<style scoped>
.image-search-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

/* 标题 */
.section-header { margin-bottom: 20px; }
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.page-title {
  font-size: 22px;
  font-weight: 900;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0;
}
.page-sub {
  font-size: 13px;
  color: var(--px-text-tertiary, #999);
}

/* 拖拽上传区 */
.drop-zone {
  position: relative;
  border: 2px dashed #d0d0d0;
  border-radius: 16px;
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s;
  overflow: hidden;
  background: #fafafa;
}
.drop-zone:hover { border-color: var(--px-blue, #0096FA); background: #f5f9ff; }
.drop-zone.dragging {
  border-color: var(--px-blue, #0096FA);
  background: rgba(0, 150, 250, 0.05);
  box-shadow: 0 0 0 4px rgba(0, 150, 250, 0.1);
}
.drop-zone.has-preview { border-style: solid; min-height: 320px; }

.drop-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  pointer-events: none;
}
.drop-title { font-size: 15px; font-weight: 600; color: #666; margin: 0; }
.drop-hint { font-size: 12px; color: #aaa; margin: 0; }

.preview-img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 12px;
}
.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.drop-zone.has-preview:hover .preview-overlay { opacity: 1; }
.overlay-text { color: #fff; font-size: 15px; font-weight: 600; }

/* 操作行 */
.action-row {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
}
.search-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  background: linear-gradient(135deg, #0096FA, #0078d4);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 3px 14px rgba(0, 150, 250, 0.35);
}
.search-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,150,250,0.45); }
.search-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.clear-btn {
  padding: 10px 20px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 999px;
  font-size: 13px;
  color: #888;
  cursor: pointer;
  transition: all 0.15s;
}
.clear-btn:hover { border-color: #ff4d4f; color: #ff4d4f; }

/* 结果 */
.results-section { margin-top: 40px; }
.results-header { margin-bottom: 16px; }
.results-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}
.results-count { font-size: 13px; color: #999; font-weight: 400; }

.empty-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 60px 20px;
  color: #aaa;
  font-size: 15px;
}
.empty-hint { font-size: 13px; color: #ccc; }

/* 结果网格 */
.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.result-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 6px rgba(0,0,0,0.08);
}
.result-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12); }

.result-thumb-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 3/4;
  overflow: hidden;
  background: #f0f0f0;
}
.result-thumb { width: 100%; height: 100%; object-fit: cover; }

.similarity-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0,0,0,0.65);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  backdrop-filter: blur(4px);
}

.result-info { padding: 10px 12px; }
.result-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.result-artist { font-size: 12px; color: #999; margin: 0; }

.spin-anim { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 600px) {
  .result-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .drop-zone { min-height: 200px; }
}
</style>
