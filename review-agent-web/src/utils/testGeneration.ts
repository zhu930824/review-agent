import type { TestCase } from '~/types/testgen'

export function groupByRiskLevel(cases: TestCase[]): Record<string, TestCase[]> {
  const groups: Record<string, TestCase[]> = { HIGH: [], MEDIUM: [], LOW: [] }
  for (const tc of cases) {
    const level = tc.riskLevel || 'MEDIUM'
    if (!groups[level]) groups[level] = []
    groups[level].push(tc)
  }
  return groups
}

export function groupByTestLevel(cases: TestCase[]): Record<string, TestCase[]> {
  const groups: Record<string, TestCase[]> = { UNIT: [], INTEGRATION: [], E2E: [] }
  for (const tc of cases) {
    const level = tc.testLevel || 'UNIT'
    if (!groups[level]) groups[level] = []
    groups[level].push(tc)
  }
  return groups
}

export function uniqueCategories(cases: TestCase[]): string[] {
  return [...new Set(cases.map(tc => tc.testCategory).filter(Boolean))]
}
