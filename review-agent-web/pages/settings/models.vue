<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-2xl font-bold text-gray-900 dark:text-white">模型配置</h2>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">管理模型供应商、模型档案、审查策略和质量闸门。</p>
    </div>

    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <UCard v-for="provider in providerCounts" :key="provider.providerId">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ provider.providerName }}</p>
            <p class="mt-1 text-xs text-gray-500">{{ provider.enabledProfiles }} / {{ provider.totalProfiles }} 个模型启用</p>
          </div>
          <UBadge :label="provider.enabled ? '已启用' : '未启用'" :color="provider.enabled ? 'green' : 'gray'" variant="subtle" />
        </div>
      </UCard>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">模型档案</h3>
            <UBadge :label="`${enabledModelProfiles.length} 个可用`" color="primary" variant="subtle" />
          </div>
        </template>

        <UTable :rows="builtinModelProfiles" :columns="modelColumns">
          <template #displayName-data="{ row }">
            <div>
              <p class="text-sm font-medium text-gray-900 dark:text-white">{{ row.displayName }}</p>
              <p class="text-xs text-gray-500">{{ row.modelName }}</p>
            </div>
          </template>
          <template #providerId-data="{ row }">
            <UBadge :label="providerName(row.providerId)" color="gray" variant="subtle" />
          </template>
          <template #capabilityTags-data="{ row }">
            <div class="flex flex-wrap gap-1">
              <UBadge v-for="tag in row.capabilityTags" :key="tag" :label="tag" color="blue" variant="subtle" size="xs" />
            </div>
          </template>
          <template #enabled-data="{ row }">
            <UBadge :label="row.enabled ? '启用' : '停用'" :color="row.enabled ? 'green' : 'gray'" variant="subtle" />
          </template>
        </UTable>
      </UCard>

      <aside class="space-y-4">
        <UCard>
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">审查策略</h3>
          </template>

          <div class="space-y-3">
            <div
              v-for="strategy in strategySummaries"
              :key="strategy.id"
              class="rounded-lg border border-gray-200 p-4 dark:border-gray-700"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ strategy.name }}</p>
                  <p class="mt-1 text-xs leading-5 text-gray-500">{{ strategy.description }}</p>
                </div>
                <UBadge :label="strategy.reviewMode" color="primary" variant="subtle" size="xs" />
              </div>

              <div class="mt-3 space-y-1">
                <div v-for="role in strategy.roleLabels" :key="role" class="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
                  <UIcon name="i-heroicons-cpu-chip" class="h-3.5 w-3.5 text-primary-500" />
                  <span>{{ role }}</span>
                </div>
              </div>

              <div class="mt-3 flex flex-wrap gap-1">
                <UBadge v-for="level in strategy.gatePolicy.blockOn" :key="level" :label="`阻断 ${level}`" color="red" variant="subtle" size="xs" />
                <UBadge v-for="level in strategy.gatePolicy.requireHumanReviewOn" :key="level" :label="`复核 ${level}`" color="orange" variant="subtle" size="xs" />
              </div>
            </div>
          </div>
        </UCard>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  builtinModelProfiles,
  builtinModelProviders,
  enabledModelProfiles,
  getProviderModelCounts,
  getStrategySummaries,
} from '~/utils/modelStrategies'

const providerCounts = getProviderModelCounts()
const strategySummaries = getStrategySummaries()

const modelColumns = [
  { key: 'displayName', label: '模型' },
  { key: 'providerId', label: '供应商' },
  { key: 'capabilityTags', label: '能力标签' },
  { key: 'defaultTemperature', label: '温度' },
  { key: 'timeoutSeconds', label: '超时' },
  { key: 'enabled', label: '状态' },
]

function providerName(providerId: string) {
  return builtinModelProviders.find(provider => provider.id === providerId)?.name ?? providerId
}
</script>
