import type { ModelCallHealth, ModelCallHealthTone, ModelCallStats } from '../types/operations'

const DEFAULT_USD_PER_1K_TOKENS = 0.002

export function formatTokenCount(tokens = 0): string {
  if (tokens >= 1_000_000) return `${(tokens / 1_000_000).toFixed(1)}M`
  if (tokens >= 10_000) return `${(tokens / 1_000).toFixed(1)}K`
  return String(tokens)
}

export function deriveModelCallHealth(
  stats: ModelCallStats | null | undefined,
  usdPer1kTokens = DEFAULT_USD_PER_1K_TOKENS,
): ModelCallHealth {
  const totalCalls = Math.max(0, stats?.totalCalls ?? 0)
  const totalTokens = Math.max(0, stats?.totalTokens ?? 0)
  const failedCalls = Math.max(0, Math.min(stats?.failedCalls ?? 0, totalCalls))
  const avgLatencyMs = Math.max(0, stats?.avgLatencyMs ?? 0)
  const failureRate = totalCalls ? Math.round((failedCalls / totalCalls) * 100) : 0
  const successRate = totalCalls ? Math.max(0, 100 - failureRate) : 100
  const latencyPenalty = getLatencyPenalty(avgLatencyMs)
  const healthScore = Math.max(0, Math.min(100, successRate - latencyPenalty))
  const estimatedCostUsd = Number(((totalTokens / 1000) * usdPer1kTokens).toFixed(4))
  const tone = getModelCallHealthTone(failureRate, avgLatencyMs)

  return {
    totalCalls,
    totalTokens,
    failedCalls,
    avgLatencyMs,
    failureRate,
    successRate,
    healthScore,
    estimatedCostUsd,
    formattedTokens: formatTokenCount(totalTokens),
    tone,
    summary: buildHealthSummary(totalCalls, failureRate, avgLatencyMs, estimatedCostUsd),
  }
}

function getLatencyPenalty(avgLatencyMs: number): number {
  if (avgLatencyMs >= 5000) return 25
  if (avgLatencyMs >= 3000) return 16
  if (avgLatencyMs >= 1500) return 8
  return 0
}

function getModelCallHealthTone(failureRate: number, avgLatencyMs: number): ModelCallHealthTone {
  if (failureRate >= 10 || avgLatencyMs >= 5000) return 'critical'
  if (failureRate >= 3 || avgLatencyMs >= 1500) return 'warning'
  return 'healthy'
}

function buildHealthSummary(totalCalls: number, failureRate: number, avgLatencyMs: number, estimatedCostUsd: number): string {
  if (!totalCalls) return '暂无模型调用记录，等待真实审查产生运行指标。'
  return `近 ${totalCalls} 次模型调用失败率 ${failureRate}%，平均耗时 ${Math.round(avgLatencyMs)}ms，预估成本 $${estimatedCostUsd.toFixed(4)}。`
}
