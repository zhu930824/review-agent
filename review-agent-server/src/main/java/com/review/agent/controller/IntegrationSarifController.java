package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.GitHubSarifUploadRequest;
import com.review.agent.domain.dto.GitHubSarifUploadResultVO;
import com.review.agent.service.GitHubSarifUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/sarif")
@RequiredArgsConstructor
public class IntegrationSarifController {

    private final GitHubSarifUploadService uploadService;

    @PostMapping("/upload")
    public Result<GitHubSarifUploadResultVO> upload(@RequestBody GitHubSarifUploadRequest request) {
        return Result.success(uploadService.upload(request));
    }
}
