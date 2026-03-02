<template>
  <div class="notification-page">
    <div class="notification-container">
      <!-- 页头 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">通知</h1>
          <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }} 条未读</span>
        </div>
        <div class="header-actions">
          <button class="action-btn" @click="handleMarkAllRead" :disabled="unreadCount === 0" title="全部已读">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M18 7l-1.41-1.41-6.34 6.34 1.41 1.41L18 7zm4.24-1.41L11.66 16.17 7.48 12l-1.41 1.41L11.66 19l12-12-1.42-1.41zM.41 13.41L6 19l1.41-1.41L1.83 12 .41 13.41z"/></svg>
            全部已读
          </button>
          <button class="action-btn" @click="loadNotifications" title="刷新">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
          </button>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="filter-bar">
        <button
          v-for="f in filters"
          :key="f.value"
          class="filter-btn"
          :class="{ active: filterType === f.value }"
          @click="filterType = f.value; handleFilterChange()"
        >
          {{ f.label }}
        </button>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <!-- 通知列表 -->
      <div v-else-if="notifications.length > 0" class="notif-list">
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notif-item"
          :class="{ unread: !n.isRead }"
          @click="handleNotificationClick(n)"
        >
          <!-- 类型图标 -->
          <div class="notif-icon" :class="'type-' + n.type.toLowerCase()">
            <svg v-if="n.type === 'LIKE'" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            <svg v-else-if="n.type === 'COMMENT'" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z"/></svg>
            <svg v-else-if="n.type === 'FOLLOW'" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
            <svg v-else-if="n.type === 'COMMISSION'" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z"/></svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
          </div>

          <!-- 内容 -->
          <div class="notif-body">
            <div class="notif-text">
              <span v-if="!n.isRead" class="dot"></span>
              {{ n.content }}
            </div>
            <div class="notif-meta">
              <span class="notif-time">{{ formatTime(n.createdAt) }}</span>
              <span class="notif-type-tag" :class="'tag-' + n.type.toLowerCase()">{{ getTypeText(n.type) }}</span>
            </div>
          </div>

          <!-- 操作 -->
          <div class="notif-actions" @click.stop>
            <button v-if="!n.isRead" class="mini-btn read-btn" @click="handleMarkRead(n.id)" title="标记已读">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
            </button>
            <button class="mini-btn del-btn" @click="handleDelete(n.id)" title="删除">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 空 -->
      <div v-else class="empty-state">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none" stroke="#ddd" stroke-width="1.5">
          <path d="M32 8C20 8 12 16 12 26v10l-4 4v2h48v-2l-4-4V26C52 16 44 8 32 8z"/>
          <circle cx="32" cy="52" r="4"/>
        </svg>
        <p>{{ getEmptyDescription() }}</p>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          small
          @current-change="loadNotifications"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '@/api/notification'

const router = useRouter()

const loading = ref(true)
const notifications = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCount = ref(0)
const filterType = ref('all')

const filters = [
  { label: '全部', value: 'all' },
  { label: '未读', value: 'unread' },
  { label: '已读', value: 'read' }
]

async function loadNotifications() {
  loading.value = true
  try {
    const res = await getNotifications({ page: page.value, size: pageSize.value })
    if (res.code === 200) {
      let items = res.data?.records || []  // 修复：使用 records 而不是 items
      if (filterType.value === 'unread') items = items.filter(n => !n.isRead)
      else if (filterType.value === 'read') items = items.filter(n => n.isRead)
      notifications.value = items
      total.value = res.data?.total || 0
    }
  } catch { ElMessage.error('加载通知失败') }
  finally { loading.value = false }
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) unreadCount.value = res.data?.count || 0
  } catch {}
}

async function handleNotificationClick(n) {
  // 先标记为已读
  if (!n.isRead) await handleMarkRead(n.id)
  
  // 如果有链接，跳转
  if (n.linkUrl) {
    try {
      await router.push(n.linkUrl)
    } catch (error) {
      // 如果跳转失败（比如作品不存在），显示提示
      console.error('跳转失败:', error)
      ElMessage.warning('该内容可能已被删除或不可访问')
    }
  }
}

async function handleMarkRead(id) {
  try {
    const res = await markAsRead(id)
    if (res.code === 200) {
      const n = notifications.value.find(x => x.id === id)
      if (n) n.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch { ElMessage.error('操作失败') }
}

async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确定将所有通知标记为已读？', '确认')
    const res = await markAllAsRead()
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条通知？', '确认', { type: 'warning' })
    const res = await deleteNotification(id)
    if (res.code === 200) { ElMessage.success('已删除'); loadNotifications(); loadUnreadCount() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

function handleFilterChange() {
  page.value = 1
  loadNotifications()
}

function getTypeText(type) {
  return { COMMISSION: '约稿', APPLICATION: '申请', FOLLOW: '关注', LIKE: '点赞', COMMENT: '评论', SYSTEM: '系统' }[type] || type
}

function formatTime(d) {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  
  // 超过 7 天显示具体日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  // 如果是今年，不显示年份
  if (year === now.getFullYear()) {
    return `${month}-${day}`
  }
  return `${year}-${month}-${day}`
}

function getEmptyDescription() {
  if (filterType.value === 'unread') return '没有未读通知'
  if (filterType.value === 'read') return '没有已读通知'
  return '暂无通知'
}

onMounted(() => { loadNotifications(); loadUnreadCount() })
</script>

<style scoped>
.notification-page {
  min-height: calc(100vh - 56px);
  background: #f2f4f5;
  padding: 32px 24px;
}
.notification-container {
  max-width: 760px;
  margin: 0 auto;
}

/* 页头 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0; }
.unread-badge {
  font-size: 12px; font-weight: 600; color: #fff;
  background: #0096FA; padding: 2px 10px; border-radius: 12px;
}
.header-actions { display: flex; gap: 8px; }
.action-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border: none; background: #fff;
  border-radius: 8px; font-size: 13px; color: #666;
  cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s;
}
.action-btn:hover { color: #0096FA; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 筛选 */
.filter-bar {
  display: flex; gap: 4px;
  background: #fff; border-radius: 10px; padding: 4px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.filter-btn {
  flex: 1; padding: 8px 16px; border: none; background: transparent;
  border-radius: 8px; font-size: 13px; font-weight: 500;
  color: #666; cursor: pointer; transition: all 0.2s;
}
.filter-btn:hover { color: #333; }
.filter-btn.active {
  background: #0096FA; color: #fff;
  box-shadow: 0 2px 8px rgba(0,150,250,0.3);
}

/* 加载 */
.loading-state { display: flex; justify-content: center; padding: 60px 0; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 列表 */
.notif-list { display: flex; flex-direction: column; gap: 6px; }
.notif-item {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 12px; padding: 16px 18px;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.notif-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); transform: translateY(-1px); }
.notif-item.unread { border-left: 3px solid #0096FA; }

/* 图标 */
.notif-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.notif-icon.type-like { background: #FFE8EC; color: #FF4D6A; }
.notif-icon.type-comment { background: #E8F4FF; color: #0096FA; }
.notif-icon.type-follow { background: #E6F7F0; color: #00C48C; }
.notif-icon.type-commission { background: #FFF3E0; color: #FF9800; }
.notif-icon.type-application { background: #F0E6FF; color: #9B59B6; }
.notif-icon.type-system { background: #F5F5F5; color: #999; }

/* 内容 */
.notif-body { flex: 1; min-width: 0; }
.notif-text {
  font-size: 14px; color: #333; line-height: 1.5;
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 4px;
}
.dot { width: 7px; height: 7px; background: #0096FA; border-radius: 50%; flex-shrink: 0; }
.notif-meta { display: flex; align-items: center; gap: 10px; }
.notif-time { font-size: 12px; color: #bbb; }
.notif-type-tag {
  font-size: 11px; padding: 1px 8px; border-radius: 4px; font-weight: 500;
}
.tag-like { background: #FFE8EC; color: #FF4D6A; }
.tag-comment { background: #E8F4FF; color: #0096FA; }
.tag-follow { background: #E6F7F0; color: #00C48C; }
.tag-commission { background: #FFF3E0; color: #FF9800; }
.tag-application { background: #F0E6FF; color: #9B59B6; }
.tag-system { background: #F5F5F5; color: #999; }

/* 操作 */
.notif-actions { display: flex; gap: 4px; flex-shrink: 0; }
.mini-btn {
  width: 32px; height: 32px; border: none; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; background: transparent; color: #ccc;
}
.read-btn:hover { background: #E8F4FF; color: #0096FA; }
.del-btn:hover { background: #FFF1F0; color: #FF4D4F; }

/* 空 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 80px 0; color: #bbb; font-size: 14px;
}

.pager { display: flex; justify-content: center; margin-top: 20px; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .notif-item { flex-direction: column; align-items: flex-start; }
  .notif-actions { width: 100%; justify-content: flex-end; }
}
</style>
