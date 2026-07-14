package com.review.agent.service;

import com.review.agent.domain.dto.GitLabMergeRequestNoteRequest;
import com.review.agent.domain.dto.GitLabMergeRequestNoteResultVO;

public interface GitLabMergeRequestNoteService {

    GitLabMergeRequestNoteResultVO post(GitLabMergeRequestNoteRequest request);

    GitLabMergeRequestNoteResultVO postAutoSummary(Long reviewId, String mergeRequestIid);
}
