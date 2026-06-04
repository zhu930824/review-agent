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
