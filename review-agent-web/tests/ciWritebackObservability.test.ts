import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const serverRoot = join(process.cwd(), '..', 'review-agent-server', 'src', 'main')

test('backend exposes ci writeback history contract', () => {
  const controller = readFileSync(join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'CiStatusConfigController.java'), 'utf8')
  const voPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'CiStatusWritebackLogVO.java')
  const migrationPath = join(serverRoot, 'resources', 'db', 'migration', 'V8__ci_writeback_log.sql')

  assert.equal(existsSync(voPath), true)
  assert.equal(existsSync(migrationPath), true)
  assert.match(controller, /@GetMapping\("\/writebacks"\)/)
  assert.match(controller, /listWritebacks/)
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
})

test('governance page loads and renders recent ci writebacks', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /ciWritebacks/)
  assert.match(governanceView, /loadCiWritebacks/)
  assert.match(governanceView, /get<CiStatusWritebackLogVO\[\]>\('\/integration\/ci-config\/writebacks'\)/)
  assert.match(governanceView, /writebackStatus/)
  assert.match(governanceView, /retryCount/)
})

test('governance page can manually retry failed ci writebacks', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /retryCiWriteback/)
  assert.match(governanceView, /ciWritebackRetryingIds/)
  assert.match(governanceView, /重试/)
  assert.match(governanceView, /post<unknown>\(`\/integration\/ci-config\/writebacks\/\$\{item\.id\}\/retry`\)/)
  assert.match(governanceView, /item\.writebackStatus === 'FAILED'/)
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
