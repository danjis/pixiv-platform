import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false }
  },
  // 使用 MainLayout 包裹的页面
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'artworks',
        name: 'Artworks',
        component: () => import('@/views/artwork/ArtworkListView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'following',
        name: 'Following',
        component: () => import('@/views/feed/FeedView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'ranking',
        name: 'Ranking',
        component: () => import('@/views/ranking/RankingView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'artworks/:id',
        name: 'ArtworkDetail',
        component: () => import('@/views/artwork/ArtworkDetailView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'artist/:id',
        name: 'ArtistProfile',
        component: () => import('@/views/artist/ArtistProfileView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'publish',
        name: 'PublishArtwork',
        component: () => import('@/views/artwork/PublishArtworkView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'artworks/:id/edit',
        name: 'EditArtwork',
        component: () => import('@/views/artwork/PublishArtworkView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'commissions',
        name: 'Commissions',
        component: () => import('@/views/commission/CommissionView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'commission/create',
        name: 'CreateCommission',
        component: () => import('@/views/commission/CreateCommissionView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'commission/pay-result',
        name: 'PayResult',
        component: () => import('@/views/commission/PayResultView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'commission/:id',
        name: 'CommissionDetail',
        component: () => import('@/views/commission/CommissionDetailView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'chat/:id',
        name: 'ChatDetail',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/notification/NotificationView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'history',
        name: 'BrowsingHistory',
        component: () => import('@/views/history/BrowsingHistoryView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'coupons',
        name: 'MyCoupons',
        component: () => import('@/views/coupon/CouponView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('@/views/order/OrderHistoryView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'membership',
        name: 'Membership',
        component: () => import('@/views/membership/MembershipView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'membership/pay-result',
        name: 'MembershipPayResult',
        component: () => import('@/views/membership/MembershipPayResultView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'contests',
        name: 'Contests',
        component: () => import('@/views/contest/ContestListView.vue'),
        meta: { requiresAuth: false }
      },
      {
        path: 'contests/:id',
        name: 'ContestDetail',
        component: () => import('@/views/contest/ContestDetailView.vue'),
        meta: { requiresAuth: false }
      },
      // 兼容旧通知链接 /commissions/:id → /commission/:id
      {
        path: 'commissions/:id',
        redirect: to => ({ path: `/commission/${to.params.id}` })
      }
    ]
  },
  // 创作者中心（画师专属，独立布局）
  {
    path: '/studio',
    component: () => import('@/layout/StudioLayout.vue'),
    meta: { requiresAuth: true, requiresArtist: true },
    children: [
      {
        path: '',
        name: 'StudioDashboard',
        component: () => import('@/views/studio/StudioDashboard.vue')
      },
      {
        path: 'artworks',
        name: 'StudioArtworks',
        component: () => import('@/views/studio/StudioArtworks.vue')
      },
      {
        path: 'commissions',
        name: 'StudioCommissions',
        component: () => import('@/views/studio/StudioCommissions.vue')
      },
      {
        path: 'earnings',
        name: 'StudioEarnings',
        component: () => import('@/views/studio/StudioEarnings.vue')
      },
      {
        path: 'settings',
        name: 'StudioSettings',
        component: () => import('@/views/studio/StudioSettings.vue')
      },
      {
        path: 'plans',
        name: 'StudioPlans',
        component: () => import('@/views/studio/StudioPlans.vue')
      }
    ]
  },
  // 404 兜底路由 — 必须放在最后
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'NotFound',
        component: () => import('@/views/NotFoundView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查认证状态
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    // 需要认证但未登录，跳转到登录页，携带完整目标路径（含 query）
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresArtist && !userStore.isArtist) {
    // 需要画师权限但不是画师，跳转到首页
    next({ name: 'Home' })
  } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isAuthenticated) {
    // 已登录用户访问登录/注册页，跳转到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
