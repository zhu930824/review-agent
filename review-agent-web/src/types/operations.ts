import type { FindingCategory, HumanStatus, SeverityLevel } from './review'

export interface OperationalFinding {
  id: number
  reviewId: number
  projectName?: string | null
  severity: SeverityLevel
  category: FindingCategory
  title: string
  humanStatus: HumanStatus
  confidence?: number | null
  isCrossHit?: boolean
}

export type OwnerRole = 'Security Owner' | 'Tech Lead' | 'Performance Owner' | 'Code Owner'
export type LearningAction = 'PROMOTE_TO_RULE' | 'SUPPRESS_PATTERN'

export interface RemediationQueueItem {
  findingId: number
  reviewId: number
  projectName: string
  title: string
  severity: SeverityLevel
  category: FindingCategory
  humanStatus: HumanStatus
  ownerRole: OwnerRole
  slaHours: number
  priorityScore: number
}

export interface RemediationSummary {
  total: number
  blockerCount: number
  humanPendingCount: number
  byOwner: Record<string, number>
  slaPressure: number
}

export interface OperationOwnerLoad {
  role: string
  count: number
  percent: number
}

export interface RuleLearningCandidate {
  findingId: number
  action: LearningAction
  ruleTitle: string
  reason: string
}

export interface OperationsScorecard {
  openRiskItems: number
  blockerCount: number
  ruleLearningCandidates: number
  topOwnerRole: string
  operationalReadiness: number
}

export interface OperationDashboard {
  findings: OperationalFinding[]
  totalFindings: number
  blockerCount: number
  majorCount: number
  pendingCount: number
  confirmedCount: number
  dismissedCount: number
  slaPressure: number
}

export interface BusinessImpactInput {
  monthlyReviews: number
  averageManualReviewMinutes: number
  automationCoveragePercent: number
  blockerFindings: number
  majorFindings: number
}

export interface BusinessImpactEstimate {
  hoursSaved: number
  avoidedReworkHours: number
  executiveSummary: string
}

export interface StrategyTelemetry {
  strategyKey: string
  totalCalls: number
  failedCalls: number
  totalTokens: number
  avgLatencyMs: number
  failureRatePercent: number
  avgCostMicroCents: number
  confirmedFindings: number
  dismissedFindings: number
  pendingFindings: number
  confirmationRatePercent: number
  falsePositiveProxyPercent: number
  reviewedReviews: number
  totalFindings: number
  strategyHitRatePercent: number
  findingsPerReview: number
  crossHitFindings: number
  crossHitRatePercent: number
  modelDiversity: number
  judgeCalls: number
  judgeFailureRatePercent: number
}

export interface ModelTelemetrySummary {
  totalCalls: number
  failedCalls: number
  totalTokens: number
  totalCostMicroCents: number
  avgLatencyMs: number
  failureRatePercent: number
  avgCostMicroCents: number
  strategies: StrategyTelemetry[]
}

export type StrategyPressureLevel = 'HIGH' | 'MEDIUM' | 'LOW'
export type TelemetryReadinessLevel = 'READY' | 'NEEDS_ATTRIBUTION' | 'JUDGE_UNSTABLE' | 'NOT_CONNECTED'

export interface StrategyPressureItem extends StrategyTelemetry {
  pressureScore: number
  pressureLevel: StrategyPressureLevel
  recommendation: string
}

export interface TelemetryReadinessItem {
  strategyKey: string
  totalCalls: number
  reviewedReviews: number
  modelDiversity: number
  crossHitRatePercent: number
  judgeFailureRatePercent: number
  readinessLevel: TelemetryReadinessLevel
  gapCode: string
  recommendation: string
}

export interface OperationsCiHealthAction {
  key: string
  connectorKey: string
  provider: string
  healthStatus: string
  severity: 'CRITICAL' | 'WARNING' | 'INFO' | string
  ownerRole: string
  slaHours: number
  latestSignal: string
  recommendation: string
}

export interface OperationsTask {
  taskKey: string
  sourceType: 'FINDING' | 'CI_HEALTH' | string
  sourceId: string
  sourceRef: string
  title: string
  status: string
  severity: string
  ownerRole: string
  slaHours: number
  priorityScore: number
  latestSignal: string | null
  recommendation: string
  closeReason?: string | null
  externalIssue?: OperationsExternalIssue | null
  createdAt?: string | null
  closedAt?: string | null
  updatedAt?: string | null
  slaDueAt?: string | null
  slaState?: 'ON_TRACK' | 'DUE_SOON' | 'OVERDUE' | 'CLOSED' | 'UNTRACKED' | 'NO_SLA' | string
  remainingHours?: number | null
}

export interface OperationsExternalIssue {
  taskKey: string
  provider: string
  issueStatus: 'SYNCED' | 'SKIPPED' | 'FAILED' | string
  externalIssueId?: string | null
  externalIssueIid?: string | null
  externalIssueUrl?: string | null
  externalIssueState?: string | null
  externalIssueTitle?: string | null
  externalIssueLabels?: string | null
  externalIssueAssignee?: string | null
  externalIssueAuthor?: string | null
  externalUpdatedAt?: string | null
  externalClosedAt?: string | null
  requestUrl?: string | null
  errorMessage?: string | null
  syncedAt?: string | null
}
