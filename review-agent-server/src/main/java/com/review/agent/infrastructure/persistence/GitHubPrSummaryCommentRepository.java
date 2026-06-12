package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.entity.CiStatusConfig;

import java.util.Optional;

public interface GitHubPrSummaryCommentRepository {

    Optional<CiStatusConfig> findConfig(String connectorKey);
}
