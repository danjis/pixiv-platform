<template>
  <div class="studio-layout">
    <!-- 顶部导航 -->
    <header class="studio-header">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="back-link" title="返回首页">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
          </router-link>
          <router-link to="/studio" class="studio-brand">
            <span class="brand-logo">pixiv</span>
            <span class="brand-divider"></span>
            <span class="brand-label">创作者中心</span>
          </router-link>
        </div>
        <div class="header-right">
          <router-link to="/publish" class="header-action-btn">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            投稿作品
          </router-link>
          <div class="header-avatar" @click="$router.push('/profile')">
            <el-avatar :size="32" :src="userStore.user?.avatarUrl || defaultAvatar" />
          </div>
        </div>
      </div>
    </header>

    <div class="studio-body">
      <!-- 左侧边栏 -->
      <aside class="studio-sidebar">
        <nav class="sidebar-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </router-link>
        </nav>

        <div class="sidebar-footer">
          <router-link to="/" class="footer-link">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
            返回首页
          </router-link>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="studio-main">
        <router-view v-slot="{ Component }">
          <transition name="studio-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const navItems = [
  {
    path: '/studio',
    label: '仪表盘',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>'
  },
  {
    path: '/studio/artworks',
    label: '作品管理',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M22 16V4c0-1.1-.9-2-2-2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2zm-11-4l2.03 2.71L16 11l4 5H8l3-4zM2 6v14c0 1.1.9 2 2 2h14v-2H4V6H2z"/></svg>'
  },
  {
    path: '/studio/commissions',
    label: '接稿管理',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z"/></svg>'
  },
  {
    path: '/studio/earnings',
    label: '收入管理',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/></svg>'
  },
  {
    path: '/studio/plans',
    label: '约稿方案',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/></svg>'
  },
  {
    path: '/studio/settings',
    label: '画师设置',
    icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>'
  }
]

function isActive(path) {
  if (path === '/studio') return route.path === '/studio'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.studio-layout {
  min-height: 100vh;
  background: #f5f6f8;
}

/* 顶部导航 */
.studio-header {
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 100%;
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #666;
  transition: all 0.2s;
}
.back-link:hover {
  background: #f0f0f0;
  color: #0096FA;
}
.studio-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.brand-logo {
  font-family: Arial, sans-serif;
  font-weight: 900;
  font-size: 20px;
  color: #0096FA;
  font-style: italic;
  letter-spacing: -1px;
}
.brand-divider {
  width: 1px;
  height: 18px;
  background: #ddd;
}
.brand-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  background: #0096FA;
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s;
}
.header-action-btn:hover {
  background: #0080d5;
}
.header-avatar {
  cursor: pointer;
}

/* 主体布局 */
.studio-body {
  display: flex;
  min-height: calc(100vh - 52px);
}

/* 侧边栏 */
.studio-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: sticky;
  top: 52px;
  height: calc(100vh - 52px);
  flex-shrink: 0;
}
.sidebar-nav {
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 10px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.15s;
}
.nav-item:hover {
  background: #f5f6f8;
  color: #333;
}
.nav-item.active {
  background: #EBF5FF;
  color: #0096FA;
  font-weight: 600;
}
.nav-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}
.footer-link {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}
.footer-link:hover {
  color: #0096FA;
}

/* 主内容区 */
.studio-main {
  flex: 1;
  padding: 28px 40px;
  min-width: 0;
}

/* 过渡动画 */
.studio-fade-enter-active,
.studio-fade-leave-active {
  transition: opacity 0.15s ease;
}
.studio-fade-enter-from,
.studio-fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .studio-sidebar {
    width: 64px;
  }
  .nav-text, .brand-label, .brand-divider, .footer-link span {
    display: none;
  }
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
  .sidebar-footer {
    display: flex;
    justify-content: center;
  }
  .footer-link {
    justify-content: center;
  }
  .studio-main {
    padding: 20px 16px;
  }
}
</style>
