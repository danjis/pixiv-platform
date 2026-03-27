<template>
  <div class="artwork-card" @click="$emit('click', artwork)">
    <!-- 图片容器 -->
    <div class="card-thumb">
      <img
        v-if="loaded || isInView"
        :src="artwork.thumbnailUrl || artwork.imageUrl"
        :alt="artwork.title"
        class="thumb-img"
        :class="{ 'is-loaded': loaded }"
        @load="loaded = true"
        @error="loadError = true"
      />
      <div v-if="!loaded && !loadError" class="thumb-placeholder">
        <div class="placeholder-shimmer"></div>
      </div>
      <div v-if="loadError" class="thumb-error">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#ccc" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <circle cx="8.5" cy="8.5" r="1.5" fill="#ccc" stroke="none" />
          <path d="M21 15l-5-5L5 21" />
        </svg>
      </div>

      <!-- 悬停遮罩 -->
      <div class="card-overlay">
        <!-- 多图标记 -->
        <div v-if="artwork.imageCount > 1" class="multi-badge">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
          </svg>
          <span>{{ artwork.imageCount }}</span>
        </div>

        <!-- 底部信息 -->
        <div class="overlay-bottom">
          <div class="overlay-stats">
            <span class="overlay-stat">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
              {{ formatCount(artwork.likeCount) }}
            </span>
          </div>
        </div>
      </div>

      <!-- R18 徽标 -->
      <div v-if="artwork.isR18" class="r18-badge">R-18</div>
    </div>

    <!-- 作品信息 -->
    <div class="card-info">
      <h3 class="card-title">{{ artwork.title }}</h3>
      <div class="card-author" @click.stop="$emit('author-click', artwork)">
        <el-avatar :size="22" :src="artwork.artistAvatar || undefined" class="author-avatar">
          {{ artwork.artistName?.charAt(0) }}
        </el-avatar>
        <span class="author-name">{{ artwork.artistName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  artwork: {
    type: Object,
    required: true
  }
})

defineEmits(['click', 'author-click'])

const loaded = ref(false)
const loadError = ref(false)
const isInView = ref(true) // 简化处理，默认可见

function formatCount(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}
</script>

<style scoped>
.artwork-card {
  cursor: pointer;
  border-radius: var(--px-radius-md, 8px);
  overflow: hidden;
  background: var(--px-bg-primary, #fff);
  break-inside: avoid;
  margin-bottom: 16px;
  transition: transform var(--px-transition-base, 0.25s ease),
              box-shadow var(--px-transition-base, 0.25s ease);
}

.artwork-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--px-shadow-card-hover, 0 6px 20px rgba(0,0,0,0.12));
}

/* 缩略图 */
.card-thumb {
  position: relative;
  width: 100%;
  min-height: 120px;
  overflow: hidden;
  background: var(--px-bg-tertiary, #f2f3f5);
  line-height: 0;
}

.thumb-img {
  display: block;
  width: 100%;
  height: auto;
  opacity: 0;
  transition: opacity 0.4s ease, transform 0.5s ease;
}

.thumb-img.is-loaded {
  opacity: 1;
}

.artwork-card:hover .thumb-img.is-loaded {
  transform: scale(1.04);
}

/* 骨架屏加载动画 */
.thumb-placeholder {
  position: absolute;
  inset: 0;
  min-height: 180px;
  background: var(--px-bg-tertiary, #f2f3f5);
}

.placeholder-shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(255,255,255,0.4) 50%,
    transparent 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 加载失败 */
.thumb-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--px-bg-tertiary, #f2f3f5);
}

/* 悬停遮罩 */
.card-overlay {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity var(--px-transition-fast, 0.15s ease);
  pointer-events: none;
}

.artwork-card:hover .card-overlay {
  opacity: 1;
}

/* 多图徽标 */
.multi-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: var(--px-radius-xs, 4px);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

/* 底部统计 */
.overlay-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  padding: 28px 10px 8px;
}

.overlay-stats {
  display: flex;
  gap: 10px;
}

.overlay-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

/* R18 */
.r18-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 6px;
  border-radius: 3px;
  background: var(--px-r18, #FF0000);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 作品信息 */
.card-info {
  padding: 10px 12px 12px;
}

.card-title {
  font-size: var(--px-font-sm, 13px);
  font-weight: 600;
  color: var(--px-text-primary, #1f1f1f);
  line-height: var(--px-lh-tight, 1.25);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
  transition: color var(--px-transition-fast, 0.15s ease);
}

.artwork-card:hover .card-title {
  color: var(--px-blue, #0096FA);
}

.card-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-avatar {
  flex-shrink: 0;
  font-size: 10px;
}

.author-name {
  font-size: var(--px-font-xs, 12px);
  color: var(--px-text-tertiary, #8c8c8c);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--px-transition-fast, 0.15s ease);
}

.card-author:hover .author-name {
  color: var(--px-blue, #0096FA);
}

/* 响应式 */
@media (max-width: 768px) {
  .card-info {
    padding: 8px 8px 10px;
  }
  .card-title {
    font-size: 12px;
  }
}
</style>
