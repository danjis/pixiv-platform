<template>
  <div class="admin-layout" :class="{ collapsed: isCollapsed }">
    <!-- 侧栏 -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo" @click="$router.push('/dashboard')">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#6366f1"/>
            <path d="M10 16L15 11L20 16L15 21Z" fill="white" opacity="0.9"/>
            <path d="M15 11L20 16L25 11L20 6Z" fill="white" opacity="0.6"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-show="!isCollapsed" class="logo-text">Pixiv Admin</span>
        </transition>
      </div>

      <!-- 导航 -->
      <nav class="sidebar-nav">
        <div class="nav-section">
          <span v-show="!isCollapsed" class="nav-label">概览</span>
          <router-link to="/dashboard" class="nav-item" active-class="active">
            <el-icon><Odometer /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">数据概览</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span v-show="!isCollapsed" class="nav-label">用户</span>
          <router-link to="/users" class="nav-item" active-class="active">
            <el-icon><User /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">用户管理</span>
          </router-link>
          <router-link to="/applications" class="nav-item" active-class="active">
            <el-icon><Stamp /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">画师审核</span>
            <span v-if="pendingCount > 0 && !isCollapsed" class="nav-badge">{{ pendingCount }}</span>
            <span v-if="pendingCount > 0 && isCollapsed" class="nav-dot"></span>
          </router-link>
          <router-link to="/membership" class="nav-item" active-class="active">
            <el-icon><Medal /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">会员管理</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span v-show="!isCollapsed" class="nav-label">内容</span>
          <router-link to="/artworks" class="nav-item" active-class="active">
            <el-icon><Picture /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">作品管理</span>
          </router-link>
          <router-link to="/comments" class="nav-item" active-class="active">
            <el-icon><ChatDotRound /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">评论管理</span>
          </router-link>
          <router-link to="/contests" class="nav-item" active-class="active">
            <el-icon><Trophy /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">比赛管理</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span v-show="!isCollapsed" class="nav-label">交易</span>
          <router-link to="/commissions" class="nav-item" active-class="active">
            <el-icon><Suitcase /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">约稿管理</span>
          </router-link>
          <router-link to="/payments" class="nav-item" active-class="active">
            <el-icon><Money /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">支付管理</span>
          </router-link>
          <router-link to="/finance" class="nav-item" active-class="active">
            <el-icon><Coin /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">财务管理</span>
          </router-link>
          <router-link to="/coupons" class="nav-item" active-class="active">
            <el-icon><Ticket /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">优惠券管理</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span v-show="!isCollapsed" class="nav-label">系统</span>
          <router-link to="/feedback" class="nav-item" active-class="active">
            <el-icon><ChatLineSquare /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">反馈管理</span>
          </router-link>
          <router-link to="/audit-logs" class="nav-item" active-class="active">
            <el-icon><Document /></el-icon>
            <span v-show="!isCollapsed" class="nav-text">审计日志</span>
          </router-link>
        </div>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <el-icon :size="18">
              <Fold v-if="!isCollapsed" />
              <Expand v-else />
            </el-icon>
          </button>
          <div class="breadcrumb-area">
            <h3 class="page-title">{{ currentTitle }}</h3>
          </div>
        </div>
        <div class="topbar-right">
          <div class="admin-info">
            <div class="admin-avatar">
              {{ adminName?.charAt(0)?.toUpperCase() || 'A' }}
            </div>
            <span class="admin-name">{{ adminName || '管理员' }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <el-icon :size="16"><SwitchButton /></el-icon>
          </button>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Odometer, User, Stamp, Picture, Document, Ticket,
  ChatDotRound, ChatLineSquare, Suitcase, Money, Trophy, Medal,
  Fold, Expand, SwitchButton, Coin
} from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import { getPendingApplications } from '@/api/artist'
import { getCurrentAdmin } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const isCollapsed = ref(false)
const pendingCount = ref(0)

// 提供给子页面（如 ApplicationsView）共享待审数量
provide('pendingApplicationCount', pendingCount)

const adminName = computed(() => adminStore.adminInfo?.username || '')

const titleMap = {
  '/dashboard': '数据概览',
  '/users': '用户管理',
  '/applications': '画师审核',
  '/artworks': '作品管理',
  '/audit-logs': '审计日志',
  '/coupons': '优惠券管理',
  '/comments': '评论管理',
  '/commissions': '约稿管理',
  '/payments': '支付管理',
  '/finance': '财务管理',
  '/contests': '比赛管理',
  '/membership': '会员管理'
}

const currentTitle = computed(() => titleMap[route.path] || '管理后台')

const loadPendingCount = async () => {
  try {
    const res = await getPendingApplications()
    if (res.code === 200) {
      const data = res.data
      pendingCount.value = data?.total || data?.length || 0
    }
  } catch { /* ignore */ }
}

// provide loadPendingCount after definition
provide('refreshPendingCount', loadPendingCount)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('admin_token')
    adminStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch { /* cancelled */ }
}

onMounted(() => {
  loadPendingCount()
  // 刷新后恢复管理员信息
  if (!adminStore.adminInfo && localStorage.getItem('admin_token')) {
    getCurrentAdmin().then(res => {
      if (res.code === 200 && res.data) {
        adminStore.setAdminInfo(res.data)
      }
    }).catch(() => { /* ignore */ })
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* ============ 侧栏 ============ */
.sidebar {
  width: 240px;
  background: var(--c-sidebar);
  display: flex;
  flex-direction: column;
  transition: width var(--transition);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow: hidden;
}

.collapsed .sidebar {
  width: 72px;
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-text-inverse);
  white-space: nowrap;
  letter-spacing: -0.3px;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.sidebar-nav::-webkit-scrollbar {
  width: 0;
}

.nav-section {
  padding: 4px 0;
}

.nav-label {
  display: block;
  padding: 12px 24px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--c-text-inverse-muted);
  text-transform: uppercase;
  letter-spacing: 1.2px;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  margin: 2px 10px;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.55);
  text-decoration: none;
  font-size: 14px;
  transition: all var(--transition-fast);
  position: relative;
  white-space: nowrap;
}

.collapsed .nav-item {
  justify-content: center;
  padding: 10px;
  margin: 2px 10px;
}

.nav-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: rgba(255, 255, 255, 0.85);
}

.nav-item.active {
  background: rgba(99, 102, 241, 0.18);
  color: #a5b4fc;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #6366f1;
}

.collapsed .nav-item.active::before {
  left: -10px;
}

.nav-item .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-text {
  white-space: nowrap;
}

.nav-badge {
  margin-left: auto;
  padding: 0 7px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 600;
}

.nav-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ef4444;
}

/* ============ 主内容区 ============ */
.main-wrapper {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left var(--transition);
}

.collapsed .main-wrapper {
  margin-left: 72px;
}

/* 顶栏 */
.topbar {
  height: 60px;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--c-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--c-bg);
  color: var(--c-text);
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.admin-name {
  font-size: 13px;
  color: var(--c-text-secondary);
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  background: #fef2f2;
  color: var(--c-danger);
}

/* 内容区 */
.content-area {
  flex: 1;
  padding: var(--page-padding);
  background: var(--c-bg);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
