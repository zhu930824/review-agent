import { describe, it } from 'node:test'
import assert from 'node:assert'
import { getCurrentStep, getCompletedCount, getStepProgress, isReleaseBlocked } from '../src/utils/releaseWorkflow'

const mockSteps = [
  { stepKey: 'gate', name: '门禁', type: 'GATE', responsible: 'SYSTEM', order: 1, status: 'COMPLETED', dependsOn: '' },
  { stepKey: 'review', name: '审查', type: 'MANUAL', responsible: 'HUMAN', order: 2, status: 'REQUIRED', dependsOn: 'gate' },
  { stepKey: 'merge', name: '合并', type: 'GATE', responsible: 'SYSTEM', order: 3, status: 'PENDING', dependsOn: 'review' },
]

describe('releaseWorkflow', () => {
  it('getCurrentStep returns first non-completed step', () => {
    const step = getCurrentStep(mockSteps)
    assert.ok(step)
    assert.strictEqual(step.stepKey, 'review')
  })

  it('getCompletedCount counts completed steps', () => {
    assert.strictEqual(getCompletedCount(mockSteps), 1)
  })

  it('getStepProgress returns percentage', () => {
    assert.strictEqual(getStepProgress(mockSteps), 33)
  })

  it('getStepProgress returns 0 for empty steps', () => {
    assert.strictEqual(getStepProgress([]), 0)
  })

  it('getStepProgress returns 100 when all completed', () => {
    const allDone = mockSteps.map(s => ({ ...s, status: 'COMPLETED' }))
    assert.strictEqual(getStepProgress(allDone), 100)
  })

  it('isReleaseBlocked detects BLOCKED gate', () => {
    const blocked = [{ ...mockSteps[0], status: 'BLOCKED' }, mockSteps[1], mockSteps[2]]
    assert.strictEqual(isReleaseBlocked(blocked), true)
  })

  it('isReleaseBlocked returns false when all pass', () => {
    const passed = [{ ...mockSteps[0], status: 'COMPLETED' }, { ...mockSteps[1], status: 'COMPLETED' }, { ...mockSteps[2], status: 'COMPLETED' }]
    assert.strictEqual(isReleaseBlocked(passed), false)
  })
})

