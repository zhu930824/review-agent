package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import com.review.agent.service.IntegrationWebhookDeliveryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationWebhookDeliveryLogServiceImpl implements IntegrationWebhookDeliveryLogService {

    private final IntegrationWebhookDeliveryRepository repository;

    @Override
    public List<IntegrationWebhookDeliveryLogVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return repository.listRecent(safeLimit).stream()
                .map(this::toVO)
                .toList();
    }

    private IntegrationWebhookDeliveryLogVO toVO(IntegrationWebhookDeliveryLog log) {
        IntegrationWebhookDeliveryLogVO vo = new IntegrationWebhookDeliveryLogVO();
        vo.setId(log.getId());
        vo.setConnectorKey(log.getConnectorKey());
        vo.setProvider(log.getProvider());
        vo.setDeliveryId(log.getDeliveryId());
        vo.setEventType(log.getEventType());
        vo.setDeliveryStatus(log.getDeliveryStatus());
        vo.setPayloadDigest(log.getPayloadDigest());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setReceivedAt(log.getReceivedAt());
        vo.setProcessedAt(log.getProcessedAt());
        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());
        return vo;
    }
}
