<template>
  <UCard :ui="{ body: { padding: 'p-4' } }">
    <div class="flex items-start justify-between">
      <div class="min-w-0 flex-1">
        <NuxtLink :to="`/projects/${project.id}`" class="text-base font-semibold text-gray-900 hover:text-primary-500 dark:text-white">
          {{ project.name }}
        </NuxtLink>
        <p class="mt-1 truncate text-sm text-gray-500">{{ project.repoUrl }}</p>
        <div class="mt-2 flex items-center gap-2">
          <UBadge :color="statusColor(project.status)" :label="statusLabel(project.status)" variant="subtle" size="xs" />
          <span class="text-xs text-gray-400">{{ project.defaultBranch }}</span>
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'

defineProps<{ project: Project }>()

function statusColor(status: string): string {
  const map: Record<string, string> = { PENDING: 'orange', CLONING: 'blue', READY: 'green', ERROR: 'red' }
  return map[status] ?? 'gray'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', CLONING: '克隆中', READY: '就绪', ERROR: '错误' }
  return map[status] ?? status
}
</script>
