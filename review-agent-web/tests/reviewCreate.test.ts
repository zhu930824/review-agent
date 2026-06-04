import assert from 'node:assert/strict'
import test from 'node:test'
import {
  buildReviewCreatePayload,
  compileReviewCreateConfig,
  getReviewCreateEndpoint,
  getReviewCreateModelName,
  getReviewCreateRoleLabels,
  type ReviewCreateStrategy,
} from '../src/utils/reviewCreate'

const strategy: ReviewCreateStrategy = {
  reviewMode: 'AGENT',
  blockOn: ['BLOCKER'],
  requireHumanReviewOn: ['MAJOR'],
  advisoryOn: ['MINOR'],
  roleBindings: [
    { role: 'SECURITY_AUDITOR', modelProfileName: '通义千问 Plus' },
    { role: 'JUDGE', modelProfileName: 'DeepSeek V3' },
  ],
}

test('review create model names normalize known and custom profile names', () => {
  assert.equal(getReviewCreateModelName('通义千问 Plus'), 'qwen-plus')
  assert.equal(getReviewCreateModelName('Custom Model V1'), 'custom-model-v1')
  assert.equal(getReviewCreateModelName(undefined), 'qwen-plus')
})

test('review create role labels include friendly role and fallback model name', () => {
  assert.deepEqual(getReviewCreateRoleLabels({
    reviewMode: 'AGENT',
    roleBindings: [{ role: 'ARCHITECT_REVIEWER' }, { role: 'CUSTOM_ROLE', modelProfileName: 'Model X' }],
  }), ['架构评审员 · 未知模型', 'CUSTOM_ROLE · Model X'])
})

test('review create config compiles strategy bindings and gate policy', () => {
  const compiled = compileReviewCreateConfig(strategy, true)

  assert.equal(compiled.reviewMode, 'AGENT')
  assert.equal(compiled.modelsConfig.mcpEnabled, true)
  assert.equal(compiled.modelsConfig.orchestrationStrategy, 'PARALLEL')
  assert.deepEqual(compiled.modelsConfig.gatePolicy, {
    blockOn: ['BLOCKER'],
    requireHumanReviewOn: ['MAJOR'],
    advisoryOn: ['MINOR'],
  })
  assert.deepEqual(
    compiled.modelsConfig.agents.map(agent => `${agent.role}:${agent.modelName}`),
    ['SECURITY_AUDITOR:qwen-plus', 'JUDGE:deepseek-v3'],
  )
})

test('review create payload uses override models config when provided', () => {
  const payload = buildReviewCreatePayload({
    projectId: '12',
    sourceBranch: 'feature/auth',
    targetBranch: 'main',
    mcpEnabled: false,
    modelsConfigOverride: '  {"manual":true}  ',
  }, strategy)

  assert.deepEqual(payload, {
    projectId: 12,
    sourceBranch: 'feature/auth',
    targetBranch: 'main',
    reviewMode: 'AGENT',
    modelsConfig: '{"manual":true}',
  })
})

test('review create payload serializes compiled models config by default', () => {
  const payload = buildReviewCreatePayload({
    projectId: '7',
    sourceBranch: 'dev',
    targetBranch: 'main',
    mcpEnabled: true,
    modelsConfigOverride: '',
  }, strategy)
  const modelsConfig = JSON.parse(payload.modelsConfig)

  assert.equal(payload.projectId, 7)
  assert.equal(payload.reviewMode, 'AGENT')
  assert.equal(modelsConfig.mcpEnabled, true)
  assert.equal(modelsConfig.gatePolicy.blockOn[0], 'BLOCKER')
})

test('review create endpoint uses pre-pr route for pre-pr submissions', () => {
  assert.equal(getReviewCreateEndpoint(true), '/reviews/pre-pr')
  assert.equal(getReviewCreateEndpoint(false), '/reviews')
})
