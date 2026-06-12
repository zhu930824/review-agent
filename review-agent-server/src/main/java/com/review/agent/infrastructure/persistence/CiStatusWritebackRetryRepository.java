package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.CiStatusWritebackLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CiStatusWritebackRetryRepository {

    Optional<CiStatusWritebackLog> findById(Long id);

    List<CiStatusWritebackLog> findDueFailedWritebacks(LocalDateTime now, int limit);

    void markRetrying(CiStatusWritebackLog log);
}
