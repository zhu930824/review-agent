import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('pre-pr creation page calls the dedicated backend endpoint', () => {
  const createView = readFileSync(join(process.cwd(), 'src/views/reviews/create.vue'), 'utf8')

  assert.match(createView, /post<ReviewDetailResponse>\('\/reviews\/pre-pr'/)
  assert.doesNotMatch(createView, /post<Review>\('\/reviews'/)
})

test('pre-pr creation routes from backend review detail payload', () => {
  const createView = readFileSync(join(process.cwd(), 'src/views/reviews/create.vue'), 'utf8')

  assert.match(createView, /res\.data\.review\.id/)
})
