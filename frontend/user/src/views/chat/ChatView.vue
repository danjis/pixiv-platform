<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧: 对话列表 -->
      <div class="conversation-list" :class="{ 'mobile-hidden': activeConversation }">
        <div class="list-header">
          <h2 class="list-title">私信</h2>
        </div>

        <div v-if="loadingConversations" class="list-loading">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="conversations.length === 0" class="list-empty">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="#ccc"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
          <p>暂无私信</p>
          <span class="empty-hint">在作品详情页点击"私信"开始对话</span>
        </div>

        <div v-else class="conversation-items">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: activeConversation?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <el-avatar :size="44" :src="conv.otherUserAvatar || defaultAvatar">
              {{ conv.otherUserName?.charAt(0) }}
            </el-avatar>
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
          <svg viewBox="0 0 24 24" width="64" height="64" fill="#ddd"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
          <p>选择一个对话开始聊天</p>
        </div>

        <!-- 消息区域 -->
        <template v-else>
          <!-- 消息头部 -->
          <div class="msg-header">
            <button class="back-btn" @click="activeConversation = null">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
            </button>
            <el-avatar :size="36" :src="activeConversation.otherUserAvatar || defaultAvatar">
              {{ activeConversation.otherUserName?.charAt(0) }}
            </el-avatar>
            <span class="msg-header-name">{{ activeConversation.otherUserName }}</span>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list" @scroll="handleScroll">
            <div v-if="loadingMessages" class="messages-loading">
              <div class="loading-spinner"></div>
            </div>
            <div v-for="msg in displayMessages" :key="msg.id" class="message-item" :class="{ mine: msg.senderId === currentUserId }">
              <el-avatar v-if="msg.senderId !== currentUserId" :size="32" :src="msg.senderAvatar || defaultAvatar">
                {{ msg.senderName?.charAt(0) }}
              </el-avatar>
              <div class="message-bubble">
                <span v-if="msg.senderId !== currentUserId" class="msg-sender">{{ msg.senderName }}</span>
                <div class="msg-content">
                  <img v-if="msg.messageType === 'IMAGE' && msg.attachmentUrl" :src="msg.attachmentUrl" class="msg-image" @click="previewImage(msg.attachmentUrl)" />
                  <p v-else>{{ msg.content }}</p>
                </div>
                <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
            </div>
            <div v-if="!loadingMessages && displayMessages.length === 0" class="no-messages">
              <p>发送第一条消息吧 👋</p>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="msg-input-area">
            <div class="input-row">
              <input
                v-model="messageInput"
                class="msg-input"
                placeholder="输入消息..."
                @keydown.enter="handleSend"
                :disabled="sending"
                maxlength="2000"
              />
              <button class="send-btn" @click="handleSend" :disabled="!messageInput.trim() || sending">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
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

// 消息
const messages = ref([])
const loadingMessages = ref(false)
const messageInput = ref('')
const sending = ref(false)
const messageListRef = ref(null)

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
</script>

<style scoped>
.chat-page {
  min-height: calc(100vh - 64px);
  background: #f5f5f5;
  padding: 24px;
}
.chat-container {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  height: calc(100vh - 112px);
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* === 左侧对话列表 === */
.conversation-list {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}
.list-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.list-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
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
}
.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #fafafa;
}
.conversation-item:hover { background: #f8f9fa; }
.conversation-item.active { background: #e8f4ff; }
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
  background: #0096FA;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 10px;
  flex-shrink: 0;
  margin-left: 8px;
}

/* === 右侧消息面板 === */
.message-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.no-conversation {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  gap: 16px;
  font-size: 15px;
}

/* 消息头部 */
.msg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}
.back-btn {
  display: none;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px;
}
.msg-header-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
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

/* 消息项 */
.message-item {
  display: flex;
  gap: 8px;
  max-width: 75%;
}
.message-item.mine {
  flex-direction: row-reverse;
  align-self: flex-end;
}
.message-bubble {
  display: flex;
  flex-direction: column;
}
.msg-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.msg-content {
  padding: 10px 14px;
  border-radius: 12px;
  background: #f0f0f0;
  font-size: 14px;
  line-height: 1.5;
  color: #1a1a1a;
  word-break: break-word;
}
.msg-content p { margin: 0; }
.message-item.mine .msg-content {
  background: #0096FA;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message-item:not(.mine) .msg-content {
  border-bottom-left-radius: 4px;
}
.msg-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
}
.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}
.message-item.mine .msg-time {
  text-align: right;
}

/* 输入区域 */
.msg-input-area {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}
.input-row {
  display: flex;
  gap: 8px;
}
.msg-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.msg-input:focus {
  border-color: #0096FA;
}
.send-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #0096FA;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  flex-shrink: 0;
}
.send-btn:hover { background: #0080dd; }
.send-btn:disabled { background: #ccc; cursor: not-allowed; }

/* Loading spinner */
.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #f0f0f0;
  border-top-color: #0096FA;
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
}
</style>
