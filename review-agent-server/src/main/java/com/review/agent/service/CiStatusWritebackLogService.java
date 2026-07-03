package com.review.agent.service;

import com.review.agent.domain.dto.CiStatusWritebackLogVO;
import com.review.agent.domain.dto.CiIntegrationHealthVO;

import java.util.List;

public interface CiStatusWritebackLogService {

    List<CiStatusWritebackLogVO> listRecent(int limit);

    List<CiIntegrationHealthVO> listHealth(int limit);

    void recordSuccess(Long reviewId, String commitSha, String state, String requestUrl);

    void recordSuccess(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl);

    void recordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage);

    void recordFailure(String connectorKey, String provider, Long reviewId, String commitSha, String state, String requestUrl, String errorMessage);

    void recordSkipped(Long reviewId, String state, String reason);

    void recordSkipped(String connectorKey, String provider, Long reviewId, String state, String reason);
}
