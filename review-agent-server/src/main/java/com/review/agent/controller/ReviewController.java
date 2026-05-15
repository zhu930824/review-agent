package com.review.agent.controller;

import com.review.agent.common.result.Result;
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
    public Result<ReviewVO> createReview(@Validated @RequestBody CreateReviewRequest request) {
        return Result.success(reviewService.createReview(request));
    }

    @GetMapping("/{id}")
    public Result<ReviewDetailVO> getReviewDetail(@PathVariable("id") Long id) {
        return Result.success(reviewService.getReviewDetail(id));
    }

    @GetMapping(value = "/{id}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@PathVariable("id") Long id) {
        return reviewProgressService.subscribe(id);
    }

    @GetMapping
    public Result<PageResult<ReviewVO>> listReviews(
            @RequestParam(value = "projectId", required = false) Long projectId,
            PageRequest pageRequest) {
        return Result.success(reviewService.listReviews(projectId, pageRequest));
    }

    @PostMapping("/pre-pr")
    public Result<ReviewDetailVO> createPrePrReview(@Validated @RequestBody CreatePrePrRequest request) {
        return Result.success(reviewService.createPrePrReview(request));
    }

    @PatchMapping("/{id}/finding/{findingId}")
    public Result<ReviewFindingVO> updateFindingStatus(
            @PathVariable("id") Long id,
            @PathVariable("findingId") Long findingId,
            @Validated @RequestBody UpdateFindingStatusRequest request) {
        return Result.success(reviewService.updateFindingStatus(findingId, request));
    }
}
