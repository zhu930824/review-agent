package com.review.agent.domain.dto;

import com.review.agent.domain.enums.ModelRole;
import com.review.agent.domain.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewModelResultVO {

    private Long id;
    private Long reviewId;
    private String modelName;
    private ModelRole role;
    private ReviewStatus status;
    private String rawResult;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
