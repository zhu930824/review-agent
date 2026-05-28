<template>
  <a-layout style="min-height: 100vh; background: transparent">
    <!-- 浅色玻璃侧边栏 -->
    <a-layout-sider width="220" style="background: rgba(255,255,255,0.75); backdrop-filter: blur(20px); border-right: 1px solid rgba(255,255,255,0.9); box-shadow: 2px 0 24px rgba(0,0,0,0.04)">
      <div style="height: 64px; display: flex; align-items: center; padding: 0 20px; border-bottom: 1px solid rgba(0,0,0,0.06)">
        <div style="display: flex; align-items: center; gap: 10px">
          <div style="width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, #6366f1, #8b5cf6); display: flex; align-items: center; justify-content: center">
            <SafetyCertificateOutlined style="font-size: 18px; color: #fff" />
          </div>
          <div>
            <div style="font-weight: 700; font-size: 15px; color: #1e293b; line-height: 1.2">Review Agent</div>
            <div style="font-size: 11px; color: #94a3b8">AI Code Review</div>
          </div>
        </div>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        :style="{ borderRight: 0, background: 'transparent', marginTop: '8px' }"
        @click="navigate"
      >
        <a-menu-item key="/" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><DashboardOutlined /></template>
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="projects" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><FolderOutlined /></template>
          <span>项目管理</span>
        </a-menu-item>
        <a-menu-item key="review-create" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><PlayCircleOutlined /></template>
          <span>发起审查</span>
        </a-menu-item>
        <a-menu-item key="governance" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><AppstoreOutlined /></template>
          <span>治理中心</span>
        </a-menu-item>
        <a-menu-item key="operations" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><PieChartOutlined /></template>
          <span>运营中心</span>
        </a-menu-item>
        <a-menu-item key="knowledge" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><GlobalOutlined /></template>
          <span>知识图谱</span>
        </a-menu-item>
        <a-menu-item key="gateway" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><ApiOutlined /></template>
          <span>AI Gateway</span>
        </a-menu-item>
        <a-menu-item key="models" style="border-radius: 10px; margin: 2px 12px">
          <template #icon><SettingOutlined /></template>
          <span>模型配置</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout style="background: transparent">
      <!-- 玻璃顶栏 -->
      <a-layout-header style="background: rgba(255,255,255,0.6); backdrop-filter: blur(16px); padding: 0 24px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid rgba(255,255,255,0.8); height: 56px">
        <a-breadcrumb>
          <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item">{{ item }}</a-breadcrumb-item>
        </a-breadcrumb>
        <a-button type="text" @click="logout" style="color: #64748b"><LogoutOutlined /> 退出</a-button>
      </a-layout-header>
      <!-- 内容区 -->
      <a-layout-content style="padding: 24px; background: transparent; min-height: calc(100vh - 56px)">
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

const route = useRoute()
const router = useRouter()

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

function logout() {
  localStorage.removeItem('review-agent-token')
  router.push('/login')
}
</script>
