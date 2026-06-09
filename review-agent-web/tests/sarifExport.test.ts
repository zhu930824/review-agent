import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('review detail exposes sarif export endpoint', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /\/reviews\/\$\{reviewId\.value\}\/sarif/)
  assert.match(detailView, /downloadSarif/)
})

test('sarif export creates a downloadable sarif file', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /new Blob/)
  assert.match(detailView, /application\/sarif\+json/)
  assert.match(detailView, /\.sarif/)
})
