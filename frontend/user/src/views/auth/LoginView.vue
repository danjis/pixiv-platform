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
.auth-page {
  min-height: 100vh;
  background: var(--px-bg-secondary, #f7f8fa);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 880px;
  min-height: 520px;
  border-radius: var(--px-radius-xl, 16px);
  overflow: hidden;
  box-shadow: var(--px-shadow-xl, 0 8px 32px rgba(0,0,0,0.16));
  background: #fff;
}

/* 左侧品牌区 */
.auth-brand {
  position: relative;
  flex: 0 0 380px;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, #0096FA 0%, #0052CC 100%);
}

.brand-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.08;
  background-image:
    radial-gradient(circle at 25% 25%, #fff 1px, transparent 1px),
    radial-gradient(circle at 75% 75%, #fff 1px, transparent 1px);
  background-size: 30px 30px;
}

.brand-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 36px;
  color: #fff;
}

.brand-logo {
  font-family: Arial, sans-serif;
  font-weight: 900;
  font-size: 42px;
  font-style: italic;
  letter-spacing: -2px;
  margin: 0 0 8px;
  cursor: pointer;
}

.brand-tagline {
  font-size: 16px;
  opacity: 0.85;
  margin: 0 0 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.brand-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  opacity: 0.9;
}

.brand-feature svg {
  flex-shrink: 0;
  opacity: 0.8;
}

/* 右侧表单区 */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.auth-form-box {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--px-text-primary, #1f1f1f);
  margin: 0 0 4px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--px-text-tertiary, #8c8c8c);
  margin: 0 0 24px;
}

/* 登录方式切换 Tab */
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--px-border-light, #f0f0f0);
}

.login-tab {
  flex: 1;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--px-text-tertiary, #8c8c8c);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-tab:hover {
  color: var(--px-text-secondary, #595959);
}

.login-tab.active {
  color: var(--px-blue, #0096FA);
  border-bottom-color: var(--px-blue, #0096FA);
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
  background: var(--px-bg-secondary, #f7f8fa);
  border: 1.5px solid var(--px-border, #e8e8e8);
  color: var(--px-blue, #0096FA);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-code-btn:hover:not(:disabled) {
  background: var(--px-blue, #0096FA);
  color: #fff;
  border-color: var(--px-blue, #0096FA);
}

.send-code-btn:disabled {
  color: var(--px-text-tertiary, #8c8c8c);
  cursor: not-allowed;
  opacity: 0.7;
}

/* 表单样式 */
.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px !important;
  padding: 4px 12px;
  background: var(--px-bg-secondary, #f7f8fa);
  box-shadow: none !important;
  border: 1.5px solid transparent;
  transition: all 0.2s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  border-color: var(--px-border, #e8e8e8);
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--px-blue, #0096FA);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(0, 150, 250, 0.1) !important;
}

.auth-form :deep(.el-input__inner) {
  font-size: 14px;
}

/* 提交按钮 */
.auth-submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  background: var(--px-blue, #0096FA);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-submit-btn:hover:not(:disabled) {
  background: var(--px-blue-hover, #007ACC);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 150, 250, 0.3);
}

.auth-submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.auth-submit-btn:disabled {
  opacity: 0.7;
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
  border-top: 1px solid var(--px-border-light, #f0f0f0);
}

.footer-text {
  font-size: 13px;
  color: var(--px-text-tertiary, #8c8c8c);
}

.footer-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--px-blue, #0096FA);
  margin-left: 4px;
}

.footer-link:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
    max-width: 420px;
    min-height: auto;
  }
  .auth-brand {
    flex: none;
    padding: 0;
  }
  .brand-content {
    padding: 32px 24px;
  }
  .brand-features {
    display: none;
  }
  .brand-logo {
    font-size: 32px;
    margin-bottom: 4px;
  }
  .brand-tagline {
    margin-bottom: 0;
    font-size: 14px;
  }
  .auth-form-wrapper {
    padding: 32px 24px;
  }
}
</style>
