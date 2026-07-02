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
