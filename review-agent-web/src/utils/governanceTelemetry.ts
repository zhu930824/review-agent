import type { TelemetryReadinessItem, TelemetryReadinessLevel } from '../types/operations'

export interface TelemetryGapAction {
  key: string
  strategyKey: string
  readinessLevel: TelemetryReadinessLevel
  gapCode: string
  title: string
  recommendation: string
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
