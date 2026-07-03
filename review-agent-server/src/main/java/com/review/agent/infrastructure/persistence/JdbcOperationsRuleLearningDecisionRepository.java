package com.review.agent.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcOperationsRuleLearningDecisionRepository implements OperationsRuleLearningDecisionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<Long> listDecidedFindingIds(int limit) {
        int safeLimit = Math.max(1, limit);
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT finding_id FROM operations_rule_learning_decision ORDER BY updated_at DESC LIMIT ?",
                Long.class,
                safeLimit));
    }

    @Override
    public void upsertDecision(Long findingId, String action, String decision, String decidedBy, String reason) {
        jdbcTemplate.update("""
                INSERT INTO operations_rule_learning_decision
                    (finding_id, action, decision, decided_by, reason)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    action = VALUES(action),
                    decision = VALUES(decision),
                    decided_by = VALUES(decided_by),
                    reason = VALUES(reason),
                    updated_at = CURRENT_TIMESTAMP
                """, findingId, action, decision, decidedBy, reason);
    }
}
