package com.review.agent.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.GovernanceRulePackDryRunVO;
import com.review.agent.domain.dto.GovernanceRulePackVersionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcGovernanceRulePackVersionRepository implements GovernanceRulePackVersionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<GovernanceRulePackVersionVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query("""
                SELECT id, rule_pack_key, source_change_id, version_no, version_status,
                       title, controls_snapshot, rationale, created_by, created_at
                FROM governance_rule_pack_version
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """, (rs, rowNum) -> {
            GovernanceRulePackVersionVO vo = new GovernanceRulePackVersionVO();
            vo.setId(rs.getLong("id"));
            vo.setRulePackKey(rs.getString("rule_pack_key"));
            vo.setSourceChangeId(rs.getLong("source_change_id"));
            vo.setVersionNo(rs.getInt("version_no"));
            vo.setVersionStatus(rs.getString("version_status"));
            vo.setTitle(rs.getString("title"));
            vo.setControlsSnapshot(rs.getString("controls_snapshot"));
            vo.setRationale(rs.getString("rationale"));
            vo.setCreatedBy(rs.getString("created_by"));
            vo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return vo;
        }, safeLimit);
    }

    @Override
    public GovernanceRulePackDryRunVO dryRunChange(Long changeId) {
        RulePackChange change = loadChange(changeId);
        List<String> existingControls = loadExistingControls(change.rulePackKey());
        String proposedControl = buildProposedControl(change);
        String controlsSnapshot = buildControlsSnapshot(change);

        GovernanceRulePackDryRunVO vo = new GovernanceRulePackDryRunVO();
        vo.setChangeId(change.id());
        vo.setRulePackKey(change.rulePackKey());
        vo.setChangeType(change.changeType());
        vo.setTitle(change.title());
        vo.setExistingControlCount(existingControls.size());
        vo.setProposedControlCount(1);
        vo.setProposedControls(List.of(proposedControl));
        vo.setControlsSnapshot(controlsSnapshot);
        vo.setImpactSummary(List.of(
                "将在规则包 " + change.rulePackKey() + " 中新增 1 条规则学习控制项",
                "当前规则包已有 " + existingControls.size() + " 条静态控制项",
                "dry-run 仅预览变更，不会修改规则包、版本或变更状态"
        ));
        return vo;
    }

    @Override
    public void applyChange(Long changeId) {
        RulePackChange change = loadChange(changeId);
        upsertRulePackControl(change, true);
        Integer nextVersion = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(version_no), 0) + 1
                FROM governance_rule_pack_version
                WHERE rule_pack_key = ?
                """, Integer.class, change.rulePackKey());

        jdbcTemplate.update("""
                INSERT INTO governance_rule_pack_version
                    (rule_pack_key, source_change_id, version_no, version_status, title,
                     controls_snapshot, rationale, created_by)
                VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?, 'governance')
                ON DUPLICATE KEY UPDATE
                    version_status = 'ACTIVE',
                    title = VALUES(title),
                    controls_snapshot = VALUES(controls_snapshot),
                    rationale = VALUES(rationale)
                """,
                change.rulePackKey(),
                change.id(),
                nextVersion,
                change.title(),
                buildControlsSnapshot(change),
                change.rationale());
    }

    @Override
    public void rollbackChange(Long changeId) {
        RulePackChange change = loadChange(changeId);
        upsertRulePackControl(change, false);
        jdbcTemplate.update("""
                UPDATE governance_rule_pack_version
                SET version_status = 'ROLLED_BACK'
                WHERE source_change_id = ?
                """, changeId);
    }

    private void upsertRulePackControl(RulePackChange change, boolean addControl) {
        ensureRulePackExists(change);
        List<String> controls = new java.util.ArrayList<>(loadExistingControls(change.rulePackKey()));
        String proposedControl = buildProposedControl(change);
        if (addControl && !controls.contains(proposedControl)) {
            controls.add(proposedControl);
        }
        if (!addControl) {
            controls.removeIf(proposedControl::equals);
        }
        jdbcTemplate.update("""
                UPDATE governance_rule_pack
                SET controls = ?, updated_at = CURRENT_TIMESTAMP
                WHERE rule_pack_key = ?
                """, writeControls(controls), change.rulePackKey());
    }

    private void ensureRulePackExists(RulePackChange change) {
        jdbcTemplate.update("""
                INSERT INTO governance_rule_pack
                    (rule_pack_key, name, business_outcome, controls, capability_keys, strategy_keys, human_checkpoints, enabled)
                VALUES (?, ?, ?, '[]', '[]', '[]', '[]', 1)
                ON DUPLICATE KEY UPDATE
                    updated_at = updated_at
                """,
                change.rulePackKey(),
                "Team rule memory",
                "Rules learned from confirmed review findings.");
    }

    private List<String> loadExistingControls(String rulePackKey) {
        String controls = jdbcTemplate.query("""
                SELECT controls
                FROM governance_rule_pack
                WHERE rule_pack_key = ?
                """, (rs, rowNum) -> rs.getString("controls"), rulePackKey)
                .stream()
                .findFirst()
                .orElse("[]");
        if (controls == null || controls.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(controls, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String writeControls(List<String> controls) {
        try {
            return objectMapper.writeValueAsString(controls);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write rule pack controls", e);
        }
    }

    private RulePackChange loadChange(Long changeId) {
        return jdbcTemplate.query("""
                SELECT id, rule_pack_key, finding_id, change_type, title, rationale
                FROM governance_rule_pack_change
                WHERE id = ?
                """, (rs, rowNum) -> new RulePackChange(
                rs.getLong("id"),
                rs.getString("rule_pack_key"),
                rs.getLong("finding_id"),
                rs.getString("change_type"),
                rs.getString("title"),
                rs.getString("rationale")
        ), changeId).stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("Rule pack change not found: " + changeId));
    }

    private String buildControlsSnapshot(RulePackChange change) {
        try {
            LinkedHashMap<String, Object> control = buildProposedControlObject(change);
            return objectMapper.writeValueAsString(List.of(control));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build rule pack version snapshot", e);
        }
    }

    private String buildProposedControl(RulePackChange change) {
        String prefix = "SUPPRESS_PATTERN".equals(change.changeType()) ? "抑制噪声模式" : "沉淀团队规则";
        return prefix + "：" + change.title() + "（Finding #" + change.findingId() + "）";
    }

    private LinkedHashMap<String, Object> buildProposedControlObject(RulePackChange change) {
        LinkedHashMap<String, Object> control = new LinkedHashMap<>();
        control.put("source", "rule-learning");
        control.put("changeType", change.changeType());
        control.put("title", change.title());
        control.put("findingId", change.findingId());
        control.put("control", buildProposedControl(change));
        return control;
    }

    private record RulePackChange(
            Long id,
            String rulePackKey,
            Long findingId,
            String changeType,
            String title,
            String rationale
    ) {
    }
}
