package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GitHubPrSummaryCommentRequest;
import com.review.agent.domain.dto.GitHubPrSummaryCommentResultVO;
import com.review.agent.service.GitHubPrSummaryCommentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationPrSummaryControllerTest {

    private final RecordingCommentService commentService = new RecordingCommentService();
    private final IntegrationPrSummaryController controller = new IntegrationPrSummaryController(commentService);

    @Test
    void delegatesCommentRequestToService() {
        GitHubPrSummaryCommentRequest request = new GitHubPrSummaryCommentRequest();
        request.setPullNumber(17);
        request.setBody("Review Agent summary");

        Result<GitHubPrSummaryCommentResultVO> result = controller.comment(request);

        assertTrue(result.isSuccess());
        assertEquals("POSTED", result.getData().getStatus());
        assertEquals(17, commentService.request.getPullNumber());
        assertEquals("Review Agent summary", commentService.request.getBody());
    }

    private static class RecordingCommentService implements GitHubPrSummaryCommentService {
        private GitHubPrSummaryCommentRequest request;

        @Override
        public GitHubPrSummaryCommentResultVO comment(GitHubPrSummaryCommentRequest request) {
            this.request = request;
            GitHubPrSummaryCommentResultVO result = new GitHubPrSummaryCommentResultVO();
            result.setStatus("POSTED");
            result.setMessage("github pr summary comment posted");
            result.setRequestUrl("https://api.github.com/repos/zhu930824/review-agent/issues/17/comments");
            return result;
        }
    }
}
