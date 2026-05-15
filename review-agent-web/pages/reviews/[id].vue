<template>
  <div class="grid gap-6">
    <div class="animate-slide-up">
      <UButton to="/" variant="ghost" color="gray" icon="i-heroicons-arrow-left" size="sm">返回质量驾驶舱</UButton>
    </div>

    <!-- 进度区域（审查中时显示） -->
    <div v-if="detail?.status === 'RUNNING' || detail?.status === 'PENDING'" class="panel-soft p-6 animate-slide-up">
      <div class="flex items-center gap-3 mb-5">
        <div class="icon-soft icon-soft-info" style="width:40px;height:40px;border-radius:12px;">
          <UIcon name="i-heroicons-arrow-path" class="h-5 w-5 animate-spin" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white">审查进行中</h3>
          <p class="text-sm text-slate-500 dark:text-slate-400">正在执行 Agent 审查，请稍候...</p>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="mb-6">
        <div class="flex items-center justify-between text-sm mb-2">
          <span class="text-slate-600 dark:text-slate-300">整体进度</span>
          <span class="font-semibold text-indigo-600 dark:text-indigo-400">{{ progressPercent }}%</span>
        </div>
        <div class="h-3 bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
          <div class="h-full bg-gradient-to-result from-indigo-500 to-purple-500 rounded-full transition-all duration-500"
               :style="{ width: `${progressPercent}%` }"></div>
        </div>
      </div>

      <!-- Agent 执行状态 -->
      <div class="grid gap-3">
        <div v-for="agent in agentProgress" :key="agent.role"
             class="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-700/30">
          <div class="flex-shrink-0">
            <div v-if="agent.status === 'RUNNING'" class="h-8 w-8 rounded-xl bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center">
              <UIcon name="i-heroicons-arrow-path" class="h-4 w-4 text-blue-500 animate-spin" />
            </div>
            <div v-else-if="agent.status === 'COMPLETED'" class="h-8 w-8 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
              <UIcon name="i-heroicons-check" class="h-4 w-4 text-emerald-500" />
            </div>
            <div v-else-if="agent.status === 'FAILED'" class="h-8 w-8 rounded-xl bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
              <UIcon name="i-heroicons-x-mark" class="h-4 w-4 text-red-500" />
            </div>
            <div v-else class="h-8 w-8 rounded-xl bg-slate-100 dark:bg-slate-600 flex items-center justify-center">
              <UIcon name="i-heroicons-clock" class="h-4 w-4 text-slate-400" />
            </div>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <span class="text-sm font-bold text-slate-800 dark:text-white">{{ roleLabel(agent.role) }}</span>
              <UBadge v-if="agent.modelName" :label="agent.modelName" color="slate" variant="subtle" size="xs" />
            </div>
            <p v-if="agent.message" class="mt-1 text-xs text-slate-500 dark:text-slate-400 truncate">{{ agent.message }}</p>
            <p v-else-if="agent.findingCount !== undefined" class="mt-1 text-xs text-emerald-600 dark:text-emerald-400">
              发现 {{ agent.findingCount }} 个问题
            </p>
          </div>
          <UBadge v-if="agent.status" :color="statusColor(agent.status)" :label="statusLabel(agent.status)" variant="subtle" size="xs" />
        </div>
      </div>

      <!-- 实时输出区域 -->
      <div v-if="agentOutputs.length" class="mt-6">
        <h4 class="text-sm font-bold text-slate-800 dark:text-white mb-3">实时输出</h4>
        <div class="bg-slate-900 rounded-2xl p-4 max-h-64 overflow-auto">
          <div v-for="(output, idx) in agentOutputs" :key="idx" class="text-xs font-mono mb-2">
            <span class="text-slate-500">[{{ output.time }}]</span>
            <span :class="output.type === 'error' ? 'text-red-400' : 'text-emerald-400'">[{{ output.role }}]</span>
            <span class="text-slate-300 ml-2">{{ output.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white dark:bg-slate-800 shadow-md">
        <UIcon name="i-heroicons-arrow-path" class="h-6 w-6 animate-spin text-indigo-500" />
      </div>
    </div>

    <template v-else-if="detail">
      <div class="panel-soft p-6 animate-slide-up stagger-2">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div class="flex items-center gap-3 mb-2">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-md shadow-indigo-500/25">
                <UIcon name="i-heroicons-document-magnifying-glass" class="h-5 w-5 text-white" />
              </div>
              <h2 class="text-xl font-bold text-slate-800 dark:text-white">审查详情</h2>
            </div>
            <p class="text-sm text-slate-500 dark:text-slate-400 ml-14">
              {{ detail.projectName || '未知项目' }} · {{ detail.sourceBranch || '-' }} → {{ detail.targetBranch || '-' }}
            </p>
            <p v-if="parsedSummary" class="mt-3 max-w-3xl text-sm leading-6 text-slate-600 dark:text-slate-300">
              {{ parsedSummary }}
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <UBadge :color="statusColor(detail.status)" :label="statusLabel(detail.status)" variant="subtle" />
            <UBadge :color="gateStatusColor" :label="gateStatusLabel" variant="subtle" />
            <UBadge :label="detail.reviewMode" color="slate" variant="subtle" />
          </div>
        </div>

        <div class="mt-6 grid grid-cols-2 gap-6 md:grid-cols-5">
          <div v-for="item in severityStats" :key="item.label" class="rounded-2xl p-4" :class="item.bgClass">
            <p class="text-xs font-semibold" :class="item.textClass">{{ item.label }}</p>
            <p class="mt-1 text-xl font-extrabold" :class="item.valueClass">{{ item.value }}</p>
          </div>
        </div>
      </div>

      <div v-if="blockedReasons.length" class="panel-soft animate-slide-up stagger-3">
        <div class="flex items-center gap-3 px-6 py-4 border-b border-red-200/50 dark:border-red-700/50">
          <div class="icon-soft icon-soft-error" style="width:36px;height:36px;border-radius:10px;">
            <UIcon name="i-heroicons-shield-exclamation" class="h-4 w-4" />
          </div>
          <h3 class="text-lg font-bold text-red-600 dark:text-red-400">Pre-PR 阻断原因</h3>
        </div>
        <ul class="p-6 space-y-2">
          <li v-for="reason in blockedReasons" :key="reason" class="flex items-start gap-3 text-sm text-red-600 dark:text-red-300">
            <span class="mt-1.5 h-2 w-2 rounded-full bg-red-500 flex-shrink-0"></span>
            <span>{{ reason }}</span>
          </li>
        </ul>
      </div>

      <div class="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
        <div class="soft-card-flat animate-slide-up stagger-4">
          <div class="flex flex-col gap-3 px-6 py-4 border-b border-slate-100 dark:border-slate-700/50 lg:flex-row lg:items-center lg:justify-between">
            <div class="flex items-center gap-3">
              <div class="icon-soft icon-soft-warning" style="width:40px;height:40px;border-radius:12px;">
                <UIcon name="i-heroicons-exclamation-triangle" class="h-5 w-5" />
              </div>
              <h3 class="text-lg font-bold text-slate-800 dark:text-white">发现问题（{{ filteredFindings.length }} / {{ detail.totalFindings }}）</h3>
            </div>
            <div class="flex items-center gap-2">
              <USelect v-model="severityFilter" :options="severityOptions" placeholder="严重度" size="sm" class="w-36" />
              <USelect v-model="categoryFilter" :options="categoryOptions" placeholder="分类" size="sm" class="w-40" />
            </div>
          </div>

          <div class="grid gap-3 p-4">
            <FindingCard v-for="finding in filteredFindings" :key="finding.id" :finding="finding" @confirm="updateFindingStatus(finding.id, 'CONFIRMED')" @dismiss="updateFindingStatus(finding.id, 'DISMISSED')" />
          </div>

          <div v-if="!filteredFindings.length" class="py-8 text-center text-slate-500">
            <UIcon name="i-heroicons-check-badge" class="mx-auto h-10 w-10 text-emerald-300" />
            <p class="mt-2">没有匹配的问题</p>
          </div>
        </div>

        <aside class="grid content-start gap-6">
          <div v-if="detail.modelResults.length" class="panel-soft p-5 animate-slide-up stagger-4">
            <div class="flex items-center gap-3 mb-4">
              <div class="icon-soft icon-soft-primary" style="width:36px;height:36px;border-radius:10px;">
                <UIcon name="i-heroicons-cpu-chip" class="h-4 w-4" />
              </div>
              <h3 class="text-sm font-bold text-slate-800 dark:text-white">模型执行</h3>
            </div>
            <div class="grid gap-3">
              <div v-for="result in detail.modelResults" :key="result.id" class="rounded-2xl bg-slate-50 dark:bg-slate-700/30 p-4">
                <div class="flex items-center justify-between gap-2">
                  <div>
                    <p class="text-sm font-bold text-slate-800 dark:text-white">{{ result.modelName }}</p>
                    <p class="text-xs text-slate-500">{{ result.role }}</p>
                  </div>
                  <UBadge :color="statusColor(result.status)" :label="statusLabel(result.status)" variant="subtle" size="xs" />
                </div>
                <p v-if="result.errorMessage" class="mt-2 text-xs text-red-500">{{ result.errorMessage }}</p>
              </div>
            </div>
          </div>

          <div class="panel-soft p-5 animate-slide-up stagger-5">
            <div class="flex items-center gap-3 mb-4">
              <div class="icon-soft icon-soft-success" style="width:36px;height:36px;border-radius:10px;">
                <UIcon name="i-heroicons-user-group" class="h-4 w-4" />
              </div>
              <h3 class="text-sm font-bold text-slate-800 dark:text-white">人工处理</h3>
            </div>
            <div class="grid gap-3">
              <div class="flex items-center justify-between rounded-2xl bg-amber-50/50 dark:bg-amber-900/10 px-4 py-3">
                <span class="text-sm text-slate-500 dark:text-slate-400 font-medium">待确认</span>
                <span class="text-lg font-extrabold text-amber-600 dark:text-amber-400">{{ pendingFindings }}</span>
              </div>
              <div class="flex items-center justify-between rounded-2xl bg-emerald-50/50 dark:bg-emerald-900/10 px-4 py-3">
                <span class="text-sm text-slate-500 dark:text-slate-400 font-medium">已确认</span>
                <span class="text-lg font-extrabold text-emerald-600 dark:text-emerald-400">{{ confirmedFindings }}</span>
              </div>
              <div class="flex items-center justify-between rounded-2xl bg-slate-50/50 dark:bg-slate-700/30 px-4 py-3">
                <span class="text-sm text-slate-500 dark:text-slate-400 font-medium">已忽略</span>
                <span class="text-lg font-extrabold text-slate-600 dark:text-slate-300">{{ dismissedFindings }}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { ReviewDetail, HumanStatus, ReviewStatus } from '~/types/review'
import { deriveGateStatus, generateBlockedReasons } from '~/utils/reviewMetrics'

interface AgentProgressItem {
  role: string
  modelName?: string
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  message?: string
  findingCount?: number
}

interface AgentOutput {
  time: string
  role: string
  message: string
  type: 'info' | 'error'
}

const route = useRoute()
const { get, patch } = useApi()
const reviewId = computed(() => route.params.id as string)
const loading = ref(false)
const detail = ref<ReviewDetail | null>(null)
const severityFilter = ref('all')
const categoryFilter = ref('all')

const agentProgress = ref<AgentProgressItem[]>([])
const agentOutputs = ref<AgentOutput[]>([])
let eventSource: EventSource | null = null

const severityOptions = [{ label: '全部严重度', value: 'all' }, { label: '阻断', value: 'BLOCKER' }, { label: '重要', value: 'MAJOR' }, { label: '一般', value: 'MINOR' }, { label: '提示', value: 'INFO' }]
const categoryOptions = [{ label: '全部分类', value: 'all' }, { label: '代码风格', value: 'CODE_STYLE' }, { label: '潜在缺陷', value: 'BUG' }, { label: '性能问题', value: 'PERFORMANCE' }, { label: '安全问题', value: 'SECURITY' }, { label: '异常处理', value: 'EXCEPTION_HANDLING' }]

const gateStatus = computed(() => detail.value ? deriveGateStatus(detail.value) : 'RUNNING')
const blockedReasons = computed(() => detail.value ? generateBlockedReasons(detail.value) : [])
const gateStatusLabel = computed(() => ({ PASSED: 'Pre-PR 通过', BLOCKED: 'Pre-PR 阻断', NEEDS_HUMAN_REVIEW: '待人工复核', RUNNING: '审查中' }[gateStatus.value] ?? gateStatus.value))
const gateStatusColor = computed(() => ({ PASSED: 'green', BLOCKED: 'red', NEEDS_HUMAN_REVIEW: 'orange', RUNNING: 'blue' }[gateStatus.value] ?? 'gray'))

const progressPercent = computed(() => {
  if (agentProgress.value.length === 0) return 0
  const completed = agentProgress.value.filter(a => a.status === 'COMPLETED' || a.status === 'FAILED').length
  return Math.round((completed / agentProgress.value.length) * 100)
})

const filteredFindings = computed(() => {
  let items = detail.value?.findings ?? []
  if (severityFilter.value !== 'all') items = items.filter(f => f.severity === severityFilter.value)
  if (categoryFilter.value !== 'all') items = items.filter(f => f.category === categoryFilter.value)
  return items
})

const pendingFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'PENDING').length)
const confirmedFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'CONFIRMED').length)
const dismissedFindings = computed(() => (detail.value?.findings ?? []).filter(item => item.humanStatus === 'DISMISSED').length)

const severityStats = computed(() => [
  { label: '阻断', value: detail.value?.blockerCount ?? 0, bgClass: 'bg-gradient-to-br from-red-50 to-rose-50 dark:from-red-900/15 dark:to-rose-900/10', textClass: 'text-red-600', valueClass: 'text-red-700 dark:text-red-300' },
  { label: '重要', value: detail.value?.majorCount ?? 0, bgClass: 'bg-gradient-to-br from-orange-50 to-amber-50 dark:from-orange-900/15 dark:to-amber-900/10', textClass: 'text-orange-600', valueClass: 'text-orange-700 dark:text-orange-300' },
  { label: '一般', value: detail.value?.minorCount ?? 0, bgClass: 'bg-gradient-to-br from-yellow-50 to-amber-50 dark:from-yellow-900/15 dark:to-amber-900/10', textClass: 'text-yellow-600', valueClass: 'text-yellow-700 dark:text-yellow-300' },
  { label: '提示', value: detail.value?.infoCount ?? 0, bgClass: 'bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-900/15 dark:to-indigo-900/10', textClass: 'text-blue-600', valueClass: 'text-blue-700 dark:text-blue-300' },
  { label: '交叉命中', value: (detail.value?.findings ?? []).filter(item => item.isCrossHit).length, bgClass: 'bg-gradient-to-br from-purple-50 to-violet-50 dark:from-purple-900/15 dark:to-violet-900/10', textClass: 'text-purple-600', valueClass: 'text-purple-700 dark:text-purple-300' },
])

const parsedSummary = computed(() => {
  const summary = detail.value?.summary
  if (!summary) return ''
  try { const parsed = JSON.parse(summary); return typeof parsed === 'string' ? parsed : parsed.summary || parsed.conclusion || parsed.description || '' }
  catch { return summary }
})

function statusColor(status: string): string { return { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }[status] ?? 'gray' }
function statusLabel(status: string): string { return { PENDING: '等待中', RUNNING: '执行中', COMPLETED: '已完成', FAILED: '失败' }[status] ?? status }
function roleLabel(role: string): string {
  const labels: Record<string, string> = {
    SECURITY_AUDITOR: '安全审计员',
    PERFORMANCE_ANALYST: '性能分析师',
    CODE_STYLE_CHECKER: '代码规范检查员',
    EXCEPTION_HANDLER: '异常处理专家',
    ARCHITECT_REVIEWER: '架构评审员',
    WORKER: '审查模型',
    JUDGE: 'Judge 模型'
  }
  return labels[role] || role
}

function addOutput(role: string, message: string, type: 'info' | 'error' = 'info') {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  agentOutputs.value.push({ time, role, message, type })
  if (agentOutputs.value.length > 100) agentOutputs.value.shift()
}

function connectSSE() {
  if (eventSource) eventSource.close()
  const config = useRuntimeConfig()
  const baseUrl = config.public.apiBaseUrl as string
  const url = `${baseUrl}/reviews/${reviewId.value}/progress`
  eventSource = new EventSource(url)

  eventSource.addEventListener('progress', (event) => {
    try {
      const data = JSON.parse(event.data)
      handleProgressEvent(data)
    } catch (e) {
      console.error('解析 SSE 数据失败', e)
    }
  })

  eventSource.onerror = () => {
    eventSource?.close()
    setTimeout(connectSSE, 3000)
  }
}

function handleProgressEvent(data: any) {
  switch (data.type) {
    case 'initial_state':
      if (data.modelResults) {
        agentProgress.value = data.modelResults.map((result: any) => ({
          role: result.role,
          modelName: result.modelName,
          status: result.status
        }))
      }
      break
    case 'agent_started':
      const existing = agentProgress.value.find(a => a.role === data.agentRole)
      if (existing) {
        existing.status = 'RUNNING'
        existing.modelName = data.modelName
      } else {
        agentProgress.value.push({
          role: data.agentRole,
          modelName: data.modelName,
          status: 'RUNNING'
        })
      }
      addOutput(data.agentRole, '开始审查...')
      break
    case 'agent_progress':
      const agent = agentProgress.value.find(a => a.role === data.agentRole)
      if (agent) {
        agent.message = data.message
      }
      addOutput(data.agentRole, data.message)
      break
    case 'agent_completed':
      const completed = agentProgress.value.find(a => a.role === data.agentRole)
      if (completed) {
        completed.status = 'COMPLETED'
        completed.findingCount = data.findingCount
        completed.message = undefined
      }
      addOutput(data.agentRole, `审查完成，发现 ${data.findingCount} 个问题`)
      break
    case 'agent_failed':
      const failed = agentProgress.value.find(a => a.role === data.agentRole)
      if (failed) {
        failed.status = 'FAILED'
        failed.message = data.message
      }
      addOutput(data.agentRole, data.message, 'error')
      break
    case 'review_completed':
      loadDetail()
      eventSource?.close()
      break
  }
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await get<ReviewDetail>(`/reviews/${reviewId.value}`)
    if (res.data) detail.value = res.data
  } catch (e) { console.error('加载审查详情失败', e) }
  finally { loading.value = false }
}

async function updateFindingStatus(findingId: number, status: HumanStatus) {
  try {
    await patch(`/reviews/${reviewId.value}/finding/${findingId}`, { humanStatus: status })
    await loadDetail()
  } catch (e) { console.error('更新问题状态失败', e) }
}

onMounted(() => {
  loadDetail().then(() => {
    if (detail.value?.status === 'RUNNING' || detail.value?.status === 'PENDING') {
      connectSSE()
    }
  })
})

onUnmounted(() => {
  eventSource?.close()
})
</script>
