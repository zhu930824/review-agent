import assert from 'node:assert/strict'
import test from 'node:test'

import {
  deriveDashboardMetrics,
  deriveGateStatus,
  deriveModelSuccessRate,
  generateBlockedReasons,
} from '../src/utils/reviewMetrics'

const reviews = [
  { status: 'COMPLETED', reviewMode: 'AGENT' },
  { status: 'RUNNING', reviewMode: 'MULTI' },
  { status: 'FAILED', reviewMode: 'JUDGE' },
]

const detail = {
  status: 'COMPLETED',
  findings: [
    { severity: 'BLOCKER', humanStatus: 'PENDING', title: 'SQL 注入风险' },
    { severity: 'MAJOR', humanStatus: 'PENDING', title: '异常被吞掉' },
    { severity: 'MINOR', humanStatus: 'CONFIRMED', title: '命名不一致' },
  ],
  modelResults: [
    { status: 'COMPLETED' },
    { status: 'COMPLETED' },
    { status: 'FAILED' },
  ],
}

test('dashboard metrics describe throughput and quality pressure', () => {
  const metrics = deriveDashboardMetrics(reviews, [detail])

  assert.equal(metrics.totalReviews, 3)
  assert.equal(metrics.completedReviews, 1)
  assert.equal(metrics.runningReviews, 1)
  assert.equal(metrics.blockerFindings, 1)
  assert.equal(metrics.pendingHumanConfirmations, 2)
})

test('gate status blocks when blocker findings exist', () => {
  assert.equal(deriveGateStatus(detail), 'BLOCKED')
})

test('gate status prefers persisted backend pre-pr decision', () => {
  assert.equal(deriveGateStatus({
    status: 'COMPLETED',
    prePrStatus: 'PASSED',
    findings: [
      { severity: 'BLOCKER', humanStatus: 'PENDING', title: 'legacy blocker already waived' },
    ],
  }), 'PASSED')
})

test('blocked reasons include blocker and human review pressure', () => {
  const reasons = generateBlockedReasons(detail)

  assert.ok(reasons.some(reason => reason.includes('BLOCKER')))
  assert.ok(reasons.some(reason => reason.includes('人工复核')))
})

test('blocked reasons prefer persisted backend reasons', () => {
  const reasons = generateBlockedReasons({
    status: 'COMPLETED',
    prePrStatus: 'BLOCKED',
    blockedReasons: ['backend persisted gate reason'],
    findings: [
      { severity: 'BLOCKER', humanStatus: 'PENDING', title: 'duplicate local blocker' },
    ],
  })

  assert.deepEqual(reasons, ['backend persisted gate reason'])
})

test('model success rate is rounded percentage', () => {
  assert.equal(deriveModelSuccessRate(detail.modelResults), 67)
})
