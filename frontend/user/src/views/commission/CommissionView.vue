<template>
  <div class="commission-page">

    <!-- ===== HERO ===== -->
    <section class="cpage-hero">
      <div class="cph-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
      </div>
      <div class="cph-content">
        <p class="cph-eyebrow">约稿管理</p>
        <h1 class="cph-title">约稿管理中心</h1>
        <p class="cph-desc">管理你的所有委托与接稿订单，随时掌握每笔交易进度</p>
        <div class="cph-stats">
          <div class="cph-stat">
            <span class="cph-num">{{ clientTotal || 0 }}</span>
            <span class="cph-lbl">我的委托</span>
          </div>
          <div class="cph-divider"></div>
          <div class="cph-stat" v-if="isArtist">
            <span class="cph-num">{{ artistTotal || 0 }}</span>
            <span class="cph-lbl">我的接稿</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== MAIN ===== -->
    <div class="main-wrap">

      <!-- Tab Rail -->
      <div class="tab-rail">
        <button
          class="tab-pill"
          :class="{ active: activeTab === 'client' }"
          @click="switchTab('client')"
        >
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-3.3 3.6-6 8-6s8 2.7 8 6"/></svg>
          我的委托
          <span v-if="clientTotal" class="tab-badge">{{ clientTotal }}</span>
        </button>
        <button
          v-if="isArtist"
          class="tab-pill"
          :class="{ active: activeTab === 'artist' }"
          @click="switchTab('artist')"
        >
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="3"/><path d="M9 12h6M12 9v6"/></svg>
          我的接稿
          <span v-if="artistTotal" class="tab-badge">{{ artistTotal }}</span>
        </button>
      </div>

      <!-- Status Filter -->
      <div class="filter-strip">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          class="filter-chip"
          :class="{ active: currentStatus === f.value }"
          @click="filterByStatus(f.value)"
        >
          <span v-if="f.dot" class="chip-dot" :style="{ background: f.dot }"></span>
          {{ f.label }}
        </button>
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="order-grid">
        <div v-for="n in 6" :key="n" class="order-card skel-card">
          <div class="skel-bar"></div>
          <div class="skel-head"></div>
          <div class="skel-body">
            <div class="skel-line w70"></div>
            <div class="skel-line w50"></div>
            <div class="skel-line w40"></div>
          </div>
        </div>
      </div>

      <!-- Order Grid -->
      <div v-else-if="commissions.length > 0" class="order-grid">
        <article
          v-for="c in commissions"
          :key="c.id"
          class="order-card"
          @click="goDetail(c)"
        >
          <!-- Status color bar -->
          <div class="card-status-bar" :class="'bar-' + c.status.toLowerCase()"></div>

          <!-- Header: party + status badge -->
          <div class="oc-header">
            <div class="oc-party">
              <div class="party-avatar-wrap">
                <el-avatar :size="44" :src="getOtherAvatar(c) || defaultAvatar" class="party-avatar"></el-avatar>
                <span class="party-online" :class="'online-' + c.status.toLowerCase()"></span>
              </div>
              <div class="party-meta">
                <span class="party-role-tag">{{ activeTab === 'client' ? '画师' : '委托方' }}</span>
                <span class="party-name">{{ getOtherName(c) || '未知用户' }}</span>
              </div>
            </div>
            <div class="status-badge" :class="'s-' + c.status.toLowerCase()">
              <span class="status-dot"></span>
              {{ getStatusText(c.status) }}
            </div>
          </div>

          <!-- Title & desc -->
          <h3 class="oc-title">{{ c.title }}</h3>
          <p class="oc-desc">{{ c.description }}</p>

          <!-- Quote banner (client sees artist's quote) -->
          <div v-if="c.status === 'QUOTED' && activeTab === 'client'" class="quote-banner">
            <div class="qb-inner">
              <div class="qb-col">
                <p class="qb-label">画师报价</p>
                <p class="qb-amount">&yen;{{ c.totalAmount }}</p>
              </div>
              <div class="qb-sep"></div>
              <div class="qb-col">
                <p class="qb-label">需付定金</p>
                <p class="qb-deposit">&yen;{{ c.depositAmount }}</p>
              </div>
            </div>
            <p v-if="c.quoteNote" class="qb-note">{{ c.quoteNote }}</p>
          </div>

          <!-- Meta chips -->
          <div class="oc-meta">
            <span v-if="c.status === 'PENDING' && c.budget" class="meta-chip price-chip">
              <svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
              预算 &yen;{{ c.budget }}
            </span>
            <span v-if="c.totalAmount && c.status !== 'PENDING'" class="meta-chip price-chip">
              <svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
              报价 &yen;{{ c.totalAmount }}
            </span>
            <span v-if="c.deadline" class="meta-chip date-chip">
              <svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
              {{ formatDate(c.deadline) }}
            </span>
            <span v-if="c.status === 'IN_PROGRESS'" class="meta-chip progress-chip">
              <span class="progress-dot"></span>创作中
            </span>
          </div>

          <!-- Footer -->
          <div class="oc-footer">
            <span class="oc-time">{{ formatRelative(c.updatedAt || c.createdAt) }}</span>
            <span class="oc-arrow">›</span>
          </div>
        </article>
      </div>

      <!-- Empty -->
      <div v-else class="empty-state">
        <div class="empty-illustration">
          <svg viewBox="0 0 120 100" width="120" height="100" fill="none">
            <rect x="15" y="20" width="90" height="65" rx="10" fill="#eef2ff" stroke="#c7d2fe" stroke-width="1.5"/>
            <rect x="25" y="35" width="50" height="6" rx="3" fill="#c7d2fe"/>
            <rect x="25" y="47" width="35" height="5" rx="2.5" fill="#ddd6fe"/>
            <rect x="25" y="58" width="42" height="5" rx="2.5" fill="#ddd6fe"/>
            <circle cx="90" cy="30" r="14" fill="#818cf8"/>
            <path d="M84 30h12M90 24v12" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">暂无{{ activeTab === 'client' ? '委托' : '接稿' }}记录</p>
        <p class="empty-sub">{{ activeTab === 'client' ? '去发现画师，开启你的第一笔约稿' : '等待客户发起委托' }}</p>
        <router-link to="/artworks" class="empty-cta" v-if="activeTab === 'client'">探索画师</router-link>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="page === 1" @click="changePage(page - 1)">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <button
          v-for="p in pageNumbers"
          :key="p"
          class="page-btn"
          :class="{ active: p === page }"
          @click="changePage(p)"
        >{{ p }}</button>
        <button class="page-btn" :disabled="page === totalPages" @click="changePage(page + 1)">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyCommissions as getCommissions } from '@/api/commission'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const isArtist = computed(() => userStore.isArtist)
const activeTab = ref('client')
const currentStatus = ref('')
const loading = ref(false)
const commissions = ref([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const clientTotal = ref(0)
const artistTotal = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const pageNumbers = computed(() => {
  const pages = []
  const start = Math.max(1, page.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待报价', value: 'PENDING', dot: '#f59e0b' },
  { label: '已报价', value: 'QUOTED', dot: '#3b82f6' },
  { label: '已付定金', value: 'DEPOSIT_PAID', dot: '#6366f1' },
  { label: '创作中', value: 'IN_PROGRESS', dot: '#ec4899' },
  { label: '已交付', value: 'DELIVERED', dot: '#10b981' },
  { label: '已完成', value: 'COMPLETED', dot: '#059669' },
  { label: '已取消', value: 'CANCELLED', dot: '#94a3b8' },
]

const statusTextMap = {
  PENDING: '待报价', QUOTED: '已报价', DEPOSIT_PAID: '已付定金',
  IN_PROGRESS: '创作中', DELIVERED: '已交付', COMPLETED: '已完成',
  CANCELLED: '已取消', REJECTED: '已拒绝'
}

function getStatusText(s) { return statusTextMap[s] || s }
function getOtherName(c) { return activeTab.value === 'client' ? c.artistName : c.clientName }
function getOtherAvatar(c) { return activeTab.value === 'client' ? c.artistAvatar : c.clientAvatar }


async function loadCommissions() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize, role: activeTab.value }
    if (currentStatus.value) params.status = currentStatus.value
    const res = await getCommissions(params)
    if (res.code === 200) {
      const d = res.data
      commissions.value = d.records || []
      total.value = d.total || 0
      if (activeTab.value === 'client') clientTotal.value = d.total || 0
      else artistTotal.value = d.total || 0
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
async function switchTab(tab) { activeTab.value = tab; currentStatus.value = ''; page.value = 1; await loadCommissions() }
async function filterByStatus(s) { currentStatus.value = s; page.value = 1; await loadCommissions() }
async function changePage(p) { page.value = p; await loadCommissions() }
function goDetail(c) { router.push({ name: 'CommissionDetail', params: { id: c.id } }) }
function formatDate(t) { if (!t) return ''; return new Date(t).toLocaleDateString('zh-CN') }
function formatRelative(t) {
  if (!t) return ''
  const diff = Date.now() - new Date(t).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '\u521a\u521a'
  if (mins < 60) return mins + '\u5206\u949f\u524d'
  const hours = Math.floor(mins / 60)
  if (hours < 24) return hours + '\u5c0f\u65f6\u524d'
  const days = Math.floor(hours / 24)
  if (days < 30) return days + '\u5929\u524d'
  return new Date(t).toLocaleDateString('zh-CN')
}
onMounted(async () => {
  await loadCommissions()
  if (isArtist.value) {
    try {
      const res = await getCommissions({ page: 1, size: 1, role: 'artist' })
      if (res.code === 200) artistTotal.value = res.data.total || 0
    } catch {}
  }
})
</script>

<style scoped>
.commission-page { min-height: calc(100vh - 64px); background: #fff; }
.cpage-hero { position: relative; overflow: hidden; padding: 60px 40px 54px; background: linear-gradient(135deg, #EFF6FF, #ECFDF5, #F5F3FF); }
.cph-orbs { display: none; }
.orb { display: none; }
.orb-1 { display: none; }
.orb-2 { display: none; }
.orb-3 { display: none; }
.cph-content { position: relative; z-index: 1; max-width: 720px; margin: 0 auto; text-align: center; }
.cph-eyebrow { font-size: 12px; font-weight: 700; letter-spacing: 3px; color: #94a3b8; margin-bottom: 14px; }
.cph-title { font-size: 36px; font-weight: 900; color: #1e293b; letter-spacing: -0.5px; margin-bottom: 10px; }
.cph-desc { font-size: 15px; color: #64748b; margin-bottom: 30px; line-height: 1.6; }
.cph-stats { display: flex; justify-content: center; align-items: center; gap: 36px; }
.cph-stat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.cph-num { font-size: 34px; font-weight: 900; color: #1e293b; line-height: 1; }
.cph-lbl { font-size: 12px; color: #94a3b8; letter-spacing: 1px; }
.cph-divider { width: 1px; height: 44px; background: #e2e8f0; }
.main-wrap { max-width: 1120px; margin: 0 auto; padding: 32px 24px 80px; }
.tab-rail { display: flex; gap: 8px; margin-bottom: 22px; }
.tab-pill { display: inline-flex; align-items: center; gap: 8px; padding: 10px 22px; border: none; border-radius: 999px; background: #F7F8FA; font-size: 14px; font-weight: 700; color: #64748b; cursor: pointer; transition: all .22s; }
.tab-pill:hover { color: #3b82f6; background: #EFF6FF; }
.tab-pill.active { background: #fff; color: #3b82f6; box-shadow: 0 2px 16px rgba(0,0,0,0.04); }
.tab-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 20px; height: 20px; padding: 0 6px; border-radius: 999px; font-size: 11px; font-weight: 800; background: rgba(59,130,246,.1); color: #3b82f6; }
.tab-pill.active .tab-badge { background: rgba(59,130,246,.12); color: #3b82f6; }
.tab-pill:not(.active) .tab-badge { background: #e0e7ff; color: #3b82f6; }
.filter-strip { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 28px; }
.filter-chip { display: inline-flex; align-items: center; gap: 6px; padding: 6px 16px; border: none; border-radius: 999px; background: #F7F8FA; font-size: 13px; font-weight: 600; color: #64748b; cursor: pointer; transition: all .18s; }
.filter-chip:hover { color: #3b82f6; background: #EFF6FF; }
.filter-chip.active { background: #fff; color: #3b82f6; box-shadow: 0 2px 16px rgba(0,0,0,0.04); }
.chip-dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }
.order-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 18px; }
.order-card { position: relative; background: #fff; border-radius: 20px; padding: 22px; cursor: pointer; border: 1px solid #f1f5f9; box-shadow: 0 2px 16px rgba(0,0,0,0.04); transition: all .28s ease; overflow: hidden; }
.order-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-color: #e2e8f0; }
.card-status-bar { position: absolute; top: 0; left: 0; right: 0; height: 3px; }
.bar-pending { background: linear-gradient(90deg,#f59e0b,#fbbf24); }
.bar-quoted { background: linear-gradient(90deg,#3b82f6,#60a5fa); }
.bar-deposit_paid { background: linear-gradient(90deg,#6366f1,#818cf8); }
.bar-in_progress { background: linear-gradient(90deg,#ec4899,#f472b6); }
.bar-delivered { background: linear-gradient(90deg,#10b981,#34d399); }
.bar-completed { background: linear-gradient(90deg,#059669,#10b981); }
.bar-cancelled,.bar-rejected { background: #e2e8f0; }
.oc-header { display: flex; align-items: center; justify-content: space-between; margin: 10px 0 14px; }
.oc-party { display: flex; align-items: center; gap: 10px; }
.party-avatar-wrap { position: relative; }
.party-online { position: absolute; bottom: 1px; right: 1px; width: 10px; height: 10px; border-radius: 50%; border: 2px solid #fff; background: #94a3b8; }
.online-in_progress { background: #10b981; animation: pulse 2s infinite; }
.online-pending { background: #f59e0b; }
.online-quoted { background: #3b82f6; }
.online-deposit_paid { background: #6366f1; }
.online-delivered,.online-completed { background: #10b981; }
@keyframes pulse { 0%,100%{box-shadow:0 0 0 0 rgba(16,185,129,.4)} 50%{box-shadow:0 0 0 5px rgba(16,185,129,0)} }
.party-meta { display: flex; flex-direction: column; gap: 3px; }
.party-role-tag { font-size: 10px; font-weight: 700; color: #94a3b8; letter-spacing: 1px; text-transform: uppercase; }
.party-name { font-size: 14px; font-weight: 800; color: #1e293b; }
.status-badge { display: inline-flex; align-items: center; gap: 5px; padding: 4px 12px; border-radius: 999px; font-size: 12px; font-weight: 700; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; opacity: .7; }
.s-pending { background: #fef9c3; color: #a16207; }
.s-quoted { background: #dbeafe; color: #1d4ed8; }
.s-deposit_paid { background: #e0e7ff; color: #4338ca; }
.s-in_progress { background: #fce7f3; color: #be185d; }
.s-delivered { background: #d1fae5; color: #065f46; }
.s-completed { background: #dcfce7; color: #15803d; }
.s-cancelled,.s-rejected { background: #f1f5f9; color: #64748b; }
.oc-title { font-size: 15px; font-weight: 800; color: #1e293b; margin: 0 0 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.oc-desc { font-size: 13px; color: #64748b; margin: 0 0 14px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.55; }
.quote-banner { background: linear-gradient(135deg,#EFF6FF,#F5F3FF); border: 1px solid #e2e8f0; border-radius: 16px; padding: 14px; margin-bottom: 14px; }
.qb-inner { display: flex; align-items: center; gap: 14px; }
.qb-col { flex: 1; }
.qb-label { font-size: 11px; color: #64748b; margin: 0 0 3px; }
.qb-amount { font-size: 20px; font-weight: 900; color: #3b82f6; margin: 0; }
.qb-deposit { font-size: 16px; font-weight: 700; color: #3b82f6; margin: 0; }
.qb-sep { width: 1px; height: 36px; background: #e2e8f0; }
.qb-note { font-size: 12px; color: #64748b; margin: 10px 0 0; }
.oc-meta { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.meta-chip { display: inline-flex; align-items: center; gap: 4px; padding: 4px 11px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.price-chip { background: #fef3c7; color: #92400e; }
.date-chip { background: #f0f9ff; color: #0369a1; }
.progress-chip { background: #fdf2f8; color: #9d174d; }
.progress-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #ec4899; animation: blink 1.4s infinite; margin-right: 2px; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }
.oc-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 12px; border-top: 1px solid #f1f5f9; }
.oc-time { font-size: 12px; color: #94a3b8; }
.oc-arrow { font-size: 22px; color: #cbd5e1; transition: color .2s, transform .2s; }
.order-card:hover .oc-arrow { color: #3b82f6; transform: translateX(3px); }
.skel-card { animation: shimmer 1.5s infinite; }
.skel-bar { height: 3px; background: #f1f5f9; margin-bottom: 18px; }
.skel-head { height: 48px; background: #f1f5f9; border-radius: 12px; margin-bottom: 14px; }
.skel-body { display: flex; flex-direction: column; gap: 8px; }
.skel-line { height: 12px; background: #f1f5f9; border-radius: 6px; }
.w70{width:70%} .w50{width:50%} .w40{width:40%}
@keyframes shimmer { 0%,100%{opacity:1} 50%{opacity:.55} }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 0 60px; gap: 14px; }
.empty-illustration { opacity: .8; }
.empty-title { font-size: 20px; font-weight: 700; color: #334155; margin: 0; }
.empty-sub { font-size: 13px; color: #94a3b8; margin: 0; }
.empty-cta { margin-top: 4px; padding: 11px 32px; background: #3b82f6; color: #fff; border-radius: 999px; font-size: 14px; font-weight: 700; text-decoration: none; box-shadow: 0 2px 16px rgba(59,130,246,.2); transition: transform .2s, box-shadow .2s; }
.empty-cta:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(59,130,246,.25); }
.pagination { display: flex; justify-content: center; gap: 8px; margin-top: 36px; }
.page-btn { width: 36px; height: 36px; border: 1px solid #f1f5f9; border-radius: 999px; background: #F7F8FA; font-size: 14px; cursor: pointer; transition: all .18s; display: flex; align-items: center; justify-content: center; color: #64748b; }
.page-btn:hover:not(:disabled) { background: #EFF6FF; color: #3b82f6; }
.page-btn.active { background: #3b82f6; border-color: transparent; color: #fff; font-weight: 700; box-shadow: 0 2px 16px rgba(59,130,246,.2); }
.page-btn:disabled { opacity: .35; cursor: not-allowed; }
@media (max-width: 768px) {
  .cpage-hero { padding: 40px 20px 36px; }
  .cph-title { font-size: 26px; }
  .order-grid { grid-template-columns: 1fr; }
  .main-wrap { padding: 20px 16px 40px; }
}
</style>
