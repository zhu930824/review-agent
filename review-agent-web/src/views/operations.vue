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
            :pagination="false"
            row-key="id"
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
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { BarChartOutlined, ExclamationCircleOutlined, ClockCircleOutlined, RiseOutlined, UnorderedListOutlined, TeamOutlined, ExperimentOutlined, CalendarOutlined, AppstoreOutlined, PlayCircleOutlined, SafetyCertificateOutlined, RocketOutlined } from '@ant-design/icons-vue'
import type { OperationalFinding } from '@/types/operations'
import type { SeverityLevel } from '@/types/review'
import { buildRemediationQueue, deriveOperationsScorecard, estimateReviewBusinessImpact, extractRuleLearningCandidates, summarizeRemediationQueue } from '@/utils/reviewOperations'

const sampleFindings: OperationalFinding[] = [
  { id: 1, reviewId: 1001, projectName: 'payment-core', severity: 'BLOCKER', category: 'SECURITY', title: 'Token can be reused after logout', humanStatus: 'CONFIRMED', confidence: 0.92, isCrossHit: true },
  { id: 2, reviewId: 1001, projectName: 'payment-core', severity: 'MAJOR', category: 'PERFORMANCE', title: 'Settlement list has N+1 query risk', humanStatus: 'PENDING', confidence: 0.82 },
  { id: 3, reviewId: 1002, projectName: 'order-service', severity: 'MAJOR', category: 'EXCEPTION_HANDLING', title: 'External timeout is not mapped to business error', humanStatus: 'CONFIRMED', confidence: 0.88, isCrossHit: true },
  { id: 4, reviewId: 1003, projectName: 'web-console', severity: 'MINOR', category: 'CODE_STYLE', title: 'Naming can be clearer', humanStatus: 'DISMISSED', confidence: 0.41 },
]

const remediationQueue = buildRemediationQueue(sampleFindings)
const queueSummary = summarizeRemediationQueue(remediationQueue)
const learningCandidates = extractRuleLearningCandidates(sampleFindings)
const scorecard = deriveOperationsScorecard(sampleFindings)
const businessImpact = estimateReviewBusinessImpact({ monthlyReviews: 80, averageManualReviewMinutes: 35, automationCoveragePercent: 65, blockerFindings: 6, majorFindings: 18 })

const queueColumns = [
  { title: '风险项', key: 'title', dataIndex: 'title' },
  { title: '级别', key: 'severity', dataIndex: 'severity' },
  { title: '负责人', key: 'ownerRole', dataIndex: 'ownerRole' },
  { title: 'SLA', key: 'slaHours', dataIndex: 'slaHours' },
  { title: '优先级', key: 'priorityScore', dataIndex: 'priorityScore' },
]
const ownerLoad = computed(() => Object.entries(queueSummary.byOwner).map(([role, count]) => ({ role, count, percent: queueSummary.total ? Math.round((count / queueSummary.total) * 100) : 0 })))

const operatingCadences = [
  { name: '每日风险清理', iconComp: SafetyCertificateOutlined, description: '每天处理 BLOCKER 和 24 小时内到期项，避免风险穿透到正式 PR。' },
  { name: '每周规则复盘', iconComp: ExperimentOutlined, description: '将确认问题固化为规则，将低置信误报加入降噪样例，持续提高模型可用性。' },
  { name: '发布前门禁', iconComp: RocketOutlined, description: '核心链路发布前检查安全、性能、异常处理和人工确认记录。' },
]

function severityColor(severity: SeverityLevel) { return { BLOCKER: 'red', MAJOR: 'orange', MINOR: 'blue', INFO: 'default' }[severity] }
</script>
