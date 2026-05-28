import type { RefactorStep, RefactorPlan } from '~/types/refactor'

export function sortStepsByPriority(steps: RefactorStep[]): RefactorStep[] {
  const priorityOrder: Record<string, number> = { P0: 0, P1: 1, P2: 2, P3: 3 }
  return [...steps].sort((a, b) =>
    (priorityOrder[a.priority] ?? 99) - (priorityOrder[b.priority] ?? 99)
  )
}

export function getBreakingSteps(plan: RefactorPlan): RefactorStep[] {
  return plan.steps.filter(s => s.breaking)
}

export function getSafeSteps(plan: RefactorPlan): RefactorStep[] {
  return plan.steps.filter(s => !s.breaking)
}

export function estimateTotalEffort(plan: RefactorPlan): string {
  const p0Count = plan.steps.filter(s => s.priority === 'P0').length
  const p1Count = plan.steps.filter(s => s.priority === 'P1').length
  const p2Count = plan.steps.filter(s => s.priority === 'P2').length
  const days = p0Count * 4 + p1Count * 2 + p2Count * 1
  if (days === 0) return '< 1 人天'
  return `${days} 人天`
}
