package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class UpdateProjectRequest {

    private String name;

    private String repoUrl;

    private String defaultBranch;

    private String description;
}
