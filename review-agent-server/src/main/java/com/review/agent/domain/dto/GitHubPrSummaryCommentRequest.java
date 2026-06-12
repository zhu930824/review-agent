package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class GitHubPrSummaryCommentRequest {

    private Integer pullNumber;
    private String body;
}
