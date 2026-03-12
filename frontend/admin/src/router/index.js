import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/UsersView.vue')
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('../views/ApplicationsView.vue')
      },
      {
        path: 'artworks',
        name: 'Artworks',
        component: () => import('../views/ArtworksView.vue')
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('../views/AuditLogsView.vue')
      },
      {
        path: 'coupons',
        name: 'Coupons',
        component: () => import('../views/CouponsView.vue')
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('../views/CommentsView.vue')
      },
      {
        path: 'commissions',
        name: 'AdminCommissions',
        component: () => import('../views/CommissionsView.vue')
      },
      {
        path: 'payments',
        name: 'Payments',
        component: () => import('../views/PaymentsView.vue')
      },
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('../views/FinanceView.vue')
      },
      {
        path: 'contests',
        name: 'Contests',
        component: () => import('../views/ContestsView.vue')
      },
      {
        path: 'membership',
        name: 'Membership',
        component: () => import('../views/MembershipView.vue')
      },
      {
        path: 'feedback',
        name: 'Feedback',
        component: () => import('../views/FeedbackView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫：检查认证状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
