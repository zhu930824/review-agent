import { createRouter, createWebHistory } from 'vue-router'
import { getStoredAuthToken } from '@/utils/authStorage'
import { canAccess, loadAccessProfile } from '@/composables/useAccess'
import type { PlatformPermission } from '@/types/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/login.vue'), meta: { guest: true } },
    { path: '/register', name: 'register', component: () => import('../views/register.vue'), meta: { guest: true } },
    {
      path: '/', component: () => import('../views/layouts/DefaultLayout.vue'),
      children: [
        { path: '', name: 'dashboard', component: () => import('../views/dashboard.vue') },
        { path: 'projects', name: 'projects', component: () => import('../views/projects/index.vue') },
        { path: 'projects/create', name: 'project-create', component: () => import('../views/projects/create.vue') },
        { path: 'projects/:id', name: 'project-detail', component: () => import('../views/projects/detail.vue') },
        { path: 'reviews/create', name: 'review-create', component: () => import('../views/reviews/create.vue') },
        { path: 'reviews/:id', name: 'review-detail', component: () => import('../views/reviews/detail.vue') },
        { path: 'governance', name: 'governance', component: () => import('../views/governance.vue'), meta: { permission: 'GOVERNANCE_VIEW' } },
        { path: 'operations', name: 'operations', component: () => import('../views/operations.vue'), meta: { permission: 'OPERATIONS_VIEW' } },
        { path: 'settings/models', name: 'models', component: () => import('../views/models.vue'), meta: { permission: 'MODEL_MANAGE' } },
        { path: 'knowledge', name: 'knowledge', component: () => import('../views/knowledge.vue') },
        { path: 'gateway', name: 'gateway', component: () => import('../views/gateway.vue'), meta: { permission: 'MODEL_MANAGE' } },
      ],
    },
  ],
})

router.beforeEach(async (to, _from) => {
  const token = getStoredAuthToken()
  if (to.meta.guest) return true
  if (!token && to.name !== 'login') return '/login'
  const permission = to.meta.permission as PlatformPermission | undefined
  if (permission) {
    await loadAccessProfile()
    if (!canAccess(permission)) return '/'
  }
  return true
})

export default router
