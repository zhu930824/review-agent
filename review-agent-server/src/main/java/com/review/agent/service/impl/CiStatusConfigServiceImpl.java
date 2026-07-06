package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.service.CiStatusConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CiStatusConfigServiceImpl implements CiStatusConfigService {

    private static final String DEFAULT_CONNECTOR_KEY = "github-checks";

    private final CiStatusConfigMapper ciStatusConfigMapper;

    @Override
    public CiStatusConfigVO getConfig(String connectorKey) {
        String key = normalizeConnectorKey(connectorKey);
        CiStatusConfig config = ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, key));
        if (config == null) {
            CiStatusConfig empty = new CiStatusConfig();
            empty.setConnectorKey(key);
            empty.setProvider("GITHUB");
            empty.setDefaultBranch("main");
            empty.setStatusContext("Review Agent");
            empty.setChecksEnabled(true);
            empty.setSarifUploadEnabled(false);
            return toVO(empty);
        }
        return toVO(config);
    }

    @Override
    public CiStatusConfigVO upsertConfig(UpsertCiStatusConfigRequest request) {
        String key = normalizeConnectorKey(request.getConnectorKey());
        CiStatusConfig config = ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, key));
        LocalDateTime now = LocalDateTime.now();
        if (config == null) {
            config = new CiStatusConfig();
            config.setConnectorKey(key);
            config.setCreatedAt(now);
        }

        config.setProvider(request.getProvider());
        config.setRepoOwner(request.getRepoOwner());
        config.setRepoName(request.getRepoName());
        config.setRepoUrl(request.getRepoUrl());
        config.setDefaultBranch(request.getDefaultBranch());
        config.setStatusContext(request.getStatusContext());
        config.setJenkinsParameterTemplate(trimToNull(request.getJenkinsParameterTemplate()));
        config.setNotificationWebhookUrl(trimToNull(request.getNotificationWebhookUrl()));
        config.setChecksEnabled(request.getChecksEnabled());
        config.setSarifUploadEnabled(request.getSarifUploadEnabled());
        if (hasText(request.getApiToken())) {
            config.setApiToken(request.getApiToken());
        }
        if (hasText(request.getWebhookSecret())) {
            config.setWebhookSecret(request.getWebhookSecret());
        }
        config.setUpdatedAt(now);

        if (config.getId() == null) {
            ciStatusConfigMapper.insert(config);
        } else {
            ciStatusConfigMapper.updateById(config);
        }
        return toVO(config);
    }

    private String normalizeConnectorKey(String connectorKey) {
        return hasText(connectorKey) ? connectorKey : DEFAULT_CONNECTOR_KEY;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private CiStatusConfigVO toVO(CiStatusConfig config) {
        CiStatusConfigVO vo = new CiStatusConfigVO();
        vo.setId(config.getId());
        vo.setConnectorKey(config.getConnectorKey());
        vo.setProvider(config.getProvider());
        vo.setRepoOwner(config.getRepoOwner());
        vo.setRepoName(config.getRepoName());
        vo.setRepoUrl(config.getRepoUrl());
        vo.setDefaultBranch(config.getDefaultBranch());
        vo.setStatusContext(config.getStatusContext());
        vo.setJenkinsParameterTemplate(config.getJenkinsParameterTemplate());
        vo.setNotificationWebhookUrl(config.getNotificationWebhookUrl());
        vo.setChecksEnabled(config.getChecksEnabled());
        vo.setSarifUploadEnabled(config.getSarifUploadEnabled());
        vo.setTokenConfigured(hasText(config.getApiToken()));
        vo.setWebhookSecretConfigured(hasText(config.getWebhookSecret()));
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
