import type { FindingCategory, HumanStatus, ReviewMode, SeverityLevel } from './review'

export interface OperationalFinding {
  id: number
  reviewId: number
  projectName?: string | null
  reviewMode?: ReviewMode | null
  modelsConfig?: string | null
  severity: SeverityLevel
  category: FindingCategory
  title: string
  humanStatus: HumanStatus
  confidence?: number | null
  isCrossHit?: boolean
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

export interface ModelCallStats {
  totalCalls: number
  totalTokens: number
  failedCalls: number
  avgLatencyMs: number
}

export type ModelCallHealthTone = 'healthy' | 'warning' | 'critical'

export interface ModelCallHealth {
  totalCalls: number
  totalTokens: number
  failedCalls: number
  avgLatencyMs: number
  failureRate: number
  successRate: number
  healthScore: number
  estimatedCostUsd: number
  formattedTokens: string
  tone: ModelCallHealthTone
  summary: string
}

export interface StrategyEffectSummary {
  strategyId: string
  reviewMode: ReviewMode | 'UNKNOWN'
  findingCount: number
  blockerCount: number
  majorCount: number
  confirmedCount: number
  dismissedCount: number
  pendingCount: number
  crossHitCount: number
  confirmationRate: number
  falsePositiveRate: number
  crossHitRate: number
  effectivenessScore: number
}
