package com.review.agent.infrastructure.integration;

import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import com.review.agent.domain.dto.ReviewVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.ReviewMode;
import com.review.agent.domain.enums.ReviewStatus;
import com.review.agent.domain.enums.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrePrReportMarkdownBuilderTest {

    @Test
    void buildsReadableMarkdownReportFromReviewDetailAndGate() {
        PrePrReportMarkdownBuilder builder = new PrePrReportMarkdownBuilder();

        String markdown = builder.build(detail(), gate());

        assertTrue(markdown.contains("# Review Agent Pre-PR 审查报告"));
        assertTrue(markdown.contains("- 项目：Review Agent"));
        assertTrue(markdown.contains("- 分支：feature/pre-pr -> main"));
        assertTrue(markdown.contains("- Gate：已阻断"));
        assertTrue(markdown.contains("- Token 未校验过期时间"));
        assertTrue(markdown.contains("- [BLOCKER] Token 未校验过期时间"));
        assertTrue(markdown.contains("  - 位置：src/Auth.java:42-43"));
        assertTrue(markdown.contains("  - 建议：增加过期时间校验。"));
    }

    private static ReviewDetailVO detail() {
        ReviewVO review = new ReviewVO();
        review.setId(18L);
        review.setProjectName("Review Agent");
        review.setSourceBranch("feature/pre-pr");
        review.setTargetBranch("main");
        review.setReviewMode(ReviewMode.AGENT);
        review.setStatus(ReviewStatus.COMPLETED);

        ReviewFindingVO finding = new ReviewFindingVO();
        finding.setFilePath("src/Auth.java");
        finding.setLineStart(42);
        finding.setLineEnd(43);
        finding.setSeverity(Severity.BLOCKER);
        finding.setCategory(FindingCategory.SECURITY);
        finding.setTitle("Token 未校验过期时间");
        finding.setDescription("JWT 解析后没有校验 exp。");
        finding.setSuggestion("增加过期时间校验。");
        finding.setModelName("qwen-plus");

        ReviewDetailVO detail = new ReviewDetailVO();
        detail.setReview(review);
        detail.setFindings(List.of(finding));
        detail.setTotalFindings(1);
        detail.setBlockerCount(1);
        detail.setMajorCount(0);
        detail.setMinorCount(0);
        detail.setInfoCount(0);
        detail.setPrePrStatus("BLOCKED");
        detail.setBlockedReasons(List.of("Token 未校验过期时间"));
        return detail;
    }

    private static PrePrGateVO gate() {
        PrePrGateVO gate = new PrePrGateVO();
        gate.setGateStatus("BLOCKED");
        gate.setSummary("存在阻断风险。");
        gate.setBlockedReasons(List.of("Token 未校验过期时间"));
        return gate;
    }
}
