<template>
  <div class="grid gap-6">
    <div class="animate-slide-up">
      <h2 class="text-2xl font-bold text-slate-800 dark:text-white">模型配置</h2>
      <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">管理模型供应商、模型档案、审查策略和质量闸门</p>
    </div>

    <!-- Provider Cards -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div v-for="(provider, index) in providerCounts" :key="provider.providerId" class="stat-card animate-slide-up" :class="`stagger-${index + 1}`">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm font-bold text-slate-800 dark:text-white">{{ provider.providerName }}</p>
            <p class="mt-1 text-xs text-slate-400">{{ provider.enabledProfiles }} / {{ provider.totalProfiles }} 个模型启用</p>
          </div>
          <span class="badge-soft" :class="provider.enabled ? 'badge-soft-success' : 'badge-soft-info'">{{ provider.enabled ? '已启用' : '未启用' }}</span>
        </div>
      </div>
    </div>

    <!-- Main Grid -->
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
      <div class="soft-card-flat animate-slide-up stagger-3">
        <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <div class="flex items-center gap-3">
            <div class="icon-soft icon-soft-primary" style="width:40px;height:40px;border-radius:12px;"><UIcon name="i-heroicons-cpu-chip" class="h-5 w-5" /></div>
            <h3 class="text-lg font-bold text-slate-800 dark:text-white">模型档案</h3>
          </div>
          <span class="badge-soft badge-soft-info">{{ modelProfiles.length }} 个可用</span>
        </div>
        <div class="p-4">
          <UTable :rows="modelProfiles" :columns="modelColumns" :loading="loading" :empty-state="{ icon: 'i-heroicons-magnifying-glass', label: '暂无模型档案' }">
            <template #displayName-data="{ row }"><div><p class="text-sm font-semibold text-slate-800 dark:text-white">{{ row.displayName }}</p><p class="text-xs text-slate-400 mono">{{ row.modelName }}</p></div></template>
            <template #providerName-data="{ row }"><span class="badge-soft badge-soft-neutral">{{ row.providerName || row.providerId }}</span></template>
            <template #capabilityTags-data="{ row }"><div class="flex flex-wrap gap-1"><span v-for="tag in row.capabilityTags" :key="tag" class="badge-soft badge-soft-primary" style="padding:0.25rem 0.5rem">{{ tag }}</span></div></template>
            <template #enabled-data="{ row }"><span class="badge-soft" :class="row.enabled ? 'badge-soft-success' : 'badge-soft-neutral'">{{ row.enabled ? '启用' : '停用' }}</span></template>
          </UTable>
        </div>
      </div>

      <!-- Right Sidebar: Strategies -->
      <aside class="grid content-start gap-6">
        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-primary" style="width:36px;height:36px;border-radius:10px;"><UIcon name="i-heroicons-squares-2x2" class="h-4 w-4" /></div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">审查策略</h3>
          </div>

          <div class="grid gap-3">
            <div v-for="strategy in strategySummaries" :key="strategy.id" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
              <div class="flex items-start justify-between gap-3">
                <div><p class="text-sm font-bold text-slate-800 dark:text-white">{{ strategy.name }}</p><p class="mt-1 text-xs leading-5 text-slate-400">{{ strategy.description }}</p></div>
                <span class="badge-soft badge-soft-primary">{{ strategy.reviewMode }}</span>
              </div>

              <div class="mt-3 space-y-1.5">
                <div v-for="label in strategy.roleLabels" :key="label" class="flex items-center gap-2 text-xs text-slate-600 dark:text-slate-300">
                  <UIcon name="i-heroicons-cpu-chip" class="h-3.5 w-3.5 text-indigo-500" />
                  <span>{{ label }}</span>
                </div>
              </div>

              <div class="mt-3 flex flex-wrap gap-1.5">
                <span v-for="level in strategy.gatePolicy?.blockOn || []" :key="level" class="badge-soft badge-soft-error">阻断 {{ level }}</span>
                <span v-for="level in strategy.gatePolicy?.requireHumanReviewOn || []" :key="level" class="badge-soft badge-soft-warning">复核 {{ level }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
interface ApiModelProvider { id: number; providerKey: string; name: string; providerType: string; endpoint: string; apiKeyEnv: string; enabled: boolean; description: string; profiles?: ApiModelProfile[] }
interface ApiModelProfile { id: number; providerId: number; providerName?: string; profileKey: string; modelName: string; displayName: string; capabilityTags: string[]; defaultTemperature: number; timeoutSeconds: number; enabled: boolean }
interface ApiReviewStrategy { id: number; strategyKey: string; name: string; reviewMode: string; description: string; recommendedFor: string[]; blockOn: string[]; requireHumanReviewOn: string[]; advisoryOn: string[]; enabled: boolean; roleBindings: { role: string; modelProfileName: string }[] }

const { get } = useApi()
const loading = ref(false)
const providers = ref<ApiModelProvider[]>([])
const modelProfiles = ref<ApiModelProfile[]>([])
const strategySummaries = ref<(ApiReviewStrategy & { roleLabels: string[]; gatePolicy: { blockOn: string[]; requireHumanReviewOn: string[] } })[]>([])

const modelColumns = [{ key: 'displayName', label: '模型' }, { key: 'providerName', label: '供应商' }, { key: 'capabilityTags', label: '能力标签' }, { key: 'defaultTemperature', label: '温度' }, { key: 'timeoutSeconds', label: '超时' }, { key: 'enabled', label: '状态' }]

const providerCounts = computed(() => providers.value.map(provider => {
  const profiles = modelProfiles.value.filter(p => p.providerId === provider.id)
  return { providerId: provider.providerKey, providerName: provider.name, enabled: provider.enabled, totalProfiles: profiles.length, enabledProfiles: profiles.filter(p => p.enabled).length }
}))

async function loadModelConfig() {
  loading.value = true
  try {
    const [providersRes, profilesRes, strategiesRes] = await Promise.all([
      get<ApiModelProvider[]>('/model-config/providers').catch(() => null),
      get<ApiModelProfile[]>('/model-config/profiles').catch(() => null),
      get<ApiReviewStrategy[]>('/model-config/strategies').catch(() => null),
    ])
    if (providersRes?.data) providers.value = providersRes.data
    if (profilesRes?.data) modelProfiles.value = profilesRes.data
    if (strategiesRes?.data) {
      strategySummaries.value = strategiesRes.data.map(strategy => ({
        ...strategy,
        roleLabels: strategy.roleBindings?.map(b => `${roleLabel(b.role)} · ${b.modelProfileName || '未知模型'}`) || [],
        gatePolicy: { blockOn: strategy.blockOn || [], requireHumanReviewOn: strategy.requireHumanReviewOn || [] },
      }))
    }
  } catch (e) { console.error('加载模型配置失败', e) }
  finally { loading.value = false }
}

function roleLabel(role: string): string {
  const labels: Record<string, string> = { WORKER: '审查模型', JUDGE: 'Judge 模型', SECURITY_AUDITOR: '安全审计员', PERFORMANCE_ANALYST: '性能分析师', CODE_STYLE_CHECKER: '代码规范检查员', EXCEPTION_HANDLER: '异常处理专家', ARCHITECT_REVIEWER: '架构评审员' }
  return labels[role] || role
}

onMounted(loadModelConfig)
</script>
