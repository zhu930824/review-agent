package com.review.agent.service;

import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.CreateReviewRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewFindingVO;
import com.review.agent.domain.dto.ReviewVO;
import com.review.agent.domain.dto.UpdateFindingStatusRequest;

public interface ReviewService {

    ReviewVO createReview(CreateReviewRequest request);

    ReviewDetailVO getReviewDetail(Long reviewId);

    PageResult<ReviewVO> listReviews(Long projectId, PageRequest pageRequest);

    ReviewDetailVO createPrePrReview(CreatePrePrRequest request);

    ReviewFindingVO updateFindingStatus(Long findingId, UpdateFindingStatusRequest request);
}
