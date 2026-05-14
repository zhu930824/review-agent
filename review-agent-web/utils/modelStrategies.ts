import type {
  CompiledAgentPipelineConfig,
  CompiledJudgeConfig,
  CompiledModelsConfig,
  CompiledMultiModelConfig,
  CompiledStrategyConfig,
  GatePolicy,
  ModelProfile,
  ModelProvider,
  ReviewStrategy,
  ReviewStrategyId,
} from '../types/model-config'

const strictGatePolicy: GatePolicy = {
  blockOn: ['BLOCKER'],
  requireHumanReviewOn: ['MAJOR'],
  advisoryOn: ['MINOR', 'INFO'],
}

const advisoryGatePolicy: GatePolicy = {
  blockOn: ['BLOCKER'],
  requireHumanReviewOn: [],
  advisoryOn: ['MAJOR', 'MINOR', 'INFO'],
}

export const builtinModelProviders: ModelProvider[] = [
  {
    id: 'dashscope',
    name: '阿里云 DashScope',
    kind: 'DASHSCOPE',
    endpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    apiKeyEnv: 'DASHSCOPE_API_KEY',
    enabled: true,
    description: '默认模型供应商，适合通义千问系列模型。',
  },
  {
    id: 'openai-compatible',
    name: 'OpenAI Compatible',
    kind: 'OPENAI_COMPATIBLE',
    endpoint: 'https://api.openai.com/v1',
    apiKeyEnv: 'OPENAI_API_KEY',
    enabled: true,
    description: '兼容 OpenAI API 协议的模型服务。',
  },
  {
    id: 'custom',
    name: '自定义供应商',
    kind: 'CUSTOM',
    endpoint: 'http://localhost:11434/v1',
    apiKeyEnv: 'CUSTOM_MODEL_API_KEY',
    enabled: false,
    description: '用于企业内网、私有化或本地模型。',
  },
]

export const builtinModelProfiles: ModelProfile[] = [
  {
    id: 'qwen-plus',
    providerId: 'dashscope',
    modelName: 'qwen-plus',
    displayName: '通义千问 Plus',
    capabilityTags: ['代码审查', '日常质量检查', '成本均衡'],
    defaultTemperature: 0.3,
    timeoutSeconds: 120,
    enabled: true,
  },
  {
    id: 'qwen-max',
    providerId: 'dashscope',
    modelName: 'qwen-max',
    displayName: '通义千问 Max',
    capabilityTags: ['Judge', '架构评审', '复杂推理'],
    defaultTemperature: 0.2,
    timeoutSeconds: 180,
    enabled: true,
  },
  {
    id: 'deepseek-v3',
    providerId: 'openai-compatible',
    modelName: 'deepseek-v3',
    displayName: 'DeepSeek V3',
    capabilityTags: ['交叉审查', '代码理解', '缺陷发现'],
    defaultTemperature: 0.25,
    timeoutSeconds: 180,
    enabled: true,
  },
  {
    id: 'kimi-k2',
    providerId: 'openai-compatible',
    modelName: 'kimi-k2',
    displayName: 'Kimi K2',
    capabilityTags: ['长上下文', '文档理解', 'PR 摘要'],
    defaultTemperature: 0.3,
    timeoutSeconds: 180,
    enabled: true,
  },
  {
    id: 'local-coder',
    providerId: 'custom',
    modelName: 'local-coder',
    displayName: '本地代码模型',
    capabilityTags: ['私有化', '离线审查'],
    defaultTemperature: 0.2,
    timeoutSeconds: 240,
    enabled: false,
  },
]

export const enabledModelProfiles = builtinModelProfiles.filter(profile => profile.enabled)

export const builtinReviewStrategies: ReviewStrategy[] = [
  {
    id: 'fast-scan',
    name: '快速自查',
    description: '单模型快速发现基础规范、低风险缺陷和明显遗漏。',
    reviewMode: 'SINGLE',
    recommendedFor: ['日常小改动', '提交前自检', '低风险配置变更'],
    gatePolicy: advisoryGatePolicy,
    roleBindings: [{ role: 'WORKER', modelProfileId: 'qwen-plus', skills: ['JavaCodeStyleSkill'] }],
  },
  {
    id: 'cross-check',
    name: '多模型交叉审查',
    description: '用不同模型互相补位，提升缺陷覆盖面和交叉命中可信度。',
    reviewMode: 'MULTI',
    recommendedFor: ['中等风险需求', '多人协作代码', '需要更高召回率的改动'],
    gatePolicy: strictGatePolicy,
    roleBindings: [
      { role: 'WORKER', modelProfileId: 'qwen-plus', skills: ['JavaCodeStyleSkill', 'PerformanceSkill'] },
      { role: 'WORKER', modelProfileId: 'deepseek-v3', skills: ['SecuritySkill', 'ExceptionHandlingSkill'] },
    ],
  },
  {
    id: 'quality-gate',
    name: 'Pre-PR 质量闸门',
    description: '多智能体并行审查，覆盖安全、性能、规范、异常处理和架构风险。',
    reviewMode: 'AGENT',
    recommendedFor: ['正式 PR 前', '核心链路改动', '高风险业务需求'],
    gatePolicy: strictGatePolicy,
    roleBindings: [
      { role: 'SECURITY_AUDITOR', modelProfileId: 'qwen-max', skills: ['SecuritySkill'], temperature: 0.2 },
      { role: 'PERFORMANCE_ANALYST', modelProfileId: 'qwen-plus', skills: ['PerformanceSkill'], temperature: 0.2 },
      { role: 'CODE_STYLE_CHECKER', modelProfileId: 'qwen-plus', skills: ['JavaCodeStyleSkill'], temperature: 0.3 },
      { role: 'EXCEPTION_HANDLER', modelProfileId: 'deepseek-v3', skills: ['ExceptionHandlingSkill'], temperature: 0.2 },
      { role: 'ARCHITECT_REVIEWER', modelProfileId: 'qwen-max', skills: ['JavaCodeStyleSkill', 'PerformanceSkill'], temperature: 0.2 },
    ],
  },
  {
    id: 'architecture-board',
    name: '架构委员会评审',
    description: 'Worker 模型先审查，Judge 模型统一裁决，适合架构或复杂业务边界调整。',
    reviewMode: 'JUDGE',
    recommendedFor: ['架构调整', '领域模型升级', '高影响范围重构'],
    gatePolicy: strictGatePolicy,
    roleBindings: [
      { role: 'WORKER', modelProfileId: 'qwen-plus', skills: ['JavaCodeStyleSkill', 'PerformanceSkill'] },
      { role: 'WORKER', modelProfileId: 'deepseek-v3', skills: ['SecuritySkill', 'ExceptionHandlingSkill'] },
      { role: 'JUDGE', modelProfileId: 'qwen-max', skills: ['JudgeReviewSkill'] },
    ],
  },
]

const roleLabels: Record<string, string> = {
  WORKER: '审查模型',
  JUDGE: 'Judge 模型',
  SECURITY_AUDITOR: '安全审计员',
  PERFORMANCE_ANALYST: '性能分析师',
  CODE_STYLE_CHECKER: '代码规范检查员',
  EXCEPTION_HANDLER: '异常处理专家',
  ARCHITECT_REVIEWER: '架构评审员',
}

export function getModelProfile(profileId: string): ModelProfile {
  const profile = builtinModelProfiles.find(item => item.id === profileId)
  if (!profile) {
    throw new Error(`Unknown model profile: ${profileId}`)
  }
  return profile
}

export function getReviewStrategy(strategyId: ReviewStrategyId): ReviewStrategy {
  const strategy = builtinReviewStrategies.find(item => item.id === strategyId)
  if (!strategy) {
    throw new Error(`Unknown review strategy: ${strategyId}`)
  }
  return strategy
}

export function deriveGatePolicy(strategyId: ReviewStrategyId): GatePolicy {
  return getReviewStrategy(strategyId).gatePolicy
}

export function getStrategyRoleLabels(strategyId: ReviewStrategyId): string[] {
  return getReviewStrategy(strategyId).roleBindings.map(binding => {
    const profile = getModelProfile(binding.modelProfileId)
    return `${roleLabels[binding.role] ?? binding.role} · ${profile.displayName}`
  })
}

export function getProviderModelCounts() {
  return builtinModelProviders.map(provider => {
    const profiles = builtinModelProfiles.filter(profile => profile.providerId === provider.id)
    return {
      providerId: provider.id,
      providerName: provider.name,
      enabled: provider.enabled,
      totalProfiles: profiles.length,
      enabledProfiles: profiles.filter(profile => profile.enabled).length,
    }
  })
}

export function getStrategySummaries() {
  return builtinReviewStrategies.map(strategy => ({
    id: strategy.id,
    name: strategy.name,
    description: strategy.description,
    reviewMode: strategy.reviewMode,
    recommendedFor: strategy.recommendedFor,
    gatePolicy: strategy.gatePolicy,
    roleLabels: getStrategyRoleLabels(strategy.id),
  }))
}

export function compileStrategyConfig(strategyId: ReviewStrategyId): CompiledStrategyConfig {
  const strategy = getReviewStrategy(strategyId)
  const modelNames = strategy.roleBindings.map(binding => getModelProfile(binding.modelProfileId).modelName)

  if (strategy.reviewMode === 'AGENT') {
    const agentConfig: CompiledAgentPipelineConfig = {
      agents: strategy.roleBindings.map(binding => {
        if (binding.role === 'WORKER' || binding.role === 'JUDGE') {
          throw new Error(`AGENT strategy cannot use generic role: ${binding.role}`)
        }
        const profile = getModelProfile(binding.modelProfileId)
        return {
          role: binding.role,
          modelName: profile.modelName,
          skills: binding.skills,
          temperature: binding.temperature ?? profile.defaultTemperature,
        }
      }),
      orchestrationStrategy: 'PARALLEL',
      mcpEnabled: false,
      mcpServerUrls: [],
      gatePolicy: strategy.gatePolicy,
      strategyId,
    }
    return { reviewMode: strategy.reviewMode, modelsConfig: agentConfig }
  }

  if (strategy.reviewMode === 'JUDGE') {
    const judgeBinding = strategy.roleBindings.find(binding => binding.role === 'JUDGE')
    if (!judgeBinding) {
      throw new Error(`JUDGE strategy requires a judge binding: ${strategyId}`)
    }

    const judgeConfig: CompiledJudgeConfig = {
      workers: strategy.roleBindings
        .filter(binding => binding.role !== 'JUDGE')
        .map(binding => getModelProfile(binding.modelProfileId).modelName),
      judge: getModelProfile(judgeBinding.modelProfileId).modelName,
      gatePolicy: strategy.gatePolicy,
      strategyId,
    }
    return { reviewMode: strategy.reviewMode, modelsConfig: judgeConfig }
  }

  if (strategy.reviewMode === 'MULTI') {
    const multiConfig: CompiledMultiModelConfig = {
      models: modelNames,
      gatePolicy: strategy.gatePolicy,
      strategyId,
    }
    return { reviewMode: strategy.reviewMode, modelsConfig: multiConfig }
  }

  const singleConfig: CompiledMultiModelConfig = {
    models: modelNames.slice(0, 1),
    gatePolicy: strategy.gatePolicy,
    strategyId,
  }
  return { reviewMode: strategy.reviewMode, modelsConfig: singleConfig as CompiledModelsConfig }
}
