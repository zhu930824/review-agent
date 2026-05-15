package com.review.agent.infrastructure.agent;

import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewModelResult;
import com.review.agent.domain.enums.ModelRole;
import com.review.agent.domain.enums.ReviewStatus;
import com.review.agent.infrastructure.agent.config.AgentConfig;
import com.review.agent.infrastructure.agent.config.AgentPipelineConfig;
import com.review.agent.infrastructure.persistence.ReviewModelResultMapper;
import com.review.agent.service.ReviewProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ReviewOrchestrator {

    private final AgentFactory agentFactory;
    private final ReviewModelResultMapper reviewModelResultMapper;
    private final ReviewProgressService reviewProgressService;
    private final Executor agentTaskExecutor;

    public ReviewOrchestrator(
            AgentFactory agentFactory,
            ReviewModelResultMapper reviewModelResultMapper,
            ReviewProgressService reviewProgressService,
            @Qualifier("agentTaskExecutor") Executor agentTaskExecutor) {
        this.agentFactory = agentFactory;
        this.reviewModelResultMapper = reviewModelResultMapper;
        this.reviewProgressService = reviewProgressService;
        this.agentTaskExecutor = agentTaskExecutor;
    }

    /** 执行 Agent 管道：按配置并发执行所有 Agent */
    public List<AgentExecutionResult> execute(Review review, List<FileChange> fileChanges,
                                               AgentPipelineConfig pipelineConfig) {
        List<AgentConfig> agentConfigs = pipelineConfig.getAgents();
        if (agentConfigs == null || agentConfigs.isEmpty()) {
            agentConfigs = new ArrayList<>(AgentRoleTemplates.getPresets().values());
        }

        // 文件分片：超过阈值时分批处理
        List<List<FileChange>> batches = batchFileChanges(fileChanges, 30);
        List<AgentExecutionResult> allResults = new ArrayList<>();

        for (List<FileChange> batch : batches) {
            List<ReviewModelResult> modelResults = createModelResults(review.getId(), agentConfigs);
            List<CompletableFuture<AgentExecutionResult>> futures = new ArrayList<>();

            for (int i = 0; i < agentConfigs.size(); i++) {
                final AgentConfig config = agentConfigs.get(i);
                final ReviewModelResult modelResult = modelResults.get(i);

                reviewProgressService.notifyAgentStarted(review.getId(), config.getRole(), config.getModelName());

                CompletableFuture<AgentExecutionResult> future = CompletableFuture
                        .supplyAsync(() -> executeSingleAgent(review.getId(), batch, config, modelResult),
                                agentTaskExecutor)
                        .orTimeout(5, TimeUnit.MINUTES);
                futures.add(future);
            }

            // 等待全部完成并收集结果
            for (int i = 0; i < futures.size(); i++) {
                try {
                    AgentExecutionResult result = futures.get(i).get();
                    allResults.add(result);
                    completeModelResult(modelResults.get(i), result);
                    reviewProgressService.notifyAgentCompleted(review.getId(),
                            agentConfigs.get(i).getRole(),
                            agentConfigs.get(i).getModelName(),
                            result.isSuccess() ? 1 : 0);
                } catch (Exception e) {
                    log.error("Agent 执行异常: role={}", agentConfigs.get(i).getRole(), e);
                    AgentExecutionResult failed = AgentExecutionResult.failed(
                            agentConfigs.get(i).getRole(),
                            agentConfigs.get(i).getModelName(),
                            "超时或执行异常: " + e.getMessage());
                    allResults.add(failed);
                    completeModelResult(modelResults.get(i), failed);
                    reviewProgressService.notifyAgentFailed(review.getId(),
                            agentConfigs.get(i).getRole(),
                            "超时或执行异常: " + e.getMessage());
                }
            }
        }

        reviewProgressService.notifyReviewCompleted(review.getId());
        return allResults;
    }

    /** 汇总所有 Agent 的执行结果，生成审查摘要 */
    public AgentReviewSummary summarizeResults(List<AgentExecutionResult> results) {
        int totalSuccess = (int) results.stream().filter(AgentExecutionResult::isSuccess).count();
        int totalFailed = results.size() - totalSuccess;

        return AgentReviewSummary.builder()
                .totalAgents(results.size())
                .successCount(totalSuccess)
                .failedCount(totalFailed)
                .agentResults(results)
                .summary("Agent 审查完成: 成功" + totalSuccess + "个, 失败" + totalFailed + "个")
                .build();
    }

    private AgentExecutionResult executeSingleAgent(Long reviewId, List<FileChange> fileChanges,
                                                      AgentConfig config, ReviewModelResult modelResult) {
        long start = System.currentTimeMillis();
        ReviewAgent agent = agentFactory.createAgent(config);
        AgentExecutionResult result = agent.executeReview(reviewId, fileChanges);
        result.setDurationMs(System.currentTimeMillis() - start);
        return result;
    }

    /** 文件列表分片：每批不超过 batchSize 个文件 */
    private List<List<FileChange>> batchFileChanges(List<FileChange> fileChanges, int batchSize) {
        List<List<FileChange>> batches = new ArrayList<>();
        for (int i = 0; i < fileChanges.size(); i += batchSize) {
            int end = Math.min(i + batchSize, fileChanges.size());
            batches.add(fileChanges.subList(i, end));
        }
        return batches;
    }

    /** 为 Agent 创建模型执行记录（写入数据库） */
    private List<ReviewModelResult> createModelResults(Long reviewId, List<AgentConfig> configs) {
        List<ReviewModelResult> results = new ArrayList<>();
        for (AgentConfig config : configs) {
            ReviewModelResult modelResult = new ReviewModelResult();
            modelResult.setReviewId(reviewId);
            modelResult.setModelName(config.getModelName());
            modelResult.setRole(ModelRole.WORKER);
            modelResult.setStatus(ReviewStatus.RUNNING);
            modelResult.setStartedAt(LocalDateTime.now());
            reviewModelResultMapper.insert(modelResult);
            results.add(modelResult);
        }
        return results;
    }

    /** 更新模型执行记录状态 */
    private void completeModelResult(ReviewModelResult modelResult, AgentExecutionResult result) {
        modelResult.setStatus(result.isSuccess() ? ReviewStatus.COMPLETED : ReviewStatus.FAILED);
        modelResult.setCompletedAt(LocalDateTime.now());
        modelResult.setRawResult(result.isSuccess() ? result.getOutput() : null);
        modelResult.setErrorMessage(result.getErrorMessage());
        reviewModelResultMapper.updateById(modelResult);
    }
}
