package com.review.agent.controller;

import com.review.agent.domain.dto.*;
import com.review.agent.service.ReviewProgressService;
import com.review.agent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewProgressService reviewProgressService;

    @PostMapping
    public ApiResponse<ReviewVO> createReview(@Validated @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(reviewService.createReview(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReviewDetailVO> getReviewDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(reviewService.getReviewDetail(id));
    }

    @GetMapping(value = "/{id}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@PathVariable("id") Long id) {
        return reviewProgressService.subscribe(id);
    }

    @GetMapping
    public ApiResponse<PageResult<ReviewVO>> listReviews(
            @RequestParam(value = "projectId", required = false) Long projectId,
            PageRequest pageRequest) {
        return ApiResponse.success(reviewService.listReviews(projectId, pageRequest));
    }

    @PostMapping("/pre-pr")
    public ApiResponse<ReviewDetailVO> createPrePrReview(@Validated @RequestBody CreatePrePrRequest request) {
        return ApiResponse.success(reviewService.createPrePrReview(request));
    }

    @PatchMapping("/{id}/finding/{findingId}")
    public ApiResponse<ReviewFindingVO> updateFindingStatus(
            @PathVariable("id") Long id,
            @PathVariable("findingId") Long findingId,
            @Validated @RequestBody UpdateFindingStatusRequest request) {
        return ApiResponse.success(reviewService.updateFindingStatus(findingId, request));
    }
}
