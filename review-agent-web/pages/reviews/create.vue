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

          <div class="grid grid-cols-1 gap-3 lg:grid-cols-2">
            <button
              v-for="strategy in builtinReviewStrategies"
              :key="strategy.id"
              type="button"
              class="rounded-lg border p-4 text-left transition hover:border-primary-400 hover:bg-primary-50/40 dark:hover:bg-primary-950/20"
              :class="form.strategyId === strategy.id ? 'border-primary-500 ring-2 ring-primary-100 dark:ring-primary-900' : 'border-gray-200 dark:border-gray-700'"
              @click="form.strategyId = strategy.id"
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

          <div class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
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
import type { ReviewStrategyId } from '~/types/model-config'
import type { PageResult } from '~/types/api'
import {
  builtinReviewStrategies,
  compileStrategyConfig,
  deriveGatePolicy,
  getReviewStrategy,
  getStrategyRoleLabels,
} from '~/utils/modelStrategies'

const route = useRoute()
const { get, post } = useApi()
const submitting = ref(false)

const projectOptions = ref<{ label: string; value: string }[]>([])

const form = reactive({
  projectId: (route.query.projectId as string) || '',
  strategyId: 'quality-gate' as ReviewStrategyId,
  sourceBranch: '',
  targetBranch: 'main',
  mcpEnabled: false,
  modelsConfigOverride: '',
})

const selectedStrategy = computed(() => getReviewStrategy(form.strategyId))
const gatePolicy = computed(() => deriveGatePolicy(form.strategyId))
const selectedRoleLabels = computed(() => getStrategyRoleLabels(form.strategyId))

const compiledConfig = computed(() => {
  const compiled = compileStrategyConfig(form.strategyId)
  if ('mcpEnabled' in compiled.modelsConfig) {
    compiled.modelsConfig.mcpEnabled = form.mcpEnabled
  }
  return compiled
})

const compiledJsonPreview = computed(() => JSON.stringify(compiledConfig.value.modelsConfig, null, 2))

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

onMounted(loadProjects)
</script>
