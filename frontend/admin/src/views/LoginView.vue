<template>
  <div class="login-root">
    <div class="bg-grid" />
    <div class="bg-glow" />
    <div class="bg-glow-2" />

    <!-- Left brand panel -->
    <div class="login-left">
      <div class="left-inner">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 40 40" fill="none">
              <rect width="40" height="40" rx="10" fill="rgba(245,166,35,0.12)"/>
              <rect width="40" height="40" rx="10" stroke="rgba(245,166,35,0.30)" stroke-width="1"/>
              <path d="M12 20L18 14L24 20L18 26Z" fill="#f5a623" opacity="0.95"/>
              <path d="M18 14L24 20L30 14L24 8Z" fill="#f5a623" opacity="0.40"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-name">Pixiv Admin</span>
            <span class="logo-ver">v3.0</span>
          </div>
        </div>

        <div class="brand-hero">
          <h1 class="hero-title">管理创作
            <span class="hero-accent">每一刻</span>
          </h1>
          <p class="hero-desc">统一管理用户、内容、交易与系统——<br>高效、安全、实时</p>
        </div>

        <div class="brand-metrics">
          <div class="metric" v-for="m in metrics" :key="m.label">
            <span class="metric-val">{{ m.val }}</span>
            <span class="metric-lbl">{{ m.label }}</span>
          </div>
        </div>

        <div class="feature-list">
          <div class="feature-item" v-for="f in features" :key="f">
            <span class="feature-dot"></span>
            <span>{{ f }}</span>
          </div>
        </div>
      </div>
      <p class="left-copyright">Pixiv Platform &copy; {{ year }} · All rights reserved</p>
    </div>

    <!-- Right login form -->
    <div class="login-right">
      <div class="form-panel">
        <div class="form-head">
          <div class="form-badge">ADMIN PORTAL</div>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-sub">请使用管理员账号登录控制台</p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="field-block">
            <label class="field-lbl">账号</label>
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="管理员账号"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
          </div>

          <div class="field-block">
            <label class="field-lbl">密码</label>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="登录密码"
                size="large"
                show-password
                :prefix-icon="Lock"
              />
            </el-form-item>
          </div>

          <el-form-item style="margin-top: 10px">
            <button
              type="button"
              class="login-btn"
              :class="{ loading: loading }"
              :disabled="loading"
              @click="handleLogin"
            >
              <span v-if="!loading" class="btn-inner">
                <span>登录控制台</span>
                <svg class="btn-arrow" viewBox="0 0 16 16" fill="none">
                  <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <span v-else class="btn-inner">
                <svg class="spin-icon" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5"
                    stroke-dasharray="31.4 31.4" stroke-linecap="round"/>
                </svg>
                <span>验证中...</span>
              </span>
            </button>
          </el-form-item>
        </el-form>

        <p class="form-footer">仅限授权管理员访问 · 操作将被审计记录</p>
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

const metrics = [
  { val: '14', label: '功能模块' },
  { val: '∞', label: '数据洞察' },
  { val: '24/7', label: '实时监控' }
]

const features = [
  '用户与画师权限管理',
  '作品审核与内容管理',
  '交易、财务与提现审批',
  '优惠券、会员与比赛系统',
  '全链路审计日志追踪'
]

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
  position: relative;
  overflow: hidden;
  background: #0b0b10;
}

/* Background */
.bg-grid {
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(rgba(245,166,35,0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(245,166,35,0.025) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
  z-index: 0;
}

.bg-glow {
  position: fixed;
  top: -20%;
  left: -10%;
  width: 60%;
  height: 60%;
  background: radial-gradient(ellipse, rgba(245,166,35,0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.bg-glow-2 {
  position: fixed;
  bottom: -20%;
  right: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(ellipse, rgba(91,91,214,0.06) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* Left panel */
.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 56px;
  position: relative;
  z-index: 1;
  border-right: 1px solid rgba(255,255,255,0.05);
}

.left-inner {
  display: flex;
  flex-direction: column;
  gap: 40px;
  flex: 1;
  justify-content: center;
}

/* Logo */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon { width: 40px; height: 40px; flex-shrink: 0; }
.logo-icon svg { width: 100%; height: 100%; }

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.logo-name {
  font-family: 'Syne', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: rgba(255,255,255,0.92);
  letter-spacing: -0.3px;
}
.logo-ver {
  font-size: 10px;
  color: rgba(255,255,255,0.28);
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.5px;
}

/* Hero */
.brand-hero { display: flex; flex-direction: column; gap: 14px; }

.hero-title {
  font-family: 'Syne', sans-serif;
  font-size: 42px;
  font-weight: 800;
  color: rgba(255,255,255,0.92);
  line-height: 1.15;
  letter-spacing: -1px;
}

.hero-accent {
  display: block;
  color: #f5a623;
  background: linear-gradient(90deg, #f5a623, #ffb84d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.44);
  line-height: 1.7;
  font-weight: 400;
}

/* Metrics */
.brand-metrics {
  display: flex;
  gap: 32px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-val {
  font-family: 'Syne', sans-serif;
  font-size: 28px;
  font-weight: 800;
  color: #f5a623;
  letter-spacing: -0.5px;
  line-height: 1;
}

.metric-lbl {
  font-size: 11px;
  color: rgba(255,255,255,0.32);
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* Features */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: rgba(255,255,255,0.48);
  font-weight: 400;
}

.feature-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f5a623;
  opacity: 0.7;
  flex-shrink: 0;
}

.left-copyright {
  font-size: 11px;
  color: rgba(255,255,255,0.18);
  letter-spacing: 0.3px;
}

/* Right panel */
.login-right {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
  background: rgba(15,15,22,0.60);
  backdrop-filter: blur(20px);
}

.form-panel {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Form head */
.form-head { display: flex; flex-direction: column; gap: 10px; }

.form-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 4px;
  background: rgba(245,166,35,0.12);
  border: 1px solid rgba(245,166,35,0.25);
  font-size: 10px;
  font-weight: 700;
  color: #f5a623;
  letter-spacing: 1.5px;
  font-family: 'Syne', sans-serif;
  width: fit-content;
}

.form-title {
  font-family: 'Syne', sans-serif;
  font-size: 26px;
  font-weight: 800;
  color: rgba(255,255,255,0.92);
  letter-spacing: -0.5px;
  margin: 0;
}

.form-sub {
  font-size: 13px;
  color: rgba(255,255,255,0.36);
  line-height: 1.6;
}

/* Login form */
.login-form { display: flex; flex-direction: column; gap: 0; }

.field-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.field-lbl {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255,255,255,0.36);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  font-family: 'Syne', sans-serif;
}

/* Login button */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #f5a623, #e8961a);
  color: #0a0a0e;
  font-family: 'Syne', sans-serif;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s cubic-bezier(0.16,1,0.3,1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(245,166,35,0.30);
}

.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent);
  opacity: 0;
  transition: opacity 0.18s;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 28px rgba(245,166,35,0.42);
}
.login-btn:hover:not(:disabled)::before { opacity: 1; }
.login-btn:active:not(:disabled) { transform: translateY(0); box-shadow: 0 2px 12px rgba(245,166,35,0.25); }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.login-btn.loading { background: linear-gradient(135deg, #c98c1a, #a87318); }

.btn-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.18s;
}
.login-btn:hover .btn-arrow { transform: translateX(3px); }

.spin-icon {
  width: 18px;
  height: 18px;
  animation: spin 0.7s linear infinite;
}

/* Footer */
.form-footer {
  text-align: center;
  font-size: 11px;
  color: rgba(255,255,255,0.20);
  letter-spacing: 0.3px;
  line-height: 1.6;
}

/* Override el-input for login */
:deep(.el-input--large .el-input__wrapper) {
  background: rgba(255,255,255,0.04) !important;
  border-radius: 8px !important;
  box-shadow: 0 0 0 1px rgba(255,255,255,0.10) inset !important;
  transition: box-shadow 0.15s !important;
}
:deep(.el-input--large .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255,255,255,0.20) inset !important;
}
:deep(.el-input--large .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #f5a623 inset, 0 0 0 4px rgba(245,166,35,0.12) !important;
}
:deep(.el-input--large .el-input__inner) {
  color: rgba(255,255,255,0.88) !important;
  font-size: 14px !important;
  font-family: 'Noto Sans SC', sans-serif !important;
}
:deep(.el-input--large .el-input__inner::placeholder) {
  color: rgba(255,255,255,0.22) !important;
}
:deep(.el-input--large .el-input__prefix-inner .el-icon) {
  color: rgba(255,255,255,0.28) !important;
}
:deep(.el-form-item) { margin-bottom: 0; }
:deep(.el-form-item__error) {
  color: #f87171 !important;
  font-size: 11px !important;
  padding-top: 4px !important;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right { width: 100%; background: #0b0b10; }
}
</style>
