package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisIntegrationWebhookDeliveryRepository implements IntegrationWebhookDeliveryRepository {

    private final CiStatusConfigMapper ciStatusConfigMapper;
    private final IntegrationWebhookDeliveryLogMapper deliveryLogMapper;

    @Override
    public Optional<CiStatusConfig> findConfig(String connectorKey) {
        return Optional.ofNullable(ciStatusConfigMapper.selectOne(
                new LambdaQueryWrapper<CiStatusConfig>().eq(CiStatusConfig::getConnectorKey, connectorKey)));
    }

    @Override
    public Optional<IntegrationWebhookDeliveryLog> findByDeliveryId(String connectorKey, String deliveryId) {
        return Optional.ofNullable(deliveryLogMapper.selectOne(
                new LambdaQueryWrapper<IntegrationWebhookDeliveryLog>()
                        .eq(IntegrationWebhookDeliveryLog::getConnectorKey, connectorKey)
                        .eq(IntegrationWebhookDeliveryLog::getDeliveryId, deliveryId)));
    }

    @Override
    public void save(IntegrationWebhookDeliveryLog log) {
        deliveryLogMapper.insert(log);
    }
}
