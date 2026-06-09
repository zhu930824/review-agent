<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <!-- 返回按钮 -->
    <div>
      <RouterLink to="/">
        <a-button size="small">
          <template #icon><ArrowLeftOutlined /></template>
          返回质量驾驶舱
        </a-button>
      </RouterLink>
    </div>

    <!-- 进度区域（审查中时显示） -->
    <a-card v-if="detail?.status === 'RUNNING' || detail?.status === 'PENDING'" size="small">
      <a-space :size="12" style="margin-bottom:16px">
        <div style="width:40px;height:40px;border-radius:12px;background:#eef2ff;display:flex;align-items:center;justify-content:center">
          <SyncOutlined spin style="font-size:20px;color:#6366f1" />
        </div>
        <div>
          <div style="font-weight:600;font-size:16px">审查进行中</div>
          <div style="font-size:13px;color:#94a3b8">正在执行 Agent 审查，请稍候...</div>
        </div>
      </a-space>

      <!-- 进度条 -->
      <div style="margin-bottom:20px">
        <a-space :size="8" style="width:100%;justify-content:space-between;margin-bottom:8px">
          <span style="font-size:13px;font-weight:500">整体进度</span>
          <span style="font-weight:600;font-size:13px;color:#6366f1">{{ progressPercent }}%</span>
        </a-space>
        <a-progress
          :percent="progressPercent"
          :show-info="false"
          :stroke-color="{ from: '#6366f1', to: '#a855f7' }"
          style="margin:0"
        />
      </div>

      <!-- Agent 执行状态 -->
      <a-space direction="vertical" :size="8" style="width:100%">
        <a-card
          v-for="agent in agentProgress"
          :key="agent.role"
          size="small"
          :body-style="{ padding: '12px 16px' }"
          style="background:#f8fafc"
        >
          <a-space :size="12" style="width:100%">
            <!-- 状态图标 -->
            <div v-if="agent.status === 'RUNNING'"
              style="width:32px;height:32px;border-radius:10px;background:#dbeafe;display:flex;align-items:center;justify-content:center">
              <SyncOutlined spin style="font-size:16px;color:#3b82f6" />
            </div>
            <div v-else-if="agent.status === 'COMPLETED'"
              style="width:32px;height:32px;border-radius:10px;background:#d1fae5;display:flex;align-items:center;justify-content:center">
              <CheckOutlined style="font-size:16px;color:#059669" />
            </div>
            <div v-else-if="agent.status === 'FAILED'"
              style="width:32px;height:32px;border-radius:10px;background:#fee2e2;display:flex;align-items:center;justify-content:center">
              <CloseOutlined style="font-size:16px;color:#dc2626" />
            </div>
            <div v-else
              style="width:32px;height:32px;border-radius:10px;background:#f1f5f9;display:flex;align-items:center;justify-content:center">
              <ClockCircleOutlined style="font-size:16px;color:#94a3b8" />
            </div>

            <div style="flex:1;min-width:0">
              <a-space :size="4" style="width:100%">
                <span style="font-weight:600;font-size:13px">{{ roleLabel(agent.role) }}</span>
                <a-tag v-if="agent.modelName">{{ agent.modelName }}</a-tag>
              </a-space>
              <div v-if="agent.message" style="margin-top:4px;font-size:12px;color:#94a3b8;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
                {{ agent.message }}
              </div>
              <div v-else-if="agent.findingCount !== undefined" style="margin-top:4px;font-size:12px;color:#059669">
                发现 {{ agent.findingCount }} 个问题
              </div>
            </div>

            <a-tag v-if="agent.status" :color="statusColor(agent.status)">
              {{ statusLabel(agent.status) }}
            </a-tag>
          </a-space>
        </a-card>
      </a-space>

      <!-- 实时输出区域 -->
      <div v-if="agentOutputs.length" style="margin-top:20px">
        <div style="font-weight:600;font-size:14px;margin-bottom:12px">实时输出</div>
        <div style="background:#1e293b;border-radius:12px;padding:16px;max-height:256px;overflow:auto">
          <div v-for="(output, idx) in agentOutputs" :key="idx" style="font-size:12px;font-family:monospace;margin-bottom:6px">
            <span style="color:#94a3b8">[{{ output.time }}]</span>
            <span :style="{ color: output.type === 'error' ? '#f87171' : '#34d399' }">[{{ output.role }}]</span>
            <span style="color:#cbd5e1;margin-left:8px">{{ output.message }}</span>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 加载中 -->
    <div v-if="loading" style="text-align:center;padding:48px 0">
      <a-spin size="large" />
    </div>

    <!-- 审查详情 -->
    <template v-else-if="detail">
      <!-- 审查头部 + 严重度统计 -->
      <a-card size="small">
        <a-space :size="8" style="width:100%;justify-content:space-between;flex-wrap:wrap">
          <div style="flex:1;min-width:0">
            <a-space :size="12" style="margin-bottom:8px">
              <div style="width:44px;height:44px;border-radius:14px;background:linear-gradient(135deg,#6366f1,#a855f7);display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(99,102,241,0.25)">
                <FileSearchOutlined style="font-size:20px;color:#fff" />
              </div>
              <h2 style="margin:0;font-size:20px;font-weight:600">审查详情</h2>
            </a-space>
            <div style="margin-left:56px;font-size:13px;color:#94a3b8">
              {{ detail.projectName || '未知项目' }} · {{ detail.sourceBranch || '-' }} -> {{ detail.targetBranch || '-' }}
            </div>
            <div v-if="parsedSummary" style="margin-left:56px;margin-top:8px;font-size:13px;color:#475569;line-height:1.5;max-width:700px">
              {{ parsedSummary }}
            </div>
          </div>
          <a-space :size="4" wrap>
            <a-button size="small" :loading="downloadingSarif" @click="downloadSarif">
              <template #icon><DownloadOutlined /></template>
              SARIF
            </a-button>
            <a-tag :color="statusColor(detail.status)">{{ statusLabel(detail.status) }}</a-tag>
            <a-tag :color="gateStatusColor">{{ gateStatusLabel }}</a-tag>
            <a-tag>{{ detail.reviewMode }}</a-tag>
          </a-space>
        </a-space>

        <!-- 严重度统计网格 -->
        <a-row :gutter="12" style="margin-top:20px">
          <a-col v-for="item in severityStats" :key="item.label" :span="24" :md="12" :lg="4" :xl="4" style="margin-bottom:8px">
            <div :style="{ background: item.bgColor, borderRadius: '12px', padding: '16px' }">
              <div :style="{ fontSize: '12px', fontWeight: 600, color: item.textColor }">{{ item.label }}</div>
              <div :style="{ fontSize: '24px', fontWeight: 800, color: item.valueColor, marginTop: '4px' }">{{ item.value }}</div>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- 阻断原因 -->
      <a-card v-if="blockedReasons.length" size="small">
        <template #title>
          <a-space :size="8">
            <SafetyOutlined style="font-size:18px;color:#dc2626" />
            <span style="color:#dc2626;font-weight:600">Pre-PR 阻断原因</span>
          </a-space>
        </template>
        <template #extra>
          <a-space :size="8">
            <a-button
              size="small"
              type="primary"
              :loading="prePrDecisionLoading"
              @click="submitPrePrDecision({ decision: 'APPROVE_WITH_RISK', comment: '人工确认风险可接受，允许进入后续流程' })"
            >
              人工放行
            </a-button>
            <a-button
              size="small"
              danger
              :loading="prePrDecisionLoading"
              @click="submitPrePrDecision({ decision: 'BLOCKED', comment: '人工确认继续阻断' })"
            >
              维持阻断
            </a-button>
          </a-space>
        </template>
        <a-space direction="vertical" :size="4" style="width:100%">
          <div v-for="reason in blockedReasons" :key="reason" style="display:flex;align-items:flex-start;gap:8px;font-size:13px;color:#dc2626">
            <span style="margin-top:6px;width:8px;height:8px;border-radius:50%;background:#ef4444;flex-shrink:0"></span>
            <span>{{ reason }}</span>
          </div>
        </a-space>
      </a-card>

      <!-- 发现问题列表 + 侧边栏 -->
      <a-row :gutter="16">
        <!-- 发现问题列表 -->
        <a-col :xl="16" :span="24" style="margin-bottom:16px">
          <a-card size="small">
            <template #title>
              <a-space :size="8">
                <ExclamationCircleOutlined style="font-size:20px;color:#d97706" />
                <span style="font-weight:600">发现问题（{{ filteredFindings.length }} / {{ detail.totalFindings }}）</span>
              </a-space>
            </template>
            <template #extra>
              <a-space :size="8">
                <a-select v-model:value="severityFilter" style="width:140px" size="small" placeholder="严重度">
                  <a-select-option v-for="opt in severityOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select v-model:value="categoryFilter" style="width:160px" size="small" placeholder="分类">
                  <a-select-option v-for="opt in categoryOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
              </a-space>
            </template>

            <a-space direction="vertical" :size="8" style="width:100%">
              <FindingCard
                v-for="finding in filteredFindings"
                :key="finding.id"
                :finding="finding"
                @confirm="updateFindingStatus(finding.id, 'CONFIRMED')"
                @dismiss="updateFindingStatus(finding.id, 'DISMISSED')"
              />
            </a-space>

            <div v-if="!filteredFindings.length" style="text-align:center;padding:32px 0;color:#94a3b8">
              <CheckCircleOutlined style="font-size:40px;color:#86efac;display:block;margin:0 auto 8px" />
              没有匹配的问题
            </div>
          </a-card>
        </a-col>

        <!-- 右侧边栏 -->
        <a-col :xl="8" :span="24">
          <!-- 模型执行结果 -->
          <a-card v-if="detail.modelResults.length" size="small" style="margin-bottom:16px">
            <template #title>
              <a-space :size="8">
                <ApiOutlined style="font-size:16px;color:#6366f1" />
                <span style="font-weight:600;font-size:14px">模型执行</span>
              </a-space>
            </template>
            <a-space direction="vertical" :size="8" style="width:100%">
              <a-card
                v-for="result in detail.modelResults"
                :key="result.id"
                size="small"
                :body-style="{ padding: '12px' }"
                style="background:#f8fafc"
              >
                <a-space :size="8" style="width:100%;justify-content:space-between">
                  <div>
                    <div style="font-weight:600;font-size:13px">{{ result.modelName }}</div>
                    <div style="font-size:12px;color:#94a3b8">{{ result.role }}</div>
                  </div>
                  <a-tag :color="statusColor(result.status)">{{ statusLabel(result.status) }}</a-tag>
                </a-space>
                <div v-if="result.errorMessage" style="margin-top:8px;font-size:12px;color:#dc2626">{{ result.errorMessage }}</div>
              </a-card>
            </a-space>
          </a-card>

          <!-- 人工处理统计 -->
          <a-card size="small">
            <template #title>
              <a-space :size="8">
                <TeamOutlined style="font-size:16px;color:#059669" />
                <span style="font-weight:600;font-size:14px">人工处理</span>
              </a-space>
            </template>
            <a-space direction="vertical" :size="8" style="width:100%">
              <div style="display:flex;align-items:center;justify-content:space-between;border-radius:10px;background:rgba(245,158,11,0.05);padding:10px 14px">
                <span style="font-size:13px;color:#94a3b8;font-weight:500">待确认</span>
                <span style="font-size:20px;font-weight:800;color:#d97706">{{ pendingFindings }}</span>
              </div>
              <div style="display:flex;align-items:center;justify-content:space-between;border-radius:10px;background:rgba(5,150,105,0.05);padding:10px 14px">
                <span style="font-size:13px;color:#94a3b8;font-weight:500">已确认</span>
                <span style="font-size:20px;font-weight:800;color:#059669">{{ confirmedFindings }}</span>
              </div>
              <div style="display:flex;align-items:center;justify-content:space-between;border-radius:10px;background:rgba(148,163,184,0.05);padding:10px 14px">
                <span style="font-size:13px;color:#94a3b8;font-weight:500">已忽略</span>
                <span style="font-size:20px;font-weight:800;color:#64748b">{{ dismissedFindings }}</span>
              </div>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <!-- 分析面板（审查完成后可用） -->
      <template v-if="detail.status === 'COMPLETED'">
        <a-card size="small" style="margin-top:8px">
          <template #title>
            <a-space :size="8">
              <FundViewOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">智能分析</span>
            </a-space>
          </template>

          <!-- Tab 切换 -->
          <a-space :size="4" style="margin-bottom:16px">
            <a-button
              v-for="tab in analysisTabs"
              :key="tab.key"
              :type="activeTab === tab.key ? 'primary' : 'default'"
              size="small"
              @click="activeTab = tab.key; loadAnalysis(tab.key)"
            >
              {{ tab.label }}
            </a-button>
          </a-space>

          <!-- 加载中 -->
          <div v-if="analysisLoading" style="text-align:center;padding:32px 0">
            <a-spin />
          </div>

          <!-- 风险预测 -->
          <template v-else-if="activeTab === 'risk' && riskData">
            <a-space direction="vertical" :size="12" style="width:100%">
              <div :style="{ padding: '16px', borderRadius: '12px', background: riskBgColor }">
                <span style="font-size:20px;font-weight:800">{{ riskData.level }}</span>
                <span style="font-size:13px;margin-left:8px">评分: {{ riskData.score }}</span>
              </div>
              <div v-if="riskData.factors.length">
                <div style="font-weight:600;font-size:13px;margin-bottom:8px">风险因素</div>
                <a-space direction="vertical" :size="4">
                  <div v-for="f in riskData.factors" :key="f" style="display:flex;align-items:center;gap:8px;font-size:13px;color:#475569">
                    <span style="width:6px;height:6px;border-radius:50%;background:#f59e0b;flex-shrink:0"></span>
                    {{ f }}
                  </div>
                </a-space>
              </div>
              <div style="padding:12px;border-radius:10px;background:#f8fafc;font-size:13px;color:#475569">
                {{ riskData.recommendation }}
              </div>
            </a-space>
          </template>

          <!-- 测试用例 -->
          <template v-else-if="activeTab === 'tests' && testPlan">
            <a-space direction="vertical" :size="12" style="width:100%">
              <div style="font-size:13px;color:#475569;line-height:1.5">{{ testPlan.coverageSummary }}</div>
              <a-row :gutter="12">
                <a-col :span="8">
                  <div style="text-align:center;padding:12px;border-radius:10px;background:rgba(220,38,38,0.05)">
                    <div style="font-size:20px;font-weight:800;color:#dc2626">{{ testPlan.highRiskCount }}</div>
                    <div style="font-size:12px;color:#ef4444;margin-top:4px">高风险</div>
                  </div>
                </a-col>
                <a-col :span="8">
                  <div style="text-align:center;padding:12px;border-radius:10px;background:rgba(217,119,6,0.05)">
                    <div style="font-size:20px;font-weight:800;color:#d97706">{{ testPlan.mediumRiskCount }}</div>
                    <div style="font-size:12px;color:#f59e0b;margin-top:4px">中风险</div>
                  </div>
                </a-col>
                <a-col :span="8">
                  <div style="text-align:center;padding:12px;border-radius:10px;background:rgba(5,150,105,0.05)">
                    <div style="font-size:20px;font-weight:800;color:#059669">{{ testPlan.lowRiskCount }}</div>
                    <div style="font-size:12px;color:#34d399;margin-top:4px">低风险</div>
                  </div>
                </a-col>
              </a-row>
            </a-space>
          </template>

          <!-- 重构计划 -->
          <template v-else-if="activeTab === 'refactor' && refactorPlan">
            <a-space direction="vertical" :size="12" style="width:100%">
              <div style="font-weight:600;font-size:14px">{{ refactorPlan.title }}</div>
              <div style="font-size:13px;color:#94a3b8">{{ refactorPlan.summary }}</div>
              <a-space direction="vertical" :size="4" style="width:100%">
                <a-card
                  v-for="step in refactorPlan.steps.slice(0, 5)"
                  :key="step.stepName"
                  size="small"
                  :body-style="{ padding: '10px 14px' }"
                  style="background:#f8fafc"
                >
                  <a-space :size="8" style="width:100%">
                    <a-tag :color="step.priority === 'P0' ? 'red' : step.priority === 'P1' ? 'orange' : 'default'">
                      {{ step.priority }}
                    </a-tag>
                    <span style="flex:1;font-size:13px;font-weight:500">{{ step.stepName }}</span>
                    <span style="font-size:12px;color:#94a3b8">{{ step.estimatedEffort }}</span>
                  </a-space>
                </a-card>
              </a-space>
            </a-space>
          </template>
        </a-card>
      </template>
    </template>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeftOutlined, SyncOutlined, CheckOutlined, CloseOutlined, ClockCircleOutlined, FileSearchOutlined, SafetyOutlined, ExclamationCircleOutlined, CheckCircleOutlined, ApiOutlined, TeamOutlined, FundViewOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import type { ReviewDetail, HumanStatus } from '@/types/review'
import { deriveGateStatus, generateBlockedReasons } from '@/utils/reviewMetrics'
import { useApi } from '@/composables/useApi'
import { getApiBaseUrl } from '@/utils/apiConfig'
import type { RiskAssessment } from '@/types/risk'
import type { TestCoveragePlan } from '@/types/testgen'
import type { RefactorPlan } from '@/types/refactor'

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
const { get, patch, post } = useApi()
const reviewId = computed(() => route.params.id as string)
const loading = ref(false)
const downloadingSarif = ref(false)
const prePrDecisionLoading = ref(false)
const detail = ref<ReviewDetail | null>(null)
const severityFilter = ref('all')
const categoryFilter = ref('all')

const agentProgress = ref<AgentProgressItem[]>([])
const agentOutputs = ref<AgentOutput[]>([])
let eventSource: EventSource | null = null

const severityOptions = [
  { label: '全部严重度', value: 'all' },
  { label: '阻断', value: 'BLOCKER' },
  { label: '重要', value: 'MAJOR' },
  { label: '一般', value: 'MINOR' },
  { label: '提示', value: 'INFO' },
]
const categoryOptions = [
  { label: '全部分类', value: 'all' },
  { label: '代码风格', value: 'CODE_STYLE' },
  { label: '潜在缺陷', value: 'BUG' },
  { label: '性能问题', value: 'PERFORMANCE' },
  { label: '安全问题', value: 'SECURITY' },
  { label: '异常处理', value: 'EXCEPTION_HANDLING' },
]

const gateStatus = computed(() => detail.value ? deriveGateStatus(detail.value) : 'RUNNING')
const blockedReasons = computed(() => detail.value ? generateBlockedReasons(detail.value) : [])
const gateStatusLabel = computed(() => ({
  PASSED: 'Pre-PR 通过', BLOCKED: 'Pre-PR 阻断', NEEDS_HUMAN_REVIEW: '待人工复核', RUNNING: '审查中'
}[gateStatus.value] ?? gateStatus.value))
const gateStatusColor = computed(() => ({
  PASSED: 'green', BLOCKED: 'red', NEEDS_HUMAN_REVIEW: 'orange', RUNNING: 'blue'
}[gateStatus.value] ?? 'default'))

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

// 严重度统计：内联背景色替代 Tailwind
const severityStats = computed(() => [
  { label: '阻断', value: detail.value?.blockerCount ?? 0,
    bgColor: 'linear-gradient(135deg,rgba(239,68,68,0.08),rgba(251,113,133,0.06))',
    textColor: '#dc2626', valueColor: '#dc2626' },
  { label: '重要', value: detail.value?.majorCount ?? 0,
    bgColor: 'linear-gradient(135deg,rgba(249,115,22,0.08),rgba(251,146,60,0.06))',
    textColor: '#ea580c', valueColor: '#ea580c' },
  { label: '一般', value: detail.value?.minorCount ?? 0,
    bgColor: 'linear-gradient(135deg,rgba(234,179,8,0.08),rgba(250,204,21,0.06))',
    textColor: '#ca8a04', valueColor: '#ca8a04' },
  { label: '提示', value: detail.value?.infoCount ?? 0,
    bgColor: 'linear-gradient(135deg,rgba(59,130,246,0.08),rgba(99,102,241,0.06))',
    textColor: '#2563eb', valueColor: '#2563eb' },
  { label: '交叉命中', value: (detail.value?.findings ?? []).filter(item => item.isCrossHit).length,
    bgColor: 'linear-gradient(135deg,rgba(168,85,247,0.08),rgba(139,92,246,0.06))',
    textColor: '#7c3aed', valueColor: '#7c3aed' },
])

const parsedSummary = computed(() => {
  const summary = detail.value?.summary
  if (!summary) return ''
  try { const parsed = JSON.parse(summary); return typeof parsed === 'string' ? parsed : parsed.summary || parsed.conclusion || parsed.description || '' }
  catch { return summary }
})

function statusColor(status: string): string {
  return { PENDING: 'orange', RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red' }[status] ?? 'default'
}
function statusLabel(status: string): string {
  return { PENDING: '等待中', RUNNING: '执行中', COMPLETED: '已完成', FAILED: '失败' }[status] ?? status
}
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
  const baseUrl = getApiBaseUrl()
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

async function submitPrePrDecision(payload: { decision: string; comment: string }) {
  prePrDecisionLoading.value = true
  try {
    const res = await patch<ReviewDetail>(`/reviews/${reviewId.value}/pre-pr-decision`, payload)
    if (res.data) detail.value = res.data
  } catch (e) {
    console.error('提交 Pre-PR 人工决策失败', e)
  } finally {
    prePrDecisionLoading.value = false
  }
}

async function downloadSarif() {
  downloadingSarif.value = true
  try {
    const res = await get<unknown>(`/reviews/${reviewId.value}/sarif`)
    if (!res.data) return

    const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/sarif+json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `review-${reviewId.value}.sarif`
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('导出 SARIF 失败', e)
  } finally {
    downloadingSarif.value = false
  }
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

const activeTab = ref('risk')
const analysisLoading = ref(false)
const riskData = ref<RiskAssessment | null>(null)
const testPlan = ref<TestCoveragePlan | null>(null)
const refactorPlan = ref<RefactorPlan | null>(null)

const analysisTabs = [
  { key: 'risk', label: '风险预测' },
  { key: 'tests', label: '测试用例' },
  { key: 'refactor', label: '重构计划' },
]

const riskBgColor = computed(() => {
  const level = riskData.value?.level
  const colors: Record<string, string> = {
    CRITICAL: 'rgba(220,38,38,0.1)',
    HIGH: 'rgba(249,115,22,0.1)',
    MEDIUM: 'rgba(217,119,6,0.1)',
    LOW: 'rgba(5,150,105,0.1)',
  }
  return colors[level ?? ''] || '#f8fafc'
})

async function loadAnalysis(tab: string) {
  if (tab === 'risk' && !riskData.value) {
    analysisLoading.value = true
    try {
      const res = await get<RiskAssessment>(`/reviews/${reviewId.value}/risk`)
      if (res.data) riskData.value = res.data
    } catch (e) { console.error(e) }
    finally { analysisLoading.value = false }
  }
  if (tab === 'tests' && !testPlan.value) {
    analysisLoading.value = true
    try {
      const res = await post<TestCoveragePlan>(`/reviews/${reviewId.value}/generate-tests`)
      if (res.data) testPlan.value = res.data
    } catch (e) { console.error(e) }
    finally { analysisLoading.value = false }
  }
  if (tab === 'refactor' && !refactorPlan.value) {
    analysisLoading.value = true
    try {
      const res = await post<RefactorPlan>(`/reviews/${reviewId.value}/refactor-plan`)
      if (res.data) refactorPlan.value = res.data
    } catch (e) { console.error(e) }
    finally { analysisLoading.value = false }
  }
}
</script>
