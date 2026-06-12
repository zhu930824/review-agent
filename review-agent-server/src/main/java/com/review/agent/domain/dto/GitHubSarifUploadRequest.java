package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class GitHubSarifUploadRequest {

    private String commitSha;
    private String ref;
    private String sarif;
}
