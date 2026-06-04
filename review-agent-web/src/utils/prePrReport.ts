import type { PrePrGate, ReviewDetail, ReviewFinding } from '@/types/review'
import { getPrePrGateLabel } from './prePrGate'

function lineRef(finding: ReviewFinding): string {
  if (!finding.lineStart) return finding.filePath
  if (!finding.lineEnd || finding.lineEnd === finding.lineStart) {
    return `${finding.filePath}:${finding.lineStart}`
  }
  return `${finding.filePath}:${finding.lineStart}-${finding.lineEnd}`
}

function findingLine(finding: ReviewFinding): string {
  const parts = [
    `- [${finding.severity}] ${finding.title}`,
    `  - 位置：${lineRef(finding)}`,
    finding.category ? `  - 分类：${finding.category}` : '',
    finding.modelName ? `  - 模型：${finding.modelName}` : '',
    finding.description ? `  - 描述：${finding.description}` : '',
    finding.suggestion ? `  - 建议：${finding.suggestion}` : '',
  ]
  return parts.filter(Boolean).join('\n')
}

export function buildPrePrReportFilename(reviewId: number | string): string {
  return `review-${reviewId}-pre-pr-report.md`
}

export function buildPrePrReportMarkdown(detail: ReviewDetail, gate: PrePrGate | null): string {
  const gateStatus = gate?.gateStatus || detail.prePrStatus || 'RUNNING'
  const gateLabel = gateStatus === 'PASSED' || gateStatus === 'BLOCKED' || gateStatus === 'NEEDS_HUMAN_REVIEW' || gateStatus === 'RUNNING'
    ? getPrePrGateLabel(gateStatus)
    : gateStatus
  const blockedReasons = gate?.blockedReasons?.length ? gate.blockedReasons : detail.blockedReasons || []
  const findings = detail.findings || []
  const topFindings = findings.slice(0, 20).map(findingLine).join('\n\n') || '无'

  return [
    `# ${detail.projectName || 'Review Agent'} Pre-PR 审查报告`,
    '',
    '## 概览',
    '',
    `- 项目：${detail.projectName || '-'}`,
    `- Review ID：${detail.id}`,
    `- 分支：${detail.sourceBranch || '-'} -> ${detail.targetBranch || '-'}`,
    `- 模式：${detail.reviewMode}`,
    `- 状态：${detail.status}`,
    `- Gate：${gateLabel}`,
    '',
    '## 风险统计',
    '',
    `- 总问题：${detail.totalFindings}`,
    `- BLOCKER：${detail.blockerCount}`,
    `- MAJOR：${detail.majorCount}`,
    `- MINOR：${detail.minorCount}`,
    `- INFO：${detail.infoCount}`,
    '',
    '## 阻断原因',
    '',
    blockedReasons.length ? blockedReasons.map(reason => `- ${reason}`).join('\n') : '无',
    '',
    '## 主要发现',
    '',
    topFindings,
    '',
    '## Gate 摘要',
    '',
    gate?.summary || detail.summary || '无',
    '',
  ].join('\n')
}
