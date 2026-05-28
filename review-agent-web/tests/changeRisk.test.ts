import { describe, it } from 'node:test'
import assert from 'node:assert'
import { getRiskColor, getRiskLabel, computeRiskTrend } from '../src/utils/changeRisk'
import type { RiskAssessment } from '../src/types/risk'

const criticalRisk: RiskAssessment = { level: 'CRITICAL', score: 80, factors: [], fileLevelRisks: [], recommendation: '' }
const highRisk: RiskAssessment = { level: 'HIGH', score: 45, factors: [], fileLevelRisks: [], recommendation: '' }
const mediumRisk: RiskAssessment = { level: 'MEDIUM', score: 20, factors: [], fileLevelRisks: [], recommendation: '' }
const lowRisk: RiskAssessment = { level: 'LOW', score: 5, factors: [], fileLevelRisks: [], recommendation: '' }

describe('changeRisk', () => {
  it('getRiskColor maps CRITICAL to red', () => assert.strictEqual(getRiskColor('CRITICAL'), 'red'))
  it('getRiskColor maps HIGH to orange', () => assert.strictEqual(getRiskColor('HIGH'), 'orange'))
  it('getRiskColor maps MEDIUM to yellow', () => assert.strictEqual(getRiskColor('MEDIUM'), 'yellow'))
  it('getRiskColor maps LOW to green', () => assert.strictEqual(getRiskColor('LOW'), 'green'))

  it('getRiskLabel maps to Chinese labels', () => {
    assert.strictEqual(getRiskLabel('CRITICAL'), '严重')
    assert.strictEqual(getRiskLabel('HIGH'), '高')
    assert.strictEqual(getRiskLabel('LOW'), '低')
  })

  it('computeRiskTrend returns stable for single assessment', () => {
    assert.strictEqual(computeRiskTrend([highRisk]), 'stable')
  })

  it('computeRiskTrend detects improving trend', () => {
    assert.strictEqual(computeRiskTrend([highRisk, mediumRisk]), 'improving')
  })

  it('computeRiskTrend detects worsening trend', () => {
    assert.strictEqual(computeRiskTrend([lowRisk, highRisk]), 'worsening')
  })
})

