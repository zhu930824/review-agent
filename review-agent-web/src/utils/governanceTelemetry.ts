import type { TelemetryReadinessItem, TelemetryReadinessLevel } from '../types/operations'
import type { CiIntegrationHealthVO } from '../types/governance'

export interface TelemetryGapAction {
  key: string
  strategyKey: string
  readinessLevel: TelemetryReadinessLevel
  gapCode: string
  title: string
  recommendation: string
}

export interface CiHealthAction {
  key: string
  connectorKey: string
  provider: string
  healthStatus: string
  severity: 'critical' | 'warning' | 'info'
  title: string
  recommendation: string
  latestSignal: string
}

export function buildTelemetryGapActions(items: readonly TelemetryReadinessItem[] = []): TelemetryGapAction[] {
  return items
    .filter(item => item.gapCode && item.gapCode !== 'NONE')
    .map(item => ({
      key: `${item.strategyKey}-${item.gapCode}`,
      strategyKey: item.strategyKey,
      readinessLevel: item.readinessLevel,
      gapCode: item.gapCode,
      title: telemetryGapTitle(item.gapCode),
      recommendation: item.recommendation,
    }))
}

export function buildCiHealthActions(items: readonly CiIntegrationHealthVO[] = []): CiHealthAction[] {
  return items
    .filter(item => item.healthStatus !== 'HEALTHY')
    .map(item => ({
      key: `${item.connectorKey}-${item.healthStatus}`,
      connectorKey: item.connectorKey,
      provider: item.provider,
      healthStatus: item.healthStatus,
      severity: ciHealthActionSeverity(item.healthStatus),
      title: ciHealthActionTitle(item),
      recommendation: ciHealthActionRecommendation(item),
      latestSignal: ciHealthLatestSignal(item),
    }))
}

export function ciHealthActionSeverity(status: string): 'critical' | 'warning' | 'info' {
  if (status === 'UNHEALTHY') return 'critical'
  if (status === 'DEGRADED') return 'warning'
  return 'info'
}

export function ciHealthActionColor(severity: CiHealthAction['severity']): string {
  return {
    critical: 'red',
    warning: 'orange',
    info: 'default',
  }[severity]
}

function ciHealthActionTitle(item: CiIntegrationHealthVO): string {
  if (item.healthStatus === 'UNHEALTHY') {
    return `${item.provider} writeback is failing`
  }
  if (item.healthStatus === 'DEGRADED') {
    return `${item.provider} writeback needs attention`
  }
  return `${item.provider} writeback has no recent evidence`
}

function ciHealthActionRecommendation(item: CiIntegrationHealthVO): string {
  if (item.healthStatus === 'UNHEALTHY') {
    return `Check ${item.connectorKey} credentials, repository binding, and the latest failed writeback before the next release gate.`
  }
  if (item.healthStatus === 'DEGRADED') {
    return `Review recent ${item.connectorKey} retries or external build state, then refresh Jenkins results if this is a pipeline connector.`
  }
  return `Run a gate publish smoke test for ${item.connectorKey} so the workbench has fresh CI evidence.`
}

function ciHealthLatestSignal(item: CiIntegrationHealthVO): string {
  const writeback = item.latestWritebackStatus || 'no writeback'
  return item.latestExternalResult ? `${writeback} / ${item.latestExternalResult}` : writeback
}

export function telemetryGapTitle(gapCode: string): string {
  return {
    NO_TELEMETRY: '接入模型调用遥测',
    WEAK_ATTRIBUTION: '补齐跨模型归因',
    JUDGE_FAILURE: '修复 Judge 调用稳定性',
  }[gapCode] ?? '复核遥测接入'
}

export function telemetryReadinessColor(level: TelemetryReadinessLevel): string {
  return {
    READY: 'green',
    NEEDS_ATTRIBUTION: 'orange',
    JUDGE_UNSTABLE: 'red',
    NOT_CONNECTED: 'default',
  }[level]
}
