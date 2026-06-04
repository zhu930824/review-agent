package com.review.agent.controller;

import com.review.agent.service.ReviewProgressService;
import com.review.agent.service.ReviewService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviewControllerTest {

    @Test
    void returnsPrePrMarkdownReportForReview() {
        ReviewService reviewService = mock(ReviewService.class);
        ReviewProgressService progressService = mock(ReviewProgressService.class);
        when(reviewService.buildPrePrReport(18L)).thenReturn("# Review Agent Pre-PR 审查报告");

        ReviewController controller = new ReviewController(reviewService, progressService);

        assertEquals(
                "# Review Agent Pre-PR 审查报告",
                controller.getPrePrReport(18L).getData());
    }

    @Test
    void publishesPrePrMarkdownReportForReview() {
        ReviewService reviewService = mock(ReviewService.class);
        ReviewProgressService progressService = mock(ReviewProgressService.class);
        when(reviewService.publishPrePrReport(18L)).thenReturn(true);

        ReviewController controller = new ReviewController(reviewService, progressService);

        assertEquals(true, controller.publishPrePrReport(18L).getData());
    }
}
