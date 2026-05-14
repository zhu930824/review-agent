<template>
  <UCard v-if="review">
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ review.projectName ?? '未知项目' }}</h3>
        <p class="text-sm text-gray-500">{{ review.sourceBranch }} → {{ review.targetBranch }}</p>
      </div>
      <UBadge :color="statusColor(review.status)" :label="statusLabel(review.status)" variant="subtle" />
    </div>
    <div class="mt-3 flex items-center gap-3 text-sm text-gray-500">
      <span>模式：{{ review.reviewMode }}</span>
      <span v-if="review.createdAt">{{ review.createdAt }}</span>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { Review } from '~/types/review'

defineProps<{ review: Review | null }>()

function statusColor(status: string): string {
  const map: Record<string, string> = { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }
  return map[status] ?? 'gray'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] ?? status
}
</script>
