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

test('governance page loads backend catalog before falling back to local catalog', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /loadGovernanceCatalog/)
  assert.match(governanceView, /get<MarketCapability\[]>\('\/governance\/capabilities'\)/)
  assert.match(governanceView, /get<IntegrationConnector\[]>\('\/governance\/connectors'\)/)
  assert.match(governanceView, /get<GovernanceRulePack\[]>\('\/governance\/rule-packs'\)/)
  assert.match(governanceView, /get<WorkflowTemplate\[]>\('\/governance\/workflows'\)/)
  assert.match(governanceView, /fallbackCapabilities/)
  assert.match(governanceView, /fallbackWorkflowTemplates/)
})
