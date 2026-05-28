import type { WorkflowStep } from '~/types/release'

export function getCurrentStep(steps: WorkflowStep[]): WorkflowStep | undefined {
  return steps.find(s => s.status !== 'COMPLETED')
}

export function getCompletedCount(steps: WorkflowStep[]): number {
  return steps.filter(s => s.status === 'COMPLETED').length
}

export function getStepProgress(steps: WorkflowStep[]): number {
  if (steps.length === 0) return 0
  return Math.round((getCompletedCount(steps) / steps.length) * 100)
}

export function isReleaseBlocked(steps: WorkflowStep[]): boolean {
  return steps.some(s =>
    s.status === 'BLOCKED' || (s.status === 'REQUIRED' && s.type === 'GATE')
  )
}
