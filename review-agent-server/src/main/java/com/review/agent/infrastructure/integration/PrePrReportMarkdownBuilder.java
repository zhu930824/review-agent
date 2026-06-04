package com.review.agent.infrastructure.integration;

import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import com.review.agent.domain.dto.ReviewVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrePrReportMarkdownBuilder {

    public String build(ReviewDetailVO detail, PrePrGateVO gate) {
        ReviewVO review = detail.getReview();
        List<ReviewFindingVO> findings = detail.getFindings() == null ? List.of() : detail.getFindings();
        List<String> blockedReasons = blockedReasons(detail, gate);
        String gateStatus = gate != null && gate.getGateStatus() != null ? gate.getGateStatus() : detail.getPrePrStatus();

        return String.join("\n",
                "# " + valueOrDefault(review.getProjectName(), "Review Agent") + " Pre-PR 审查报告",
                "",
                "## 概览",
                "",
                "- 项目：" + valueOrDefault(review.getProjectName(), "-"),
                "- Review ID：" + valueOrDefault(review.getId(), "-"),
                "- 分支：" + valueOrDefault(review.getSourceBranch(), "-") + " -> " + valueOrDefault(review.getTargetBranch(), "-"),
                "- 模式：" + valueOrDefault(review.getReviewMode(), "-"),
                "- 状态：" + valueOrDefault(review.getStatus(), "-"),
                "- Gate：" + gateLabel(gateStatus),
                "",
                "## 风险统计",
                "",
                "- 总问题：" + detail.getTotalFindings(),
                "- BLOCKER：" + detail.getBlockerCount(),
                "- MAJOR：" + detail.getMajorCount(),
                "- MINOR：" + detail.getMinorCount(),
                "- INFO：" + detail.getInfoCount(),
                "",
                "## 阻断原因",
                "",
                blockedReasons.isEmpty() ? "无" : blockedReasons.stream().map(reason -> "- " + reason).collect(Collectors.joining("\n")),
                "",
                "## 主要发现",
                "",
                findings.isEmpty() ? "无" : findings.stream().limit(20).map(this::findingLine).collect(Collectors.joining("\n\n")),
                "",
                "## Gate 摘要",
                "",
                gate != null && gate.getSummary() != null ? gate.getSummary() : valueOrDefault(review.getSummary(), "无"),
                "");
    }

    private List<String> blockedReasons(ReviewDetailVO detail, PrePrGateVO gate) {
        if (gate != null && gate.getBlockedReasons() != null && !gate.getBlockedReasons().isEmpty()) {
            return gate.getBlockedReasons();
        }
        return detail.getBlockedReasons() == null ? List.of() : detail.getBlockedReasons();
    }

    private String findingLine(ReviewFindingVO finding) {
        return List.of(
                        "- [" + valueOrDefault(finding.getSeverity(), "-") + "] " + valueOrDefault(finding.getTitle(), "-"),
                        "  - 位置：" + lineRef(finding),
                        finding.getCategory() == null ? "" : "  - 分类：" + finding.getCategory(),
                        finding.getModelName() == null ? "" : "  - 模型：" + finding.getModelName(),
                        finding.getDescription() == null ? "" : "  - 描述：" + finding.getDescription(),
                        finding.getSuggestion() == null ? "" : "  - 建议：" + finding.getSuggestion())
                .stream()
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private String lineRef(ReviewFindingVO finding) {
        String filePath = valueOrDefault(finding.getFilePath(), "-");
        if (finding.getLineStart() == null) {
            return filePath;
        }
        if (finding.getLineEnd() == null || finding.getLineEnd().equals(finding.getLineStart())) {
            return filePath + ":" + finding.getLineStart();
        }
        return filePath + ":" + finding.getLineStart() + "-" + finding.getLineEnd();
    }

    private String gateLabel(String status) {
        return switch (status == null ? "" : status) {
            case "PASSED" -> "已通过";
            case "BLOCKED" -> "已阻断";
            case "NEEDS_HUMAN_REVIEW" -> "待人工确认";
            case "RUNNING" -> "执行中";
            default -> valueOrDefault(status, "-");
        };
    }

    private String valueOrDefault(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }
}
