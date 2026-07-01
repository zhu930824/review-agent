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

test('review detail can upload sarif to github code scanning', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /uploadSarifToCodeScanning/)
  assert.match(detailView, /sarifUploadLoading/)
  assert.match(detailView, /post<unknown>\('\/integration\/sarif\/upload'/)
  assert.match(detailView, /commitSha:\s*resolveIntegrationCommitSha\(\)/)
  assert.match(detailView, /ref:\s*resolveIntegrationRef\(\)/)
})

test('review detail can post summary back to a pull request', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /openPrSummaryModal/)
  assert.match(detailView, /submitPrSummaryComment/)
  assert.match(detailView, /prSummaryCommentLoading/)
  assert.match(detailView, /post<unknown>\('\/integration\/pr-summary\/comment'/)
  assert.match(detailView, /pullNumber:\s*Number\(prSummaryPullNumber\.value\)/)
  assert.match(detailView, /body:\s*buildPrSummaryBody\(\)/)
})
