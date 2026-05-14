package com.review.agent.infrastructure.ai;

import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.dto.diff.Hunk;
import com.review.agent.domain.dto.diff.DiffLine;
import com.review.agent.domain.dto.diff.LineType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeReviewEngine {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 审查单个文件的变更
     */
    public List<ReviewFindingResult> reviewFile(FileChange fileChange, String modelName) {
        String prompt = buildPrompt(fileChange);
        ChatClient chatClient = chatClientBuilder.build();

        try {
            ReviewFindingList result = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .entity(ReviewFindingList.class);

            if (result == null || result.getFindings() == null) {
                return List.of();
            }

            // 标记来源模型和文件路径
            result.getFindings().forEach(f -> {
                f.setModelName(modelName);
                f.setFilePath(fileChange.getFilePath());
            });
            return result.getFindings();
        } catch (Exception e) {
            log.error("AI 审查失败: file={}, model={}", fileChange.getFilePath(), modelName, e);
            return List.of();
        }
    }

    /**
     * 批量审查多个文件
     */
    public List<ReviewFindingResult> reviewFiles(List<FileChange> fileChanges, String modelName) {
        List<ReviewFindingResult> allFindings = new ArrayList<>();
        for (FileChange fileChange : fileChanges) {
            List<ReviewFindingResult> findings = reviewFile(fileChange, modelName);
            allFindings.addAll(findings);
        }
        return allFindings;
    }

    private String buildPrompt(FileChange fileChange) {
        StringBuilder sb = new StringBuilder();
        sb.append("请审查以下代码变更：\n\n");
        sb.append("文件：").append(fileChange.getFilePath()).append("\n");
        sb.append("变更类型：").append(fileChange.getChangeType()).append("\n\n");

        for (Hunk hunk : fileChange.getHunks()) {
            sb.append("@@ -").append(hunk.getOldStart()).append(",")
              .append(hunk.getOldLines()).append(" +").append(hunk.getNewStart())
              .append(",").append(hunk.getNewLines()).append(" @@\n");

            for (DiffLine line : hunk.getLines()) {
                String prefix = switch (line.getType()) {
                    case ADDED -> "+";
                    case REMOVED -> "-";
                    case CONTEXT -> " ";
                };
                sb.append(prefix).append(line.getContent()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("请以 JSON 格式返回审查结果。");
        return sb.toString();
    }

    private static final String SYSTEM_PROMPT = """
            你是一位资深的代码审查专家。请对提交的代码变更进行审查，从以下维度发现问题：

            1. CODE_STYLE（代码风格）：命名规范、代码格式、注释质量
            2. BUG（潜在缺陷）：空指针、边界条件、类型转换、并发问题
            3. PERFORMANCE（性能问题）：不必要的对象创建、低效算法、资源泄漏
            4. SECURITY（安全问题）：SQL注入、XSS、敏感信息泄露、权限绕过
            5. EXCEPTION_HANDLING（异常处理）：未捕获异常、不当的异常吞没、缺少异常信息

            对每个发现的问题，请提供：
            - category: 问题类别（从上述5种中选择，不属于前5种选OTHER）
            - severity: 严重程度（BLOCKER/MAJOR/MINOR/INFO）
            - title: 问题标题（简短描述）
            - description: 问题描述（详细说明）
            - suggestion: 改进建议（具体修复方案）
            - lineStart: 问题起始行号（在新文件中的行号，不确定则为null）
            - lineEnd: 问题结束行号（在新文件中的行号，不确定则为null）

            严重程度标准：
            - BLOCKER: 安全漏洞、必现Bug、数据丢失风险
            - MAJOR: 可能导致功能异常、性能严重下降
            - MINOR: 代码质量问题，不影响功能
            - INFO: 优化建议、最佳实践推荐

            如果代码没有问题，返回空的 findings 数组。
            """;
}
