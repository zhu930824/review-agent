import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const serverRoot = join(process.cwd(), '..', 'review-agent-server', 'src', 'main')

test('backend exposes model telemetry contracts', () => {
  const controllerPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'ModelTelemetryController.java')
  const requestPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'ModelCallTelemetryRequest.java')
  const summaryPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'ModelTelemetrySummaryVO.java')
  const migrationPath = join(serverRoot, 'resources', 'db', 'migration', 'V12__model_call_telemetry.sql')

  assert.equal(existsSync(controllerPath), true)
  assert.equal(existsSync(requestPath), true)
  assert.equal(existsSync(summaryPath), true)
  assert.equal(existsSync(migrationPath), true)

  const controller = readFileSync(controllerPath, 'utf8')
  assert.match(controller, /@RequestMapping\("\/api\/model-telemetry"\)/)
  assert.match(controller, /@PostMapping\("\/records"\)/)
  assert.match(controller, /@GetMapping\("\/summary"\)/)
})

test('gateway page loads model telemetry summary and renders strategy metrics', () => {
  const gatewayView = readFileSync(join(process.cwd(), 'src/views/gateway.vue'), 'utf8')

  assert.match(gatewayView, /modelTelemetrySummary/)
  assert.match(gatewayView, /loadModelTelemetrySummary/)
  assert.match(gatewayView, /get<ModelTelemetrySummary>\('\/model-telemetry\/summary'\)/)
  assert.match(gatewayView, /modelTelemetrySummary\.strategies/)
  assert.match(gatewayView, /失败率/)
  assert.match(gatewayView, /平均成本/)
  assert.match(gatewayView, /formatMicroCents/)
  assert.match(gatewayView, /strategy\.avgCostMicroCents/)
  assert.match(gatewayView, /confirmedFindings/)
  assert.match(gatewayView, /dismissedFindings/)
  assert.match(gatewayView, /pendingFindings/)
  assert.match(gatewayView, /confirmationRatePercent/)
  assert.match(gatewayView, /falsePositiveProxyPercent/)
  assert.match(gatewayView, /strategyHitRatePercent/)
  assert.match(gatewayView, /crossHitRatePercent/)
  assert.match(gatewayView, /modelDiversity/)
  assert.match(gatewayView, /judgeFailureRatePercent/)
})
