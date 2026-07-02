import assert from 'node:assert/strict'
import test from 'node:test'
import { buildTelemetryGapActions, telemetryReadinessColor } from '../src/utils/governanceTelemetry'
import type { TelemetryReadinessItem } from '../src/types/operations'

test('buildTelemetryGapActions maps readiness gaps into governance actions', () => {
  const items: TelemetryReadinessItem[] = [
    item('ready-strategy', 'READY', 'NONE', 'No action needed'),
    item('model-telemetry', 'NOT_CONNECTED', 'NO_TELEMETRY', 'Connect recorder'),
    item('cross-check', 'NEEDS_ATTRIBUTION', 'WEAK_ATTRIBUTION', 'Add review and role attribution'),
    item('architecture-board', 'JUDGE_UNSTABLE', 'JUDGE_FAILURE', 'Stabilize judge path'),
  ]

  const actions = buildTelemetryGapActions(items)

  assert.deepEqual(actions.map(action => action.title), [
    '接入模型调用遥测',
    '补齐跨模型归因',
    '修复 Judge 调用稳定性',
  ])
  assert.deepEqual(actions.map(action => action.key), [
    'model-telemetry-NO_TELEMETRY',
    'cross-check-WEAK_ATTRIBUTION',
    'architecture-board-JUDGE_FAILURE',
  ])
  assert.equal(actions[0].recommendation, 'Connect recorder')
})

test('buildTelemetryGapActions keeps unknown gaps reviewable', () => {
  const actions = buildTelemetryGapActions([
    item('experimental', 'NEEDS_ATTRIBUTION', 'MISSING_REVIEW_ID', 'Attach review id'),
  ])

  assert.equal(actions.length, 1)
  assert.equal(actions[0].title, '复核遥测接入')
  assert.equal(actions[0].gapCode, 'MISSING_REVIEW_ID')
})

test('telemetryReadinessColor maps levels to tag colors', () => {
  assert.equal(telemetryReadinessColor('READY'), 'green')
  assert.equal(telemetryReadinessColor('NEEDS_ATTRIBUTION'), 'orange')
  assert.equal(telemetryReadinessColor('JUDGE_UNSTABLE'), 'red')
  assert.equal(telemetryReadinessColor('NOT_CONNECTED'), 'default')
})

function item(
  strategyKey: string,
  readinessLevel: TelemetryReadinessItem['readinessLevel'],
  gapCode: string,
  recommendation: string,
): TelemetryReadinessItem {
  return {
    strategyKey,
    totalCalls: 1,
    reviewedReviews: 1,
    modelDiversity: 1,
    crossHitRatePercent: 0,
    judgeFailureRatePercent: 0,
    readinessLevel,
    gapCode,
    recommendation,
  }
}
