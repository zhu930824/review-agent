<template>
  <RouterLink :to="`/projects/${project.id}`" style="display: block; text-decoration: none; color: inherit">
    <a-card hoverable :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
      <a-space direction="vertical" size="small" style="width: 100%">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <a-space :size="12">
            <div style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: #eef2ff; color: #4f46e5">
              <FolderOutlined style="font-size: 20px" />
            </div>
            <a-typography-title :level="5" ellipsis style="margin: 0; max-width: 200px">
              {{ project.name }}
            </a-typography-title>
          </a-space>
          <RightOutlined style="font-size: 14px; color: #cbd5e1" />
        </div>

        <div style="padding-left: 52px">
          <a-typography-paragraph
            ellipsis
            type="secondary"
            style="margin-bottom: 8px; font-family: monospace; font-size: 13px"
          >
            {{ project.repoUrl }}
          </a-typography-paragraph>

          <a-space :size="8">
            <a-tag :color="statusTagColor(project.status)">{{ statusLabel(project.status) }}</a-tag>
            <a-typography-text type="secondary" style="font-family: monospace; font-size: 12px">
              {{ project.defaultBranch }}
            </a-typography-text>
          </a-space>
        </div>
      </a-space>
    </a-card>
  </RouterLink>
</template>

<script setup lang="ts">
import { FolderOutlined, RightOutlined } from '@ant-design/icons-vue'
import type { Project } from '~/types/project'

defineProps<{ project: Project }>()

function statusTagColor(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'gold',
    CLONING: 'processing',
    READY: 'success',
    ERROR: 'error',
  }
  return map[status] ?? 'default'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', CLONING: '克隆中', READY: '就绪', ERROR: '错误' }
  return map[status] ?? status
}
</script>
