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
          <p class="brand-tagline">创建你的账号，开始创作之旅</p>
          <div class="brand-features">
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
              <span>发布并展示你的艺术作品</span>
            </div>
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              <span>与创作者互动交流</span>
            </div>
            <div class="brand-feature">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
              <span>委托画师创作定制作品</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-box">
          <h2 class="form-title">注册</h2>
          <p class="form-subtitle">创建你的 pixiv 账号</p>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="rules"
            class="auth-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱地址"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>

            <!-- 图形验证码 -->
            <el-form-item prop="captchaCode">
              <div class="inline-field">
                <el-input
                  v-model="registerForm.captchaCode"
                  placeholder="图形验证码"
                  size="large"
                  :prefix-icon="Key"
                />
                <div class="captcha-box" @click="refreshCaptcha" :class="{ loading: loadingCaptcha }">
                  <img v-if="captchaImage && !loadingCaptcha" :src="captchaImage" alt="验证码" />
                  <span v-else-if="loadingCaptcha" class="captcha-loading">
                    <svg class="spin-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#999" stroke-width="2"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
                  </span>
                  <span v-else class="captcha-hint">点击获取</span>
                </div>
              </div>
            </el-form-item>

            <!-- 邮箱验证码 -->
            <el-form-item prop="emailCode">
              <div class="inline-field">
                <el-input
                  v-model="registerForm.emailCode"
                  placeholder="邮箱验证码"
                  size="large"
                  :prefix-icon="Message"
                />
                <button
                  type="button"
                  class="send-code-btn"
                  :disabled="countdown > 0 || sendingCode"
                  @click="sendCode"
                >
                  <svg v-if="sendingCode" class="spin-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（至少8位，包含字母和数字）"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item>
              <button
                type="button"
                class="auth-submit-btn"
                :class="{ 'is-loading': loading }"
                :disabled="loading"
                @click="handleRegister"
              >
                <span v-if="!loading">注册</span>
                <span v-else class="btn-loading">
                  <svg class="spin-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/></svg>
                  注册中...
                </span>
              </button>
            </el-form-item>
          </el-form>

          <div class="auth-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">返回登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { register } from '@/api/auth'
import { getImageCaptcha, sendEmailCode } from '@/api/captcha'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const loadingCaptcha = ref(false)
const captchaImage = ref('')
const captchaId = ref('')
const countdown = ref(0)
let countdownTimer = null

const registerForm = ref({
  username: '',
  email: '',
  captchaCode: '',
  emailCode: '',
  password: ''
})

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度至少为 8 个字符'))
  } else if (!/[a-zA-Z]/.test(value) || !/[0-9]/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '图形验证码为4位', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '邮箱验证码为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  loadingCaptcha.value = true
  try {
    const response = await getImageCaptcha()
    if (response.code === 200) {
      captchaImage.value = response.data.image
      captchaId.value = response.data.captchaId
    } else {
      ElMessage.error(response.message || '获取图形验证码失败')
    }
  } catch (error) {
    console.error('获取图形验证码失败:', error)
    ElMessage.error('获取图形验证码失败，请刷新页面重试')
  } finally {
    loadingCaptcha.value = false
  }
}

const sendCode = async () => {
  if (!registerForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!registerForm.value.captchaCode) {
    ElMessage.warning('请先输入图形验证码')
    return
  }

  sendingCode.value = true
  try {
    const response = await sendEmailCode(
      registerForm.value.email,
      captchaId.value,
      registerForm.value.captchaCode
    )
    if (response.code === 200) {
      ElMessage.success('验证码已发送到您的邮箱，请查收')
      countdown.value = 30
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) clearInterval(countdownTimer)
      }, 1000)
    } else {
      ElMessage.error(response.message || '发送失败，请重试')
      refreshCaptcha()
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请检查网络连接')
    refreshCaptcha()
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { username, email, emailCode, password } = registerForm.value
        await register({ username, email, emailCode, password })
        ElMessage.success('注册成功，请登录')
        router.push({ name: 'Login' })
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  refreshCaptcha()
})
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
  max-width: 940px;
  min-height: 600px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(0, 0, 0, 0.06),
    0 12px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04);
  background: #fff;
}

/* =============================================
   左侧品牌区
   ============================================= */
.auth-brand {
  position: relative;
  flex: 0 0 380px;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e0f2fe 0%, #dbeafe 100%);
}

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
   右侧表单区 - 玻璃拟态
   ============================================= */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 44px;
  overflow-y: auto;
  background: #fff;
  border-left: 1px solid #f1f5f9;
}

.auth-form-box {
  width: 100%;
  max-width: 360px;
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

/* 表单 Element Plus 覆盖 */
.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
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

/* 验证码行 */
.inline-field {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.inline-field .el-input {
  flex: 1;
}

/* 图形验证码框 */
.captcha-box {
  width: 108px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  overflow: hidden;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.captcha-box:hover {
  border-color: #cbd5e1;
}

.captcha-box.loading {
  cursor: wait;
}

.captcha-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-hint {
  font-size: 12px;
  color: #cbd5e1;
}

.captcha-loading {
  display: flex;
  align-items: center;
}

/* 发送验证码按钮 */
.send-code-btn {
  height: 40px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid #bae6fd;
  background: #f0f9ff;
  color: #0284c7;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.send-code-btn:hover:not(:disabled) {
  background: #e0f2fe;
  border-color: #7dd3fc;
}

.send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  border-color: #e2e8f0;
  color: #cbd5e1;
  background: #f8fafc;
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
  margin-top: 24px;
  padding-top: 18px;
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
    max-width: 460px;
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
    padding: 32px 28px;
  }
}
</style> 
