package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.CiStatusConfigVO;
import com.review.agent.domain.dto.UpsertCiStatusConfigRequest;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.infrastructure.persistence.CiStatusConfigMapper;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.service.CiStatusConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CiStatusConfigServiceImpl implements CiStatusConfigService {

    private static final String DEFAULT_CONNECTOR_KEY = "github-checks";
    private static final String JENKINS_CONNECTOR_KEY = "jenkins-pipeline";
    private static final Pattern INSTANCE_KEY_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]{0,63}");

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final ProjectMapper projectMapper;

    @Override
    public CiStatusConfigVO getConfig(String connectorKey) {
        String key = normalizeConnectorKey(connectorKey);
        CiStatusConfig config = ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, key));
        if (config == null) {
            CiStatusConfig empty = new CiStatusConfig();
            empty.setConnectorKey(key);
            empty.setProvider(providerForKey(key));
            empty.setDisplayName(defaultDisplayName(key));
            empty.setDefaultBranch("main");
            empty.setStatusContext("Review Agent");
            empty.setChecksEnabled(true);
            empty.setSarifUploadEnabled(false);
            return toVO(empty);
        }
        return toVO(config);
    }

    @Override
    public List<CiStatusConfigVO> listConfigs(String provider) {
        LambdaQueryWrapper<CiStatusConfig> query = new LambdaQueryWrapper<CiStatusConfig>()
                .eq(hasText(provider), CiStatusConfig::getProvider, normalizeProvider(provider))
                .orderByAsc(CiStatusConfig::getProvider)
                .orderByAsc(CiStatusConfig::getDisplayName)
                .orderByAsc(CiStatusConfig::getConnectorKey);
        return ciStatusConfigMapper.selectList(query).stream().map(this::toVO).toList();
    }

    @Override
    public CiStatusConfigVO upsertConfig(UpsertCiStatusConfigRequest request) {
        String key = normalizeConnectorKey(request.getConnectorKey());
        String provider = normalizeProvider(request.getProvider());
        validateConnectorKey(key, provider);
        validateProject(request.getProjectId());
        CiStatusConfig config = ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, key));
        LocalDateTime now = LocalDateTime.now();
        if (config == null) {
            config = new CiStatusConfig();
            config.setConnectorKey(key);
            config.setCreatedAt(now);
        }

        config.setDisplayName(hasText(request.getDisplayName())
                ? request.getDisplayName().trim()
                : defaultDisplayName(key));
        config.setProjectId(request.getProjectId());
        config.setProvider(provider);
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

    @Override
    public void deleteConfig(String connectorKey) {
        CiStatusConfig config = ciStatusConfigMapper.selectOne(new LambdaQueryWrapper<CiStatusConfig>()
                .eq(CiStatusConfig::getConnectorKey, normalizeConnectorKey(connectorKey)));
        if (config == null) {
            return;
        }
        ciStatusConfigMapper.deleteById(config.getId());
    }

    private String normalizeConnectorKey(String connectorKey) {
        return hasText(connectorKey) ? connectorKey.trim().toLowerCase(Locale.ROOT) : DEFAULT_CONNECTOR_KEY;
    }

    private String normalizeProvider(String provider) {
        return hasText(provider) ? provider.trim().toUpperCase(Locale.ROOT) : "GITHUB";
    }

    private String providerForKey(String key) {
        if (key.equals(JENKINS_CONNECTOR_KEY) || key.startsWith(JENKINS_CONNECTOR_KEY + ":")) {
            return "JENKINS";
        }
        if (key.equals("gitlab-merge-request")) {
            return "GITLAB";
        }
        return "GITHUB";
    }

    private String defaultDisplayName(String key) {
        if (key.equals(JENKINS_CONNECTOR_KEY)) {
            return "Default Jenkins";
        }
        if (key.startsWith(JENKINS_CONNECTOR_KEY + ":")) {
            return key.substring((JENKINS_CONNECTOR_KEY + ":").length());
        }
        return key;
    }

    private void validateConnectorKey(String key, String provider) {
        if ("JENKINS".equals(provider)) {
            if (key.equals(JENKINS_CONNECTOR_KEY)) {
                return;
            }
            String prefix = JENKINS_CONNECTOR_KEY + ":";
            if (!key.startsWith(prefix) || !INSTANCE_KEY_PATTERN.matcher(key.substring(prefix.length())).matches()) {
                throw new IllegalArgumentException(
                        "Jenkins connector key must be jenkins-pipeline or jenkins-pipeline:<instance-key>");
            }
            return;
        }
        if (("GITHUB".equals(provider) && !"github-checks".equals(key))
                || ("GITLAB".equals(provider) && !"gitlab-merge-request".equals(key))) {
            throw new IllegalArgumentException("Connector key does not match provider " + provider);
        }
    }

    private void validateProject(Long projectId) {
        if (projectId != null && projectMapper.selectById(projectId) == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private CiStatusConfigVO toVO(CiStatusConfig config) {
        CiStatusConfigVO vo = new CiStatusConfigVO();
        vo.setId(config.getId());
        vo.setConnectorKey(config.getConnectorKey());
        vo.setDisplayName(config.getDisplayName());
        vo.setProjectId(config.getProjectId());
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
