package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.IntegrationActionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MybatisIntegrationActionLogRepository implements IntegrationActionLogRepository {

    private final IntegrationActionLogMapper integrationActionLogMapper;

    @Override
    public List<IntegrationActionLog> listRecent(int limit) {
        return integrationActionLogMapper.selectList(
                new LambdaQueryWrapper<IntegrationActionLog>()
                        .orderByDesc(IntegrationActionLog::getCreatedAt)
                        .last("LIMIT " + limit));
    }
}
