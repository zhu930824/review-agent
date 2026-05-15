package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.common.exception.BizException;
import com.review.agent.domain.dto.*;
import com.review.agent.domain.dto.diff.FileChange;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.entity.ReviewModelResult;
import com.review.agent.domain.enums.*;
import com.review.agent.domain.exception.CommonExceptionEnum;
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
import com.review.agent.service.ReviewProgressService;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final ReviewProgressService reviewProgressService;
    private final ObjectMapper objectMapper;

    @Override
    public ReviewVO createReview(CreateReviewRequest request) {
        Review review = new Review();
        review.setProjectId(request.getProjectId());
        review.setSourceBranch(request.getSourceBranch());
        review.setTargetBranch(request.getTargetBranch());
        review.setSourceCommit(request.getSourceCommit());
        review.setTargetCommit(request.getTargetCommit());
        review.setReviewMode(request.getReviewMode());
        review.setModelsConfig(request.getModelsConfig());
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        if (request.getSourceBranch() != null && request.getTargetBranch() != null) {
            review.setSourceCommit(gitDiffService.getLatestCommit(request.getProjectId(), request.getSourceBranch()));
            review.setTargetCommit(gitDiffService.getLatestCommit(request.getProjectId(), request.getTargetBranch()));
        }

        review.setStatus(ReviewStatus.RUNNING);
        reviewMapper.insert(review);

        executeReviewAsync(review, request.getModelsConfig());

        return buildReviewVO(review, getProjectName(review.getProjectId()));
    }

    @Async("agentTaskExecutor")
    public void executeReviewAsync(Review review, String modelsConfig) {
        executeReview(review, modelsConfig);
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long reviewId) {
        Review review = requireReview(reviewId);
        String projectName = getProjectName(review.getProjectId());
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>().eq(ReviewFinding::getReviewId, reviewId));
        List<ReviewModelResult> modelResults = reviewModelResultMapper.selectList(
                new LambdaQueryWrapper<ReviewModelResult>().eq(ReviewModelResult::getReviewId, reviewId));
        return buildReviewDetailVO(review, projectName, findings, modelResults);
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
                .map(r -> buildReviewVO(r, getProjectName(r.getProjectId())))
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

        Review review = new Review();
        review.setProjectId(reviewRequest.getProjectId());
        review.setSourceBranch(reviewRequest.getSourceBranch());
        review.setTargetBranch(reviewRequest.getTargetBranch());
        review.setSourceCommit(reviewRequest.getSourceCommit());
        review.setTargetCommit(reviewRequest.getTargetCommit());
        review.setReviewMode(reviewRequest.getReviewMode());
        review.setModelsConfig(reviewRequest.getModelsConfig());
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.RUNNING);
        reviewMapper.insert(review);

        executeReview(review, null);

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
        ReviewDetailVO detail = buildReviewDetailVO(review, projectName, findings, modelResults);
        detail.setBlockedReasons(blockedReasons);
        return detail;
    }

    @Override
    public ReviewFindingVO updateFindingStatus(Long findingId, UpdateFindingStatusRequest request) {
        ReviewFinding finding = reviewFindingMapper.selectById(findingId);
        if (finding == null) {
            throw new BizException(CommonExceptionEnum.REVIEW_FINDING_NOT_FOUND);
        }
        finding.setHumanStatus(request.getHumanStatus());
        reviewFindingMapper.updateById(finding);
        return buildReviewFindingVO(finding);
    }

    private void executeReview(Review review, String modelsConfig) {
        try {
            List<FileChange> fileChanges = resolveFileChanges(review);

            if (fileChanges.isEmpty()) {
                review.setStatus(ReviewStatus.COMPLETED);
                review.setSummary("{\"totalFindings\":0}");
                reviewMapper.updateById(review);
                reviewProgressService.notifyReviewCompleted(review.getId());
                return;
            }

            List<ReviewFindingResult> aiFindings;

            switch (review.getReviewMode()) {
                case MULTI -> aiFindings = executeMultiReview(review, fileChanges, modelsConfig);
                case JUDGE -> aiFindings = executeJudgeReview(review, fileChanges, modelsConfig);
                case AGENT -> aiFindings = executeAgentReview(review, fileChanges, modelsConfig);
                default -> aiFindings = executeSingleReview(review, fileChanges, modelsConfig);
            }

            for (ReviewFindingResult finding : aiFindings) {
                reviewFindingMapper.insert(toReviewFinding(review.getId(), finding));
            }

            long blockerCount = aiFindings.stream().filter(f -> f.getSeverity() == Severity.BLOCKER).count();
            long majorCount = aiFindings.stream().filter(f -> f.getSeverity() == Severity.MAJOR).count();
            review.setStatus(ReviewStatus.COMPLETED);
            review.setSummary(String.format("{\"totalFindings\":%d,\"blockerCount\":%d,\"majorCount\":%d}",
                    aiFindings.size(), blockerCount, majorCount));
            reviewMapper.updateById(review);
            reviewProgressService.notifyReviewCompleted(review.getId());

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("审查执行失败: reviewId={}", review.getId(), e);
            review.setStatus(ReviewStatus.FAILED);
            reviewMapper.updateById(review);
            reviewProgressService.notifyReviewCompleted(review.getId());
            throw new BizException(CommonExceptionEnum.REVIEW_FAILED, e);
        }
    }

    private List<ReviewFindingResult> executeSingleReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        String modelName = parseModelName(modelsConfig);

        ReviewModelResult modelResult = createModelResult(review.getId(), modelName, ModelRole.WORKER);
        reviewProgressService.notifyAgentStarted(review.getId(), "WORKER", modelName);

        List<ReviewFindingResult> findings = aiReviewService.reviewWithModel(fileChanges, modelName);

        completeModelResult(modelResult, null);
        reviewProgressService.notifyAgentCompleted(review.getId(), "WORKER", modelName, findings.size());
        return findings;
    }

    private List<ReviewFindingResult> executeMultiReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        List<String> modelNames = parseModelNames(modelsConfig);

        List<ReviewModelResult> modelResults = modelNames.stream()
                .map(name -> createModelResult(review.getId(), name, ModelRole.WORKER))
                .toList();

        for (String name : modelNames) {
            reviewProgressService.notifyAgentStarted(review.getId(), "WORKER", name);
        }

        List<ModelReviewResult> parallelResults = aiReviewService.reviewWithModelsParallel(fileChanges, modelNames);

        List<ReviewFindingResult> findings = aiReviewService.crossValidate(parallelResults);

        for (int i = 0; i < modelResults.size(); i++) {
            completeModelResult(modelResults.get(i), parallelResults.get(i).getFindings().size() + " findings");
            reviewProgressService.notifyAgentCompleted(review.getId(), "WORKER", modelNames.get(i),
                    parallelResults.get(i).getFindings().size());
        }

        return findings;
    }

    private List<ReviewFindingResult> executeJudgeReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        JudgeConfig judgeConfig = parseJudgeConfig(modelsConfig);

        List<ReviewModelResult> workerModelResults = judgeConfig.workers.stream()
                .map(name -> createModelResult(review.getId(), name, ModelRole.WORKER))
                .toList();

        ReviewModelResult judgeModelResult = createModelResult(review.getId(), judgeConfig.judge, ModelRole.JUDGE);

        for (String name : judgeConfig.workers) {
            reviewProgressService.notifyAgentStarted(review.getId(), "WORKER", name);
        }
        reviewProgressService.notifyAgentStarted(review.getId(), "JUDGE", judgeConfig.judge);

        JudgeReviewResult judgeReviewResult = aiReviewService.reviewWithJudge(
                fileChanges, judgeConfig.workers, judgeConfig.judge);

        List<ReviewFindingResult> findings = aiReviewService.crossValidate(judgeReviewResult.getWorkerResults());

        for (int i = 0; i < workerModelResults.size(); i++) {
            completeModelResult(workerModelResults.get(i),
                    judgeReviewResult.getWorkerResults().get(i).getFindings().size() + " findings");
            reviewProgressService.notifyAgentCompleted(review.getId(), "WORKER", judgeConfig.workers.get(i),
                    judgeReviewResult.getWorkerResults().get(i).getFindings().size());
        }

        String judgeRawResult = serializeJudgeEvaluation(judgeReviewResult.getEvaluation());
        completeModelResult(judgeModelResult, judgeRawResult);
        reviewProgressService.notifyAgentCompleted(review.getId(), "JUDGE", judgeConfig.judge, findings.size());

        return findings;
    }

    private List<ReviewFindingResult> executeAgentReview(Review review, List<FileChange> fileChanges, String modelsConfig) {
        AgentPipelineConfig pipelineConfig = parseAgentConfig(modelsConfig);

        agentReviewService.executeAgentReview(review, pipelineConfig);

        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>().eq(ReviewFinding::getReviewId, review.getId()));

        return findings.stream()
                .map(this::toReviewFindingResult)
                .toList();
    }

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

    private List<FileChange> resolveFileChanges(Review review) {
        if (review.getSourceBranch() != null && review.getTargetBranch() != null) {
            return gitDiffService.getBranchDiff(
                    review.getProjectId(), review.getSourceBranch(), review.getTargetBranch());
        }
        if (review.getSourceCommit() != null && review.getTargetCommit() != null) {
            return gitDiffService.getCommitDiff(
                    review.getProjectId(), review.getSourceCommit(), review.getTargetCommit());
        }
        throw new BizException(CommonExceptionEnum.INVALID_REVIEW_PARAMS);
    }

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

    private void completeModelResult(ReviewModelResult modelResult, String rawResult) {
        modelResult.setStatus(ReviewStatus.COMPLETED);
        modelResult.setCompletedAt(LocalDateTime.now());
        modelResult.setRawResult(rawResult);
        reviewModelResultMapper.updateById(modelResult);
    }

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
            throw new BizException(CommonExceptionEnum.INVALID_MODELS_CONFIG, e);
        }
    }

    private record JudgeConfig(List<String> workers, String judge) {}

    @SuppressWarnings("unchecked")
    private JudgeConfig parseJudgeConfig(String modelsConfig) {
        try {
            Map<String, Object> config = objectMapper.readValue(modelsConfig, new TypeReference<>() {});
            Object workersObj = config.get("workers");
            Object judgeObj = config.get("judge");

            if (!(workersObj instanceof List<?> list) || judgeObj == null) {
                throw new BizException(CommonExceptionEnum.INVALID_MODELS_CONFIG);
            }

            List<String> workers = list.stream().map(Object::toString).toList();
            return new JudgeConfig(workers, judgeObj.toString());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(CommonExceptionEnum.INVALID_MODELS_CONFIG, e);
        }
    }

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
            throw new BizException(CommonExceptionEnum.REVIEW_NOT_FOUND);
        }
        return review;
    }

    private String getProjectName(Long projectId) {
        if (projectId == null) return null;
        var project = projectMapper.selectById(projectId);
        return project != null ? project.getName() : null;
    }

    private ReviewVO buildReviewVO(Review entity, String projectName) {
        ReviewVO vo = new ReviewVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setProjectName(projectName);
        vo.setSourceBranch(entity.getSourceBranch());
        vo.setTargetBranch(entity.getTargetBranch());
        vo.setSourceCommit(entity.getSourceCommit());
        vo.setTargetCommit(entity.getTargetCommit());
        vo.setStatus(entity.getStatus());
        vo.setReviewMode(entity.getReviewMode());
        vo.setModelsConfig(entity.getModelsConfig());
        vo.setSummary(entity.getSummary());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private ReviewFindingVO buildReviewFindingVO(ReviewFinding entity) {
        ReviewFindingVO vo = new ReviewFindingVO();
        vo.setId(entity.getId());
        vo.setReviewId(entity.getReviewId());
        vo.setFilePath(entity.getFilePath());
        vo.setLineStart(entity.getLineStart());
        vo.setLineEnd(entity.getLineEnd());
        vo.setCategory(entity.getCategory());
        vo.setSeverity(entity.getSeverity());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setSuggestion(entity.getSuggestion());
        vo.setModelName(entity.getModelName());
        vo.setConfidence(entity.getConfidence());
        vo.setIsCrossHit(entity.getIsCrossHit());
        vo.setHumanStatus(entity.getHumanStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private ReviewModelResultVO buildReviewModelResultVO(ReviewModelResult entity) {
        ReviewModelResultVO vo = new ReviewModelResultVO();
        vo.setId(entity.getId());
        vo.setReviewId(entity.getReviewId());
        vo.setModelName(entity.getModelName());
        vo.setRole(entity.getRole());
        vo.setStatus(entity.getStatus());
        vo.setRawResult(entity.getRawResult());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setStartedAt(entity.getStartedAt());
        vo.setCompletedAt(entity.getCompletedAt());
        return vo;
    }

    private ReviewDetailVO buildReviewDetailVO(Review review, String projectName,
                                                List<ReviewFinding> findings,
                                                List<ReviewModelResult> modelResults) {
        ReviewDetailVO detail = new ReviewDetailVO();
        detail.setReview(buildReviewVO(review, projectName));
        detail.setFindings(findings.stream().map(this::buildReviewFindingVO).toList());
        detail.setModelResults(modelResults.stream().map(this::buildReviewModelResultVO).toList());

        int blocker = 0, major = 0, minor = 0, info = 0;
        for (ReviewFinding f : findings) {
            if (f.getSeverity() == null) continue;
            switch (f.getSeverity()) {
                case BLOCKER -> blocker++;
                case MAJOR -> major++;
                case MINOR -> minor++;
                case INFO -> info++;
            }
        }
        detail.setTotalFindings(findings.size());
        detail.setBlockerCount(blocker);
        detail.setMajorCount(major);
        detail.setMinorCount(minor);
        detail.setInfoCount(info);

        String summary = review.getSummary();
        if (summary != null) {
            if (summary.contains("\"prePrStatus\":\"BLOCKED\"")) {
                detail.setPrePrStatus("BLOCKED");
            } else if (summary.contains("\"prePrStatus\":\"PASSED\"")) {
                detail.setPrePrStatus("PASSED");
            }
        }
        return detail;
    }
}