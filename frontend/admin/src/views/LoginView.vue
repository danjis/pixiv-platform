<template>
  <div class="login-root">
    <!-- Left: Brand Panel -->
    <div class="login-left">
      <div class="left-noise" />
      <div class="left-grid" />
      <div class="left-content">
        <div class="brand-lockup">
          <div class="brand-logo">
            <svg
              viewBox="0 0 44 44"
              fill="none"
            >
              <rect
                width="44"
                height="44"
                rx="12"
                fill="rgba(255,255,255,0.12)"
              />
              <rect
                width="44"
                height="44"
                rx="12"
                stroke="rgba(255,255,255,0.18)"
                stroke-width="1"
              />
              <path
                d="M13 22L20 15L27 22L20 29Z"
                fill="white"
                opacity="0.95"
              />
              <path
                d="M20 15L27 22L34 15L27 8Z"
                fill="white"
                opacity="0.55"
              />
            </svg>
          </div>
          <h1 class="brand-name">
            Pixiv Admin
          </h1>
        </div>
        <div class="brand-copy">
          <p class="brand-tagline">
            创作平台管理控制台
          </p>
          <p class="brand-sub">
            统一管理用户、内容、交易与系统配置
          </p>
        </div>
        <div class="brand-stats">
          <div class="bstat">
            <span class="bstat-num">14</span>
            <span class="bstat-lbl">功能模块</span>
          </div>
          <div class="bstat-divider" />
          <div class="bstat">
            <span class="bstat-num">∞</span>
            <span class="bstat-lbl">数据洞察</span>
          </div>
          <div class="bstat-divider" />
          <div class="bstat">
            <span class="bstat-num">24/7</span>
            <span class="bstat-lbl">实时监控</span>
          </div>
        </div>
      </div>
      <p class="left-footer">
        Pixiv Platform &copy; {{ year }}
      </p>
    </div>

    <!-- Right: Login Form -->
    <div class="login-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">
            欢迎回来
          </h2>
          <p class="form-desc">
            请使用管理员账号登录
          </p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="field-group">
            <label class="field-label">账号</label>
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="输入管理员账号"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
          </div>

          <div class="field-group">
            <label class="field-label">密码</label>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="输入登录密码"
                size="large"
                show-password
                :prefix-icon="Lock"
              />
            </el-form-item>
          </div>

          <el-form-item style="margin-top: 8px">
            <button
              type="button"
              class="login-btn"
              :class="{ loading: loading }"
              :disabled="loading"
              @click="handleLogin"
            >
              <span
                v-if="!loading"
                class="btn-text"
              >登录</span>
              <span
                v-else
                class="btn-text"
              >
                <svg
                  class="spin-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                >
                  <circle
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-dasharray="31.4 31.4"
                    stroke-linecap="round"
                  />
                </svg>
                登录中...
              </span>
            </button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useAdminStore } from '@/stores/admin'

const year = new Date().getFullYear()
const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm)
      if (res.code === 200) {
        const { token, admin } = res.data
        localStorage.setItem('admin_token', token)
        adminStore.setAdminInfo(admin)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      ElMessage.error('登录请求失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-root {
  display: flex;
  min-height: 100vh;
}

/* ── Left panel ── */
.login-left {
  position: relative;
  width: 420px;
  flex-shrink: 0;
  background: #111118;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-noise {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='1'/%3E%3C/svg%3E");
  opacity: 0.035;
  pointer-events: none;
}

.left-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.left-content {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 56px 48px;
  gap: 40px;
}

.brand-lockup {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}
.brand-logo svg { width: 100%; height: 100%; }

.brand-name {
  font-size: 22px;
  font-weight: 700;
  color: rgba(255,255,255,0.95);
  letter-spacing: -0.5px;
}

.brand-copy { display: flex; flex-direction: column; gap: 8px; }

.brand-tagline {
  font-size: 28px;
  font-weight: 700;
  color: rgba(255,255,255,0.92);
  line-height: 1.25;
  letter-spacing: -0.6px;
}

.brand-sub {
  font-size: 14px;
  color: rgba(255,255,255,0.42);
  line-height: 1.6;
}

.brand-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.bstat {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.bstat-num {
  font-size: 20px;
  font-weight: 700;
  color: rgba(255,255,255,0.88);
  letter-spacing: -0.4px;
  font-variant-numeric: tabular-nums;
}

.bstat-lbl {
  font-size: 11px;
  color: rgba(255,255,255,0.35);
  text-transform: uppercase;
  letter-spacing: 0.8px;
}

.bstat-divider {
  width: 1px;
  height: 32px;
  background: rgba(255,255,255,0.1);
}

.left-footer {
  position: relative;
  padding: 24px 48px;
  font-size: 12px;
  color: rgba(255,255,255,0.2);
}

/* ── Right panel ── */
.login-right {
  flex: 1;
  background: var(--c-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 36px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.form-desc {
  font-size: 14px;
  color: var(--c-text-muted);
}

/* Fields */
.field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  margin-bottom: 7px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.login-form :deep(.el-input__wrapper) {
  padding: 6px 14px;
  border-radius: var(--radius-sm) !important;
  background: var(--c-surface-2) !important;
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  transition: all var(--t-fast);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #b8b8bf inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff !important;
  box-shadow: 0 0 0 1.5px var(--c-primary) inset, 0 0 0 4px var(--c-primary-glow) !important;
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
  font-weight: 400;
}

/* Custom login button */
.login-btn {
  width: 100%;
  height: 46px;
  background: var(--c-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background var(--t-fast), transform var(--t-fast), box-shadow var(--t-fast);
  box-shadow: var(--shadow-pri);
  letter-spacing: 0.02em;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  background: var(--c-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(91,91,214,0.38);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: var(--shadow-pri);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spin-icon {
  width: 16px;
  height: 16px;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .login-left { display: none; }
}
</style>
