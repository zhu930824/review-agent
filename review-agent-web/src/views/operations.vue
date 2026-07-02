<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <!-- 标题栏 -->
    <a-space style="width:100%;justify-content:space-between;flex-wrap:wrap">
      <div>
        <h2 style="margin:0">运营中心</h2>
        <p style="margin-top:4px;color:#94a3b8;font-size:13px">把审查结果转成修复队列、SLA、规则学习和业务收益，让质量治理能持续推进。</p>
      </div>
      <a-space wrap>
        <RouterLink to="/governance">
          <a-button>
            <template #icon><AppstoreOutlined /></template>
            查看治理路线
          </a-button>
        </RouterLink>
        <RouterLink to="/reviews/create">
          <a-button type="primary">
            <template #icon><PlayCircleOutlined /></template>
            发起审查
          </a-button>
        </RouterLink>
      </a-space>
    </a-space>

    <!-- KPI 卡片 -->
    <a-row :gutter="16">
      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">运营就绪度</div>
              <div style="font-size:28px;font-weight:800;background:linear-gradient(135deg,#6366f1,#a855f7);-webkit-background-clip:text;-webkit-text-fill-color:transparent;margin-top:4px">
                {{ scorecard.operationalReadiness }}%
              </div>
            </div>
            <BarChartOutlined style="font-size:20px;color:#6366f1" />
          </a-space>
          <a-progress :percent="scorecard.operationalReadiness" :show-info="false" stroke-color="linear-gradient(90deg,#6366f1,#a855f7)" style="margin-top:8px" />
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">开放风险项</div>
              <div style="font-size:28px;font-weight:800;color:#d97706;margin-top:4px">{{ scorecard.openRiskItems }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">{{ scorecard.blockerCount }} 个 BLOCKER 需要优先处理</div>
            </div>
            <ExclamationCircleOutlined style="font-size:20px;color:#d97706" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">SLA 压力</div>
              <div style="font-size:28px;font-weight:800;color:#dc2626;margin-top:4px">{{ queueSummary.slaPressure }}%</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">{{ queueSummary.humanPendingCount }} 个等待人工确认</div>
            </div>
            <ClockCircleOutlined style="font-size:20px;color:#dc2626" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">月度收益估算</div>
              <div style="font-size:28px;font-weight:800;color:#059669;margin-top:4px">{{ businessImpact.hoursSaved }}h</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">另规避 {{ businessImpact.avoidedReworkHours }}h 返工</div>
            </div>
            <RiseOutlined style="font-size:20px;color:#059669" />
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 策略质量压力 -->
    <a-card size="small" style="margin-bottom:16px">
      <template #title>
        <a-space :size="8">
          <BarChartOutlined style="font-size:20px;color:#6366f1" />
          <span style="font-weight:600">策略成本/质量压力</span>
        </a-space>
      </template>
      <a-spin v-if="strategyPressureLoading" style="display:flex;justify-content:center;padding:16px 0" />
      <a-row v-else-if="strategyPressureItems.length" :gutter="12">
        <a-col v-for="strategy in strategyPressureItems" :key="strategy.strategyKey" :xl="8" :md="12" :span="24" style="margin-bottom:12px">
          <a-card size="small" :body-style="{ padding: '12px' }" style="background:#f8fafc">
            <a-space :size="8" style="width:100%;justify-content:space-between;align-items:flex-start">
              <div>
                <div style="font-weight:600;font-size:13px">{{ strategy.strategyKey }}</div>
                <div style="font-size:12px;color:#64748b;margin-top:4px">
                  压力分 {{ strategy.pressureScore }} · 确认率 {{ strategy.confirmationRatePercent }}% · 误报代理 {{ strategy.falsePositiveProxyPercent }}%
                </div>
                <div style="font-size:12px;color:#94a3b8;margin-top:4px">
                  失败率 {{ strategy.failureRatePercent }}% · 平均成本 {{ formatMicroCents(strategy.avgCostMicroCents) }} · {{ strategy.avgLatencyMs }}ms
                </div>
                <div style="font-size:12px;color:#94a3b8;margin-top:4px">
                  命中率 {{ strategy.strategyHitRatePercent }}% · 跨模型 {{ strategy.crossHitRatePercent }}% · Judge {{ strategy.judgeFailureRatePercent }}%
                </div>
              </div>
              <a-tag :color="pressureColor(strategy.pressureLevel)">{{ strategy.pressureLevel }}</a-tag>
            </a-space>
            <a-progress :percent="strategy.pressureScore" :show-info="false" size="small" style="margin-top:10px" />
            <div style="font-size:12px;color:#475569;margin-top:8px;line-height:1.5">{{ strategy.recommendation }}</div>
          </a-card>
        </a-col>
      </a-row>
      <a-row v-if="telemetryReadinessItems.length" :gutter="12" style="margin-top:4px">
        <a-col v-for="item in telemetryReadinessItems.slice(0, 3)" :key="item.strategyKey" :xl="8" :md="12" :span="24" style="margin-bottom:8px">
          <a-space :size="8" style="width:100%;justify-content:space-between;background:#f8fafc;padding:8px 10px;border-radius:6px">
            <div style="min-width:0">
              <div style="font-size:12px;font-weight:600;color:#334155">{{ item.strategyKey }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">调用 {{ item.totalCalls }} · 模型 {{ item.modelDiversity }} · 跨模型 {{ item.crossHitRatePercent }}%</div>
            </div>
            <a-tag :color="telemetryReadinessColor(item.readinessLevel)">{{ item.readinessLevel }}</a-tag>
          </a-space>
        </a-col>
      </a-row>
      <div v-if="!strategyPressureLoading && !strategyPressureItems.length" style="font-size:13px;color:#94a3b8;padding:12px 0">暂无模型遥测数据，真实调用接入后会显示策略成本/质量压力。</div>
    </a-card>

    <!-- 主内容区 -->
    <a-row :gutter="16">
      <!-- 修复队列表格 -->
      <a-col :xl="16" :span="24">
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <UnorderedListOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">修复队列</span>
            </a-space>
          </template>
          <template #extra>
            <a-tag color="processing">{{ queueSummary.total }} 项</a-tag>
          </template>
          <a-table
            :columns="queueColumns"
            :data-source="remediationQueue"
            :loading="remediationQueueLoading"
            :pagination="false"
            row-key="findingId"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'title'">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ record.title }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px">{{ record.projectName }} · Review #{{ record.reviewId }}</div>
                </div>
              </template>
              <template v-else-if="column.key === 'severity'">
                <a-tag :color="severityColor(record.severity)">{{ record.severity }}</a-tag>
              </template>
              <template v-else-if="column.key === 'ownerRole'">
                <a-tag>{{ record.ownerRole }}</a-tag>
              </template>
              <template v-else-if="column.key === 'slaHours'">
                <span style="font-weight:600;font-size:13px">{{ record.slaHours }}h</span>
              </template>
              <template v-else-if="column.key === 'priorityScore'">
                <a-tag color="orange">{{ record.priorityScore }}</a-tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space :size="4" wrap>
                  <a-button
                    type="link"
                    size="small"
                    :loading="actingFindingId === record.findingId && actingFindingAction === 'CONFIRM'"
                    @click="handleQueueAction(record, 'CONFIRM')"
                  >
                    确认有效
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    danger
                    :loading="actingFindingId === record.findingId && actingFindingAction === 'DISMISS'"
                    @click="handleQueueAction(record, 'DISMISS')"
                  >
                    标记误报
                  </a-button>
                  <a-button type="link" size="small" @click="router.push(`/reviews/${record.reviewId}`)">
                    查看 Review
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xl="8" :span="24">
        <!-- 责任人负载 -->
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <TeamOutlined style="font-size:16px;color:#6366f1" />
              <span style="font-weight:600;font-size:14px">责任人负载</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="12" style="width:100%">
            <div v-for="owner in ownerLoad" :key="owner.role">
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <span style="font-weight:500;font-size:13px">{{ owner.role }}</span>
                <span style="font-weight:600;font-size:13px">{{ owner.count }}</span>
              </a-space>
              <a-progress :percent="owner.percent" :show-info="false" size="small" style="margin-top:4px" />
            </div>
          </a-space>
        </a-card>

        <!-- 规则学习 -->
        <a-card size="small" title="规则学习">
          <template #title>
            <a-space :size="8">
              <ExperimentOutlined style="font-size:16px;color:#059669" />
              <span style="font-weight:600;font-size:14px">规则学习</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="candidate in learningCandidates"
              :key="candidate.findingId"
              size="small"
              :body-style="{ padding: '12px' }"
              style="background:#f8fafc"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ candidate.ruleTitle }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px;line-height:1.4">{{ candidate.reason }}</div>
                </div>
                <a-tag :color="candidate.action === 'PROMOTE_TO_RULE' ? 'green' : 'default'">
                  {{ candidate.action === 'PROMOTE_TO_RULE' ? '固化' : '降噪' }}
                </a-tag>
              </a-space>
            </a-card>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 运营节奏建议 -->
    <a-card size="small">
      <template #title>
        <a-space :size="8">
          <CalendarOutlined style="font-size:20px;color:#059669" />
          <span style="font-weight:600">运营节奏建议</span>
        </a-space>
      </template>
      <a-row :gutter="16">
        <a-col v-for="cadence in operatingCadences" :key="cadence.name" :md="8" :span="24" style="margin-bottom:12px">
          <a-card size="small" style="background:#f8fafc">
            <a-space :size="8">
              <div style="width:40px;height:40px;border-radius:12px;background:#eef2ff;display:flex;align-items:center;justify-content:center">
                <component :is="cadence.iconComp" style="font-size:20px;color:#6366f1" />
              </div>
              <div style="font-weight:600;font-size:13px">{{ cadence.name }}</div>
            </a-space>
            <div style="font-size:12px;color:#94a3b8;margin-top:8px;line-height:1.5">{{ cadence.description }}</div>
          </a-card>
        </a-col>
      </a-row>
      <div style="margin-top:12px;font-size:13px;color:#475569;line-height:1.5">{{ businessImpact.executiveSummary }}</div>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { BarChartOutlined, ExclamationCircleOutlined, ClockCircleOutlined, RiseOutlined, UnorderedListOutlined, TeamOutlined, ExperimentOutlined, CalendarOutlined, AppstoreOutlined, PlayCircleOutlined, SafetyCertificateOutlined, RocketOutlined } from '@ant-design/icons-vue'
import type { BusinessImpactEstimate, OperationDashboard, OperationOwnerLoad, OperationalFinding, RemediationQueueItem, RuleLearningCandidate, StrategyPressureItem, StrategyPressureLevel, TelemetryReadinessItem, TelemetryReadinessLevel } from '@/types/operations'
import type { SeverityLevel } from '@/types/review'
import { useApi } from '@/composables/useApi'
import { buildRemediationQueue, deriveOperationsScorecard, estimateReviewBusinessImpact, extractRuleLearningCandidates, summarizeRemediationQueue } from '@/utils/reviewOperations'

const fallbackBusinessImpact = estimateReviewBusinessImpact({ monthlyReviews: 80, averageManualReviewMinutes: 35, automationCoveragePercent: 65, blockerFindings: 6, majorFindings: 18 })
const router = useRouter()
const { get, post } = useApi()
const remediationQueueLoading = ref(false)
const actingFindingId = ref<number | null>(null)
const actingFindingAction = ref<'CONFIRM' | 'DISMISS' | null>(null)
const operationalFindings = ref<OperationalFinding[]>([])
const operationDashboard = ref<OperationDashboard | null>(null)
const backendOwnerLoad = ref<OperationOwnerLoad[]>([])
const backendRuleLearningCandidates = ref<RuleLearningCandidate[]>([])
const backendBusinessImpact = ref<BusinessImpactEstimate | null>(null)
const strategyPressureLoading = ref(false)
const strategyPressureItems = ref<StrategyPressureItem[]>([])
const telemetryReadinessItems = ref<TelemetryReadinessItem[]>([])
const remediationQueue = computed(() => buildRemediationQueue(operationalFindings.value))
const businessImpact = computed(() => backendBusinessImpact.value ?? fallbackBusinessImpact)
const learningCandidates = computed(() => backendRuleLearningCandidates.value.length
  ? backendRuleLearningCandidates.value
  : extractRuleLearningCandidates(operationalFindings.value))
const queueSummary = computed(() => {
  const localSummary = summarizeRemediationQueue(remediationQueue.value)
  const dashboard = operationDashboard.value
  if (!dashboard) {
    return localSummary
  }
  return {
    ...localSummary,
    total: dashboard.totalFindings,
    blockerCount: dashboard.blockerCount,
    humanPendingCount: dashboard.pendingCount,
    slaPressure: dashboard.slaPressure,
  }
})
const scorecard = computed(() => {
  const dashboard = operationDashboard.value
  if (!dashboard) {
    return deriveOperationsScorecard(operationalFindings.value)
  }
  const readinessPenalty = dashboard.blockerCount * 12 + dashboard.pendingCount * 6
  return {
    openRiskItems: dashboard.totalFindings,
    blockerCount: dashboard.blockerCount,
    ruleLearningCandidates: learningCandidates.value.length,
    topOwnerRole: ownerLoad.value[0]?.role ?? 'None',
    operationalReadiness: Math.max(0, Math.min(100, 88 - readinessPenalty + learningCandidates.value.length * 3)),
  }
})

const queueColumns = [
  { title: '风险项', key: 'title', dataIndex: 'title' },
  { title: '级别', key: 'severity', dataIndex: 'severity' },
  { title: '负责人', key: 'ownerRole', dataIndex: 'ownerRole' },
  { title: 'SLA', key: 'slaHours', dataIndex: 'slaHours' },
  { title: '优先级', key: 'priorityScore', dataIndex: 'priorityScore' },
  { title: '操作', key: 'actions', width: 220 },
]
const ownerLoad = computed(() => backendOwnerLoad.value.length
  ? backendOwnerLoad.value
  : Object.entries(queueSummary.value.byOwner).map(([role, count]) => ({ role, count, percent: queueSummary.value.total ? Math.round((count / queueSummary.value.total) * 100) : 0 })))

const operatingCadences = [
  { name: '每日风险清理', iconComp: SafetyCertificateOutlined, description: '每天处理 BLOCKER 和 24 小时内到期项，避免风险穿透到正式 PR。' },
  { name: '每周规则复盘', iconComp: ExperimentOutlined, description: '将确认问题固化为规则，将低置信误报加入降噪样例，持续提高模型可用性。' },
  { name: '发布前门禁', iconComp: RocketOutlined, description: '核心链路发布前检查安全、性能、异常处理和人工确认记录。' },
]

function severityColor(severity: SeverityLevel) { return { BLOCKER: 'red', MAJOR: 'orange', MINOR: 'blue', INFO: 'default' }[severity] }

function pressureColor(level: StrategyPressureLevel) {
  return { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }[level]
}

function telemetryReadinessColor(level: TelemetryReadinessLevel) {
  return { READY: 'green', NEEDS_ATTRIBUTION: 'orange', JUDGE_UNSTABLE: 'red', NOT_CONNECTED: 'default' }[level]
}

function formatMicroCents(n: number): string {
  return `$${(n / 100000000).toFixed(4)}`
}

async function loadStrategyPressure() {
  strategyPressureLoading.value = true
  try {
    const res = await get<StrategyPressureItem[]>('/operations/strategy-pressure')
    if (res.data) strategyPressureItems.value = res.data.slice(0, 3)
  } catch (e) {
    console.error(e)
  } finally {
    strategyPressureLoading.value = false
  }
}

async function loadTelemetryReadiness() {
  try {
    const res = await get<TelemetryReadinessItem[]>('/operations/telemetry-readiness')
    if (res.data) telemetryReadinessItems.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadOperationsDashboard() {
  try {
    const res = await get<OperationDashboard>('/operations/dashboard')
    if (res.data) operationDashboard.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadOwnerLoad() {
  try {
    const res = await get<OperationOwnerLoad[]>('/operations/owner-load')
    if (res.data) backendOwnerLoad.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadRemediationQueue() {
  remediationQueueLoading.value = true
  try {
    const res = await get<OperationalFinding[]>('/operations/remediation-queue?limit=20')
    if (res.data) operationalFindings.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    remediationQueueLoading.value = false
  }
}

async function loadRuleLearningCandidates() {
  try {
    const res = await get<RuleLearningCandidate[]>('/operations/rule-learning-candidates?limit=20')
    if (res.data) backendRuleLearningCandidates.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadBusinessImpact() {
  try {
    const res = await get<BusinessImpactEstimate>('/operations/business-impact')
    if (res.data) backendBusinessImpact.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function reloadOperationsWorkflows() {
  await Promise.all([
    loadOperationsDashboard(),
    loadOwnerLoad(),
    loadRemediationQueue(),
    loadRuleLearningCandidates(),
    loadBusinessImpact(),
  ])
}

async function handleQueueAction(record: RemediationQueueItem, action: 'CONFIRM' | 'DISMISS') {
  actingFindingId.value = record.findingId
  actingFindingAction.value = action
  const endpoint = action === 'CONFIRM'
    ? `/operations/remediation-queue/${record.findingId}/confirm`
    : `/operations/remediation-queue/${record.findingId}/dismiss`
  try {
    await post(endpoint)
    message.success(action === 'CONFIRM' ? '已确认风险项有效' : '已标记为误报')
    await reloadOperationsWorkflows()
  } catch (e) {
    console.error('更新运营队列项失败', e)
    message.error('更新运营队列项失败')
  } finally {
    actingFindingId.value = null
    actingFindingAction.value = null
  }
}

onMounted(() => {
  loadOperationsDashboard()
  loadOwnerLoad()
  loadStrategyPressure()
  loadTelemetryReadiness()
  loadRemediationQueue()
  loadRuleLearningCandidates()
  loadBusinessImpact()
})
</script>
