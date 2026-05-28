<template>
  <a-card v-if="review" hoverable style="margin-bottom: 16px">
    <div style="display: flex; align-items: center; justify-content: space-between">
      <div style="display: flex; align-items: center; gap: 16px">
        <div style="width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #4F46E5, #7C3AED); display: flex; align-items: center; justify-content: center">
          <FileSearchOutlined style="font-size: 20px; color: #fff" />
        </div>
        <div>
          <h3 style="margin: 0; font-size: 16px; font-weight: 700">{{ review.projectName ?? '未知项目' }}</h3>
          <p style="margin: 2px 0 0; font-size: 13px; color: #999; font-family: monospace">{{ review.sourceBranch }} → {{ review.targetBranch }}</p>
        </div>
      </div>
      <a-tag :color="statusColor(review.status)">{{ statusLabel(review.status) }}</a-tag>
    </div>
    <div style="display: flex; gap: 16px; margin-top: 12px; font-size: 13px; color: #999">
      <span><ApiOutlined style="margin-right: 4px" />模式：{{ review.reviewMode }}</span>
      <span v-if="review.createdAt"><ClockCircleOutlined style="margin-right: 4px" />{{ review.createdAt }}</span>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import type { Review } from '~/types/review'
import { FileSearchOutlined, ApiOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'

defineProps<{ review: Review | null }>()

function statusColor(status: string): string {
  return { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }[status] ?? 'default'
}
function statusLabel(status: string): string {
  return { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }[status] ?? status
}
</script>
