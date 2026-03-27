<template>
  <div class="home-page">

    <!-- ===== HERO ===== -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-noise"></div>
        <div class="hero-orb hero-orb-1"></div>
        <div class="hero-orb hero-orb-2"></div>
      </div>
      <div class="hero-inner">
        <div class="hero-text">
          <p class="hero-eyebrow">✦ 艺术创作社区</p>
          <h1 class="hero-title">
            <span class="hero-title-line">发现</span>
            <span class="hero-title-line accent">好作品</span>
          </h1>
          <p class="hero-desc">在这里遇见最打动你的艺术，连接全球创作者</p>
          <div class="hero-actions">
            <button class="hero-btn-primary" @click="$router.push('/artworks')">
              探索作品
              <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z"/></svg>
            </button>
            <button
              v-if="!userStore.isAuthenticated"
              class="hero-btn-ghost"
              @click="$router.push('/register')"
            >免费注册</button>
          </div>
          <div class="hero-stats">
            <div class="hero-stat">
              <span class="stat-num">10万+</span>
              <span class="stat-label">原创作品</span>
            </div>
            <div class="stat-divider"></div>
            <div class="hero-stat">
              <span class="stat-num">5万+</span>
              <span class="stat-label">创作者</span>
            </div>
            <div class="stat-divider"></div>
            <div class="hero-stat">
              <span class="stat-num">每日</span>
              <span class="stat-label">持续更新</span>
            </div>
          </div>
        </div>

        <!-- Artwork showcase mosaic -->
        <div class="hero-mosaic">
          <div
            v-for="(art, i) in featuredArtworks"
            :key="art.id || i"
            class="mosaic-item"
            :class="'mosaic-' + i"
            @click="goToDetail(art.id)"
          >
            <img :src="art.thumbnailUrl || art.imageUrl" :alt="art.title" class="mosaic-img" />
            <div class="mosaic-shine"></div>
          </div>
        </div>
      </div>

      <!-- Scroll hint -->
      <div class="scroll-hint">
        <span class="scroll-line"></span>
        <span class="scroll-label">向下探索</span>
      </div>
    </section>

    <!-- ===== CONTEST SECTION ===== -->
    <section v-if="contestEntries.length > 0" class="contest-section">
      <div class="section-wrap">
        <div class="section-head">
          <div class="section-label">比赛精选</div>
          <h2 class="section-title">参赛佳作</h2>
          <router-link to="/contests" class="section-more">查看全部 →</router-link>
        </div>
        <div class="contest-grid">
          <div
            v-for="entry in contestEntries"
            :key="entry.id"
            class="contest-card"
            @click="goToContestEntry(entry)"
          >
            <div class="contest-thumb">
              <img :src="entry.imageUrl" :alt="entry.title" />
              <div class="contest-meta">
                <span class="contest-badge">{{ entry.contestTitle }}</span>
                <span v-if="entry.averageScore > 0" class="contest-score">
                  ★ {{ entry.averageScore.toFixed(1) }}
                </span>
              </div>
            </div>
            <div class="contest-info">
              <h3 class="contest-name">{{ entry.title }}</h3>
              <p class="contest-artist">{{ entry.artistName }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== ARTWORKS GRID ===== -->
    <section class="artworks-section">
      <div class="section-wrap">
        <div class="section-head">
          <div class="section-label">推荐</div>
          <h2 class="section-title">精选作品</h2>
          <router-link to="/artworks" class="section-more">更多作品 →</router-link>
        </div>

        <div v-if="loading" class="artwork-grid">
          <div v-for="n in 12" :key="n" class="skeleton-card">
            <div class="skeleton-thumb"></div>
            <div class="skeleton-info">
              <div class="skeleton-line w70"></div>
              <div class="skeleton-line w40"></div>
            </div>
          </div>
        </div>

        <div v-else-if="artworks.length > 0" class="artwork-grid">
          <ArtworkCard
            v-for="artwork in artworks"
            :key="artwork.id"
            :artwork="artwork"
            @click="goToDetail(artwork.id)"
            @author-click="(art) => goToArtist(art.artistId)"
          />
        </div>

        <div v-else class="empty-state">
          <svg viewBox="0 0 64 64" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.2">
            <rect x="8" y="8" width="48" height="48" rx="6"/>
            <circle cx="24" cy="26" r="5"/>
            <path d="M8 44l14-14 10 10 12-14 12 12"/>
          </svg>
          <p>暂无推荐作品</p>
        </div>
      </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <span class="footer-logo"><span class="footer-logo-mark">A</span>rtfolio</span>
        <span class="footer-copy">© 2026 Artfolio Platform. Built with passion.</span>
      </div>
    </footer>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArtworks } from '@/api/artwork'
import { getFeaturedEntries } from '@/api/contest'
import ArtworkCard from '@/components/ArtworkCard.vue'

const router = useRouter()
const userStore = useUserStore()
const artworks = ref([])
const featuredArtworks = ref([])
const contestEntries = ref([])
const loading = ref(true)

async function loadContestEntries() {
  try {
    const res = await getFeaturedEntries(8)
    if (res.code === 200 && res.data) contestEntries.value = res.data
  } catch (e) { console.error(e) }
}

async function loadArtworks() {
  loading.value = true
  try {
    const response = await getArtworks({ page: 1, size: 18, sortBy: 'latest' })
    if (response.code === 200 && response.data) {
      const records = response.data.records || []
      artworks.value = records
      featuredArtworks.value = records.slice(0, 5)
    }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

function goToDetail(id) { if (id) router.push({ name: 'ArtworkDetail', params: { id } }) }
function goToArtist(id) { if (id) router.push(`/artist/${id}`) }
function goToContestEntry(entry) { if (entry.contestId) router.push(`/contests/${entry.contestId}`) }

onMounted(() => { loadArtworks(); loadContestEntries() })
</script>
<style scoped>
.home-page { min-height: 100vh; background: var(--px-bg-secondary); }

/* ===== HERO ===== */
.hero-section {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 20% 50%, #1a0f08 0%, var(--ink-900) 60%);
}
.hero-noise {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 200px;
}
.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.hero-orb-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(255,107,71,0.18) 0%, transparent 70%);
  top: -100px; left: -80px;
}
.hero-orb-2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(245,200,66,0.1) 0%, transparent 70%);
  bottom: 0; right: 10%;
}
.hero-inner {
  position: relative;
  z-index: 1;
  flex: 1;
  max-width: var(--px-max-width);
  margin: 0 auto;
  padding: 100px 24px 80px;
  display: flex;
  align-items: center;
  gap: 64px;
  width: 100%;
}
.hero-text { flex: 0 0 420px; }
.hero-eyebrow {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: var(--coral);
  margin-bottom: 20px;
  font-family: var(--px-font-mono);
  animation: reveal-up 0.6s ease both;
}
.hero-title {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}
.hero-title-line {
  font-family: var(--px-font-display);
  font-weight: 900;
  font-size: clamp(48px, 6vw, 80px);
  line-height: 1.0;
  color: var(--px-text-primary);
  animation: reveal-up 0.7s ease both;
}
.hero-title-line.accent {
  color: transparent;
  -webkit-text-stroke: 2px var(--coral);
  animation-delay: 0.08s;
}
.hero-desc {
  font-size: 15px;
  color: var(--px-text-tertiary);
  line-height: 1.7;
  margin-bottom: 32px;
  max-width: 340px;
  animation: reveal-up 0.7s 0.15s ease both;
}
.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
  animation: reveal-up 0.7s 0.22s ease both;
}
.hero-btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 28px;
  background: var(--coral); color: #fff;
  border: none; border-radius: var(--px-radius-round);
  font-size: 14px; font-weight: 600;
  font-family: var(--px-font-family);
  cursor: pointer; transition: all var(--px-transition-base);
  letter-spacing: 0.3px;
}
.hero-btn-primary:hover {
  background: var(--px-blue-hover);
  transform: translateY(-2px);
  box-shadow: 0 8px 28px var(--coral-glow);
}
.hero-btn-ghost {
  display: inline-flex; align-items: center;
  padding: 12px 28px;
  background: transparent;
  color: var(--px-text-secondary);
  border: 1px solid var(--px-border);
  border-radius: var(--px-radius-round);
  font-size: 14px; font-weight: 500;
  font-family: var(--px-font-family);
  cursor: pointer; transition: all var(--px-transition-base);
}
.hero-btn-ghost:hover {
  border-color: var(--px-text-tertiary);
  color: var(--px-text-primary);
  transform: translateY(-2px);
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  animation: reveal-up 0.7s 0.3s ease both;
}
.hero-stat { display: flex; flex-direction: column; gap: 2px; }
.stat-num { font-family: var(--px-font-display); font-size: 22px; font-weight: 700; color: var(--px-text-primary); }
.stat-label { font-size: 11px; color: var(--px-text-tertiary); letter-spacing: 0.5px; }
.stat-divider { width: 1px; height: 32px; background: var(--px-border); }

/* Mosaic */
.hero-mosaic {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 140px);
  gap: 8px;
  animation: reveal-fade 1s 0.3s ease both;
}
.mosaic-item {
  position: relative;
  border-radius: var(--px-radius-md);
  overflow: hidden;
  cursor: pointer;
  background: var(--ink-600);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.mosaic-0 { grid-row: 1 / 3; }
.mosaic-item:hover { transform: scale(1.03); box-shadow: 0 12px 32px rgba(0,0,0,0.5); z-index: 2; }
.mosaic-img { width: 100%; height: 100%; object-fit: cover; }
.mosaic-shine {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.06) 0%, transparent 60%);
  pointer-events: none;
}

/* Scroll hint */
.scroll-hint {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding-bottom: 32px;
  opacity: 0.5;
}
.scroll-line {
  width: 1px; height: 48px;
  background: linear-gradient(to bottom, transparent, var(--coral));
  animation: pulse-line 2s ease-in-out infinite;
}
.scroll-label { font-size: 10px; letter-spacing: 2px; color: var(--px-text-tertiary); font-family: var(--px-font-mono); }
@keyframes pulse-line { 0%,100%{opacity:0.3} 50%{opacity:1} }

/* ===== CONTEST ===== */
.contest-section {
  padding: 80px 0;
  background: var(--ink-800);
  border-top: 1px solid var(--px-border);
  border-bottom: 1px solid var(--px-border);
}
.section-wrap { max-width: var(--px-max-width); margin: 0 auto; padding: 0 24px; }
.section-head {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 36px;
}
.section-label {
  font-size: 10px; font-weight: 700;
  letter-spacing: 3px; text-transform: uppercase;
  color: var(--coral); font-family: var(--px-font-mono);
  padding-bottom: 4px;
}
.section-title {
  font-family: var(--px-font-display);
  font-size: clamp(24px, 3vw, 36px);
  font-weight: 700;
  color: var(--px-text-primary);
  flex: 1;
}
.section-more {
  font-size: 12px; font-weight: 500;
  color: var(--px-text-tertiary);
  text-decoration: none;
  transition: color var(--px-transition-fast);
  padding-bottom: 4px;
  letter-spacing: 0.3px;
}
.section-more:hover { color: var(--coral); }
.contest-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.contest-card {
  background: var(--ink-700);
  border-radius: var(--px-radius-lg);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--px-border);
  transition: all var(--px-transition-base);
}
.contest-card:hover { transform: translateY(-6px); box-shadow: 0 16px 40px rgba(0,0,0,0.5); border-color: var(--ink-400); }
.contest-thumb { position: relative; width: 100%; padding-bottom: 70%; overflow: hidden; }
.contest-thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s ease; }
.contest-card:hover .contest-thumb img { transform: scale(1.06); }
.contest-meta { position: absolute; bottom: 0; left: 0; right: 0; display: flex; justify-content: space-between; align-items: flex-end; padding: 24px 10px 8px; background: linear-gradient(transparent, rgba(0,0,0,0.7)); }
.contest-badge { font-size: 10px; font-weight: 700; color: var(--amber); background: var(--amber-glow); padding: 2px 8px; border-radius: 3px; font-family: var(--px-font-mono); }
.contest-score { font-size: 12px; font-weight: 600; color: var(--amber); }
.contest-info { padding: 12px 12px 14px; }
.contest-name { font-size: 13px; font-weight: 600; color: var(--px-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.contest-artist { font-size: 11px; color: var(--px-text-tertiary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ===== ARTWORKS ===== */
.artworks-section { padding: 80px 0 96px; background: var(--px-bg-secondary); }
.artwork-grid { column-count: 5; column-gap: 14px; }

/* Skeleton */
.skeleton-card { border-radius: var(--px-radius-md); overflow: hidden; background: var(--ink-700); break-inside: avoid; margin-bottom: 14px; border: 1px solid var(--px-border); }
.skeleton-thumb { width: 100%; padding-bottom: 100%; background: linear-gradient(90deg, var(--ink-600) 25%, var(--ink-500) 50%, var(--ink-600) 75%); background-size: 200% 100%; animation: shimmer 1.8s infinite; }
.skeleton-info { padding: 10px 11px 12px; display: flex; flex-direction: column; gap: 6px; }
.skeleton-line { height: 12px; border-radius: 3px; background: var(--ink-600); }
.w70 { width: 70%; }
.w40 { width: 40%; }

/* Empty */
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 80px 0; color: var(--px-text-tertiary); font-size: 14px; }

/* Footer */
.site-footer { border-top: 1px solid var(--px-border); background: var(--ink-900); padding: 28px 0; }
.footer-inner { max-width: var(--px-max-width); margin: 0 auto; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; }
.footer-logo { font-family: var(--px-font-display); font-weight: 700; font-style: italic; font-size: 18px; color: var(--px-text-secondary); }
.footer-logo-mark { color: var(--coral); font-weight: 900; }
.footer-copy { font-size: 11px; color: var(--px-text-tertiary); }

/* Responsive */
@media (max-width: 1200px) {
  .artwork-grid { column-count: 4; }
  .contest-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 900px) {
  .hero-inner { flex-direction: column; padding: 80px 24px 60px; gap: 40px; }
  .hero-text { flex: none; text-align: center; }
  .hero-desc { max-width: none; }
  .hero-actions, .hero-stats { justify-content: center; }
  .hero-mosaic { grid-template-rows: repeat(2, 110px); width: 100%; }
  .artwork-grid { column-count: 3; }
  .contest-grid { grid-template-columns: repeat(2, 1fr); }
  .section-head { flex-wrap: wrap; }
}
@media (max-width: 640px) {
  .hero-mosaic { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 90px); }
  .mosaic-0 { grid-row: auto; }
  .mosaic-item:nth-child(n+5) { display: none; }
  .artwork-grid { column-count: 2; column-gap: 10px; }
  .contest-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>