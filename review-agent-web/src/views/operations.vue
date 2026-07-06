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
              <span style="font-weight:600">Unified Operations Tasks</span>
            </a-space>
          </template>
          <template #extra>
            <a-space :size="8">
              <a-tag color="processing">{{ operationsTasks.length }} tasks</a-tag>
              <a-tag v-if="selectedTaskKeys.length" color="blue">{{ selectedTaskKeys.length }} selected</a-tag>
              <a-button size="small" :disabled="!selectedTaskKeys.length" :loading="batchAssigningTasks" @click="batchAssignOperationsTasks">Batch assign</a-button>
              <a-button size="small" :loading="operationsTasksSyncing" @click="syncOperationsTasks">Sync tasks</a-button>
            </a-space>
          </template>
          <a-table
            :columns="taskColumns"
            :data-source="operationsTasks"
            :loading="operationsTasksLoading"
            :pagination="false"
            :row-selection="taskRowSelection"
            row-key="taskKey"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'title'">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ record.title }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px">{{ record.sourceType }} · {{ record.sourceRef }}</div>
                  <div v-if="record.recommendation" style="font-size:12px;color:#64748b;margin-top:2px;line-height:1.4">{{ record.recommendation }}</div>
                  <div v-if="record.externalIssue" style="font-size:12px;margin-top:4px">
                    <a
                      v-if="record.externalIssue.externalIssueUrl"
                      :href="record.externalIssue.externalIssueUrl"
                      target="_blank"
                      rel="noreferrer"
                      style="color:#2563eb"
                    >
                      GitLab issue #{{ record.externalIssue.externalIssueIid || record.externalIssue.externalIssueId }}
                    </a>
                    <a-tag
                      v-if="record.externalIssue.externalIssueState"
                      :color="externalIssueStateColor(record.externalIssue.externalIssueState)"
                      style="margin-left:6px;margin-inline-end:0"
                    >
                      {{ record.externalIssue.externalIssueState }}
                    </a-tag>
                    <a-tag v-else :color="externalIssueStatusColor(record.externalIssue.issueStatus)">
                      {{ record.externalIssue.provider }} {{ record.externalIssue.issueStatus }}
                    </a-tag>
                    <span v-if="record.externalIssue.syncedAt" style="color:#94a3b8;margin-left:6px">
                      auto sync {{ formatExternalIssueSyncedAt(record.externalIssue.syncedAt) }}
                    </span>
                    <div
                      v-if="record.externalIssue.externalIssueAssignee || record.externalIssue.externalIssueLabels || record.externalIssue.externalUpdatedAt"
                      style="color:#64748b;margin-top:2px"
                    >
                      <span v-if="record.externalIssue.externalIssueAssignee">
                        assignee {{ record.externalIssue.externalIssueAssignee }}
                      </span>
                      <span v-if="record.externalIssue.externalIssueLabels" style="margin-left:6px">
                        labels {{ record.externalIssue.externalIssueLabels }}
                      </span>
                      <span v-if="record.externalIssue.externalUpdatedAt" style="margin-left:6px">
                        updated {{ formatExternalIssueSyncedAt(record.externalIssue.externalUpdatedAt) }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
              <template v-else-if="column.key === 'severity'">
                <a-tag :color="taskSeverityColor(record.severity)">{{ record.severity }}</a-tag>
              </template>
              <template v-else-if="column.key === 'ownerRole'">
                <a-tag>{{ record.ownerRole }}</a-tag>
              </template>
              <template v-else-if="column.key === 'slaHours'">
                <a-space direction="vertical" :size="2">
                  <span style="font-weight:600;font-size:13px">{{ record.slaHours }}h</span>
                  <a-tag v-if="record.slaState" :color="slaStateColor(record.slaState)" style="margin-inline-end:0">
                    {{ record.slaState }}{{ formatRemainingHours(record.remainingHours) }}
                  </a-tag>
                </a-space>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="taskStatusColor(record.status)">{{ record.status }}</a-tag>
              </template>
              <template v-else-if="column.key === 'latestSignal'">
                <span style="font-size:12px;color:#64748b">{{ record.latestSignal || '-' }}</span>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space v-if="record.status !== 'RESOLVED'" :size="4" wrap>
                  <a-button
                    v-if="record.status === 'OPEN' || record.status === 'CONFIRMED'"
                    type="link"
                    size="small"
                    :loading="updatingTaskKey === record.taskKey"
                    @click="updateOperationsTaskStatus(record, 'IN_PROGRESS')"
                  >
                    Start
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    :loading="updatingTaskKey === record.taskKey"
                    @click="editOperationsTaskOwner(record)"
                  >
                    Owner/SLA
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    :loading="updatingTaskKey === record.taskKey"
                    @click="updateOperationsTaskStatus(record, 'ACCEPTED_RISK')"
                  >
                    Accept risk
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    :loading="syncingIssueTaskKey === record.taskKey"
                    @click="syncOperationsTaskGitLabIssue(record)"
                  >
                    GitLab Issue
                  </a-button>
                  <a-button
                    v-if="record.externalIssue?.externalIssueIid"
                    type="link"
                    size="small"
                    :loading="syncingIssueTaskKey === record.taskKey"
                    @click="refreshOperationsTaskGitLabIssue(record)"
                  >
                    Refresh Issue
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    :loading="closingTaskKey === record.taskKey"
                    @click="closeOperationsTask(record)"
                  >
                    Close
                  </a-button>
                </a-space>
                <span v-else style="font-size:12px;color:#94a3b8">{{ record.closeReason || 'Resolved' }}</span>
              </template>
            </template>
          </a-table>
        </a-card>

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
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <ClockCircleOutlined style="font-size:16px;color:#dc2626" />
              <span style="font-weight:600;font-size:14px">SLA Alerts</span>
            </a-space>
          </template>
          <a-space v-if="slaAlertTasks.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="task in slaAlertTasks"
              :key="task.taskKey"
              size="small"
              :body-style="{ padding: '12px' }"
              style="background:#fff7ed;border-color:#fed7aa"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag :color="slaStateColor(task.slaState || 'ON_TRACK')">{{ task.slaState }}</a-tag>
                  <a-tag>{{ task.ownerRole }}</a-tag>
                  <a-tag :color="taskSeverityColor(task.severity)">{{ task.severity }}</a-tag>
                  <a-tag color="orange">{{ task.slaHours }}h SLA</a-tag>
                </a-space>
                <div style="font-weight:600;font-size:13px;color:#334155">{{ task.title }}</div>
                <div style="font-size:12px;color:#64748b">
                  {{ task.sourceType }} · {{ task.sourceRef }} · {{ formatRemainingHours(task.remainingHours) || 'due time tracked' }}
                </div>
                <a-space :size="4" wrap>
                  <a-button type="link" size="small" @click="updateOperationsTaskStatus(task, 'IN_PROGRESS')">Start</a-button>
                  <a-button type="link" size="small" @click="editOperationsTaskOwner(task)">Owner/SLA</a-button>
                  <a-button type="link" size="small" @click="closeOperationsTask(task)">Close</a-button>
                </a-space>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="No SLA alerts" :image="undefined" />
        </a-card>

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

        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <SafetyCertificateOutlined style="font-size:16px;color:#d97706" />
              <span style="font-weight:600;font-size:14px">CI Health Actions</span>
            </a-space>
          </template>
          <a-space v-if="ciHealthActions.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="action in ciHealthActions"
              :key="action.key"
              size="small"
              :body-style="{ padding: '12px' }"
              style="background:#f8fafc"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag color="processing">{{ action.provider }}</a-tag>
                  <a-tag :color="ciActionSeverityColor(action.severity)">{{ action.healthStatus }}</a-tag>
                  <a-tag>{{ action.ownerRole }}</a-tag>
                  <a-tag color="orange">{{ action.slaHours }}h SLA</a-tag>
                </a-space>
                <div style="font-size:12px;color:#64748b">Latest: {{ action.latestSignal }}</div>
                <div style="font-size:12px;color:#475569;line-height:1.5">{{ action.recommendation }}</div>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="No CI health actions" :image="undefined" />
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
              <a-space :size="4" wrap style="margin-top:10px">
                <a-button
                  type="primary"
                  size="small"
                  :loading="actingRuleCandidateId === candidate.findingId && actingRuleCandidateAction === 'ACCEPT'"
                  @click="handleRuleLearningAction(candidate, 'ACCEPT')"
                >
                  采纳
                </a-button>
                <a-button
                  size="small"
                  :loading="actingRuleCandidateId === candidate.findingId && actingRuleCandidateAction === 'REJECT'"
                  @click="handleRuleLearningAction(candidate, 'REJECT')"
                >
                  拒绝
                </a-button>
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
import type { BusinessImpactEstimate, OperationDashboard, OperationOwnerLoad, OperationalFinding, OperationsCiHealthAction, OperationsExternalIssue, OperationsTask, RemediationQueueItem, RuleLearningCandidate, StrategyPressureItem, StrategyPressureLevel, TelemetryReadinessItem, TelemetryReadinessLevel } from '@/types/operations'
import type { SeverityLevel } from '@/types/review'
import { useApi } from '@/composables/useApi'
import { buildRemediationQueue, deriveOperationsScorecard, estimateReviewBusinessImpact, extractRuleLearningCandidates, summarizeRemediationQueue } from '@/utils/reviewOperations'

const fallbackBusinessImpact = estimateReviewBusinessImpact({ monthlyReviews: 80, averageManualReviewMinutes: 35, automationCoveragePercent: 65, blockerFindings: 6, majorFindings: 18 })
const router = useRouter()
const { get, post, patch } = useApi()
const remediationQueueLoading = ref(false)
const operationsTasksLoading = ref(false)
const operationsTasksSyncing = ref(false)
const batchAssigningTasks = ref(false)
const updatingTaskKey = ref<string | null>(null)
const syncingIssueTaskKey = ref<string | null>(null)
const closingTaskKey = ref<string | null>(null)
const selectedTaskKeys = ref<string[]>([])
const actingFindingId = ref<number | null>(null)
const actingFindingAction = ref<'CONFIRM' | 'DISMISS' | null>(null)
const actingRuleCandidateId = ref<number | null>(null)
const actingRuleCandidateAction = ref<'ACCEPT' | 'REJECT' | null>(null)
const operationalFindings = ref<OperationalFinding[]>([])
const operationDashboard = ref<OperationDashboard | null>(null)
const backendOwnerLoad = ref<OperationOwnerLoad[]>([])
const backendRuleLearningCandidates = ref<RuleLearningCandidate[]>([])
const backendBusinessImpact = ref<BusinessImpactEstimate | null>(null)
const strategyPressureLoading = ref(false)
const strategyPressureItems = ref<StrategyPressureItem[]>([])
const telemetryReadinessItems = ref<TelemetryReadinessItem[]>([])
const ciHealthActions = ref<OperationsCiHealthAction[]>([])
const operationsTasks = ref<OperationsTask[]>([])
const slaAlertTasks = ref<OperationsTask[]>([])
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
const taskColumns = [
  { title: '任务', key: 'title', dataIndex: 'title' },
  { title: '级别', key: 'severity', dataIndex: 'severity' },
  { title: '负责人', key: 'ownerRole', dataIndex: 'ownerRole' },
  { title: 'SLA', key: 'slaHours', dataIndex: 'slaHours' },
  { title: '状态', key: 'status', dataIndex: 'status' },
  { title: '最新信号', key: 'latestSignal', dataIndex: 'latestSignal' },
  { title: '操作', key: 'actions', width: 260 },
]
const ownerLoad = computed(() => backendOwnerLoad.value.length
  ? backendOwnerLoad.value
  : Object.entries(queueSummary.value.byOwner).map(([role, count]) => ({ role, count, percent: queueSummary.value.total ? Math.round((count / queueSummary.value.total) * 100) : 0 })))
const taskRowSelection = computed(() => ({
  selectedRowKeys: selectedTaskKeys.value,
  onChange: (keys: Array<string | number>) => {
    selectedTaskKeys.value = keys.map(String)
  },
}))

const operatingCadences = [
  { name: '每日风险清理', iconComp: SafetyCertificateOutlined, description: '每天处理 BLOCKER 和 24 小时内到期项，避免风险穿透到正式 PR。' },
  { name: '每周规则复盘', iconComp: ExperimentOutlined, description: '将确认问题固化为规则，将低置信误报加入降噪样例，持续提高模型可用性。' },
  { name: '发布前门禁', iconComp: RocketOutlined, description: '核心链路发布前检查安全、性能、异常处理和人工确认记录。' },
]

function severityColor(severity: SeverityLevel) { return { BLOCKER: 'red', MAJOR: 'orange', MINOR: 'blue', INFO: 'default' }[severity] }

function taskSeverityColor(severity: string) {
  return { BLOCKER: 'red', CRITICAL: 'red', MAJOR: 'orange', WARNING: 'orange', MINOR: 'blue', INFO: 'default' }[severity] ?? 'default'
}

function taskStatusColor(status: string) {
  return {
    OPEN: 'orange',
    CONFIRMED: 'blue',
    IN_PROGRESS: 'processing',
    ACCEPTED_RISK: 'purple',
    RESOLVED: 'green',
  }[status] ?? 'default'
}

function slaStateColor(state: string) {
  return {
    OVERDUE: 'red',
    DUE_SOON: 'orange',
    ON_TRACK: 'green',
    CLOSED: 'default',
    UNTRACKED: 'default',
    NO_SLA: 'default',
  }[state] ?? 'default'
}

function formatRemainingHours(remainingHours?: number | null) {
  if (remainingHours === null || remainingHours === undefined) return ''
  if (remainingHours < 0) return ` · overdue ${Math.abs(remainingHours)}h`
  return ` · ${remainingHours}h left`
}

function pressureColor(level: StrategyPressureLevel) {
  return { HIGH: 'red', MEDIUM: 'orange', LOW: 'green' }[level]
}

function telemetryReadinessColor(level: TelemetryReadinessLevel) {
  return { READY: 'green', NEEDS_ATTRIBUTION: 'orange', JUDGE_UNSTABLE: 'red', NOT_CONNECTED: 'default' }[level]
}

function ciActionSeverityColor(severity: string) {
  return { CRITICAL: 'red', WARNING: 'orange', INFO: 'default' }[severity] ?? 'default'
}

function externalIssueStatusColor(status: string) {
  return { SYNCED: 'green', SKIPPED: 'default', FAILED: 'red' }[status] ?? 'default'
}

function externalIssueStateColor(state: string) {
  return { opened: 'processing', closed: 'green', reopened: 'blue' }[state] ?? 'default'
}

function formatExternalIssueSyncedAt(value: string) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
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

async function loadCiHealthActions() {
  try {
    const res = await get<OperationsCiHealthAction[]>('/operations/ci-health-actions')
    if (res.data) ciHealthActions.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadOperationsTasks() {
  operationsTasksLoading.value = true
  try {
    const res = await get<OperationsTask[]>('/operations/tasks?limit=20')
    if (res.data) operationsTasks.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    operationsTasksLoading.value = false
  }
}

async function loadOperationsTaskSlaAlerts() {
  try {
    const res = await get<OperationsTask[]>('/operations/tasks/sla-alerts?limit=8')
    if (res.data) slaAlertTasks.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function syncOperationsTasks() {
  operationsTasksSyncing.value = true
  try {
    const res = await post<OperationsTask[]>('/operations/tasks/sync?limit=20')
    if (res.data) operationsTasks.value = res.data
    await loadOperationsTaskSlaAlerts()
  } catch (e) {
    console.error(e)
  } finally {
    operationsTasksSyncing.value = false
  }
}

async function updateOperationsTaskStatus(task: OperationsTask, status: string) {
  updatingTaskKey.value = task.taskKey
  try {
    await patch(`/operations/tasks/${encodeURIComponent(task.taskKey)}`, { status })
    message.success(`Task moved to ${status}`)
    await Promise.all([loadOperationsTasks(), loadOperationsTaskSlaAlerts()])
  } catch (e) {
    console.error(e)
    message.error('Failed to update task status')
  } finally {
    updatingTaskKey.value = null
  }
}

async function batchAssignOperationsTasks() {
  if (!selectedTaskKeys.value.length) return
  const ownerRole = window.prompt('Owner role', 'Code Owner')
  if (ownerRole === null) return
  const slaInput = window.prompt('SLA hours', '24')
  if (slaInput === null) return
  const slaHours = Number.parseInt(slaInput, 10)
  if (!Number.isFinite(slaHours) || slaHours <= 0) {
    message.warning('SLA hours must be a positive number')
    return
  }
  batchAssigningTasks.value = true
  try {
    await patch('/operations/tasks/batch', {
      taskKeys: selectedTaskKeys.value,
      status: 'IN_PROGRESS',
      ownerRole,
      slaHours,
    })
    message.success(`Batch assigned ${selectedTaskKeys.value.length} tasks`)
    selectedTaskKeys.value = []
    await Promise.all([loadOperationsTasks(), loadOperationsTaskSlaAlerts(), loadOwnerLoad()])
  } catch (e) {
    console.error(e)
    message.error('Failed to batch assign tasks')
  } finally {
    batchAssigningTasks.value = false
  }
}

async function editOperationsTaskOwner(task: OperationsTask) {
  const ownerRole = window.prompt('Owner role', task.ownerRole || 'Code Owner')
  if (ownerRole === null) return
  const slaInput = window.prompt('SLA hours', String(task.slaHours || 24))
  if (slaInput === null) return
  const slaHours = Number.parseInt(slaInput, 10)
  if (!Number.isFinite(slaHours) || slaHours <= 0) {
    message.warning('SLA hours must be a positive number')
    return
  }
  updatingTaskKey.value = task.taskKey
  try {
    await patch(`/operations/tasks/${encodeURIComponent(task.taskKey)}`, { ownerRole, slaHours })
    message.success('Task owner and SLA updated')
    await Promise.all([loadOperationsTasks(), loadOperationsTaskSlaAlerts()])
  } catch (e) {
    console.error(e)
    message.error('Failed to update task owner')
  } finally {
    updatingTaskKey.value = null
  }
}

async function syncOperationsTaskGitLabIssue(task: OperationsTask) {
  syncingIssueTaskKey.value = task.taskKey
  try {
    const res = await post<OperationsExternalIssue>(`/operations/tasks/${encodeURIComponent(task.taskKey)}/gitlab-issue`)
    if (res.data?.issueStatus === 'SYNCED') {
      message.success('GitLab issue created')
    } else if (res.data?.issueStatus === 'SKIPPED') {
      message.warning(res.data.errorMessage || 'GitLab issue sync skipped')
    } else {
      message.error(res.data?.errorMessage || 'GitLab issue sync failed')
    }
    await loadOperationsTasks()
  } catch (e) {
    console.error(e)
    message.error('Failed to sync GitLab issue')
  } finally {
    syncingIssueTaskKey.value = null
  }
}

async function refreshOperationsTaskGitLabIssue(task: OperationsTask) {
  syncingIssueTaskKey.value = task.taskKey
  try {
    const res = await post<OperationsExternalIssue>(`/operations/tasks/${encodeURIComponent(task.taskKey)}/gitlab-issue/refresh`)
    if (res.data?.issueStatus === 'SYNCED') {
      const state = res.data.externalIssueState ? `: ${res.data.externalIssueState}` : ''
      message.success(`GitLab issue refreshed${state}`)
    } else if (res.data?.issueStatus === 'SKIPPED') {
      message.warning(res.data.errorMessage || 'GitLab issue refresh skipped')
    } else {
      message.error(res.data?.errorMessage || 'GitLab issue refresh failed')
    }
    await Promise.all([loadOperationsTasks(), loadOperationsTaskSlaAlerts()])
  } catch (e) {
    console.error(e)
    message.error('Failed to refresh GitLab issue')
  } finally {
    syncingIssueTaskKey.value = null
  }
}

async function closeOperationsTask(task: OperationsTask) {
  const closeReason = window.prompt('Close reason', 'Resolved from operations workbench')
  if (closeReason === null) return
  closingTaskKey.value = task.taskKey
  try {
    await post(`/operations/tasks/${encodeURIComponent(task.taskKey)}/close`, { closeReason })
    await Promise.all([loadOperationsTasks(), loadOperationsTaskSlaAlerts()])
  } catch (e) {
    console.error(e)
  } finally {
    closingTaskKey.value = null
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
    loadCiHealthActions(),
    loadOperationsTasks(),
    loadOperationsTaskSlaAlerts(),
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

async function handleRuleLearningAction(candidate: RuleLearningCandidate, action: 'ACCEPT' | 'REJECT') {
  actingRuleCandidateId.value = candidate.findingId
  actingRuleCandidateAction.value = action
  const endpoint = action === 'ACCEPT'
    ? `/operations/rule-learning-candidates/${candidate.findingId}/accept`
    : `/operations/rule-learning-candidates/${candidate.findingId}/reject`
  try {
    await post(endpoint)
    message.success(action === 'ACCEPT' ? '已采纳规则学习候选' : '已拒绝规则学习候选')
    await Promise.all([
      loadRuleLearningCandidates(),
      loadBusinessImpact(),
    ])
  } catch (e) {
    console.error('更新规则学习候选失败', e)
    message.error('更新规则学习候选失败')
  } finally {
    actingRuleCandidateId.value = null
    actingRuleCandidateAction.value = null
  }
}

onMounted(() => {
  loadOperationsDashboard()
  loadOwnerLoad()
  loadStrategyPressure()
  loadTelemetryReadiness()
  loadCiHealthActions()
  loadOperationsTasks()
  loadOperationsTaskSlaAlerts()
  loadRemediationQueue()
  loadRuleLearningCandidates()
  loadBusinessImpact()
})
</script>
