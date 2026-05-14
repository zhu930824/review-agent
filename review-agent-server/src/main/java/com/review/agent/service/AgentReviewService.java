package com.review.agent.service;

import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.agent.AgentExecutionResult;
import com.review.agent.infrastructure.agent.AgentReviewSummary;
import com.review.agent.infrastructure.agent.ReviewOrchestrator;
import com.review.agent.infrastructure.agent.config.AgentPipelineConfig;
import com.review.agent.infrastructure.git.GitDiffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 模式审查服务 —— 负责获取代码差异并编排多智能体执行审查流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentReviewService {

    private final ReviewOrchestrator orchestrator;
    private final GitDiffService gitDiffService;

    /**
     * 执行 Agent 模式审查：获取代码变更 -> 编排智能体 -> 汇总结果
     *
     * @param review         审查记录
     * @param pipelineConfig Agent 管道配置（角色、策略等）
     * @return 审查摘要，包含各 Agent 执行结果和汇总信息
     */
    public AgentReviewSummary executeAgentReview(Review review, AgentPipelineConfig pipelineConfig) {
        // 1. 获取代码差异
        List<FileChange> fileChanges = resolveFileChanges(review);
        if (fileChanges.isEmpty()) {
            return AgentReviewSummary.builder()
                    .totalAgents(0).successCount(0).failedCount(0)
                    .summary("无代码变更需审查").build();
        }

        log.info("Agent 审查开始: reviewId={}, files={}, agents={}",
                review.getId(), fileChanges.size(),
                pipelineConfig.getAgents() != null ? pipelineConfig.getAgents().size() : "预设");

        // 2. 编排 Agent 执行
        List<AgentExecutionResult> results = orchestrator.execute(review, fileChanges, pipelineConfig);

        // 3. 汇总结果
        return orchestrator.summarizeResults(results);
    }

    /** 根据审查记录中的分支或 commit 信息解析文件变更列表 */
    private List<FileChange> resolveFileChanges(Review review) {
        if (review.getSourceBranch() != null && review.getTargetBranch() != null) {
            return gitDiffService.getBranchDiff(
                    review.getProjectId(), review.getSourceBranch(), review.getTargetBranch());
        }
        if (review.getSourceCommit() != null && review.getTargetCommit() != null) {
            return gitDiffService.getCommitDiff(
                    review.getProjectId(), review.getSourceCommit(), review.getTargetCommit());
        }
        return List.of();
    }
}
