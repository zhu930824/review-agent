package com.review.agent.infrastructure.persistence;

import java.util.Set;

public interface OperationsRuleLearningDecisionRepository {

    Set<Long> listDecidedFindingIds(int limit);

    void upsertDecision(Long findingId, String action, String decision, String decidedBy, String reason);
}
