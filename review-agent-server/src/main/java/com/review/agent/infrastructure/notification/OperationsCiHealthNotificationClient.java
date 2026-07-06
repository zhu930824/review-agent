package com.review.agent.infrastructure.notification;

import com.review.agent.domain.dto.OperationsCiHealthActionVO;

public interface OperationsCiHealthNotificationClient {

    void send(String webhookUrl, OperationsCiHealthActionVO action);
}
