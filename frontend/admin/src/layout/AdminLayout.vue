<template>
  <div class="admin-layout">
    <el-container style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo">
          <img v-if="!isCollapse" src="" alt="" style="display:none" />
          <h3 v-if="!isCollapse">Pixiv 管理</h3>
          <h3 v-else>P</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          background-color="#1d1e1f"
          text-color="#a3a6ad"
          active-text-color="#409eff"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>数据概览</template>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/applications">
            <el-icon><Stamp /></el-icon>
            <template #title>
              <span>画师审核</span>
              <span v-if="pendingCount > 0" class="menu-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/artworks">
            <el-icon><Picture /></el-icon>
            <template #title>作品管理</template>
          </el-menu-item>
          <el-menu-item index="/audit-logs">
            <el-icon><Document /></el-icon>
            <template #title>审计日志</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-container>
        <el-header class="topbar">
          <div class="topbar-left">
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="topbar-right">
            <span class="admin-name">{{ adminStore.adminInfo?.username || '管理员' }}</span>
            <el-button type="danger" text @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出
            </el-button>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Odometer, User, Stamp, Picture, Document, Expand, Fold, SwitchButton } from '@element-plus/icons-vue'
import { useAdminStore } from '../stores/admin'
import { logout } from '../api/auth'
import { getPendingCount } from '../api/artist'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const isCollapse = ref(false)
const pendingCount = ref(0)

const activeMenu = computed(() => route.path)

const titleMap = {
  '/dashboard': '',
  '/users': '用户管理',
  '/applications': '画师审核',
  '/artworks': '作品管理',
  '/audit-logs': '审计日志'
}

const currentTitle = computed(() => titleMap[route.path] || '')

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingCount()
    pendingCount.value = res.data || 0
  } catch (e) {
    // 忽略错误
  }
}

onMounted(() => {
  loadPendingCount()
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try { await logout() } catch (e) { /* ignore */ }
    adminStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background-color: #1d1e1f;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #191a1b;
  border-bottom: 1px solid #2a2b2c;
}

.logo h3 {
  color: #409eff;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.menu-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  background-color: #f56c6c;
  color: #fff;
  font-size: 12px;
  margin-left: 8px;
  padding: 0 5px;
  box-sizing: border-box;
  vertical-align: middle;
  font-weight: normal;
}

.topbar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-name {
  color: #303133;
  font-size: 14px;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
