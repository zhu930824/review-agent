<template>
  <div v-if="review" class="stat-card animate-slide-up">
    <div class="flex items-center justify-between p-5">
      <div class="flex items-center gap-4">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-md shadow-indigo-500/25">
          <UIcon name="i-heroicons-document-magnifying-glass" class="h-5 w-5 text-white" />
        </div>
        <div>
          <h3 class="text-base font-bold text-slate-800 dark:text-white">{{ review.projectName ?? '未知项目' }}</h3>
          <p class="text-sm text-slate-500 mono">{{ review.sourceBranch }} → {{ review.targetBranch }}</p>
        </div>
      </div>
      <UBadge :color="statusColor(review.status)" :label="statusLabel(review.status)" variant="subtle" size="sm" />
    </div>
    <div class="px-5 pb-5 flex items-center gap-4 text-sm text-slate-500">
      <span class="flex items-center gap-1.5 font-medium"><UIcon name="i-heroicons-cpu-chip" class="h-4 w-4" />模式：{{ review.reviewMode }}</span>
      <span v-if="review.createdAt" class="flex items-center gap-1.5 font-medium"><UIcon name="i-heroicons-clock" class="h-4 w-4" />{{ review.createdAt }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Review } from '~/types/review'

defineProps<{ review: Review | null }>()

function statusColor(status: string): string { return { PENDING: 'amber', RUNNING: 'blue', COMPLETED: 'emerald', FAILED: 'red' }[status] ?? 'slate' }
function statusLabel(status: string): string { return { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }[status] ?? status }
</script>