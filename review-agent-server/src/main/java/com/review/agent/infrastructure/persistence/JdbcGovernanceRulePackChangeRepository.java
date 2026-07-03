package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.GovernanceRulePackChangeVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcGovernanceRulePackChangeRepository implements GovernanceRulePackChangeRepository {

    private static final String DEFAULT_RULE_PACK_KEY = "team-rule-memory";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<GovernanceRulePackChangeVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query("""
                SELECT id, rule_pack_key, finding_id, change_type, title, rationale, status, created_by, created_at
                FROM governance_rule_pack_change
                ORDER BY updated_at DESC
                LIMIT ?
                """, (rs, rowNum) -> {
            GovernanceRulePackChangeVO vo = new GovernanceRulePackChangeVO();
            vo.setId(rs.getLong("id"));
            vo.setRulePackKey(rs.getString("rule_pack_key"));
            vo.setFindingId(rs.getLong("finding_id"));
            vo.setChangeType(rs.getString("change_type"));
            vo.setTitle(rs.getString("title"));
            vo.setRationale(rs.getString("rationale"));
            vo.setStatus(rs.getString("status"));
            vo.setCreatedBy(rs.getString("created_by"));
            vo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return vo;
        }, safeLimit);
    }

    @Override
    public void proposeFromRuleLearningCandidate(OperationRuleLearningCandidateVO candidate) {
        jdbcTemplate.update("""
                INSERT INTO governance_rule_pack_change
                    (rule_pack_key, finding_id, change_type, title, rationale, status, created_by)
                VALUES (?, ?, ?, ?, ?, 'PROPOSED', 'operations')
                ON DUPLICATE KEY UPDATE
                    rule_pack_key = VALUES(rule_pack_key),
                    change_type = VALUES(change_type),
                    title = VALUES(title),
                    rationale = VALUES(rationale),
                    status = 'PROPOSED',
                    updated_at = CURRENT_TIMESTAMP
                """,
                DEFAULT_RULE_PACK_KEY,
                candidate.getFindingId(),
                candidate.getAction(),
                candidate.getRuleTitle(),
                candidate.getReason());
    }

    @Override
    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("""
                UPDATE governance_rule_pack_change
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, status, id);
    }
}
