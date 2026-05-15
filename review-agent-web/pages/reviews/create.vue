<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">发起 Pre-PR 审查</h2>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">选择项目、分支和审查策略，生成提交前质量结论。</p>
      </div>
      <UButton to="/settings/models" variant="soft" color="gray" icon="i-heroicons-cog-6-tooth">
        模型配置
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
      <UCard>
        <form class="space-y-6" @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
            <UFormGroup label="选择项目" name="projectId" required>
              <USelect v-model="form.projectId" :options="projectOptions" placeholder="选择项目" searchable />
            </UFormGroup>

            <UFormGroup label="源分支" name="sourceBranch" required>
              <UInput v-model="form.sourceBranch" placeholder="feature/order-risk" icon="i-heroicons-code-bracket" />
            </UFormGroup>

            <UFormGroup label="目标分支" name="targetBranch" required>
              <UInput v-model="form.targetBranch" placeholder="main" icon="i-heroicons-code-bracket" />
            </UFormGroup>
          </div>

          <UDivider label="审查策略" />

          <!-- 加载中状态 -->
          <div v-if="loadingStrategies" class="flex items-center justify-center gap-2 py-8 text-sm text-gray-400">
            <UIcon name="i-heroicons-arrow-path" class="h-5 w-5 animate-spin" />
            <span>加载审查策略中...</span>
          </div>

          <!-- 加载失败状态 -->
          <div v-else-if="errorLoadingStrategies" class="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-600 dark:border-red-800 dark:bg-red-950/20 dark:text-red-400">
            加载审查策略失败，请刷新页面重试。
          </div>

          <!-- 策略卡片列表 -->
          <div v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
            <button
              v-for="strategy in strategies"
              :key="strategy.strategyKey"
              type="button"
              class="rounded-lg border p-4 text-left transition hover:border-primary-400 hover:bg-primary-50/40 dark:hover:bg-primary-950/20"
              :class="form.strategyId === strategy.strategyKey ? 'border-primary-500 ring-2 ring-primary-100 dark:ring-primary-900' : 'border-gray-200 dark:border-gray-700'"
              @click="form.strategyId = strategy.strategyKey"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <h3 class="text-sm font-semibold text-gray-900 dark:text-white">{{ strategy.name }}</h3>
                  <p class="mt-1 text-xs leading-5 text-gray-500 dark:text-gray-400">{{ strategy.description }}</p>
                </div>
                <UBadge :label="strategy.reviewMode" variant="subtle" color="primary" size="xs" />
              </div>
              <div class="mt-3 flex flex-wrap gap-1">
                <UBadge
                  v-for="item in strategy.recommendedFor"
                  :key="item"
                  :label="item"
                  color="gray"
                  variant="subtle"
                  size="xs"
                />
              </div>
            </button>
          </div>

          <!-- 策略模型编排：仅在选中策略后渲染 -->
          <div v-if="selectedStrategy" class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-semibold text-gray-900 dark:text-white">策略模型编排</h3>
                <p class="mt-1 text-xs text-gray-500">提交时会编译为后端兼容的 modelsConfig。</p>
              </div>
              <UBadge :label="selectedStrategy.reviewMode" color="primary" variant="subtle" />
            </div>

            <div class="mt-4 space-y-2">
              <div
                v-for="label in selectedRoleLabels"
                :key="label"
                class="flex items-center gap-2 rounded-md bg-gray-50 px-3 py-2 text-sm text-gray-700 dark:bg-gray-800 dark:text-gray-200"
              >
                <UIcon name="i-heroicons-cpu-chip" class="h-4 w-4 text-primary-500" />
                <span>{{ label }}</span>
              </div>
            </div>
          </div>

          <UFormGroup label="MCP 外部工具">
            <UToggle v-model="form.mcpEnabled" />
          </UFormGroup>

          <UAccordion
            :items="[{ label: '高级 JSON 配置', icon: 'i-heroicons-code-bracket-square', slot: 'advanced' }]"
            variant="ghost"
          >
            <template #advanced>
              <UTextarea v-model="form.modelsConfigOverride" :rows="6" :placeholder="compiledJsonPreview" />
              <p class="mt-2 text-xs text-gray-500">填写后将覆盖策略自动生成的模型配置。</p>
            </template>
          </UAccordion>

          <div class="flex items-center gap-3 pt-2">
            <UButton type="submit" variant="solid" color="primary" :loading="submitting" icon="i-heroicons-play">
              开始审查
            </UButton>
            <UButton to="/" variant="ghost" color="gray">取消</UButton>
          </div>
        </form>
      </UCard>

      <aside class="space-y-4">
        <UCard>
          <template #header>
            <h3 class="text-sm font-semibold text-gray-900 dark:text-white">质量闸门</h3>
          </template>
          <div class="space-y-3 text-sm">
            <div>
              <p class="text-xs text-gray-500">阻断级别</p>
              <div class="mt-1 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.blockOn" :key="level" :label="level" color="red" variant="subtle" />
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500">需人工复核</p>
              <div class="mt-1 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.requireHumanReviewOn" :key="level" :label="level" color="orange" variant="subtle" />
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500">建议关注</p>
              <div class="mt-1 flex flex-wrap gap-1">
                <UBadge v-for="level in gatePolicy.advisoryOn" :key="level" :label="level" color="blue" variant="subtle" />
              </div>
            </div>
          </div>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="text-sm font-semibold text-gray-900 dark:text-white">配置预览</h3>
          </template>
          <pre class="max-h-96 overflow-auto rounded-md bg-gray-950 p-3 text-xs text-gray-100">{{ compiledJsonPreview }}</pre>
        </UCard>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { Review } from '~/types/review'
import type { PageResult } from '~/types/api'

// ============================================================
// 1. 从后端 API 获取的策略 VO（值对象）
// ============================================================
interface StrategyVO {
  id: number
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

// ============================================================
// 策略角色中文标签（保持与旧版一致）
// ============================================================
const roleLabels: Record<string, string> = {
  WORKER: '审查模型',
  JUDGE: 'Judge 模型',
  SECURITY_AUDITOR: '安全审计员',
  PERFORMANCE_ANALYST: '性能分析师',
  CODE_STYLE_CHECKER: '代码规范检查员',
  EXCEPTION_HANDLER: '异常处理专家',
  ARCHITECT_REVIEWER: '架构评审员',
}

const route = useRoute()
const { get, post } = useApi()
const submitting = ref(false)

// ============================================================
// 2. 从后端加载审查策略
// ============================================================
const strategies = ref<StrategyVO[]>([])
const loadingStrategies = ref(true)
const errorLoadingStrategies = ref(false)

const projectOptions = ref<{ label: string; value: string }[]>([])

const form = reactive({
  projectId: (route.query.projectId as string) || '',
  // 策略 ID 现在对应后端 API 返回的 strategyKey
  strategyId: 'quality-gate',
  sourceBranch: '',
  targetBranch: 'main',
  mcpEnabled: false,
  modelsConfigOverride: '',
})

// ============================================================
// 5. 根据 strategyKey 从已加载的策略中查找当前选中策略
// ============================================================
const selectedStrategy = computed<StrategyVO | undefined>(() =>
  strategies.value.find(s => s.strategyKey === form.strategyId),
)

// 6. 门禁策略和角色标签均从 selectedStrategy 派生
const gatePolicy = computed(() =>
  selectedStrategy.value?.gatePolicy ?? { blockOn: [], requireHumanReviewOn: [], advisoryOn: [] },
)

const selectedRoleLabels = computed<string[]>(() => {
  if (!selectedStrategy.value) return []
  return selectedStrategy.value.roleBindings.map(
    b => `${roleLabels[b.role] ?? b.role} · ${b.modelProfileName}`,
  )
})

// ============================================================
// 7. 直接从策略 roleBindings 构建 modelsConfig（不再依赖 compileStrategyConfig）
// ============================================================
function buildModelsConfig(strategy: StrategyVO): Record<string, unknown> {
  const { strategyKey, reviewMode, gatePolicy, roleBindings } = strategy

  if (reviewMode === 'AGENT') {
    return {
      agents: roleBindings.map(b => ({
        role: b.role,
        modelName: b.modelName,
        skills: b.skills,
        temperature: b.temperature,
      })),
      orchestrationStrategy: 'PARALLEL',
      mcpEnabled: false,
      mcpServerUrls: [],
      gatePolicy,
      strategyKey,
    }
  }

  if (reviewMode === 'JUDGE') {
    const judgeBinding = roleBindings.find(b => b.role === 'JUDGE')
    return {
      workers: roleBindings.filter(b => b.role !== 'JUDGE').map(b => b.modelName),
      judge: judgeBinding?.modelName ?? '',
      gatePolicy,
      strategyKey,
    }
  }

  // MULTI / SINGLE
  const models = reviewMode === 'SINGLE'
    ? roleBindings.slice(0, 1).map(b => b.modelName)
    : roleBindings.map(b => b.modelName)

  return { models, gatePolicy, strategyKey }
}

const compiledConfig = computed(() => {
  if (!selectedStrategy.value) return { reviewMode: '', modelsConfig: {} }
  const modelsConfig = buildModelsConfig(selectedStrategy.value)
  // AGENT 模式下 mcpEnabled 由表单开关控制
  if ('mcpEnabled' in modelsConfig) {
    (modelsConfig as Record<string, unknown>).mcpEnabled = form.mcpEnabled
  }
  return { reviewMode: selectedStrategy.value.reviewMode, modelsConfig }
})

const compiledJsonPreview = computed(() => JSON.stringify(compiledConfig.value.modelsConfig, null, 2))

// ============================================================
// 4. 加载策略：在 onMounted 中调用 API
// ============================================================
async function loadStrategies() {
  loadingStrategies.value = true
  errorLoadingStrategies.value = false
  try {
    const res = await get<StrategyVO[]>('/api/model-config/strategies')
    if (res.data) {
      strategies.value = res.data.filter(s => s.enabled)
      // 如果当前 strategyId 在已加载策略中不存在，回退到第一个可用策略
      if (strategies.value.length > 0 && !strategies.value.some(s => s.strategyKey === form.strategyId)) {
        form.strategyId = strategies.value[0].strategyKey
      }
    }
  } catch {
    errorLoadingStrategies.value = true
  } finally {
    loadingStrategies.value = false
  }
}

async function loadProjects() {
  try {
    const res = await get<PageResult<Project>>('/projects?pageNum=1&pageSize=100')
    if (res.data) {
      projectOptions.value = res.data.records
        .filter(p => p.status === 'READY')
        .map(p => ({ label: p.name, value: String(p.id) }))
    }
  } catch (e) {
    console.error('加载项目列表失败', e)
  }
}

async function handleSubmit() {
  if (!form.projectId || !form.sourceBranch || !form.targetBranch) return
  submitting.value = true
  try {
    const params = {
      projectId: Number(form.projectId),
      sourceBranch: form.sourceBranch,
      targetBranch: form.targetBranch,
      reviewMode: compiledConfig.value.reviewMode,
      modelsConfig: form.modelsConfigOverride.trim() || JSON.stringify(compiledConfig.value.modelsConfig),
    }

    const res = await post<Review>('/reviews', params)
    if (res.data) {
      navigateTo(`/reviews/${res.data.id}`)
    }
  } catch {
    // useApi 已统一记录请求错误
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProjects()
  loadStrategies()
})
</script>
