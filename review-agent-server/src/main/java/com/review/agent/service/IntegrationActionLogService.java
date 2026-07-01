package com.review.agent.service;

import com.review.agent.domain.dto.IntegrationActionLogVO;

import java.util.List;

public interface IntegrationActionLogService {

    List<IntegrationActionLogVO> listRecent(int limit);
}
