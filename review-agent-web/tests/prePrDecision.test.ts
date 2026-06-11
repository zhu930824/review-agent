import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('review detail can submit a manual pre-pr gate decision', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /submitPrePrDecision/)
  assert.match(detailView, /\/reviews\/\$\{reviewId\.value\}\/pre-pr-decision/)
  assert.match(detailView, /gateStatus:\s*'PASSED'/)
  assert.match(detailView, /reason:\s*'人工确认风险可接受，允许进入后续流程'/)
  assert.match(detailView, /decidedBy:\s*'manual-reviewer'/)
})

test('review detail uses persisted backend gate as primary source', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /get<PrePrGate>\(`\/reviews\/\$\{reviewId\.value\}\/gate`\)/)
  assert.match(detailView, /post<PrePrGate>\(`\/reviews\/\$\{reviewId\.value\}\/gate\/refresh`\)/)
  assert.match(detailView, /patch<PrePrGate>/)
  assert.match(detailView, /applyGate\(res\.data\)/)
  assert.match(detailView, /prePrStatus:\s*gate\.gateStatus/)
  assert.match(detailView, /blockedReasons:\s*gate\.blockedReasons/)
  assert.match(detailView, /prePrDecisionLoading/)
})

test('review detail can manually republish persisted gate to ci status', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /republishCiStatus/)
  assert.match(detailView, /ciRepublishLoading/)
  assert.match(detailView, /重发 CI/)
  assert.match(detailView, /post<unknown>\(`\/reviews\/\$\{reviewId\.value\}\/gate\/publish-ci`\)/)
})

test('pre-pr creation initializes persisted gate before routing to detail', () => {
  const createView = readFileSync(join(process.cwd(), 'src/views/reviews/create.vue'), 'utf8')

  assert.match(createView, /initializePrePrGate/)
  assert.match(createView, /post<PrePrGate>\(`\/reviews\/\$\{reviewId\}\/gate\/initialize`\)/)
  assert.match(createView, /await initializePrePrGate\(res\.data\.review\.id\)/)
})
