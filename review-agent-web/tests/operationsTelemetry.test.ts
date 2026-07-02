import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('operations page renders model strategy pressure from operations api', () => {
  const operationsView = readFileSync(join(process.cwd(), 'src/views/operations.vue'), 'utf8')

  assert.match(operationsView, /loadStrategyPressure/)
  assert.match(operationsView, /get<StrategyPressureItem\[]>\('\/operations\/strategy-pressure'\)/)
  assert.match(operationsView, /strategyPressureItems/)
  assert.match(operationsView, /loadTelemetryReadiness/)
  assert.match(operationsView, /get<TelemetryReadinessItem\[]>\('\/operations\/telemetry-readiness'\)/)
  assert.match(operationsView, /telemetryReadinessItems/)
  assert.match(operationsView, /loadOperationsDashboard/)
  assert.match(operationsView, /get<OperationDashboard>\('\/operations\/dashboard'\)/)
  assert.match(operationsView, /loadOwnerLoad/)
  assert.match(operationsView, /get<OperationOwnerLoad\[]>\('\/operations\/owner-load'\)/)
  assert.match(operationsView, /loadRemediationQueue/)
  assert.match(operationsView, /get<OperationalFinding\[]>\('\/operations\/remediation-queue\?limit=20'\)/)
  assert.match(operationsView, /loadRuleLearningCandidates/)
  assert.match(operationsView, /get<RuleLearningCandidate\[]>\('\/operations\/rule-learning-candidates\?limit=20'\)/)
  assert.match(operationsView, /loadBusinessImpact/)
  assert.match(operationsView, /get<BusinessImpactEstimate>\('\/operations\/business-impact'\)/)
  assert.match(operationsView, /row-key="findingId"/)
  assert.match(operationsView, /pressureScore/)
  assert.match(operationsView, /confirmationRatePercent/)
  assert.match(operationsView, /falsePositiveProxyPercent/)
  assert.match(operationsView, /strategyHitRatePercent/)
  assert.match(operationsView, /crossHitRatePercent/)
  assert.match(operationsView, /judgeFailureRatePercent/)
})
