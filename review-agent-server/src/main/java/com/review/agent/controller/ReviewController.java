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
    public ApiResponse<ReviewVO> createReview(@Validated @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReviewDetailVO> getReviewDetail(@PathVariable Long id) {
        return ApiResponse.success(reviewService.getReviewDetail(id));
    }

    @GetMapping
    public ApiResponse<PageResult<ReviewVO>> listReviews(
            @RequestParam(required = false) Long projectId,
            PageRequest pageRequest) {
        return ApiResponse.success(reviewService.listReviews(projectId, pageRequest));
    }

    @PostMapping("/pre-pr")
    public ApiResponse<ReviewDetailVO> createPrePrReview(@Validated @RequestBody CreatePrePrRequest request) {
        return ApiResponse.success(reviewService.createPrePrReview(request));
    }

    @PatchMapping("/{id}/finding/{findingId}")
    public ApiResponse<ReviewFindingVO> updateFindingStatus(
            @PathVariable Long id,
            @PathVariable Long findingId,
            @Validated @RequestBody UpdateFindingStatusRequest request) {
        return ApiResponse.success(reviewService.updateFindingStatus(findingId, request));
    }
}
