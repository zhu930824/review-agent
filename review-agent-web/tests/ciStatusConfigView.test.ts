import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('governance page can load and save ci status config', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /ciConfigForm/)
  assert.match(governanceView, /loadCiStatusConfig/)
  assert.match(governanceView, /saveCiStatusConfig/)
  assert.match(governanceView, /get<CiStatusConfigVO>\('\/integration\/ci-config'\)/)
  assert.match(governanceView, /put<CiStatusConfigVO>\('\/integration\/ci-config'/)
})

test('governance page exposes repository binding fields', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /repoOwner/)
  assert.match(governanceView, /repoName/)
  assert.match(governanceView, /statusContext/)
  assert.match(governanceView, /sarifUploadEnabled/)
  assert.match(governanceView, /tokenConfigured/)
})
