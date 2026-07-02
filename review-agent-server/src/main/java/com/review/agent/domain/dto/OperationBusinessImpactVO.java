package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class OperationBusinessImpactVO {

    private Long monthlyReviews = 0L;

    private Long averageManualReviewMinutes = 35L;

    private Long automationCoveragePercent = 0L;

    private Long blockerFindings = 0L;

    private Long majorFindings = 0L;

    private Long hoursSaved = 0L;

    private Long avoidedReworkHours = 0L;

    private String executiveSummary;
}
