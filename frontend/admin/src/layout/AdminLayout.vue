<template>
  <div class="admin-layout" :class="{ collapsed: isCollapsed }">
    <!-- Sidebar -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo" @click="$router.push('/dashboard')">
        <div class="logo-mark">
          <svg viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="9" fill="rgba(255,255,255,0.10)"/>
            <rect width="36" height="36" rx="9" stroke="rgba(255,255,255,0.14)" stroke-width="1"/>
            <path d="M10 18L16 12L22 18L16 24Z" fill="white" opacity="0.95"/>
            <path d="M16 12L22 18L28 12L22 6Z" fill="white" opacity="0.50"/>
          </svg>
        </div>
        <transition name="fade">
          <div v-show="!isCollapsed" class="logo-text-wrap">
            <span class="logo-title">Pixiv Admin</span>
            <span class="logo-version">v2.0</span>
          </div>
        </transition>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <div v-for="section in navSections" :key="section.label" class="nav-section">
          <transition name="fade">
            <span v-show="!isCollapsed" class="nav-label">{{ section.label }}</span>
          </transition>
          <router-link
            v-for="item in section.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            active-class="is-active"
            :title="isCollapsed ? item.text : ''"
          >
            <span class="nav-icon"><el-icon :size="17"><component :is="item.icon" /></el-icon></span>
            <transition name="fade">
              <span v-show="!isCollapsed" class="nav-text">{{ item.text }}</span>
            </transition>
            <transition name="fade">
              <span
                v-if="item.badge && item.badge > 0 && !isCollapsed"
                class="nav-badge"
              >{{ item.badge }}</span>
            </transition>
            <span
              v-if="item.badge && item.badge > 0 && isCollapsed"
              class="nav-dot"
            ></span>
          </router-link>
        </div>
      </nav>

      <!-- Collapse toggle -->
      <button class="sidebar-collapse-btn" @click="isCollapsed = !isCollapsed" :title="isCollapsed ? '展开' : '收起'">
        <el-icon :size="15">
          <component :is="isCollapsed ? Expand : Fold" />
        </el-icon>
        <transition name="fade">
          <span v-show="!isCollapsed">收起</span>
        </transition>
      </button>
    </aside>

    <!-- Main wrapper -->
    <div class="main-wrapper">
      <!-- Topbar -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="page-breadcrumb">
            <span class="breadcrumb-home">管理后台</span>
            <span class="breadcrumb-sep">/</span>
            <span class="breadcrumb-current">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="topbar-time">{{ currentTime }}</div>
          <div class="topbar-divider"></div>
          <div class="admin-pill">
            <div class="admin-avatar">{{ adminInitial }}</div>
            <div class="admin-meta">
              <span class="admin-name">{{ adminName }}</span>
              <span class="admin-role">Super Admin</span>
            </div>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <el-icon :size="15"><SwitchButton /></el-icon>
          </button>
        </div>
      </header>

      <!-- Content -->
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Odometer, User, Stamp, Picture, Document, Ticket,
  ChatDotRound, ChatLineSquare, Suitcase, Money, Trophy, Medal,
  Fold, Expand, SwitchButton, Coin
} from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'
import { getPendingApplications } from '@/api/artist'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const isCollapsed = ref(false)
const pendingCount = ref(0)
const currentTime = ref('')

const adminName = computed(() => adminStore.adminInfo?.username || '管理员')
const adminInitial = computed(() => adminName.value.charAt(0).toUpperCase())

const titleMap = {
  '/dashboard':   '数据概览',
  '/users':       '用户管理',
  '/applications':'画师审核',
  '/artworks':    '作品管理',
  '/comments':    '评论管理',
  '/contests':    '比赛管理',
  '/commissions': '约稿管理',
  '/payments':    '支付管理',
  '/finance':     '财务管理',
  '/coupons':     '优惠券管理',
  '/membership':  '会员管理',
  '/feedback':    '反馈管理',
  '/audit-logs':  '审计日志'
}

const currentTitle = computed(() => titleMap[route.path] || '管理后台')

const navSections = computed(() => [
  {
    label: '概览',
    items: [
      { path: '/dashboard',   text: '数据概览',   icon: markRaw(Odometer) }
    ]
  },
  {
    label: '用户',
    items: [
      { path: '/users',        text: '用户管理',   icon: markRaw(User) },
      { path: '/applications', text: '画师审核',   icon: markRaw(Stamp), badge: pendingCount.value },
      { path: '/membership',   text: '会员管理',   icon: markRaw(Medal) }
    ]
  },
  {
    label: '内容',
    items: [
      { path: '/artworks',  text: '作品管理', icon: markRaw(Picture) },
      { path: '/comments',  text: '评论管理', icon: markRaw(ChatDotRound) },
      { path: '/contests',  text: '比赛管理', icon: markRaw(Trophy) }
    ]
  },
  {
    label: '交易',
    items: [
      { path: '/commissions', text: '约稿管理',   icon: markRaw(Suitcase) },
      { path: '/payments',    text: '支付管理',   icon: markRaw(Money) },
      { path: '/finance',     text: '财务管理',   icon: markRaw(Coin) },
      { path: '/coupons',     text: '优惠券',     icon: markRaw(Ticket) }
    ]
  },
  {
    label: '系统',
    items: [
      { path: '/feedback',   text: '反馈管理', icon: markRaw(ChatLineSquare) },
      { path: '/audit-logs', text: '审计日志', icon: markRaw(Document) }
    ]
  }
])

let timer = null
function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const loadPendingCount = async () => {
  try {
    const res = await getPendingApplications()
    if (res.code === 200) {
      const data = res.data
      pendingCount.value = data?.total || data?.length || 0
    }
  } catch { /* ignore */ }
}

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
  updateTime()
  timer = setInterval(updateTime, 30000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* ═══════════════════════════════
   SIDEBAR
═══════════════════════════════ */
.sidebar {
  width: var(--sidebar-w);
  background: var(--c-sidebar);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow: hidden;
  transition: width var(--t-slow) var(--ease-out);
  border-right: 1px solid var(--c-sidebar-border);
}

.collapsed .sidebar {
  width: var(--sidebar-w-sm);
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px 18px;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid var(--c-sidebar-border);
  min-height: 68px;
}

.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}
.logo-mark svg { width: 100%; height: 100%; }

.logo-text-wrap {
  display: flex;
  flex-direction: column;
  gap: 1px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  color: rgba(255,255,255,0.92);
  letter-spacing: -0.3px;
}

.logo-version {
  font-size: 10px;
  color: rgba(255,255,255,0.28);
  letter-spacing: 0.5px;
  font-family: 'DM Mono', monospace;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 0 8px;
  scrollbar-width: none;
}
.sidebar-nav::-webkit-scrollbar { width: 0; }

.nav-section {
  padding: 2px 0;
}

.nav-label {
  display: block;
  padding: 12px 18px 5px;
  font-size: 10px;
  font-weight: 700;
  color: var(--c-sidebar-label);
  text-transform: uppercase;
  letter-spacing: 1.2px;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  margin: 1px 8px;
  border-radius: var(--radius-sm);
  color: var(--c-sidebar-item);
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 500;
  transition: all var(--t-fast);
  position: relative;
  white-space: nowrap;
  overflow: hidden;
}

.collapsed .nav-item {
  justify-content: center;
  padding: 10px 0;
  margin: 1px 10px;
}

.nav-item:hover {
  background: rgba(255,255,255,0.06);
  color: var(--c-sidebar-item-h);
}

.nav-item.is-active {
  background: var(--c-sidebar-active);
  color: var(--c-sidebar-active-t);
}

.nav-item.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  border-radius: 0 3px 3px 0;
  background: var(--c-sidebar-active-t);
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 20px;
}

.nav-text { flex: 1; overflow: hidden; text-overflow: ellipsis; }

.nav-badge {
  flex-shrink: 0;
  padding: 0 6px;
  height: 17px;
  line-height: 17px;
  border-radius: 9px;
  background: #dc2626;
  color: white;
  font-size: 10px;
  font-weight: 700;
  font-family: 'DM Mono', monospace;
}

.nav-dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #dc2626;
}

/* Collapse button */
.sidebar-collapse-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  border: none;
  background: transparent;
  color: var(--c-sidebar-label);
  font-size: 12px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  border-top: 1px solid var(--c-sidebar-border);
  transition: color var(--t-fast);
  white-space: nowrap;
  overflow: hidden;
}
.collapsed .sidebar-collapse-btn { justify-content: center; }
.sidebar-collapse-btn:hover { color: var(--c-sidebar-item-h); }

/* ═══════════════════════════════
   MAIN WRAPPER
═══════════════════════════════ */
.main-wrapper {
  flex: 1;
  margin-left: var(--sidebar-w);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left var(--t-slow) var(--ease-out);
}

.collapsed .main-wrapper {
  margin-left: var(--sidebar-w-sm);
}

/* Topbar */
.topbar {
  height: var(--topbar-h);
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left { display: flex; align-items: center; }

.page-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.breadcrumb-home { color: var(--c-text-muted); }
.breadcrumb-sep  { color: var(--c-border); }
.breadcrumb-current {
  font-weight: 600;
  color: var(--c-text);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-time {
  font-size: 13px;
  font-weight: 500;
  color: var(--c-text-muted);
  font-family: 'DM Mono', monospace;
  letter-spacing: 0.5px;
}

.topbar-divider {
  width: 1px;
  height: 20px;
  background: var(--c-border);
}

.admin-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  border-radius: 100px;
  background: var(--c-surface-2);
  border: 1px solid var(--c-border);
}

.admin-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--c-primary), #7c7ce8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.admin-meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.admin-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--c-text);
  line-height: 1.2;
}
.admin-role {
  font-size: 10px;
  color: var(--c-text-muted);
  letter-spacing: 0.3px;
}
.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--c-border);
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all var(--t-fast);
}
.logout-btn:hover {
  background: var(--c-danger-bg);
  border-color: transparent;
  color: var(--c-danger);
}
.content-area {
  flex: 1;
  padding: var(--page-padding);
  background: var(--c-bg);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--t-fast);
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
