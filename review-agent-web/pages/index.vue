<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between animate-slide-up">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-white">质量驾驶舱</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">跟踪 Pre-PR 通过率、阻断风险和多模型审查稳定性</p>
      </div>
      <NuxtLink to="/reviews/create">
        <button class="soft-button-primary rounded-full">
          <UIcon name="i-heroicons-play" class="h-5 w-5" />
          <span>发起 Pre-PR</span>
        </button>
      </NuxtLink>
    </div>

    <!-- KPI Stats Row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="(stat, index) in stats" :key="stat.label" class="stat-card animate-slide-up" :class="`stagger-${index + 1}`">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">{{ stat.label }}</p>
            <p class="mt-2 text-3xl font-extrabold text-slate-800 dark:text-white">{{ stat.value }}</p>
            <p class="mt-1 text-xs text-slate-400">{{ stat.hint }}</p>
          </div>
          <div class="icon-soft" :class="stat.iconClass">
            <UIcon :name="stat.icon" class="h-5 w-5" />
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
      <!-- Recent Reviews Table -->
      <div class="soft-card-flat animate-slide-up stagger-3">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="flex items-center gap-3">
            <div class="icon-soft icon-soft-info" style="width:36px;height:36px;border-radius:10px;">
              <UIcon name="i-heroicons-clock" class="h-4 w-4" />
            </div>
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">最近审查</h3>
          </div>
          <NuxtLink to="/reviews/create" class="badge-soft badge-soft-primary hover:shadow-md transition-shadow">
            <UIcon name="i-heroicons-plus" class="h-3.5 w-3.5" />
            新建
          </NuxtLink>
        </div>

        <UTable :rows="recentReviews" :columns="reviewColumns" :loading="loading" :empty-state="{ icon: 'i-heroicons-magnifying-glass', label: '暂无审查记录' }">
          <template #status-data="{ row }">
            <span class="badge-soft" :class="statusBadgeClass(row.status)">
              {{ statusLabel(row.status) }}
            </span>
          </template>
          <template #reviewMode-data="{ row }">
            <span class="badge-soft badge-soft-neutral">{{ row.reviewMode }}</span>
          </template>
          <template #actions-data="{ row }">
            <NuxtLink :to="`/reviews/${row.id}`" class="inline-flex items-center gap-1 text-sm font-semibold text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 dark:hover:text-indigo-300 transition-colors">
              查看
              <UIcon name="i-heroicons-arrow-right" class="h-3.5 w-3.5" />
            </NuxtLink>
          </template>
        </UTable>
      </div>

      <!-- Right Sidebar Cards -->
      <aside class="space-y-5">
        <!-- Risk Pressure -->
        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-error" style="width:36px;height:36px;border-radius:10px;">
              <UIcon name="i-heroicons-shield-exclamation" class="h-4 w-4" />
            </div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">风险压力</h3>
          </div>

          <div class="space-y-4">
            <div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-slate-500 dark:text-slate-400 font-medium">Pre-PR 通过率</span>
                <span class="font-bold text-slate-800 dark:text-white">{{ metrics.prePrPassRate }}%</span>
              </div>
              <div class="progress-soft mt-2">
                <div
                  class="progress-soft-fill"
                  :class="metrics.prePrPassRate >= 80 ? 'progress-soft-fill-success' : 'progress-soft-fill-warning'"
                  :style="{ width: metrics.prePrPassRate + '%' }"
                ></div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div class="rounded-2xl bg-gradient-to-br from-red-50 to-rose-50 dark:from-red-900/15 dark:to-rose-900/10 p-4">
                <p class="text-xs font-semibold text-red-600 dark:text-red-300">阻断审查</p>
                <p class="mt-1 text-2xl font-extrabold text-red-700 dark:text-red-200">{{ metrics.blockedReviews }}</p>
              </div>
              <div class="rounded-2xl bg-gradient-to-br from-amber-50 to-yellow-50 dark:from-amber-900/15 dark:to-yellow-900/10 p-4">
                <p class="text-xs font-semibold text-amber-600 dark:text-amber-300">待人工确认</p>
                <p class="mt-1 text-2xl font-extrabold text-amber-700 dark:text-amber-200">{{ metrics.pendingHumanConfirmations }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Model Stability -->
        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-primary" style="width:36px;height:36px;border-radius:10px;">
              <UIcon name="i-heroicons-cpu-chip" class="h-4 w-4" />
            </div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">模型稳定性</h3>
          </div>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-400 dark:to-purple-400 bg-clip-text text-transparent">
                {{ metrics.modelSuccessRate }}%
              </p>
              <p class="mt-1 text-xs text-slate-400 font-medium">模型成功率</p>
            </div>
            <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 shadow-sm">
              <UIcon name="i-heroicons-chart-bar" class="h-7 w-7 text-indigo-500" />
            </div>
          </div>
        </div>

        <!-- Governance Maturity -->
        <div class="panel-soft p-5 animate-slide-up stagger-5">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="icon-soft icon-soft-success" style="width:36px;height:36px;border-radius:10px;">
                <UIcon name="i-heroicons-squares-2x2" class="h-4 w-4" />
              </div>
              <h3 class="text-sm font-bold text-slate-800 dark:text-white">治理成熟度</h3>
            </div>
            <NuxtLink to="/governance" class="inline-flex items-center gap-1 text-xs font-semibold text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 dark:hover:text-indigo-300 transition-colors">
              进入
              <UIcon name="i-heroicons-arrow-right" class="h-3 w-3" />
            </NuxtLink>
          </div>

          <div class="space-y-3">
            <div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-slate-500 dark:text-slate-400 font-medium">市场能力覆盖</span>
                <span class="font-bold text-slate-800 dark:text-white">{{ governanceCoverage.coveragePercent }}%</span>
              </div>
              <div class="progress-soft mt-2">
                <div class="progress-soft-fill" :style="{ width: governanceCoverage.coveragePercent + '%' }"></div>
              </div>
            </div>
            <div class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
              <p class="text-xs text-slate-400 font-medium">下一优先项</p>
              <p class="mt-1 text-sm font-bold text-slate-800 dark:text-white">{{ topGovernanceAction?.name }}</p>
            </div>
          </div>
        </div>
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
  { label: '项目总数', value: projectTotal.value, hint: '已纳入治理的代码库', icon: 'i-heroicons-folder', iconClass: 'icon-soft-primary' },
  { label: '审查总数', value: metrics.value.totalReviews, hint: '最近列表样本', icon: 'i-heroicons-magnifying-glass', iconClass: 'icon-soft-info' },
  { label: '阻断问题', value: metrics.value.blockerFindings, hint: 'BLOCKER 级风险', icon: 'i-heroicons-shield-exclamation', iconClass: 'icon-soft-error' },
  { label: '已完成审查', value: metrics.value.completedReviews, hint: `${metrics.value.runningReviews} 个执行中`, icon: 'i-heroicons-check-badge', iconClass: 'icon-soft-success' },
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

function statusBadgeClass(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'badge-soft-warning',
    RUNNING: 'badge-soft-info',
    COMPLETED: 'badge-soft-success',
    FAILED: 'badge-soft-error',
  }
  return map[status] ?? 'badge-soft-info'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] ?? status
}

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

onMounted(loadDashboard)
</script>