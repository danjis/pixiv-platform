<template>
  <div class="artworks-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">作品管理</h1>
        <p class="page-desc">管理你发布的所有作品</p>
      </div>
      <router-link to="/publish" class="publish-btn">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        投稿新作品
      </router-link>
    </div>

    <!-- Tab 切换：已发布 / 草稿 -->
    <div class="tab-bar">
      <button class="tab-btn" :class="{ active: activeTab === 'published' }" @click="switchTab('published')">
        已发布 <span v-if="total > 0" class="tab-count">{{ total }}</span>
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'drafts' }" @click="switchTab('drafts')">
        草稿 <span v-if="draftTotal > 0" class="tab-count">{{ draftTotal }}</span>
      </button>
    </div>

    <!-- 搜索与筛选 -->
    <div class="toolbar">
      <div class="search-box">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="#bbb"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        <input v-model="searchKeyword" placeholder="搜索作品标题..." @input="handleSearch" />
      </div>
      <div v-if="activeTab === 'published'" class="sort-group">
        <button
          v-for="s in sortOptions"
          :key="s.value"
          class="sort-btn"
          :class="{ active: sortBy === s.value }"
          @click="sortBy = s.value; loadArtworks()"
        >
          {{ s.label }}
        </button>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <!-- ===== 已发布作品列表 ===== -->
    <template v-else-if="activeTab === 'published'">
      <div v-if="artworks.length > 0" class="artwork-grid">
        <div v-for="art in artworks" :key="art.id" class="artwork-card">
          <div class="card-image" @click="$router.push(`/artworks/${art.id}`)">
            <img :src="art.thumbnailUrl || art.imageUrl" :alt="art.title" loading="lazy" />
            <div class="image-overlay">
              <span class="view-text">查看详情</span>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title" @click="$router.push(`/artworks/${art.id}`)">{{ art.title }}</h3>
            <div class="card-stats">
              <span class="stat">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                {{ art.likeCount || 0 }}
              </span>
              <span class="stat">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                {{ art.viewCount || 0 }}
              </span>
              <span class="stat">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z"/></svg>
                {{ art.commentCount || 0 }}
              </span>
            </div>
            <div class="card-meta">
              <span class="card-date">{{ formatDate(art.createdAt) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn edit" @click="handleEdit(art)" title="编辑">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
            </button>
            <button class="action-btn delete" @click="handleDelete(art)" title="删除">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none" stroke="#ddd" stroke-width="1.5">
          <rect x="8" y="12" width="48" height="40" rx="6"/>
          <circle cx="22" cy="28" r="6"/>
          <path d="M8 46l14-14 10 10 10-16 14 20" stroke-linejoin="round"/>
        </svg>
        <p>还没有发布过作品</p>
        <router-link to="/publish" class="empty-action">投稿第一个作品</router-link>
      </div>
    </template>

    <!-- ===== 草稿列表 ===== -->
    <template v-else-if="activeTab === 'drafts'">
      <div v-if="drafts.length > 0" class="artwork-grid">
        <div v-for="draft in drafts" :key="draft.id" class="artwork-card draft-card">
          <div class="card-image" @click="$router.push(`/publish?draftId=${draft.id}`)">
            <img v-if="draft.thumbnailUrl || draft.imageUrl" :src="draft.thumbnailUrl || draft.imageUrl" :alt="draft.title" loading="lazy" />
            <div v-else class="draft-no-image">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="#ccc"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
              <span>无图片</span>
            </div>
            <div class="image-overlay">
              <span class="view-text">继续编辑</span>
            </div>
            <span class="draft-badge">草稿</span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ draft.title || '未命名草稿' }}</h3>
            <div class="card-meta">
              <span class="card-date">{{ formatDate(draft.createdAt) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn publish" @click="handlePublishDraft(draft)" title="发布">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
            </button>
            <button class="action-btn delete" @click="handleDelete(draft)" title="删除">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none" stroke="#ddd" stroke-width="1.5">
          <rect x="8" y="8" width="48" height="48" rx="4" />
          <path d="M20 24h24M20 32h16M20 40h20" stroke-linecap="round"/>
        </svg>
        <p>暂无草稿</p>
      </div>
    </template>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        small
        @current-change="loadArtworks"
      />
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="editDialog" class="dialog-mask" @click.self="editDialog = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>编辑作品</h3>
          <button class="close-btn" @click="editDialog = false">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
          </button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>标题</label>
            <input v-model="editForm.title" class="form-input" placeholder="作品标题" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="editForm.description" class="form-textarea" rows="4" placeholder="作品描述"></textarea>
          </div>
          <div class="form-group">
            <label>标签 <span class="hint">（逗号分隔）</span></label>
            <input v-model="editForm.tagsStr" class="form-input" placeholder="例如：原创, 风景, 水彩" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="editDialog = false">取消</button>
          <button class="btn-save" @click="handleSaveEdit" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArtworks, updateArtwork, deleteArtwork, getDrafts, publishDraft } from '@/api/artwork'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const artworks = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const searchKeyword = ref('')
const sortBy = ref('newest')

const activeTab = ref('published')
const drafts = ref([])
const draftPage = ref(1)
const draftTotal = ref(0)

const editDialog = ref(false)
const editForm = ref({ id: null, title: '', description: '', tagsStr: '' })
const saving = ref(false)

const sortOptions = [
  { label: '最新发布', value: 'newest' },
  { label: '最多赞', value: 'likes' },
  { label: '最多浏览', value: 'views' }
]

let searchTimer = null
function handleSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    if (activeTab.value === 'published') {
      loadArtworks()
    }
  }, 300)
}

function switchTab(tab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  if (tab === 'published') {
    loadArtworks()
  } else {
    loadDrafts()
  }
}

async function loadArtworks() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      artistId: userStore.user?.id,
      keyword: searchKeyword.value || undefined,
      sortBy: sortBy.value
    }
    const res = await getArtworks(params)
    if (res.code === 200) {
      artworks.value = res.data?.records || res.data?.content || res.data?.items || []
      total.value = res.data?.total || res.data?.totalElements || 0
    }
  } catch {
    ElMessage.error('加载作品失败')
  } finally {
    loading.value = false
  }
}

async function loadDrafts() {
  loading.value = true
  try {
    const res = await getDrafts({ page: draftPage.value, size: pageSize.value })
    if (res.code === 200) {
      drafts.value = res.data?.records || res.data?.content || res.data?.items || []
      draftTotal.value = res.data?.total || res.data?.totalElements || 0
    }
  } catch {
    ElMessage.error('加载草稿失败')
  } finally {
    loading.value = false
  }
}

async function handlePublishDraft(draft) {
  try {
    await ElMessageBox.confirm(`确定要发布「${draft.title || '未命名草稿'}」吗？`, '发布草稿', { type: 'info', confirmButtonText: '发布', cancelButtonText: '取消' })
    const res = await publishDraft(draft.id)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadDrafts()
      // 同时刷新已发布计数
      getArtworks({ page: 1, size: 1, artistId: userStore.user?.id }).then(r => {
        if (r.code === 200) total.value = r.data?.total || r.data?.totalElements || 0
      })
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('发布失败')
  }
}

function handleEdit(art) {
  router.push({ name: 'EditArtwork', params: { id: art.id } })
}

async function handleSaveEdit() {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  saving.value = true
  try {
    const tags = editForm.value.tagsStr
      .split(/[,，]/)
      .map(t => t.trim())
      .filter(Boolean)
    const res = await updateArtwork(editForm.value.id, {
      title: editForm.value.title.trim(),
      description: editForm.value.description.trim(),
      tags
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialog.value = false
      loadArtworks()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    ElMessage.error('更新失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(art) {
  try {
    await ElMessageBox.confirm(`确定要删除「${art.title || '未命名草稿'}」吗？此操作不可恢复。`, '确认删除', { type: 'warning' })
    const res = await deleteArtwork(art.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      if (activeTab.value === 'published') {
        loadArtworks()
      } else {
        loadDrafts()
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  loadArtworks()
  // 预加载草稿计数
  getDrafts({ page: 1, size: 1 }).then(res => {
    if (res.code === 200) draftTotal.value = res.data?.total || res.data?.totalElements || 0
  }).catch(() => {})
})
</script>

<style scoped>
.artworks-page { max-width: 100%; }

/* 页头 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title { font-size: 22px; font-weight: 700; color: #1a1a1a; margin: 0 0 4px 0; }
.page-desc { font-size: 14px; color: #999; margin: 0; }
.publish-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px; background: #0096FA; color: #fff;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 600;
  text-decoration: none; cursor: pointer; transition: background 0.2s;
}
.publish-btn:hover { background: #0080d5; }

/* 工具栏 */
.tab-bar {
  display: flex; gap: 0; margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}
.tab-btn {
  padding: 10px 20px; border: none; background: none;
  font-size: 15px; font-weight: 500; color: #999;
  cursor: pointer; transition: all 0.2s;
  position: relative;
}
.tab-btn:hover { color: #555; }
.tab-btn.active { color: #0096FA; font-weight: 600; }
.tab-btn.active::after {
  content: ''; position: absolute; bottom: -2px; left: 0; right: 0;
  height: 2px; background: #0096FA; border-radius: 1px;
}
.tab-count {
  display: inline-block; margin-left: 4px; padding: 1px 7px;
  background: #f0f0f0; border-radius: 10px; font-size: 12px; color: #999;
}
.tab-btn.active .tab-count { background: rgba(0,150,250,0.1); color: #0096FA; }

.toolbar {
  display: flex; gap: 16px; align-items: center;
  margin-bottom: 20px; flex-wrap: wrap;
}
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: #fff; padding: 8px 14px; border-radius: 10px;
  flex: 1; max-width: 320px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.search-box input {
  border: none; outline: none; font-size: 14px; color: #333;
  background: transparent; width: 100%;
}
.search-box input::placeholder { color: #bbb; }
.sort-group { display: flex; gap: 4px; }
.sort-btn {
  padding: 7px 14px; border: none; background: #fff;
  border-radius: 8px; font-size: 13px; color: #666;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.sort-btn:hover { color: #333; }
.sort-btn.active { background: #0096FA; color: #fff; box-shadow: 0 2px 8px rgba(0,150,250,0.3); }

/* 加载 */
.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #eee;
  border-top-color: #0096FA; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 作品网格 - 瀑布流 */
.artwork-grid {
  column-count: 4;
  column-gap: 16px;
}
.artwork-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: all 0.2s;
  position: relative;
  break-inside: avoid;
  margin-bottom: 16px;
}
.artwork-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}
.card-image {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  line-height: 0;
}
.card-image img {
  width: 100%; height: auto; display: block;
  transition: transform 0.3s;
}
.artwork-card:hover .card-image img { transform: scale(1.05); }
.image-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.card-image:hover .image-overlay { opacity: 1; }
.view-text { color: #fff; font-size: 14px; font-weight: 500; }

.card-body { padding: 12px 14px 8px; }
.card-title {
  font-size: 14px; font-weight: 600; color: #333;
  margin: 0 0 6px 0; cursor: pointer;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.card-title:hover { color: #0096FA; }
.card-stats {
  display: flex; gap: 12px; margin-bottom: 4px;
}
.stat {
  display: flex; align-items: center; gap: 3px;
  font-size: 12px; color: #bbb;
}
.card-meta { padding-bottom: 4px; }
.card-date { font-size: 11px; color: #ccc; }

.card-actions {
  position: absolute; top: 8px; right: 8px;
  display: flex; gap: 4px;
  opacity: 0; transition: opacity 0.2s;
}
.artwork-card:hover .card-actions { opacity: 1; }
.action-btn {
  width: 32px; height: 32px; border: none; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.15s;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(4px);
}
.action-btn.edit { color: #0096FA; }
.action-btn.edit:hover { background: #0096FA; color: #fff; }
.action-btn.delete { color: #FF4D4F; }
.action-btn.delete:hover { background: #FF4D4F; color: #fff; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 80px 0; color: #bbb; font-size: 14px;
}
.empty-action {
  padding: 8px 20px; background: #0096FA; color: #fff;
  border-radius: 8px; font-size: 14px; text-decoration: none;
}
.empty-action:hover { background: #0080d5; }

.pager { display: flex; justify-content: center; margin-top: 24px; }

/* 编辑弹窗 */
.dialog-mask {
  position: fixed; inset: 0; z-index: 2000;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #fff; border-radius: 16px; width: 480px; max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.dialog-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px 0;
}
.dialog-header h3 { font-size: 18px; font-weight: 600; margin: 0; }
.close-btn {
  background: none; border: none; color: #999; cursor: pointer;
  padding: 4px; border-radius: 6px;
}
.close-btn:hover { background: #f0f0f0; color: #333; }
.dialog-body { padding: 20px 24px; }
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: #555;
  margin-bottom: 6px;
}
.hint { font-weight: 400; color: #bbb; }
.form-input, .form-textarea {
  width: 100%; padding: 10px 14px; border: 1px solid #e0e0e0;
  border-radius: 8px; font-size: 14px; color: #333;
  transition: border-color 0.2s; box-sizing: border-box;
}
.form-input:focus, .form-textarea:focus {
  outline: none; border-color: #0096FA;
}
.form-textarea { resize: vertical; font-family: inherit; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 0 24px 20px;
}
.btn-cancel {
  padding: 8px 20px; border: 1px solid #e0e0e0; background: #fff;
  border-radius: 8px; font-size: 14px; color: #666; cursor: pointer;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-save {
  padding: 8px 24px; border: none; background: #0096FA; color: #fff;
  border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer;
}
.btn-save:hover { background: #0080d5; }
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }

/* 草稿相关 */
.draft-card { border: 1px dashed #e0e0e0; }
.draft-badge {
  position: absolute; top: 8px; left: 8px;
  padding: 2px 10px; background: rgba(255,165,0,0.85); color: #fff;
  font-size: 11px; font-weight: 600; border-radius: 6px;
  backdrop-filter: blur(4px);
}
.draft-no-image {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 140px; background: #f8f8f8; color: #ccc; gap: 6px;
}
.draft-no-image span { font-size: 12px; }
.action-btn.publish { color: #52c41a; }
.action-btn.publish:hover { background: #52c41a; color: #fff; }

@media (max-width: 600px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 12px; }
  .toolbar { flex-direction: column; }
  .search-box { max-width: 100%; }
}
</style>
