<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between animate-slide-up">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-white">发起 Pre-PR 审查</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">选择项目、分支和审查策略，生成提交前质量结论。</p>
      </div>
      <UButton to="/settings/models" variant="soft" color="slate" icon="i-heroicons-cog-6-tooth">
        模型配置
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
      <div class="soft-card-flat p-6 animate-slide-up stagger-2">
        <form class="space-y-6" @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
            <UFormGroup label="选择项目" name="projectId" required>
              <USelect v-model="form.projectId" :options="projectOptions" placeholder="选择项目" searchable :loading="loadingProjects" />
            </UFormGroup>

            <UFormGroup label="源分支" name="sourceBranch" required>
              <USelect v-model="form.sourceBranch" :options="branchOptions" placeholder="选择源分支" searchable :loading="loadingBranches" :disabled="!form.projectId" />
            </UFormGroup>

            <UFormGroup label="目标分支" name="targetBranch" required>
              <USelect v-model="form.targetBranch" :options="branchOptions" placeholder="选择目标分支" searchable :loading="loadingBranches" :disabled="!form.projectId" />
            </UFormGroup>
          </div>

          <UDivider label="审查策略" />

          <div v-if="loadingStrategies" class="py-8 text-center">
            <div class="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-white dark:bg-slate-800 shadow-md">
              <UIcon name="i-heroicons-arrow-path" class="h-5 w-5 animate-spin text-indigo-500" />
            </div>
          </div>
          <div v-else class="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <button
              v-for="strategy in strategyOptions"
              :key="strategy.id"
              type="button"
              class="stat-card p-5 text-left cursor-pointer"
              :class="form.strategyId === strategy.strategyKey ? 'ring-2 ring-indigo-500/50 border-indigo-300' : ''"
              @click="selectStrategy(strategy)"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <div class="flex items-center gap-2">
                    <div class="icon-soft icon-soft-primary" style="width:32px;height:32px;border-radius:10px;">
                      <UIcon name="i-heroicons-cpu-chip" class="h-4 w-4" />
                    </div>
                    <h3 class="text-sm font-bold text-slate-800 dark:text-white">{{ strategy.name }}</h3>
                  </div>
                  <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ strategy.description }}</p>
                </div>
                <UBadge :label="strategy.reviewMode" variant="subtle" color="primary" size="xs" />
              </div>
              <div class="mt-3 flex flex-wrap gap-1">
                <UBadge v-for="item in strategy.recommendedFor || []" :key="item" :label="item" color="slate" variant="subtle" size="xs" />
              </div>
            </button>
          </div>

          <div class="panel-soft p-5">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-bold text-slate-800 dark:text-white">策略模型编排</h3>
                <p class="mt-1 text-xs text-slate-500">提交时会编译为后端兼容的 modelsConfig。</p>
              </div>
              <UBadge :label="selectedStrategy?.reviewMode || '-'" color="primary" variant="subtle" />
            </div>

            <div class="mt-4 space-y-2">
              <div
                v-for="label in selectedRoleLabels"
                :key="label"
                class="flex items-center gap-2 rounded-2xl bg-gradient-to-r from-indigo-50/50 to-purple-50/50 dark:from-indigo-900/20 dark:to-purple-900/20 px-4 py-2.5 text-sm text-slate-700 dark:text-slate-200"
              >
                <UIcon name="i-heroicons-cpu-chip" class="h-4 w-4 text-indigo-500" />
                <span>{{ label }}</span>
              </div>
            </div>
          </div>

          <UFormGroup label="MCP 外部工具">
            <UToggle v-model="form.mcpEnabled" />
          </UFormGroup>

          <UAccordion :items="[{ label: '高级 JSON 配置', icon: 'i-heroicons-code-bracket-square', slot: 'advanced' }]" variant="ghost">
            <template #advanced>
              <UTextarea v-model="form.modelsConfigOverride" :rows="6" :placeholder="compiledJsonPreview" />
              <p class="mt-2 text-xs text-slate-500">填写后将覆盖策略自动生成的模型配置。</p>
            </template>
          </UAccordion>

          <div class="flex items-center gap-3 pt-2">
            <UButton type="submit" variant="solid" :loading="submitting" icon="i-heroicons-play">
              开始审查
            </UButton>
            <UButton to="/" variant="ghost" color="slate">取消</UButton>
          </div>
        </form>
      </div>

      <aside class="space-y-5">
        <div class="panel-soft p-5 animate-slide-up stagger-3">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-error" style="width:36px;height:36px;border-radius:10px;">
              <UIcon name="i-heroicons-shield-check" class="h-4 w-4" />
            </div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">质量闸门</h3>
          </div>
          <div class="space-y-4 text-sm">
            <div>
              <p class="text-xs font-semibold text-slate-500">阻断级别</p>
              <div class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.blockOn" :key="level" :label="level" color="red" variant="subtle" size="xs" />
              </div>
            </div>
            <div>
              <p class="text-xs font-semibold text-slate-500">需人工复核</p>
              <div class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.requireHumanReviewOn" :key="level" :label="level" color="orange" variant="subtle" size="xs" />
              </div>
            </div>
            <div>
              <p class="text-xs font-semibold text-slate-500">建议关注</p>
              <div class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.advisoryOn" :key="level" :label="level" color="blue" variant="subtle" size="xs" />
              </div>
            </div>
          </div>
        </div>

        <div class="panel-soft p-5 animate-slide-up stagger-4">
          <div class="flex items-center gap-3 mb-4">
            <div class="icon-soft icon-soft-neutral" style="width:36px;height:36px;border-radius:10px;">
              <UIcon name="i-heroicons-code-bracket" class="h-4 w-4" />
            </div>
            <h3 class="text-sm font-bold text-slate-800 dark:text-white">配置预览</h3>
          </div>
          <pre class="max-h-96 overflow-auto rounded-2xl bg-slate-900/5 dark:bg-slate-900/30 p-4 text-xs text-slate-100 font-mono">{{ compiledJsonPreview }}</pre>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { Review } from '~/types/review'
import type { PageResult } from '~/types/api'

interface ApiReviewStrategy {
  id: number
  strategyKey: string
  name: string
  reviewMode: string
  description: string
  recommendedFor: string[]
  blockOn: string[]
  requireHumanReviewOn: string[]
  advisoryOn: string[]
  enabled: boolean
  roleBindings: { role: string; modelProfileName: string }[]
}

const route = useRoute()
const { get, post } = useApi()
const submitting = ref(false)
const loadingStrategies = ref(false)
const loadingProjects = ref(false)
const loadingBranches = ref(false)

const projectOptions = ref<{ label: string; value: string }[]>([])
const branchOptions = ref<{ label: string; value: string }[]>([])
const strategyOptions = ref<ApiReviewStrategy[]>([])

const form = reactive({
  projectId: (route.query.projectId as string) || '',
  strategyId: '',
  strategyKey: '',
  sourceBranch: '',
  targetBranch: '',
  mcpEnabled: false,
  modelsConfigOverride: '',
})

const selectedStrategy = computed(() => strategyOptions.value.find(s => s.strategyKey === form.strategyKey))
const gatePolicy = computed(() => ({
  blockOn: selectedStrategy.value?.blockOn || [],
  requireHumanReviewOn: selectedStrategy.value?.requireHumanReviewOn || [],
  advisoryOn: selectedStrategy.value?.advisoryOn || [],
}))
const selectedRoleLabels = computed(() => {
  if (selectedStrategy.value?.roleBindings?.length) {
    return selectedStrategy.value.roleBindings.map(b => `${roleLabel(b.role)} · ${b.modelProfileName || '未知模型'}`)
  }
  return []
})

const compiledConfig = computed(() => ({
  reviewMode: selectedStrategy.value?.reviewMode || 'AGENT',
  modelsConfig: {
    agents: selectedStrategy.value?.roleBindings?.map(b => ({ role: b.role, modelName: getModelName(b.modelProfileName), skills: [], temperature: 0.3 })) || [],
    orchestrationStrategy: 'PARALLEL',
    mcpEnabled: form.mcpEnabled,
    gatePolicy: gatePolicy.value,
  },
}))
const compiledJsonPreview = computed(() => JSON.stringify(compiledConfig.value.modelsConfig, null, 2))

function roleLabel(role: string): string {
  const labels: Record<string, string> = { WORKER: '审查模型', JUDGE: 'Judge 模型', SECURITY_AUDITOR: '安全审计员', PERFORMANCE_ANALYST: '性能分析师', CODE_STYLE_CHECKER: '代码规范检查员', EXCEPTION_HANDLER: '异常处理专家', ARCHITECT_REVIEWER: '架构评审员' }
  return labels[role] || role
}

function getModelName(displayName: string | undefined): string {
  if (!displayName) return 'qwen-plus'
  const nameMap: Record<string, string> = { '通义千问 Plus': 'qwen-plus', '通义千问 Max': 'qwen-max', 'DeepSeek V3': 'deepseek-v3', 'Kimi K2': 'kimi-k2', '本地代码模型': 'local-coder' }
  return nameMap[displayName] || displayName.toLowerCase().replace(/\s+/g, '-')
}

function selectStrategy(strategy: ApiReviewStrategy) {
  form.strategyId = strategy.strategyKey
  form.strategyKey = strategy.strategyKey
}

async function loadProjects() {
  loadingProjects.value = true
  try {
    const res = await get<PageResult<Project>>('/projects?pageNum=1&pageSize=100')
    if (res.data) {
      projectOptions.value = res.data.records
        .filter(p => p.status === 'READY')
        .map(p => ({ label: p.name, value: String(p.id) }))
    }
  } catch (e) { console.error('加载项目列表失败', e) }
  finally { loadingProjects.value = false }
}

async function loadBranches(projectId: string) {
  if (!projectId) {
    branchOptions.value = []
    return
  }
  loadingBranches.value = true
  try {
    const res = await get<string[]>(`/projects/${projectId}/branches`)
    if (res.data) {
      branchOptions.value = res.data.map(b => ({ label: b, value: b }))
    }
  } catch (e) { console.error('加载分支列表失败', e) }
  finally { loadingBranches.value = false }
}

watch(() => form.projectId, (newVal) => {
  form.sourceBranch = ''
  form.targetBranch = ''
  loadBranches(newVal)
})

async function loadStrategies() {
  loadingStrategies.value = true
  try {
    const res = await get<ApiReviewStrategy[]>('/model-config/strategies')
    if (res.data?.length) {
      strategyOptions.value = res.data.filter(s => s.enabled)
      if (strategyOptions.value.length && !form.strategyKey) {
        form.strategyKey = strategyOptions.value[0].strategyKey
        form.strategyId = strategyOptions.value[0].strategyKey
      }
    }
  } catch (e) { console.error('加载策略失败', e) }
  finally { loadingStrategies.value = false }
}

async function handleSubmit() {
  if (!form.projectId || !form.sourceBranch || !form.targetBranch || !form.strategyKey) return
  submitting.value = true
  try {
    const res = await post<Review>('/reviews', {
      projectId: Number(form.projectId),
      sourceBranch: form.sourceBranch,
      targetBranch: form.targetBranch,
      reviewMode: compiledConfig.value.reviewMode,
      modelsConfig: form.modelsConfigOverride.trim() || JSON.stringify(compiledConfig.value.modelsConfig),
    })
    if (res.data) navigateTo(`/reviews/${res.data.id}`)
  } catch { }
  finally { submitting.value = false }
}

onMounted(() => { loadProjects(); loadStrategies(); if (form.projectId) loadBranches(form.projectId) })
</script>