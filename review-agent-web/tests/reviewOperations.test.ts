import assert from 'node:assert/strict'
import test from 'node:test'

import {
  buildRemediationQueue,
  deriveOperationsScorecard,
  deriveStrategyPressureItems,
  estimateReviewBusinessImpact,
  extractRuleLearningCandidates,
  summarizeRemediationQueue,
} from '../src/utils/reviewOperations'

const sampleFindings = [
  {
    id: 1,
    reviewId: 100,
    projectName: 'payment-core',
    severity: 'BLOCKER',
    category: 'SECURITY',
    title: 'Token can be reused after logout',
    humanStatus: 'CONFIRMED',
    confidence: 0.92,
    isCrossHit: true,
  },
  {
    id: 2,
    reviewId: 100,
    projectName: 'payment-core',
    severity: 'MAJOR',
    category: 'PERFORMANCE',
    title: 'N+1 query in settlement list',
    humanStatus: 'PENDING',
    confidence: 0.82,
    isCrossHit: false,
  },
  {
    id: 3,
    reviewId: 101,
    projectName: 'web-console',
    severity: 'MINOR',
    category: 'CODE_STYLE',
    title: 'Naming can be clearer',
    humanStatus: 'DISMISSED',
    confidence: 0.41,
    isCrossHit: false,
  },
] as const

test('remediation queue prioritizes confirmed cross-hit security blockers', () => {
  const queue = buildRemediationQueue(sampleFindings)

  assert.equal(queue[0].findingId, 1)
  assert.equal(queue[0].ownerRole, 'Security Owner')
  assert.equal(queue[0].slaHours, 4)
  assert.ok(queue[0].priorityScore > queue[1].priorityScore)
})

test('remediation summary exposes SLA pressure and owner workload', () => {
  const summary = summarizeRemediationQueue(buildRemediationQueue(sampleFindings))

  assert.equal(summary.total, 3)
  assert.equal(summary.blockerCount, 1)
  assert.equal(summary.humanPendingCount, 1)
  assert.equal(summary.byOwner['Security Owner'], 1)
  assert.ok(summary.slaPressure >= 50)
})

test('rule learning candidates distinguish confirmed rules from dismissed noise', () => {
  const candidates = extractRuleLearningCandidates(sampleFindings)

  assert.ok(candidates.some(candidate => candidate.action === 'PROMOTE_TO_RULE' && candidate.findingId === 1))
  assert.ok(candidates.some(candidate => candidate.action === 'SUPPRESS_PATTERN' && candidate.findingId === 3))
})

test('operations scorecard combines queue and learning signals', () => {
  const scorecard = deriveOperationsScorecard(sampleFindings)

  assert.equal(scorecard.openRiskItems, 3)
  assert.equal(scorecard.ruleLearningCandidates, 2)
  assert.equal(scorecard.topOwnerRole, 'Security Owner')
  assert.ok(scorecard.operationalReadiness >= 60)
})

test('business impact estimate scales with review volume and avoided defects', () => {
  const impact = estimateReviewBusinessImpact({
    monthlyReviews: 80,
    averageManualReviewMinutes: 35,
    automationCoveragePercent: 65,
    blockerFindings: 6,
    majorFindings: 18,
  })

  assert.ok(impact.hoursSaved >= 30)
  assert.ok(impact.avoidedReworkHours > impact.hoursSaved)
  assert.equal(impact.executiveSummary.includes('80'), true)
})

test('strategy pressure ranks costly noisy strategies first', () => {
  const pressure = deriveStrategyPressureItems([
    {
      strategyKey: 'quality-gate',
      totalCalls: 12,
      failedCalls: 3,
      totalTokens: 12000,
      avgLatencyMs: 1800,
      failureRatePercent: 25,
      avgCostMicroCents: 900,
      confirmedFindings: 2,
      dismissedFindings: 6,
      pendingFindings: 4,
      confirmationRatePercent: 17,
      falsePositiveProxyPercent: 75,
    },
    {
      strategyKey: 'fast-scan',
      totalCalls: 20,
      failedCalls: 0,
      totalTokens: 4000,
      avgLatencyMs: 450,
      failureRatePercent: 0,
      avgCostMicroCents: 90,
      confirmedFindings: 8,
      dismissedFindings: 1,
      pendingFindings: 1,
      confirmationRatePercent: 80,
      falsePositiveProxyPercent: 11,
    },
  ])

  assert.equal(pressure[0].strategyKey, 'quality-gate')
  assert.equal(pressure[0].pressureLevel, 'HIGH')
  assert.ok(pressure[0].pressureScore > pressure[1].pressureScore)
  assert.match(pressure[0].recommendation, /降噪|复核|成本/)
})

