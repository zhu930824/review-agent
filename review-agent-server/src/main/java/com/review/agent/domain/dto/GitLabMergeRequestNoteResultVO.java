package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class GitLabMergeRequestNoteResultVO {

    private String status;
    private String message;
    private String mergeRequestIid;
    private String requestUrl;
}
