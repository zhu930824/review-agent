package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GitHubSarifUploadRequest;
import com.review.agent.domain.dto.GitHubSarifUploadResultVO;
import com.review.agent.service.GitHubSarifUploadService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationSarifControllerTest {

    private final RecordingUploadService uploadService = new RecordingUploadService();
    private final IntegrationSarifController controller = new IntegrationSarifController(uploadService);

    @Test
    void delegatesUploadRequestToService() {
        GitHubSarifUploadRequest request = new GitHubSarifUploadRequest();
        request.setCommitSha("abc123");
        request.setRef("refs/heads/main");
        request.setSarif("{\"version\":\"2.1.0\",\"runs\":[]}");

        Result<GitHubSarifUploadResultVO> result = controller.upload(request);

        assertTrue(result.isSuccess());
        assertEquals("UPLOADED", result.getData().getStatus());
        assertEquals("abc123", uploadService.request.getCommitSha());
        assertEquals("refs/heads/main", uploadService.request.getRef());
    }

    private static class RecordingUploadService implements GitHubSarifUploadService {
        private GitHubSarifUploadRequest request;

        @Override
        public GitHubSarifUploadResultVO upload(GitHubSarifUploadRequest request) {
            this.request = request;
            GitHubSarifUploadResultVO result = new GitHubSarifUploadResultVO();
            result.setStatus("UPLOADED");
            result.setMessage("github sarif upload accepted");
            result.setRequestUrl("https://api.github.com/repos/zhu930824/review-agent/code-scanning/sarifs");
            return result;
        }
    }
}
