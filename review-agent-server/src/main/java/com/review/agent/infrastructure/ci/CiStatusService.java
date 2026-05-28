package com.review.agent.infrastructure.ci;

public interface CiStatusService {

    void reportPass(Long reviewId, String description);

    void reportBlock(Long reviewId, String description);

    void reportRunning(Long reviewId, String description);
}
