<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 左侧品牌区 -->
      <div class="auth-brand">
        <div class="brand-bg">
          <div class="brand-pattern"></div>
        </div>
        <div class="brand-content">
          <h1 class="brand-logo" @click="$router.push('/')">pixiv</h1>
          <p class="brand-tagline">发现好作品，连接创作者</p>
          <div class="brand-features">
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
              <span>数百万作品等你来发现</span>
            </div>
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
              <span>加入全球创作者社区</span>
            </div>
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              <span>收藏和互动你喜爱的作品</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-box">
          <h2 class="form-title">登录</h2>
          <p class="form-subtitle">欢迎回来</p>

          <!-- 登录方式切换 -->
          <div class="login-tabs">
            <button
              class="login-tab"
              :class="{ active: loginMode === 'password' }"
              @click="loginMode = 'password'"
            >密码登录</button>
            <button
              class="login-tab"
              :class="{ active: loginMode === 'email' }"
              @click="loginMode = 'email'"
            >邮箱验证码</button>
          </div>

          <!-- 密码登录表单 -->
          <el-form
            v-if="loginMode === 'password'"
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="auth-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="usernameOrEmail">
              <el-input
                v-model="loginForm.usernameOrEmail"
                placeholder="用户名或邮箱地址"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <button
                type="button"
                class="auth-submit-btn"
                :class="{ 'is-loading': loading }"
                :disabled="loading"
                @click="handleLogin"
              >
                <span v-if="!loading">登录</span>
                <span v-else class="btn-loading">
                  <svg class="spin-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/>
                  </svg>
                  登录中...
                </span>
              </button>
            </el-form-item>
          </el-form>

          <!-- 邮箱验证码登录表单 -->
          <el-form
            v-else
            ref="emailFormRef"
            :model="emailForm"
            :rules="emailRules"
            class="auth-form"
            @submit.prevent="handleEmailLogin"
          >
            <el-form-item prop="email">
              <el-input
                v-model="emailForm.email"
                placeholder="邮箱地址"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item prop="emailCode">
              <div class="code-input-wrapper">
                <el-input
                  v-model="emailForm.emailCode"
                  placeholder="邮箱验证码"
                  size="large"
                  :prefix-icon="Key"
                  @keyup.enter="handleEmailLogin"
                />
                <button
                  type="button"
                  class="send-code-btn"
                  :disabled="codeCooldown > 0 || sendingCode"
                  @click="handleSendCode"
                >
                  <span v-if="sendingCode">发送中...</span>
                  <span v-else-if="codeCooldown > 0">{{ codeCooldown }}s</span>
                  <span v-else>获取验证码</span>
                </button>
              </div>
            </el-form-item>

            <el-form-item>
              <button
                type="button"
                class="auth-submit-btn"
                :class="{ 'is-loading': loading }"
                :disabled="loading"
                @click="handleEmailLogin"
              >
                <span v-if="!loading">登录</span>
                <span v-else class="btn-loading">
                  <svg class="spin-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/>
                  </svg>
                  登录中...
                </span>
              </button>
            </el-form-item>
          </el-form>

          <div class="auth-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { login, loginByEmail, sendEmailCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const emailFormRef = ref(null)
const loading = ref(false)
const loginMode = ref('password')

// 密码登录表单
const loginForm = ref({
  usernameOrEmail: '',
  password: ''
})

// 邮箱验证码登录表单
const emailForm = ref({
  email: '',
  emailCode: ''
})

// 验证码发送相关
const codeCooldown = ref(0)
const sendingCode = ref(false)
let cooldownTimer = null

const rules = {
  usernameOrEmail: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少为 8 个字符', trigger: 'blur' }
  ]
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  if (!emailForm.value.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  // 简单邮箱格式校验
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.value.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  sendingCode.value = true
  try {
    await sendEmailCode(emailForm.value.email)
    ElMessage.success('验证码已发送到您的邮箱')
    // 开始倒计时
    codeCooldown.value = 60
    cooldownTimer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

// 处理登录成功后的公共逻辑
const handleLoginSuccess = (response) => {
  const authData = {
    token: response.data.accessToken,
    refreshToken: response.data.refreshToken,
    user: response.data.user
  }
  userStore.setAuth(authData)
  ElMessage.success('登录成功')
  setTimeout(() => {
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  }, 300)
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login(loginForm.value)
        handleLoginSuccess(response)
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleEmailLogin = async () => {
  if (!emailFormRef.value) return

  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await loginByEmail(emailForm.value)
        handleLoginSuccess(response)
      } catch (error) {
        console.error('邮箱登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* =============================================
   页面背景 - 契合二次元艺术站的高级动态流体光影与点阵设计
   ============================================= */
.auth-page {
  min-height: 100vh;
  /* 基础底色：干净冷白 */
  background: linear-gradient(135deg, #f8faff 0%, #edf2f9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

/* 动态流体光斑 - 经典ACG感幻彩渐变 */
.auth-page::before {
  content: '';
  position: absolute;
  top: -50%; left: -50%; width: 200%; height: 200%;
  /* 由主题蓝、温和紫、樱花粉、清透青组成的多重发光球 */
  background-image:
    radial-gradient(circle at 40% 30%, rgba(0, 150, 250, 0.15) 0%, transparent 40%),
    radial-gradient(circle at 60% 70%, rgba(160, 100, 255, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 80% 20%, rgba(255, 100, 180, 0.1) 0%, transparent 35%),
    radial-gradient(circle at 20% 80%, rgba(0, 220, 200, 0.12) 0%, transparent 45%);
  pointer-events: none;
  z-index: -2;
  /* 开启丝滑的流动动画 */
  animation: bg-fluid-motion 25s infinite alternate ease-in-out;
}

/* 科技设计感点阵叠加与光影融合蒙版 */
.auth-page::after {
  content: '';
  position: absolute;
  inset: 0;
  /* 细腻的设计感点阵 */
  background-image: radial-gradient(rgba(100, 130, 200, 0.25) 1px, transparent 1px);
  background-size: 24px 24px;
  background-position: center;
  pointer-events: none;
  z-index: -1;
  /* 让边缘柔和淡出，聚焦中心登录框 */
  mask-image: radial-gradient(circle at center, black 40%, rgba(0,0,0,0) 110%);
  -webkit-mask-image: radial-gradient(circle at center, black 40%, rgba(0,0,0,0) 110%);
}

@keyframes bg-fluid-motion {
  0% { transform: rotate(0deg) scale(1) translate(0px, 0px); }
  50% { transform: rotate(3deg) scale(1.05) translate(40px, -30px); }
  100% { transform: rotate(-2deg) scale(0.95) translate(-30px, 40px); }
}

.auth-container {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 540px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(0, 0, 0, 0.06),
    0 12px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fff;
}

/* =============================================
   左侧品牌区 - 深色调，光晕装饰
   ============================================= */
.auth-brand {
  position: relative;
  flex: 0 0 390px;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
}

/* 品牌区光晕装饰 */
.brand-bg::before {
  content: '';
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
  top: -60px;
  right: -60px;
}

.brand-bg::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.1) 0%, transparent 70%);
  bottom: 20px;
  left: -40px;
}

.brand-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.04) 1px, transparent 1px);
  background-size: 40px 40px;
}

.brand-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px 40px;
  color: #1e293b;
}

.brand-logo {
  font-family: Arial, sans-serif;
  font-weight: 900;
  font-size: 44px;
  font-style: italic;
  letter-spacing: -2px;
  margin: 0 0 10px;
  cursor: pointer;
  background: linear-gradient(135deg, #0369a1 0%, #0284c7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-tagline {
  font-size: 15px;
  color: #475569;
  margin: 0 0 48px;
  letter-spacing: 0.2px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.brand-feature {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #64748b;
}

.brand-feature svg {
  flex-shrink: 0;
  color: #0284c7;
}

/* =============================================
   右侧表单区 - 清爽白色
   ============================================= */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 44px;
  overflow-y: auto;
  background: #fff;
  border-left: 1px solid #f1f5f9;
}

.auth-form-box {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 28px;
}

/* 登录方式切换 Tab */
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 1px solid #e2e8f0;
}

.login-tab {
  flex: 1;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
  transition: all 0.22s ease;
}

.login-tab:hover {
  color: #64748b;
}

.login-tab.active {
  color: #0284c7;
  border-bottom-color: #0284c7;
}

/* 验证码输入行 */
.code-input-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-input-wrapper .el-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  width: 110px;
  height: 40px;
  border-radius: 8px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  color: #0284c7;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-code-btn:hover:not(:disabled) {
  background: #e0f2fe;
  border-color: #7dd3fc;
}

.send-code-btn:disabled {
  color: #cbd5e1;
  cursor: not-allowed;
  border-color: #e2e8f0;
  background: #f8fafc;
}

/* 表单 Element Plus 覆盖 */
.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-form-item__error) {
  color: #ef4444;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 9px !important;
  padding: 4px 12px;
  background: #f8fafc !important;
  box-shadow: none !important;
  border: 1px solid #e2e8f0 !important;
  transition: all 0.2s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1 !important;
  background: #f1f5f9 !important;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: #0284c7 !important;
  background: #fff !important;
  box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1) !important;
}

.auth-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #1e293b !important;
  caret-color: #0284c7;
}

.auth-form :deep(.el-input__inner::placeholder) {
  color: #cbd5e1 !important;
}

.auth-form :deep(.el-input__prefix-inner) {
  color: #94a3b8;
}

/* 提交按钮 */
.auth-submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
  letter-spacing: 0.3px;
  position: relative;
  overflow: hidden;
}

.auth-submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.2s;
}

.auth-submit-btn:hover:not(:disabled)::before {
  opacity: 1;
}

.auth-submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(2, 132, 199, 0.35);
}

.auth-submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spin-icon {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部链接 */
.auth-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.footer-text {
  font-size: 13px;
  color: #94a3b8;
}

.footer-link {
  font-size: 13px;
  font-weight: 600;
  color: #0284c7;
  margin-left: 4px;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #0369a1;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
    max-width: 440px;
    min-height: auto;
  }
  .auth-brand {
    flex: none;
  }
  .brand-content {
    padding: 32px 28px;
  }
  .brand-features {
    display: none;
  }
  .brand-logo {
    font-size: 34px;
    margin-bottom: 4px;
  }
  .brand-tagline {
    margin-bottom: 0;
    font-size: 14px;
  }
  .auth-form-wrapper {
    padding: 36px 28px;
  }
}
</style>
  
