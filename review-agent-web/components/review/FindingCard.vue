<template>
  <div class="rounded-lg border p-4" :class="borderColor">
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <div class="flex items-center gap-2">
          <UBadge :color="severityColor" :label="finding.severity" variant="subtle" size="xs" />
          <UBadge :color="categoryColor" :label="categoryLabel" variant="subtle" size="xs" />
          <UBadge v-if="finding.isCrossHit" color="purple" label="交叉命中" variant="subtle" size="xs" />
          <span v-if="finding.modelName" class="text-xs text-gray-400">{{ finding.modelName }}</span>
        </div>
        <h4 class="mt-2 text-sm font-semibold text-gray-900 dark:text-white">{{ finding.title }}</h4>
        <p class="mt-1 text-sm text-gray-500">{{ finding.filePath }}<span v-if="finding.lineStart">:{{ finding.lineStart }}</span></p>
      </div>
      <div class="flex items-center gap-1">
        <UButton v-if="finding.humanStatus === 'PENDING'" variant="ghost" color="green" size="xs" icon="i-heroicons-check" @click="$emit('confirm')" />
        <UButton v-if="finding.humanStatus === 'PENDING'" variant="ghost" color="red" size="xs" icon="i-heroicons-x-mark" @click="$emit('dismiss')" />
        <UBadge v-else :color="finding.humanStatus === 'CONFIRMED' ? 'green' : 'gray'" :label="finding.humanStatus === 'CONFIRMED' ? '已确认' : '已忽略'" variant="subtle" size="xs" />
      </div>
    </div>
    <p v-if="finding.description && expanded" class="mt-2 text-sm text-gray-600 dark:text-gray-300">{{ finding.description }}</p>
    <p v-if="finding.suggestion && expanded" class="mt-1 text-sm text-blue-600 dark:text-blue-400">建议：{{ finding.suggestion }}</p>
    <UButton variant="ghost" color="gray" size="xs" :label="expanded ? '收起' : '展开'" @click="expanded = !expanded" />
  </div>
</template>

<script setup lang="ts">
import type { ReviewFinding, FindingCategory } from '~/types/review'

const props = defineProps<{ finding: ReviewFinding }>()
defineEmits<{ confirm: []; dismiss: [] }>()

const expanded = ref(false)

const severityColor = computed(() => {
  const map: Record<string, string> = { BLOCKER: 'red', MAJOR: 'orange', MINOR: 'yellow', INFO: 'blue' }
  return map[props.finding.severity] ?? 'gray'
})

const borderColor = computed(() => {
  const map: Record<string, string> = { BLOCKER: 'border-red-300', MAJOR: 'border-orange-300', MINOR: 'border-yellow-300', INFO: 'border-blue-200' }
  return map[props.finding.severity] ?? 'border-gray-200'
})

const categoryColor = computed(() => {
  const map: Record<string, string> = { BUG: 'red', SECURITY: 'purple', PERFORMANCE: 'orange', CODE_STYLE: 'gray', EXCEPTION_HANDLING: 'yellow' }
  return map[props.finding.category] ?? 'gray'
})

const categoryLabel = computed(() => {
  const map: Record<string, string> = { CODE_STYLE: '代码风格', BUG: '潜在缺陷', PERFORMANCE: '性能', SECURITY: '安全', EXCEPTION_HANDLING: '异常处理', OTHER: '其他' }
  return map[props.finding.category] ?? props.finding.category
})
</script>
