import assert from 'node:assert/strict'
import test from 'node:test'

import {
  compileGovernancePolicyPack,
  getCapabilityCoverageSummary,
  getConnectorsByStage,
  getRecommendedNextActions,
  governanceRulePacks,
  marketCapabilities,
  workflowTemplates,
} from '../utils/governanceCatalog'

test('market capability catalog covers core AI review product expectations', () => {
  const capabilityIds = marketCapabilities.map(item => item.id)

  assert.ok(capabilityIds.includes('pr-summary'))
  assert.ok(capabilityIds.includes('quality-gate'))
  assert.ok(capabilityIds.includes('security-scan'))
  assert.ok(capabilityIds.includes('sarif-export'))
  assert.ok(capabilityIds.includes('autofix-workflow'))
})

test('coverage summary separates enabled, partial, and planned capabilities', () => {
  const summary = getCapabilityCoverageSummary()

  assert.ok(summary.total >= 10)
  assert.ok(summary.enabled >= 4)
  assert.ok(summary.partial >= 2)
  assert.ok(summary.planned >= 1)
  assert.equal(summary.coveragePercent, Math.round(((summary.enabled + summary.partial * 0.5) / summary.total) * 100))
})

test('recommended actions are sorted by business impact and readiness', () => {
  const actions = getRecommendedNextActions(4)

  assert.equal(actions.length, 4)
  assert.ok(actions[0].priorityScore >= actions[1].priorityScore)
  assert.ok(actions.every(action => action.businessImpact !== 'LOW'))
})

test('connectors can be grouped by rollout stage', () => {
  const connectorsByStage = getConnectorsByStage()

  assert.ok(connectorsByStage.live.some(connector => connector.id === 'local-repository'))
  assert.ok(connectorsByStage.next.some(connector => connector.id === 'github-checks'))
  assert.ok(connectorsByStage.later.some(connector => connector.id === 'jira-linear-sync'))
})

test('governance rule packs compile to review strategy inputs', () => {
  const compiled = compileGovernancePolicyPack(['security-release', 'ai-generated-code'])

  assert.deepEqual(compiled.packIds, ['security-release', 'ai-generated-code'])
  assert.ok(compiled.requiredCapabilities.includes('security-scan'))
  assert.ok(compiled.requiredCapabilities.includes('quality-gate'))
  assert.ok(compiled.suggestedStrategies.includes('quality-gate'))
  assert.ok(compiled.requiredHumanCheckpoints.includes('security owner sign-off'))
})

test('workflow templates map business scenarios to model strategies and governance packs', () => {
  const releaseGate = workflowTemplates.find(item => item.id === 'release-gate')

  assert.ok(releaseGate)
  assert.equal(releaseGate.strategyId, 'quality-gate')
  assert.ok(releaseGate.rulePackIds.includes('security-release'))
  assert.ok(releaseGate.integrationIds.includes('github-checks'))
})

test('rule packs have measurable coverage metadata', () => {
  assert.ok(governanceRulePacks.length >= 5)
  assert.ok(governanceRulePacks.every(pack => pack.controls.length > 0))
  assert.ok(governanceRulePacks.every(pack => pack.businessOutcome.length > 0))
})
