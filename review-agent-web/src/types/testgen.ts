export interface TestCase {
  testName: string
  targetMethod: string
  testLevel: string
  riskLevel: string
  inputs: string[]
  expectedOutput: string
  preconditions: string
  testCategory: string
}

export interface TestCoveragePlan {
  reviewId: string
  totalInterfaces: number
  highRiskCount: number
  mediumRiskCount: number
  lowRiskCount: number
  testCases: TestCase[]
  uncoveredPaths: string[]
  coverageSummary: string
}
