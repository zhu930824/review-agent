package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.service.GitHubPrSummaryCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/pr-summary")
@RequiredArgsConstructor
public class IntegrationPrSummaryController {

    private final GitHubPrSummaryCommentService commentService;

    @PostMapping("/comment")
    public Result<GitHubPrSummaryCommentResultVO> comment(@RequestBody GitHubPrSummaryCommentRequest request) {
        return Result.success(commentService.comment(request));
    }
}
