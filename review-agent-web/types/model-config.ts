import type { AgentRole, ReviewMode, SeverityLevel } from './review'

export type ModelProviderKind = 'DASHSCOPE' | 'OPENAI_COMPATIBLE' | 'CUSTOM'
export type ReviewStrategyId = 'fast-scan' | 'cross-check' | 'quality-gate' | 'architecture-board'

export interface ModelProvider {
  id: string
  name: string
  kind: ModelProviderKind
  endpoint: string
  apiKeyEnv: string
  enabled: boolean
  description: string
}

export interface ModelProfile {
  id: string
  providerId: string
  modelName: string
  displayName: string
  capabilityTags: string[]
  defaultTemperature: number
  timeoutSeconds: number
  enabled: boolean
}

export interface GatePolicy {
  blockOn: SeverityLevel[]
  requireHumanReviewOn: SeverityLevel[]
  advisoryOn: SeverityLevel[]
}

export interface StrategyRoleBinding {
  role: AgentRole | 'WORKER' | 'JUDGE'
  modelProfileId: string
  skills: string[]
  temperature?: number
}

export interface ReviewStrategy {
  id: ReviewStrategyId
  name: string
  description: string
  reviewMode: ReviewMode
  recommendedFor: string[]
  gatePolicy: GatePolicy
  roleBindings: StrategyRoleBinding[]
}

export interface CompiledAgentConfig {
  role: AgentRole
  modelName: string
  skills: string[]
  temperature: number
}

export interface CompiledAgentPipelineConfig {
  agents: CompiledAgentConfig[]
  orchestrationStrategy: 'PARALLEL' | 'SEQUENTIAL' | 'HYBRID'
  mcpEnabled: boolean
  mcpServerUrls: string[]
  gatePolicy: GatePolicy
  strategyId: ReviewStrategyId
}

export interface CompiledMultiModelConfig {
  models: string[]
  gatePolicy: GatePolicy
  strategyId: ReviewStrategyId
}

export interface CompiledJudgeConfig {
  workers: string[]
  judge: string
  gatePolicy: GatePolicy
  strategyId: ReviewStrategyId
}

export type CompiledModelsConfig =
  | CompiledAgentPipelineConfig
  | CompiledMultiModelConfig
  | CompiledJudgeConfig

export interface CompiledStrategyConfig {
  reviewMode: ReviewMode
  modelsConfig: CompiledModelsConfig
}
