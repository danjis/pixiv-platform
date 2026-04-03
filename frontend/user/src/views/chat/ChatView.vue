<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧: 对话列表 -->
      <div class="conversation-list" :class="{ 'mobile-hidden': activeConversation }">
        <div class="list-header">
          <h2 class="list-title">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="var(--px-blue, #0096FA)" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            私信
          </h2>
          <span class="list-badge" v-if="totalUnread > 0">{{ totalUnread > 99 ? '99+' : totalUnread }}</span>
        </div>

        <!-- 搜索对话 -->
        <div class="list-search">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#999" stroke-width="1.8"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input v-model="searchQuery" placeholder="搜索联系人..." class="search-input" />
        </div>

        <div v-if="loadingConversations" class="list-loading">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="filteredConversations.length === 0" class="list-empty">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ccc" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <p>暂无私信</p>
          <span class="empty-hint">在作品详情页点击"私信"开始对话</span>
        </div>

        <div v-else class="conversation-items">
          <div
            v-for="conv in filteredConversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: activeConversation?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar-wrap">
              <el-avatar :size="44" :src="conv.otherUserAvatar || defaultAvatar">
                {{ conv.otherUserName?.charAt(0) }}
              </el-avatar>
              <span class="online-dot" v-if="conv.isOnline"></span>
            </div>
            <div class="conv-info">
              <div class="conv-top">
                <span class="conv-name">{{ conv.otherUserName }}</span>
                <span class="conv-time">{{ formatTime(conv.lastMessageAt) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-last-msg">{{ conv.lastMessage || '开始对话吧' }}</span>
                <span v-if="conv.unreadCount > 0" class="conv-unread">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧: 消息面板 -->
      <div class="message-panel" :class="{ 'mobile-hidden': !activeConversation }">
        <!-- 未选中状态 -->
        <div v-if="!activeConversation" class="no-conversation">
          <div class="no-conv-icon">
            <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="#ddd" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          </div>
          <p class="no-conv-title">选择一个对话开始聊天</p>
          <p class="no-conv-hint">基于 WebSocket 的实时通讯</p>
        </div>

        <!-- 消息区域 -->
        <template v-else>
          <!-- 消息头部 -->
          <div class="msg-header">
            <button class="back-btn" @click="activeConversation = null">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
            </button>
            <el-avatar :size="36" :src="activeConversation.otherUserAvatar || defaultAvatar">
              {{ activeConversation.otherUserName?.charAt(0) }}
            </el-avatar>
            <div class="msg-header-info">
              <span class="msg-header-name">{{ activeConversation.otherUserName }}</span>
              <span class="msg-header-status">WebSocket 实时连接</span>
            </div>
            <div class="msg-header-actions">
              <button class="header-action-btn" @click="goToArtist(activeConversation.otherUserId)" title="查看主页">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list" @scroll="handleScroll">
            <div v-if="loadingMessages" class="messages-loading">
              <div class="loading-spinner"></div>
            </div>

            <!-- 日期分隔线 -->
            <template v-for="(msg, idx) in displayMessages" :key="msg.id">
              <div v-if="shouldShowDate(idx)" class="msg-date-sep">
                <span>{{ formatDateLabel(msg.createdAt) }}</span>
              </div>
              <div class="message-item" :class="{ mine: msg.senderId === currentUserId }">
                <el-avatar v-if="msg.senderId !== currentUserId" :size="32" :src="msg.senderAvatar || defaultAvatar">
                  {{ msg.senderName?.charAt(0) }}
                </el-avatar>
                <div class="message-bubble">
                  <div class="msg-content">
                    <img v-if="msg.messageType === 'IMAGE' && msg.attachmentUrl" :src="msg.attachmentUrl" class="msg-image" @click="previewImage(msg.attachmentUrl)" />
                    <p v-else>{{ msg.content }}</p>
                  </div>
                  <span class="msg-time">{{ formatMsgTime(msg.createdAt) }}</span>
                </div>
              </div>
            </template>

            <div v-if="!loadingMessages && displayMessages.length === 0" class="no-messages">
              <p>发送第一条消息吧 👋</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="msg-input-area">
            <!-- 快捷表情 -->
            <div class="emoji-bar">
              <button
                v-for="emoji in quickEmojis"
                :key="emoji"
                class="emoji-btn"
                @click="insertEmoji(emoji)"
              >{{ emoji }}</button>
            </div>
            <div class="input-row">
              <input
                v-model="messageInput"
                class="msg-input"
                placeholder="输入消息... (Enter 发送)"
                @keydown.enter="handleSend"
                :disabled="sending"
                maxlength="2000"
              />
              <button class="send-btn" @click="handleSend" :disabled="!messageInput.trim() || sending">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyConversations, getConversation, getMessages, sendMessage, markAsRead } from '@/api/chat'
import { useWebSocketNotification } from '@/composables/useWebSocket'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { onChatMessage, offChatMessage } = useWebSocketNotification()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const currentUserId = computed(() => userStore.user?.id)

// 对话列表
const conversations = ref([])
const loadingConversations = ref(false)
const activeConversation = ref(null)
const searchQuery = ref('')

// 消息
const messages = ref([])
const loadingMessages = ref(false)
const messageInput = ref('')
const sending = ref(false)
const messageListRef = ref(null)

// 快捷表情
const quickEmojis = ['😊', '😂', '❤️', '👍', '🎉', '😭', '🔥', '✨', '💕', '🤔']

// 搜索过滤
const filteredConversations = computed(() => {
  if (!searchQuery.value.trim()) return conversations.value
  const q = searchQuery.value.trim().toLowerCase()
  return conversations.value.filter(c =>
    c.otherUserName?.toLowerCase().includes(q) ||
    c.lastMessage?.toLowerCase().includes(q)
  )
})

// 未读总数
const totalUnread = computed(() => {
  return conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
})

// 消息按时间正序显示（API 返回倒序）
const displayMessages = computed(() => {
  return [...messages.value].reverse()
})

onMounted(async () => {
  await loadConversations()
  // 如果路由有 :id 参数，自动选中
  if (route.params.id) {
    const convId = Number(route.params.id)
    const found = conversations.value.find(c => c.id === convId)
    if (found) {
      await selectConversation(found)
    } else {
      // 尝试直接加载该对话
      try {
        const res = await getConversation(convId)
        if (res.code === 200 && res.data) {
          activeConversation.value = res.data
          await loadMessages(convId)
        }
      } catch {
        // 忽略
      }
    }
  }

  // 注册 WebSocket 私信回调，实时接收对方发来的消息
  onChatMessage(handleIncomingMessage)
})

onBeforeUnmount(() => {
  offChatMessage()
})

/**
 * 处理 WebSocket 推送的实时私信
 */
async function handleIncomingMessage(chatMsg) {
  const convId = chatMsg.conversationId ? Number(chatMsg.conversationId) : null

  // 如果当前正在查看该对话，直接追加消息到列表
  if (activeConversation.value && activeConversation.value.id === convId) {
    // 避免重复（发送方已经本地追加过）
    const exists = messages.value.some(m => m.id === chatMsg.id)
    if (!exists) {
      messages.value.unshift(chatMsg)
      await nextTick()
      scrollToBottom()
      // 自动标记已读
      try { await markAsRead(convId) } catch { /* ignore */ }
    }
  }

  // 更新对话列表中的最后消息和未读数
  const conv = conversations.value.find(c => c.id === convId)
  if (conv) {
    conv.lastMessage = chatMsg.content
    conv.lastMessageAt = chatMsg.createdAt || new Date().toISOString()
    // 如果不是当前正在查看的对话，未读数 +1
    if (!activeConversation.value || activeConversation.value.id !== convId) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
    }
  } else {
    // 新对话未在列表中，重新加载对话列表
    await loadConversations()
  }
}

async function loadConversations() {
  loadingConversations.value = true
  try {
    const res = await getMyConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (e) {
    console.error('加载对话列表失败', e)
    ElMessage.error('加载对话列表失败')
  } finally {
    loadingConversations.value = false
  }
}

async function selectConversation(conv) {
  activeConversation.value = conv
  router.replace(`/chat/${conv.id}`)
  await loadMessages(conv.id)
  // 标记已读
  if (conv.unreadCount > 0) {
    try {
      await markAsRead(conv.id)
      conv.unreadCount = 0
    } catch {
      // 忽略
    }
  }
}

async function loadMessages(conversationId) {
  loadingMessages.value = true
  try {
    const res = await getMessages(conversationId, { page: 0, size: 100 })
    if (res.code === 200 && res.data) {
      messages.value = res.data.content || res.data || []
    }
  } catch (e) {
    console.error('加载消息失败', e)
    ElMessage.error('加载消息失败')
  } finally {
    loadingMessages.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function handleSend() {
  const content = messageInput.value.trim()
  if (!content || !activeConversation.value || sending.value) return

  sending.value = true
  try {
    const res = await sendMessage(activeConversation.value.id, { content, messageType: 'TEXT' })
    if (res.code === 200 || res.code === 201) {
      // 本地追加（要放到 messages 头部，因为倒序存储）
      messages.value.unshift(res.data)
      messageInput.value = ''
      // 更新对话列表中的最后消息
      if (activeConversation.value) {
        activeConversation.value.lastMessage = content
        activeConversation.value.lastMessageAt = new Date().toISOString()
      }
      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

function handleScroll() {
  // 预留：滚动到顶部时加载更多历史消息
}

function previewImage(url) {
  window.open(url, '_blank')
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${m}-${d}`
}

function formatMsgTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${min}`
}

function formatDateLabel(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diff = today - target
  if (diff === 0) return '今天'
  if (diff === 86400000) return '昨天'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  if (y === now.getFullYear()) return `${m}月${d}日`
  return `${y}年${m}月${d}日`
}

function shouldShowDate(idx) {
  if (idx === 0) return true
  const cur = new Date(displayMessages.value[idx].createdAt)
  const prev = new Date(displayMessages.value[idx - 1].createdAt)
  // 超过5分钟显示日期分隔
  return cur - prev > 300000
}

function insertEmoji(emoji) {
  messageInput.value += emoji
}

function goToArtist(userId) {
  if (userId) router.push(`/artist/${userId}`)
}
</script>

<style scoped>
.chat-page {
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  padding: 24px;
}
.chat-container {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  height: calc(100vh - 112px);
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.08), 0 1px 4px rgba(0,0,0,0.04);
}

/* === 左侧对话列表 === */
.conversation-list {
  width: 340px;
  min-width: 340px;
  border-right: 1px solid #f0f1f5;
  display: flex;
  flex-direction: column;
  background: #fff;
}
.list-header {
  padding: 20px 20px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.list-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.list-badge {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 999px;
  line-height: 1.5;
}

/* 搜索对话 */
.list-search {
  padding: 0 16px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
}
.list-search svg {
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #eee;
  border-radius: 999px;
  font-size: 13px;
  outline: none;
  background: #f8f8fa;
  transition: border-color 0.2s, background 0.2s;
}
.search-input:focus {
  border-color: #6366f1;
  background: #fff;
}

.list-loading, .list-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 12px;
}
.empty-hint {
  font-size: 13px;
  color: #bbb;
}
.conversation-items {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}
.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 16px;
  margin-bottom: 2px;
}
.conversation-item:hover { background: #f5f6f7; }
.conversation-item.active {
  background: linear-gradient(135deg, #f0f0ff, #ede9fe);
}
.conv-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  background: #22c55e;
  border: 2px solid #fff;
  border-radius: 50%;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conv-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
}
.conv-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.conv-last-msg {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.conv-unread {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 999px;
  flex-shrink: 0;
  margin-left: 8px;
  min-width: 18px;
  text-align: center;
}

/* === 右侧消息面板 === */
.message-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f8f9ff;
}
.no-conversation {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  gap: 12px;
}
.no-conv-icon {
  width: 88px;
  height: 88px;
  background: #f0f1f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.no-conv-title {
  font-size: 16px;
  color: #999;
  margin: 0;
}
.no-conv-hint {
  font-size: 13px;
  color: #ccc;
  margin: 0;
}

/* 消息头部 */
.msg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f1f5;
  background: #fff;
}
.back-btn {
  display: none;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px;
  border-radius: 999px;
  transition: background 0.2s;
}
.back-btn:hover { background: #f0f0f0; }
.msg-header-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}
.msg-header-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.msg-header-status {
  font-size: 11px;
  color: #22c55e;
  display: flex;
  align-items: center;
  gap: 4px;
}
.msg-header-actions {
  display: flex;
  gap: 4px;
}
.header-action-btn {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: none;
  border: 1px solid #eee;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.header-action-btn:hover {
  background: #f5f5ff;
  border-color: #6366f1;
  color: #6366f1;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.messages-loading {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.no-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 14px;
}

/* 日期分隔线 */
.msg-date-sep {
  text-align: center;
  padding: 12px 0 6px;
}
.msg-date-sep span {
  font-size: 12px;
  color: #bbb;
  background: #f0f1f5;
  padding: 3px 14px;
  border-radius: 999px;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 8px;
  max-width: 72%;
  animation: msgIn 0.25s ease;
}
@keyframes msgIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.message-item.mine {
  flex-direction: row-reverse;
  align-self: flex-end;
}
.message-bubble {
  display: flex;
  flex-direction: column;
}
.msg-content {
  padding: 10px 14px;
  border-radius: 18px;
  background: #fff;
  font-size: 14px;
  line-height: 1.6;
  color: #1a1a1a;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.msg-content p { margin: 0; }
.message-item.mine .msg-content {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}
.message-item:not(.mine) .msg-content {
  border-bottom-left-radius: 4px;
}
.msg-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: 12px;
  cursor: pointer;
  object-fit: cover;
}
.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  padding: 0 4px;
}
.message-item.mine .msg-time {
  text-align: right;
}

/* 输入区域 */
.msg-input-area {
  padding: 10px 20px 16px;
  border-top: 1px solid #f0f1f5;
  background: #fff;
}

/* 快捷表情 */
.emoji-bar {
  display: flex;
  gap: 2px;
  padding-bottom: 8px;
  overflow-x: auto;
}
.emoji-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
  transition: background 0.15s, transform 0.15s;
  line-height: 1;
}
.emoji-btn:hover {
  background: #f0f0f5;
  transform: scale(1.2);
}

.input-row {
  display: flex;
  gap: 8px;
}
.msg-input {
  flex: 1;
  padding: 10px 16px;
  border: 1.5px solid #ebebeb;
  border-radius: 999px;
  font-size: 14px;
  outline: none;
  background: #f8f8fa;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}
.msg-input:focus {
  border-color: #6366f1;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.send-btn {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}
.send-btn:hover {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  transform: scale(1.05);
}
.send-btn:disabled {
  background: #d4d4d8;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

/* Loading spinner */
.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #f0f0f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* === 响应式 === */
@media (max-width: 768px) {
  .chat-page { padding: 0; }
  .chat-container {
    height: 100vh;
    border-radius: 0;
  }
  .conversation-list { width: 100%; min-width: 100%; }
  .mobile-hidden { display: none !important; }
  .message-panel { width: 100%; }
  .back-btn { display: flex; }
  .message-item { max-width: 85%; }
}
</style>
