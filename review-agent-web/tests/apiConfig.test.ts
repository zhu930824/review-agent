import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'
import { getApiBaseUrl } from '../src/utils/apiConfig'

test('api base url defaults to /api', () => {
  assert.equal(getApiBaseUrl({}), '/api')
})

test('api base url can be overridden by VITE_API_BASE_URL', () => {
  assert.equal(getApiBaseUrl({ VITE_API_BASE_URL: 'http://localhost:8080/api' }), 'http://localhost:8080/api')
})

test('api base url ignores blank overrides', () => {
  assert.equal(getApiBaseUrl({ VITE_API_BASE_URL: '   ' }), '/api')
})

test('review progress sse uses shared api base helper', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/reviews/detail.vue'), 'utf8')

  assert.match(detailView, /getApiBaseUrl/)
  assert.doesNotMatch(detailView, /VITE_API_BASE_URL\s*\|\|/)
})

test('auth requests use shared api base helper', () => {
  const authComposable = readFileSync(join(process.cwd(), 'src/composables/useAuth.ts'), 'utf8')

  assert.match(authComposable, /getApiBaseUrl/)
  assert.doesNotMatch(authComposable, /const API_BASE = ['"]\/api['"]/)
})
