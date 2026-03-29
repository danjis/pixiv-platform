<template>
  <div class="login-page">
    <!-- 装饰背景 -->
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌区 -->
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#6366f1"/>
            <path d="M12 20L18 14L24 20L18 26Z" fill="white" opacity="0.9"/>
            <path d="M18 14L24 20L30 14L24 8Z" fill="white" opacity="0.6"/>
          </svg>
        </div>
        <h1 class="brand-title">Pixiv Admin</h1>
        <p class="brand-desc">创作平台管理控制台</p>
      </div>

      <!-- 表单 -->
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="管理员账号"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
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
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-footer">Pixiv Platform &copy; {{ new Date().getFullYear() }}</p>
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

const loginForm = reactive({
  username: '',
  password: ''
})

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
    } catch {
      ElMessage.error('登录请求失败')
    } finally {
      loading.value = false
    }
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
  background: var(--c-sidebar);
  overflow: hidden;
}

/* 装饰背景 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  background: #6366f1;
  top: -15%;
  right: -10%;
  animation: float-1 20s ease-in-out infinite;
}

.bg-shape-2 {
  width: 400px;
  height: 400px;
  background: #818cf8;
  bottom: -10%;
  left: -8%;
  animation: float-2 25s ease-in-out infinite;
}

.bg-shape-3 {
  width: 300px;
  height: 300px;
  background: #a5b4fc;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float-3 18s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-40px, 30px); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -40px); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.15); }
}

/* 卡片 */
.login-card {
  position: relative;
  width: 400px;
  padding: 48px 40px 36px;
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

/* 品牌 */
.login-brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 16px;
}

.brand-icon svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.5px;
  margin-bottom: 6px;
}

.brand-desc {
  font-size: 13px;
  color: var(--c-text-muted);
}

/* 表单 */
.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 14px;
  border-radius: var(--radius-sm);
  background: #f8fafc;
  box-shadow: 0 0 0 1px var(--c-border) inset !important;
  transition: all var(--transition-fast);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1.5px var(--c-primary) inset !important;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: var(--c-text-muted);
  font-size: 16px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-sm) !important;
  letter-spacing: 2px;
}

/* 页脚 */
.login-footer {
  text-align: center;
  font-size: 12px;
  color: var(--c-text-muted);
  margin-top: 28px;
}
</style>
