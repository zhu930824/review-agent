package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.IntegrationActionLog;

import java.util.List;

public interface IntegrationActionLogRepository {

    List<IntegrationActionLog> listRecent(int limit);

    void save(IntegrationActionLog log);
}
