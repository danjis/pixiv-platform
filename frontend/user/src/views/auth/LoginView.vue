<template>
  <div class="auth-page">
    <!-- Left decorative panel -->
    <div class="auth-panel">
      <div class="panel-bg">
        <div class="panel-orb panel-orb-1"></div>
        <div class="panel-orb panel-orb-2"></div>
        <div class="panel-grid"></div>
      </div>
      <div class="panel-content">
        <div class="panel-logo" @click="$router.push('/')">
          <span class="panel-logo-mark">A</span><span class="panel-logo-rest">rtfolio</span>
        </div>
        <div class="panel-copy">
          <h2 class="panel-heading">创作，
            <br/>属于你的
            <br/><em>故事</em>
          </h2>
          <p class="panel-sub">加入数万创作者，在这里分享你最打动人心的作品</p>
        </div>
        <div class="panel-pills">
          <span class="pill">✦ 原创插画</span>
          <span class="pill">✦ 约稿委托</span>
          <span class="pill">✦ 比赛活动</span>
          <span class="pill">✦ 创作社区</span>
        </div>
      </div>
    </div>

    <!-- Right form panel -->
    <div class="auth-form-side">
      <div class="form-card">
        <div class="form-top">
          <h1 class="form-title">欢迎回来</h1>
          <p class="form-sub">登录以继续探索</p>
        </div>

        <!-- Tab switcher -->
        <div class="tab-row">
          <button
            class="tab-btn"
            :class="{ active: loginMode === 'password' }"
            @click="loginMode = 'password'"
          >密码登录</button>
          <button
            class="tab-btn"
            :class="{ active: loginMode === 'email' }"
            @click="loginMode = 'email'"
          >邮箱验证码</button>
        </div>

        <!-- Password login -->
        <el-form
          v-if="loginMode === 'password'"
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="auth-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="usernameOrEmail">
            <div class="field-wrap">
              <svg class="field-ico" viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z"/></svg>
              <el-input
                v-model="loginForm.usernameOrEmail"
                placeholder="用户名或邮箱地址"
                class="px-field"
              />
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <div class="field-wrap">
              <svg class="field-ico" viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码（至少8位）"
                show-password
                class="px-field"
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>
          <el-form-item>
            <button
              type="button"
              class="submit-btn"
              :class="{ loading }"
              :disabled="loading"
              @click="handleLogin"
            >
              <svg v-if="loading" class="spin" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
              {{ loading ? '登录中…' : '登录' }}
            </button>
          </el-form-item>
        </el-form>

        <!-- Email code login -->
        <el-form
          v-else
          ref="emailFormRef"
          :model="emailForm"
          :rules="emailRules"
          class="auth-form"
          @submit.prevent="handleEmailLogin"
        >
          <el-form-item prop="email">
            <div class="field-wrap">
              <svg class="field-ico" viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>
              <el-input
                v-model="emailForm.email"
                placeholder="邮箱地址"
                class="px-field"
              />
            </div>
          </el-form-item>
          <el-form-item prop="emailCode">
            <div class="field-wrap code-wrap">
              <svg class="field-ico" viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/></svg>
              <el-input
                v-model="emailForm.emailCode"
                placeholder="6位验证码"
                class="px-field"
                @keyup.enter="handleEmailLogin"
              />
              <button
                type="button"
                class="code-btn"
                :disabled="codeCooldown > 0 || sendingCode"
                @click="handleSendCode"
              >
                {{ sendingCode ? '发送中' : codeCooldown > 0 ? codeCooldown + 's' : '获取验证码' }}
              </button>
            </div>
          </el-form-item>
          <el-form-item>
            <button
              type="button"
              class="submit-btn"
              :class="{ loading }"
              :disabled="loading"
              @click="handleEmailLogin"
            >
              <svg v-if="loading" class="spin" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
              {{ loading ? '登录中…' : '登录' }}
            </button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="footer-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, loginByEmail, sendEmailCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const emailFormRef = ref(null)
const loading = ref(false)
const loginMode = ref('password')
const loginForm = ref({ usernameOrEmail: '', password: '' })
const emailForm = ref({ email: '', emailCode: '' })
const codeCooldown = ref(0)
const sendingCode = ref(false)
let cooldownTimer = null

const rules = {
  usernameOrEmail: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, message: '密码至少8位', trigger: 'blur' }]
}
const emailRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '验证码为6位', trigger: 'blur' }]
}

const handleSendCode = async () => {
  if (!emailForm.value.email) { ElMessage.warning('请先输入邮箱'); return }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.value.email)) { ElMessage.warning('邮箱格式不正确'); return }
  sendingCode.value = true
  try {
    await sendEmailCode(emailForm.value.email)
    ElMessage.success('验证码已发送')
    codeCooldown.value = 60
    cooldownTimer = setInterval(() => { if (--codeCooldown.value <= 0) clearInterval(cooldownTimer) }, 1000)
  } catch (e) { console.error(e) } finally { sendingCode.value = false }
}

const handleLoginSuccess = (response) => {
  userStore.setAuth({ token: response.data.accessToken, refreshToken: response.data.refreshToken, user: response.data.user })
  ElMessage.success('登录成功')
  setTimeout(() => router.push(route.query.redirect || '/artworks'), 300)
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try { const res = await login(loginForm.value); handleLoginSuccess(res) }
    catch (e) { console.error(e) } finally { loading.value = false }
  })
}

const handleEmailLogin = async () => {
  if (!emailFormRef.value) return
  await emailFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try { const res = await loginByEmail(emailForm.value); handleLoginSuccess(res) }
    catch (e) { console.error(e) } finally { loading.value = false }
  })
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: var(--ink-900);
}

/* ===== Left panel ===== */
.auth-panel {
  position: relative;
  flex: 0 0 440px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.panel-bg {
  position: absolute; inset: 0;
  background: linear-gradient(160deg, #1a0a06 0%, #0e0e0f 100%);
}
.panel-orb {
  position: absolute; border-radius: 50%; filter: blur(70px); pointer-events: none;
}
.panel-orb-1 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(255,107,71,0.22) 0%, transparent 70%);
  top: -80px; left: -100px;
}
.panel-orb-2 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(245,200,66,0.12) 0%, transparent 70%);
  bottom: 60px; right: -60px;
}
.panel-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(var(--ink-700) 1px, transparent 1px),
    linear-gradient(90deg, var(--ink-700) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.15;
}
.panel-content {
  position: relative; z-index: 1;
  height: 100%;
  display: flex; flex-direction: column;
  justify-content: space-between;
  padding: 40px 44px;
}
.panel-logo {
  display: flex; align-items: baseline; gap: 1px;
  cursor: pointer; text-decoration: none;
}
.panel-logo-mark {
  font-family: var(--px-font-display); font-weight: 900; font-style: italic;
  font-size: 28px; color: var(--coral); line-height: 1; letter-spacing: -1px;
}
.panel-logo-rest {
  font-family: var(--px-font-display); font-weight: 400; font-style: italic;
  font-size: 24px; color: var(--px-text-primary); letter-spacing: -0.5px;
}
.panel-copy { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.panel-heading {
  font-family: var(--px-font-display);
  font-size: clamp(36px, 3.5vw, 52px);
  font-weight: 900; line-height: 1.1;
  color: var(--px-text-primary);
  margin-bottom: 20px;
}
.panel-heading em {
  font-style: italic; color: var(--coral);
  -webkit-text-stroke: 0;
}
.panel-sub {
  font-size: 14px; color: var(--px-text-tertiary);
  line-height: 1.7; max-width: 300px;
}
.panel-pills {
  display: flex; flex-wrap: wrap; gap: 8px;
}
.pill {
  font-size: 11px; font-weight: 600;
  color: var(--px-text-tertiary);
  background: var(--ink-600);
  border: 1px solid var(--px-border);
  padding: 5px 12px; border-radius: var(--px-radius-round);
  font-family: var(--px-font-mono); letter-spacing: 0.5px;
  transition: all var(--px-transition-fast);
}
.pill:hover { color: var(--coral); border-color: var(--coral-dim); }

/* ===== Right form ===== */
.auth-form-side {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  padding: 48px 40px;
  background: var(--ink-800);
  border-left: 1px solid var(--px-border);
}
.form-card { width: 100%;  max-width: 380px; }
.form-top { margin-bottom: 28px; }
.form-title {
  font-family: var(--px-font-display);
  font-size: 28px; font-weight: 700;
  color: var(--px-text-primary); margin-bottom: 6px;
}
.form-sub { font-size: 13px; color: var(--px-text-tertiary); }

/* Tabs */
.tab-row {
  display: flex; gap: 0;
  border-bottom: 1px solid var(--px-border);
  margin-bottom: 28px;
}
.tab-btn {
  flex: 1; padding: 10px 0;
  font-size: 13px; font-weight: 500;
  color: var(--px-text-tertiary);
  background: none; border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer; transition: all var(--px-transition-fast);
  font-family: var(--px-font-family);
}
.tab-btn:hover { color: var(--px-text-secondary); }
.tab-btn.active { color: var(--coral); border-bottom-color: var(--coral); }

/* Fields */
.auth-form :deep(.el-form-item) { margin-bottom: 18px; }
.auth-form :deep(.el-form-item__content) { display: block; }
.field-wrap {
  display: flex; align-items: center; gap: 10px;
  background: var(--ink-700);
  border: 1px solid var(--px-border);
  border-radius: var(--px-radius-md);
  padding: 0 14px;
  transition: border-color var(--px-transition-fast), box-shadow var(--px-transition-fast);
}
.field-wrap:focus-within {
  border-color: var(--coral);
  box-shadow: 0 0 0 3px var(--coral-glow);
}
.field-ico { color: var(--ink-200); flex-shrink: 0; transition: color var(--px-transition-fast); }
.field-wrap:focus-within .field-ico { color: var(--coral); }
.px-field { flex: 1; }
.px-field :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  padding: 10px 0 !important;
}
.px-field :deep(.el-input__inner) {
  color: var(--px-text-primary) !important;
  font-size: 14px !important;
}
.px-field :deep(.el-input__inner::placeholder) { color: var(--px-text-placeholder) !important; }

/* Code field */
.code-wrap { padding-right: 6px; }
.code-btn {
  flex-shrink: 0;
  padding: 6px 14px;
  background: var(--ink-600);
  color: var(--coral);
  border: 1px solid var(--ink-400);
  border-radius: var(--px-radius-sm);
  font-size: 12px; font-weight: 600;
  font-family: var(--px-font-family);
  cursor: pointer; white-space: nowrap;
  transition: all var(--px-transition-fast);
}
.code-btn:hover:not(:disabled) { background: var(--coral); color: #fff; border-color: var(--coral); }
.code-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Submit */
.submit-btn {
  width: 100%; height: 46px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  background: var(--coral); color: #fff;
  border: none; border-radius: var(--px-radius-md);
  font-size: 15px; font-weight: 600;
  font-family: var(--px-font-family);
  cursor: pointer; transition: all var(--px-transition-base);
  letter-spacing: 0.3px;
}
.submit-btn:hover:not(:disabled) {
  background: var(--px-blue-hover);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--coral-glow);
}
.submit-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.spin { animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Footer */
.form-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--px-border);
  font-size: 13px; color: var(--px-text-tertiary);
}
.footer-link {
  color: var(--coral); font-weight: 600;
  margin-left: 4px; text-decoration: none;
  transition: color var(--px-transition-fast);
}
.footer-link:hover { color: var(--px-blue-hover); }

/* Responsive */
@media (max-width: 900px) {
  .auth-panel { display: none; }
  .auth-form-side { background: var(--ink-900); border-left: none; padding: 32px 20px; }
}
</style>