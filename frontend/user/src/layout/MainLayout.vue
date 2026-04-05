<template>
  <div class="main-layout">
    <header class="px-header">
      <div class="header-content">
        <router-link to="/" class="logo-link">
          <svg class="logo-icon" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/>
          </svg>
          <span class="logo-text">幻画空间</span>
        </router-link>

        <div class="search-container" ref="searchContainerRef">
          <div class="search-wrapper">
            <el-input
              v-model="keyword"
              placeholder="搜索插画、画师或约稿企划..."
              @keyup.enter="handleSearch"
              @input="handleSearchInput"
              @focus="showSuggestions = suggestions.length > 0"
              class="px-search-input"
            >
              <template #suffix>
                <div class="search-suffix-row">
                  <el-icon class="camera-icon" @click.stop="goImageSearch" title="以图搜图"><Camera /></el-icon>
                  <span class="suffix-divider"></span>
                  <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
                </div>
              </template>
            </el-input>
            <!-- 搜索建议下拉 -->
            <div v-if="showSuggestions && suggestions.length > 0" class="search-suggestions">
              <div
                v-for="(item, idx) in suggestions"
                :key="idx"
                class="suggestion-item"
                @mousedown.prevent="selectSuggestion(item)"
              >
                <span class="suggestion-icon">
                  <svg v-if="item.type === 'tag'" viewBox="0 0 24 24" width="14" height="14" fill="#999"><path d="M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z"/></svg>
                  <svg v-else viewBox="0 0 24 24" width="14" height="14" fill="#999"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
                </span>
                <span class="suggestion-text">{{ item.text }}</span>
                <span v-if="item.type === 'tag' && item.count" class="suggestion-count">{{ item.count }} 作品</span>
                <span class="suggestion-type">{{ item.type === 'tag' ? '标签' : '作品' }}</span>
              </div>
            </div>
          </div>
        </div>

        <nav class="main-nav">
          <router-link to="/artworks" class="nav-item" active-class="router-link-active" exact-active-class="router-link-exact-active">发现</router-link>
          <router-link to="/following" class="nav-item" v-if="userStore.isAuthenticated" active-class="router-link-active" exact-active-class="router-link-exact-active">关注</router-link>
          <router-link to="/ranking" class="nav-item" active-class="router-link-active" exact-active-class="router-link-exact-active">排行榜</router-link>
          <router-link to="/contests" class="nav-item" active-class="router-link-active" exact-active-class="router-link-exact-active">比赛</router-link>
        </nav>

        <div class="right-actions">
          <template v-if="userStore.isAuthenticated">
            <el-button v-if="userStore.isArtist" class="publish-btn" round @click="$router.push('/publish')">
              <el-icon><Upload /></el-icon>
              投稿作品
            </el-button>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar-container">
                <el-avatar :size="40" :src="userStore.user?.avatarUrl || defaultAvatar" class="user-avatar" />
                <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="px-dropdown">
                  <!-- 当前账号信息 -->
                  <div class="current-account-info">
                    <el-avatar :size="28" :src="userStore.user?.avatarUrl || defaultAvatar" />
                    <div class="account-detail">
                      <span class="account-name">{{ userStore.user?.username }}</span>
                      <span class="account-role">{{ userStore.isArtist ? '画师' : '用户' }}</span>
                    </div>
                  </div>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isArtist" command="studio">创作者中心</el-dropdown-item>
                  <el-dropdown-item command="chat">私信</el-dropdown-item>
                  <el-dropdown-item command="commissions">约稿管理</el-dropdown-item>
                  <el-dropdown-item command="history">浏览记录</el-dropdown-item>
                  <el-dropdown-item command="coupons">我的优惠券</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="membership">会员中心</el-dropdown-item>
                  <el-dropdown-item command="notifications">
                    通知 <el-badge :value="unreadCount" :hidden="!unreadCount" is-dot class="nav-badge" />
                  </el-dropdown-item>
                  <!-- 快速切换账号 -->
                  <template v-if="otherAccounts.length > 0">
                    <div class="dropdown-divider-label">切换账号</div>
                    <el-dropdown-item
                      v-for="acc in otherAccounts"
                      :key="acc.id"
                      :command="'switch-' + acc.id"
                    >
                      <div class="switch-account-item">
                        <el-avatar :size="22" :src="acc.avatarUrl || defaultAvatar" />
                        <span class="switch-name">{{ acc.username }}</span>
                        <span class="switch-role-tag" :class="acc.role === 'ARTIST' ? 'artist' : 'user'">
                          {{ acc.role === 'ARTIST' ? '画师' : '用户' }}
                        </span>
                      </div>
                    </el-dropdown-item>
                  </template>
                  <el-dropdown-item command="addAccount">
                    <div class="switch-account-item add-account">
                      <svg viewBox="0 0 24 24" width="18" height="18" fill="#999"><path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                      <span>登录其他账号</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <el-button type="primary" round class="register-btn" @click="$router.push('/register')">
              快速注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 悬浮客服/反馈按钮 -->
    <FloatingHelpButton />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'
import { getSearchSuggestions } from '@/api/artwork'
import { useWebSocketNotification } from '@/composables/useWebSocket'
import { Search, Upload, CaretBottom, Camera } from '@element-plus/icons-vue'
import { ElNotification, ElMessage } from 'element-plus'
import FloatingHelpButton from '@/components/FloatingHelpButton.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { connect: wsConnect, disconnect: wsDisconnect, onChatMessage: wsOnChat } = useWebSocketNotification()
const keyword = ref('')
const unreadCount = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 提供给子组件（如 NotificationView）共享未读数
provide('notificationUnreadCount', unreadCount)

// 其他已保存的账号
const otherAccounts = computed(() => userStore.getOtherAccounts())

// 搜索建议
const suggestions = ref([])
const showSuggestions = ref(false)
const searchContainerRef = ref(null)
let searchTimer = null

const handleSearch = () => {
  showSuggestions.value = false
  if (keyword.value.trim()) {
    router.push({ name: 'Artworks', query: { keyword: keyword.value } })
  }
}

const goImageSearch = () => {
  router.push({ name: 'ImageSearch' })
}

const handleSearchInput = () => {
  clearTimeout(searchTimer)
  const val = keyword.value.trim()
  if (!val) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await getSearchSuggestions(val)
      if (res.code === 200 && res.data) {
        suggestions.value = res.data
        showSuggestions.value = suggestions.value.length > 0
      }
    } catch {
      suggestions.value = []
      showSuggestions.value = false
    }
  }, 300)
}

const selectSuggestion = (item) => {
  showSuggestions.value = false
  if (item.type === 'tag') {
    router.push({ name: 'Artworks', query: { tag: item.text } })
  } else {
    keyword.value = item.text
    router.push({ name: 'Artworks', query: { keyword: item.text } })
  }
}

// 点击外部关闭下拉
const handleClickOutside = (e) => {
  if (searchContainerRef.value && !searchContainerRef.value.contains(e.target)) {
    showSuggestions.value = false
  }
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.clearAuth()
    router.push('/')
  } else if (cmd === 'addAccount') {
    // 保存当前账号后跳转到登录页
    userStore.saveCurrentAccount()
    userStore.clearAuth()
    router.push('/login')
  } else if (cmd.startsWith('switch-')) {
    // 切换到已保存的账号
    const accountId = parseInt(cmd.replace('switch-', ''))
    const account = userStore.savedAccounts.find(a => a.id === accountId)
    if (account) {
      userStore.switchToAccount(account)
      ElMessage.success(`已切换到 ${account.username}`)
      // 刷新页面以重新加载数据
      window.location.reload()
    }
  } else if (['profile', 'commissions', 'notifications', 'chat', 'studio', 'history', 'coupons', 'membership', 'orders'].includes(cmd)) {
    router.push(`/${cmd}`)
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  if (userStore.isAuthenticated) {
    try {
      const res = await getUnreadCount()
      if (res.code === 200) unreadCount.value = res.data.count
    } catch (e) {
      console.error(e)
    }
    // 建立 WebSocket 连接，实时接收通知
    wsConnect((notification) => {
      unreadCount.value++
      ElNotification({
        title: '新通知',
        message: notification.content || '你收到了一条新通知',
        type: 'info',
        duration: 4500,
        onClick: () => {
          if (notification.linkUrl) {
            router.push(notification.linkUrl)
          } else {
            router.push('/notifications')
          }
        }
      })
    })

    // 注册私信 WebSocket 回调：不在聊天页时弹出提醒
    wsOnChat((chatMsg) => {
      // 如果当前不在对应的聊天页面，弹出通知提醒
      const currentPath = route.path
      const chatConvId = chatMsg.conversationId ? Number(chatMsg.conversationId) : null
      const isOnChatPage = currentPath === '/chat' || currentPath === `/chat/${chatConvId}`
      if (!isOnChatPage) {
        ElNotification({
          title: '新私信',
          message: `${chatMsg.senderName || '用户'}: ${chatMsg.content || '发来一条消息'}`,
          type: 'info',
          duration: 4500,
          onClick: () => {
            if (chatConvId) {
              router.push(`/chat/${chatConvId}`)
            } else {
              router.push('/chat')
            }
          }
        })
      }
    })
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  wsDisconnect()
})
</script>

<style scoped>
/* Pixiv 风格导航栏样式 */
.px-header {
    height: 56px;
    background-color: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-bottom: 1px solid rgba(0,0,0,0.06);
    position: sticky;
    top: 0;
    z-index: 1000;
    display: flex;
    justify-content: center;
    box-shadow: 0 1px 12px rgba(0,0,0,0.02);
  }

.header-content {
  width: 100%;
  max-width: 1200px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-icon {
  color: var(--px-blue);
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--px-text-primary);
  letter-spacing: 1px;
}

.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 24px;
  position: relative;
}

.search-wrapper {
  position: relative;
}

/* 搜索建议下拉 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  z-index: 2000;
  overflow: hidden;
  max-height: 360px;
  overflow-y: auto;
}
.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
  font-size: 14px;
  color: #333;
}
.suggestion-item:hover {
  background: #f5f7fa;
}
.suggestion-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.suggestion-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.suggestion-count {
  font-size: 12px;
  color: #bbb;
  flex-shrink: 0;
}
.suggestion-type {
  font-size: 11px;
  color: #aaa;
  background: #f0f0f0;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 12px;
}

.nav-item {
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  text-decoration: none;
  border-radius: 20px;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  background-color: #f2f4f5;
  color: #0096fa;
}

.nav-item.router-link-active {
  background-color: #e8f4fd;
  color: #0096fa;
}

/* 覆盖 Element 输入框样式，使其更扁平 */
.px-search-input :deep(.el-input__wrapper) {
  background-color: #f2f4f5;
  box-shadow: none;
  border-radius: 4px;
  transition: all 0.2s;
}

.px-search-input :deep(.el-input__wrapper:hover),
.px-search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #d0d0d0 inset;
}

.search-suffix-row {
  display: flex;
  align-items: center;
  gap: 4px;
}
.camera-icon {
  cursor: pointer;
  color: #999;
  font-size: 16px;
  transition: color 0.15s;
}
.camera-icon:hover { color: var(--px-blue, #0096FA); }
.suffix-divider {
  width: 1px;
  height: 14px;
  background: #ddd;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-icon-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: #666;
  transition: all 0.2s;
}
.nav-icon-link:hover {
  background-color: #f2f4f5;
  color: #0096fa;
}

.publish-btn {
  background-color: #333;
  color: #fff;
  border: none;
  font-weight: 600;
}
.publish-btn:hover {
  background-color: #555;
}

.nav-link {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  text-decoration: none;
}

.register-btn {
  font-weight: 600;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.main-content {
  min-height: calc(100vh - 56px);
  background-color: #f2f4f5; /* 全局浅灰背景 */
}

/* 当前账号信息 */
.current-account-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px 8px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}
.account-detail {
  display: flex;
  flex-direction: column;
}
.account-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}
.account-role {
  font-size: 11px;
  color: #999;
}

/* 切换账号分隔标签 */
.dropdown-divider-label {
  font-size: 11px;
  color: #999;
  padding: 8px 16px 4px;
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}

/* 切换账号项 */
.switch-account-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.switch-name {
  flex: 1;
  font-size: 13px;
  color: #333;
}
.switch-role-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
}
.switch-role-tag.artist {
  background: #fff3e0;
  color: #e65100;
}
.switch-role-tag.user {
  background: #e3f2fd;
  color: #1565c0;
}
.add-account {
  color: #999;
  font-size: 13px;
}
.add-account svg {
  flex-shrink: 0;
}
</style>