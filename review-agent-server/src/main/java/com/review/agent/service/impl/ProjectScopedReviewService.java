package com.review.agent.service.impl;

import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.CreateReviewRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.PrePrDecisionRequest;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import com.review.agent.domain.dto.ReviewVO;
import com.review.agent.domain.dto.UpdateFindingStatusRequest;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.auth.ProjectPermission;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.infrastructure.sarif.SarifLog;
import com.review.agent.service.ProjectAccessService;
import com.review.agent.service.ReviewService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Primary
@Service
public class ProjectScopedReviewService implements ReviewService {

    private final ReviewService delegate;
    private final ReviewMapper reviewMapper;
    private final ReviewFindingMapper reviewFindingMapper;
    private final ProjectAccessService projectAccessService;

    public ProjectScopedReviewService(
            @Qualifier("reviewServiceImpl") ReviewService delegate,
            ReviewMapper reviewMapper,
            ReviewFindingMapper reviewFindingMapper,
            ProjectAccessService projectAccessService) {
        this.delegate = delegate;
        this.reviewMapper = reviewMapper;
        this.reviewFindingMapper = reviewFindingMapper;
        this.projectAccessService = projectAccessService;
    }

    @Override
    public ReviewVO createReview(CreateReviewRequest request) {
        projectAccessService.require(request.getProjectId(), ProjectPermission.REVIEW_EXECUTE);
        return delegate.createReview(request);
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long id) {
        requireReview(id, ProjectPermission.VIEW);
        return delegate.getReviewDetail(id);
    }

    @Override
    public PageResult<ReviewVO> listReviews(Long projectId, PageRequest pageRequest) {
        projectAccessService.require(projectId, ProjectPermission.VIEW);
        return delegate.listReviews(projectId, pageRequest);
    }

    @Override
    public ReviewDetailVO createPrePrReview(CreatePrePrRequest request) {
        projectAccessService.require(request.getProjectId(), ProjectPermission.REVIEW_EXECUTE);
        return delegate.createPrePrReview(request);
    }

    @Override
    public ReviewFindingVO updateFindingStatus(Long findingId, UpdateFindingStatusRequest request) {
        ReviewFinding finding = reviewFindingMapper.selectById(findingId);
        if (finding == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review finding not found");
        }
        requireReview(finding.getReviewId(), ProjectPermission.REVIEW_EXECUTE);
        return delegate.updateFindingStatus(findingId, request);
    }

    @Override
    public ReviewDetailVO prePrDecision(Long reviewId, PrePrDecisionRequest request) {
        requireReview(reviewId, ProjectPermission.REVIEW_EXECUTE);
        return delegate.prePrDecision(reviewId, request);
    }

    @Override
    public SarifLog exportSarif(Long reviewId) {
        requireReview(reviewId, ProjectPermission.VIEW);
        return delegate.exportSarif(reviewId);
    }

    private Review requireReview(Long reviewId, ProjectPermission permission) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        projectAccessService.require(review.getProjectId(), permission);
        return review;
    }
}
