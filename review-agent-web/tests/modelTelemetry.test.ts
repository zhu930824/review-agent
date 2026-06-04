import assert from 'node:assert/strict'
import test from 'node:test'

import { deriveModelCallHealth, formatTokenCount } from '../src/utils/modelTelemetry'

test('model telemetry formats token counts for compact cards', () => {
  assert.equal(formatTokenCount(9000), '9000')
  assert.equal(formatTokenCount(12000), '12.0K')
  assert.equal(formatTokenCount(1_250_000), '1.3M')
})

test('model call health derives rates, score, tone, and cost', () => {
  const health = deriveModelCallHealth({
    totalCalls: 100,
    totalTokens: 250000,
    failedCalls: 4,
    avgLatencyMs: 1800,
  })

  assert.equal(health.failureRate, 4)
  assert.equal(health.successRate, 96)
  assert.equal(health.tone, 'warning')
  assert.equal(health.estimatedCostUsd, 0.5)
  assert.ok(health.healthScore < 96)
  assert.equal(health.summary.includes('100'), true)
})

test('model call health clamps invalid failed calls and handles empty stats', () => {
  const empty = deriveModelCallHealth(null)
  assert.equal(empty.healthScore, 100)
  assert.equal(empty.tone, 'healthy')
  assert.equal(empty.summary.includes('暂无'), true)

  const clamped = deriveModelCallHealth({
    totalCalls: 2,
    totalTokens: 1000,
    failedCalls: 5,
    avgLatencyMs: 6000,
  })
  assert.equal(clamped.failedCalls, 2)
  assert.equal(clamped.failureRate, 100)
  assert.equal(clamped.tone, 'critical')
})
