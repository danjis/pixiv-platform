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
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/><path d="M4 12l5 5L20 6"/></svg>
            全部已读
          </button>
          <button class="action-btn" @click="loadNotifications" title="刷新">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 2v6h-6"/><path d="M3 12a9 9 0 0 1 15-6.7L21 8"/><path d="M3 22v-6h6"/><path d="M21 12a9 9 0 0 1-15 6.7L3 16"/></svg>
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
            <svg v-if="n.type === 'LIKE'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78z"/></svg>
            <svg v-else-if="n.type === 'COMMENT'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <svg v-else-if="n.type === 'FOLLOW'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
            <svg v-else-if="n.type === 'COMMISSION'" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/></svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
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
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </button>
            <button class="mini-btn del-btn" @click="handleDelete(n.id)" title="删除">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 3h4a1 1 0 0 1 1 1v2H9V4a1 1 0 0 1 1-1z"/></svg>
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
  background: #fff;
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
  margin-bottom: 24px;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 24px; font-weight: 700; color: #1a1a1a; margin: 0; }
.unread-badge {
  font-size: 12px; font-weight: 600; color: #fff;
  background: #0096FA; padding: 2px 10px; border-radius: 999px;
}
.header-actions { display: flex; gap: 8px; }
.action-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border: none; background: #F7F8FA;
  border-radius: 999px; font-size: 13px; color: #666;
  cursor: pointer; transition: all 0.2s;
}
.action-btn:hover { color: #0096FA; background: #EDF6FF; }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 筛选 */
.filter-bar {
  display: flex; gap: 8px;
  margin-bottom: 20px;
}
.filter-btn {
  padding: 8px 20px; border: none; background: #F7F8FA;
  border-radius: 999px; font-size: 13px; font-weight: 500;
  color: #666; cursor: pointer; transition: all 0.2s;
}
.filter-btn:hover { color: #333; background: #EDEEF0; }
.filter-btn.active {
  background: #fff; color: #0096FA; font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 加载 */
.loading-state { display: flex; justify-content: center; padding: 60px 0; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #F7F8FA;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 列表 */
.notif-list { display: flex; flex-direction: column; gap: 10px; }
.notif-item {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 16px; padding: 16px 18px;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.notif-item:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.07); transform: translateY(-1px); }
.notif-item.unread { border-left: 3px solid #0096FA; }

/* 图标 */
.notif-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.notif-icon.type-like { background: #FFF0F1; color: #FF4D6A; }
.notif-icon.type-comment { background: #EDF6FF; color: #0096FA; }
.notif-icon.type-follow { background: #E9F9F2; color: #00C48C; }
.notif-icon.type-commission { background: #FFF6EB; color: #FF9800; }
.notif-icon.type-application { background: #F3EDFF; color: #9B59B6; }
.notif-icon.type-system { background: #F7F8FA; color: #999; }

/* 内容 */
.notif-body { flex: 1; min-width: 0; }
.notif-text {
  font-size: 14px; color: #333; line-height: 1.5;
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 6px;
}
.dot { width: 7px; height: 7px; background: #0096FA; border-radius: 50%; flex-shrink: 0; }
.notif-meta { display: flex; align-items: center; gap: 10px; }
.notif-time { font-size: 12px; color: #bbb; }
.notif-type-tag {
  font-size: 11px; padding: 2px 10px; border-radius: 999px; font-weight: 500;
}
.tag-like { background: #FFF0F1; color: #FF4D6A; }
.tag-comment { background: #EDF6FF; color: #0096FA; }
.tag-follow { background: #E9F9F2; color: #00C48C; }
.tag-commission { background: #FFF6EB; color: #FF9800; }
.tag-application { background: #F3EDFF; color: #9B59B6; }
.tag-system { background: #F7F8FA; color: #999; }

/* 操作 */
.notif-actions { display: flex; gap: 4px; flex-shrink: 0; }
.mini-btn {
  width: 32px; height: 32px; border: none; border-radius: 999px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; background: transparent; color: #ccc;
}
.read-btn:hover { background: #EDF6FF; color: #0096FA; }
.del-btn:hover { background: #FFF0F1; color: #FF4D4F; }

/* 空 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 16px; padding: 80px 0; color: #bbb; font-size: 14px;
}

.pager { display: flex; justify-content: center; margin-top: 24px; }

@media (max-width: 768px) {
  .notification-page { padding: 20px 12px; }
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .notif-item { flex-direction: column; align-items: flex-start; }
  .notif-actions { width: 100%; justify-content: flex-end; }
}
</style>
