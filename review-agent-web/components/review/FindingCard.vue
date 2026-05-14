<template>
  <div class="panel-soft p-4 animate-slide-up" :class="borderClass">
    <div class="flex items-start justify-between gap-4">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 flex-wrap">
          <UBadge :color="severityColor" :label="finding.severity" variant="subtle" size="xs" />
          <UBadge :color="categoryColor" :label="categoryLabel" variant="subtle" size="xs" />
          <UBadge v-if="finding.isCrossHit" color="purple" label="交叉命中" variant="subtle" size="xs" />
          <span v-if="finding.modelName" class="text-xs text-slate-400">{{ finding.modelName }}</span>
        </div>
        <h4 class="mt-2 text-sm font-bold text-slate-800 dark:text-white truncate">{{ finding.title }}</h4>
        <p class="mt-1 text-sm text-slate-500 truncate mono">{{ finding.filePath }}<span v-if="finding.lineStart">:{{ finding.lineStart }}</span></p>
      </div>
      <div class="flex items-center gap-2 flex-shrink-0">
        <UButton v-if="finding.humanStatus === 'PENDING'" variant="ghost" color="emerald" size="xs" icon="i-heroicons-check" @click="$emit('confirm')" />
        <UButton v-if="finding.humanStatus === 'PENDING'" variant="ghost" color="red" size="xs" icon="i-heroicons-x-mark" @click="$emit('dismiss')" />
        <UBadge v-else :color="finding.humanStatus === 'CONFIRMED' ? 'emerald' : 'slate'" :label="finding.humanStatus === 'CONFIRMED' ? '已确认' : '已忽略'" variant="subtle" size="xs" />
      </div>
    </div>

    <div v-if="expanded" class="mt-3 pt-3 border-t border-slate-100/50 dark:border-slate-700/50">
      <p v-if="finding.description" class="text-sm text-slate-600 dark:text-slate-300 leading-5">{{ finding.description }}</p>
      <p v-if="finding.suggestion" class="mt-2 text-sm text-indigo-600 dark:text-indigo-400">
        <UIcon name="i-heroicons-light-bulb" class="h-4 w-4 inline mr-1" />
        建议：{{ finding.suggestion }}
      </p>
    </div>

    <div class="mt-2">
      <UButton variant="ghost" color="slate" size="xs" :label="expanded ? '收起详情' : '展开详情'" @click="expanded = !expanded" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ReviewFinding } from '~/types/review'

const props = defineProps<{ finding: ReviewFinding }>()
defineEmits<{ confirm: []; dismiss: [] }>()

const expanded = ref(false)

const severityColor = computed(() => ({ BLOCKER: 'red', MAJOR: 'amber', MINOR: 'yellow', INFO: 'blue' }[props.finding.severity] ?? 'slate'))
const categoryColor = computed(() => ({ BUG: 'red', SECURITY: 'purple', PERFORMANCE: 'amber', CODE_STYLE: 'slate', EXCEPTION_HANDLING: 'yellow' }[props.finding.category] ?? 'slate'))
const categoryLabel = computed(() => ({ CODE_STYLE: '代码风格', BUG: '潜在缺陷', PERFORMANCE: '性能', SECURITY: '安全', EXCEPTION_HANDLING: '异常处理', OTHER: '其他' }[props.finding.category] ?? props.finding.category))
const borderClass = computed(() => ({ BLOCKER: 'border-red-200 dark:border-red-800/30', MAJOR: 'border-orange-200 dark:border-orange-800/30', MINOR: 'border-yellow-200 dark:border-yellow-800/30', INFO: 'border-blue-200 dark:border-blue-800/30' }[props.finding.severity] ?? ''))
</script>