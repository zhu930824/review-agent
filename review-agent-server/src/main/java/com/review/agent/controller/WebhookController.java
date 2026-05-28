package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.webhook.WebhookDispatchService;
import com.review.agent.infrastructure.webhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookDispatchService dispatchService;

    @PostMapping("/{source}")
    public Result<String> receiveWebhook(
            @PathVariable("source") String source,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {
        log.info("收到 webhook: source={}", source);
        WebhookEvent event = dispatchService.handle(source, payload, signature);
        if (event == null) {
            return Result.success("ignored");
        }
        log.info("处理 webhook 事件: type={}, project={}", event.getEventType(), event.getProjectKey());
        return Result.success("received");
    }
}
