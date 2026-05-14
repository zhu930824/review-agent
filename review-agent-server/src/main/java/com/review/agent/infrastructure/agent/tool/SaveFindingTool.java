package com.review.agent.infrastructure.agent.tool;

import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveFindingTool {

    private final ReviewFindingMapper reviewFindingMapper;

    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("0.85");

    @Tool(description = "将代码审查发现的问题持久化到数据库。调用此工具后，问题将被保存并可在审查报告中查看。"
            + "category取值为: CODE_STYLE/BUG/PERFORMANCE/SECURITY/EXCEPTION_HANDLING/OTHER; "
            + "severity取值为: BLOCKER/MAJOR/MINOR/INFO")
    public String saveFinding(
            @ToolParam(description = "审查任务ID") Long reviewId,
            @ToolParam(description = "文件路径") String filePath,
            @ToolParam(description = "问题起始行号") Integer lineStart,
            @ToolParam(description = "问题结束行号") Integer lineEnd,
            @ToolParam(description = "问题类别") String category,
            @ToolParam(description = "严重程度") String severity,
            @ToolParam(description = "问题标题") String title,
            @ToolParam(description = "问题描述") String description,
            @ToolParam(description = "修复建议") String suggestion,
            @ToolParam(description = "发现此问题的Agent名称") String agentName) {

        try {
            ReviewFinding finding = new ReviewFinding();
            finding.setReviewId(reviewId);
            finding.setFilePath(filePath);
            finding.setLineStart(lineStart);
            finding.setLineEnd(lineEnd);
            finding.setCategory(FindingCategory.valueOf(category));
            finding.setSeverity(Severity.valueOf(severity));
            finding.setTitle(title);
            finding.setDescription(description);
            finding.setSuggestion(suggestion);
            finding.setModelName(agentName);
            finding.setConfidence(DEFAULT_CONFIDENCE);
            finding.setIsCrossHit(false);
            finding.setHumanStatus(HumanStatus.PENDING);
            reviewFindingMapper.insert(finding);
            return "问题已保存: " + title + " (ID=" + finding.getId() + ")";
        } catch (Exception e) {
            log.error("保存 finding 失败", e);
            return "保存失败: " + e.getMessage();
        }
    }
}
