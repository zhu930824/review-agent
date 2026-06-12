package com.review.agent.service;

public interface CiStatusWritebackRetryService {

    void retry(Long writebackLogId);

    int retryDueWritebacks();
}
