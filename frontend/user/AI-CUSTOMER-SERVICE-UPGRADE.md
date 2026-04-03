# AI 智能客服升级说明文档

## 一、升级概览

本次对 AI 智能客服（`FloatingHelpButton.vue`）进行了全面升级，涵盖**交互体验**、**会话管理**和**内容渲染**三个核心维度。

### 升级前 vs 升级后

| 维度 | 升级前 | 升级后 |
|------|--------|--------|
| AI 回复方式 | 一次性整段输出 | **打字机逐字输出**（18ms/字） |
| 会话历史 | 无法查看历史会话 | **侧边栏会话管理**（切换 / 删除） |
| 内容渲染 | 纯文本 | **增强 Markdown**（代码块、标题、列表、分隔线） |
| 输入状态 | 无动画反馈 | **打字光标闪烁动画** |

---

## 二、功能详解

### 2.1 打字机效果（Typewriter Effect）

**文件位置：** `FloatingHelpButton.vue` — `typewriterEffect()` 函数

**实现原理：**
- AI 回复到达后，先在消息列表中创建一条空的 AI 消息
- 通过 `typewriterEffect(text, msgIndex)` 逐字追加到该消息的 `content` 字段
- 每个字符间隔 18ms，通过 `Promise` + `setTimeout` 实现
- 输出过程中设置 `isTyping = true`，在消息气泡末尾显示闪烁光标
- 输出完成后 `isTyping = false`，光标消失

**关键代码逻辑：**
```javascript
async function typewriterEffect(text, msgIndex) {
  isTyping.value = true
  typingText.value = ''
  for (let i = 0; i < text.length; i++) {
    typingText.value += text[i]
    chatMessages.value[msgIndex].content = typingText.value
    await new Promise(r => setTimeout(r, 18))
  }
  isTyping.value = false
}
```

**用户体验：** 模拟真人打字的渐进式输出，避免大段文字突然出现的割裂感。

---

### 2.2 会话历史管理

**文件位置：** `FloatingHelpButton.vue` — 会话侧边栏相关函数

**功能说明：**

| 操作 | 入口 | API |
|------|------|-----|
| 查看历史会话列表 | 聊天面板顶部时钟图标 | `getAiChatSessions()` |
| 切换到历史会话 | 点击某条会话 | `getAiChatHistory(sessionId)` |
| 删除历史会话 | 会话条目右侧删除按钮 | `deleteAiChatSession(sessionId)` |
| 新建会话 | 侧边栏底部 "+ 新对话" 按钮 | 前端重置状态 |

**侧边栏 UI：**
- 从左侧滑入，半透明黑色遮罩背景
- 每条会话显示标题（取首条用户消息前 20 字）和相对时间
- 当前激活会话高亮标记
- 支持「刚刚 / X分钟前 / X小时前 / X天前」相对时间格式

---

### 2.3 增强 Markdown 渲染

**文件位置：** `FloatingHelpButton.vue` — `renderMarkdown()` 函数

**支持的 Markdown 语法：**

| 语法 | 渲染效果 | CSS 类名 |
|------|----------|----------|
| ` ```code``` ` | 代码块（深色背景 + 等宽字体） | `.md-code-block` |
| `` `inline` `` | 行内代码 | `.md-inline-code` |
| `## 标题` | 二级标题 | `.md-h3` |
| `### 标题` | 三级标题 | `.md-h4` |
| `1. 有序列表` | 带数字的列表 | `.md-ol` |
| `- 无序列表` | 带圆点的列表 | `.md-ul` |
| `---` | 水平分隔线 | `.md-hr` |
| `**粗体**` | 粗体文字 | `<strong>` |

**渲染管线（按优先级）：**
1. 先提取代码块并替换为占位符（避免内部语法被误处理）
2. 处理行内代码
3. 处理标题（h2 / h3）
4. 处理列表
5. 处理粗体
6. 处理分隔线和换行
7. 还原代码块占位符

---

## 三、技术架构

```
┌──────────────────────────────────────────┐
│            FloatingHelpButton.vue         │
│                                          │
│  ┌──────────┐  ┌──────────────────────┐  │
│  │ 会话侧栏  │  │    聊天主面板         │  │
│  │           │  │                      │  │
│  │ - 历史列表│  │ 消息列表              │  │
│  │ - 切换/删除│ │ ├─ 用户消息(右)      │  │
│  │ - 新建对话│  │ ├─ AI消息(左)        │  │
│  │           │  │ │  └─ 打字机效果     │  │
│  │           │  │ │  └─ Markdown渲染   │  │
│  │           │  │ └─ 打字光标动画      │  │
│  └──────────┘  │                      │  │
│                │ 输入框 + 发送按钮     │  │
│                └──────────────────────┘  │
└──────────────────────────────────────────┘
          │                    │
          ▼                    ▼
   ┌─────────────┐   ┌─────────────────┐
   │ AI Chat API  │   │  Gateway :8080   │
   │              │   │                  │
   │ Sessions列表 │   │ /api/ai-chat/** │
   │ History查询  │   │       │          │
   │ Session删除  │   │       ▼          │
   └─────────────┘   │ notification-svc │
                      │   AiChatService  │
                      │       │          │
                      │       ▼          │
                      │  ai-service:8000 │
                      │  (Python FastAPI)│
                      └─────────────────┘
```

---

## 四、新增状态变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `showSessionList` | `ref(false)` | 控制会话侧边栏显示/隐藏 |
| `sessionList` | `ref([])` | 历史会话列表数据 |
| `sessionLoading` | `ref(false)` | 会话列表加载状态 |
| `typingText` | `ref('')` | 打字机效果的累积文字 |
| `isTyping` | `ref(false)` | 是否正在执行打字机效果 |

---

## 五、新增 / 修改函数

| 函数 | 类型 | 说明 |
|------|------|------|
| `typewriterEffect(text, msgIndex)` | 新增 | 逐字输出AI回复内容 |
| `toggleSessionList()` | 新增 | 切换会话侧边栏显示 |
| `loadSessions()` | 新增 | 加载历史会话列表 |
| `switchSession(session)` | 新增 | 切换到指定历史会话 |
| `handleDeleteSession(session, index)` | 新增 | 删除指定会话 |
| `formatSessionTime(time)` | 新增 | 格式化会话时间为相对时间 |
| `renderMarkdown(text)` | 修改 | 增强：支持代码块、标题、列表等 |
| `sendMessage()` | 修改 | 集成打字机效果 |

---

## 六、调用的 API 接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `sendAiChatMessage` | POST | `/api/ai-chat/send` | 发送消息并获取 AI 回复 |
| `getAiChatHistory` | GET | `/api/ai-chat/history` | 获取指定会话的聊天记录 |
| `getAiChatSessions` | GET | `/api/ai-chat/sessions` | 获取所有会话列表 |
| `deleteAiChatSession` | DELETE | `/api/ai-chat/sessions/{id}` | 删除指定会话 |

---

## 七、CSS 新增样式

### 打字光标动画
```css
.typing-cursor::after {
  content: '▌';
  animation: blink 0.8s infinite;
  color: #999;
  font-weight: 300;
  margin-left: 1px;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
```

### 会话侧边栏
- `.session-sidebar` — 全屏遮罩 + 左侧 280px 滑入面板
- `.session-item` — 会话条目，hover 高亮，active 标记
- `slide-left-enter/leave-active` — 300ms ease 滑入滑出过渡

### Markdown 深度样式
- `.md-code-block` — 暗色代码块（#1e1e1e 背景）
- `.md-inline-code` — 行内代码（粉色背景）
- `.md-h3 / .md-h4` — 标题（带底部边框）
- `.md-ol / .md-ul` — 列表（左 padding 缩进）
- `.md-hr` — 分隔线

---

## 八、已知限制与后续规划

| 项目 | 现状 | 计划 |
|------|------|------|
| 真实 SSE 流式传输 | 后端仅支持同步调用，前端用打字机模拟 | 需 Java 层新增 SSE 代理端点 |
| 图片发送 | 不支持 | 需后端支持多模态消息 |
| 消息搜索 | 不支持 | 可在会话历史侧栏增加搜索功能 |
| 语音输入 | 不支持 | 可集成 Web Speech API |

---

## 九、变更文件清单

| 文件 | 变更类型 |
|------|----------|
| `src/components/FloatingHelpButton.vue` | 重构（模板 + 脚本 + 样式） |
| `src/api/aiChat.js` | 无变更（已有所需接口） |
