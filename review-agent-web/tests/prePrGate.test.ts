import assert from 'node:assert/strict'
import test from 'node:test'
import {
  buildPrePrDecisionPayload,
  buildSarifFilename,
  getPrePrGateLabel,
  getPrePrGateTone,
  toStatusCheckState,
} from '../src/utils/prePrGate'

test('pre-pr gate labels and tones cover all backend states', () => {
  assert.equal(getPrePrGateLabel('PASSED'), '已通过')
  assert.equal(getPrePrGateLabel('BLOCKED'), '已阻断')
  assert.equal(getPrePrGateLabel('NEEDS_HUMAN_REVIEW'), '待人工确认')
  assert.equal(getPrePrGateLabel('RUNNING'), '执行中')

  assert.equal(getPrePrGateTone('PASSED'), 'success')
  assert.equal(getPrePrGateTone('BLOCKED'), 'error')
  assert.equal(getPrePrGateTone('NEEDS_HUMAN_REVIEW'), 'warning')
  assert.equal(getPrePrGateTone('RUNNING'), 'processing')
})

test('pre-pr decisions trim comments and preserve explicit decision', () => {
  assert.deepEqual(buildPrePrDecisionPayload('PASSED', '  LGTM  '), {
    decision: 'PASSED',
    comment: 'LGTM',
  })
})

test('sarif filename is stable for review downloads', () => {
  assert.equal(buildSarifFilename(42), 'review-42.sarif.json')
})

test('status check state maps gate state to provider-neutral state', () => {
  assert.equal(toStatusCheckState('PASSED'), 'success')
  assert.equal(toStatusCheckState('BLOCKED'), 'failure')
  assert.equal(toStatusCheckState('NEEDS_HUMAN_REVIEW'), 'pending')
  assert.equal(toStatusCheckState('RUNNING'), 'pending')
})
