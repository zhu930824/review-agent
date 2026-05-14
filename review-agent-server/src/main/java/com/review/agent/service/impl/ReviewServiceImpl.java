package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.*;
import com.review.agent.domain.dto.convert.ReviewConverter;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.entity.ReviewModelResult;
import com.review.agent.domain.enums.*;
import com.review.agent.domain.exception.BusinessException;
import com.review.agent.infrastructure.agent.AgentReviewSummary;
import com.review.agent.infrastructure.agent.AgentRoleTemplates;
import com.review.agent.infrastructure.agent.config.AgentConfig;
import com.review.agent.infrastructure.agent.config.AgentPipelineConfig;
import com.review.agent.infrastructure.ai.AIReviewService;
import com.review.agent.infrastructure.ai.JudgeEvaluation;
import com.review.agent.infrastructure.ai.JudgeReviewResult;
import com.review.agent.infrastructure.ai.ModelReviewResult;
import com.review.agent.infrastructure.ai.ReviewFindingResult;
import com.review.agent.infrastructure.git.GitDiffService;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.infrastructure.persistence.ReviewModelResultMapper;
import com.review.agent.service.AgentReviewService;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReviewFindingMapper reviewFindingMapper;
    private final ReviewModelResultMapper reviewModelResultMapper;
    private final ProjectMapper projectMapper;
    private final GitDiffService gitDiffService;
    private final AIReviewService aiReviewService;
    private final AgentReviewService agentReviewService;
    private final ObjectMapper objectMapper;

    @Override
    public ReviewVO createReview(CreateReviewRequest request) {
        Review review = ReviewConverter.toEntity(request);

        // 解析分支的最新 commit SHA
        if (request.getSourceBranch() != null && request.getTargetBranch() != null) {
            review.setSourceCommit(gitDiffService.getLatestCommit(request.getProjectId(), request.getSourceBranch()));
            review.setTargetCommit(gitDiffService.getLatestCommit(request.getProjectId(), request.getTargetBranch()));
        }

        review.setStatus(ReviewStatus.RUNNING);
        reviewMapper.insert(review);

        // 简化为同步执行，后续可改为异步
        executeReview(review, request.getModelsConfig());

        return ReviewConverter.toVO(review, getProjectName(review.getProjectId()));
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long reviewId) {
        Review review = requireReview(reviewId);
        String projectName = getProjectName(review.getProjectId());
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>().eq(ReviewFinding::getReviewId, reviewId));
        List<ReviewModelResult> modelResults = reviewModelResultMapper.selectList(
                new LambdaQueryWrapper<ReviewModelResult>().eq(ReviewModelResult::getReviewId, reviewId));
        return ReviewConverter.toDetailVO(review, projectName, findings, modelResults);
    }

    @Override
    public PageResult<ReviewVO> listReviews(Long projectId, PageRequest pageRequest) {
        Page<Review> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(Review::getProjectId, projectId);
        }
        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> result = reviewMapper.selectPage(page, wrapper);
        List<ReviewVO> voList = result.getRecords().stream()
                .map(r -> ReviewConverter.toVO(r, getProjectName(r.getProjectId())))
                .toList();

        return new PageResult<>(result.getTotal(), pageRequest.getPageNum(),
                pageRequest.getPageSize(), voList);
    }

    @Override
    public ReviewDetailVO createPrePrReview(CreatePrePrRequest request) {
        CreateReviewRequest reviewRequest = new CreateReviewRequest();
        reviewRequest.setProjectId(request.getProjectId());
        reviewRequest.setSourceBranch(request.getSourceBranch());
        reviewRequest.setTargetBranch(request.getTargetBranch());
        reviewRequest.setReviewMode(ReviewMode.SINGLE);

        Review review = ReviewConverter.toEntity(reviewRequest);
        review.setStatus(ReviewStatus.RUNNING);
        reviewMapper.insert(review);

        executeReview(review, null);

        // 收集阻塞原因：BLOCKER 级别 或 SECURITY 类别的 MAJOR 问题
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>().eq(ReviewFinding::getReviewId, review.getId()));

        List<String> blockedReasons = new ArrayList<>();
        long blockerCount = 0;
        for (ReviewFinding f : findings) {
            if (f.getSeverity() == Severity.BLOCKER) {
                blockedReasons.add(f.getTitle());
                blockerCount++;
            } else if (f.getCategory() == FindingCategory.SECURITY && f.getSeverity() == Severity.MAJOR) {
                blockedReasons.add("[安全问题] " + f.getTitle());
            }
        }

        if (!blockedReasons.isEmpty()) {
            review.setSummary("{\"prePrStatus\":\"BLOCKED\",\"blockerCount\":" + blockerCount + "}");
        } else {
            review.setSummary("{\"prePrStatus\":\"PASSED\"}");
        }
        reviewMapper.updateById(review);

        String projectName = getProjectName(review.getProjectId());
        List<ReviewModelResult> modelResults = reviewModelResultMapper.selectList(
                new LambdaQueryWrapper<ReviewModelResult>().eq(ReviewModelResult::getReviewId, review.getId()));
        ReviewDetailVO detail = ReviewConverter.toDetailVO(review, projectName, findings, modelResults);
        detail.setBlockedReasons(blockedReasons);
        return detail;
    }

    @Override
    public ReviewFindingVO updateFindingStatus(Long findingId, UpdateFindingStatusRequest request) {
        ReviewFinding finding = reviewFindingMapper.selectById(findingId);
        if (finding == null) {
            throw new BusinessException("404", "审查发现不存在: " + findingId);
        }
        finding.setHumanStatus(request.getHumanStatus());
        reviewFindingMapper.updateById(finding);
        return ReviewConverter.toVO(finding);
    }

    /**
     * 审查核心流程：获取 diff -> 按模式审查 -> 存储结果
     */
    private void executeReview(Review review, String modelsConfig) {
        try {
            List<FileChange> fileChanges = resolveFileChanges(review);

            if (fileChanges.isEmpty()) {
                review.setStatus(ReviewStatus.COMPLETED);
                review.setSummary("{\"totalFindings\":0}");
                reviewMapper.updateById(review);
                return;
            }

            List<ReviewFindingResult> aiFindings;

            switch (review.getReviewMode()) {
                case MULTI -> aiFindings = executeMultiReview(review, fileChanges, modelsConfig);
                case JUDGE -> aiFindings = executeJudgeReview(review, fileChanges, modelsConfig);
                case AGENT -> aiFindings = executeAgentReview(review, fileChanges, modelsConfig);
                default -> aiFindings = executeSingleReview(review, fileChanges, modelsConfig);
            }

            // 存储审查发现
            for (ReviewFindingResult finding : aiFindings) {
                reviewFindingMapper.insert(toReviewFinding(review.getId(), finding));
            }

            // 更新审查状态
            long blockerCount = aiFindings.stream().filter(f -> f.getSeverity() == Severity.BLOCKER).count();
            long majorCount = aiFindings.stream().filter(f -> f.getSeverity() == Severity.MAJOR).count();
            review.setStatus(ReviewStatus.COMPLETED);
            review.setSummary(String.format("{\"totalFindings\":%d,\"blockerCount\":%d,\"majorCount\":%d}",
                    aiFindings.size(), blockerCount, majorCount));
            reviewMapper.updateById(review);

        } catch (Exception e) {
            log.error("审查执行失败: reviewId={}", review.getId(), e);
            review.setStatus(ReviewStatus.FAILED);
            reviewMapper.updateById(review);
            throw new BusinessException("REVIEW_FAILED", "审查执行失败: " + e.getMessage(), e);
        }
    }

    /** SINGLE 模式：单模型审查 */
    private List<ReviewFindingResult> executeSingleReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        String modelName = parseModelName(modelsConfig);

        ReviewModelResult modelResult = createModelResult(review.getId(), modelName, ModelRole.WORKER);

        List<ReviewFindingResult> findings = aiReviewService.reviewWithModel(fileChanges, modelName);

        completeModelResult(modelResult, null);
        return findings;
    }

    /** MULTI 模式：多模型并行审查 + 交叉验证 */
    private List<ReviewFindingResult> executeMultiReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        List<String> modelNames = parseModelNames(modelsConfig);

        // 为每个模型创建执行记录
        List<ReviewModelResult> modelResults = modelNames.stream()
                .map(name -> createModelResult(review.getId(), name, ModelRole.WORKER))
                .toList();

        // 并行审查
        List<ModelReviewResult> parallelResults = aiReviewService.reviewWithModelsParallel(fileChanges, modelNames);

        // 交叉验证
        List<ReviewFindingResult> findings = aiReviewService.crossValidate(parallelResults);

        // 更新模型执行记录
        for (int i = 0; i < modelResults.size(); i++) {
            completeModelResult(modelResults.get(i), parallelResults.get(i).getFindings().size() + " findings");
        }

        return findings;
    }

    /** JUDGE 模式：Worker 并行审查 + Judge 评估 */
    private List<ReviewFindingResult> executeJudgeReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        JudgeConfig judgeConfig = parseJudgeConfig(modelsConfig);

        // Worker 模型执行记录
        List<ReviewModelResult> workerModelResults = judgeConfig.workers.stream()
                .map(name -> createModelResult(review.getId(), name, ModelRole.WORKER))
                .toList();

        // Judge 模型执行记录
        ReviewModelResult judgeModelResult = createModelResult(review.getId(), judgeConfig.judge, ModelRole.JUDGE);

        // Worker 并行审查 + Judge 评估
        JudgeReviewResult judgeReviewResult = aiReviewService.reviewWithJudge(
                fileChanges, judgeConfig.workers, judgeConfig.judge);

        // 交叉验证 Worker 的 findings
        List<ReviewFindingResult> findings = aiReviewService.crossValidate(judgeReviewResult.getWorkerResults());

        // 更新 Worker 执行记录
        for (int i = 0; i < workerModelResults.size(); i++) {
            completeModelResult(workerModelResults.get(i),
                    judgeReviewResult.getWorkerResults().get(i).getFindings().size() + " findings");
        }

        // 更新 Judge 执行记录
        String judgeRawResult = serializeJudgeEvaluation(judgeReviewResult.getEvaluation());
        completeModelResult(judgeModelResult, judgeRawResult);

        return findings;
    }

    /** AGENT 模式：多智能体审查，Agent 通过 SaveFindingTool 自行持久化发现结果 */
    private List<ReviewFindingResult> executeAgentReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        AgentPipelineConfig pipelineConfig = parseAgentConfig(modelsConfig);

        AgentReviewSummary summary = agentReviewService.executeAgentReview(review, pipelineConfig);

        // Agent 已通过 SaveFindingTool 将 findings 持久化，从数据库加载
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>().eq(ReviewFinding::getReviewId, review.getId()));

        return findings.stream()
                .map(this::toReviewFindingResult)
                .toList();
    }

    /** 将持久化实体转为 AI 审查结果 DTO */
    private ReviewFindingResult toReviewFindingResult(ReviewFinding entity) {
        ReviewFindingResult result = new ReviewFindingResult();
        result.setFilePath(entity.getFilePath());
        result.setLineStart(entity.getLineStart());
        result.setLineEnd(entity.getLineEnd());
        result.setCategory(entity.getCategory());
        result.setSeverity(entity.getSeverity());
        result.setTitle(entity.getTitle());
        result.setDescription(entity.getDescription());
        result.setSuggestion(entity.getSuggestion());
        result.setModelName(entity.getModelName());
        result.setCrossHit(entity.getIsCrossHit());
        result.setConfidence(entity.getConfidence() != null ? entity.getConfidence().doubleValue() : 0.0);
        return result;
    }

    /** 根据审查记录中的分支或 commit 信息获取文件差异 */
    private List<FileChange> resolveFileChanges(Review review) {
        if (review.getSourceBranch() != null && review.getTargetBranch() != null) {
            return gitDiffService.getBranchDiff(
                    review.getProjectId(), review.getSourceBranch(), review.getTargetBranch());
        }
        if (review.getSourceCommit() != null && review.getTargetCommit() != null) {
            return gitDiffService.getCommitDiff(
                    review.getProjectId(), review.getSourceCommit(), review.getTargetCommit());
        }
        throw new BusinessException("INVALID_REVIEW_PARAMS", "必须指定分支或 commit 范围");
    }

    /** 将 AI 审查结果转换为持久化实体 */
    private ReviewFinding toReviewFinding(Long reviewId, ReviewFindingResult finding) {
        ReviewFinding entity = new ReviewFinding();
        entity.setReviewId(reviewId);
        entity.setFilePath(finding.getFilePath());
        entity.setLineStart(finding.getLineStart());
        entity.setLineEnd(finding.getLineEnd());
        entity.setCategory(finding.getCategory() != null ? finding.getCategory() : FindingCategory.OTHER);
        entity.setSeverity(finding.getSeverity() != null ? finding.getSeverity() : Severity.INFO);
        entity.setTitle(finding.getTitle());
        entity.setDescription(finding.getDescription());
        entity.setSuggestion(finding.getSuggestion());
        entity.setModelName(finding.getModelName());
        entity.setConfidence(BigDecimal.valueOf(finding.isCrossHit() ? finding.getConfidence() : 0.80));
        entity.setIsCrossHit(finding.isCrossHit());
        entity.setHumanStatus(HumanStatus.PENDING);
        return entity;
    }

    /** 创建模型执行记录 */
    private ReviewModelResult createModelResult(Long reviewId, String modelName, ModelRole role) {
        ReviewModelResult modelResult = new ReviewModelResult();
        modelResult.setReviewId(reviewId);
        modelResult.setModelName(modelName);
        modelResult.setRole(role);
        modelResult.setStatus(ReviewStatus.RUNNING);
        modelResult.setStartedAt(LocalDateTime.now());
        reviewModelResultMapper.insert(modelResult);
        return modelResult;
    }

    /** 完成模型执行记录 */
    private void completeModelResult(ReviewModelResult modelResult, String rawResult) {
        modelResult.setStatus(ReviewStatus.COMPLETED);
        modelResult.setCompletedAt(LocalDateTime.now());
        modelResult.setRawResult(rawResult);
        reviewModelResultMapper.updateById(modelResult);
    }

    /** 解析 SINGLE 模式的模型名称 */
    private String parseModelName(String modelsConfig) {
        if (modelsConfig == null || modelsConfig.isBlank()) {
            return "qwen-plus";
        }
        try {
            Map<String, Object> config = objectMapper.readValue(modelsConfig, new TypeReference<>() {});
            Object model = config.get("model");
            return model != null ? model.toString() : "qwen-plus";
        } catch (Exception e) {
            log.warn("解析模型配置失败，使用默认模型: {}", modelsConfig, e);
            return "qwen-plus";
        }
    }

    /** 解析 MULTI 模式的模型名称列表 */
    @SuppressWarnings("unchecked")
    private List<String> parseModelNames(String modelsConfig) {
        if (modelsConfig == null || modelsConfig.isBlank()) {
            return List.of("qwen-plus");
        }
        try {
            Map<String, Object> config = objectMapper.readValue(modelsConfig, new TypeReference<>() {});
            Object models = config.get("models");
            if (models instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            return List.of("qwen-plus");
        } catch (Exception e) {
            throw new BusinessException("INVALID_MODELS_CONFIG", "MULTI 模式的模型配置格式错误: " + modelsConfig, e);
        }
    }

    /** JUDGE 模式配置 */
    private record JudgeConfig(List<String> workers, String judge) {}

    /** 解析 JUDGE 模式的模型配置 */
    @SuppressWarnings("unchecked")
    private JudgeConfig parseJudgeConfig(String modelsConfig) {
        try {
            Map<String, Object> config = objectMapper.readValue(modelsConfig, new TypeReference<>() {});
            Object workersObj = config.get("workers");
            Object judgeObj = config.get("judge");

            if (!(workersObj instanceof List<?> list) || judgeObj == null) {
                throw new BusinessException("INVALID_MODELS_CONFIG",
                        "JUDGE 模式需要 workers 数组和 judge 字段");
            }

            List<String> workers = list.stream().map(Object::toString).toList();
            return new JudgeConfig(workers, judgeObj.toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("INVALID_MODELS_CONFIG", "JUDGE 模式的模型配置格式错误: " + modelsConfig, e);
        }
    }

    /** 解析 AGENT 模式的管道配置 */
    private AgentPipelineConfig parseAgentConfig(String modelsConfig) {
        if (modelsConfig == null || modelsConfig.isBlank()) {
            List<AgentConfig> agents = new ArrayList<>(AgentRoleTemplates.getPresets().values());
            return AgentPipelineConfig.builder()
                    .agents(agents)
                    .orchestrationStrategy("PARALLEL")
                    .build();
        }
        try {
            return objectMapper.readValue(modelsConfig, AgentPipelineConfig.class);
        } catch (Exception e) {
            log.warn("解析 Agent 配置失败，使用预设模板: {}", modelsConfig, e);
            List<AgentConfig> agents = new ArrayList<>(AgentRoleTemplates.getPresets().values());
            return AgentPipelineConfig.builder()
                    .agents(agents)
                    .orchestrationStrategy("PARALLEL")
                    .build();
        }
    }

    /** 序列化 Judge 评估结果 */
    private String serializeJudgeEvaluation(JudgeEvaluation evaluation) {
        if (evaluation == null) return null;
        try {
            return objectMapper.writeValueAsString(evaluation);
        } catch (Exception e) {
            log.error("序列化 Judge 评估结果失败", e);
            return null;
        }
    }

    private Review requireReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("404", "审查记录不存在: " + reviewId);
        }
        return review;
    }

    private String getProjectName(Long projectId) {
        if (projectId == null) return null;
        var project = projectMapper.selectById(projectId);
        return project != null ? project.getName() : null;
    }
}
