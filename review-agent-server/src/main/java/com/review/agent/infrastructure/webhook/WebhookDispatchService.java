package com.review.agent.infrastructure.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WebhookDispatchService {

    private final Map<String, WebhookHandler> handlerMap;

    public WebhookDispatchService(List<WebhookHandler> handlers) {
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(WebhookHandler::source, Function.identity()));
    }

    public WebhookEvent handle(String source, String payload, String signature) {
        WebhookHandler handler = handlerMap.get(source);
        if (handler == null) {
            log.warn("未找到 Webhook handler: source={}", source);
            return null;
        }

        if (!handler.verifySignature(payload, signature)) {
            log.warn("Webhook 签名验证失败: source={}", source);
            return null;
        }

        return handler.parse(payload, signature);
    }
}
