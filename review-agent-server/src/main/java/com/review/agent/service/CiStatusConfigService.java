package com.review.agent.service;

import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;

public interface CiStatusConfigService {

    CiStatusConfigVO getConfig(String connectorKey);

    CiStatusConfigVO upsertConfig(UpsertCiStatusConfigRequest request);
}
