import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Auth/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Auth/RegisterView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue')
      },
      {
        path: '/market',
        name: 'Market',
        component: () => import('@/views/Market/MarketListView.vue')
      },
      {
        path: '/market/:id',
        name: 'MarketDetail',
        component: () => import('@/views/Market/MarketDetailView.vue')
      },
      {
        path: '/market/publish',
        name: 'MarketPublish',
        component: () => import('@/views/Market/MarketPublishView.vue')
      },
      {
        path: '/tasks',
        name: 'Task',
        component: () => import('@/views/Task/TaskListView.vue')
      },
      {
        path: '/tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/views/Task/TaskDetailView.vue')
      },
      {
        path: '/tasks/publish',
        name: 'TaskPublish',
        component: () => import('@/views/Task/TaskPublishView.vue')
      },
      {
        path: '/clubs',
        name: 'Club',
        component: () => import('@/views/Activity/ClubListView.vue')
      },
      {
        path: '/clubs/:id',
        name: 'ClubDetail',
        component: () => import('@/views/Activity/ClubDetailView.vue')
      },
      {
        path: '/activities',
        name: 'Activity',
        component: () => import('@/views/Activity/ActivityListView.vue')
      },
      {
        path: '/activities/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/Activity/ActivityDetailView.vue')
      },
      {
        path: '/notifications',
        name: 'Notification',
        component: () => import('@/views/Notification/NotificationView.vue')
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Auth/ProfileView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth

  if (requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (!requiresAuth && userStore.isLoggedIn && to.path === '/login') {
    next('/')
  } else {
    next()
  }
})

export default router
