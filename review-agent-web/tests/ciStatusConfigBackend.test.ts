import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const serverRoot = join(process.cwd(), '..', 'review-agent-server', 'src', 'main')

test('backend exposes ci status config controller contract', () => {
  const controllerPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'CiStatusConfigController.java')

  assert.equal(existsSync(controllerPath), true)
  const controller = readFileSync(controllerPath, 'utf8')

  assert.match(controller, /@RequestMapping\("\/api\/integration\/ci-config"\)/)
  assert.match(controller, /@GetMapping/)
  assert.match(controller, /@PutMapping/)
  assert.match(controller, /upsertConfig/)
})

test('backend ci status config request supports repository binding and secrets', () => {
  const requestPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'UpsertCiStatusConfigRequest.java')

  assert.equal(existsSync(requestPath), true)
  const request = readFileSync(requestPath, 'utf8')

  assert.match(request, /repoOwner/)
  assert.match(request, /repoName/)
  assert.match(request, /statusContext/)
  assert.match(request, /apiToken/)
  assert.match(request, /webhookSecret/)
  assert.match(request, /sarifUploadEnabled/)
  assert.match(request, /jenkinsParameterTemplate/)
  assert.match(request, /notificationWebhookUrl/)
})

test('backend ci status config response does not expose secrets', () => {
  const voPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'domain', 'dto', 'CiStatusConfigVO.java')

  assert.equal(existsSync(voPath), true)
  const vo = readFileSync(voPath, 'utf8')

  assert.match(vo, /tokenConfigured/)
  assert.match(vo, /webhookSecretConfigured/)
  assert.match(vo, /jenkinsParameterTemplate/)
  assert.match(vo, /notificationWebhookUrl/)
  assert.doesNotMatch(vo, /apiToken;/)
  assert.doesNotMatch(vo, /webhookSecret;/)
})

test('backend exposes github and gitlab webhook receivers', () => {
  const controllerPath = join(serverRoot, 'java', 'com', 'review', 'agent', 'controller', 'IntegrationWebhookController.java')
  const servicePath = join(serverRoot, 'java', 'com', 'review', 'agent', 'service', 'impl', 'IntegrationWebhookDeliveryServiceImpl.java')

  assert.equal(existsSync(controllerPath), true)
  assert.equal(existsSync(servicePath), true)
  const controller = readFileSync(controllerPath, 'utf8')
  const service = readFileSync(servicePath, 'utf8')

  assert.match(controller, /@PostMapping\("\/github"\)/)
  assert.match(controller, /@PostMapping\("\/gitlab"\)/)
  assert.match(controller, /X-Gitlab-Token/)
  assert.match(service, /receiveGitHubDelivery/)
  assert.match(service, /receiveGitLabDelivery/)
  assert.match(service, /GITLAB_CONNECTOR_KEY = "gitlab-merge-request"/)
  assert.match(service, /Invalid GitLab webhook token/)
})
