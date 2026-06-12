package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class GitHubSarifUploadResultVO {

    private String status;
    private String message;
    private String requestUrl;
}
