<template>
  <div class="skeleton-block" :style="blockStyle">
    <div class="skeleton-shimmer"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '16px' },
  radius: { type: String, default: '6px' },
  circle: { type: Boolean, default: false }
})

const blockStyle = computed(() => ({
  width: props.circle ? props.height : props.width,
  height: props.height,
  borderRadius: props.circle ? '50%' : props.radius
}))
</script>

<style scoped>
.skeleton-block {
  background: #e8e8e8;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}
.skeleton-shimmer {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.45) 50%, transparent 100%);
  animation: shimmer 1.4s ease-in-out infinite;
}
@keyframes shimmer {
  0%   { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
</style>
