export interface WorkflowStep {
  stepKey: string
  name: string
  type: string
  responsible: string
  order: number
  dependsOn: string
  status: string
}

export interface ReleaseWorkflow {
  reviewId: number
  workflowId: string
  steps: WorkflowStep[]
  currentStep: string
  status: string
}
