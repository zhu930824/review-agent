import assert from 'node:assert/strict'
import test from 'node:test'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import {
  AUTH_TOKEN_KEY,
  AUTH_USER_KEY,
  clearStoredAuth,
  getStoredAuthToken,
  setStoredAuth,
} from '../src/utils/authStorage'

class MemoryStorage {
  private values = new Map<string, string>()

  getItem(key: string): string | null {
    return this.values.get(key) ?? null
  }

  setItem(key: string, value: string): void {
    this.values.set(key, value)
  }

  removeItem(key: string): void {
    this.values.delete(key)
  }
}

test('auth storage uses the shared review-agent token key', () => {
  const storage = new MemoryStorage()

  setStoredAuth(storage, { token: 'token-1', user: { username: 'alice' } })

  assert.equal(AUTH_TOKEN_KEY, 'review-agent-token')
  assert.equal(AUTH_USER_KEY, 'review-agent-user')
  assert.equal(storage.getItem('review-agent-token'), 'token-1')
  assert.equal(getStoredAuthToken(storage), 'token-1')
})

test('clearStoredAuth removes token and user consistently', () => {
  const storage = new MemoryStorage()

  setStoredAuth(storage, { token: 'token-1', user: { username: 'alice' } })
  clearStoredAuth(storage)

  assert.equal(storage.getItem('review-agent-token'), null)
  assert.equal(storage.getItem('review-agent-user'), null)
  assert.equal(getStoredAuthToken(storage), null)
})

test('layout logout actions use the shared auth composable logout', () => {
  const defaultLayout = readFileSync(join(process.cwd(), 'src/views/layouts/DefaultLayout.vue'), 'utf8')
  const appHeader = readFileSync(join(process.cwd(), 'src/components/layout/AppHeader.vue'), 'utf8')

  for (const source of [defaultLayout, appHeader]) {
    assert.match(source, /logout:\s*logoutAuth/)
    assert.match(source, /await logoutAuth\(\)/)
    assert.doesNotMatch(source, /localStorage\.removeItem\('review-agent-token'\)/)
    assert.doesNotMatch(source, /clearAuth\(\)/)
  }
})
