package com.review.agent.service;

import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewSummaryMarkdownBuilder {

    public String build(ReviewDetailVO detail) {
        if (detail == null || detail.getReview() == null) {
            throw new IllegalArgumentException("Review detail is required");
        }
        List<ReviewFindingVO> findings = detail.getFindings() == null ? List.of() : detail.getFindings();
        String topFindings = findings.stream()
                .limit(5)
                .map(this::findingLine)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- No significant findings were selected for this summary");
        String summary = detail.getReview().getSummary();
        if (summary == null || summary.isBlank()) {
            summary = "Review completed without a generated narrative summary.";
        }
        String gate = detail.getPrePrStatus() == null ? "UNKNOWN" : detail.getPrePrStatus();
        return "## Review Agent Summary\n\n"
                + "- Project: " + safe(detail.getReview().getProjectName()) + "\n"
                + "- Branches: " + safe(detail.getReview().getSourceBranch()) + " -> "
                + safe(detail.getReview().getTargetBranch()) + "\n"
                + "- Gate: " + gate + "\n"
                + "- Findings: BLOCKER " + detail.getBlockerCount()
                + ", MAJOR " + detail.getMajorCount()
                + ", MINOR " + detail.getMinorCount()
                + ", INFO " + detail.getInfoCount() + "\n\n"
                + "### Summary\n" + summary + "\n\n"
                + "### Top Findings\n" + topFindings + "\n\n"
                + "_Generated from Review Agent review #" + detail.getReview().getId() + "_";
    }

    private String findingLine(ReviewFindingVO finding) {
        String severity = finding.getSeverity() == null ? "UNKNOWN" : finding.getSeverity().name();
        String line = finding.getLineStart() == null ? "-" : String.valueOf(finding.getLineStart());
        return "- [" + severity + "] " + safe(finding.getTitle())
                + " (" + safe(finding.getFilePath()) + ":" + line + ")";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
