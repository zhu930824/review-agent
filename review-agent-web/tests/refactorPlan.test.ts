import { describe, it } from 'node:test'
import assert from 'node:assert'
import { sortStepsByPriority, getBreakingSteps, getSafeSteps, estimateTotalEffort } from '../src/utils/refactorPlan'
import type { RefactorPlan } from '../src/types/refactor'

const plan: RefactorPlan = {
  planId: 'p1',
  title: '测试',
  summary: '测试摘要',
  totalSteps: 4,
  estimatedImpact: 20,
  steps: [
    { stepName: 's1', targetFile: 'A.java', description: '', approach: '', priority: 'P2', breaking: false, estimatedEffort: '1d' },
    { stepName: 's2', targetFile: 'B.java', description: '', approach: '', priority: 'P0', breaking: true, estimatedEffort: '3d' },
    { stepName: 's3', targetFile: 'C.java', description: '', approach: '', priority: 'P1', breaking: false, estimatedEffort: '2d' },
    { stepName: 's4', targetFile: 'D.java', description: '', approach: '', priority: 'P0', breaking: true, estimatedEffort: '4d' },
  ],
  prerequisites: [],
  risks: [],
}

describe('refactorPlan utils', () => {
  it('sortStepsByPriority orders P0 first', () => {
    const sorted = sortStepsByPriority(plan.steps)
    assert.strictEqual(sorted[0].priority, 'P0')
    assert.strictEqual(sorted[1].priority, 'P0')
  })

  it('getBreakingSteps returns only breaking steps', () => {
    const breaking = getBreakingSteps(plan)
    assert.strictEqual(breaking.length, 2)
    assert.ok(breaking.every(s => s.breaking))
  })

  it('getSafeSteps returns only non-breaking steps', () => {
    const safe = getSafeSteps(plan)
    assert.strictEqual(safe.length, 2)
    assert.ok(safe.every(s => !s.breaking))
  })

  it('estimateTotalEffort calculates correctly', () => {
    assert.strictEqual(estimateTotalEffort(plan), '11 人天')
  })

  it('estimateTotalEffort handles empty plan', () => {
    assert.strictEqual(estimateTotalEffort({ ...plan, steps: [] }), '< 1 人天')
  })
})

