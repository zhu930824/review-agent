package com.review.agent.infrastructure.webhook;

import java.time.LocalDateTime;

public record GitLabMergeRequestReviewTriggerResult(
        String status,
        String triggerKey,
        Long reviewId,
        String message,
        Integer retryCount,
        LocalDateTime nextRetryAt) {

    public static GitLabMergeRequestReviewTriggerResult processed(String triggerKey, Long reviewId) {
        return processed(triggerKey, reviewId, null);
    }

    public static GitLabMergeRequestReviewTriggerResult processed(
            String triggerKey, Long reviewId, String message) {
        return new GitLabMergeRequestReviewTriggerResult(
                "PROCESSED", triggerKey, reviewId, message, null, null);
    }

    public static GitLabMergeRequestReviewTriggerResult deduplicated(String triggerKey, Long reviewId, String message) {
        return new GitLabMergeRequestReviewTriggerResult("DEDUPLICATED", triggerKey, reviewId, message, null, null);
    }

    public static GitLabMergeRequestReviewTriggerResult skipped(String message) {
        return new GitLabMergeRequestReviewTriggerResult("SKIPPED", null, null, message, null, null);
    }

    public static GitLabMergeRequestReviewTriggerResult failed(String triggerKey, String message) {
        return failed(triggerKey, message, null, null);
    }

    public static GitLabMergeRequestReviewTriggerResult failed(
            String triggerKey,
            String message,
            Integer retryCount,
            LocalDateTime nextRetryAt) {
        return new GitLabMergeRequestReviewTriggerResult(
                "FAILED", triggerKey, null, message, retryCount, nextRetryAt);
    }

    public static GitLabMergeRequestReviewTriggerResult exhausted(
            String triggerKey,
            String message,
            Integer retryCount) {
        return new GitLabMergeRequestReviewTriggerResult(
                "EXHAUSTED", triggerKey, null, message, retryCount, null);
    }
}
