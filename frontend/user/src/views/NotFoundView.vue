<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-illustration">
        <svg viewBox="0 0 200 140" width="200" height="140" fill="none">
          <rect x="30" y="20" width="140" height="100" rx="8" stroke="#d0d0d0" stroke-width="2" />
          <circle cx="80" cy="60" r="12" stroke="#d0d0d0" stroke-width="2" />
          <path d="M30 90 l40-30 l30 20 l30-40 l40 50" stroke="#d0d0d0" stroke-width="2" stroke-linejoin="round" />
          <line x1="60" y1="55" x2="68" y2="63" stroke="#d0d0d0" stroke-width="2" stroke-linecap="round" />
          <line x1="68" y1="55" x2="60" y2="63" stroke="#d0d0d0" stroke-width="2" stroke-linecap="round" />
          <line x1="88" y1="55" x2="96" y2="63" stroke="#d0d0d0" stroke-width="2" stroke-linecap="round" />
          <line x1="96" y1="55" x2="88" y2="63" stroke="#d0d0d0" stroke-width="2" stroke-linecap="round" />
        </svg>
      </div>
      <h1 class="error-title">页面走丢了</h1>
      <p class="error-desc">你访问的页面不存在或已被移除</p>
      <div class="error-actions">
        <el-button type="primary" round @click="$router.push('/')">返回首页</el-button>
        <el-button round @click="$router.back()">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 56px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f4f5;
}

.not-found-content {
  text-align: center;
  padding: 60px 24px;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: #e0e0e0;
  line-height: 1;
  margin-bottom: 8px;
  font-family: Arial, sans-serif;
}

.error-illustration {
  margin-bottom: 24px;
  opacity: 0.6;
}

.error-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px;
}

.error-desc {
  font-size: 15px;
  color: #999;
  margin: 0 0 32px;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
