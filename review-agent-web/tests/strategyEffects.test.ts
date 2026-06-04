import assert from 'node:assert/strict'
import test from 'node:test'

import { extractStrategyId, summarizeStrategyEffects } from '../src/utils/strategyEffects'

const strategyFindings = [
  {
    id: 1,
    reviewId: 101,
    projectName: 'payment-core',
    reviewMode: 'AGENT',
    modelsConfig: JSON.stringify({ strategyId: 'quality-gate' }),
    severity: 'BLOCKER',
    category: 'SECURITY',
    title: 'Token reuse risk',
    humanStatus: 'CONFIRMED',
    confidence: 0.93,
    isCrossHit: true,
  },
  {
    id: 2,
    reviewId: 101,
    projectName: 'payment-core',
    reviewMode: 'AGENT',
    modelsConfig: JSON.stringify({ strategyId: 'quality-gate' }),
    severity: 'MAJOR',
    category: 'PERFORMANCE',
    title: 'N+1 query',
    humanStatus: 'PENDING',
    confidence: 0.8,
    isCrossHit: false,
  },
  {
    id: 3,
    reviewId: 102,
    projectName: 'web-console',
    reviewMode: 'SINGLE',
    modelsConfig: JSON.stringify({ strategyId: 'fast-scan' }),
    severity: 'MINOR',
    category: 'CODE_STYLE',
    title: 'Naming issue',
    humanStatus: 'DISMISSED',
    confidence: 0.42,
    isCrossHit: false,
  },
] as const

test('extract strategy id from models config with fallback', () => {
  assert.equal(extractStrategyId(JSON.stringify({ strategyId: 'quality-gate' })), 'quality-gate')
  assert.equal(extractStrategyId('{}'), 'unknown-strategy')
  assert.equal(extractStrategyId('{bad json'), 'unknown-strategy')
  assert.equal(extractStrategyId(''), 'unknown-strategy')
})

test('strategy effects summarize risk interception and human feedback', () => {
  const summaries = summarizeStrategyEffects(strategyFindings)
  const qualityGate = summaries.find(item => item.strategyId === 'quality-gate')

  assert.ok(qualityGate)
  assert.equal(qualityGate.findingCount, 2)
  assert.equal(qualityGate.blockerCount, 1)
  assert.equal(qualityGate.majorCount, 1)
  assert.equal(qualityGate.confirmationRate, 50)
  assert.equal(qualityGate.falsePositiveRate, 0)
  assert.equal(qualityGate.crossHitRate, 50)
  assert.ok(qualityGate.effectivenessScore > 60)
})

test('strategy effects rank lower when dismissed findings dominate', () => {
  const summaries = summarizeStrategyEffects(strategyFindings)
  const fastScan = summaries.find(item => item.strategyId === 'fast-scan')

  assert.ok(fastScan)
  assert.equal(fastScan.falsePositiveRate, 100)
  assert.ok(fastScan.effectivenessScore < summaries[0].effectivenessScore)
})
