import type {
  BusinessImpactEstimate,
  BusinessImpactInput,
  OperationalFinding,
  OperationsScorecard,
  OwnerRole,
  RemediationQueueItem,
  RemediationSummary,
  RuleLearningCandidate,
  StrategyPressureItem,
  StrategyTelemetry,
} from '../types/operations'
import type { FindingCategory, SeverityLevel } from '../types/review'

const severityScore: Record<SeverityLevel, number> = {
  BLOCKER: 100,
  MAJOR: 70,
  MINOR: 30,
  INFO: 10,
}

const categoryOwner: Record<FindingCategory, OwnerRole> = {
  SECURITY: 'Security Owner',
  PERFORMANCE: 'Performance Owner',
  BUG: 'Tech Lead',
  EXCEPTION_HANDLING: 'Tech Lead',
  CODE_STYLE: 'Code Owner',
  OTHER: 'Code Owner',
}

const severitySlaHours: Record<SeverityLevel, number> = {
  BLOCKER: 4,
  MAJOR: 24,
  MINOR: 72,
  INFO: 168,
}

export function buildRemediationQueue(findings: readonly OperationalFinding[] = []): RemediationQueueItem[] {
  return findings
    .map(finding => ({
      findingId: finding.id,
      reviewId: finding.reviewId,
      projectName: finding.projectName ?? 'unknown-project',
      title: finding.title,
      severity: finding.severity,
      category: finding.category,
      humanStatus: finding.humanStatus,
      ownerRole: categoryOwner[finding.category] ?? 'Code Owner',
      slaHours: severitySlaHours[finding.severity],
      priorityScore: getPriorityScore(finding),
    }))
    .sort((left, right) => right.priorityScore - left.priorityScore)
}

export function summarizeRemediationQueue(queue: readonly RemediationQueueItem[] = []): RemediationSummary {
  const byOwner = queue.reduce<Record<string, number>>((acc, item) => {
    acc[item.ownerRole] = (acc[item.ownerRole] ?? 0) + 1
    return acc
  }, {})
  const blockerCount = queue.filter(item => item.severity === 'BLOCKER').length
  const humanPendingCount = queue.filter(item => item.humanStatus === 'PENDING').length
  const urgentCount = queue.filter(item => item.slaHours <= 24).length

  return {
    total: queue.length,
    blockerCount,
    humanPendingCount,
    byOwner,
    slaPressure: queue.length ? Math.round((urgentCount / queue.length) * 100) : 0,
  }
}

export function extractRuleLearningCandidates(findings: readonly OperationalFinding[] = []): RuleLearningCandidate[] {
  return findings.flatMap(finding => {
    if (finding.humanStatus === 'CONFIRMED' && (finding.isCrossHit || (finding.confidence ?? 0) >= 0.85)) {
      return [{
        findingId: finding.id,
        action: 'PROMOTE_TO_RULE' as const,
        ruleTitle: `固化规则：${finding.title}`,
        reason: '人工已确认且置信度高，适合沉淀为团队规则或质量门禁条件。',
      }]
    }

    if (finding.humanStatus === 'DISMISSED' && (finding.confidence ?? 1) <= 0.5) {
      return [{
        findingId: finding.id,
        action: 'SUPPRESS_PATTERN' as const,
        ruleTitle: `降噪模式：${finding.title}`,
        reason: '人工已驳回且置信度低，适合加入降噪样例，减少重复干扰。',
      }]
    }

    return []
  })
}

export function deriveOperationsScorecard(findings: readonly OperationalFinding[] = []): OperationsScorecard {
  const queue = buildRemediationQueue(findings)
  const summary = summarizeRemediationQueue(queue)
  const candidates = extractRuleLearningCandidates(findings)
  const sortedOwners = Object.entries(summary.byOwner).sort((left, right) => right[1] - left[1])
  const readinessPenalty = summary.blockerCount * 12 + summary.humanPendingCount * 6

  return {
    openRiskItems: summary.total,
    blockerCount: summary.blockerCount,
    ruleLearningCandidates: candidates.length,
    topOwnerRole: sortedOwners[0]?.[0] ?? 'None',
    operationalReadiness: Math.max(0, Math.min(100, 88 - readinessPenalty + candidates.length * 3)),
  }
}

export function estimateReviewBusinessImpact(input: BusinessImpactInput): BusinessImpactEstimate {
  const automatedMinutes = input.monthlyReviews * input.averageManualReviewMinutes * (input.automationCoveragePercent / 100)
  const hoursSaved = Math.round(automatedMinutes / 60)
  const avoidedReworkHours = Math.round(input.blockerFindings * 6 + input.majorFindings * 2.5)

  return {
    hoursSaved,
    avoidedReworkHours,
    executiveSummary: `按每月 ${input.monthlyReviews} 次审查估算，可节省约 ${hoursSaved} 小时人工审查，并前置规避约 ${avoidedReworkHours} 小时返工。`,
  }
}

export function deriveStrategyPressureItems(strategies: readonly StrategyTelemetry[] = []): StrategyPressureItem[] {
  const maxCost = Math.max(1, ...strategies.map(strategy => strategy.avgCostMicroCents || 0))

  return strategies
    .map(strategy => {
      const lowConfirmationPenalty = Math.max(0, 100 - (strategy.confirmationRatePercent || 0))
      const costPressure = Math.round(((strategy.avgCostMicroCents || 0) / maxCost) * 100)
      const pressureScore = clampScore(Math.round(
        (strategy.failureRatePercent || 0) * 0.30
        + (strategy.falsePositiveProxyPercent || 0) * 0.25
        + lowConfirmationPenalty * 0.25
        + costPressure * 0.10
        + latencyPressure(strategy.avgLatencyMs) * 0.05
        + (strategy.judgeFailureRatePercent || 0) * 0.05
        + Math.max(0, 100 - (strategy.strategyHitRatePercent || 0)) * 0.05,
      ))

      return {
        ...strategy,
        pressureScore,
        pressureLevel: pressureLevel(pressureScore),
        recommendation: strategyRecommendation(strategy, pressureScore),
      }
    })
    .sort((left, right) => right.pressureScore - left.pressureScore)
}

function getPriorityScore(finding: OperationalFinding): number {
  const confidenceBonus = Math.round((finding.confidence ?? 0.5) * 10)
  const crossHitBonus = finding.isCrossHit ? 15 : 0
  const confirmedBonus = finding.humanStatus === 'CONFIRMED' ? 12 : 0
  const pendingBonus = finding.humanStatus === 'PENDING' ? 6 : 0
  const securityBonus = finding.category === 'SECURITY' ? 18 : 0

  return severityScore[finding.severity] + confidenceBonus + crossHitBonus + confirmedBonus + pendingBonus + securityBonus
}

function latencyPressure(avgLatencyMs: number): number {
  if (avgLatencyMs >= 3000) return 100
  if (avgLatencyMs >= 1500) return 70
  if (avgLatencyMs >= 800) return 40
  return 15
}

function pressureLevel(score: number) {
  if (score >= 60) return 'HIGH'
  if (score >= 35) return 'MEDIUM'
  return 'LOW'
}

function strategyRecommendation(strategy: StrategyTelemetry, score: number): string {
  if (score >= 60) {
    return '优先降噪并复核成本口径，必要时缩小适用场景。'
  }
  if ((strategy.judgeFailureRatePercent || 0) > 0) {
    return '复核 Judge 调用稳定性，确认裁决链路是否需要降级兜底。'
  }
  if ((strategy.confirmationRatePercent || 0) < 50) {
    return '补充人工样本复核，确认策略是否需要调参。'
  }
  if ((strategy.strategyHitRatePercent || 0) < 50) {
    return '策略命中偏低，建议检查适用场景和触发条件。'
  }
  if ((strategy.avgCostMicroCents || 0) > 0) {
    return '持续观察成本和质量趋势，保留当前策略。'
  }
  return '保持观察，等待更多调用样本。'
}

function clampScore(score: number): number {
  return Math.max(0, Math.min(100, score))
}
