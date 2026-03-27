<template>
  <div class="main-layout">
    <header class="px-header" :class="{ 'header-scrolled': isScrolled }">
      <div class="header-content">
        <router-link to="/" class="logo-link">
          <span class="logo-mark">A</span><span class="logo-rest">rtfolio</span>
        </router-link>

        <div class="search-container" ref="searchContainerRef">
          <div class="search-wrapper">
            <div class="search-field">
              <svg class="search-ico" viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/>
              </svg>
              <input
                v-model="keyword"
                class="search-input"
                placeholder="搜索作品、画师、标签..."
                @keyup.enter="handleSearch"
                @input="handleSearchInput"
                @focus="onSearchFocus"
              />
            </div>
            <div v-if="showSuggestions && suggestions.length > 0" class="search-suggestions">
              <div
                v-for="(item, idx) in suggestions"
                :key="idx"
                class="suggestion-item"
                @mousedown.prevent="selectSuggestion(item)"
              >
                <span class="sug-pill" :class="item.type">{{ item.type === 'tag' ? '#' : '+' }}</span>
                <span class="suggestion-text">{{ item.text }}</span>
                <span v-if="item.count" class="suggestion-count">{{ item.count }}</span>
              </div>
            </div>
          </div>
        </div>

        <nav class="main-nav">
          <router-link to="/artworks" class="nav-item">发现</router-link>
          <router-link to="/following" class="nav-item" v-if="userStore.isAuthenticated">关注</router-link>
          <router-link to="/ranking" class="nav-item">排行榜</router-link>
          <router-link to="/contests" class="nav-item">比赛</router-link>
        </nav>

        <div class="right-actions">
          <template v-if="userStore.isAuthenticated">
            <button v-if="userStore.isArtist" class="publish-btn" @click="$router.push('/publish')">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6z"/></svg>
              投稿
            </button>

            <button class="icon-btn" @click="$router.push('/notifications')">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M12 22c1.1 0 2-.9 2-2h-4a2 2 0 002 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
              </svg>
              <span v-if="unreadCount" class="notif-dot"></span>
            </button>

            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-trigger">
                <el-avatar :size="32" :src="userStore.user && userStore.user.avatarUrl ? userStore.user.avatarUrl : defaultAvatar" class="user-avatar" />
                <span class="user-name">{{ userStore.user && userStore.user.username }}</span>
                <svg viewBox="0 0 24 24" width="11" height="11" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="px-dropdown">
                  <div class="dropdown-header">
                    <el-avatar :size="28" :src="userStore.user && userStore.user.avatarUrl ? userStore.user.avatarUrl : defaultAvatar" />
                    <div>
                      <div class="dh-name">{{ userStore.user && userStore.user.username }}</div>
                      <div class="dh-role">{{ userStore.isArtist ? '✦ 画师' : '用户' }}</div>
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
                    通知 <span v-if="unreadCount" class="dd-badge">{{ unreadCount }}</span>
                  </el-dropdown-item>
                  <template v-if="otherAccounts.length > 0">
                    <div class="dd-divider-label">切换账号</div>
                    <el-dropdown-item v-for="acc in otherAccounts" :key="acc.id" :command="'switch-' + acc.id">
                      <div class="switch-row">
                        <el-avatar :size="20" :src="acc.avatarUrl || defaultAvatar" />
                        <span>{{ acc.username }}</span>
                        <span class="switch-role" :class="acc.role === 'ARTIST' ? 'artist' : 'user'">{{ acc.role === 'ARTIST' ? '画师' : '用户' }}</span>
                      </div>
                    </el-dropdown-item>
                  </template>
                  <el-dropdown-item command="addAccount"><span class="muted-item">+ 登录其他账号</span></el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else>
            <router-link to="/login" class="nav-ghost-link">登录</router-link>
            <button class="cta-btn" @click="$router.push('/register')">开始创作</button>
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

    <FloatingHelpButton />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'
import { getSearchSuggestions } from '@/api/artwork'
import { useWebSocketNotification } from '@/composables/useWebSocket'
import { ElNotification, ElMessage } from 'element-plus'
import FloatingHelpButton from '@/components/FloatingHelpButton.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { connect: wsConnect, disconnect: wsDisconnect, onChatMessage: wsOnChat } = useWebSocketNotification()

const keyword = ref('')
const unreadCount = ref(0)
const isScrolled = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const otherAccounts = computed(() => userStore.getOtherAccounts())
const suggestions = ref([])
const showSuggestions = ref(false)
const searchContainerRef = ref(null)
let searchTimer = null

const onSearchFocus = () => { if (suggestions.value.length > 0) showSuggestions.value = true }

const handleSearch = () => {
  showSuggestions.value = false
  if (keyword.value.trim()) router.push({ name: 'Artworks', query: { keyword: keyword.value } })
}

const handleSearchInput = () => {
  clearTimeout(searchTimer)
  const val = keyword.value.trim()
  if (!val) { suggestions.value = []; showSuggestions.value = false; return }
  searchTimer = setTimeout(async () => {
    try {
      const res = await getSearchSuggestions(val)
      if (res.code === 200 && res.data) { suggestions.value = res.data; showSuggestions.value = res.data.length > 0 }
    } catch { suggestions.value = []; showSuggestions.value = false }
  }, 300)
}

const selectSuggestion = (item) => {
  showSuggestions.value = false
  if (item.type === 'tag') router.push({ name: 'Artworks', query: { tag: item.text } })
  else { keyword.value = item.text; router.push({ name: 'Artworks', query: { keyword: item.text } }) }
}

const handleClickOutside = (e) => {
  if (searchContainerRef.value && !searchContainerRef.value.contains(e.target)) showSuggestions.value = false
}

const handleScroll = () => { isScrolled.value = window.scrollY > 20 }

const handleCommand = (cmd) => {
  if (cmd === 'logout') { userStore.clearAuth(); router.push('/') }
  else if (cmd === 'addAccount') { userStore.saveCurrentAccount(); userStore.clearAuth(); router.push('/login') }
  else if (cmd.startsWith('switch-')) {
    const accountId = parseInt(cmd.replace('switch-', ''))
    const account = userStore.savedAccounts.find(a => a.id === accountId)
    if (account) { userStore.switchToAccount(account); ElMessage.success(`已切换到 ${account.username}`); window.location.reload() }
  } else if (['profile','commissions','notifications','chat','studio','history','coupons','membership','orders'].includes(cmd)) {
    router.push(`/${cmd}`)
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', handleScroll, { passive: true })
  if (userStore.isAuthenticated) {
    try { const res = await getUnreadCount(); if (res.code === 200) unreadCount.value = res.data.count } catch (e) { console.error(e) }
    wsConnect((notification) => {
      unreadCount.value++
      ElNotification({ title: '新通知', message: notification.content || '你收到了一条新通知', type: 'info', duration: 4500,
        onClick: () => router.push(notification.linkUrl || '/notifications') })
    })
    wsOnChat((chatMsg) => {
      const chatConvId = chatMsg.conversationId ? Number(chatMsg.conversationId) : null
      const isOnChatPage = route.path === '/chat' || route.path === `/chat/${chatConvId}`
      if (!isOnChatPage) {
        ElNotification({ title: '新私信', message: `${chatMsg.senderName || '用户'}: ${chatMsg.content || '发来一条消息'}`, type: 'info', duration: 4500,
          onClick: () => router.push(chatConvId ? `/chat/${chatConvId}` : '/chat') })
      }
    })
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('scroll', handleScroll)
  wsDisconnect()
})
</script>

<style scoped>
.main-layout { min-height: 100vh; background: var(--px-bg-secondary); }

/* ===== Header ===== */
.px-header {
  height: var(--px-header-height);
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(14, 14, 15, 0.82);
  backdrop-filter: blur(20px) saturate(1.4);
  -webkit-backdrop-filter: blur(20px) saturate(1.4);
  border-bottom: 1px solid transparent;
  transition: border-color var(--px-transition-base), box-shadow var(--px-transition-base);
}
.px-header.header-scrolled {
  border-bottom-color: var(--px-border);
  box-shadow: 0 4px 32px rgba(0,0,0,0.5);
}
.header-content {
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Logo */
.logo-link { display: flex; align-items: baseline; gap: 1px; text-decoration: none; flex-shrink: 0; }
.logo-mark {
  font-family: var(--px-font-display);
  font-weight: 900;
  font-style: italic;
  font-size: 26px;
  color: var(--coral);
  line-height: 1;
  letter-spacing: -1px;
}
.logo-rest {
  font-family: var(--px-font-display);
  font-weight: 400;
  font-style: italic;
  font-size: 22px;
  color: var(--px-text-primary);
  letter-spacing: -0.5px;
  line-height: 1;
}

/* Search */
.search-container { flex: 1; max-width: 400px; position: relative; }
.search-wrapper { position: relative; }
.search-field {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  height: 36px;
  background: var(--ink-700);
  border: 1px solid var(--px-border);
  border-radius: var(--px-radius-round);
  transition: border-color var(--px-transition-fast), box-shadow var(--px-transition-fast);
}
.search-field:focus-within {
  border-color: var(--coral);
  box-shadow: 0 0 0 3px var(--coral-glow);
}
.search-ico { color: var(--ink-200); flex-shrink: 0; transition: color var(--px-transition-fast); }
.search-ico { color: var(--ink-200); flex-shrink: 0; transition: color var(--px-transition-fast); }
.search-field:focus-within .search-ico { color: var(--coral); }
.search-input {
  flex: 1; background: none; border: none; outline: none;
  color: var(--px-text-primary); font-family: var(--px-font-family); font-size: 13px;
}
.search-input::placeholder { color: var(--px-text-placeholder); }
.search-suggestions {
  position: absolute; top: calc(100% + 6px); left: 0; right: 0;
  background: var(--ink-700); border: 1px solid var(--px-border);
  border-radius: var(--px-radius-md); box-shadow: var(--px-shadow-lg);
  z-index: 2000; overflow: hidden; max-height: 320px; overflow-y: auto;
}
.suggestion-item { display: flex; align-items: center; gap: 10px; padding: 9px 14px; cursor: pointer; transition: background var(--px-transition-fast); }
.suggestion-item:hover { background: var(--ink-600); }
.sug-pill { font-size: 11px; font-weight: 700; padding: 2px 6px; border-radius: 3px; flex-shrink: 0; font-family: var(--px-font-mono); }
.sug-pill.tag { background: var(--amber-glow); color: var(--amber); }
.sug-pill.artwork { background: var(--coral-glow); color: var(--coral); }
.suggestion-text { flex: 1; font-size: 13px; color: var(--px-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.suggestion-count { font-size: 11px; color: var(--px-text-tertiary); flex-shrink: 0; }
.main-nav { display: flex; align-items: center; gap: 2px; }
.nav-item { padding: 5px 12px; font-size: 13px; font-weight: 500; color: var(--px-text-tertiary); text-decoration: none; border-radius: var(--px-radius-round); transition: all var(--px-transition-fast); white-space: nowrap; }
.nav-item:hover { color: var(--px-text-primary); background: var(--ink-600); }
.nav-item.router-link-active { color: var(--coral); background: var(--coral-glow); }
.right-actions { display: flex; align-items: center; gap: 12px; margin-left: auto; flex-shrink: 0; }
.publish-btn { display: flex; align-items: center; gap: 6px; padding: 6px 16px; background: var(--coral); color: #fff; border: none; border-radius: var(--px-radius-round); font-size: 13px; font-weight: 600; font-family: var(--px-font-family); cursor: pointer; transition: all var(--px-transition-fast); }
.publish-btn:hover { background: var(--px-blue-hover); transform: translateY(-1px); box-shadow: 0 4px 16px var(--coral-glow); }
.icon-btn { position: relative; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 50%; color: var(--px-text-tertiary); background: none; border: none; cursor: pointer; transition: all var(--px-transition-fast); }
.icon-btn:hover { color: var(--px-text-primary); background: var(--ink-600); }
.notif-dot { position: absolute; top: 7px; right: 7px; width: 7px; height: 7px; background: var(--coral); border-radius: 50%; border: 2px solid var(--px-bg-secondary); }
.user-trigger { display: flex; align-items: center; gap: 7px; cursor: pointer; padding: 4px 8px; border-radius: var(--px-radius-round); transition: background var(--px-transition-fast); }
.user-trigger:hover { background: var(--ink-600); }
.user-avatar { flex-shrink: 0; }
.user-name { font-size: 13px; font-weight: 500; color: var(--px-text-secondary); max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nav-ghost-link { font-size: 13px; font-weight: 500; color: var(--px-text-tertiary); text-decoration: none; padding: 6px 12px; border-radius: var(--px-radius-round); transition: all var(--px-transition-fast); }
.nav-ghost-link:hover { color: var(--px-text-primary); background: var(--ink-600); }
.cta-btn { padding: 7px 18px; background: var(--coral); color: #fff; border: none; border-radius: var(--px-radius-round); font-size: 13px; font-weight: 600; font-family: var(--px-font-family); cursor: pointer; transition: all var(--px-transition-fast); }
.cta-btn:hover { background: var(--px-blue-hover); transform: translateY(-1px); }
.dropdown-header { display: flex; align-items: center; gap: 10px; padding: 12px 16px 10px; border-bottom: 1px solid var(--px-border); margin-bottom: 4px; }
.dh-name { font-size: 13px; font-weight: 600; color: var(--px-text-primary); }
.dh-role { font-size: 11px; color: var(--px-text-tertiary); margin-top: 1px; }
.dd-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 18px; height: 18px; padding: 0 5px; background: var(--coral); color: #fff; font-size: 10px; font-weight: 700; border-radius: 9px; margin-left: 6px; }
.dd-divider-label { font-size: 11px; color: var(--px-text-tertiary); padding: 8px 16px 4px; border-top: 1px solid var(--px-border); margin-top: 4px; }
.switch-row { display: flex; align-items: center; gap: 8px; }
.switch-row span { font-size: 13px; color: var(--px-text-secondary); }
.switch-role { font-size: 10px; padding: 1px 6px; border-radius: 3px; font-weight: 500; }
.switch-role.artist { background: var(--amber-glow); color: var(--amber); }
.switch-role.user { background: var(--coral-glow); color: var(--coral); }
.muted-item { color: var(--px-text-tertiary); font-size: 13px; }
.main-content { min-height: calc(100vh - var(--px-header-height)); background: var(--px-bg-secondary); }
</style>