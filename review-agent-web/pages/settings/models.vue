<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-2xl font-bold text-gray-900 dark:text-white">模型配置</h2>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">管理模型供应商、模型档案、审查策略和质量闸门。</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-12">
      <UIcon name="i-heroicons-arrow-path" class="h-8 w-8 animate-spin text-primary-500" />
    </div>

    <template v-else>
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
              <UBadge :label="`${enabledProfileCount} 个可用`" color="primary" variant="subtle" />
            </div>
          </template>

          <UTable :rows="profiles" :columns="modelColumns">
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
    </template>
  </div>
</template>

<script setup lang="ts">

// -- 后端返回的 VO 类型（仅在当前页面使用，内联定义） --

interface ModelProviderVO {
  id: string
  providerKey: string
  name: string
  kind: string
  endpoint: string
  apiKeyEnv: string
  enabled: boolean
  description: string
}

interface ModelProfileVO {
  id: string
  providerId: string
  profileKey: string
  modelName: string
  displayName: string
  capabilityTags: string[]
  defaultTemperature: number
  timeoutSeconds: number
  enabled: boolean
}

interface ReviewStrategyVO {
  id: string
  strategyKey: string
  name: string
  reviewMode: string
  description: string
  recommendedFor: string[]
  gatePolicy: {
    blockOn: string[]
    requireHumanReviewOn: string[]
    advisoryOn: string[]
  }
  roleBindings: Array<{
    role: string
    modelProfileId: string
    modelProfileName: string
    modelName: string
    skills: string[]
    temperature: number
  }>
  enabled: boolean
}

// -- 角色中文映射（与后端 AgentRole 对应） --

const roleLabelMap: Record<string, string> = {
  WORKER: '审查模型',
  JUDGE: 'Judge 模型',
  SECURITY_AUDITOR: '安全审计员',
  PERFORMANCE_ANALYST: '性能分析师',
  CODE_STYLE_CHECKER: '代码规范检查员',
  EXCEPTION_HANDLER: '异常处理专家',
  ARCHITECT_REVIEWER: '架构评审员',
}

// -- 响应式状态 --

const { get } = useApi()
const loading = ref(true)
const providers = ref<ModelProviderVO[]>([])
const profiles = ref<ModelProfileVO[]>([])
const strategies = ref<ReviewStrategyVO[]>([])

// -- 派生数据 --

const enabledProfileCount = computed(() => profiles.value.filter(p => p.enabled).length)

/** 供应商卡片数据：统计每个供应商下启用/总档案数 */
const providerCounts = computed(() =>
  providers.value.map(provider => {
    const relatedProfiles = profiles.value.filter(p => p.providerId === provider.id)
    return {
      providerId: provider.id,
      providerName: provider.name,
      enabled: provider.enabled,
      totalProfiles: relatedProfiles.length,
      enabledProfiles: relatedProfiles.filter(p => p.enabled).length,
    }
  }),
)

/** 策略摘要数据：将 roleBindings 转为可展示的角色标签 */
const strategySummaries = computed(() =>
  strategies.value.map(strategy => ({
    id: strategy.id,
    name: strategy.name,
    description: strategy.description,
    reviewMode: strategy.reviewMode,
    recommendedFor: strategy.recommendedFor,
    gatePolicy: strategy.gatePolicy,
    roleLabels: strategy.roleBindings.map(binding => {
      const roleDisplayName = roleLabelMap[binding.role] ?? binding.role
      // 优先从 profiles 列表中查找档案的 displayName，回退到 API 返回的 modelProfileName
      const profile = profiles.value.find(p => p.id === binding.modelProfileId)
      const profileDisplayName = profile?.displayName ?? binding.modelProfileName
      return `${roleDisplayName} · ${profileDisplayName}`
    }),
  })),
)

// -- 表格列定义 --

const modelColumns = [
  { key: 'displayName', label: '模型' },
  { key: 'providerId', label: '供应商' },
  { key: 'capabilityTags', label: '能力标签' },
  { key: 'defaultTemperature', label: '温度' },
  { key: 'timeoutSeconds', label: '超时' },
  { key: 'enabled', label: '状态' },
]

/** 根据供应商 ID 获取供应商名称 */
function providerName(providerId: string) {
  return providers.value.find(p => p.id === providerId)?.name ?? providerId
}

// -- 数据加载 --

async function loadData() {
  loading.value = true
  try {
    const [providersRes, profilesRes, strategiesRes] = await Promise.all([
      get<ModelProviderVO[]>('/api/model-config/providers'),
      get<ModelProfileVO[]>('/api/model-config/profiles'),
      get<ReviewStrategyVO[]>('/api/model-config/strategies'),
    ])
    providers.value = providersRes.data ?? []
    profiles.value = profilesRes.data ?? []
    strategies.value = strategiesRes.data ?? []
  } catch (error) {
    console.error('加载模型配置失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
