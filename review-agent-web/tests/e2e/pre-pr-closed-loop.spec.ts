import { expect, test, type Page } from '@playwright/test'
import assert from 'node:assert/strict'

const reviewDetail = {
  id: 42,
  projectId: 1,
  projectName: 'Review Agent',
  sourceBranch: 'feature/pre-pr',
  targetBranch: 'main',
  sourceCommit: null,
  targetCommit: null,
  status: 'COMPLETED',
  reviewMode: 'AGENT',
  modelsConfig: null,
  summary: 'Pre-PR 审查完成，存在阻断风险。',
  createdAt: '2026-06-03T10:00:00',
  updatedAt: '2026-06-03T10:05:00',
  findings: [
    {
      id: 7,
      reviewId: 42,
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

const prePrGate = {
  id: 3,
  reviewId: 42,
  gateStatus: 'BLOCKED',
  summary: '存在阻断风险。',
  blockedReasons: ['Token 未校验过期时间'],
  decidedBy: null,
  decidedAt: null,
  createdAt: '2026-06-03T10:00:00',
  updatedAt: '2026-06-03T10:05:00',
}

async function mockReviewApis(page: Page) {
  await page.route('**/api/reviews/42', async route => {
    if (route.request().method() === 'GET') {
      await route.fulfill({ json: { code: 200, data: reviewDetail, message: 'ok' } })
      return
    }
    await route.fallback()
  })

  await page.route('**/api/reviews/42/gate', async route => {
    await route.fulfill({ json: { code: 200, data: prePrGate, message: 'ok' } })
  })

  await page.route('**/api/reviews/42/sarif', async route => {
    await route.fulfill({
      json: {
        code: 200,
        data: {
          version: '2.1.0',
          runs: [{ tool: { driver: { name: 'Review Agent' } }, results: [] }],
        },
        message: 'ok',
      },
    })
  })
}

test('unauthenticated users are redirected to login before review detail', async ({ page }) => {
  await page.goto('/reviews/42')

  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Review Agent' })).toBeVisible()
  await expect(page.getByText('用户名')).toBeVisible()
  await expect(page.getByText('密码')).toBeVisible()
})

test('review detail exposes Pre-PR gate, SARIF, report, and manual decision actions', async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('review-agent-token', 'e2e-token')
  })
  await mockReviewApis(page)
  let reportRequested = false
  await page.route('**/api/reviews/42/pre-pr-report', async route => {
    reportRequested = true
    await route.fulfill({
      json: {
        code: 200,
        data: '# Review Agent Pre-PR 审查报告\n\n后端生成报告',
        message: 'ok',
      },
    })
  })

  let decisionPayload: unknown = null
  await page.route('**/api/reviews/42/pre-pr-decision', async route => {
    decisionPayload = route.request().postDataJSON()
    await route.fulfill({ json: { code: 200, data: null, message: 'ok' } })
  })

  await page.goto('/reviews/42')

  await expect(page.getByRole('heading', { name: '审查详情' })).toBeVisible()
  await expect(page.getByText('已阻断')).toBeVisible()
  await expect(page.getByText('Token 未校验过期时间')).toBeVisible()

  await page.getByRole('button', { name: '人工通过' }).click()
  expect(decisionPayload).toEqual({ decision: 'PASSED', comment: '人工确认通过' })

  const sarifDownload = page.waitForEvent('download')
  await page.getByRole('button', { name: '下载 SARIF' }).click()
  assert.equal((await sarifDownload).suggestedFilename(), 'review-42.sarif.json')

  const reportDownload = page.waitForEvent('download')
  await page.getByRole('button', { name: '下载报告' }).click()
  assert.equal((await reportDownload).suggestedFilename(), 'review-42-pre-pr-report.md')
  assert.equal(reportRequested, true)
})
