import type { OperationalFinding, StrategyEffectSummary } from '../types/operations'
import type { ReviewMode } from '../types/review'

const UNKNOWN_STRATEGY = 'unknown-strategy'

export function extractStrategyId(modelsConfig?: string | null): string {
  if (!modelsConfig?.trim()) return UNKNOWN_STRATEGY

  try {
    const parsed = JSON.parse(modelsConfig)
    const strategyId = parsed?.strategyId
    return typeof strategyId === 'string' && strategyId.trim()
      ? strategyId.trim()
      : UNKNOWN_STRATEGY
  } catch {
    return UNKNOWN_STRATEGY
  }
}

export function summarizeStrategyEffects(
  findings: readonly OperationalFinding[] = [],
): StrategyEffectSummary[] {
  const groups = findings.reduce<Map<string, MutableStrategyEffect>>((acc, finding) => {
    const strategyId = extractStrategyId(finding.modelsConfig)
    const key = `${strategyId}:${finding.reviewMode ?? 'UNKNOWN'}`
    const current = acc.get(key) ?? createMutableSummary(strategyId, finding.reviewMode ?? 'UNKNOWN')

    current.findingCount += 1
    if (finding.severity === 'BLOCKER') current.blockerCount += 1
    if (finding.severity === 'MAJOR') current.majorCount += 1
    if (finding.humanStatus === 'CONFIRMED') current.confirmedCount += 1
    if (finding.humanStatus === 'DISMISSED') current.dismissedCount += 1
    if (finding.humanStatus === 'PENDING') current.pendingCount += 1
    if (finding.isCrossHit) current.crossHitCount += 1

    acc.set(key, current)
    return acc
  }, new Map())

  return Array.from(groups.values())
    .map(finalizeStrategyEffect)
    .sort((left, right) => right.effectivenessScore - left.effectivenessScore)
}

type MutableStrategyEffect = Omit<
  StrategyEffectSummary,
  'confirmationRate' | 'falsePositiveRate' | 'crossHitRate' | 'effectivenessScore'
>

function createMutableSummary(strategyId: string, reviewMode: ReviewMode | 'UNKNOWN'): MutableStrategyEffect {
  return {
    strategyId,
    reviewMode,
    findingCount: 0,
    blockerCount: 0,
    majorCount: 0,
    confirmedCount: 0,
    dismissedCount: 0,
    pendingCount: 0,
    crossHitCount: 0,
  }
}

function finalizeStrategyEffect(summary: MutableStrategyEffect): StrategyEffectSummary {
  const confirmationRate = percent(summary.confirmedCount, summary.findingCount)
  const falsePositiveRate = percent(summary.dismissedCount, summary.findingCount)
  const crossHitRate = percent(summary.crossHitCount, summary.findingCount)
  const riskInterceptionScore = Math.min(35, summary.blockerCount * 12 + summary.majorCount * 5)
  const effectivenessScore = clamp(
    45 + riskInterceptionScore + Math.round(crossHitRate * 0.2) + Math.round(confirmationRate * 0.15) - Math.round(falsePositiveRate * 0.35),
    0,
    100,
  )

  return {
    ...summary,
    confirmationRate,
    falsePositiveRate,
    crossHitRate,
    effectivenessScore,
  }
}

function percent(count: number, total: number): number {
  return total ? Math.round((count / total) * 100) : 0
}

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value))
}
