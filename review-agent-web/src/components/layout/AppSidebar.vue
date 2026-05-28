<template>
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

    <a-menu
      v-model:selectedKeys="selectedKeys"
      theme="dark"
      mode="inline"
      :style="{ borderRight: 0 }"
      @click="navigate"
    >
      <a-menu-item key="/">
        <template #icon><DashboardOutlined /></template>
        <span>仪表盘</span>
      </a-menu-item>
      <a-menu-item key="/projects">
        <template #icon><FolderOutlined /></template>
        <span>项目管理</span>
      </a-menu-item>
      <a-menu-item key="/reviews/create">
        <template #icon><PlayCircleOutlined /></template>
        <span>发起审查</span>
      </a-menu-item>
      <a-menu-item key="/governance">
        <template #icon><AppstoreOutlined /></template>
        <span>治理中心</span>
      </a-menu-item>
      <a-menu-item key="/operations">
        <template #icon><PieChartOutlined /></template>
        <span>运营中心</span>
      </a-menu-item>
      <a-menu-item key="/knowledge">
        <template #icon><GlobalOutlined /></template>
        <span>知识图谱</span>
      </a-menu-item>
      <a-menu-item key="/gateway">
        <template #icon><ApiOutlined /></template>
        <span>AI Gateway</span>
      </a-menu-item>
      <a-menu-item key="/settings/models">
        <template #icon><SettingOutlined /></template>
        <span>模型配置</span>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>
</template>

<script setup lang="ts">
import {
  DashboardOutlined, FolderOutlined, PlayCircleOutlined, AppstoreOutlined,
  PieChartOutlined, GlobalOutlined, ApiOutlined, SettingOutlined,
  SafetyCertificateOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const selectedKeys = ref<string[]>([route.path.startsWith('/projects') ? '/projects'
  : route.path.startsWith('/reviews/create') ? '/reviews/create'
  : route.path.startsWith('/governance') ? '/governance'
  : route.path.startsWith('/operations') ? '/operations'
  : route.path.startsWith('/knowledge') ? '/knowledge'
  : route.path.startsWith('/gateway') ? '/gateway'
  : route.path.startsWith('/settings') ? '/settings/models'
  : '/'])

function navigate({ key }: { key: string }) {
  router.push(key)
}
</script>
