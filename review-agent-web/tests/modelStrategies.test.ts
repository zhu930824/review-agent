import assert from 'node:assert/strict'
import test from 'node:test'

import {
  builtinModelProfiles,
  builtinModelProviders,
  builtinReviewStrategies,
  compileStrategyConfig,
  deriveGatePolicy,
  enabledModelProfiles,
  getProviderModelCounts,
  getStrategyRoleLabels,
  getStrategySummaries,
} from '../utils/modelStrategies'

test('built-in model providers include DashScope and OpenAI compatible providers', () => {
  const providerIds = builtinModelProviders.map(provider => provider.id)

  assert.ok(providerIds.includes('dashscope'))
  assert.ok(providerIds.includes('openai-compatible'))
})

test('enabled model profiles exclude disabled profiles', () => {
  assert.ok(enabledModelProfiles.every(profile => profile.enabled))
  assert.ok(enabledModelProfiles.length < builtinModelProfiles.length)
})

test('quality gate strategy compiles to agent role bindings', () => {
  const config = compileStrategyConfig('quality-gate')

  assert.equal(config.reviewMode, 'AGENT')
  assert.equal(config.modelsConfig.orchestrationStrategy, 'PARALLEL')
  assert.deepEqual(
    config.modelsConfig.agents.map(agent => agent.role),
    [
      'SECURITY_AUDITOR',
      'PERFORMANCE_ANALYST',
      'CODE_STYLE_CHECKER',
      'EXCEPTION_HANDLER',
      'ARCHITECT_REVIEWER',
    ],
  )
})

test('cross-check strategy compiles to multi-model worker config', () => {
  const config = compileStrategyConfig('cross-check')

  assert.equal(config.reviewMode, 'MULTI')
  assert.ok('models' in config.modelsConfig)
  assert.ok(config.modelsConfig.models.length >= 2)
})

test('gate policy blocks blockers and requires human review for major findings', () => {
  const policy = deriveGatePolicy('quality-gate')

  assert.equal(policy.blockOn.includes('BLOCKER'), true)
  assert.equal(policy.requireHumanReviewOn.includes('MAJOR'), true)
})

test('strategy role labels describe every built-in strategy role', () => {
  for (const strategy of builtinReviewStrategies) {
    assert.ok(getStrategyRoleLabels(strategy.id).length > 0)
  }
})

test('all built-in strategies compile to review create payload fields', () => {
  for (const strategy of builtinReviewStrategies) {
    const compiled = compileStrategyConfig(strategy.id)
    const payload = {
      reviewMode: compiled.reviewMode,
      modelsConfig: JSON.stringify(compiled.modelsConfig),
    }

    assert.equal(typeof payload.reviewMode, 'string')
    assert.equal(typeof payload.modelsConfig, 'string')
    assert.ok(payload.modelsConfig.includes(strategy.id))
  }
})

test('provider model counts include enabled and total profile counts', () => {
  const counts = getProviderModelCounts()
  const dashScope = counts.find(item => item.providerId === 'dashscope')

  assert.ok(dashScope)
  assert.equal(typeof dashScope.totalProfiles, 'number')
  assert.equal(typeof dashScope.enabledProfiles, 'number')
  assert.ok(dashScope.enabledProfiles <= dashScope.totalProfiles)
})

test('strategy summaries include model roles and gate rules', () => {
  const summaries = getStrategySummaries()
  const qualityGate = summaries.find(item => item.id === 'quality-gate')

  assert.ok(qualityGate)
  assert.ok(qualityGate.roleLabels.length >= 5)
  assert.deepEqual(qualityGate.gatePolicy.blockOn, ['BLOCKER'])
})
