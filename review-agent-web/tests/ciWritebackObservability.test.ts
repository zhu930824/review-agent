import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const serverRoot = join(process.cwd(), '..', 'review-agent-server', 'src', 'main')

test('backend exposes ci writeback history contract', () => {
  const controller = readFileSync(join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'CiStatusConfigController.java'), 'utf8')
  const voPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'CiStatusWritebackLogVO.java')
  const healthVoPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'CiIntegrationHealthVO.java')
  const migrationPath = join(serverRoot, 'resources', 'db', 'migration', 'V8__ci_writeback_log.sql')

  assert.equal(existsSync(voPath), true)
  assert.equal(existsSync(healthVoPath), true)
  assert.equal(existsSync(migrationPath), true)
  assert.match(controller, /@GetMapping\("\/writebacks"\)/)
  assert.match(controller, /listWritebacks/)
  assert.match(controller, /@GetMapping\("\/writebacks\/health"\)/)
  assert.match(controller, /listWritebackHealth/)
  assert.match(controller, /@PostMapping\("\/writebacks\/jenkins\/refresh"\)/)
  assert.match(controller, /refreshJenkinsWritebackResults/)
})

test('backend ci writeback log records retry and failure state', () => {
  const entityPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'entity', 'CiStatusWritebackLog.java')

  assert.equal(existsSync(entityPath), true)
  const entity = readFileSync(entityPath, 'utf8')

  assert.match(entity, /reviewId/)
  assert.match(entity, /commitSha/)
  assert.match(entity, /writebackStatus/)
  assert.match(entity, /errorMessage/)
  assert.match(entity, /retryCount/)
  assert.match(entity, /nextRetryAt/)
  assert.match(entity, /externalQueueUrl/)
  assert.match(entity, /externalBuildUrl/)
  assert.match(entity, /externalBuildNumber/)
  assert.match(entity, /externalBuildResult/)
})

test('governance page loads and renders recent ci writebacks', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')
  const governanceTypes = readFileSync(join(process.cwd(), 'src/types/governance.ts'), 'utf8')

  assert.match(governanceView, /ciWritebacks/)
  assert.match(governanceView, /ciIntegrationHealth/)
  assert.match(governanceView, /loadCiWritebacks/)
  assert.match(governanceView, /loadCiIntegrationHealth/)
  assert.match(governanceView, /get<CiStatusWritebackLogVO\[\]>\('\/integration\/ci-config\/writebacks'\)/)
  assert.match(governanceView, /get<CiIntegrationHealthVO\[\]>\('\/integration\/ci-config\/writebacks\/health'\)/)
  assert.match(governanceView, /writebackStatus/)
  assert.match(governanceView, /retryCount/)
  assert.match(governanceView, /externalBuildResult/)
  assert.match(governanceView, /refreshJenkinsResults/)
  assert.match(governanceView, /ciHealthColor/)
  assert.match(governanceView, /CI Health Actions/)
  assert.match(governanceView, /buildCiHealthActions/)
  assert.match(governanceView, /ciHealthActionColor/)
  assert.match(governanceTypes, /interface CiIntegrationHealthVO/)
  assert.match(governanceTypes, /healthStatus/)
})

test('governance page can manually retry failed ci writebacks', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /retryCiWriteback/)
  assert.match(governanceView, /ciWritebackRetryingIds/)
  assert.match(governanceView, /重试/)
  assert.match(governanceView, /post<unknown>\(`\/integration\/ci-config\/writebacks\/\$\{item\.id\}\/retry`\)/)
  assert.match(governanceView, /item\.writebackStatus === 'FAILED'/)
})

test('governance page can refresh jenkins build results', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /jenkinsResultRefreshing/)
  assert.match(governanceView, /post<number>\('\/integration\/ci-config\/writebacks\/jenkins\/refresh\?limit=20'\)/)
  assert.match(governanceView, /jenkinsBuildResultColor/)
})

test('backend exposes integration action log query contract', () => {
  const controllerPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'IntegrationActionLogController.java')
  const voPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'IntegrationActionLogVO.java')
  const migrationPath = join(serverRoot, 'resources', 'db', 'migration', 'V11__integration_action_log.sql')

  assert.equal(existsSync(controllerPath), true)
  assert.equal(existsSync(voPath), true)
  assert.equal(existsSync(migrationPath), true)

  const controller = readFileSync(controllerPath, 'utf8')
  assert.match(controller, /@RequestMapping\("\/api\/integration\/actions"\)/)
  assert.match(controller, /listActions/)
})

test('governance page loads and renders recent integration actions', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /integrationActions/)
  assert.match(governanceView, /loadIntegrationActions/)
  assert.match(governanceView, /get<IntegrationActionLogVO\[\]>\('\/integration\/actions'\)/)
  assert.match(governanceView, /actionStatus/)
  assert.match(governanceView, /actionStatusColor/)
})

test('backend exposes webhook delivery log query contract', () => {
  const controllerPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'IntegrationWebhookController.java')
  const servicePath = join(serverRoot, 'java', 'com', 'review', 'agent', 'service', 'impl', 'IntegrationWebhookDeliveryLogServiceImpl.java')
  const voPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'IntegrationWebhookDeliveryLogVO.java')

  assert.equal(existsSync(controllerPath), true)
  assert.equal(existsSync(servicePath), true)
  assert.equal(existsSync(voPath), true)

  const controller = readFileSync(controllerPath, 'utf8')
  const service = readFileSync(servicePath, 'utf8')
  const vo = readFileSync(voPath, 'utf8')

  assert.match(controller, /@GetMapping\("\/deliveries"\)/)
  assert.match(controller, /deliveryLogService\.listRecent\(limit\)/)
  assert.match(service, /repository\.listRecent\(safeLimit\)/)
  assert.match(vo, /deliveryStatus/)
  assert.match(vo, /payloadDigest/)
  assert.match(vo, /receivedAt/)
  assert.match(vo, /triggerStatus/)
  assert.match(vo, /triggerKey/)
  assert.match(vo, /triggerReviewId/)
  assert.match(vo, /triggerRetryCount/)
  assert.match(vo, /triggerNextRetryAt/)
})

test('governance page loads and renders recent webhook deliveries', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')
  const governanceTypes = readFileSync(join(process.cwd(), 'src/types/governance.ts'), 'utf8')

  assert.match(governanceView, /webhookDeliveries/)
  assert.match(governanceView, /loadWebhookDeliveries/)
  assert.match(governanceView, /get<IntegrationWebhookDeliveryLogVO\[\]>\('\/integration\/webhooks\/deliveries\?limit=10'\)/)
  assert.match(governanceView, /Recent Webhook Deliveries/)
  assert.match(governanceView, /webhookDeliveryStatusColor/)
  assert.match(governanceView, /triggerReviewId/)
  assert.match(governanceView, /Review trigger/)
  assert.match(governanceView, /webhookTriggerStatusColor/)
  assert.match(governanceView, /item\.triggerKey/)
  assert.match(governanceView, /retryWebhookReviewTrigger/)
  assert.match(governanceView, /review-triggers\/retry/)
  assert.match(governanceView, /review-triggers\/health/)
  assert.match(governanceView, /webhookTriggerHealth/)
  assert.match(governanceView, /testCiConnection/)
  assert.match(governanceView, /\/integration\/ci-config\/test\?connectorKey=/)
  assert.match(governanceView, /ciConnectionTestResult/)
  assert.match(governanceView, /credential-health/)
  assert.match(governanceView, /credentialSecurityHealth/)
  assert.match(governanceView, /credential-rotation/)
  assert.match(governanceView, /ROTATE CREDENTIALS/)
  assert.match(governanceView, /unreadableCredentialCount/)
  assert.match(governanceTypes, /interface IntegrationWebhookDeliveryLogVO/)
  assert.match(governanceTypes, /deliveryStatus/)
  assert.match(governanceTypes, /triggerStatus/)
  assert.match(governanceTypes, /triggerKey/)
  assert.match(governanceTypes, /interface GitLabReviewTriggerHealthVO/)
  assert.match(governanceTypes, /interface CiConnectionTestResultVO/)
  assert.match(governanceTypes, /interface CredentialSecurityHealthVO/)
})
