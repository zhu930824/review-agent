package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisGitHubPrSummaryCommentRepository implements GitHubPrSummaryCommentRepository {

    private final CiStatusConfigMapper ciStatusConfigMapper;

    @Override
    public Optional<CiStatusConfig> findConfig(String connectorKey) {
        return Optional.ofNullable(ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, connectorKey)));
    }
}
