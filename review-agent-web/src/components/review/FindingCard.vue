<template>
  <a-card style="margin-bottom: 8px" :body-style="{ padding: '12px 16px' }">
    <div style="display: flex; align-items: flex-start; justify-content: space-between">
      <div style="flex: 1; min-width: 0">
        <a-space :size="4" wrap>
          <a-tag :color="severityColor">{{ finding.severity }}</a-tag>
          <a-tag>{{ categoryLabel }}</a-tag>
          <a-tag v-if="finding.isCrossHit" color="purple">交叉命中</a-tag>
          <span v-if="finding.modelName" style="font-size: 12px; color: #999">{{ finding.modelName }}</span>
        </a-space>
        <h4 style="margin: 8px 0 4px; font-size: 14px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ finding.title }}</h4>
        <p style="margin: 0; font-size: 12px; color: #999; font-family: monospace">{{ finding.filePath }}<span v-if="finding.lineStart">:{{ finding.lineStart }}</span></p>
      </div>
      <div style="flex-shrink: 0; margin-left: 8px">
        <template v-if="finding.humanStatus === 'PENDING'">
          <a-button size="small" type="text" @click="$emit('confirm')"><CheckOutlined style="color: #52c41a" /></a-button>
          <a-button size="small" type="text" @click="$emit('dismiss')"><CloseOutlined style="color: #ff4d4f" /></a-button>
        </template>
        <a-tag v-else :color="finding.humanStatus === 'CONFIRMED' ? 'green' : 'default'">
          {{ finding.humanStatus === 'CONFIRMED' ? '已确认' : '已忽略' }}
        </a-tag>
      </div>
    </div>

    <div v-if="expanded" style="border-top: 1px solid #f0f0f0; margin-top: 12px; padding-top: 12px">
      <p v-if="finding.description" style="font-size: 13px; color: #666; line-height: 1.6; margin: 0 0 8px">{{ finding.description }}</p>
      <p v-if="finding.suggestion" style="font-size: 13px; color: #4F46E5; margin: 0">
        <BulbOutlined style="margin-right: 4px" />建议：{{ finding.suggestion }}
      </p>
    </div>

    <a-button type="link" size="small" @click="expanded = !expanded" style="padding: 0; margin-top: 8px">
      {{ expanded ? '收起详情' : '展开详情' }}
    </a-button>
  </a-card>
</template>

<script setup lang="ts">
import type { ReviewFinding } from '~/types/review'
import { CheckOutlined, CloseOutlined, BulbOutlined } from '@ant-design/icons-vue'

const props = defineProps<{ finding: ReviewFinding }>()
defineEmits<{ confirm: []; dismiss: [] }>()

const expanded = ref(false)

const severityColor = computed(() => ({
  BLOCKER: 'red', MAJOR: 'orange', MINOR: 'gold', INFO: 'blue'
}[props.finding.severity] ?? 'default'))

const categoryLabel = computed(() => ({
  CODE_STYLE: '代码风格', BUG: '潜在缺陷', PERFORMANCE: '性能',
  SECURITY: '安全', EXCEPTION_HANDLING: '异常处理', OTHER: '其他'
}[props.finding.category] ?? props.finding.category))
</script>
