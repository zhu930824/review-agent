package com.review.agent.infrastructure.ai;

import com.review.agent.domain.dto.diff.FileChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReviewService {

    private final CodeReviewEngine codeReviewEngine;
    private final ChatClient.Builder chatClientBuilder;
    private final Executor reviewTaskExecutor;

    public List<ReviewFindingResult> reviewWithModel(List<FileChange> fileChanges, String modelName) {
        log.info("开始审查: model={}, fileCount={}", modelName, fileChanges.size());
        List<ReviewFindingResult> findings = codeReviewEngine.reviewFiles(fileChanges, modelName);
        log.info("审查完成: model={}, findingCount={}", modelName, findings.size());
        return findings;
    }

    /** 多模型并行审查同一份代码变更 */
    public List<ModelReviewResult> reviewWithModelsParallel(List<FileChange> fileChanges, List<String> modelNames) {
        List<CompletableFuture<ModelReviewResult>> futures = modelNames.stream()
                .map(modelName -> CompletableFuture.supplyAsync(
                        () -> {
                            List<ReviewFindingResult> findings = reviewWithModel(fileChanges, modelName);
                            return new ModelReviewResult(modelName, findings);
                        },
                        reviewTaskExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /** Judge 模式：Worker 模型先并行审查，Judge 模型评估审查质量 */
    public JudgeReviewResult reviewWithJudge(List<FileChange> fileChanges,
                                              List<String> workerModels,
                                              String judgeModel) {
        List<ModelReviewResult> workerResults = reviewWithModelsParallel(fileChanges, workerModels);
        JudgeReviewResult judgeResult = judgeReview(workerResults, fileChanges, judgeModel);
        return judgeResult;
    }

    /** 交叉验证：标记多个模型共同发现的问题为高置信度 */
    public List<ReviewFindingResult> crossValidate(List<ModelReviewResult> modelResults) {
        List<ReviewFindingResult> merged = new ArrayList<>();

        for (ModelReviewResult mr : modelResults) {
            for (ReviewFindingResult finding : mr.getFindings()) {
                boolean isCrossHit = isCrossHitFinding(finding, modelResults, mr.getModelName());
                finding.setCrossHit(isCrossHit);
                if (isCrossHit) {
                    finding.setConfidence(0.95);
                }
                merged.add(finding);
            }
        }

        return merged;
    }

    /** 判断某个 finding 是否被其他模型也发现 */
    private boolean isCrossHitFinding(ReviewFindingResult target,
                                       List<ModelReviewResult> allResults,
                                       String excludeModel) {
        return allResults.stream()
                .filter(mr -> !mr.getModelName().equals(excludeModel))
                .flatMap(mr -> mr.getFindings().stream())
                .anyMatch(other -> isSimilarFinding(target, other));
    }

    /** 同文件 + 同类别 + 行号相近（±5行内）视为相似发现 */
    private boolean isSimilarFinding(ReviewFindingResult a, ReviewFindingResult b) {
        if (!a.getFilePath().equals(b.getFilePath())) return false;
        if (a.getCategory() != b.getCategory()) return false;
        if (a.getLineStart() == null || b.getLineStart() == null) return false;
        return Math.abs(a.getLineStart() - b.getLineStart()) <= 5;
    }

    /** 使用 Judge 模型评估 Worker 的审查结果 */
    private JudgeReviewResult judgeReview(List<ModelReviewResult> workerResults,
                                           List<FileChange> fileChanges,
                                           String judgeModel) {
        String judgePrompt = buildJudgePrompt(workerResults, fileChanges);
        ChatClient chatClient = chatClientBuilder.build();

        try {
            JudgeEvaluation evaluation = chatClient.prompt()
                    .system(JUDGE_SYSTEM_PROMPT)
                    .user(judgePrompt)
                    .call()
                    .entity(JudgeEvaluation.class);

            return new JudgeReviewResult(workerResults, evaluation);
        } catch (Exception e) {
            log.error("Judge 评估失败", e);
            return new JudgeReviewResult(workerResults, null);
        }
    }

    private String buildJudgePrompt(List<ModelReviewResult> workerResults, List<FileChange> fileChanges) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是多个 AI 模型对代码变更的审查结果，请评估这些审查的质量。\n\n");
        sb.append("代码变更文件数：").append(fileChanges.size()).append("\n\n");

        for (ModelReviewResult mr : workerResults) {
            sb.append("模型 ").append(mr.getModelName()).append(" 的审查结果：\n");
            for (ReviewFindingResult f : mr.getFindings()) {
                sb.append("- [").append(f.getSeverity()).append("][").append(f.getCategory())
                  .append("] ").append(f.getTitle()).append(" (").append(f.getFilePath())
                  .append(":").append(f.getLineStart()).append(")\n");
            }
            sb.append("\n");
        }

        sb.append("请评估审查结果的准确性和完整性，指出误报和遗漏。");
        return sb.toString();
    }

    private static final String JUDGE_SYSTEM_PROMPT = """
            你是一位高级代码审查专家，负责评估其他 AI 模型的审查质量。
            请从以下维度评估：
            1. falsePositives: 误报列表（不应该是问题的 finding）
            2. missedIssues: 可能遗漏的重要问题
            3. qualityScore: 审查质量评分 (0.0-1.0)
            4. summary: 总体评价
            """;
}
