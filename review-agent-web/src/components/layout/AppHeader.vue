<template>
  <div style="display: flex; flex: 1; align-items: center; justify-content: space-between">
    <div>
      <a-breadcrumb v-if="breadcrumbItems.length > 1">
        <a-breadcrumb-item v-for="item in breadcrumbItems" :key="item">
          {{ item }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    <div style="display: flex; align-items: center; gap: 12px">
      <a-button type="text" @click="handleLogout">
        <template #icon><LogoutOutlined /></template>
        退出
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { LogoutOutlined } from '@ant-design/icons-vue'

const route = useRoute()

const breadcrumbItems = computed(() => {
  const parts = route.path.split('/').filter(Boolean)
  if (!parts.length) return ['仪表盘']
  return parts.map(p => {
    const map: Record<string, string> = {
      projects: '项目管理', reviews: '审查详情',
      governance: '治理中心', operations: '运营中心',
      knowledge: '知识图谱', gateway: 'AI Gateway',
      settings: '模型配置', create: '新建', models: '配置'
    }
    return map[p] || p
  })
})

const { logout: logoutAuth } = useAuth()

async function handleLogout() {
  await logoutAuth()
}
</script>
