package com.review.agent.service;

import com.review.agent.domain.dto.CiStatusWritebackLogVO;

import java.util.List;

public interface CiStatusWritebackLogService {

    List<CiStatusWritebackLogVO> listRecent(int limit);

    void recordSuccess(Long reviewId, String commitSha, String state, String requestUrl);

    void recordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage);

    void recordSkipped(Long reviewId, String state, String reason);
}
