package com.review.agent.service;

import com.review.agent.domain.dto.ProjectGitLabReviewPolicyVO;
import com.review.agent.domain.dto.UpdateProjectGitLabReviewPolicyRequest;

public interface ProjectGitLabReviewPolicyService {

    ProjectGitLabReviewPolicyVO get(Long projectId);

    ProjectGitLabReviewPolicyVO update(Long projectId, UpdateProjectGitLabReviewPolicyRequest request);
}
