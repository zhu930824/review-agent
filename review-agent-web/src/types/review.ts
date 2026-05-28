export type ReviewStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
export type ReviewMode = 'SINGLE' | 'MULTI' | 'JUDGE' | 'AGENT'
export type SeverityLevel = 'BLOCKER' | 'MAJOR' | 'MINOR' | 'INFO'
export type FindingCategory = 'CODE_STYLE' | 'BUG' | 'PERFORMANCE' | 'SECURITY' | 'EXCEPTION_HANDLING' | 'OTHER'
export type HumanStatus = 'PENDING' | 'CONFIRMED' | 'DISMISSED'
export type ModelRole = 'WORKER' | 'JUDGE'

export interface Review {
  id: number
  projectId: number
  projectName: string | null
  sourceBranch: string | null
  targetBranch: string | null
  sourceCommit: string | null
  targetCommit: string | null
  status: ReviewStatus
  reviewMode: ReviewMode
  modelsConfig: string | null
  summary: string | null
  createdAt: string
  updatedAt: string
}

export interface ReviewFinding {
  id: number
  reviewId: number
  filePath: string
  lineStart: number | null
  lineEnd: number | null
  category: FindingCategory
  severity: SeverityLevel
  title: string
  description: string | null
  suggestion: string | null
  modelName: string | null
  confidence: number | null
  isCrossHit: boolean
  humanStatus: HumanStatus
  createdAt: string
}

export interface ReviewModelResult {
  id: number
  reviewId: number
  modelName: string
  role: ModelRole
  status: ReviewStatus
  rawResult: string | null
  errorMessage: string | null
  startedAt: string | null
  completedAt: string | null
}

export interface ReviewDetail extends Review {
  findings: ReviewFinding[]
  modelResults: ReviewModelResult[]
  totalFindings: number
  blockerCount: number
  majorCount: number
  minorCount: number
  infoCount: number
  prePrStatus: string | null
  blockedReasons: string[] | null
}

export interface CreateReviewParams {
  projectId: number
  sourceBranch?: string
  targetBranch?: string
  sourceCommit?: string
  targetCommit?: string
  reviewMode: ReviewMode
  modelsConfig?: string
}

export interface CreatePrePrParams {
  projectId: number
  sourceBranch: string
  targetBranch: string
}

export type AgentRole = 'SECURITY_AUDITOR' | 'PERFORMANCE_ANALYST' | 'CODE_STYLE_CHECKER' | 'EXCEPTION_HANDLER' | 'ARCHITECT_REVIEWER'

export interface AgentConfig {
  role: AgentRole
  modelName: string
  systemPrompt?: string
  skills: string[]
  temperature?: number
  maxSteps?: number
}

export interface AgentPipelineConfig {
  agents: AgentConfig[]
  orchestrationStrategy: 'PARALLEL' | 'SEQUENTIAL' | 'HYBRID'
  mcpEnabled: boolean
  mcpServerUrls: string[]
}

export interface AgentExecutionResult {
  role: string
  modelName: string
  success: boolean
  output: string | null
  errorMessage: string | null
  durationMs: number
}

export interface AgentRoleOption {
  role: AgentRole
  label: string
  defaultModel: string
  skills: string[]
  description: string
}

export interface ModelOption {
  value: string
  label: string
}

export interface AgentReviewDetail extends ReviewDetail {
  agentResults: AgentExecutionResult[]
}
