import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('review detail can submit a manual pre-pr gate decision', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /submitPrePrDecision/)
  assert.match(detailView, /\/reviews\/\$\{reviewId\.value\}\/pre-pr-decision/)
  assert.match(detailView, /decision:\s*'APPROVE_WITH_RISK'/)
})

test('manual pre-pr decision refreshes detail from backend response', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /patch<ReviewDetail>/)
  assert.match(detailView, /detail\.value\s*=\s*res\.data/)
  assert.match(detailView, /prePrDecisionLoading/)
})
