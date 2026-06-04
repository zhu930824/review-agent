import { createRouter, createWebHistory } from 'vue-router'
import { getStoredAuthToken } from '@/utils/authStorage'
import { resolveAuthRedirect } from './authGuard'

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
        { path: 'governance', name: 'governance', component: () => import('../views/governance.vue') },
        { path: 'operations', name: 'operations', component: () => import('../views/operations.vue') },
        { path: 'settings/models', name: 'models', component: () => import('../views/models.vue') },
        { path: 'knowledge', name: 'knowledge', component: () => import('../views/knowledge.vue') },
        { path: 'gateway', name: 'gateway', component: () => import('../views/gateway.vue') },
      ],
    },
  ],
})

router.beforeEach((to, _from) => {
  const token = getStoredAuthToken()
  return resolveAuthRedirect(to, token)
})

export default router
