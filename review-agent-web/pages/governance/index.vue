<template>
  <div class="grid gap-6">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between animate-slide-up">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-white">治理中心</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">对标 AI Review、质量门禁和安全扫描产品能力，把平台路线图转成可执行的规则包、集成和工作流。</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <UButton to="/reviews/create" icon="i-heroicons-play">按模板发起审查</UButton>
        <UButton to="/settings/models" icon="i-heroicons-cog-6-tooth" variant="soft" color="gray">配置模型策略</UButton>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
      <div class="stat-card animate-slide-up stagger-1">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">市场能力覆盖</p>
            <div class="mt-2 flex items-end gap-2">
              <p class="text-3xl font-extrabold bg-gradient-to-result from-indigo-600 to-purple-600 dark:from-indigo-400 dark:to-purple-400 bg-clip-text text-transparent">{{ coverage.coveragePercent }}%</p>
            </div>
            <p class="mt-1 text-xs text-slate-400">按已启用与部分具备折算</p>
          </div>
          <div class="icon-soft icon-soft-primary"><UIcon name="i-heroicons-chart-bar" class="h-5 w-5" /></div>
        </div>
        <div class="progress-soft mt-3"><div class="progress-soft-fill" :style="{ width: coverage.coveragePercent + '%' }"></div></div>
      </div>

      <div class="stat-card animate-slide-up stagger-2">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">已启用能力</p>
            <p class="mt-2 text-3xl font-extrabold text-emerald-600 dark:text-emerald-300">{{ coverage.enabled }}</p>
            <p class="mt-1 text-xs text-slate-400">PR 摘要、行级审查、质量门禁、效能分析</p>
          </div>
          <div class="icon-soft icon-soft-success"><UIcon name="i-heroicons-check-circle" class="h-5 w-5" /></div>
        </div>
      </div>

      <div class="stat-card animate-slide-up stagger-3">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">待补齐关键项</p>
            <p class="mt-2 text-3xl font-extrabold text-amber-600 dark:text-amber-300">{{ coverage.partial + coverage.planned }}</p>
            <p class="mt-1 text-xs text-slate-400">安全扫描、状态检查、SARIF、自动修复</p>
          </div>
          <div class="icon-soft icon-soft-warning"><UIcon name="i-heroicons-exclamation-circle" class="h-5 w-5" /></div>
        </div>
      </div>

      <div class="stat-card animate-slide-up stagger-4">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">工作流模板</p>
            <p class="mt-2 text-3xl font-extrabold text-slate-800 dark:text-white">{{ workflowTemplates.length }}</p>
            <p class="mt-1 text-xs text-slate-400">覆盖日常自查、发布门禁和架构评审</p>
          </div>
          <div class="icon-soft icon-soft-info"><UIcon name="i-heroicons-squares-2x2" class="h-5 w-5" /></div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
      <div class="soft-card-flat animate-slide-up stagger-3">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="flex items-center gap-3">
            <div class="icon-soft icon-soft-info" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-squares-2x2" class="h-5 w-5" /></div>
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">市场能力对标</h3>
          </div>
          <UBadge :label="`${marketCapabilities.length} 项能力`" color="primary" variant="subtle" />
        </div>
        <div class="p-4">
          <UTable :rows="marketCapabilities" :columns="capabilityColumns">
            <template #name-data="{ row }"><div><p class="text-sm font-semibold text-slate-800 dark:text-white">{{ row.name }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ row.platformMove }}</p></div></template>
            <template #category-data="{ row }"><UBadge :label="categoryLabel(row.category)" color="slate" variant="subtle" size="xs" /></template>
            <template #status-data="{ row }"><UBadge :label="statusLabel(row.status)" :color="statusColor(row.status)" variant="subtle" size="xs" /></template>
            <template #businessImpact-data="{ row }"><UBadge :label="impactLabel(row.businessImpact)" :color="impactColor(row.businessImpact)" variant="subtle" size="xs" /></template>
            <template #sourceProducts-data="{ row }"><div class="flex flex-wrap gap-1"><UBadge v-for="product in row.sourceProducts" :key="product" :label="product" color="blue" variant="subtle" size="xs" /></div></template>
          </UTable>
        </div>
      </div>

      <aside class="grid h-full grid-rows-[auto_minmax(0,1fr)] gap-6">
        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-warning" style="width:36px;height:36px;border-radius:10px;"><UIcon name="i-heroicons-bolt" class="h-4 w-4" /></div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">优先落地项</h3>
          </div>
          <div class="grid gap-3">
            <div v-for="action in recommendedActions" :key="action.id" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
              <div class="flex items-start justify-between gap-3">
                <div><p class="text-sm font-bold text-slate-800 dark:text-white">{{ action.name }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ action.platformMove }}</p></div>
                <UBadge :label="String(action.priorityScore)" color="amber" variant="subtle" size="xs" />
              </div>
            </div>
          </div>
        </div>

        <div class="panel-soft h-full p-5 animate-slide-up stagger-5">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-error" style="width:36px;height:36px;border-radius:10px;"><UIcon name="i-heroicons-shield-check" class="h-4 w-4" /></div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">发布门禁策略包</h3>
          </div>
          <div class="grid gap-4">
            <div><p class="text-xs font-semibold text-slate-500">要求能力</p><div class="mt-2 flex flex-wrap gap-1"><UBadge v-for="capability in releasePolicy.requiredCapabilities" :key="capability" :label="capability" color="red" variant="subtle" size="xs" /></div></div>
            <div><p class="text-xs font-semibold text-slate-500">建议策略</p><div class="mt-2 flex flex-wrap gap-1"><UBadge v-for="strategy in releasePolicy.suggestedStrategies" :key="strategy" :label="strategy" color="primary" variant="subtle" size="xs" /></div></div>
            <div><p class="text-xs font-semibold text-slate-500">人工检查点</p><ul class="mt-2 space-y-1"><li v-for="checkpoint in releasePolicy.requiredHumanCheckpoints" :key="checkpoint" class="flex items-center gap-2 text-xs text-slate-600 dark:text-slate-300"><UIcon name="i-heroicons-user-circle" class="h-4 w-4 text-amber-500" /><span>{{ checkpoint }}</span></li></ul></div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Bottom Grid -->
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <div class="soft-card-flat animate-slide-up stagger-4">
        <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="icon-soft icon-soft-primary" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-map" class="h-5 w-5" /></div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white">集成路线图</h3>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 gap-6 md:grid-cols-3">
            <div v-for="stage in rolloutStages" :key="stage.key" class="grid content-start gap-3">
              <div class="flex items-center gap-2"><UIcon :name="stage.icon" class="h-5 w-5" :class="stage.iconClass" /><p class="text-sm font-bold text-slate-800 dark:text-white">{{ stage.label }}</p></div>
              <div v-for="connector in connectorsByStage[stage.key]" :key="connector.id" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
                <p class="text-sm font-semibold text-slate-800 dark:text-white">{{ connector.name }}</p>
                <p class="mt-1 text-xs leading-5 text-slate-500">{{ connector.businessValue }}</p>
                <p class="mt-2 text-xs text-slate-400">{{ connector.implementationHint }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="soft-card-flat animate-slide-up stagger-5">
        <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="icon-soft icon-soft-success" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-arrow-path-rounded-square" class="h-5 w-5" /></div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white">业务工作流模板</h3>
        </div>
        <div class="grid gap-3 p-6">
          <div v-for="workflow in workflowTemplates" :key="workflow.id" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-5">
            <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
              <div><p class="text-sm font-bold text-slate-800 dark:text-white">{{ workflow.name }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ workflow.scenario }}</p></div>
              <UBadge :label="workflow.strategyId" color="primary" variant="subtle" size="xs" />
            </div>
            <div class="mt-3 flex flex-wrap gap-1">
              <UBadge v-for="packId in workflow.rulePackIds" :key="packId" :label="packId" color="slate" variant="subtle" size="xs" />
              <UBadge v-for="integrationId in workflow.integrationIds" :key="integrationId" :label="integrationId" color="blue" variant="subtle" size="xs" />
            </div>
            <p class="mt-3 text-xs text-slate-500">{{ workflow.successMetric }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { BusinessImpact, CapabilityStatus, RolloutStage } from '~/types/governance'
import { compileGovernancePolicyPack, getCapabilityCoverageSummary, getConnectorsByStage, getRecommendedNextActions, marketCapabilities, workflowTemplates } from '~/utils/governanceCatalog'

const coverage = getCapabilityCoverageSummary()
const recommendedActions = getRecommendedNextActions(5)
const connectorsByStage = getConnectorsByStage()
const releasePolicy = compileGovernancePolicyPack(['security-release', 'ai-generated-code', 'compliance-evidence'])

const capabilityColumns = [{ key: 'name', label: '能力' }, { key: 'category', label: '类别' }, { key: 'status', label: '状态' }, { key: 'businessImpact', label: '业务影响' }, { key: 'sourceProducts', label: '市场信号' }]
const rolloutStages: Array<{ key: RolloutStage; label: string; icon: string; iconClass: string }> = [
  { key: 'live', label: '已可用', icon: 'i-heroicons-check-circle', iconClass: 'text-green-500' },
  { key: 'next', label: '下一阶段', icon: 'i-heroicons-arrow-path-rounded-square', iconClass: 'text-indigo-500' },
  { key: 'later', label: '后续扩展', icon: 'i-heroicons-clock', iconClass: 'text-slate-400' },
]

function statusLabel(status: CapabilityStatus) { return { ENABLED: '已启用', PARTIAL: '部分具备', PLANNED: '待建设' }[status] }
function statusColor(status: CapabilityStatus) { return { ENABLED: 'green', PARTIAL: 'orange', PLANNED: 'gray' }[status] }
function impactLabel(impact: BusinessImpact) { return { HIGH: '高', MEDIUM: '中', LOW: '低' }[impact] }
function impactColor(impact: BusinessImpact) { return { HIGH: 'red', MEDIUM: 'blue', LOW: 'gray' }[impact] }
function categoryLabel(category: string) { return { AI_REVIEW: 'AI 审查', QUALITY: '质量', SECURITY: '安全', INTEGRATION: '集成', KNOWLEDGE: '知识库', ANALYTICS: '分析' }[category] ?? category }
</script>
