<template>
  <div class="image-search-page">
    <div class="bg-glow glow-a"></div>
    <div class="bg-glow glow-b"></div>
    <div class="bg-grid"></div>

    <div class="search-shell">
      <header class="hero">
        <div class="hero-copy">
          <span class="eyebrow">以图搜图</span>
          <h1 class="title">上传一张图，找到风格接近的作品</h1>
          <p class="subtitle">适合找同风格参考、追溯画师作品，或者继续发现相似题材的内容。</p>
        </div>

        <div class="hero-stats">
          <div class="stat-card">
            <span class="stat-value">1</span>
            <span class="stat-label">张图片输入</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">20</span>
            <span class="stat-label">默认返回数量</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">10MB</span>
            <span class="stat-label">单图上限</span>
          </div>
        </div>
      </header>

      <section class="layout">
        <div class="panel upload-panel">
          <div class="panel-head">
            <div>
              <h2>上传图片</h2>
              <p>点击选择，或直接拖拽图片到区域中</p>
            </div>
            <div class="panel-pill">JPG / PNG / WebP</div>
          </div>

          <div
            class="drop-zone"
            :class="{ dragging: isDragging, 'has-preview': previewUrl }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleDrop"
            @click="triggerFileInput"
          >
            <template v-if="previewUrl">
              <img :src="previewUrl" class="preview-img" alt="搜索图片" />
              <div class="preview-overlay">
                <span class="overlay-text">点击可重新选择图片</span>
              </div>
            </template>
            <template v-else>
              <div class="drop-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="44" height="44" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="15" />
                  </svg>
                </div>
                <p class="drop-title">拖拽图片到这里，或者点击选择</p>
                <p class="drop-hint">建议使用清晰主体图，便于提取相似作品特征</p>
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
            <button class="search-btn" :disabled="searching" @click="doSearch" type="button">
              <svg v-if="!searching" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
              </svg>
              <svg v-else class="spin-anim" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/>
              </svg>
              {{ searching ? '搜索中...' : '开始搜图' }}
            </button>
            <button class="clear-btn" @click="clearImage" type="button">清除图片</button>
          </div>
        </div>

        <div class="panel tips-panel">
          <div class="panel-head">
            <div>
              <h2>使用建议</h2>
              <p>让搜图结果更稳定的小技巧</p>
            </div>
          </div>
          <div class="tip-list">
            <div class="tip-item">
              <div class="tip-index">1</div>
              <div>
                <div class="tip-title">选主体清晰的图片</div>
                <div class="tip-text">人物、构图、主色调越明确，结果越容易命中。</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-index">2</div>
              <div>
                <div class="tip-title">优先使用无压缩原图</div>
                <div class="tip-text">清晰度更高时，特征提取通常更准确。</div>
              </div>
            </div>
            <div class="tip-item">
              <div class="tip-index">3</div>
              <div>
                <div class="tip-title">结果可直接跳转详情页</div>
                <div class="tip-text">点开作品即可继续浏览作者和更多作品。</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="results-section" v-if="hasSearched">
        <div class="results-header">
          <div>
            <h2 class="results-title">搜索结果</h2>
            <p class="results-subtitle" v-if="results.length">找到 {{ results.length }} 个相似作品</p>
            <p class="results-subtitle" v-else>没有找到匹配结果，可以尝试更清晰的图片</p>
          </div>
        </div>

        <div v-if="results.length === 0 && !searching" class="empty-results">
          <div class="empty-card">
            <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="#d0d0d0" stroke-width="1.5">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <div class="empty-title">没有找到相似作品</div>
            <div class="empty-hint">可以换一张主体更明显的图片再试试。</div>
          </div>
        </div>

        <div v-else class="result-grid">
          <div
            v-for="item in results"
            :key="item.id"
            class="result-card"
            @click="goToDetail(item)"
          >
            <div class="result-thumb-wrap">
              <img :src="item.thumbnailUrl || item.imageUrl" :alt="item.title" class="result-thumb" loading="lazy" />
              <div class="similarity-badge">{{ item.hotnessScore?.toFixed(1) || '0.0' }}%</div>
            </div>
            <div class="result-info">
              <p class="result-name">{{ item.title }}</p>
              <p class="result-artist">{{ item.artistName }}</p>
            </div>
          </div>
        </div>
      </section>
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
  min-height: calc(100vh - 56px);
  position: relative;
  overflow: hidden;
  padding: 32px 20px 64px;
  background:
    radial-gradient(circle at top left, rgba(56, 189, 248, 0.10), transparent 28%),
    radial-gradient(circle at top right, rgba(244, 114, 182, 0.10), transparent 24%),
    linear-gradient(180deg, #fcfdff 0%, #f4f7fb 100%);
}
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.15), transparent 72%);
  pointer-events: none;
}
.bg-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(36px);
  pointer-events: none;
  opacity: 0.7;
}
.glow-a { width: 220px; height: 220px; left: -50px; top: 80px; background: rgba(59, 130, 246, 0.14); }
.glow-b { width: 220px; height: 220px; right: -60px; top: 60px; background: rgba(236, 72, 153, 0.12); }
.search-shell {
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
  margin-bottom: 24px;
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
.hero-stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.stat-card {
  min-width: 110px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}
.stat-value {
  display: block;
  color: #0f172a;
  font-size: 18px;
  font-weight: 800;
}
.stat-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}
.layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(280px, 0.9fr);
  gap: 24px;
  align-items: start;
}
.panel {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 26px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(12px);
}
.upload-panel { padding: 22px; }
.tips-panel { padding: 22px; }
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}
.panel-head h2 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #0f172a;
}
.panel-head p {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}
.panel-pill {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.drop-zone {
  position: relative;
  border: 1.5px dashed #d8dee9;
  border-radius: 22px;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(241, 245, 249, 0.92));
}
.drop-zone:hover,
.drop-zone.dragging {
  border-color: #4f8df7;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.98), rgba(236, 245, 255, 0.96));
  transform: translateY(-1px);
}
.drop-zone.has-preview {
  min-height: 420px;
  border-style: solid;
}
.drop-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  pointer-events: none;
  text-align: center;
}
.upload-icon {
  display: grid;
  place-items: center;
  width: 82px;
  height: 82px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.16);
  color: #94a3b8;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.06);
}
.drop-title {
  font-size: 16px;
  font-weight: 700;
  color: #334155;
  margin: 0;
}
.drop-hint {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
  max-width: 320px;
  line-height: 1.7;
}
.preview-img {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}
.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.34);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.drop-zone.has-preview:hover .preview-overlay { opacity: 1; }
.overlay-text {
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}
.action-row {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  justify-content: center;
}
.search-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 30px;
  background: linear-gradient(135deg, #0096FA, #2563eb);
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.28);
}
.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(37, 99, 235, 0.34);
}
.search-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.clear-btn {
  padding: 11px 20px;
  background: #fff;
  border: 1px solid #dbe3ef;
  border-radius: 999px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
}
.clear-btn:hover { border-color: #ff4d4f; color: #ff4d4f; }
.tip-list {
  display: grid;
  gap: 12px;
}
.tip-item {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fafc, #f4f7fb);
  border: 1px solid rgba(226, 232, 240, 0.9);
}
.tip-index {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #2563eb;
  background: #eff6ff;
}
.tip-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}
.tip-text {
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
}
.results-section {
  margin-top: 34px;
}
.results-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 16px;
}
.results-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}
.results-subtitle {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}
.empty-results {
  padding: 12px 0 0;
}
.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 56px 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(226, 232, 240, 0.9);
}
.empty-title {
  font-size: 16px;
  font-weight: 700;
  color: #475569;
}
.empty-hint {
  font-size: 13px;
  color: #94a3b8;
}
.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}
.result-card {
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  transition: all 0.22s;
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}
.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.12);
}
.result-thumb-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background: #f0f0f0;
}
.result-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}
.result-card:hover .result-thumb { transform: scale(1.03); }
.similarity-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(15, 23, 42, 0.7);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 9px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}
.result-info { padding: 12px 14px; }
.result-name {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.result-artist {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}
.spin-anim { animation: spin 0.8s linear infinite; }
@keyframes spin {
  to { transform: rotate(360deg); }
}
@media (max-width: 960px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .image-search-page { padding: 20px 14px 40px; }
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .hero-stats { justify-content: flex-start; }
  .title { font-size: 26px; }
}
@media (max-width: 600px) {
  .result-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .drop-zone { min-height: 240px; }
  .action-row {
    flex-direction: column;
  }
}
</style>
