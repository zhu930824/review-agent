package com.review.agent.service;

import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;

import java.util.List;

public interface IntegrationWebhookDeliveryLogService {

    List<IntegrationWebhookDeliveryLogVO> listRecent(int limit);
}
