package com.review.agent.infrastructure.ci;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpCiStatusService implements CiStatusService {

    private final CiStatusPayloadFactory payloadFactory;
    private final HttpCiStatusPublisher publisher;

    @Override
    public void reportPass(Long reviewId, String description) {
        logPayload(payloadFactory.pass(reviewId, description));
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        logPayload(payloadFactory.block(reviewId, description));
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        logPayload(payloadFactory.running(reviewId, description));
    }

    private void logPayload(CiStatusPayload payload) {
        log.info("[CI-Status] review={} state={} context={} targetUrl={} description={}",
                payload.reviewId(),
                payload.state(),
                payload.context(),
                payload.targetUrl(),
                payload.description());
        publisher.publish(payload);
    }
}
