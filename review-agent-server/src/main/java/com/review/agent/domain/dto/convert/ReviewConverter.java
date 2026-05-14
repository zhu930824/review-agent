package com.review.agent.domain.dto.convert;

import com.review.agent.domain.dto.CreateReviewRequest;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import com.review.agent.domain.dto.ReviewModelResultVO;
import com.review.agent.domain.dto.ReviewVO;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.entity.ReviewModelResult;
import com.review.agent.domain.enums.ReviewStatus;
import com.review.agent.domain.enums.Severity;

import java.time.LocalDateTime;
import java.util.List;

/** 审查相关对象转换 */
public final class ReviewConverter {

    private ReviewConverter() {}

    public static Review toEntity(CreateReviewRequest req) {
        Review entity = new Review();
        entity.setProjectId(req.getProjectId());
        entity.setSourceBranch(req.getSourceBranch());
        entity.setTargetBranch(req.getTargetBranch());
        entity.setSourceCommit(req.getSourceCommit());
        entity.setTargetCommit(req.getTargetCommit());
        entity.setReviewMode(req.getReviewMode());
        entity.setModelsConfig(req.getModelsConfig());
        entity.setStatus(ReviewStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public static ReviewVO toVO(Review entity, String projectName) {
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

    public static ReviewFindingVO toVO(ReviewFinding entity) {
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

    public static ReviewModelResultVO toVO(ReviewModelResult entity) {
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

    /** 审查详情聚合转换，含各严重级别统计与 Pre-PR 状态解析 */
    public static ReviewDetailVO toDetailVO(Review review, String projectName,
                                             List<ReviewFinding> findings,
                                             List<ReviewModelResult> modelResults) {
        ReviewDetailVO detail = new ReviewDetailVO();
        detail.setReview(toVO(review, projectName));
        detail.setFindings(findings.stream().map(ReviewConverter::toVO).toList());
        detail.setModelResults(modelResults.stream().map(ReviewConverter::toVO).toList());

        // 按严重程度统计
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

        // 从 summary 中解析 Pre-PR 状态
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
