<template>
  <div class="login-page">
    <!-- 背景层 -->
    <div class="login-bg">
      <div class="bg-grid"></div>
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-topline"></div>

      <div class="login-brand">
        <div class="brand-emblem">
          <svg viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="url(#gLogin)"/>
            <path d="M14 24L22 16L30 24L22 32Z" fill="white" opacity="0.92"/>
            <path d="M22 16L30 24L38 16L30 8Z" fill="white" opacity="0.55"/>
            <defs>
              <linearGradient id="gLogin" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#a88930"/>
                <stop offset="100%" stop-color="#e8c96e"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="brand-title">Pixiv Admin</h1>
        <p class="brand-desc">创作平台管理控制台</p>
      </div>

      <div class="login-divider"></div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <div class="field-group">
          <label class="field-label">管理员账号</label>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
        </div>
        <div class="field-group">
          <label class="field-label">登录密码</label>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
        </div>
        <el-form-item>
          <button
            class="login-btn"
            :class="{ loading }"
            @click.prevent="handleLogin"
            :disabled="loading"
          >
            <span v-if="!loading" class="btn-content">
              <span class="btn-text">进入控制台</span>
              <svg class="btn-arrow" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </span>
            <span v-else class="btn-spinner"></span>
          </button>
        </el-form-item>
      </el-form>

      <p class="login-footer">Pixiv Platform · Admin Console · {{ new Date().getFullYear() }}</p>
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

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码',   trigger: 'blur' }]
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
      } else { ElMessage.error(res.message || '登录失败') }
    } catch { ElMessage.error('登录请求失败') }
    finally { loading.value = false }
  })
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--c-bg);
  overflow: hidden;
}

/* 背景 */
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(201,168,76,.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(201,168,76,.04) 1px, transparent 1px);
  background-size: 52px 52px;
  mask-image: radial-gradient(ellipse 80% 80% at 50% 50%, black 20%, transparent 100%);
}
.bg-orb { position: absolute; border-radius: 50%; filter: blur(100px); opacity: .09; }
.bg-orb-1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, #c9a84c, transparent 70%);
  top: -20%; right: -12%;
  animation: orb-drift 24s ease-in-out infinite;
}
.bg-orb-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #2ec4b6, transparent 70%);
  bottom: -15%; left: -10%;
  animation: orb-drift 30s ease-in-out infinite reverse;
}
.bg-orb-3 {
  width: 320px; height: 320px;
  background: radial-gradient(circle, #8b7cf6, transparent 70%);
  top: 40%; left: 38%;
  animation: orb-drift 20s ease-in-out infinite 4s;
}
@keyframes orb-drift {
  0%,100% { transform: translate(0,0) scale(1); }
  33% { transform: translate(-30px,20px) scale(1.05); }
  66% { transform: translate(20px,-30px) scale(.95); }
}

/* 卡片 */
.login-card {
  position: relative;
  width: 420px;
  background: var(--c-surface);
  border: 1px solid var(--c-border-gold);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg), var(--shadow-gold), inset 0 1px 0 rgba(255,255,255,.06);
  padding: 0 40px 36px;
  overflow: hidden;
  animation: card-in .5s cubic-bezier(.34,1.56,.64,1) both;
}
@keyframes card-in {
  from { opacity: 0; transform: translateY(28px) scale(.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.card-topline {
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--c-primary), var(--c-primary-bright), var(--c-primary), transparent);
  margin-bottom: 38px;
}

/* 品牌区 */
.login-brand { text-align: center; margin-bottom: 30px; }
.brand-emblem {
  width: 58px; height: 58px;
  margin: 0 auto 18px;
  filter: drop-shadow(0 4px 18px rgba(201,168,76,.42));
  animation: emblem-glow 3s ease-in-out infinite alternate;
}
.brand-emblem svg { width: 100%; height: 100%; }
@keyframes emblem-glow {
  from { filter: drop-shadow(0 4px 12px rgba(201,168,76,.30)); }
  to   { filter: drop-shadow(0 4px 26px rgba(201,168,76,.58)); }
}
.brand-title {
  font-size: 23px;
  font-weight: 700;
  color: var(--c-text-inverse);
  letter-spacing: .5px;
  margin-bottom: 7px;
  font-family: 'DM Sans', sans-serif;
}
.brand-desc {
  font-size: 12px;
  color: var(--c-text-muted);
  letter-spacing: 1.2px;
}

.login-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--c-border-gold), transparent);
  margin-bottom: 28px;
}

/* 表单 */
.login-form { margin-top: 0; }
.field-group { margin-bottom: 6px; }
.field-label {
  display: block;
  font-size: 10.5px;
  font-weight: 700;
  color: var(--c-text-muted);
  letter-spacing: 1.2px;
  text-transform: uppercase;
  margin-bottom: 7px;
  font-family: 'DM Sans', sans-serif;
}
.login-form :deep(.el-form-item) { margin-bottom: 18px; }
.login-form :deep(.el-input__wrapper) { padding: 6px 14px; }

/* 登录按钮 */
.login-btn {
  width: 100%; height: 48px; border: none;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--c-primary-active), var(--c-primary) 50%, var(--c-primary-bright));
  color: #0a1018; font-size: 14px; font-weight: 700; letter-spacing: 1.5px;
  cursor: pointer; transition: all var(--transition-fast);
  position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.login-btn::before {
  content: '';
  position: absolute; inset: 0;
  background: linear-gradient(135deg, transparent, rgba(255,255,255,.18), transparent);
  transform: translateX(-100%);
  transition: transform .45s ease;
}
.login-btn:hover::before { transform: translateX(100%); }
.login-btn:hover {
  box-shadow: 0 6px 28px rgba(201,168,76,.45);
  transform: translateY(-1px);
}
.login-btn:active { transform: translateY(0); }
.login-btn.loading { opacity: .75; cursor: not-allowed; }

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}
.btn-arrow {
  width: 16px; height: 16px;
  transition: transform var(--transition-fast);
}
.login-btn:hover .btn-arrow { transform: translateX(3px); }

.btn-spinner {
  display: inline-block;
  width: 18px; height: 18px;
  border: 2px solid rgba(10,16,24,.3);
  border-top-color: #0a1018;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.login-footer {
  text-align: center;
  font-size: 11px;
  color: var(--c-text-muted);
  margin-top: 28px;
  letter-spacing: .5px;
}
</style>
