package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationActionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisGitHubPrSummaryCommentRepository implements GitHubPrSummaryCommentRepository {

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final IntegrationActionLogMapper integrationActionLogMapper;

    @Override
    public Optional<CiStatusConfig> findConfig(String connectorKey) {
        return Optional.ofNullable(ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, connectorKey)));
    }

    @Override
    public void recordAction(String actionType, String status, String targetKey, String requestUrl, String errorMessage) {
        IntegrationActionLog log = new IntegrationActionLog();
        log.setConnectorKey("github-checks");
        log.setProvider("GITHUB");
        log.setActionType(actionType);
        log.setActionStatus(status);
        log.setTargetKey(targetKey);
        log.setRequestUrl(requestUrl);
        log.setErrorMessage(errorMessage);
        integrationActionLogMapper.insert(log);
    }
}
