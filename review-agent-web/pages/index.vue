<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">质量驾驶舱</h2>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">跟踪 Pre-PR 通过率、阻断风险和多模型审查稳定性。</p>
      </div>
      <UButton to="/reviews/create" variant="solid" color="primary" icon="i-heroicons-play">发起 Pre-PR</UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <UCard v-for="stat in stats" :key="stat.label" :ui="{ body: { padding: 'p-5' } }">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ stat.label }}</p>
            <p class="mt-2 text-3xl font-bold text-gray-900 dark:text-white">{{ stat.value }}</p>
            <p class="mt-1 text-xs text-gray-400">{{ stat.hint }}</p>
          </div>
          <UIcon :name="stat.icon" class="h-9 w-9" :class="stat.iconClass" />
        </div>
      </UCard>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最近审查</h3>
            <UButton to="/reviews/create" variant="soft" color="primary" size="sm" icon="i-heroicons-plus">新建</UButton>
          </div>
        </template>

        <UTable :rows="recentReviews" :columns="reviewColumns" :loading="loading" :empty-state="{ icon: 'i-heroicons-magnifying-glass', label: '暂无审查记录' }">
          <template #status-data="{ row }">
            <UBadge :color="statusColor(row.status)" :label="statusLabel(row.status)" variant="subtle" size="xs" />
          </template>
          <template #reviewMode-data="{ row }">
            <UBadge :label="row.reviewMode" color="gray" variant="subtle" size="xs" />
          </template>
          <template #actions-data="{ row }">
            <UButton :to="`/reviews/${row.id}`" variant="ghost" color="primary" size="xs" icon="i-heroicons-arrow-right">查看</UButton>
          </template>
        </UTable>
      </UCard>

      <aside class="space-y-4">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">风险压力</h3>
          </template>
          <div class="space-y-4">
            <div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">Pre-PR 通过率</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ metrics.prePrPassRate }}%</span>
              </div>
              <UProgress :value="metrics.prePrPassRate" color="green" class="mt-2" />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="rounded-lg bg-red-50 p-3 dark:bg-red-950/30">
                <p class="text-xs text-red-600 dark:text-red-300">阻断审查</p>
                <p class="mt-1 text-xl font-bold text-red-700 dark:text-red-200">{{ metrics.blockedReviews }}</p>
              </div>
              <div class="rounded-lg bg-orange-50 p-3 dark:bg-orange-950/30">
                <p class="text-xs text-orange-600 dark:text-orange-300">待人工确认</p>
                <p class="mt-1 text-xl font-bold text-orange-700 dark:text-orange-200">{{ metrics.pendingHumanConfirmations }}</p>
              </div>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">模型稳定性</h3>
          </template>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ metrics.modelSuccessRate }}%</p>
              <p class="mt-1 text-xs text-gray-500">最近详情样本的模型成功率</p>
            </div>
            <UIcon name="i-heroicons-cpu-chip" class="h-10 w-10 text-primary-400" />
          </div>
        </UCard>

        <UCard>
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">治理成熟度</h3>
              <UButton to="/governance" variant="ghost" color="primary" size="xs" icon="i-heroicons-arrow-right">进入</UButton>
            </div>
          </template>
          <div class="space-y-3">
            <div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">市场能力覆盖</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ governanceCoverage.coveragePercent }}%</span>
              </div>
              <UProgress :value="governanceCoverage.coveragePercent" color="primary" class="mt-2" />
            </div>
            <div class="rounded-lg bg-gray-50 p-3 dark:bg-gray-900/60">
              <p class="text-xs text-gray-500">下一优先项</p>
              <p class="mt-1 text-sm font-semibold text-gray-900 dark:text-white">{{ topGovernanceAction?.name }}</p>
            </div>
          </div>
        </UCard>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { Review, ReviewDetail } from '~/types/review'
import type { PageResult } from '~/types/api'
import { getCapabilityCoverageSummary, getRecommendedNextActions } from '~/utils/governanceCatalog'
import { deriveDashboardMetrics } from '~/utils/reviewMetrics'

const { get } = useApi()
const loading = ref(false)
const projectTotal = ref(0)
const recentReviews = ref<Review[]>([])
const recentDetails = ref<ReviewDetail[]>([])
const governanceCoverage = getCapabilityCoverageSummary()
const topGovernanceAction = getRecommendedNextActions(1)[0]

const metrics = computed(() => deriveDashboardMetrics(recentReviews.value, recentDetails.value))

const stats = computed(() => [
  { label: '项目总数', value: projectTotal.value, hint: '已纳入治理的代码库', icon: 'i-heroicons-folder', iconClass: 'text-gray-300 dark:text-gray-600' },
  { label: '审查总数', value: metrics.value.totalReviews, hint: '最近列表样本', icon: 'i-heroicons-magnifying-glass', iconClass: 'text-primary-300' },
  { label: '阻断问题', value: metrics.value.blockerFindings, hint: 'BLOCKER 级风险', icon: 'i-heroicons-shield-exclamation', iconClass: 'text-red-400' },
  { label: '已完成审查', value: metrics.value.completedReviews, hint: `${metrics.value.runningReviews} 个执行中`, icon: 'i-heroicons-check-badge', iconClass: 'text-green-400' },
])

const reviewColumns = [
  { key: 'projectName', label: '项目' },
  { key: 'sourceBranch', label: '源分支' },
  { key: 'targetBranch', label: '目标分支' },
  { key: 'reviewMode', label: '模式' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '时间' },
  { key: 'actions', label: '操作' },
]

async function loadDashboard() {
  loading.value = true
  try {
    const [projectsRes, reviewsRes] = await Promise.all([
      get<PageResult<Project>>('/projects?pageNum=1&pageSize=1'),
      get<PageResult<Review>>('/reviews?pageNum=1&pageSize=10'),
    ])

    projectTotal.value = projectsRes.data?.total ?? 0
    recentReviews.value = reviewsRes.data?.records ?? []
    recentDetails.value = (await Promise.all(
      recentReviews.value.slice(0, 5).map(async review => {
        try {
          const res = await get<ReviewDetail>(`/reviews/${review.id}`)
          return res.data
        } catch {
          return null
        }
      }),
    )).filter((item): item is ReviewDetail => Boolean(item))
  } catch (e) {
    console.error('加载质量驾驶舱失败', e)
  } finally {
    loading.value = false
  }
}

function statusColor(status: string): string {
  const map: Record<string, string> = { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }
  return map[status] ?? 'gray'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] ?? status
}

onMounted(loadDashboard)
</script>
