package com.review.agent.service;

import com.review.agent.domain.dto.GitHubSarifUploadRequest;
import com.review.agent.domain.dto.GitHubSarifUploadResultVO;

public interface GitHubSarifUploadService {

    GitHubSarifUploadResultVO upload(GitHubSarifUploadRequest request);
}
