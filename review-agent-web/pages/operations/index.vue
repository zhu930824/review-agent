<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">运营中心</h2>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          把审查结果转成修复队列、SLA、规则学习和业务收益，让质量治理能持续推进。
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <UButton to="/governance" icon="i-heroicons-squares-2x2" color="primary">查看治理路线</UButton>
        <UButton to="/reviews/create" icon="i-heroicons-play" variant="soft" color="gray">发起审查</UButton>
      </div>
    </div>

    <!-- 加载态：骨架屏 + 进度条 -->
    <template v-if="loading">
      <UProgress animation="carousel" size="sm" color="primary" />
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <UCard v-for="i in 4" :key="`sk-${i}`">
          <div class="animate-pulse space-y-3">
            <div class="h-3 w-20 rounded bg-gray-200 dark:bg-gray-700" />
            <div class="h-8 w-16 rounded bg-gray-300 dark:bg-gray-600" />
            <div class="h-4 w-full rounded bg-gray-200 dark:bg-gray-700" />
            <div class="h-3 w-24 rounded bg-gray-200 dark:bg-gray-700" />
          </div>
        </UCard>
      </div>
      <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
        <UCard>
          <template #header>
            <div class="animate-pulse h-5 w-24 rounded bg-gray-200 dark:bg-gray-700" />
          </template>
          <div class="animate-pulse space-y-3">
            <div v-for="j in 5" :key="`row-${j}`" class="h-10 rounded bg-gray-100 dark:bg-gray-800" />
          </div>
        </UCard>
        <aside class="space-y-4">
          <UCard v-for="k in 2" :key="`aside-${k}`">
            <template #header>
              <div class="animate-pulse h-5 w-20 rounded bg-gray-200 dark:bg-gray-700" />
            </template>
            <div class="animate-pulse space-y-3">
              <div v-for="l in 2" :key="`item-${l}`" class="h-4 w-full rounded bg-gray-100 dark:bg-gray-800" />
            </div>
          </UCard>
        </aside>
      </div>
    </template>

    <!-- 错误态 -->
    <UCard v-else-if="error">
      <div class="flex flex-col items-center gap-4 py-8">
        <UIcon name="i-heroicons-exclamation-triangle" class="h-12 w-12 text-red-400" />
        <p class="text-sm text-gray-500 dark:text-gray-400">{{ error }}</p>
        <UButton color="primary" variant="soft" icon="i-heroicons-arrow-path" @click="loadDashboard">重试</UButton>
      </div>
    </UCard>

    <!-- 正常数据渲染 -->
    <template v-else>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <UCard>
          <p class="text-sm text-gray-500 dark:text-gray-400">运营就绪度</p>
          <p class="mt-3 text-3xl font-bold text-gray-900 dark:text-white">{{ scorecard.operationalReadiness }}%</p>
          <UProgress :value="scorecard.operationalReadiness" color="primary" class="mt-3" />
        </UCard>
        <UCard>
          <p class="text-sm text-gray-500 dark:text-gray-400">开放风险项</p>
          <p class="mt-3 text-3xl font-bold text-orange-600 dark:text-orange-300">{{ scorecard.openRiskItems }}</p>
          <p class="mt-1 text-xs text-gray-500">{{ scorecard.blockerCount }} 个 BLOCKER 需要优先处理</p>
        </UCard>
        <UCard>
          <p class="text-sm text-gray-500 dark:text-gray-400">SLA 压力</p>
          <p class="mt-3 text-3xl font-bold text-red-600 dark:text-red-300">{{ queueSummary.slaPressure }}%</p>
          <p class="mt-1 text-xs text-gray-500">{{ queueSummary.humanPendingCount }} 个等待人工确认</p>
        </UCard>
        <UCard>
          <p class="text-sm text-gray-500 dark:text-gray-400">月度收益估算</p>
          <p class="mt-3 text-3xl font-bold text-green-600 dark:text-green-300">{{ businessImpact.hoursSaved }}h</p>
          <p class="mt-1 text-xs text-gray-500">另规避 {{ businessImpact.avoidedReworkHours }}h 返工</p>
        </UCard>
      </div>

      <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
        <UCard>
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">修复队列</h3>
              <UBadge :label="`${queueSummary.total} 项`" color="primary" variant="subtle" />
            </div>
          </template>

          <div v-if="remediationQueue.length === 0" class="flex flex-col items-center gap-2 py-10">
            <UIcon name="i-heroicons-inbox" class="h-10 w-10 text-gray-300 dark:text-gray-600" />
            <p class="text-sm text-gray-400 dark:text-gray-500">暂无待修复项，运营队列已清空</p>
          </div>

          <UTable v-else :rows="remediationQueue" :columns="queueColumns">
            <template #title-data="{ row }">
              <div>
                <p class="text-sm font-medium text-gray-900 dark:text-white">{{ row.title }}</p>
                <p class="mt-1 text-xs text-gray-500">{{ row.projectName }} · Review #{{ row.reviewId }}</p>
              </div>
            </template>
            <template #severity-data="{ row }">
              <UBadge :label="row.severity" :color="severityColor(row.severity)" variant="subtle" size="xs" />
            </template>
            <template #ownerRole-data="{ row }">
              <UBadge :label="row.ownerRole" color="gray" variant="subtle" size="xs" />
            </template>
            <template #slaHours-data="{ row }">
              <span class="text-sm font-medium text-gray-900 dark:text-white">{{ row.slaHours }}h</span>
            </template>
            <template #priorityScore-data="{ row }">
              <UBadge :label="String(row.priorityScore)" color="orange" variant="subtle" size="xs" />
            </template>
          </UTable>
        </UCard>

        <aside class="space-y-4">
          <UCard>
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">责任人负载</h3>
            </template>
            <div v-if="ownerLoad.length === 0" class="flex flex-col items-center gap-2 py-6">
              <UIcon name="i-heroicons-users" class="h-8 w-8 text-gray-300 dark:text-gray-600" />
              <p class="text-xs text-gray-400 dark:text-gray-500">暂无责任人分配数据</p>
            </div>
            <div v-else class="space-y-3">
              <div v-for="owner in ownerLoad" :key="owner.role">
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-300">{{ owner.role }}</span>
                  <span class="font-semibold text-gray-900 dark:text-white">{{ owner.count }}</span>
                </div>
                <UProgress :value="owner.percent" color="primary" class="mt-2" />
              </div>
            </div>
          </UCard>

          <UCard>
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">规则学习</h3>
            </template>
            <div v-if="learningCandidates.length === 0" class="flex flex-col items-center gap-2 py-6">
              <UIcon name="i-heroicons-light-bulb" class="h-8 w-8 text-gray-300 dark:text-gray-600" />
              <p class="text-xs text-gray-400 dark:text-gray-500">暂无规则学习候选，待更多人工确认后生成</p>
            </div>
            <div v-else class="space-y-3">
              <div v-for="candidate in learningCandidates" :key="candidate.findingId" class="rounded-lg border border-gray-200 p-3 dark:border-gray-700">
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ candidate.ruleTitle }}</p>
                    <p class="mt-1 text-xs leading-5 text-gray-500">{{ candidate.reason }}</p>
                  </div>
                  <UBadge :label="candidate.action === 'PROMOTE_TO_RULE' ? '固化' : '降噪'" :color="candidate.action === 'PROMOTE_TO_RULE' ? 'green' : 'gray'" variant="subtle" size="xs" />
                </div>
              </div>
            </div>
          </UCard>
        </aside>
      </div>

      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">运营节奏建议</h3>
        </template>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div v-for="cadence in operatingCadences" :key="cadence.name" class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
            <div class="flex items-center gap-2">
              <UIcon :name="cadence.icon" class="h-5 w-5 text-primary-500" />
              <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ cadence.name }}</p>
            </div>
            <p class="mt-2 text-xs leading-5 text-gray-500">{{ cadence.description }}</p>
          </div>
        </div>
        <p class="mt-4 text-sm text-gray-600 dark:text-gray-300">{{ businessImpact.executiveSummary }}</p>
      </UCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { SeverityLevel } from '~/types/review'
import { useApi } from '~/composables/useApi'
import {
  buildRemediationQueue,
  deriveOperationsScorecard,
  estimateReviewBusinessImpact,
  extractRuleLearningCandidates,
  summarizeRemediationQueue,
} from '~/utils/reviewOperations'

/** 运营仪表盘 API 返回数据结构 */
interface OperationDashboardVO {
  findings: Array<{
    id: number
    reviewId: number
    projectName: string
    severity: string
    category: string
    title: string
    humanStatus: string
    confidence: number
    isCrossHit: boolean
  }>
  totalFindings: number
  blockerCount: number
  majorCount: number
  pendingCount: number
  confirmedCount: number
  dismissedCount: number
  slaPressure: number
}

const { get } = useApi()
const dashboardData = ref<OperationDashboardVO | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

/** 计算属性 —— 从 API 数据派生出各类运营指标 */

// 修复队列：按优先级排序的待处理项列表
const remediationQueue = computed(() =>
  dashboardData.value ? buildRemediationQueue(dashboardData.value.findings) : [],
)

// 修复队列摘要：总数、阻断数、各角色负载、SLA 压力
const queueSummary = computed(() => summarizeRemediationQueue(remediationQueue.value))

// 规则学习候选：可固化为规则的确认项 + 可加入降噪样例的驳回项
const learningCandidates = computed(() =>
  dashboardData.value ? extractRuleLearningCandidates(dashboardData.value.findings) : [],
)

// 运营计分卡：综合风险、规则学习和就绪度
const scorecard = computed(() =>
  dashboardData.value ? deriveOperationsScorecard(dashboardData.value.findings) : {
    openRiskItems: 0,
    blockerCount: 0,
    ruleLearningCandidates: 0,
    topOwnerRole: 'None',
    operationalReadiness: 100,
  },
)

// 业务收益估算：基于仪表盘指标计算月度节省工时和规避返工
const businessImpact = computed(() => {
  if (!dashboardData.value) {
    return { hoursSaved: 0, avoidedReworkHours: 0, executiveSummary: '暂无收益数据' }
  }

  const data = dashboardData.value
  return estimateReviewBusinessImpact({
    monthlyReviews: Math.max(1, data.totalFindings),
    averageManualReviewMinutes: 35,
    automationCoveragePercent: Math.round(
      (data.totalFindings / Math.max(1, data.totalFindings)) * 100,
    ),
    blockerFindings: data.blockerCount,
    majorFindings: data.majorCount,
  })
})

const queueColumns = [
  { key: 'title', label: '风险项' },
  { key: 'severity', label: '级别' },
  { key: 'ownerRole', label: '负责人' },
  { key: 'slaHours', label: 'SLA' },
  { key: 'priorityScore', label: '优先级' },
]

const ownerLoad = computed(() => Object.entries(queueSummary.value.byOwner).map(([role, count]) => ({
  role,
  count,
  percent: queueSummary.value.total ? Math.round((count / queueSummary.value.total) * 100) : 0,
})))

const operatingCadences = [
  {
    name: '每日风险清理',
    icon: 'i-heroicons-shield-exclamation',
    description: '每天处理 BLOCKER 和 24 小时内到期项，避免风险穿透到正式 PR。',
  },
  {
    name: '每周规则复盘',
    icon: 'i-heroicons-academic-cap',
    description: '将确认问题固化为规则，将低置信误报加入降噪样例，持续提高模型可用性。',
  },
  {
    name: '发布前门禁',
    icon: 'i-heroicons-rocket-launch',
    description: '核心链路发布前检查安全、性能、异常处理和人工确认记录。',
  },
]

function severityColor(severity: SeverityLevel) {
  const map: Record<SeverityLevel, string> = {
    BLOCKER: 'red',
    MAJOR: 'orange',
    MINOR: 'blue',
    INFO: 'gray',
  }
  return map[severity]
}

/** 从后端拉取运营仪表盘数据 */
async function loadDashboard() {
  loading.value = true
  error.value = null
  try {
    const res = await get<OperationDashboardVO>('/api/operations/dashboard')
    dashboardData.value = res.data ?? null
  } catch (e) {
    console.error('加载运营仪表盘失败', e)
    error.value = '数据加载失败，请检查网络连接后重试。'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>
