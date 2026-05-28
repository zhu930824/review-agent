<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider width="220" style="background: #001529">
      <div style="height: 64px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(255,255,255,0.1)">
        <div style="display: flex; align-items: center; gap: 10px">
          <SafetyCertificateOutlined style="font-size: 24px; color: #4F46E5" />
          <div>
            <div style="color: #fff; font-weight: 700; font-size: 15px; line-height: 1.2">Review Agent</div>
            <div style="color: rgba(255,255,255,0.45); font-size: 11px">AI Code Review</div>
          </div>
        </div>
      </div>

      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline" :style="{ borderRight: 0 }" @click="navigate">
        <a-menu-item key="/"><template #icon><DashboardOutlined /></template><span>仪表盘</span></a-menu-item>
        <a-menu-item key="projects"><template #icon><FolderOutlined /></template><span>项目管理</span></a-menu-item>
        <a-menu-item key="review-create"><template #icon><PlayCircleOutlined /></template><span>发起审查</span></a-menu-item>
        <a-menu-item key="governance"><template #icon><AppstoreOutlined /></template><span>治理中心</span></a-menu-item>
        <a-menu-item key="operations"><template #icon><PieChartOutlined /></template><span>运营中心</span></a-menu-item>
        <a-menu-item key="knowledge"><template #icon><GlobalOutlined /></template><span>知识图谱</span></a-menu-item>
        <a-menu-item key="gateway"><template #icon><ApiOutlined /></template><span>AI Gateway</span></a-menu-item>
        <a-menu-item key="models"><template #icon><SettingOutlined /></template><span>模型配置</span></a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f0f0f0">
        <a-breadcrumb>
          <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item">{{ item }}</a-breadcrumb-item>
        </a-breadcrumb>
        <a-button type="text" @click="logout"><LogoutOutlined /> 退出</a-button>
      </a-layout-header>
      <a-layout-content style="padding: 24px; background: #f5f5f5">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  SafetyCertificateOutlined, DashboardOutlined, FolderOutlined, PlayCircleOutlined,
  AppstoreOutlined, PieChartOutlined, GlobalOutlined, ApiOutlined, SettingOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const breadcrumbMap: Record<string, string> = {
  '': '仪表盘', projects: '项目管理', reviews: '审查详情',
  governance: '治理中心', operations: '运营中心',
  knowledge: '知识图谱', gateway: 'AI Gateway',
  settings: '模型配置', create: '新建', models: '配置'
}

const breadcrumbItems = computed(() => {
  return route.path.split('/').filter(Boolean).map(p => breadcrumbMap[p] || p)
})

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

function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>
