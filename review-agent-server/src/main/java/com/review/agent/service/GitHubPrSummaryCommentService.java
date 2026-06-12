package com.review.agent.service;

import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;

public interface GitHubPrSummaryCommentService {

    GitHubPrSummaryCommentResultVO comment(GitHubPrSummaryCommentRequest request);
}
