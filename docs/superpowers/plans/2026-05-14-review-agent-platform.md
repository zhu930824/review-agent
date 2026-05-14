# Review Agent Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn review-agent into a business-facing AI Coding governance platform with multi-model strategy configuration, Pre-PR quality gates, and useful review dashboards.

**Architecture:** Implement the first slice with frontend-first strategy models and pure TypeScript helpers that compile selected strategies into the existing backend `modelsConfig` JSON. Keep backend changes additive and safe because existing Java source files appear protected in this workspace.

**Tech Stack:** Nuxt 3, Vue 3, TypeScript, Nuxt UI, Spring Boot 3, MySQL/Flyway.

---

### Task 1: Model Strategy Core

**Files:**
- Create: `review-agent-web/types/model-config.ts`
- Create: `review-agent-web/utils/modelStrategies.ts`
- Create: `review-agent-web/tests/modelStrategies.test.mjs`
- Modify: `review-agent-web/package.json`

- [ ] **Step 1: Write the failing tests**

Create tests that import `utils/modelStrategies.ts` and verify:

- built-in providers include DashScope and OpenAI-compatible providers.
- `compileStrategyConfig('quality-gate')` returns AGENT-compatible config with security, performance, style, exception, and architect roles.
- `compileStrategyConfig('cross-check')` returns MULTI-compatible config with at least two workers.
- `deriveGatePolicy()` blocks on BLOCKER and requires human review for MAJOR.

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
cd review-agent-web
npm test
```

Expected: fails because test script and helper files do not exist.

- [ ] **Step 3: Implement model types and helpers**

Add typed provider/profile/strategy definitions, built-in seed data, and pure helper functions.

- [ ] **Step 4: Run tests to verify they pass**

Run:

```bash
cd review-agent-web
npm test
```

Expected: PASS.

### Task 2: Guided Review Creation

**Files:**
- Modify: `review-agent-web/pages/reviews/create.vue`
- Modify: `review-agent-web/types/review.ts`

- [ ] **Step 1: Add tests or type checks for compiled payload shape**

Extend `modelStrategies.test.mjs` to assert that every built-in strategy can produce a `modelsConfig` payload accepted by existing `CreateReviewParams`.

- [ ] **Step 2: Run tests to verify failure before UI integration**

Run:

```bash
cd review-agent-web
npm test
```

- [ ] **Step 3: Replace raw JSON-first flow**

Add strategy cards, model profile previews, and an advanced JSON fallback. On submit, compile selected strategy into `modelsConfig`.

- [ ] **Step 4: Verify tests and Nuxt build**

Run:

```bash
cd review-agent-web
npm test
npm run build
```

### Task 3: Model Configuration Center

**Files:**
- Create: `review-agent-web/pages/settings/models.vue`
- Modify: `review-agent-web/components/layout/AppSidebar.vue`
- Modify: `review-agent-web/utils/modelStrategies.ts`

- [ ] **Step 1: Add tests for model summary helpers**

Verify provider/profile summaries, enabled model filtering, and strategy role labels.

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
cd review-agent-web
npm test
```

- [ ] **Step 3: Build `/settings/models`**

Add a dense operations-style page showing model providers, model profiles, review strategies, role bindings, and gate policies.

- [ ] **Step 4: Add navigation link**

Add "模型配置" to the sidebar.

- [ ] **Step 5: Verify**

Run:

```bash
cd review-agent-web
npm test
npm run build
```

### Task 4: Pre-PR Detail And Dashboard

**Files:**
- Modify: `review-agent-web/pages/index.vue`
- Modify: `review-agent-web/pages/reviews/[id].vue`
- Create: `review-agent-web/utils/reviewMetrics.ts`
- Create: `review-agent-web/tests/reviewMetrics.test.mjs`

- [ ] **Step 1: Write tests for review metrics**

Test derived metrics for total reviews, completed reviews, pending human confirmations, blockers, Pre-PR blocked count, and model success rate.

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
cd review-agent-web
npm test
```

- [ ] **Step 3: Implement review metric helpers**

Add pure metric functions over review list/detail-like objects.

- [ ] **Step 4: Upgrade dashboard and review detail text**

Replace mojibake text, show business readiness metrics, blocked reasons, PR summary panel, and clearer model execution status.

- [ ] **Step 5: Verify**

Run:

```bash
cd review-agent-web
npm test
npm run build
```

### Task 5: Backend Safe Migration Assets

**Files:**
- Create: `review-agent-server/src/main/resources/db/migration/V2__model_strategy_governance.sql`
- Create: `doc/平台架构治理SOP.md`

- [ ] **Step 1: Verify backend source access**

Run:

```bash
cd review-agent-server
mvn -q -DskipTests compile
```

Expected: either PASS or a clear source-readability/build error.

- [ ] **Step 2: Add migration SQL**

Add additive tables for providers, profiles, strategies, strategy model bindings, and Pre-PR gates. Do not alter existing tables destructively.

- [ ] **Step 3: Add governance SOP**

Document current constraints, target layering, Pre-PR operating process, and backend migration path.

- [ ] **Step 4: Verify backend compile if source is accessible**

Run:

```bash
cd review-agent-server
mvn -q -DskipTests compile
```

Expected: PASS if protected source is actually compilable; otherwise document blocker.

## Self-Review

- Spec coverage: all four tracks from the design are represented.
- Placeholder scan: no TBD/TODO implementation placeholders are left.
- Type consistency: frontend strategy config compiles to existing `review.modelsConfig`, preserving backend compatibility.
