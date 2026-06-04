export interface ReviewCreateRoleBinding {
  role: string
  modelProfileName?: string
}

export interface ReviewCreateStrategy {
  reviewMode: string
  blockOn?: string[]
  requireHumanReviewOn?: string[]
  advisoryOn?: string[]
  roleBindings?: ReviewCreateRoleBinding[]
}

export interface ReviewCreateForm {
  projectId: string
  sourceBranch: string
  targetBranch: string
  mcpEnabled: boolean
  modelsConfigOverride?: string
}

export interface CompiledReviewCreateConfig {
  reviewMode: string
  modelsConfig: {
    agents: Array<{ role: string; modelName: string; skills: string[]; temperature: number }>
    orchestrationStrategy: 'PARALLEL'
    mcpEnabled: boolean
    gatePolicy: {
      blockOn: string[]
      requireHumanReviewOn: string[]
      advisoryOn: string[]
    }
  }
}

export interface ReviewCreatePayload {
  projectId: number
  sourceBranch: string
  targetBranch: string
  reviewMode: string
  modelsConfig: string
}

const roleLabels: Record<string, string> = {
  WORKER: '审查模型',
  JUDGE: 'Judge 模型',
  SECURITY_AUDITOR: '安全审计员',
  PERFORMANCE_ANALYST: '性能分析员',
  CODE_STYLE_CHECKER: '代码规范检查员',
  EXCEPTION_HANDLER: '异常处理专家',
  ARCHITECT_REVIEWER: '架构评审员',
}

const modelNameMap: Record<string, string> = {
  '通义千问 Plus': 'qwen-plus',
  '通义千问 Max': 'qwen-max',
  'DeepSeek V3': 'deepseek-v3',
  'Kimi K2': 'kimi-k2',
  本地代码模型: 'local-coder',
}

export function getReviewCreateRoleLabels(strategy: ReviewCreateStrategy | undefined): string[] {
  return strategy?.roleBindings?.map(binding => {
    const roleLabel = roleLabels[binding.role] || binding.role
    return `${roleLabel} · ${binding.modelProfileName || '未知模型'}`
  }) || []
}

export function getReviewCreateModelName(displayName: string | undefined): string {
  if (!displayName) return 'qwen-plus'
  return modelNameMap[displayName] || displayName.toLowerCase().replace(/\s+/g, '-')
}

export function compileReviewCreateConfig(
  strategy: ReviewCreateStrategy | undefined,
  mcpEnabled: boolean,
): CompiledReviewCreateConfig {
  const gatePolicy = {
    blockOn: strategy?.blockOn || [],
    requireHumanReviewOn: strategy?.requireHumanReviewOn || [],
    advisoryOn: strategy?.advisoryOn || [],
  }

  return {
    reviewMode: strategy?.reviewMode || 'AGENT',
    modelsConfig: {
      agents: strategy?.roleBindings?.map(binding => ({
        role: binding.role,
        modelName: getReviewCreateModelName(binding.modelProfileName),
        skills: [],
        temperature: 0.3,
      })) || [],
      orchestrationStrategy: 'PARALLEL',
      mcpEnabled,
      gatePolicy,
    },
  }
}

export function buildReviewCreatePayload(
  form: ReviewCreateForm,
  strategy: ReviewCreateStrategy,
): ReviewCreatePayload {
  const compiled = compileReviewCreateConfig(strategy, form.mcpEnabled)
  const modelsConfigOverride = form.modelsConfigOverride?.trim()

  return {
    projectId: Number(form.projectId),
    sourceBranch: form.sourceBranch,
    targetBranch: form.targetBranch,
    reviewMode: compiled.reviewMode,
    modelsConfig: modelsConfigOverride || JSON.stringify(compiled.modelsConfig),
  }
}

export function getReviewCreateEndpoint(prePr: boolean): string {
  return prePr ? '/reviews/pre-pr' : '/reviews'
}
