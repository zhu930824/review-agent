package com.review.agent.controller;

import com.review.agent.domain.dto.*;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public BaseResult<ReviewVO> createReview(@Validated @RequestBody CreateReviewRequest request) {
        return BaseResult.success(reviewService.createReview(request));
    }

    @GetMapping("/{id}")
    public BaseResult<ReviewDetailVO> getReviewDetail(@PathVariable Long id) {
        return BaseResult.success(reviewService.getReviewDetail(id));
    }

    @GetMapping
    public BaseResult<PageResult<ReviewVO>> listReviews(
            @RequestParam(required = false) Long projectId,
            PageRequest pageRequest) {
        return BaseResult.success(reviewService.listReviews(projectId, pageRequest));
    }

    @PostMapping("/pre-pr")
    public BaseResult<ReviewDetailVO> createPrePrReview(@Validated @RequestBody CreatePrePrRequest request) {
        return BaseResult.success(reviewService.createPrePrReview(request));
    }

    @PatchMapping("/{id}/finding/{findingId}")
    public BaseResult<ReviewFindingVO> updateFindingStatus(
            @PathVariable Long id,
            @PathVariable Long findingId,
            @Validated @RequestBody UpdateFindingStatusRequest request) {
        return BaseResult.success(reviewService.updateFindingStatus(findingId, request));
    }
}
