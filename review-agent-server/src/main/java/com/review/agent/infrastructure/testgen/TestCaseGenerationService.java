package com.review.agent.infrastructure.testgen;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseGenerationService {

    public TestCoveragePlan generate(Long reviewId, List<ReviewFinding> findings,
                                      List<String> changedFiles) {
        List<TestCase> cases = new ArrayList<>();

        Set<String> highRiskFiles = findings.stream()
                .filter(f -> f.getSeverity() == Severity.BLOCKER
                        || (f.getCategory() == FindingCategory.SECURITY && f.getSeverity() == Severity.MAJOR))
                .map(ReviewFinding::getFilePath)
                .filter(f -> f != null)
                .collect(Collectors.toSet());

        for (String filePath : changedFiles) {
            cases.addAll(generateForFile(filePath, highRiskFiles.contains(filePath), findings));
        }

        int highRisk = (int) cases.stream().filter(c -> "HIGH".equals(c.getRiskLevel())).count();
        int mediumRisk = (int) cases.stream().filter(c -> "MEDIUM".equals(c.getRiskLevel())).count();
        int lowRisk = cases.size() - highRisk - mediumRisk;

        return TestCoveragePlan.builder()
                .reviewId(String.valueOf(reviewId))
                .totalInterfaces(changedFiles.size())
                .highRiskCount(highRisk)
                .mediumRiskCount(mediumRisk)
                .lowRiskCount(lowRisk)
                .testCases(cases)
                .uncoveredPaths(List.of())
                .coverageSummary(String.format("共生成 %d 个测试用例，覆盖 %d 个变更文件（高风险 %d / 中风险 %d / 低风险 %d）",
                        cases.size(), changedFiles.size(), highRisk, mediumRisk, lowRisk))
                .build();
    }

    private List<TestCase> generateForFile(String filePath, boolean highRisk,
                                            List<ReviewFinding> findings) {
        List<TestCase> cases = new ArrayList<>();
        String methodName = extractMethodName(filePath);

        cases.add(TestCase.builder()
                .testName("shouldReturnExpectedResult_" + methodName)
                .targetMethod(methodName)
                .testLevel(highRisk ? "INTEGRATION" : "UNIT")
                .riskLevel(highRisk ? "HIGH" : "MEDIUM")
                .inputs(List.of("validInput"))
                .expectedOutput("expectedResult")
                .preconditions("系统环境正常")
                .testCategory(highRisk ? "核心路径" : "正常流程")
                .build());

        if (highRisk) {
            cases.add(TestCase.builder()
                    .testName("shouldHandleNullInput_" + methodName)
                    .targetMethod(methodName)
                    .testLevel("UNIT")
                    .riskLevel("HIGH")
                    .inputs(List.of("null"))
                    .expectedOutput("抛出 IllegalArgumentException")
                    .preconditions("无")
                    .testCategory("异常分支")
                    .build());

            cases.add(TestCase.builder()
                    .testName("shouldHandleBoundaryValue_" + methodName)
                    .targetMethod(methodName)
                    .testLevel("UNIT")
                    .riskLevel("HIGH")
                    .inputs(List.of("边界值"))
                    .expectedOutput("正确处理边界情况")
                    .preconditions("无")
                    .testCategory("边界条件")
                    .build());
        }
        return cases;
    }

    private String extractMethodName(String filePath) {
        if (filePath == null) return "unknownMethod";
        String name = filePath.substring(filePath.lastIndexOf('/') + 1);
        return name.replace(".java", "");
    }
}
