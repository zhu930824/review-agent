# Review Agent Evolution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stabilize the current Review Agent platform, document the evolution path, and execute the first low-risk productization fixes.

**Architecture:** Keep the existing Vite + Vue 3 + Ant Design Vue frontend and Spring Boot backend. Avoid deep backend refactors until protected Java source files are confirmed maintainable. Start with documentation, authentication storage consistency, and verification.

**Tech Stack:** Vue 3, Vue Router, TypeScript, Vite, Ant Design Vue, Node test runner via `tsx --test`, Spring Boot 3.3.5, Java 21, Maven, MyBatis Plus, Flyway.

---

## File Structure

- Create: `docs/superpowers/specs/2026-06-03-review-agent-evolution-design.md`
  - Stores the current-state assessment, gaps, and staged evolution strategy.
- Create: `docs/superpowers/plans/2026-06-03-review-agent-evolution.md`
  - Stores the executable implementation plan.
- Create: `review-agent-web/src/utils/authStorage.ts`
  - Centralizes auth storage keys and localStorage operations.
- Create: `review-agent-web/tests/authStorage.test.ts`
  - Verifies auth token storage behavior before production code changes.
- Modify: `review-agent-web/src/composables/useAuth.ts`
  - Uses shared auth storage helpers instead of local hard-coded keys.
- Modify: `review-agent-web/src/composables/useApi.ts`
  - Clears the shared auth token on 401.
- Modify: `review-agent-web/src/router/index.ts`
  - Uses the same auth token key as `useAuth`.
- Modify: `README.md`
  - Corrects the documented frontend stack and local commands.
- Create: `docs/api-catalog.md`
  - Documents confirmed backend APIs and frontend API usage.
- Create: `docs/development-environment.md`
  - Documents Node runtime requirements, the temporary Codex Node workaround, and the configured frontend port.
- Create: `review-agent-web/src/utils/apiConfig.ts`
  - Centralizes frontend API base URL resolution.
- Create: `review-agent-web/tests/apiConfig.test.ts`
  - Verifies default and environment-overridden API base URL behavior.
- Create: `review-agent-web/tests/viteConfig.test.ts`
  - Verifies Vite manual chunk configuration remains in place.
- Create: `review-agent-web/tests/frameworkConsistency.test.ts`
  - Verifies project cards use Vue Router instead of Nuxt components.
- Modify: `review-agent-web/vite.config.ts`
  - Splits Vue, Ant Design Vue, and review utilities into explicit chunks.
- Modify: `review-agent-web/src/components/project/ProjectCard.vue`
  - Replaces the stale `NuxtLink` component with `RouterLink`.
- Modify: `review-agent-web/src/views/reviews/detail.vue`
  - Uses the shared API base helper for SSE progress connections.

## Task 1: Documentation Baseline

**Files:**
- Create: `docs/superpowers/specs/2026-06-03-review-agent-evolution-design.md`
- Create: `docs/superpowers/plans/2026-06-03-review-agent-evolution.md`

- [ ] **Step 1: Write the evolution design document**

Add a document covering:

- Current implemented capabilities.
- Main product and engineering gaps.
- Four-stage evolution route.
- Recommended priority path.

- [ ] **Step 2: Write this implementation plan**

Add the executable plan with exact files, test commands, and verification commands.

- [ ] **Step 3: Inspect the new documents**

Run:

```powershell
Get-Content docs\superpowers\specs\2026-06-03-review-agent-evolution-design.md
Get-Content docs\superpowers\plans\2026-06-03-review-agent-evolution.md
```

Expected: Both files exist and contain no `TBD` or `TODO` placeholders.

## Task 2: Auth Storage Consistency

**Files:**
- Create: `review-agent-web/src/utils/authStorage.ts`
- Create: `review-agent-web/tests/authStorage.test.ts`
- Modify: `review-agent-web/src/composables/useAuth.ts`
- Modify: `review-agent-web/src/composables/useApi.ts`
- Modify: `review-agent-web/src/router/index.ts`

- [ ] **Step 1: Write the failing auth storage test**

Create `review-agent-web/tests/authStorage.test.ts`:

```ts
import assert from 'node:assert/strict'
import test from 'node:test'
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
npm test -- tests/authStorage.test.ts
```

Expected: FAIL because `../src/utils/authStorage` does not exist yet.

- [ ] **Step 3: Implement auth storage helpers**

Create `review-agent-web/src/utils/authStorage.ts`:

```ts
export const AUTH_TOKEN_KEY = 'review-agent-token'
export const AUTH_USER_KEY = 'review-agent-user'

type StorageLike = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>

export interface StoredAuth {
  token: string
  user: unknown
}

export function getStoredAuthToken(storage: StorageLike = localStorage): string | null {
  return storage.getItem(AUTH_TOKEN_KEY)
}

export function getStoredAuthUser<T = unknown>(storage: StorageLike = localStorage): T | null {
  const storedUser = storage.getItem(AUTH_USER_KEY)
  if (!storedUser) return null

  try {
    return JSON.parse(storedUser) as T
  } catch {
    return null
  }
}

export function setStoredAuth(storage: StorageLike = localStorage, auth: StoredAuth): void {
  storage.setItem(AUTH_TOKEN_KEY, auth.token)
  storage.setItem(AUTH_USER_KEY, JSON.stringify(auth.user))
}

export function clearStoredAuth(storage: StorageLike = localStorage): void {
  storage.removeItem(AUTH_TOKEN_KEY)
  storage.removeItem(AUTH_USER_KEY)
}
```

- [ ] **Step 4: Run auth storage test to verify it passes**

Run:

```powershell
npm test -- tests/authStorage.test.ts
```

Expected: PASS for the two auth storage tests.

- [ ] **Step 5: Wire shared storage into production auth code**

Update:

- `useAuth.ts`: import `clearStoredAuth`, `getStoredAuthToken`, `getStoredAuthUser`, `setStoredAuth`.
- `useApi.ts`: import `clearStoredAuth` and call it on 401.
- `router/index.ts`: import `getStoredAuthToken` and use it in the guard.

- [ ] **Step 6: Run frontend tests**

Run:

```powershell
npm test
```

Expected: all tests pass.

## Task 3: README Reality Check

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Replace outdated stack and setup notes**

Update README to describe:

- Backend: Java 21, Spring Boot 3.3.5, MyBatis Plus, MySQL, Flyway, JGit, JWT.
- Frontend: Vite, Vue 3, TypeScript, Ant Design Vue, Vue Router.
- API base: frontend development uses `/api`, typically proxied by Vite.
- Verification commands: backend compile, frontend tests, frontend build.

- [ ] **Step 2: Run README placeholder scan**

Run:

```powershell
Select-String -Path README.md -Pattern 'Nuxt|Nuxt UI|TBD|TODO'
```

Expected: no matches for outdated Nuxt references or placeholders.

## Task 4: Verification

**Files:**
- No new files.

- [ ] **Step 1: Run frontend tests**

Run:

```powershell
npm test
```

Expected: all tests pass.

- [ ] **Step 2: Run frontend production build**

Run:

```powershell
npm run build
```

Expected: Vite build exits with code 0.

- [ ] **Step 3: Run backend compile**

Run:

```powershell
mvn -q -DskipTests compile
```

Expected: Maven compile exits with code 0.

- [ ] **Step 4: Inspect git diff**

Run:

```powershell
git status --short
git diff -- README.md docs/superpowers/specs/2026-06-03-review-agent-evolution-design.md docs/superpowers/plans/2026-06-03-review-agent-evolution.md review-agent-web/src/utils/authStorage.ts review-agent-web/tests/authStorage.test.ts review-agent-web/src/composables/useAuth.ts review-agent-web/src/composables/useApi.ts review-agent-web/src/router/index.ts
```

Expected: diff only includes files listed in this plan plus pre-existing user changes outside this task.

## Task 5: API Catalog

**Files:**
- Create: `docs/api-catalog.md`

- [x] **Step 1: Inspect readable controllers and frontend API calls**

Run:

```powershell
rg "@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@PatchMapping|@DeleteMapping" review-agent-server/src/main/java/com/review/agent/controller
rg "get<|post<|put<|patch<|del<|/api/|/reviews|/projects|/model-config|/governance|/operations|/auth" review-agent-web/src -n
```

Expected: API paths are collected from readable backend controllers and frontend usage sites.

- [x] **Step 2: Document confirmed APIs**

Create `docs/api-catalog.md` with sections for:

- Auth
- Projects
- Reviews
- Model Config
- Governance
- Operations
- Knowledge
- AI Gateway
- Future Pre-PR Gate API priorities

## Task 6: Vite Chunk Splitting

**Files:**
- Create: `review-agent-web/tests/viteConfig.test.ts`
- Modify: `review-agent-web/vite.config.ts`

- [x] **Step 1: Write the failing config test**

Create a test that reads `vite.config.ts` and expects `manualChunks`, `ant-design-vue`, `@ant-design/icons-vue`, and `vue-router`.

- [x] **Step 2: Verify the test fails**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\viteConfig.test.ts
```

Expected: FAIL before implementation because `manualChunks` is missing.

- [x] **Step 3: Add manual chunks**

Update `vite.config.ts` to split:

- `vue-vendor`
- `ant-design-vue`
- `review-utils`

- [x] **Step 4: Verify tests and build**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\*.test.ts
& "$env:TEMP\codex-node.exe" node_modules\vite\bin\vite.js build
```

Expected: all frontend tests pass and build exits with code 0. The remaining large chunk warning is isolated to Ant Design Vue vendor size.

## Task 7: Framework Consistency

**Files:**
- Create: `review-agent-web/tests/frameworkConsistency.test.ts`
- Modify: `review-agent-web/src/components/project/ProjectCard.vue`

- [x] **Step 1: Write the failing framework consistency test**

Create a test that reads `ProjectCard.vue`, rejects `NuxtLink`, and expects `RouterLink`.

- [x] **Step 2: Verify the test fails**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\frameworkConsistency.test.ts
```

Expected: FAIL before implementation because `ProjectCard.vue` still uses `NuxtLink`.

- [x] **Step 3: Replace `NuxtLink` with `RouterLink`**

Update the opening and closing link tags in `ProjectCard.vue`.

- [x] **Step 4: Verify tests and build**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\*.test.ts
& "$env:TEMP\codex-node.exe" node_modules\vite\bin\vite.js build
```

Expected: all frontend tests pass and build exits with code 0.

## Task 8: API Base Configuration

**Files:**
- Create: `review-agent-web/src/utils/apiConfig.ts`
- Create: `review-agent-web/tests/apiConfig.test.ts`
- Modify: `review-agent-web/src/composables/useApi.ts`
- Modify: `review-agent-web/src/views/reviews/detail.vue`

- [x] **Step 1: Write the failing API config tests**

Create tests that verify:

- Default API base URL is `/api`.
- `VITE_API_BASE_URL` overrides the default.
- Blank overrides fall back to `/api`.
- Review progress SSE uses the shared helper.

- [x] **Step 2: Verify the tests fail**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\apiConfig.test.ts
```

Expected: FAIL before implementation because `apiConfig` does not exist, then FAIL again until the SSE detail page uses the helper.

- [x] **Step 3: Implement shared API base helper**

Create `src/utils/apiConfig.ts` with `getApiBaseUrl`.

- [x] **Step 4: Wire normal API requests and SSE progress to the helper**

Update:

- `src/composables/useApi.ts`
- `src/views/reviews/detail.vue`

- [x] **Step 5: Verify tests and build**

Run:

```powershell
& "$env:TEMP\codex-node.exe" node_modules\tsx\dist\cli.mjs --test tests\*.test.ts
& "$env:TEMP\codex-node.exe" node_modules\vite\bin\vite.js build
```

Expected: all frontend tests pass and build exits with code 0.

## Execution Notes

- Local `D:\develop\node\node.exe` began failing with `Could not determine Node.js install directory`.
- A working Node runtime was recovered by copying Codex bundled `node.exe` to `%TEMP%\codex-node.exe`.
- Frontend verification commands in this plan use the temporary Node path until the local Node installation is repaired.
- `review-agent-web/vite.config.ts` configures the frontend dev server on port `3000`.

## Self-Review

- Spec coverage: The plan covers documentation, token key consistency, README correction, and verification. It intentionally defers full mojibake repair, CI status writeback, SARIF export, strategy telemetry, and Agent repair workflow to later stages.
- Placeholder scan: This plan contains no `TBD` or `TODO` placeholders.
- Type consistency: `AUTH_TOKEN_KEY`, `AUTH_USER_KEY`, `getStoredAuthToken`, `getStoredAuthUser`, `setStoredAuth`, and `clearStoredAuth` are consistently named across tests and planned production usage.
