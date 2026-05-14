<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between animate-slide-up">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-white">运营中心</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">把审查结果转成修复队列、SLA、规则学习和业务收益，让质量治理能持续推进。</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <UButton to="/governance" icon="i-heroicons-squares-2x2">查看治理路线</UButton>
        <UButton to="/reviews/create" icon="i-heroicons-play" variant="soft" color="slate">发起审查</UButton>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <div class="stat-card animate-slide-up stagger-1">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">运营就绪度</p>
            <p class="mt-2 text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-400 dark:to-purple-400 bg-clip-text text-transparent">{{ scorecard.operationalReadiness }}%</p>
          </div>
          <div class="icon-soft icon-soft-primary"><UIcon name="i-heroicons-chart-bar" class="h-5 w-5" /></div>
        </div>
        <div class="progress-soft mt-3"><div class="progress-soft-fill" :style="{ width: scorecard.operationalReadiness + '%' }"></div></div>
      </div>

      <div class="stat-card animate-slide-up stagger-2">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">开放风险项</p>
            <p class="mt-2 text-3xl font-extrabold text-amber-600 dark:text-amber-300">{{ scorecard.openRiskItems }}</p>
            <p class="mt-1 text-xs text-slate-400">{{ scorecard.blockerCount }} 个 BLOCKER 需要优先处理</p>
          </div>
          <div class="icon-soft icon-soft-warning"><UIcon name="i-heroicons-exclamation-triangle" class="h-5 w-5" /></div>
        </div>
      </div>

      <div class="stat-card animate-slide-up stagger-3">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">SLA 压力</p>
            <p class="mt-2 text-3xl font-extrabold text-red-600 dark:text-red-300">{{ queueSummary.slaPressure }}%</p>
            <p class="mt-1 text-xs text-slate-400">{{ queueSummary.humanPendingCount }} 个等待人工确认</p>
          </div>
          <div class="icon-soft icon-soft-error"><UIcon name="i-heroicons-clock" class="h-5 w-5" /></div>
        </div>
      </div>

      <div class="stat-card animate-slide-up stagger-4">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">月度收益估算</p>
            <p class="mt-2 text-3xl font-extrabold text-emerald-600 dark:text-emerald-300">{{ businessImpact.hoursSaved }}h</p>
            <p class="mt-1 text-xs text-slate-400">另规避 {{ businessImpact.avoidedReworkHours }}h 返工</p>
          </div>
          <div class="icon-soft icon-soft-success"><UIcon name="i-heroicons-arrow-trending-up" class="h-5 w-5" /></div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
      <div class="soft-card-flat animate-slide-up stagger-3">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="flex items-center gap-3">
            <div class="icon-soft icon-soft-info" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-list-bullet" class="h-5 w-5" /></div>
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">修复队列</h3>
          </div>
          <UBadge :label="`${queueSummary.total} 项`" color="primary" variant="subtle" />
        </div>
        <div class="p-4">
          <UTable :rows="remediationQueue" :columns="queueColumns">
            <template #title-data="{ row }"><div><p class="text-sm font-semibold text-slate-800 dark:text-white">{{ row.title }}</p><p class="mt-1 text-xs text-slate-500">{{ row.projectName }} · Review #{{ row.reviewId }}</p></div></template>
            <template #severity-data="{ row }"><UBadge :label="row.severity" :color="severityColor(row.severity)" variant="subtle" size="xs" /></template>
            <template #ownerRole-data="{ row }"><UBadge :label="row.ownerRole" color="slate" variant="subtle" size="xs" /></template>
            <template #slaHours-data="{ row }"><span class="text-sm font-semibold text-slate-800 dark:text-white">{{ row.slaHours }}h</span></template>
            <template #priorityScore-data="{ row }"><UBadge :label="String(row.priorityScore)" color="amber" variant="subtle" size="xs" /></template>
          </UTable>
        </div>
      </div>

      <aside class="space-y-5">
        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-primary" style="width:36px;height:36px;border-radius:10px;"><UIcon name="i-heroicons-users" class="h-4 w-4" /></div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">责任人负载</h3>
          </div>
          <div class="space-y-3">
            <div v-for="owner in ownerLoad" :key="owner.role">
              <div class="flex items-center justify-between text-sm"><span class="text-slate-600 dark:text-slate-300 font-medium">{{ owner.role }}</span><span class="font-bold text-slate-800 dark:text-white">{{ owner.count }}</span></div>
              <UProgress :value="owner.percent" class="mt-2" />
            </div>
          </div>
        </div>

        <div class="panel-soft p-5 animate-slide-up stagger-5">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-success" style="width:36px;height:36px;border-radius:10px;"><UIcon name="i-heroicons-academic-cap" class="h-4 w-4" /></div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">规则学习</h3>
          </div>
          <div class="space-y-3">
            <div v-for="candidate in learningCandidates" :key="candidate.findingId" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
              <div class="flex items-start justify-between gap-3">
                <div><p class="text-sm font-bold text-slate-800 dark:text-white">{{ candidate.ruleTitle }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ candidate.reason }}</p></div>
                <UBadge :label="candidate.action === 'PROMOTE_TO_RULE' ? '固化' : '降噪'" :color="candidate.action === 'PROMOTE_TO_RULE' ? 'emerald' : 'slate'" variant="subtle" size="xs" />
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Cadence Card -->
    <div class="soft-card-flat animate-slide-up stagger-5">
      <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
        <div class="icon-soft icon-soft-success" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-calendar" class="h-5 w-5" /></div>
        <h3 class="text-lg font-bold text-slate-800 dark:text-white">运营节奏建议</h3>
      </div>
      <div class="p-6">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div v-for="cadence in operatingCadences" :key="cadence.name" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-5">
            <div class="flex items-center gap-3">
              <div class="icon-soft icon-soft-primary" style="width:40px;height:40px;border-radius:12px;"><UIcon :name="cadence.icon" class="h-5 w-5" /></div>
              <p class="text-sm font-bold text-slate-800 dark:text-white">{{ cadence.name }}</p>
            </div>
            <p class="mt-3 text-xs leading-5 text-slate-500">{{ cadence.description }}</p>
          </div>
        </div>
        <p class="mt-4 text-sm text-slate-600 dark:text-slate-300">{{ businessImpact.executiveSummary }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { OperationalFinding } from '~/types/operations'
import type { SeverityLevel } from '~/types/review'
import { buildRemediationQueue, deriveOperationsScorecard, estimateReviewBusinessImpact, extractRuleLearningCandidates, summarizeRemediationQueue } from '~/utils/reviewOperations'

const sampleFindings: OperationalFinding[] = [
  { id: 1, reviewId: 1001, projectName: 'payment-core', severity: 'BLOCKER', category: 'SECURITY', title: 'Token can be reused after logout', humanStatus: 'CONFIRMED', confidence: 0.92, isCrossHit: true },
  { id: 2, reviewId: 1001, projectName: 'payment-core', severity: 'MAJOR', category: 'PERFORMANCE', title: 'Settlement list has N+1 query risk', humanStatus: 'PENDING', confidence: 0.82 },
  { id: 3, reviewId: 1002, projectName: 'order-service', severity: 'MAJOR', category: 'EXCEPTION_HANDLING', title: 'External timeout is not mapped to business error', humanStatus: 'CONFIRMED', confidence: 0.88, isCrossHit: true },
  { id: 4, reviewId: 1003, projectName: 'web-console', severity: 'MINOR', category: 'CODE_STYLE', title: 'Naming can be clearer', humanStatus: 'DISMISSED', confidence: 0.41 },
]

const remediationQueue = buildRemediationQueue(sampleFindings)
const queueSummary = summarizeRemediationQueue(remediationQueue)
const learningCandidates = extractRuleLearningCandidates(sampleFindings)
const scorecard = deriveOperationsScorecard(sampleFindings)
const businessImpact = estimateReviewBusinessImpact({ monthlyReviews: 80, averageManualReviewMinutes: 35, automationCoveragePercent: 65, blockerFindings: 6, majorFindings: 18 })

const queueColumns = [{ key: 'title', label: '风险项' }, { key: 'severity', label: '级别' }, { key: 'ownerRole', label: '负责人' }, { key: 'slaHours', label: 'SLA' }, { key: 'priorityScore', label: '优先级' }]
const ownerLoad = computed(() => Object.entries(queueSummary.byOwner).map(([role, count]) => ({ role, count, percent: queueSummary.total ? Math.round((count / queueSummary.total) * 100) : 0 })))

const operatingCadences = [
  { name: '每日风险清理', icon: 'i-heroicons-shield-exclamation', description: '每天处理 BLOCKER 和 24 小时内到期项，避免风险穿透到正式 PR。' },
  { name: '每周规则复盘', icon: 'i-heroicons-academic-cap', description: '将确认问题固化为规则，将低置信误报加入降噪样例，持续提高模型可用性。' },
  { name: '发布前门禁', icon: 'i-heroicons-rocket-launch', description: '核心链路发布前检查安全、性能、异常处理和人工确认记录。' },
]

function severityColor(severity: SeverityLevel) { return { BLOCKER: 'red', MAJOR: 'orange', MINOR: 'blue', INFO: 'gray' }[severity] }
</script>