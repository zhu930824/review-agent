package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.ProjectGitLabReviewPolicyVO;
import com.review.agent.domain.dto.UpdateProjectGitLabReviewPolicyRequest;
import com.review.agent.service.ProjectGitLabReviewPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/gitlab-review-policy")
@RequiredArgsConstructor
public class ProjectGitLabReviewPolicyController {

    private final ProjectGitLabReviewPolicyService service;

    @GetMapping
    public Result<ProjectGitLabReviewPolicyVO> get(@PathVariable("projectId") Long projectId) {
        return Result.success(service.get(projectId));
    }

    @PutMapping
    public Result<ProjectGitLabReviewPolicyVO> update(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody UpdateProjectGitLabReviewPolicyRequest request) {
        return Result.success(service.update(projectId, request));
    }
}
