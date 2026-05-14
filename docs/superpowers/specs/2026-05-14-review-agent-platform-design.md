# Review Agent Platform Design

## Goal

Upgrade the current review-agent project from a basic AI code review tool into a business-facing AI Coding governance platform. The platform should help teams ship faster while controlling technical debt through multi-model review, Pre-PR quality gates, human confirmation workflows, and governance metrics.

## Context From `doc/设计.md`

The document describes four reusable principles:

- AI Coding does not automatically reduce complexity. Without team-wide constraints, AI-generated code accelerates system decay.
- Governance must follow "human alignment first, human-machine alignment second": teams agree on engineering standards before encoding those standards into AI rules, skills, and review workflows.
- Technical debt should be consumed progressively through normal business delivery instead of waiting for a large rewrite.
- Pre-PR review should move routine checks to AI and leave humans focused on business semantics, architecture, and whether the right problem is being solved.

The existing system already has project management, review tasks, findings, model result storage, review modes, Agent roles, and a Nuxt UI. The missing piece is a coherent business workflow around model configuration, quality gates, review strategy reuse, and operational visibility.

## Scope

This design covers four deliverable tracks.

### 1. Multi-Model Configuration Center

Users should configure model providers and review strategies without hand-writing JSON.

Core concepts:

- Model provider: DashScope, DeepSeek, Kimi, OpenAI-compatible endpoint, or a custom provider.
- Model profile: model name, display name, provider, API key environment variable, endpoint, timeout, temperature, max tokens, enabled state, and business tags.
- Review strategy: reusable template for SINGLE, MULTI, JUDGE, or AGENT review modes.
- Role binding: maps a business role such as security auditor or architect reviewer to one model profile and optional skills.

The first implementation can be frontend-first and config-backed. When backend source access is safe, persist these records in database tables and expose typed APIs.

### 2. Pre-PR Quality Gate

Pre-PR should become the main business workflow.

Workflow:

1. User selects project, source branch, target branch, and a review strategy.
2. Platform runs AI checks using the selected strategy.
3. Findings are grouped into blockers, major risks, minor improvements, and info items.
4. Platform generates blocked reasons and a PR summary.
5. Human reviewer confirms or dismisses findings.
6. The final gate status becomes PASSED, BLOCKED, or NEEDS_HUMAN_REVIEW.

Gate rules:

- BLOCKER findings block the Pre-PR by default.
- MAJOR findings require human confirmation unless the selected strategy marks them advisory.
- Cross-model hits raise confidence and should be surfaced above single-model findings.
- Judge model output is treated as decision support, not as the only source of truth.

### 3. Business Empowerment Dashboard

The platform should show whether AI review is improving delivery quality.

Dashboard metrics:

- Review throughput: total reviews, completed reviews, running reviews.
- Quality pressure: blockers, major findings, pending human confirmations.
- Model effectiveness: model success rate, cross-hit count, Judge disagreement count.
- Business readiness: Pre-PR pass rate, blocked review count, average time to complete.

The first implementation should use available review list/detail data and local derived metrics. Backend aggregation can be added later.

### 4. Architecture Governance

Long-term backend direction:

- Application layer: use cases and workflow orchestration.
- Domain layer: business concepts such as Review, Finding, ModelProfile, ReviewStrategy, PrePrGate.
- Infrastructure layer: MyBatis persistence, Git operations, model clients, MCP bridge.
- Common layer: shared response, errors, validation, and utility contracts.

Current backend Java source files appear to be protected or encoded in this workspace, so direct backend refactoring is risky until source access is confirmed. Initial backend-safe changes should be limited to readable configuration, migrations, documentation, and new files that do not overwrite protected source. Existing unreadable files must not be edited blindly.

## Data Model Direction

Add these tables when backend source access and database migration flow are confirmed:

- `model_provider`: provider identity, endpoint, auth environment variable, enabled state.
- `model_profile`: provider reference, model name, display name, capability tags, default parameters.
- `review_strategy`: strategy name, review mode, gate policy, enabled state.
- `review_strategy_model`: strategy-to-model role bindings.
- `pre_pr_gate`: review reference, gate status, generated summary, blocked reasons, human decision metadata.

The current `review.models_config` field remains backward compatible. New strategies can compile into the existing JSON payload until first-class backend APIs are available.

## Frontend Design

Pages and components:

- Add `/settings/models` as the multi-model configuration center.
- Add a model strategy selector to `/reviews/create`.
- Replace raw JSON-first configuration with guided strategy cards and advanced JSON fallback.
- Improve `/reviews/[id]` text, Pre-PR status, blocked reasons, model results, and findings filters.
- Upgrade dashboard copy and metrics so the first screen communicates business value, not only task counts.

UI principles:

- Operational dashboard style: dense, scannable, restrained.
- Avoid marketing hero layout.
- Use existing Nuxt UI components.
- Keep configuration forms explicit and predictable.

## API Direction

Near-term:

- Keep using existing `/reviews` and `/projects` APIs.
- Store selected strategy as `modelsConfig` JSON.
- Derive dashboard metrics on the frontend.
- Keep static built-in model profiles as a seed list.

Long-term:

- `GET /model-providers`
- `POST /model-providers`
- `GET /model-profiles`
- `POST /model-profiles`
- `GET /review-strategies`
- `POST /review-strategies`
- `POST /reviews/pre-pr`
- `PATCH /reviews/{id}/pre-pr-decision`

## Testing Strategy

Frontend:

- Add pure TypeScript helpers for model profiles, strategies, gate policy, and dashboard metrics.
- Test helpers with Node's built-in test runner or Vitest if added later.
- Build the Nuxt app after UI changes.

Backend:

- Before modifying existing Java, run Maven compile to confirm source readability.
- If Maven cannot compile due protected source, defer backend code edits and document the blocker.
- Migration SQL can be validated structurally as text until an integration database is available.

## Risks

- Backend source files are not safely readable in this workspace. Editing them could corrupt protected files.
- There is no repository metadata at the root, so commits required by the brainstorming skill cannot be created from the current directory.
- Existing frontend contains some mojibake in review detail text. Fixing the visible UI text is low-risk and should be done as part of the dashboard/Pre-PR track.

## Acceptance Criteria

- Users can choose multi-model review strategies without writing JSON.
- Users can see model profiles and strategy definitions in a configuration page.
- Review creation sends a valid `modelsConfig` compiled from the selected strategy.
- Review detail page clearly shows gate status, blocked reasons, model execution, and human finding actions.
- Dashboard communicates business readiness and quality pressure.
- Backend architecture and migration path are documented, with direct backend source edits avoided unless build/source checks pass.
