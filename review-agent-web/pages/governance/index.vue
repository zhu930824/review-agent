<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">治理中心</h2>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          对标 AI Review、质量门禁和安全扫描产品能力，把平台路线图转成可执行的规则包、集成和工作流。
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <UButton to="/reviews/create" icon="i-heroicons-play" color="primary">按模板发起审查</UButton>
        <UButton to="/settings/models" icon="i-heroicons-cog-6-tooth" variant="soft" color="gray">配置模型策略</UButton>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <UCard>
        <p class="text-sm text-gray-500 dark:text-gray-400">市场能力覆盖</p>
        <div class="mt-3 flex items-end gap-2">
          <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ coverage.coveragePercent }}%</p>
          <p class="pb-1 text-xs text-gray-500">按已启用与部分具备折算</p>
        </div>
        <UProgress :value="coverage.coveragePercent" color="primary" class="mt-3" />
      </UCard>

      <UCard>
        <p class="text-sm text-gray-500 dark:text-gray-400">已启用能力</p>
        <p class="mt-3 text-3xl font-bold text-green-600 dark:text-green-300">{{ coverage.enabled }}</p>
        <p class="mt-1 text-xs text-gray-500">PR 摘要、行级审查、质量门禁、效能分析</p>
      </UCard>

      <UCard>
        <p class="text-sm text-gray-500 dark:text-gray-400">待补齐关键项</p>
        <p class="mt-3 text-3xl font-bold text-orange-600 dark:text-orange-300">{{ coverage.partial + coverage.planned }}</p>
        <p class="mt-1 text-xs text-gray-500">安全扫描、状态检查、SARIF、自动修复</p>
      </UCard>

      <UCard>
        <p class="text-sm text-gray-500 dark:text-gray-400">工作流模板</p>
        <p class="mt-3 text-3xl font-bold text-gray-900 dark:text-white">{{ workflowTemplates.length }}</p>
        <p class="mt-1 text-xs text-gray-500">覆盖日常自查、发布门禁和架构评审</p>
      </UCard>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">市场能力对标</h3>
            <UBadge :label="`${marketCapabilities.length} 项能力`" color="primary" variant="subtle" />
          </div>
        </template>

        <UTable :rows="marketCapabilities" :columns="capabilityColumns">
          <template #name-data="{ row }">
            <div>
              <p class="text-sm font-medium text-gray-900 dark:text-white">{{ row.name }}</p>
              <p class="mt-1 text-xs leading-5 text-gray-500">{{ row.platformMove }}</p>
            </div>
          </template>
          <template #category-data="{ row }">
            <UBadge :label="categoryLabel(row.category)" color="gray" variant="subtle" size="xs" />
          </template>
          <template #status-data="{ row }">
            <UBadge :label="statusLabel(row.status)" :color="statusColor(row.status)" variant="subtle" size="xs" />
          </template>
          <template #businessImpact-data="{ row }">
            <UBadge :label="impactLabel(row.businessImpact)" :color="impactColor(row.businessImpact)" variant="subtle" size="xs" />
          </template>
          <template #sourceProducts-data="{ row }">
            <div class="flex flex-wrap gap-1">
              <UBadge v-for="product in row.sourceProducts" :key="product" :label="product" color="blue" variant="subtle" size="xs" />
            </div>
          </template>
        </UTable>
      </UCard>

      <aside class="space-y-4">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">优先落地项</h3>
          </template>
          <div class="space-y-3">
            <div v-for="action in recommendedActions" :key="action.id" class="rounded-lg border border-gray-200 p-3 dark:border-gray-700">
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ action.name }}</p>
                  <p class="mt-1 text-xs leading-5 text-gray-500">{{ action.platformMove }}</p>
                </div>
                <UBadge :label="String(action.priorityScore)" color="orange" variant="subtle" size="xs" />
              </div>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">发布门禁策略包</h3>
          </template>
          <div class="space-y-3">
            <div>
              <p class="text-xs text-gray-500">要求能力</p>
              <div class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="capability in releasePolicy.requiredCapabilities" :key="capability" :label="capability" color="red" variant="subtle" size="xs" />
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500">建议策略</p>
              <div class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="strategy in releasePolicy.suggestedStrategies" :key="strategy" :label="strategy" color="primary" variant="subtle" size="xs" />
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500">人工检查点</p>
              <ul class="mt-2 space-y-1">
                <li v-for="checkpoint in releasePolicy.requiredHumanCheckpoints" :key="checkpoint" class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
                  <UIcon name="i-heroicons-user-circle" class="h-4 w-4 text-orange-500" />
                  <span>{{ checkpoint }}</span>
                </li>
              </ul>
            </div>
          </div>
        </UCard>
      </aside>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">集成路线图</h3>
        </template>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div v-for="stage in rolloutStages" :key="stage.key" class="space-y-3">
            <div class="flex items-center gap-2">
              <UIcon :name="stage.icon" class="h-5 w-5" :class="stage.iconClass" />
              <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ stage.label }}</p>
            </div>
            <div v-for="connector in connectorsByStage[stage.key]" :key="connector.id" class="rounded-lg border border-gray-200 p-3 dark:border-gray-700">
              <p class="text-sm font-medium text-gray-900 dark:text-white">{{ connector.name }}</p>
              <p class="mt-1 text-xs leading-5 text-gray-500">{{ connector.businessValue }}</p>
              <p class="mt-2 text-xs text-gray-400">{{ connector.implementationHint }}</p>
            </div>
          </div>
        </div>
      </UCard>

      <UCard>
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">业务工作流模板</h3>
        </template>
        <div class="space-y-3">
          <div v-for="workflow in workflowTemplates" :key="workflow.id" class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
            <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ workflow.name }}</p>
                <p class="mt-1 text-xs leading-5 text-gray-500">{{ workflow.scenario }}</p>
              </div>
              <UBadge :label="workflow.strategyId" color="primary" variant="subtle" size="xs" />
            </div>
            <div class="mt-3 flex flex-wrap gap-1">
              <UBadge v-for="packId in workflow.rulePackIds" :key="packId" :label="packId" color="gray" variant="subtle" size="xs" />
              <UBadge v-for="integrationId in workflow.integrationIds" :key="integrationId" :label="integrationId" color="blue" variant="subtle" size="xs" />
            </div>
            <p class="mt-3 text-xs text-gray-500">{{ workflow.successMetric }}</p>
          </div>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { BusinessImpact, CapabilityStatus, RolloutStage } from '~/types/governance'
import {
  compileGovernancePolicyPack,
  getCapabilityCoverageSummary,
  getConnectorsByStage,
  getRecommendedNextActions,
  marketCapabilities,
  workflowTemplates,
} from '~/utils/governanceCatalog'

const coverage = getCapabilityCoverageSummary()
const recommendedActions = getRecommendedNextActions(5)
const connectorsByStage = getConnectorsByStage()
const releasePolicy = compileGovernancePolicyPack(['security-release', 'ai-generated-code', 'compliance-evidence'])

const capabilityColumns = [
  { key: 'name', label: '能力' },
  { key: 'category', label: '类别' },
  { key: 'status', label: '状态' },
  { key: 'businessImpact', label: '业务影响' },
  { key: 'sourceProducts', label: '市场信号' },
]

const rolloutStages: Array<{ key: RolloutStage; label: string; icon: string; iconClass: string }> = [
  { key: 'live', label: '已可用', icon: 'i-heroicons-check-circle', iconClass: 'text-green-500' },
  { key: 'next', label: '下一阶段', icon: 'i-heroicons-arrow-path-rounded-square', iconClass: 'text-primary-500' },
  { key: 'later', label: '后续扩展', icon: 'i-heroicons-clock', iconClass: 'text-gray-400' },
]

function statusLabel(status: CapabilityStatus) {
  const map: Record<CapabilityStatus, string> = {
    ENABLED: '已启用',
    PARTIAL: '部分具备',
    PLANNED: '待建设',
  }
  return map[status]
}

function statusColor(status: CapabilityStatus) {
  const map: Record<CapabilityStatus, string> = {
    ENABLED: 'green',
    PARTIAL: 'orange',
    PLANNED: 'gray',
  }
  return map[status]
}

function impactLabel(impact: BusinessImpact) {
  const map: Record<BusinessImpact, string> = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低',
  }
  return map[impact]
}

function impactColor(impact: BusinessImpact) {
  const map: Record<BusinessImpact, string> = {
    HIGH: 'red',
    MEDIUM: 'blue',
    LOW: 'gray',
  }
  return map[impact]
}

function categoryLabel(category: string) {
  const map: Record<string, string> = {
    AI_REVIEW: 'AI 审查',
    QUALITY: '质量',
    SECURITY: '安全',
    INTEGRATION: '集成',
    KNOWLEDGE: '知识库',
    ANALYTICS: '分析',
  }
  return map[category] ?? category
}
</script>
