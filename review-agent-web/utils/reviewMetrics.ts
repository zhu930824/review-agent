type ReviewLike = {
  status?: string | null
  reviewMode?: string | null
}

type FindingLike = {
  severity?: string | null
  humanStatus?: string | null
  title?: string | null
}

type ModelResultLike = {
  status?: string | null
}

type ReviewDetailLike = {
  status?: string | null
  findings?: FindingLike[]
  modelResults?: ModelResultLike[]
  blockedReasons?: string[] | null
}

export type GateStatus = 'PASSED' | 'BLOCKED' | 'NEEDS_HUMAN_REVIEW' | 'RUNNING'

export interface DashboardMetrics {
  totalReviews: number
  completedReviews: number
  runningReviews: number
  failedReviews: number
  blockerFindings: number
  majorFindings: number
  pendingHumanConfirmations: number
  blockedReviews: number
  prePrPassRate: number
  modelSuccessRate: number
}

export function deriveModelSuccessRate(modelResults: ModelResultLike[] = []): number {
  if (!modelResults.length) return 0
  const completed = modelResults.filter(result => result.status === 'COMPLETED').length
  return Math.round((completed / modelResults.length) * 100)
}

export function deriveGateStatus(detail: ReviewDetailLike): GateStatus {
  if (detail.status === 'RUNNING' || detail.status === 'PENDING') return 'RUNNING'

  const findings = detail.findings ?? []
  if (findings.some(finding => finding.severity === 'BLOCKER')) return 'BLOCKED'
  if (findings.some(finding => finding.severity === 'MAJOR' && finding.humanStatus === 'PENDING')) {
    return 'NEEDS_HUMAN_REVIEW'
  }
  return 'PASSED'
}

export function generateBlockedReasons(detail: ReviewDetailLike): string[] {
  const findings = detail.findings ?? []
  const blockerCount = findings.filter(finding => finding.severity === 'BLOCKER').length
  const pendingMajorCount = findings.filter(finding => finding.severity === 'MAJOR' && finding.humanStatus === 'PENDING').length
  const reasons = [...(detail.blockedReasons ?? [])]

  if (blockerCount > 0) {
    reasons.push(`存在 ${blockerCount} 个 BLOCKER 级别问题，Pre-PR 暂不可通过。`)
  }
  if (pendingMajorCount > 0) {
    reasons.push(`存在 ${pendingMajorCount} 个 MAJOR 问题等待人工复核。`)
  }
  return Array.from(new Set(reasons))
}

export function deriveDashboardMetrics(reviews: ReviewLike[] = [], details: ReviewDetailLike[] = []): DashboardMetrics {
  const allFindings = details.flatMap(detail => detail.findings ?? [])
  const allModelResults = details.flatMap(detail => detail.modelResults ?? [])
  const blockedReviews = details.filter(detail => deriveGateStatus(detail) === 'BLOCKED').length
  const passedReviews = details.filter(detail => deriveGateStatus(detail) === 'PASSED').length

  return {
    totalReviews: reviews.length,
    completedReviews: reviews.filter(review => review.status === 'COMPLETED').length,
    runningReviews: reviews.filter(review => review.status === 'RUNNING').length,
    failedReviews: reviews.filter(review => review.status === 'FAILED').length,
    blockerFindings: allFindings.filter(finding => finding.severity === 'BLOCKER').length,
    majorFindings: allFindings.filter(finding => finding.severity === 'MAJOR').length,
    pendingHumanConfirmations: allFindings.filter(finding => finding.humanStatus === 'PENDING' && finding.severity !== 'INFO').length,
    blockedReviews,
    prePrPassRate: details.length ? Math.round((passedReviews / details.length) * 100) : 0,
    modelSuccessRate: deriveModelSuccessRate(allModelResults),
  }
}
