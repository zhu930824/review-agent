<template>
  <a-layout class="app-shell">
    <a-layout-sider class="app-sidebar" width="232">
      <div class="app-sidebar-brand">
        <div style="display: flex; align-items: center; gap: 10px">
          <div class="app-logo">
            <SafetyCertificateOutlined />
          </div>
          <div>
            <div class="app-title">Review Agent</div>
            <div class="app-subtitle">AI Code Review</div>
          </div>
        </div>
      </div>

      <a-menu
        class="app-menu"
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        @click="navigate"
      >
        <a-menu-item key="/">
          <template #icon><DashboardOutlined /></template>
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="projects">
          <template #icon><FolderOutlined /></template>
          <span>项目管理</span>
        </a-menu-item>
        <a-menu-item key="review-create">
          <template #icon><PlayCircleOutlined /></template>
          <span>发起审查</span>
        </a-menu-item>
        <a-menu-item key="governance">
          <template #icon><AppstoreOutlined /></template>
          <span>治理中心</span>
        </a-menu-item>
        <a-menu-item key="operations">
          <template #icon><PieChartOutlined /></template>
          <span>运营中心</span>
        </a-menu-item>
        <a-menu-item key="knowledge">
          <template #icon><GlobalOutlined /></template>
          <span>知识图谱</span>
        </a-menu-item>
        <a-menu-item key="gateway">
          <template #icon><ApiOutlined /></template>
          <span>AI Gateway</span>
        </a-menu-item>
        <a-menu-item key="models">
          <template #icon><SettingOutlined /></template>
          <span>模型配置</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout class="app-main-layout">
      <a-layout-header class="app-header">
        <a-breadcrumb>
          <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item">{{ item }}</a-breadcrumb-item>
        </a-breadcrumb>
        <a-button type="text" @click="handleLogout" style="color: var(--ra-color-text-muted)"><LogoutOutlined /> 退出</a-button>
      </a-layout-header>
      <a-layout-content class="app-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  SafetyCertificateOutlined, DashboardOutlined, FolderOutlined, PlayCircleOutlined,
  AppstoreOutlined, PieChartOutlined, GlobalOutlined, ApiOutlined, SettingOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { logout: logoutAuth } = useAuth()

const breadcrumbMap: Record<string, string> = {
  '': '仪表盘', projects: '项目管理', reviews: '审查详情',
  governance: '治理中心', operations: '运营中心',
  knowledge: '知识图谱', gateway: 'AI Gateway',
  settings: '模型配置', create: '新建', models: '配置'
}

const breadcrumbItems = computed(() =>
  route.path.split('/').filter(Boolean).map(p => breadcrumbMap[p] || p)
)

const selectedKeys = computed(() => {
  const p = route.path
  if (p.startsWith('/projects')) return ['projects']
  if (p.startsWith('/reviews/create')) return ['review-create']
  return [p.replace('/', '') || '/']
})

function navigate({ key }: { key: string }) {
  const map: Record<string, string> = {
    '/': '/', projects: '/projects', 'review-create': '/reviews/create',
    governance: '/governance', operations: '/operations',
    knowledge: '/knowledge', gateway: '/gateway', models: '/settings/models'
  }
  router.push(map[key] || '/')
}

async function handleLogout() {
  await logoutAuth()
}
</script>
