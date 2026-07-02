import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('governance page renders ci status readiness panel', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /ciStatusReadiness/)
  assert.match(governanceView, /getCiStatusIntegrationReadiness/)
  assert.match(governanceView, /backendSignals/)
  assert.match(governanceView, /nextActions/)
})

test('governance page turns telemetry readiness gaps into action items', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /telemetryGapActions/)
  assert.match(governanceView, /loadTelemetryReadiness/)
  assert.match(governanceView, /get<TelemetryReadinessItem\[]>\('\/operations\/telemetry-readiness'\)/)
  assert.match(governanceView, /buildTelemetryGapActions/)
  assert.match(governanceView, /telemetryReadinessColor/)
})
