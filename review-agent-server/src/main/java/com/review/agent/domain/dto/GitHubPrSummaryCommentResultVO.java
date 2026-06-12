package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class GitHubPrSummaryCommentResultVO {

    private String status;
    private String message;
    private String requestUrl;
}
