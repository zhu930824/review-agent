<template>
  <div class="space-y-6">
    <div>
      <UButton to="/" variant="ghost" color="gray" icon="i-heroicons-arrow-left" size="sm">返回质量驾驶舱</UButton>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <UIcon name="i-heroicons-arrow-path" class="h-8 w-8 animate-spin text-primary-500" />
    </div>

    <template v-else-if="detail">
      <UCard>
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <h2 class="text-xl font-bold text-gray-900 dark:text-white">审查详情</h2>
            <p class="mt-1 text-sm text-gray-500">
              {{ detail.projectName || '未知项目' }} · {{ detail.sourceBranch || '-' }} → {{ detail.targetBranch || '-' }}
            </p>
            <p v-if="parsedSummary" class="mt-3 max-w-3xl text-sm leading-6 text-gray-600 dark:text-gray-300">
              {{ parsedSummary }}
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <UBadge :color="statusColor(detail.status)" :label="statusLabel(detail.status)" variant="subtle" />
            <UBadge :color="gateStatusColor" :label="gateStatusLabel" variant="subtle" />
            <UBadge :label="detail.reviewMode" color="gray" variant="subtle" />
          </div>
        </div>

        <div class="mt-5 grid grid-cols-2 gap-3 md:grid-cols-5">
          <div v-for="item in severityStats" :key="item.label" class="rounded-lg bg-gray-50 p-3 dark:bg-gray-800">
            <p class="text-xs text-gray-500">{{ item.label }}</p>
            <p class="mt-1 text-xl font-bold" :class="item.textClass">{{ item.value }}</p>
          </div>
        </div>
      </UCard>

      <UCard v-if="blockedReasons.length" class="border-red-200 dark:border-red-800">
        <template #header>
          <div class="flex items-center gap-2">
            <UIcon name="i-heroicons-shield-exclamation" class="h-5 w-5 text-red-500" />
            <h3 class="text-lg font-semibold text-red-600">Pre-PR 阻断原因</h3>
          </div>
        </template>
        <ul class="space-y-2 text-sm text-red-600 dark:text-red-300">
          <li v-for="reason in blockedReasons" :key="reason" class="flex gap-2">
            <span class="mt-2 h-1.5 w-1.5 rounded-full bg-red-500"></span>
            <span>{{ reason }}</span>
          </li>
        </ul>
      </UCard>

      <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
        <UCard>
          <template #header>
            <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">发现问题（{{ filteredFindings.length }} / {{ detail.totalFindings }}）</h3>
              <div class="flex items-center gap-2">
                <USelect v-model="severityFilter" :options="severityOptions" placeholder="严重度" size="sm" class="w-36" />
                <USelect v-model="categoryFilter" :options="categoryOptions" placeholder="分类" size="sm" class="w-40" />
              </div>
            </div>
          </template>

          <div class="space-y-3">
            <FindingCard
              v-for="finding in filteredFindings"
              :key="finding.id"
              :finding="finding"
              @confirm="updateFindingStatus(finding.id, 'CONFIRMED')"
              @dismiss="updateFindingStatus(finding.id, 'DISMISSED')"
            />
          </div>

          <div v-if="!filteredFindings.length" class="py-8 text-center text-gray-500">
            <UIcon name="i-heroicons-check-badge" class="mx-auto h-10 w-10 text-green-300" />
            <p class="mt-2">没有匹配的问题</p>
          </div>
        </UCard>

        <aside class="space-y-4">
          <UCard v-if="detail.modelResults.length">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">模型执行</h3>
            </template>
            <div class="space-y-3">
              <div
                v-for="result in detail.modelResults"
                :key="result.id"
                class="rounded-lg border border-gray-200 p-3 dark:border-gray-700"
              >
                <div class="flex items-center justify-between gap-2">
                  <div>
                    <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ result.modelName }}</p>
                    <p class="text-xs text-gray-500">{{ result.role }}</p>
                  </div>
                  <UBadge :color="statusColor(result.status)" :label="statusLabel(result.status)" variant="subtle" size="xs" />
                </div>
                <p v-if="result.errorMessage" class="mt-2 text-xs text-red-500">{{ result.errorMessage }}</p>
                <p v-if="result.startedAt || result.completedAt" class="mt-2 text-xs text-gray-400">
                  {{ result.startedAt || '-' }} → {{ result.completedAt || '-' }}
                </p>
              </div>
            </div>
          </UCard>

          <UCard>
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">人工处理</h3>
            </template>
            <div class="space-y-3">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">待确认</span>
                <span class="font-semibold text-orange-600">{{ pendingFindings }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">已确认</span>
                <span class="font-semibold text-green-600">{{ confirmedFindings }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">已忽略</span>
                <span class="font-semibold text-gray-600 dark:text-gray-300">{{ dismissedFindings }}</span>
              </div>
            </div>
          </UCard>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { ReviewDetail, HumanStatus } from '~/types/review'
import { deriveGateStatus, generateBlockedReasons } from '~/utils/reviewMetrics'

const route = useRoute()
const { get, patch } = useApi()
const reviewId = computed(() => route.params.id as string)
const loading = ref(false)
const detail = ref<ReviewDetail | null>(null)
const severityFilter = ref('all')
const categoryFilter = ref('all')

const severityOptions = [
  { label: '全部严重度', value: 'all' },
  { label: '阻断', value: 'BLOCKER' },
  { label: '重要', value: 'MAJOR' },
  { label: '一般', value: 'MINOR' },
  { label: '提示', value: 'INFO' },
]

const categoryOptions = [
  { label: '全部分类', value: 'all' },
  { label: '代码风格', value: 'CODE_STYLE' },
  { label: '潜在缺陷', value: 'BUG' },
  { label: '性能问题', value: 'PERFORMANCE' },
  { label: '安全问题', value: 'SECURITY' },
  { label: '异常处理', value: 'EXCEPTION_HANDLING' },
]

const gateStatus = computed(() => detail.value ? deriveGateStatus(detail.value) : 'RUNNING')
const blockedReasons = computed(() => detail.value ? generateBlockedReasons(detail.value) : [])

const gateStatusLabel = computed(() => {
  const map: Record<string, string> = {
    PASSED: 'Pre-PR 通过',
    BLOCKED: 'Pre-PR 阻断',
    NEEDS_HUMAN_REVIEW: '待人工复核',
    RUNNING: '审查中',
  }
  return map[gateStatus.value] ?? gateStatus.value
})

const gateStatusColor = computed(() => {
  const map: Record<string, string> = {
    PASSED: 'green',
    BLOCKED: 'red',
    NEEDS_HUMAN_REVIEW: 'orange',
    RUNNING: 'blue',
  }
  return map[gateStatus.value] ?? 'gray'
})

const filteredFindings = computed(() => {
  let items = detail.value?.findings ?? []
  if (severityFilter.value !== 'all') {
    items = items.filter(f => f.severity === severityFilter.value)
  }
  if (categoryFilter.value !== 'all') {
    items = items.filter(f => f.category === categoryFilter.value)
  }
  return items
})

const pendingFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'PENDING').length)
const confirmedFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'CONFIRMED').length)
const dismissedFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'DISMISSED').length)

const severityStats = computed(() => [
  { label: '阻断', value: detail.value?.blockerCount ?? 0, textClass: 'text-red-600' },
  { label: '重要', value: detail.value?.majorCount ?? 0, textClass: 'text-orange-600' },
  { label: '一般', value: detail.value?.minorCount ?? 0, textClass: 'text-yellow-600' },
  { label: '提示', value: detail.value?.infoCount ?? 0, textClass: 'text-blue-600' },
  { label: '交叉命中', value: (detail.value?.findings ?? []).filter(item => item.isCrossHit).length, textClass: 'text-purple-600' },
])

const parsedSummary = computed(() => {
  const summary = detail.value?.summary
  if (!summary) return ''
  try {
    const parsed = JSON.parse(summary)
    if (typeof parsed === 'string') return parsed
    return parsed.summary || parsed.conclusion || parsed.description || ''
  } catch {
    return summary
  }
})

function statusColor(status: string): string {
  const map: Record<string, string> = { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }
  return map[status] ?? 'gray'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] ?? status
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await get<ReviewDetail>(`/reviews/${reviewId.value}`)
    if (res.data) detail.value = res.data
  } catch (e) {
    console.error('加载审查详情失败', e)
  } finally {
    loading.value = false
  }
}

async function updateFindingStatus(findingId: number, status: HumanStatus) {
  try {
    await patch(`/reviews/${reviewId.value}/finding/${findingId}`, { humanStatus: status })
    await loadDetail()
  } catch (e) {
    console.error('更新问题状态失败', e)
  }
}

onMounted(loadDetail)
</script>
