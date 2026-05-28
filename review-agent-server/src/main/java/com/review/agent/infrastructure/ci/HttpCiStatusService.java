package com.review.agent.infrastructure.ci;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpCiStatusService implements CiStatusService {

    @Override
    public void reportPass(Long reviewId, String description) {
        log.info("[CI-Status] review={} PASS: {}", reviewId, description);
    }

    @Override
    public void reportBlock(Long reviewId, String description) {
        log.info("[CI-Status] review={} BLOCK: {}", reviewId, description);
    }

    @Override
    public void reportRunning(Long reviewId, String description) {
        log.info("[CI-Status] review={} RUNNING: {}", reviewId, description);
    }
}
