<template>
  <div class="artwork-card" @click="$emit('click', artwork)">
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
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.2">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor" stroke="none" />
          <path d="M21 15l-5-5L5 21" />
        </svg>
      </div>

      <!-- Hover overlay -->
      <div class="card-overlay">
        <div class="overlay-top">
          <div v-if="artwork.imageCount > 1" class="multi-badge">
            <svg viewBox="0 0 24 24" width="11" height="11" fill="currentColor">
              <path d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
            </svg>
            {{ artwork.imageCount }}
          </div>
        </div>
        <div class="overlay-bottom">
          <span class="like-stat">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            {{ formatCount(artwork.likeCount) }}
          </span>
        </div>
      </div>

      <div v-if="artwork.isR18" class="r18-badge">R-18</div>
    </div>

    <div class="card-info">
      <h3 class="card-title">{{ artwork.title }}</h3>
      <div class="card-author" @click.stop="$emit('author-click', artwork)">
        <el-avatar :size="20" :src="artwork.artistAvatar || undefined" class="author-avatar">
          {{ artwork.artistName && artwork.artistName.charAt(0) }}
        </el-avatar>
        <span class="author-name">{{ artwork.artistName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  artwork: { type: Object, required: true }
})
defineEmits(['click', 'author-click'])

const loaded = ref(false)
const loadError = ref(false)
const isInView = ref(true)

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
  border-radius: var(--px-radius-md);
  overflow: hidden;
  background: var(--ink-700);
  break-inside: avoid;
  margin-bottom: 14px;
  border: 1px solid transparent;
  transition:
    transform var(--px-transition-base),
    box-shadow var(--px-transition-base),
    border-color var(--px-transition-base);
}

.artwork-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--px-shadow-card-hover);
  border-color: var(--ink-400);
}

/* Thumb */
.card-thumb {
  position: relative;
  width: 100%;
  min-height: 100px;
  overflow: hidden;
  background: var(--ink-600);
  line-height: 0;
}

.thumb-img {
  display: block;
  width: 100%;
  height: auto;
  opacity: 0;
  transition: opacity 0.35s ease, transform 0.45s ease;
}

.thumb-img.is-loaded { opacity: 1; }

.artwork-card:hover .thumb-img.is-loaded {
  transform: scale(1.05);
}

.thumb-placeholder {
  position: absolute;
  inset: 0;
  min-height: 160px;
  background: var(--ink-600);
}

.placeholder-shimmer {
  width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.04) 50%, transparent 100%);
  background-size: 200% 100%;
  animation: shimmer 1.8s infinite;
}

.thumb-error {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  background: var(--ink-600); color: var(--ink-300);
}

/* Overlay */
.card-overlay {
  position: absolute; inset: 0;
  opacity: 0;
  transition: opacity var(--px-transition-fast);
  display: flex; flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(
    to bottom,
    rgba(0,0,0,0.1) 0%,
    transparent 30%,
    transparent 60%,
    rgba(0,0,0,0.65) 100%
  );
  padding: 8px;
  pointer-events: none;
}

.artwork-card:hover .card-overlay { opacity: 1; }

.overlay-top { display: flex; justify-content: flex-end; }

.multi-badge {
  display: flex; align-items: center; gap: 3px;
  padding: 3px 7px; border-radius: var(--px-radius-xs);
  background: rgba(0,0,0,0.7); backdrop-filter: blur(4px);
  color: #fff; font-size: 11px; font-weight: 600;
  font-family: var(--px-font-mono);
}

.overlay-bottom {
  display: flex; align-items: center;
  padding: 4px 4px 2px;
}

.like-stat {
  display: flex; align-items: center; gap: 4px;
  color: #fff; font-size: 12px; font-weight: 500;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

.r18-badge {
  position: absolute; top: 7px; left: 7px;
  padding: 2px 6px; border-radius: var(--px-radius-xs);
  background: var(--px-r18); color: #fff;
  font-size: 10px; font-weight: 700; letter-spacing: 0.5px;
  font-family: var(--px-font-mono);
}

/* Info */
.card-info { padding: 10px 11px 12px; }

.card-title {
  font-size: 12px; font-weight: 500;
  color: var(--px-text-secondary);
  line-height: 1.3;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 7px;
  transition: color var(--px-transition-fast);
}

.artwork-card:hover .card-title { color: var(--px-text-primary); }

.card-author {
  display: flex; align-items: center; gap: 6px;
  cursor: pointer;
}

.author-avatar { flex-shrink: 0; font-size: 9px; }

.author-name {
  font-size: 11px; color: var(--px-text-tertiary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  transition: color var(--px-transition-fast);
}

.card-author:hover .author-name { color: var(--coral); }

@media (max-width: 768px) {
  .card-info { padding: 8px 9px 10px; }
  .card-title { font-size: 11px; }
}
</style>
