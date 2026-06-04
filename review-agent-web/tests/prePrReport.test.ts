import assert from 'node:assert/strict'
import test from 'node:test'
import { buildPrePrReportFilename, buildPrePrReportMarkdown } from '../src/utils/prePrReport'
import type { PrePrGate, ReviewDetail } from '../src/types/review'

const detail: ReviewDetail = {
  id: 18,
  projectId: 1,
  projectName: 'Review Agent',
  sourceBranch: 'feature/pre-pr',
  targetBranch: 'main',
  sourceCommit: null,
  targetCommit: null,
  status: 'COMPLETED',
  reviewMode: 'AGENT',
  modelsConfig: null,
  summary: null,
  createdAt: '2026-06-03T10:00:00',
  updatedAt: '2026-06-03T10:05:00',
  findings: [
    {
      id: 1,
      reviewId: 18,
      filePath: 'src/Auth.java',
      lineStart: 42,
      lineEnd: 43,
      category: 'SECURITY',
      severity: 'BLOCKER',
      title: 'Token 未校验过期时间',
      description: 'JWT 解析后没有校验 exp。',
      suggestion: '增加过期时间校验。',
      modelName: 'qwen-plus',
      confidence: 0.92,
      isCrossHit: true,
      humanStatus: 'PENDING',
      createdAt: '2026-06-03T10:02:00',
    },
  ],
  modelResults: [],
  totalFindings: 1,
  blockerCount: 1,
  majorCount: 0,
  minorCount: 0,
  infoCount: 0,
  prePrStatus: 'BLOCKED',
  blockedReasons: ['Token 未校验过期时间'],
}

const gate: PrePrGate = {
  id: 9,
  reviewId: 18,
  gateStatus: 'BLOCKED',
  summary: '存在阻断风险。',
  blockedReasons: ['Token 未校验过期时间'],
}

test('pre-pr report markdown contains readable review summary, gate state, and findings', () => {
  const report = buildPrePrReportMarkdown(detail, gate)

  assert.match(report, /# Review Agent Pre-PR 审查报告/)
  assert.match(report, /项目：Review Agent/)
  assert.match(report, /分支：feature\/pre-pr -> main/)
  assert.match(report, /Gate：已阻断/)
  assert.match(report, /Token 未校验过期时间/)
  assert.match(report, /src\/Auth.java:42/)
  assert.match(report, /增加过期时间校验/)
})

test('pre-pr report filename is stable per review', () => {
  assert.equal(buildPrePrReportFilename(18), 'review-18-pre-pr-report.md')
})
