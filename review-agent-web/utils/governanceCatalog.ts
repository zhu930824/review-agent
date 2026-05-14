import type {
  CapabilityCoverageSummary,
  CompiledGovernancePolicyPack,
  GovernanceRulePack,
  IntegrationConnector,
  MarketCapability,
  RecommendedAction,
  RolloutStage,
  WorkflowTemplate,
} from '../types/governance'

const impactScore = {
  HIGH: 3,
  MEDIUM: 2,
  LOW: 1,
} as const

const statusScore = {
  ENABLED: 0,
  PARTIAL: 1,
  PLANNED: 2,
} as const

export const marketCapabilities: MarketCapability[] = [
  {
    id: 'pr-summary',
    name: 'PR 变更摘要',
    category: 'AI_REVIEW',
    status: 'ENABLED',
    businessImpact: 'HIGH',
    readiness: 92,
    marketSignal: '主流 AI Review 产品会在 PR 中解释变更范围、风险和测试影响。',
    platformMove: '用现有多模型策略生成摘要，沉淀为审查记录和后续发布说明素材。',
    sourceProducts: ['CodeRabbit', 'Qodo Merge', 'GitHub Copilot'],
  },
  {
    id: 'inline-review',
    name: '行级审查建议',
    category: 'AI_REVIEW',
    status: 'ENABLED',
    businessImpact: 'HIGH',
    readiness: 88,
    marketSignal: 'PR 评论需要落到具体文件、行号和可执行建议。',
    platformMove: '继续使用 FindingCard 和 diff 视图承载问题、证据和修复建议。',
    sourceProducts: ['CodeRabbit', 'Qodo Merge', 'GitHub Copilot'],
  },
  {
    id: 'ask-on-pr',
    name: 'PR 问答与追问',
    category: 'AI_REVIEW',
    status: 'PARTIAL',
    businessImpact: 'MEDIUM',
    readiness: 58,
    marketSignal: '成熟产品支持在 PR 评论里追问原因、影响面和替代方案。',
    platformMove: '下一步把审查上下文、项目文档和模型结果组成 Ask API。',
    sourceProducts: ['CodeRabbit', 'Qodo Merge'],
  },
  {
    id: 'autofix-workflow',
    name: '自动修复工作流',
    category: 'AI_REVIEW',
    status: 'PLANNED',
    businessImpact: 'HIGH',
    readiness: 36,
    marketSignal: '市场正在把“发现问题”推进到“生成修复 PR”。',
    platformMove: '先做修复草案和人工确认，再接分支提交或独立修复 PR。',
    sourceProducts: ['CodeRabbit', 'GitHub Copilot', 'Snyk'],
  },
  {
    id: 'quality-gate',
    name: '质量门禁',
    category: 'QUALITY',
    status: 'ENABLED',
    businessImpact: 'HIGH',
    readiness: 90,
    marketSignal: '质量门禁会按可靠性、安全性、可维护性和新代码风险阻断合并。',
    platformMove: '把 BLOCKER、MAJOR 人工确认和 Pre-PR 通过率转化为发布准入规则。',
    sourceProducts: ['SonarQube', 'Snyk'],
  },
  {
    id: 'new-code-policy',
    name: '新代码策略',
    category: 'QUALITY',
    status: 'PARTIAL',
    businessImpact: 'HIGH',
    readiness: 64,
    marketSignal: '优秀质量平台强调只让新增变更满足更高标准，降低历史债务噪音。',
    platformMove: '以 source/target diff 为核心，只对本次变更给出阻断和 SLA。',
    sourceProducts: ['SonarQube'],
  },
  {
    id: 'security-scan',
    name: '安全扫描与风险分级',
    category: 'SECURITY',
    status: 'PARTIAL',
    businessImpact: 'HIGH',
    readiness: 66,
    marketSignal: '安全产品在 PR 中阻止新增漏洞，并按严重级别汇总。',
    platformMove: '把 SecuritySkill 结果映射到漏洞、热点、依赖风险和人工确认。',
    sourceProducts: ['Snyk', 'SonarQube', 'CodeRabbit'],
  },
  {
    id: 'secret-dependency-risk',
    name: '密钥与依赖风险',
    category: 'SECURITY',
    status: 'PLANNED',
    businessImpact: 'HIGH',
    readiness: 42,
    marketSignal: '业务真正关心的是密钥泄露、供应链风险和高危依赖能否进入主干。',
    platformMove: '新增 secret/dependency rule pack，并预留 SCA 工具结果导入。',
    sourceProducts: ['Snyk', 'GitHub Code Scanning'],
  },
  {
    id: 'sarif-export',
    name: 'SARIF 与代码扫描导出',
    category: 'INTEGRATION',
    status: 'PLANNED',
    businessImpact: 'MEDIUM',
    readiness: 48,
    marketSignal: '代码扫描生态用 SARIF 把外部工具告警带回 PR 检查。',
    platformMove: '把 findings 导出 SARIF，供 GitHub code scanning 和安全平台消费。',
    sourceProducts: ['GitHub Code Scanning', 'Snyk'],
  },
  {
    id: 'ci-status-check',
    name: 'CI 状态检查',
    category: 'INTEGRATION',
    status: 'PLANNED',
    businessImpact: 'HIGH',
    readiness: 52,
    marketSignal: 'PR 检查必须能以 pass/fail 形式进入分支保护。',
    platformMove: '提供 review gate callback，写回 GitHub/GitLab status check。',
    sourceProducts: ['GitHub', 'Snyk', 'SonarQube'],
  },
  {
    id: 'team-rule-memory',
    name: '团队规则记忆',
    category: 'KNOWLEDGE',
    status: 'PARTIAL',
    businessImpact: 'MEDIUM',
    readiness: 60,
    marketSignal: '可配置规则和最佳实践决定 AI 审查是否贴近真实团队。',
    platformMove: '把 doc、SOP、项目规则和历史人工确认转成可审计规则包。',
    sourceProducts: ['Qodo Merge', 'CodeRabbit'],
  },
  {
    id: 'review-analytics',
    name: '审查效能分析',
    category: 'ANALYTICS',
    status: 'ENABLED',
    businessImpact: 'MEDIUM',
    readiness: 76,
    marketSignal: '管理者需要看到阻断原因、模型稳定性和交付节奏。',
    platformMove: '现有驾驶舱继续扩展到团队、项目、规则包和模型维度。',
    sourceProducts: ['CodeRabbit', 'SonarQube', 'Snyk'],
  },
]

export const integrationConnectors: IntegrationConnector[] = [
  {
    id: 'local-repository',
    name: '本地仓库审查',
    stage: 'live',
    status: 'ENABLED',
    businessValue: '研发在发起 PR 前即可做自查，减少无效往返。',
    implementationHint: '继续复用当前项目、分支和差异审查流程。',
  },
  {
    id: 'github-checks',
    name: 'GitHub Checks / Status',
    stage: 'next',
    status: 'PLANNED',
    businessValue: '把 Review Agent 变成分支保护的一道可见门禁。',
    implementationHint: '新增 webhook 接收器、状态回写和 SARIF 上传任务。',
  },
  {
    id: 'gitlab-merge-request',
    name: 'GitLab Merge Request',
    stage: 'next',
    status: 'PLANNED',
    businessValue: '覆盖企业常见 GitLab 私有化工作流。',
    implementationHint: '适配 MR webhook、discussion thread 和 pipeline status。',
  },
  {
    id: 'sarif-code-scanning',
    name: 'SARIF 代码扫描',
    stage: 'next',
    status: 'PLANNED',
    businessValue: '让安全告警进入标准代码扫描视图，便于审计归档。',
    implementationHint: '将 finding severity、rule id、location 转换为 SARIF result。',
  },
  {
    id: 'dingtalk-slack',
    name: '钉钉 / Slack 通知',
    stage: 'later',
    status: 'PLANNED',
    businessValue: '阻断类问题及时通知责任人与团队频道。',
    implementationHint: '按项目配置通知策略，只推送 BLOCKER 和超 SLA 事项。',
  },
  {
    id: 'jira-linear-sync',
    name: 'Jira / Linear 事项同步',
    stage: 'later',
    status: 'PLANNED',
    businessValue: '把无法立即修复的技术债转成可跟踪工作项。',
    implementationHint: '从人工确认后的 finding 创建外部 issue，并回写状态。',
  },
]

export const governanceRulePacks: GovernanceRulePack[] = [
  {
    id: 'security-release',
    name: '安全发布门禁',
    businessOutcome: '防止新增高危漏洞、密钥和权限绕过进入主干。',
    controls: ['BLOCKER 必须阻断', 'MAJOR 必须安全负责人确认', '敏感接口需要测试证据'],
    capabilityIds: ['quality-gate', 'security-scan', 'secret-dependency-risk'],
    suggestedStrategies: ['quality-gate', 'architecture-board'],
    humanCheckpoints: ['security owner sign-off'],
  },
  {
    id: 'ai-generated-code',
    name: 'AI 生成代码保障',
    businessOutcome: '让 AI 生成代码满足更严格的新代码质量和安全标准。',
    controls: ['新代码必须走质量门禁', '异常处理与边界条件必须覆盖', '高风险建议需要人工确认'],
    capabilityIds: ['quality-gate', 'new-code-policy', 'team-rule-memory'],
    suggestedStrategies: ['quality-gate'],
    humanCheckpoints: ['tech lead confirmation'],
  },
  {
    id: 'architecture-boundary',
    name: '架构边界守护',
    businessOutcome: '避免跨层依赖、领域模型污染和不可控耦合。',
    controls: ['跨模块调用必须说明影响面', '核心链路变更需架构复核', '禁止绕过统一异常和权限入口'],
    capabilityIds: ['team-rule-memory', 'pr-summary', 'quality-gate'],
    suggestedStrategies: ['architecture-board'],
    humanCheckpoints: ['architect review'],
  },
  {
    id: 'delivery-velocity',
    name: '交付效率优化',
    businessOutcome: '降低 PR 往返次数，优先在提交前暴露可修复问题。',
    controls: ['小改动默认快速扫描', '中风险改动交叉审查', '摘要自动同步给 Reviewer'],
    capabilityIds: ['pr-summary', 'inline-review', 'review-analytics'],
    suggestedStrategies: ['fast-scan', 'cross-check'],
    humanCheckpoints: [],
  },
  {
    id: 'compliance-evidence',
    name: '审计证据留存',
    businessOutcome: '沉淀发布前审查证据，支撑安全、合规和客户审计。',
    controls: ['保留模型结果、人工确认和阻断原因', '导出标准扫描结果', '关键发布关联审批记录'],
    capabilityIds: ['sarif-export', 'review-analytics', 'quality-gate'],
    suggestedStrategies: ['quality-gate'],
    humanCheckpoints: ['release manager approval'],
  },
]

export const workflowTemplates: WorkflowTemplate[] = [
  {
    id: 'daily-pre-pr',
    name: '日常 Pre-PR 自查',
    scenario: '低风险需求、小修复、配置变更。',
    strategyId: 'fast-scan',
    rulePackIds: ['delivery-velocity'],
    integrationIds: ['local-repository'],
    successMetric: '10 分钟内完成审查，减少 Reviewer 基础问题反馈。',
  },
  {
    id: 'cross-model-review',
    name: '中风险交叉审查',
    scenario: '多人协作、业务逻辑变更、测试覆盖不充分的改动。',
    strategyId: 'cross-check',
    rulePackIds: ['delivery-velocity', 'ai-generated-code'],
    integrationIds: ['local-repository', 'gitlab-merge-request'],
    successMetric: '提高缺陷召回率，同时保持人工确认数量可控。',
  },
  {
    id: 'release-gate',
    name: '发布质量门禁',
    scenario: '正式 PR、核心链路、安全敏感发布。',
    strategyId: 'quality-gate',
    rulePackIds: ['security-release', 'ai-generated-code', 'compliance-evidence'],
    integrationIds: ['github-checks', 'sarif-code-scanning'],
    successMetric: '阻断新增 BLOCKER，并沉淀安全与质量证据。',
  },
  {
    id: 'architecture-board',
    name: '架构委员会评审',
    scenario: '领域边界、公共组件、数据模型或跨仓库契约调整。',
    strategyId: 'architecture-board',
    rulePackIds: ['architecture-boundary', 'compliance-evidence'],
    integrationIds: ['local-repository', 'jira-linear-sync'],
    successMetric: '把架构影响面、风险裁决和后续事项集中归档。',
  },
]

export function getCapabilityCoverageSummary(): CapabilityCoverageSummary {
  const enabled = marketCapabilities.filter(item => item.status === 'ENABLED').length
  const partial = marketCapabilities.filter(item => item.status === 'PARTIAL').length
  const planned = marketCapabilities.filter(item => item.status === 'PLANNED').length
  const total = marketCapabilities.length

  return {
    total,
    enabled,
    partial,
    planned,
    coveragePercent: Math.round(((enabled + partial * 0.5) / total) * 100),
  }
}

export function getRecommendedNextActions(limit = 5): RecommendedAction[] {
  return marketCapabilities
    .filter(item => item.status !== 'ENABLED' && item.businessImpact !== 'LOW')
    .map(item => ({
      ...item,
      priorityScore: impactScore[item.businessImpact] * 100 + statusScore[item.status] * 20 + item.readiness,
    }))
    .sort((left, right) => right.priorityScore - left.priorityScore)
    .slice(0, limit)
}

export function getConnectorsByStage(): Record<RolloutStage, IntegrationConnector[]> {
  return integrationConnectors.reduce(
    (groups, connector) => {
      groups[connector.stage].push(connector)
      return groups
    },
    { live: [], next: [], later: [] } as Record<RolloutStage, IntegrationConnector[]>,
  )
}

export function compileGovernancePolicyPack(packIds: string[]): CompiledGovernancePolicyPack {
  const selectedPacks = packIds.map(packId => {
    const pack = governanceRulePacks.find(item => item.id === packId)
    if (!pack) {
      throw new Error(`Unknown governance rule pack: ${packId}`)
    }
    return pack
  })

  return {
    packIds,
    requiredCapabilities: unique(selectedPacks.flatMap(pack => pack.capabilityIds)),
    suggestedStrategies: unique(selectedPacks.flatMap(pack => pack.suggestedStrategies)),
    requiredHumanCheckpoints: unique(selectedPacks.flatMap(pack => pack.humanCheckpoints)),
    controls: unique(selectedPacks.flatMap(pack => pack.controls)),
  }
}

function unique<T>(items: T[]): T[] {
  return Array.from(new Set(items))
}
