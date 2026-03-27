<template>
  <div class="admin-layout" :class="{ collapsed: isCollapsed }">
    <!-- 侧栏 -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo" @click="$router.push('/dashboard')">
        <div class="logo-mark">
          <svg viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="9" fill="url(#gLogo)"/>
            <path d="M11 18L17 12L23 18L17 24Z" fill="white" opacity="0.92"/>
            <path d="M17 12L23 18L29 12L23 6Z" fill="white" opacity="0.55"/>
            <defs>
              <linearGradient id="gLogo" x1="0" y1="0" x2="36" y2="36">
                <stop offset="0%" stop-color="#a88930"/>
                <stop offset="100%" stop-color="#e8c96e"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <transition name="slide-fade">
          <div v-show="!isCollapsed" class="logo-text-group">
            <span class="logo-title">Pixiv</span>
            <span class="logo-sub">Admin Console</span>
          </div>
        </transition>
      </div>

      <!-- 金线分割 -->
      <div class="sidebar-divider"></div>

      <!-- 导航 -->
      <nav class="sidebar-nav">
        <div v-for="group in navGroups" :key="group.label" class="nav-group">
          <transition name="fade">
            <span v-show="!isCollapsed" class="nav-group-label">{{ group.label }}</span>
          </transition>
          <router-link
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="nav-item"
            active-class="nav-item--active"
          >
            <span class="nav-icon">
              <component :is="item.icon" />
            </span>
            <transition name="fade">
              <span v-show="!isCollapsed" class="nav-label">{{ item.label }}</span>
            </transition>
            <transition name="fade">
              <span
                v-if="!isCollapsed && item.badge && pendingCount > 0"
                class="nav-badge"
              >{{ pendingCount }}</span>
            </transition>
            <span
              v-if="isCollapsed && item.badge && pendingCount > 0"
              class="nav-dot"
            ></span>
          </router-link>
        </div>
      </nav>

      <!-- 底部折叠按钮 -->
      <div class="sidebar-footer">
        <button class="collapse-toggle" @click="isCollapsed = !isCollapsed">
          <el-icon :size="16">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <transition name="fade">
            <span v-show="!isCollapsed" class="collapse-label">收起侧栏</span>
          </transition>
        </button>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main-wrapper">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="breadcrumb-line">
            <span class="bread-root">管理后台</span>
            <span class="bread-sep">/</span>
            <span class="bread-current">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="admin-chip">
            <div class="admin-avatar-ring">
              <div class="admin-avatar">{{ adminInitial }}</div>
            </div>
            <div class="admin-text">
              <span class="admin-name">{{ adminName }}</span>
              <span class="admin-role">超级管理员</span>
            </div>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <el-icon :size="15"><SwitchButton /></el-icon>
          </button>
        </div>
      </header>

      <!-- 内容 -->
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
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
const route  = useRoute()
const adminStore = useAdminStore()
const isCollapsed = ref(false)
const pendingCount = ref(0)

const adminName    = computed(() => adminStore.adminInfo?.username || '管理员')
const adminInitial = computed(() => adminName.value?.charAt(0)?.toUpperCase() || 'A')

const navGroups = [
  {
    label: '概览',
    items: [
      { to: '/dashboard', label: '数据概览', icon: markRaw(Odometer) }
    ]
  },
  {
    label: '用户',
    items: [
      { to: '/users',        label: '用户管理', icon: markRaw(User) },
      { to: '/applications', label: '画师审核', icon: markRaw(Stamp), badge: true },
      { to: '/membership',   label: '会员管理', icon: markRaw(Medal) }
    ]
  },
  {
    label: '内容',
    items: [
      { to: '/artworks',  label: '作品管理', icon: markRaw(Picture) },
      { to: '/comments',  label: '评论管理', icon: markRaw(ChatDotRound) },
      { to: '/contests',  label: '比赛管理', icon: markRaw(Trophy) }
    ]
  },
  {
    label: '交易',
    items: [
      { to: '/commissions', label: '约稿管理', icon: markRaw(Suitcase) },
      { to: '/payments',    label: '支付管理', icon: markRaw(Money) },
      { to: '/finance',     label: '财务管理', icon: markRaw(Coin) },
      { to: '/coupons',     label: '优惠券',   icon: markRaw(Ticket) }
    ]
  },
  {
    label: '系统',
    items: [
      { to: '/feedback',   label: '反馈管理', icon: markRaw(ChatLineSquare) },
      { to: '/audit-logs', label: '审计日志', icon: markRaw(Document) }
    ]
  }
]

const titleMap = {
  '/dashboard': '数据概览', '/users': '用户管理', '/applications': '画师审核',
  '/artworks': '作品管理', '/audit-logs': '审计日志', '/coupons': '优惠券管理',
  '/comments': '评论管理', '/commissions': '约稿管理', '/payments': '支付管理',
  '/finance': '财务管理', '/contests': '比赛管理', '/membership': '会员管理',
  '/feedback': '反馈管理'
}
const currentTitle = computed(() => titleMap[route.path] || '管理后台')

const loadPendingCount = async () => {
  try {
    const res = await getPendingApplications()
    if (res.code === 200) {
      const d = res.data
      pendingCount.value = d?.total || d?.length || 0
    }
  } catch { /* ignore */ }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning'
    })
    localStorage.removeItem('admin_token')
    adminStore.clearAdminInfo()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch { /* cancelled */ }
}

onMounted(() => { loadPendingCount() })
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* ===== 侧栏 ===== */
.sidebar {
  width: var(--sidebar-width);
  background: var(--c-sidebar);
  border-right: 1px solid var(--c-sidebar-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 100;
  overflow: hidden;
  transition: width var(--transition);
}
.collapsed .sidebar { width: var(--sidebar-collapsed); }

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 18px 16px 14px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity var(--transition-fast);
}
.sidebar-logo:hover { opacity: 0.85; }

.logo-mark {
  width: 36px; height: 36px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 8px rgba(201,168,76,.35));
}
.logo-mark svg { width: 100%; height: 100%; }

.logo-text-group {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
}
.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--c-text-gold-bright);
  letter-spacing: 1px;
  font-family: 'Noto Serif SC', serif;
}
.logo-sub {
  font-size: 9px;
  font-weight: 500;
  color: var(--c-text-inverse-muted);
  letter-spacing: 1.5px;
  text-transform: uppercase;
  margin-top: 1px;
}

/* 金线分割 */
.sidebar-divider {
  height: 1px;
  margin: 0 16px;
  background: linear-gradient(90deg, transparent, var(--c-border-gold), transparent);
  flex-shrink: 0;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0 4px;
}
.sidebar-nav::-webkit-scrollbar { width: 0; }

.nav-group { padding: 6px 0 2px; }

.nav-group-label {
  display: block;
  padding: 8px 20px 4px;
  font-size: 10px;
  font-weight: 700;
  color: var(--c-text-inverse-muted);
  text-transform: uppercase;
  letter-spacing: 1.5px;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 9px 14px;
  margin: 1px 8px;
  border-radius: var(--radius-sm);
  color: var(--c-text-inverse-secondary);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
  white-space: nowrap;
  overflow: hidden;
}
.collapsed .nav-item {
  justify-content: center;
  padding: 10px;
  margin: 1px 8px;
}
.nav-item:hover {
  background: var(--c-sidebar-hover);
  color: var(--c-text-gold);
}
.nav-item--active {
  background: var(--c-sidebar-active) !important;
  color: var(--c-primary-bright) !important;
}
.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0; top: 6px; bottom: 6px;
  width: 2.5px;
  border-radius: 0 2px 2px 0;
  background: var(--c-primary);
  box-shadow: 0 0 8px var(--c-primary-glow);
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px; height: 18px;
  flex-shrink: 0;
  font-size: 17px;
}

.nav-label { flex: 1; }

.nav-badge {
  margin-left: auto;
  padding: 1px 6px;
  height: 17px;
  line-height: 15px;
  border-radius: 9px;
  background: var(--c-danger);
  color: white;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}
.nav-dot {
  position: absolute;
  top: 7px; right: 7px;
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--c-danger);
  box-shadow: 0 0 5px var(--c-danger);
}

/* 底部折叠 */
.sidebar-footer {
  flex-shrink: 0;
  padding: 10px 8px 14px;
  border-top: 1px solid var(--c-sidebar-border);
}
.collapse-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--c-text-inverse-muted);
  cursor: pointer;
  font-size: 12px;
  transition: all var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
}
.collapse-toggle:hover {
  background: var(--c-sidebar-hover);
  color: var(--c-text-gold);
}
.collapse-label { font-size: 12px; }

/* ===== 主区域 ===== */
.main-wrapper {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left var(--transition);
}
.collapsed .main-wrapper { margin-left: var(--sidebar-collapsed); }

/* 顶栏 */
.topbar {
  height: var(--topbar-height);
  background: var(--c-topbar);
  border-bottom: 1px solid var(--c-topbar-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(12px);
}

.topbar-left { display: flex; align-items: center; }
.breadcrumb-line {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.bread-root { color: var(--c-text-muted); }
.bread-sep  { color: var(--c-border-gold); font-size: 11px; }
.bread-current {
  color: var(--c-text-inverse);
  font-weight: 600;
  font-size: 14px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 12px 5px 5px;
  background: var(--c-surface-2);
  border-radius: var(--radius-full);
  border: 1px solid var(--c-border);
}
.admin-avatar-ring {
  width: 28px; height: 28px;
  border-radius: 50%;
  padding: 1.5px;
  background: linear-gradient(135deg, var(--c-primary-active), var(--c-primary-bright));
}
.admin-avatar {
  width: 100%; height: 100%;
  border-radius: 50%;
  background: var(--c-sidebar);
  color: var(--c-primary-bright);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}
.admin-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}
.admin-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-text);
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
  width: 32px; height: 32px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--c-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.logout-btn:hover {
  background: var(--c-danger-bg);
  color: var(--c-danger);
}

/* 内容区 */
.content-area {
  flex: 1;
  padding: var(--page-padding);
  background: var(--c-bg);
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-fade-enter-active { transition: all 0.18s ease; }
.slide-fade-leave-active { transition: all 0.12s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translateX(-6px); }
</style>