import { describe, it } from 'node:test'
import assert from 'node:assert'
import { groupByRiskLevel, groupByTestLevel, uniqueCategories } from '../src/utils/testGeneration'
import type { TestCase } from '../src/types/testgen'

const sampleCases: TestCase[] = [
  { testName: 'tc1', targetMethod: 'foo', testLevel: 'UNIT', riskLevel: 'HIGH', inputs: [], expectedOutput: '', preconditions: '', testCategory: '核心路径' },
  { testName: 'tc2', targetMethod: 'foo', testLevel: 'UNIT', riskLevel: 'HIGH', inputs: [], expectedOutput: '', preconditions: '', testCategory: '异常分支' },
  { testName: 'tc3', targetMethod: 'bar', testLevel: 'INTEGRATION', riskLevel: 'MEDIUM', inputs: [], expectedOutput: '', preconditions: '', testCategory: '核心路径' },
  { testName: 'tc4', targetMethod: 'baz', testLevel: 'INTEGRATION', riskLevel: 'LOW', inputs: [], expectedOutput: '', preconditions: '', testCategory: '正常流程' },
]

describe('testGeneration utils', () => {
  it('groupByRiskLevel separates by risk', () => {
    const groups = groupByRiskLevel(sampleCases)
    assert.strictEqual(groups.HIGH.length, 2)
    assert.strictEqual(groups.MEDIUM.length, 1)
    assert.strictEqual(groups.LOW.length, 1)
  })

  it('groupByTestLevel separates by test level', () => {
    const groups = groupByTestLevel(sampleCases)
    assert.strictEqual(groups.UNIT.length, 2)
    assert.strictEqual(groups.INTEGRATION.length, 2)
  })

  it('uniqueCategories returns distinct categories', () => {
    const cats = uniqueCategories(sampleCases)
    assert.ok(cats.includes('核心路径'))
    assert.ok(cats.includes('异常分支'))
    assert.strictEqual(cats.length, 3)
  })

  it('handles empty cases', () => {
    assert.strictEqual(groupByRiskLevel([]).HIGH.length, 0)
    assert.strictEqual(uniqueCategories([]).length, 0)
  })
})

