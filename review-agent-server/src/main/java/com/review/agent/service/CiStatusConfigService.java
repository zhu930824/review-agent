package com.review.agent.service;

import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;

import java.util.List;

public interface CiStatusConfigService {

    CiStatusConfigVO getConfig(String connectorKey);

    default List<CiStatusConfigVO> listConfigs(String provider) {
        return List.of();
    }

    CiStatusConfigVO upsertConfig(UpsertCiStatusConfigRequest request);

    default void deleteConfig(String connectorKey) {
        throw new UnsupportedOperationException("Deleting connector instances is not supported");
    }
}
