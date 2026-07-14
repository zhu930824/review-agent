package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GitLabMergeRequestNoteRequest;
import com.review.agent.domain.dto.GitLabMergeRequestNoteResultVO;
import com.review.agent.service.GitLabMergeRequestNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/gitlab/merge-requests")
@RequiredArgsConstructor
public class IntegrationGitLabMergeRequestController {

    private final GitLabMergeRequestNoteService service;

    @PostMapping("/summary-note")
    public Result<GitLabMergeRequestNoteResultVO> postSummaryNote(
            @Valid @RequestBody GitLabMergeRequestNoteRequest request) {
        return Result.success(service.post(request));
    }
}
