package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;
import com.review.agent.domain.entity.CiStatusConfig;
import com.review.agent.domain.entity.IntegrationWebhookDeliveryLog;
import com.review.agent.infrastructure.persistence.IntegrationWebhookDeliveryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationWebhookDeliveryLogServiceImplTest {

    private final FakeRepository repository = new FakeRepository();
    private final IntegrationWebhookDeliveryLogServiceImpl service = new IntegrationWebhookDeliveryLogServiceImpl(repository);

    @Test
    void listRecentClampsLimitAndMapsRecords() {
        repository.logs = List.of(log("gitlab-merge-request", "GITLAB", "Merge Request Hook", "ACCEPTED"));

        List<IntegrationWebhookDeliveryLogVO> result = service.listRecent(200);

        assertEquals(50, repository.limit);
        assertEquals("gitlab-merge-request", result.get(0).getConnectorKey());
        assertEquals("GITLAB", result.get(0).getProvider());
        assertEquals("Merge Request Hook", result.get(0).getEventType());
        assertEquals("ACCEPTED", result.get(0).getDeliveryStatus());
    }

    private IntegrationWebhookDeliveryLog log(String connectorKey, String provider, String eventType, String status) {
        IntegrationWebhookDeliveryLog log = new IntegrationWebhookDeliveryLog();
        log.setConnectorKey(connectorKey);
        log.setProvider(provider);
        log.setDeliveryId("delivery-1");
        log.setEventType(eventType);
        log.setDeliveryStatus(status);
        log.setPayloadDigest("digest");
        log.setReceivedAt(LocalDateTime.now());
        return log;
    }

    private static class FakeRepository implements IntegrationWebhookDeliveryRepository {
        private int limit;
        private List<IntegrationWebhookDeliveryLog> logs = List.of();

        @Override
        public Optional<CiStatusConfig> findConfig(String connectorKey) {
            return Optional.empty();
        }

        @Override
        public Optional<IntegrationWebhookDeliveryLog> findByDeliveryId(String connectorKey, String deliveryId) {
            return Optional.empty();
        }

        @Override
        public List<IntegrationWebhookDeliveryLog> listRecent(int limit) {
            this.limit = limit;
            return logs;
        }

        @Override
        public void save(IntegrationWebhookDeliveryLog log) {
        }
    }
}
