import assert from 'node:assert/strict'
import test from 'node:test'
import { resolveAuthRedirect } from '../src/router/authGuard'

test('auth guard redirects unauthenticated business routes to login', () => {
  assert.equal(resolveAuthRedirect({}, null), '/login')
})

test('auth guard allows authenticated business routes', () => {
  assert.equal(resolveAuthRedirect({}, 'token-1'), true)
})

test('auth guard allows unauthenticated guest routes', () => {
  assert.equal(resolveAuthRedirect({ meta: { guest: true } }, null), true)
})

test('auth guard redirects authenticated users away from guest routes', () => {
  assert.equal(resolveAuthRedirect({ meta: { guest: true } }, 'token-1'), '/')
})
