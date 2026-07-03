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

export interface GovernanceRulePackChange {
  id: number
  rulePackKey: string
  findingId: number
  changeType: string
  title: string
  rationale?: string | null
  status: string
  createdBy?: string | null
  createdAt?: string | null
}

export interface GovernanceRulePackVersion {
  id: number
  rulePackKey: string
  sourceChangeId: number
  versionNo: number
  versionStatus: string
  title: string
  controlsSnapshot?: string | null
  rationale?: string | null
  createdBy?: string | null
  createdAt?: string | null
}

export interface GovernanceRulePackDryRun {
  changeId: number
  rulePackKey: string
  changeType: string
  title: string
  existingControlCount: number
  proposedControlCount: number
  proposedControls: string[]
  controlsSnapshot?: string | null
  impactSummary: string[]
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

export interface CiStatusIntegrationReadiness {
  connectorId: string
  name: string
  stage: RolloutStage
  status: CapabilityStatus
  requiredCapabilityIds: string[]
  backendSignals: string[]
  nextActions: string[]
}

export interface CiStatusConfigVO {
  id: number | null
  connectorKey: string
  provider: string
  repoOwner: string | null
  repoName: string | null
  repoUrl: string | null
  defaultBranch: string | null
  statusContext: string
  checksEnabled: boolean
  sarifUploadEnabled: boolean
  tokenConfigured: boolean
  webhookSecretConfigured: boolean
}

export interface CiStatusWritebackLogVO {
  id: number
  connectorKey: string
  provider: string
  reviewId: number | null
  commitSha: string | null
  state: string | null
  writebackStatus: 'SUCCESS' | 'FAILED' | 'SKIPPED'
  requestUrl: string | null
  externalQueueUrl: string | null
  externalBuildUrl: string | null
  externalBuildNumber: string | null
  externalBuildResult: string | null
  externalResultUpdatedAt: string | null
  errorMessage: string | null
  retryCount: number
  nextRetryAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CiIntegrationHealthVO {
  connectorKey: string
  provider: string
  healthStatus: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY' | 'NO_DATA' | string
  totalCount: number
  successCount: number
  failedCount: number
  skippedCount: number
  latestWritebackStatus: string | null
  latestExternalResult: string | null
  latestAt: string | null
  summary: string | null
}

export interface IntegrationActionLogVO {
  id: number
  connectorKey: string
  provider: string
  actionType: 'SARIF_UPLOAD' | 'PR_SUMMARY_COMMENT' | string
  actionStatus: 'UPLOADED' | 'POSTED' | 'SKIPPED' | 'FAILED' | string
  targetKey: string | null
  commitSha: string | null
  requestUrl: string | null
  errorMessage: string | null
  createdAt: string
  updatedAt: string
}
