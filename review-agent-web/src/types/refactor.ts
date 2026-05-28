export interface RefactorStep {
  stepName: string
  targetFile: string
  description: string
  approach: string
  priority: string
  breaking: boolean
  estimatedEffort: string
}

export interface RefactorPlan {
  planId: string
  title: string
  summary: string
  totalSteps: number
  estimatedImpact: number
  steps: RefactorStep[]
  prerequisites: string[]
  risks: string[]
}
