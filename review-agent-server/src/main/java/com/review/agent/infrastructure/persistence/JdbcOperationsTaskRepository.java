package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationsTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcOperationsTaskRepository implements OperationsTaskRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, OperationsTaskVO> listByTaskKeys(List<String> taskKeys) {
        if (taskKeys == null || taskKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = taskKeys.stream().map(ignored -> "?").collect(Collectors.joining(","));
        return jdbcTemplate.query("""
                SELECT task_key, source_type, source_id, source_ref, title, task_status, severity,
                       owner_role, sla_hours, priority_score, latest_signal, recommendation,
                       close_reason, created_at, closed_at, updated_at
                FROM operations_task
                WHERE task_key IN (%s)
                """.formatted(placeholders), this::toVO, taskKeys.toArray())
                .stream()
                .collect(Collectors.toMap(OperationsTaskVO::getTaskKey, Function.identity()));
    }

    @Override
    public List<OperationsTaskVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query("""
                SELECT task_key, source_type, source_id, source_ref, title, task_status, severity,
                       owner_role, sla_hours, priority_score, latest_signal, recommendation,
                       close_reason, created_at, closed_at, updated_at
                FROM operations_task
                ORDER BY updated_at DESC
                LIMIT ?
                """, this::toVO, safeLimit);
    }

    @Override
    public void upsertTasks(List<OperationsTaskVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (OperationsTaskVO task : tasks) {
            jdbcTemplate.update("""
                    INSERT INTO operations_task
                        (task_key, source_type, source_id, source_ref, title, task_status, severity,
                         owner_role, sla_hours, priority_score, latest_signal, recommendation)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        source_type = VALUES(source_type),
                        source_id = VALUES(source_id),
                        source_ref = VALUES(source_ref),
                        title = VALUES(title),
                        severity = VALUES(severity),
                        owner_role = VALUES(owner_role),
                        sla_hours = VALUES(sla_hours),
                        priority_score = VALUES(priority_score),
                        latest_signal = VALUES(latest_signal),
                        recommendation = VALUES(recommendation),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    task.getTaskKey(),
                    task.getSourceType(),
                    task.getSourceId(),
                    task.getSourceRef(),
                    task.getTitle(),
                    task.getStatus() == null ? "OPEN" : task.getStatus(),
                    task.getSeverity(),
                    task.getOwnerRole(),
                    task.getSlaHours(),
                    task.getPriorityScore(),
                    task.getLatestSignal(),
                    task.getRecommendation());
        }
    }

    @Override
    public void updateTask(String taskKey, String status, String ownerRole, Long slaHours) {
        jdbcTemplate.update("""
                UPDATE operations_task
                SET task_status = COALESCE(?, task_status),
                    owner_role = COALESCE(?, owner_role),
                    sla_hours = COALESCE(?, sla_hours),
                    updated_at = CURRENT_TIMESTAMP
                WHERE task_key = ?
                """, status, ownerRole, slaHours, taskKey);
    }

    @Override
    public void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours) {
        if (taskKeys == null || taskKeys.isEmpty()) {
            return;
        }
        for (String taskKey : taskKeys) {
            updateTask(taskKey, status, ownerRole, slaHours);
        }
    }

    @Override
    public void closeTask(String taskKey, String closeReason) {
        jdbcTemplate.update("""
                UPDATE operations_task
                SET task_status = 'RESOLVED',
                    close_reason = ?,
                    closed_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE task_key = ?
                """, closeReason, taskKey);
    }

    private OperationsTaskVO toVO(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        OperationsTaskVO vo = new OperationsTaskVO();
        vo.setTaskKey(rs.getString("task_key"));
        vo.setSourceType(rs.getString("source_type"));
        vo.setSourceId(rs.getString("source_id"));
        vo.setSourceRef(rs.getString("source_ref"));
        vo.setTitle(rs.getString("title"));
        vo.setStatus(rs.getString("task_status"));
        vo.setSeverity(rs.getString("severity"));
        vo.setOwnerRole(rs.getString("owner_role"));
        vo.setSlaHours(rs.getObject("sla_hours", Long.class));
        vo.setPriorityScore(rs.getObject("priority_score", Integer.class));
        vo.setLatestSignal(rs.getString("latest_signal"));
        vo.setRecommendation(rs.getString("recommendation"));
        vo.setCloseReason(rs.getString("close_reason"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp closedAt = rs.getTimestamp("closed_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        vo.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        vo.setClosedAt(closedAt == null ? null : closedAt.toLocalDateTime());
        vo.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return vo;
    }
}
