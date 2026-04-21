<template>
  <!-- 悬浮帮助按钮 -->
  <div class="floating-help">
    <div class="help-btn" @click="togglePanel" :class="{ active: showPanel || showAiChat }">
      <svg v-if="!showAiChat" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/>
        <path d="M11 5h2v6h-2zm0 8h2v2h-2z"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
      </svg>
    </div>

    <!-- 展开面板 -->
    <transition name="help-panel">
      <div v-if="showPanel && !showAiChat" class="help-panel">
        <div class="panel-header">
          <span>帮助与反馈</span>
          <el-icon class="close-icon" @click="showPanel = false"><Close /></el-icon>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-links">
          <div class="quick-item" @click="openAiChat">
            <div class="quick-icon ai">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M21 10.975V8a2 2 0 00-2-2h-6V4.688c.305-.274.5-.668.5-1.11a1.5 1.5 0 00-3 0c0 .442.195.836.5 1.11V6H5a2 2 0 00-2 2v2.998l-.072.005A.999.999 0 002 12v2a1 1 0 001 1v5a2 2 0 002 2h14a2 2 0 002-2v-5a1 1 0 001-1v-2a.999.999 0 00-1-1.025zM9 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM15 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM8 18v-2h8v2H8z"/></svg>
            </div>
            <span>AI 助手</span>
          </div>
          <div class="quick-item" @click="openFeedbackDialog('CONSULTATION')">
            <div class="quick-icon consultation">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>
            </div>
            <span>在线咨询</span>
          </div>
          <div class="quick-item" @click="openFeedbackDialog('BUG_REPORT')">
            <div class="quick-icon bug">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 8h-2.81c-.45-.78-1.07-1.45-1.82-1.96L17 4.41 15.59 3l-2.17 2.17C12.96 5.06 12.49 5 12 5c-.49 0-.96.06-1.41.17L8.41 3 7 4.41l1.62 1.63C7.88 6.55 7.26 7.22 6.81 8H4v2h2.09c-.05.33-.09.66-.09 1v1H4v2h2v1c0 .34.04.67.09 1H4v2h2.81c1.04 1.79 2.97 3 5.19 3s4.15-1.21 5.19-3H20v-2h-2.09c.05-.33.09-.66.09-1v-1h2v-2h-2v-1c0-.34-.04-.67-.09-1H20V8zm-6 8h-4v-2h4v2zm0-4h-4v-2h4v2z"/></svg>
            </div>
            <span>Bug反馈</span>
          </div>
          <div class="quick-item" @click="showHistory = true; showPanel = false">
            <div class="quick-icon history">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            </div>
            <span>反馈记录</span>
          </div>
        </div>

        <div class="panel-footer">
          <p>工作时间：周一至周五 9:00-18:00</p>
          <p>邮箱：support@huanhua.com</p>
        </div>
      </div>
    </transition>

    <!-- AI 对话面板 -->
    <transition name="ai-slide">
      <div v-if="showAiChat" class="ai-chat-panel">
        <!-- 头部 -->
        <div class="ai-chat-header">
          <div class="ai-header-left">
            <div class="ai-avatar">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M21 10.975V8a2 2 0 00-2-2h-6V4.688c.305-.274.5-.668.5-1.11a1.5 1.5 0 00-3 0c0 .442.195.836.5 1.11V6H5a2 2 0 00-2 2v2.998l-.072.005A.999.999 0 002 12v2a1 1 0 001 1v5a2 2 0 002 2h14a2 2 0 002-2v-5a1 1 0 001-1v-2a.999.999 0 00-1-1.025zM9 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM15 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM8 18v-2h8v2H8z"/></svg>
            </div>
            <div class="ai-header-info">
              <span class="ai-name">小幻 · AI 助手</span>
              <span class="ai-status">在线</span>
            </div>
          </div>
          <div class="ai-header-actions">
            <el-tooltip content="会话记录" placement="top">
              <el-icon class="header-action-btn" @click="toggleSessionList">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9z"/></svg>
              </el-icon>
            </el-tooltip>
            <el-tooltip content="新建会话" placement="top">
              <el-icon class="header-action-btn" @click="handleNewSession"><Plus /></el-icon>
            </el-tooltip>
            <el-tooltip content="关闭" placement="top">
              <el-icon class="header-action-btn" @click="showAiChat = false"><Close /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <!-- 会话历史侧栏 -->
        <transition name="session-slide">
          <div v-if="showSessionList" class="session-sidebar">
            <div class="session-sb-header">
              <span class="session-sb-title">会话记录</span>
              <el-icon class="session-sb-close" @click="showSessionList = false"><Close /></el-icon>
            </div>
            <div v-if="sessionLoading" class="session-sb-loading">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
            <div v-else-if="sessionList.length === 0" class="session-sb-empty">暂无历史会话</div>
            <div v-else class="session-sb-list">
              <div v-for="s in sessionList" :key="s.id"
                   class="session-sb-item" :class="{ active: s.id === currentSessionId }"
                   @click="switchSession(s)">
                <div class="session-sb-info">
                  <span class="session-sb-name">{{ s.title || '会话 ' + String(s.id).slice(-4) }}</span>
                  <span class="session-sb-time">{{ formatSessionTime(s.updatedAt || s.createdAt) }}</span>
                </div>
                <el-icon class="session-sb-del" @click="handleDeleteSession(s, $event)"><Close /></el-icon>
              </div>
            </div>
          </div>
        </transition>

        <!-- 消息列表 -->
        <div class="ai-messages" ref="messagesRef">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0 && !loading" class="ai-welcome">
            <div class="welcome-avatar">
              <svg viewBox="0 0 24 24" width="36" height="36" fill="currentColor"><path d="M21 10.975V8a2 2 0 00-2-2h-6V4.688c.305-.274.5-.668.5-1.11a1.5 1.5 0 00-3 0c0 .442.195.836.5 1.11V6H5a2 2 0 00-2 2v2.998l-.072.005A.999.999 0 002 12v2a1 1 0 001 1v5a2 2 0 002 2h14a2 2 0 002-2v-5a1 1 0 001-1v-2a.999.999 0 00-1-1.025zM9 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM15 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM8 18v-2h8v2H8z"/></svg>
            </div>
            <h3>你好！我是小幻 👋</h3>
            <p>幻画空间的 AI 智能助手，有什么可以帮你的吗？</p>
            <div class="welcome-suggestions">
              <button v-for="q in defaultQuestions" :key="q" @click="sendMessage(q)">{{ q }}</button>
            </div>
          </div>

          <!-- 消息气泡 -->
          <div v-for="msg in messages" :key="msg.id"
               :class="['ai-msg', msg.role === 'user' ? 'msg-user' : 'msg-ai']">
            <div v-if="msg.role === 'assistant'" class="msg-avatar">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M21 10.975V8a2 2 0 00-2-2h-6V4.688c.305-.274.5-.668.5-1.11a1.5 1.5 0 00-3 0c0 .442.195.836.5 1.11V6H5a2 2 0 00-2 2v2.998l-.072.005A.999.999 0 002 12v2a1 1 0 001 1v5a2 2 0 002 2h14a2 2 0 002-2v-5a1 1 0 001-1v-2a.999.999 0 00-1-1.025zM9 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM15 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM8 18v-2h8v2H8z"/></svg>
            </div>
            <div class="msg-bubble" :class="{ 'typing-cursor': isTyping && msg === messages[messages.length - 1] && msg.role === 'assistant' }" v-html="renderMarkdown(msg.content)"></div>
          </div>

          <!-- 加载中 -->
          <div v-if="sending" class="ai-msg msg-ai">
            <div class="msg-avatar">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M21 10.975V8a2 2 0 00-2-2h-6V4.688c.305-.274.5-.668.5-1.11a1.5 1.5 0 00-3 0c0 .442.195.836.5 1.11V6H5a2 2 0 00-2 2v2.998l-.072.005A.999.999 0 002 12v2a1 1 0 001 1v5a2 2 0 002 2h14a2 2 0 002-2v-5a1 1 0 001-1v-2a.999.999 0 00-1-1.025zM9 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM15 13.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3zM8 18v-2h8v2H8z"/></svg>
            </div>
            <div class="msg-bubble typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- 推荐问题 -->
        <div class="ai-suggestions" v-if="suggestions.length && !sending">
          <button v-for="s in suggestions" :key="s" @click="sendMessage(s)">{{ s }}</button>
        </div>

        <!-- 输入区域 -->
        <div class="ai-input-bar">
          <input
            v-model="inputText"
            placeholder="有什么可以帮你的？"
            @keyup.enter="sendMessage(inputText)"
            :disabled="sending"
            maxlength="500"
            ref="inputRef"
          />
          <button class="send-btn" @click="sendMessage(inputText)" :disabled="!inputText.trim() || sending">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
          </button>
        </div>
      </div>
    </transition>
  </div>

  <!-- 反馈对话框 -->
  <el-dialog
    v-model="showFeedbackDialog"
    :title="feedbackDialogTitle"
    width="500px"
    :close-on-click-modal="false"
    class="feedback-dialog"
  >
    <el-form :model="feedbackForm" :rules="rules" ref="feedbackFormRef" label-position="top">
      <el-form-item label="反馈类型" prop="type">
        <el-select v-model="feedbackForm.type" style="width: 100%">
          <el-option label="在线咨询" value="CONSULTATION" />
          <el-option label="Bug反馈" value="BUG_REPORT" />
          <el-option label="功能建议" value="FEATURE_REQUEST" />
          <el-option label="投诉" value="COMPLAINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="feedbackForm.title" placeholder="请简要描述您的问题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="详细描述" prop="content">
        <el-input
          v-model="feedbackForm.content"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您遇到的问题或建议..."
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="联系方式（选填）">
        <el-input v-model="feedbackForm.contactInfo" placeholder="邮箱或手机号，方便我们联系您" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showFeedbackDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">提交反馈</el-button>
    </template>
  </el-dialog>

  <!-- 反馈记录抽屉 -->
  <el-drawer v-model="showHistory" title="我的反馈记录" size="420px" direction="rtl">
    <div class="feedback-history">
      <div v-if="historyLoading" class="loading-placeholder">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="feedbackList.length === 0" class="empty-state">
        <el-empty description="暂无反馈记录" />
      </div>
      <div v-else class="feedback-list">
        <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
          <div class="feedback-head">
            <el-tag :type="typeTagMap[item.type]?.type || 'info'" size="small" effect="plain">
              {{ typeTagMap[item.type]?.label || item.type }}
            </el-tag>
            <el-tag :type="statusTagMap[item.status]?.type || 'info'" size="small">
              {{ statusTagMap[item.status]?.label || item.status }}
            </el-tag>
          </div>
          <h4 class="feedback-title">{{ item.title }}</h4>
          <p class="feedback-content">{{ item.content }}</p>
          <div class="feedback-time">{{ formatTime(item.createdAt) }}</div>
          <div v-if="item.adminReply" class="admin-reply">
            <div class="reply-label">客服回复：</div>
            <div class="reply-content">{{ item.adminReply }}</div>
            <div class="reply-time">{{ formatTime(item.repliedAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import { Close, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { submitFeedback, getMyFeedbacks } from '@/api/feedback'
import { createAiChatSession, sendAiChatMessage, getAiChatMessages, getAiChatSessions, deleteAiChatSession } from '@/api/aiChat'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const showPanel = ref(false)
const showAiChat = ref(false)
const showFeedbackDialog = ref(false)
const showHistory = ref(false)
const submitting = ref(false)
const historyLoading = ref(false)
const feedbackList = ref([])
const feedbackFormRef = ref(null)

// AI 对话状态
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const loading = ref(false)
const suggestions = ref([])
const currentSessionId = ref(null)
const messagesRef = ref(null)
const inputRef = ref(null)
const showSessionList = ref(false)
const sessionList = ref([])
const sessionLoading = ref(false)
const typingText = ref('')
const isTyping = ref(false)

const defaultQuestions = [
  '平台有哪些功能？',
  '如何发起约稿？',
  '如何成为画师？',
  '以图搜图怎么用？'
]

const feedbackForm = ref({
  type: 'CONSULTATION',
  title: '',
  content: '',
  contactInfo: ''
})

const feedbackDialogTitle = ref('提交反馈')

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const typeTagMap = {
  AFTER_SALE: { label: '售后', type: 'warning' },
  CONSULTATION: { label: '咨询', type: '' },
  BUG_REPORT: { label: 'Bug', type: 'danger' },
  FEATURE_REQUEST: { label: '建议', type: 'success' },
  COMPLAINT: { label: '投诉', type: 'warning' },
  OTHER: { label: '其他', type: 'info' }
}

const statusTagMap = {
  PENDING: { label: '待处理', type: 'info' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: 'success' },
  CLOSED: { label: '已关闭', type: '' }
}

const togglePanel = () => {
  if (showAiChat.value) {
    showAiChat.value = false
    return
  }
  showPanel.value = !showPanel.value
}

// ========== AI 对话 ==========

const openAiChat = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后使用 AI 助手')
    return
  }
  showPanel.value = false
  showAiChat.value = true
  
  // 如果没有当前会话，创建一个
  if (!currentSessionId.value) {
    await createNewSession()
  }
  
  nextTick(() => {
    if (inputRef.value) inputRef.value.focus()
  })
}

const createNewSession = async () => {
  try {
    loading.value = true
    const res = await createAiChatSession()
    if (res.code === 200 && res.data) {
      currentSessionId.value = res.data.id
      messages.value = []
      suggestions.value = []
    }
  } catch (e) {
    console.error('创建会话失败:', e)
  } finally {
    loading.value = false
  }
}

const handleNewSession = async () => {
  currentSessionId.value = null
  messages.value = []
  suggestions.value = []
  showSessionList.value = false
  await createNewSession()
  nextTick(() => {
    if (inputRef.value) inputRef.value.focus()
  })
}

// 打字机效果
const typewriterEffect = (text, msgIndex) => {
  return new Promise((resolve) => {
    isTyping.value = true
    let i = 0
    const speed = 18 // 每字符间隔(ms)
    const tick = () => {
      if (i < text.length) {
        messages.value[msgIndex].content = text.slice(0, i + 1)
        i++
        scrollToBottom()
        setTimeout(tick, speed)
      } else {
        isTyping.value = false
        resolve()
      }
    }
    tick()
  })
}

const sendMessage = async (text) => {
  if (!text || !text.trim() || sending.value) return
  
  const msg = text.trim()
  inputText.value = ''
  suggestions.value = []
  
  // 如果没有会话，先创建
  if (!currentSessionId.value) {
    await createNewSession()
  }
  
  // 添加用户消息到界面
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: msg,
    createdAt: new Date().toISOString()
  })
  
  scrollToBottom()
  sending.value = true
  
  try {
    const res = await sendAiChatMessage(currentSessionId.value, msg)
    if (res.code === 200 && res.data) {
      const aiMessage = res.data.message
      if (aiMessage) {
        // 先添加空消息，再用打字机效果逐字显示
        const msgObj = {
          id: aiMessage.id,
          role: 'assistant',
          content: '',
          createdAt: aiMessage.createdAt
        }
        messages.value.push(msgObj)
        sending.value = false
        const msgIdx = messages.value.length - 1
        await typewriterEffect(aiMessage.content, msgIdx)
      }
      if (res.data.suggestions) {
        suggestions.value = res.data.suggestions
      }
    } else {
      sending.value = false
      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: '抱歉，我暂时无法回复，请稍后再试 😊',
        createdAt: new Date().toISOString()
      })
    }
  } catch (e) {
    console.error('发送消息失败:', e)
    sending.value = false
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: '网络异常，请稍后重试 😅',
      createdAt: new Date().toISOString()
    })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

// ========== 会话历史 ==========

const toggleSessionList = async () => {
  showSessionList.value = !showSessionList.value
  if (showSessionList.value) {
    await loadSessions()
  }
}

const loadSessions = async () => {
  sessionLoading.value = true
  try {
    const res = await getAiChatSessions(0, 20)
    if (res.code === 200 && res.data) {
      sessionList.value = (res.data.records || res.data.content || [])
    }
  } catch (e) {
    console.error('加载会话列表失败:', e)
  } finally {
    sessionLoading.value = false
  }
}

const switchSession = async (session) => {
  currentSessionId.value = session.id
  showSessionList.value = false
  messages.value = []
  suggestions.value = []
  loading.value = true
  try {
    const res = await getAiChatMessages(session.id)
    if (res.code === 200 && res.data) {
      messages.value = (res.data || []).map(m => ({
        id: m.id,
        role: m.role === 'USER' ? 'user' : 'assistant',
        content: m.content,
        createdAt: m.createdAt
      }))
    }
  } catch (e) {
    console.error('加载消息失败:', e)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const handleDeleteSession = async (session, e) => {
  e.stopPropagation()
  try {
    const res = await deleteAiChatSession(session.id)
    if (res.code === 200) {
      sessionList.value = sessionList.value.filter(s => s.id !== session.id)
      if (currentSessionId.value === session.id) {
        currentSessionId.value = null
        messages.value = []
        suggestions.value = []
        await createNewSession()
      }
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const formatSessionTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 代码块 (```)
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="md-code-block"><code>$2</code></pre>')
    // 加粗
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    // 行内代码
    .replace(/`(.*?)`/g, '<code class="md-inline-code">$1</code>')
    // 有序列表
    .replace(/^\d+\.\s+(.+)/gm, '<li class="md-ol">$1</li>')
    // 无序列表
    .replace(/^[-•]\s+(.+)/gm, '<li class="md-ul">$1</li>')
    // 标题
    .replace(/^###\s+(.+)/gm, '<h4 class="md-h4">$1</h4>')
    .replace(/^##\s+(.+)/gm, '<h3 class="md-h3">$1</h3>')
    // 分隔线
    .replace(/^---$/gm, '<hr class="md-hr">')
    // 换行
    .replace(/\n/g, '<br>')
}

// ========== 反馈功能 ==========

const openFeedbackDialog = (type) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再提交反馈')
    return
  }
  feedbackForm.value = { type, title: '', content: '', contactInfo: '' }
  const titles = {
    CONSULTATION: '在线咨询',
    BUG_REPORT: 'Bug反馈',
    FEATURE_REQUEST: '功能建议',
    COMPLAINT: '投诉',
    OTHER: '其他反馈'
  }
  feedbackDialogTitle.value = titles[type] || '提交反馈'
  showPanel.value = false
  showFeedbackDialog.value = true
}

const handleSubmit = async () => {
  if (!feedbackFormRef.value) return
  try {
    await feedbackFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await submitFeedback(feedbackForm.value)
    if (res.code === 200) {
      ElMessage.success('反馈提交成功，我们会尽快处理')
      showFeedbackDialog.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadHistory = async () => {
  if (!userStore.isAuthenticated) return
  historyLoading.value = true
  try {
    const res = await getMyFeedbacks({ page: 0, size: 20 })
    if (res.code === 200) {
      feedbackList.value = res.data?.records || res.data?.content || []
    }
  } catch {
    feedbackList.value = []
  } finally {
    historyLoading.value = false
  }
}

watch(showHistory, (val) => {
  if (val) loadHistory()
})

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.floating-help {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 2000;
}

.help-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.help-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.help-btn.active {
  background: linear-gradient(135deg, #764ba2, #667eea);
}

/* 展开面板 */
.help-panel {
  position: absolute;
  bottom: 64px;
  right: 0;
  width: 280px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.close-icon {
  cursor: pointer;
  font-size: 16px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.close-icon:hover {
  opacity: 1;
}

.quick-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  padding: 16px 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
  color: #333;
}

.quick-item:hover {
  background: #f5f7fa;
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-icon.ai {
  background: linear-gradient(135deg, #e8eaf6, #e0d6f6);
  color: #667eea;
}

.quick-icon.consultation {
  background: #e6f4ff;
  color: #0096fa;
}

.quick-icon.bug {
  background: #fff1f0;
  color: #f56c6c;
}

.quick-icon.history {
  background: #fdf6ec;
  color: #e6a23c;
}

.panel-footer {
  padding: 12px 18px;
  background: #f9fafb;
  border-top: 1px solid #f0f0f0;
}

.panel-footer p {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.8;
}

/* 面板动画 */
.help-panel-enter-active,
.help-panel-leave-active {
  transition: all 0.25s ease;
}

.help-panel-enter-from,
.help-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.95);
}

/* ========== AI 对话面板 ========== */
.ai-chat-panel {
  position: absolute;
  bottom: 64px;
  right: 0;
  width: 380px;
  height: 540px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  flex-shrink: 0;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-header-info {
  display: flex;
  flex-direction: column;
}

.ai-name {
  font-size: 14px;
  font-weight: 600;
}

.ai-status {
  font-size: 11px;
  opacity: 0.85;
}

.ai-header-actions {
  display: flex;
  gap: 8px;
}

.header-action-btn {
  cursor: pointer;
  font-size: 18px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.header-action-btn:hover {
  opacity: 1;
}

/* 消息列表 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #f8f9fc;
}

.ai-messages::-webkit-scrollbar {
  width: 4px;
}

.ai-messages::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 4px;
}

/* 欢迎 */
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 10px;
  text-align: center;
}

.welcome-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.ai-welcome h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #333;
}

.ai-welcome p {
  margin: 0 0 18px;
  font-size: 13px;
  color: #888;
}

.welcome-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.welcome-suggestions button {
  padding: 7px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  background: #fff;
  color: #555;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.welcome-suggestions button:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f0f0ff;
}

/* 消息气泡 */
.ai-msg {
  display: flex;
  gap: 8px;
  max-width: 85%;
}

.ai-msg.msg-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-msg.msg-ai {
  align-self: flex-start;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-user .msg-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-ai .msg-bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.msg-bubble :deep(code) {
  background: rgba(0,0,0,0.06);
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 12px;
}

.msg-bubble :deep(.md-inline-code) {
  background: rgba(0,0,0,0.06);
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 12px;
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', monospace;
}

.msg-bubble :deep(.md-code-block) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 10px 12px;
  border-radius: 8px;
  margin: 8px 0;
  font-size: 12px;
  overflow-x: auto;
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', monospace;
  line-height: 1.5;
}

.msg-bubble :deep(.md-code-block code) {
  background: none;
  padding: 0;
  color: inherit;
}

.msg-bubble :deep(.md-h3),
.msg-bubble :deep(.md-h4) {
  margin: 8px 0 4px;
  font-weight: 600;
}

.msg-bubble :deep(.md-h3) { font-size: 14px; }
.msg-bubble :deep(.md-h4) { font-size: 13px; }

.msg-bubble :deep(.md-ol),
.msg-bubble :deep(.md-ul) {
  margin-left: 8px;
  padding-left: 12px;
  list-style: disc;
  line-height: 1.8;
}

.msg-bubble :deep(.md-ol) {
  list-style: decimal;
}

.msg-bubble :deep(.md-hr) {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 8px 0;
}

.msg-bubble :deep(strong) {
  font-weight: 600;
}

/* 打字动画 */
.msg-bubble.typing {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

/* 推荐问题 */
.ai-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
}

.ai-suggestions button {
  padding: 5px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 14px;
  background: #fafafa;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.ai-suggestions button:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f0f0ff;
}

/* 输入区域 */
.ai-input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
}

.ai-input-bar input {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  padding: 9px 16px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.ai-input-bar input:focus {
  border-color: #667eea;
}

.ai-input-bar input::placeholder {
  color: #bbb;
}

.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* AI 面板动画 */
.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: all 0.3s ease;
}

.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 反馈对话框 */
:deep(.feedback-dialog .el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.feedback-dialog .el-dialog__body) {
  padding: 20px;
}

/* 反馈历史记录 */
.feedback-history {
  padding: 0 4px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  padding: 16px;
  border-radius: 10px;
  background: #fafbfc;
  border: 1px solid #eef0f4;
}

.feedback-head {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.feedback-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.feedback-content {
  margin: 0 0 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feedback-time {
  font-size: 12px;
  color: #aaa;
}

.admin-reply {
  margin-top: 12px;
  padding: 12px;
  background: #e6f4ff;
  border-radius: 8px;
  border-left: 3px solid #0096fa;
}

.reply-label {
  font-size: 12px;
  color: #0078d4;
  font-weight: 600;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: #333;
  line-height: 1.6;
}

.reply-time {
  font-size: 12px;
  color: #aaa;
  margin-top: 6px;
}

.loading-placeholder {
  padding: 16px;
}

.empty-state {
  padding: 40px 0;
}

/* ========== 会话历史侧栏 ========== */
.session-sidebar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  z-index: 10;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
}

.session-sb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  flex-shrink: 0;
}

.session-sb-title {
  font-size: 14px;
  font-weight: 600;
}

.session-sb-close {
  cursor: pointer;
  font-size: 16px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.session-sb-close:hover {
  opacity: 1;
}

.session-sb-loading {
  display: flex;
  gap: 4px;
  justify-content: center;
  padding: 40px;
}

.session-sb-loading .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite;
}

.session-sb-loading .dot:nth-child(2) { animation-delay: 0.2s; }
.session-sb-loading .dot:nth-child(3) { animation-delay: 0.4s; }

.session-sb-empty {
  padding: 60px 20px;
  text-align: center;
  color: #aaa;
  font-size: 13px;
}

.session-sb-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-sb-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.session-sb-item:hover {
  background: #f5f7fa;
}

.session-sb-item.active {
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}

.session-sb-info {
  flex: 1;
  min-width: 0;
}

.session-sb-name {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-sb-time {
  font-size: 11px;
  color: #aaa;
  margin-top: 2px;
  display: block;
}

.session-sb-del {
  font-size: 14px;
  color: #ccc;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  flex-shrink: 0;
  margin-left: 8px;
}

.session-sb-item:hover .session-sb-del {
  opacity: 1;
}

.session-sb-del:hover {
  color: #f56c6c;
}

.session-slide-enter-active,
.session-slide-leave-active {
  transition: all 0.25s ease;
}

.session-slide-enter-from {
  opacity: 0;
  transform: translateX(-100%);
}

.session-slide-leave-to {
  opacity: 0;
  transform: translateX(-100%);
}

/* 打字机光标 */
.msg-bubble.typing-cursor::after {
  content: '▋';
  animation: blink-cursor 0.8s step-end infinite;
  color: #667eea;
  font-size: 14px;
}

@keyframes blink-cursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
