import assert from 'node:assert/strict'
import test from 'node:test'
import { buildCiHealthActions, ciHealthActionColor, buildTelemetryGapActions, telemetryReadinessColor } from '../src/utils/governanceTelemetry'
import type { CiIntegrationHealthVO } from '../src/types/governance'
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

test('buildCiHealthActions turns unhealthy connectors into governance actions', () => {
  const actions = buildCiHealthActions([
    ciHealth('github-checks', 'GITHUB', 'HEALTHY', 'SUCCESS'),
    ciHealth('gitlab-merge-request', 'GITLAB', 'UNHEALTHY', 'FAILED'),
    ciHealth('jenkins-pipeline', 'JENKINS', 'DEGRADED', 'SUCCESS', 'BUILDING'),
    ciHealth('github-checks-empty', 'GITHUB', 'NO_DATA'),
  ])

  assert.deepEqual(actions.map(action => action.connectorKey), [
    'gitlab-merge-request',
    'jenkins-pipeline',
    'github-checks-empty',
  ])
  assert.equal(actions[0].severity, 'critical')
  assert.equal(actions[1].severity, 'warning')
  assert.equal(actions[1].latestSignal, 'SUCCESS / BUILDING')
  assert.match(actions[2].recommendation, /smoke test/)
})

test('ciHealthActionColor maps severities to tag colors', () => {
  assert.equal(ciHealthActionColor('critical'), 'red')
  assert.equal(ciHealthActionColor('warning'), 'orange')
  assert.equal(ciHealthActionColor('info'), 'default')
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

function ciHealth(
  connectorKey: string,
  provider: string,
  healthStatus: CiIntegrationHealthVO['healthStatus'],
  latestWritebackStatus: string | null = null,
  latestExternalResult: string | null = null,
): CiIntegrationHealthVO {
  return {
    connectorKey,
    provider,
    healthStatus,
    totalCount: 1,
    successCount: latestWritebackStatus === 'SUCCESS' ? 1 : 0,
    failedCount: latestWritebackStatus === 'FAILED' ? 1 : 0,
    skippedCount: latestWritebackStatus === 'SKIPPED' ? 1 : 0,
    latestWritebackStatus,
    latestExternalResult,
    latestAt: null,
    summary: null,
  }
}
