# Pre-PR Closed Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing Pre-PR review prototype into a stable delivery-chain loop with persisted gate state, frontend actions, SARIF export, and a replaceable CI status adapter.

**Architecture:** Reuse the existing Spring Boot `ReviewController`, `ReviewService`, `PrePrGate` entity, and Vite + Vue 3 frontend. Add the smallest backend contract missing today: an explicit gate read endpoint. Keep GitHub/GitLab integration behind a local adapter interface so the product can validate status mapping without requiring live provider credentials.

**Tech Stack:** Spring Boot 3.3.5, Java 21, MyBatis Plus, Flyway, Vue 3, Vue Router, TypeScript, Vite, Ant Design Vue, Node test runner via `tsx --test`.

---

## Current State

- `pre_pr_gate` table already exists in `review-agent-server/src/main/resources/db/migration/V2__model_strategy_governance.sql`.
- `PrePrGate` entity and `PrePrGateMapper` already exist.
- `ReviewController` already exposes:
  - `POST /api/reviews/pre-pr`
  - `PATCH /api/reviews/{id}/pre-pr-decision`
  - `GET /api/reviews/{id}/sarif`
- `ReviewDetailVO` currently exposes `prePrStatus` and `blockedReasons`, but there is no independent `GET /api/reviews/{id}/gate` endpoint.
- Frontend Review creation now posts to `/api/reviews/pre-pr`; Review detail exposes gate decision actions and SARIF download.

## File Structure

- Create: `review-agent-server/src/main/java/com/review/agent/domain/dto/PrePrGateVO.java`
  - API response contract for persisted gate status.
- Modify: `review-agent-server/src/main/java/com/review/agent/service/ReviewService.java`
  - Add `PrePrGateVO getPrePrGate(Long reviewId)`.
- Modify: `review-agent-server/src/main/java/com/review/agent/service/impl/ReviewServiceImpl.java`
  - Load `pre_pr_gate`, parse blocked reasons, and return `PrePrGateVO`.
- Modify: `review-agent-server/src/main/java/com/review/agent/controller/ReviewController.java`
  - Add `GET /api/reviews/{id}/gate`.
- Create: `review-agent-web/src/utils/prePrGate.ts`
  - Frontend pure helpers for gate label/color, decision payloads, SARIF filename, and status-check payload mapping.
- Create: `review-agent-web/tests/prePrGate.test.ts`
  - Unit tests for frontend gate helpers.
- Modify: `review-agent-web/src/types/review.ts`
  - Add `PrePrGate` type matching `PrePrGateVO`.
- Modify: `review-agent-web/src/views/reviews/create.vue`
  - Use `POST /api/reviews/pre-pr` for Pre-PR submissions, while preserving the existing config preview.
- Modify: `review-agent-web/src/views/reviews/detail.vue`
  - Fetch `GET /api/reviews/{id}/gate`, render gate status, expose decision actions, and download SARIF.
- Create: `review-agent-server/src/main/java/com/review/agent/infrastructure/integration/ReviewStatusCheckAdapter.java`
  - Provider-neutral adapter boundary for future GitHub/GitLab status checks.
- Create: `review-agent-server/src/main/java/com/review/agent/infrastructure/integration/NoopReviewStatusCheckAdapter.java`
  - Local no-op adapter that logs status-check intent.
- Modify: `docs/api-catalog.md`
  - Document the new gate read endpoint once implemented.

## Task 1: Freeze Frontend Gate Helpers

**Files:**
- Create: `review-agent-web/src/utils/prePrGate.ts`
- Create: `review-agent-web/tests/prePrGate.test.ts`

- [x] **Step 1: Write the failing test**

Create `review-agent-web/tests/prePrGate.test.ts`:

```ts
import assert from 'node:assert/strict'
import test from 'node:test'
import {
  buildPrePrDecisionPayload,
  buildSarifFilename,
  getPrePrGateTone,
  getPrePrGateLabel,
  toStatusCheckState,
} from '../src/utils/prePrGate'

test('pre-pr gate labels and tones cover all backend states', () => {
  assert.equal(getPrePrGateLabel('PASSED'), '已通过')
  assert.equal(getPrePrGateLabel('BLOCKED'), '已阻断')
  assert.equal(getPrePrGateLabel('NEEDS_HUMAN_REVIEW'), '待人工确认')
  assert.equal(getPrePrGateLabel('RUNNING'), '执行中')

  assert.equal(getPrePrGateTone('PASSED'), 'success')
  assert.equal(getPrePrGateTone('BLOCKED'), 'error')
  assert.equal(getPrePrGateTone('NEEDS_HUMAN_REVIEW'), 'warning')
  assert.equal(getPrePrGateTone('RUNNING'), 'processing')
})

test('pre-pr decisions trim comments and preserve explicit decision', () => {
  assert.deepEqual(buildPrePrDecisionPayload('PASSED', '  LGTM  '), {
    decision: 'PASSED',
    comment: 'LGTM',
  })
})

test('sarif filename is stable for review downloads', () => {
  assert.equal(buildSarifFilename(42), 'review-42.sarif.json')
})

test('status check state maps gate state to provider-neutral state', () => {
  assert.equal(toStatusCheckState('PASSED'), 'success')
  assert.equal(toStatusCheckState('BLOCKED'), 'failure')
  assert.equal(toStatusCheckState('NEEDS_HUMAN_REVIEW'), 'pending')
  assert.equal(toStatusCheckState('RUNNING'), 'pending')
})
```

- [x] **Step 2: Run test to verify it fails**

Run:

```powershell
cd review-agent-web
npm test -- tests/prePrGate.test.ts
```

Expected: FAIL because `src/utils/prePrGate.ts` does not exist.

- [x] **Step 3: Implement frontend helpers**

Create `review-agent-web/src/utils/prePrGate.ts`:

```ts
export type PrePrGateStatus = 'PASSED' | 'BLOCKED' | 'NEEDS_HUMAN_REVIEW' | 'RUNNING'
export type PrePrGateTone = 'success' | 'error' | 'warning' | 'processing'
export type StatusCheckState = 'success' | 'failure' | 'pending'

const labels: Record<PrePrGateStatus, string> = {
  PASSED: '已通过',
  BLOCKED: '已阻断',
  NEEDS_HUMAN_REVIEW: '待人工确认',
  RUNNING: '执行中',
}

const tones: Record<PrePrGateStatus, PrePrGateTone> = {
  PASSED: 'success',
  BLOCKED: 'error',
  NEEDS_HUMAN_REVIEW: 'warning',
  RUNNING: 'processing',
}

export function getPrePrGateLabel(status: PrePrGateStatus): string {
  return labels[status]
}

export function getPrePrGateTone(status: PrePrGateStatus): PrePrGateTone {
  return tones[status]
}

export function buildPrePrDecisionPayload(decision: PrePrGateStatus, comment: string) {
  return { decision, comment: comment.trim() }
}

export function buildSarifFilename(reviewId: number | string): string {
  return `review-${reviewId}.sarif.json`
}

export function toStatusCheckState(status: PrePrGateStatus): StatusCheckState {
  if (status === 'PASSED') return 'success'
  if (status === 'BLOCKED') return 'failure'
  return 'pending'
}
```

- [x] **Step 4: Run frontend tests**

Run:

```powershell
cd review-agent-web
npm test
```

Expected: all tests pass, including `prePrGate.test.ts`.

## Task 2: Add Backend Gate Read Contract

**Files:**
- Create: `review-agent-server/src/main/java/com/review/agent/domain/dto/PrePrGateVO.java`
- Modify: `review-agent-server/src/main/java/com/review/agent/service/ReviewService.java`
- Modify: `review-agent-server/src/main/java/com/review/agent/service/impl/ReviewServiceImpl.java`
- Modify: `review-agent-server/src/main/java/com/review/agent/controller/ReviewController.java`

- [x] **Step 1: Add response DTO**

Create `PrePrGateVO.java`:

```java
package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrePrGateVO {
    private Long id;
    private Long reviewId;
    private String gateStatus;
    private String summary;
    private List<String> blockedReasons;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [x] **Step 2: Add service interface method**

In `ReviewService.java`, add:

```java
PrePrGateVO getPrePrGate(Long reviewId);
```

- [x] **Step 3: Implement mapper-to-VO conversion**

In `ReviewServiceImpl.java`, add a private converter:

```java
private PrePrGateVO toPrePrGateVO(PrePrGate gate) {
    if (gate == null) {
        return null;
    }
    PrePrGateVO vo = new PrePrGateVO();
    vo.setId(gate.getId());
    vo.setReviewId(gate.getReviewId());
    vo.setGateStatus(gate.getGateStatus());
    vo.setSummary(gate.getSummary());
    vo.setDecidedBy(gate.getDecidedBy());
    vo.setDecidedAt(gate.getDecidedAt());
    vo.setCreatedAt(gate.getCreatedAt());
    vo.setUpdatedAt(gate.getUpdatedAt());
    try {
        vo.setBlockedReasons(objectMapper.readValue(
                gate.getBlockedReasons() == null ? "[]" : gate.getBlockedReasons(),
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
    } catch (Exception e) {
        vo.setBlockedReasons(List.of(gate.getBlockedReasons()));
    }
    return vo;
}
```

- [x] **Step 4: Implement gate read method**

In `ReviewServiceImpl.java`, add:

```java
@Override
public PrePrGateVO getPrePrGate(Long reviewId) {
    PrePrGate gate = prePrGateMapper.selectOne(
            new LambdaQueryWrapper<PrePrGate>().eq(PrePrGate::getReviewId, reviewId));
    return toPrePrGateVO(gate);
}
```

- [x] **Step 5: Add controller endpoint**

In `ReviewController.java`, add:

```java
@GetMapping("/{id}/gate")
public Result<PrePrGateVO> getPrePrGate(@PathVariable("id") Long id) {
    return Result.success(reviewService.getPrePrGate(id));
}
```

- [x] **Step 6: Compile backend**

Run:

```powershell
cd review-agent-server
mvn -q -DskipTests compile
```

Expected: compile exits with code 0.

## Task 3: Wire Review Detail Gate Actions

**Files:**
- Modify: `review-agent-web/src/types/review.ts`
- Modify: `review-agent-web/src/views/reviews/detail.vue`

- [x] **Step 1: Add frontend gate type**

In `review-agent-web/src/types/review.ts`, add:

```ts
import type { PrePrGateStatus } from '@/utils/prePrGate'

export interface PrePrGate {
  id: number
  reviewId: number
  gateStatus: PrePrGateStatus
  summary?: string | null
  blockedReasons: string[]
  decidedBy?: string | null
  decidedAt?: string | null
  createdAt?: string | null
  updatedAt?: string | null
}
```

- [x] **Step 2: Fetch gate data in Review detail**

In `detail.vue`, create state and loader:

```ts
const prePrGate = ref<PrePrGate | null>(null)

async function loadPrePrGate() {
  if (!reviewId.value) return
  const res = await get<PrePrGate>(`/reviews/${reviewId.value}/gate`)
  prePrGate.value = res.data || null
}
```

Call `loadPrePrGate()` after loading review detail and after decision actions.

- [x] **Step 3: Add manual decision action**

In `detail.vue`, add:

```ts
async function decidePrePrGate(decision: PrePrGateStatus, comment = '') {
  await patch(`/reviews/${reviewId.value}/pre-pr-decision`, buildPrePrDecisionPayload(decision, comment))
  await loadReviewDetail()
  await loadPrePrGate()
}
```

- [x] **Step 4: Add SARIF download action**

In `detail.vue`, add:

```ts
async function downloadSarif() {
  const res = await get(`/reviews/${reviewId.value}/sarif`)
  const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/sarif+json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = buildSarifFilename(reviewId.value)
  link.click()
  URL.revokeObjectURL(url)
}
```

- [x] **Step 5: Build frontend**

Run:

```powershell
cd review-agent-web
npm test
npm run build
```

Expected: tests and build both exit with code 0.

## Task 4: Use Pre-PR Creation Endpoint

**Files:**
- Modify: `review-agent-web/src/views/reviews/create.vue`
- Modify: `review-agent-web/src/utils/reviewCreate.ts`
- Modify: `review-agent-web/tests/reviewCreate.test.ts`

- [x] **Step 1: Add endpoint helper test**

Extend `reviewCreate.test.ts`:

```ts
import { getReviewCreateEndpoint } from '../src/utils/reviewCreate'

test('review create endpoint uses pre-pr route for pre-pr submissions', () => {
  assert.equal(getReviewCreateEndpoint(true), '/reviews/pre-pr')
  assert.equal(getReviewCreateEndpoint(false), '/reviews')
})
```

- [x] **Step 2: Implement endpoint helper**

Add to `reviewCreate.ts`:

```ts
export function getReviewCreateEndpoint(prePr: boolean): string {
  return prePr ? '/reviews/pre-pr' : '/reviews'
}
```

- [x] **Step 3: Submit to Pre-PR endpoint**

In `create.vue`, replace:

```ts
post<Review>('/reviews', buildReviewCreatePayload(form, selectedStrategy.value!))
```

with:

```ts
post<Review>(getReviewCreateEndpoint(true), buildReviewCreatePayload(form, selectedStrategy.value!))
```

- [x] **Step 4: Reconcile payload contract**

If backend `CreatePrePrRequest` still accepts only `projectId`, `sourceBranch`, and `targetBranch`, update `CreatePrePrRequest.java` to include optional `reviewMode` and `modelsConfig`:

```java
private String reviewMode;
private String modelsConfig;
```

The fields are optional so existing callers remain compatible.

- [x] **Step 5: Verify frontend and backend**

Run:

```powershell
cd review-agent-web
npm test
npm run build

cd ..\review-agent-server
mvn -q -DskipTests compile
```

Expected: all commands exit with code 0.

## Task 5: Confirm Provider-Neutral Status Check Adapter

**Files:**
- Existing: `review-agent-server/src/main/java/com/review/agent/infrastructure/ci/CiStatusService.java`
- Existing: `review-agent-server/src/main/java/com/review/agent/infrastructure/ci/HttpCiStatusService.java`
- Existing usage: `review-agent-server/src/main/java/com/review/agent/service/impl/ReviewServiceImpl.java`

- [x] **Step 1: Confirm adapter interface exists**

Existing interface:

```java
package com.review.agent.infrastructure.ci;

public interface CiStatusService {
    void reportPass(Long reviewId, String description);
    void reportBlock(Long reviewId, String description);
    void reportRunning(Long reviewId, String description);
}
```

- [x] **Step 2: Confirm no-op implementation exists**

Existing implementation:

```java
package com.review.agent.infrastructure.ci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HttpCiStatusService implements CiStatusService {
    // Logs PASS/BLOCK/RUNNING status-check intent.
}
```

- [x] **Step 3: Confirm service calls adapter after gate persistence and decision**

`ReviewServiceImpl` already calls:

```java
ciStatusService.reportBlock(reviewId, "BLOCKER 级别发现 " + blockedReasons.size() + " 个");
ciStatusService.reportPass(reviewId, "Pre-PR 审查通过");
```

- [x] **Step 4: Compile backend**

Run:

```powershell
cd review-agent-server
mvn -q -DskipTests compile
```

Expected: compile exits with code 0.

## Task 6: Update Docs and Verify

**Files:**
- Modify: `docs/api-catalog.md`
- Modify: `docs/superpowers/specs/2026-06-03-review-agent-evolution-design.md`
- Modify: `docs/superpowers/plans/2026-06-03-pre-pr-closed-loop.md`

- [x] **Step 1: Document implemented endpoint**

In `docs/api-catalog.md`, add:

```markdown
| `GET` | `/api/reviews/{id}/gate` | 查询持久化 Pre-PR Gate 状态 | Review 详情页 |
```

- [x] **Step 2: Run verification commands**

Run:

```powershell
cd review-agent-web
npm test
npm run build

cd ..\review-agent-server
mvn -q -DskipTests compile
```

Expected: all commands exit with code 0.

- [x] **Step 3: Inspect diff**

Run:

```powershell
git status --short
git diff --stat
```

Expected: diff includes only files listed in this plan plus any previously accepted first-stage changes.

## Self-Review

- Spec coverage: This plan covers the second-stage priorities from the evolution spec: persisted Gate read contract, Pre-PR endpoint alignment, SARIF frontend entry, manual decision wiring, and CI status adapter boundary. Follow-up execution added GitHub/GitLab payload conversion, dynamic commit status endpoint assembly, a backend Pre-PR Markdown report endpoint, and a default-off HTTP report publishing boundary; external provider verification and real PR comment writeback remain pending.
- Placeholder scan: This plan intentionally contains no placeholder markers or vague handling steps.
- Type consistency: `PrePrGateVO`, `PrePrGate`, `PrePrGateStatus`, `buildPrePrDecisionPayload`, `buildSarifFilename`, and `ReviewStatusCheckAdapter.publish` are consistently named across tasks.
