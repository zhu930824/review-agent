<template>
  <div class="min-w-0 flex-1 animate-slide-up">
    <NuxtLink :to="`/projects/${project.id}`" class="group block">
      <div class="stat-card cursor-pointer">
        <div class="flex items-start justify-between">
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-3">
              <div class="icon-soft icon-soft-primary" style="width:40px;height:40px;border-radius:12px;">
                <UIcon name="i-heroicons-folder" class="h-5 w-5" />
              </div>
              <h3 class="text-base font-bold text-slate-800 group-hover:text-indigo-600 dark:text-white dark:group-hover:text-indigo-400 transition-colors truncate">
                {{ project.name }}
              </h3>
            </div>
            <p class="mt-2 truncate text-sm text-slate-500 pl-13 mono">{{ project.repoUrl }}</p>
            <div class="mt-3 flex items-center gap-2 pl-13">
              <span class="badge-soft" :class="statusBadgeClass(project.status)">{{ statusLabel(project.status) }}</span>
              <span class="text-xs text-slate-400 font-medium mono">{{ project.defaultBranch }}</span>
            </div>
          </div>
          <UIcon name="i-heroicons-chevron-right" class="h-5 w-5 text-slate-300 group-hover:text-indigo-500 group-hover:translate-x-1 transition-all mt-2" />
        </div>
      </div>
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'

defineProps<{ project: Project }>()

function statusBadgeClass(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'badge-soft-warning',
    CLONING: 'badge-soft-info',
    READY: 'badge-soft-success',
    ERROR: 'badge-soft-error',
  }
  return map[status] ?? 'badge-soft-info'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', CLONING: '克隆中', READY: '就绪', ERROR: '错误' }
  return map[status] ?? status
}
</script>