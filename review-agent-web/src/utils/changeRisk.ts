import type { RiskAssessment } from '~/types/risk'

export function getRiskColor(level: string): string {
  switch (level) {
    case 'CRITICAL': return 'red'
    case 'HIGH': return 'orange'
    case 'MEDIUM': return 'yellow'
    case 'LOW': return 'green'
    default: return 'gray'
  }
}

export function getRiskLabel(level: string): string {
  switch (level) {
    case 'CRITICAL': return '严重'
    case 'HIGH': return '高'
    case 'MEDIUM': return '中'
    case 'LOW': return '低'
    default: return '未知'
  }
}

export function computeRiskTrend(assessments: RiskAssessment[]): 'improving' | 'stable' | 'worsening' {
  if (assessments.length < 2) return 'stable'
  const latest = assessments[assessments.length - 1].score
  const previous = assessments[assessments.length - 2].score
  if (latest < previous) return 'improving'
  if (latest > previous) return 'worsening'
  return 'stable'
}
