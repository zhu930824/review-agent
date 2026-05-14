import type { ReviewStrategyId } from './model-config'

export type CapabilityStatus = 'ENABLED' | 'PARTIAL' | 'PLANNED'
export type BusinessImpact = 'HIGH' | 'MEDIUM' | 'LOW'
export type RolloutStage = 'live' | 'next' | 'later'

export interface MarketCapability {
  id: string
  name: string
  category: 'AI_REVIEW' | 'QUALITY' | 'SECURITY' | 'INTEGRATION' | 'KNOWLEDGE' | 'ANALYTICS'
  status: CapabilityStatus
  businessImpact: BusinessImpact
  readiness: number
  marketSignal: string
  platformMove: string
  sourceProducts: string[]
}

export interface IntegrationConnector {
  id: string
  name: string
  stage: RolloutStage
  status: CapabilityStatus
  businessValue: string
  implementationHint: string
}

export interface GovernanceRulePack {
  id: string
  name: string
  businessOutcome: string
  controls: string[]
  capabilityIds: string[]
  suggestedStrategies: ReviewStrategyId[]
  humanCheckpoints: string[]
}

export interface WorkflowTemplate {
  id: string
  name: string
  scenario: string
  strategyId: ReviewStrategyId
  rulePackIds: string[]
  integrationIds: string[]
  successMetric: string
}

export interface CapabilityCoverageSummary {
  total: number
  enabled: number
  partial: number
  planned: number
  coveragePercent: number
}

export interface RecommendedAction extends MarketCapability {
  priorityScore: number
}

export interface CompiledGovernancePolicyPack {
  packIds: string[]
  requiredCapabilities: string[]
  suggestedStrategies: ReviewStrategyId[]
  requiredHumanCheckpoints: string[]
  controls: string[]
}
