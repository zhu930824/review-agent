package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationsExternalIssueVO;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcOperationsExternalIssueRepository implements OperationsExternalIssueRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<OperationsIssueSyncContext> findGitLabSyncContext(String taskKey) {
        List<OperationsIssueSyncContext> contexts = jdbcTemplate.query("""
                SELECT ot.task_key, ot.source_type, ot.source_id, ot.source_ref, ot.title, ot.severity,
                       ot.owner_role, ot.latest_signal, ot.recommendation,
                       rf.review_id, r.project_id, p.name AS project_name,
                       gl.id AS gitlab_config_id, gl.gitlab_host, gl.gitlab_token, gl.project_path, gl.enabled
                FROM operations_task ot
                LEFT JOIN review_finding rf
                    ON ot.source_type = 'FINDING'
                   AND ot.source_id = CAST(rf.id AS CHAR)
                LEFT JOIN review r ON rf.review_id = r.id
                LEFT JOIN project p ON r.project_id = p.id
                LEFT JOIN project_gitlab_config gl
                    ON r.project_id = gl.project_id
                   AND gl.enabled = 1
                WHERE ot.task_key = ?
                LIMIT 1
                """, (rs, rowNum) -> {
            ProjectGitLabConfig config = null;
            Long configId = rs.getObject("gitlab_config_id", Long.class);
            if (configId != null) {
                config = new ProjectGitLabConfig();
                config.setId(configId);
                config.setProjectId(rs.getObject("project_id", Long.class));
                config.setGitlabHost(rs.getString("gitlab_host"));
                config.setGitlabToken(rs.getString("gitlab_token"));
                config.setProjectPath(rs.getString("project_path"));
                config.setEnabled(rs.getBoolean("enabled"));
            }
            return new OperationsIssueSyncContext(
                    rs.getString("task_key"),
                    rs.getString("source_type"),
                    rs.getString("source_id"),
                    rs.getString("source_ref"),
                    rs.getString("title"),
                    rs.getString("severity"),
                    rs.getString("owner_role"),
                    rs.getString("latest_signal"),
                    rs.getString("recommendation"),
                    rs.getObject("review_id", Long.class),
                    rs.getObject("project_id", Long.class),
                    rs.getString("project_name"),
                    config);
        }, taskKey);
        return contexts.stream().findFirst();
    }

    @Override
    public Optional<OperationsExternalIssueVO> findLatestByTaskKey(String taskKey) {
        List<OperationsExternalIssueVO> links = jdbcTemplate.query("""
                SELECT task_key, provider, issue_status, external_issue_id, external_issue_iid,
                       external_issue_url, external_issue_state, external_issue_title, external_issue_labels,
                       external_issue_assignee, external_issue_author, external_updated_at, external_closed_at,
                       request_url, error_message, synced_at
                FROM operations_external_issue_link
                WHERE task_key = ?
                ORDER BY synced_at DESC, id DESC
                LIMIT 1
                """, this::toVO, taskKey);
        return links.stream().findFirst();
    }

    @Override
    public Map<String, OperationsExternalIssueVO> findLatestByTaskKeys(List<String> taskKeys) {
        if (taskKeys == null || taskKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = taskKeys.stream().map(ignored -> "?").collect(Collectors.joining(","));
        return jdbcTemplate.query("""
                SELECT l.task_key, l.provider, l.issue_status, l.external_issue_id, l.external_issue_iid,
                       l.external_issue_url, l.external_issue_state, l.external_issue_title, l.external_issue_labels,
                       l.external_issue_assignee, l.external_issue_author, l.external_updated_at, l.external_closed_at,
                       l.request_url, l.error_message, l.synced_at
                FROM operations_external_issue_link l
                JOIN (
                    SELECT task_key, MAX(id) AS id
                    FROM operations_external_issue_link
                    WHERE task_key IN (%s)
                    GROUP BY task_key
                ) latest ON l.task_key = latest.task_key AND l.id = latest.id
                """.formatted(placeholders), this::toVO, taskKeys.toArray())
                .stream()
                .collect(Collectors.toMap(OperationsExternalIssueVO::getTaskKey, Function.identity(), (left, right) -> left));
    }

    @Override
    public List<OperationsExternalIssueVO> findRecentGitLabRefreshCandidates(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query("""
                SELECT l.task_key, l.provider, l.issue_status, l.external_issue_id, l.external_issue_iid,
                       l.external_issue_url, l.external_issue_state, l.external_issue_title, l.external_issue_labels,
                       l.external_issue_assignee, l.external_issue_author, l.external_updated_at, l.external_closed_at,
                       l.request_url, l.error_message, l.synced_at
                FROM operations_external_issue_link l
                JOIN (
                    SELECT task_key, MAX(id) AS id
                    FROM operations_external_issue_link
                    WHERE provider = 'GITLAB'
                    GROUP BY task_key
                ) latest ON l.task_key = latest.task_key AND l.id = latest.id
                WHERE l.provider = 'GITLAB'
                  AND l.issue_status = 'SYNCED'
                  AND l.external_issue_iid IS NOT NULL
                  AND l.external_issue_iid <> ''
                  AND (l.external_issue_state IS NULL OR l.external_issue_state <> 'closed')
                ORDER BY l.synced_at ASC, l.id ASC
                LIMIT ?
                """, this::toVO, safeLimit);
    }

    @Override
    public OperationsExternalIssueVO record(
            String taskKey,
            String provider,
            String issueStatus,
            String externalIssueId,
            String externalIssueIid,
            String externalIssueUrl,
            String externalIssueState,
            String requestUrl,
            String errorMessage) {
        return recordDetailed(
                taskKey,
                provider,
                issueStatus,
                externalIssueId,
                externalIssueIid,
                externalIssueUrl,
                externalIssueState,
                null,
                null,
                null,
                null,
                null,
                null,
                requestUrl,
                errorMessage);
    }

    @Override
    public OperationsExternalIssueVO recordDetailed(
            String taskKey,
            String provider,
            String issueStatus,
            String externalIssueId,
            String externalIssueIid,
            String externalIssueUrl,
            String externalIssueState,
            String externalIssueTitle,
            String externalIssueLabels,
            String externalIssueAssignee,
            String externalIssueAuthor,
            java.time.LocalDateTime externalUpdatedAt,
            java.time.LocalDateTime externalClosedAt,
            String requestUrl,
            String errorMessage) {
        jdbcTemplate.update("""
                INSERT INTO operations_external_issue_link
                    (task_key, provider, issue_status, external_issue_id, external_issue_iid,
                     external_issue_url, external_issue_state, external_issue_title, external_issue_labels,
                     external_issue_assignee, external_issue_author, external_updated_at, external_closed_at,
                     request_url, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                taskKey,
                provider,
                issueStatus,
                externalIssueId,
                externalIssueIid,
                externalIssueUrl,
                externalIssueState,
                truncate(externalIssueTitle, 500),
                truncate(externalIssueLabels, 500),
                truncate(externalIssueAssignee, 160),
                truncate(externalIssueAuthor, 160),
                externalUpdatedAt == null ? null : Timestamp.valueOf(externalUpdatedAt),
                externalClosedAt == null ? null : Timestamp.valueOf(externalClosedAt),
                requestUrl,
                truncate(errorMessage));
        return findLatestByTaskKey(taskKey).orElseGet(() -> {
            OperationsExternalIssueVO vo = new OperationsExternalIssueVO();
            vo.setTaskKey(taskKey);
            vo.setProvider(provider);
            vo.setIssueStatus(issueStatus);
            vo.setExternalIssueId(externalIssueId);
            vo.setExternalIssueIid(externalIssueIid);
            vo.setExternalIssueUrl(externalIssueUrl);
            vo.setExternalIssueState(externalIssueState);
            vo.setExternalIssueTitle(truncate(externalIssueTitle, 500));
            vo.setExternalIssueLabels(truncate(externalIssueLabels, 500));
            vo.setExternalIssueAssignee(truncate(externalIssueAssignee, 160));
            vo.setExternalIssueAuthor(truncate(externalIssueAuthor, 160));
            vo.setExternalUpdatedAt(externalUpdatedAt);
            vo.setExternalClosedAt(externalClosedAt);
            vo.setRequestUrl(requestUrl);
            vo.setErrorMessage(truncate(errorMessage));
            return vo;
        });
    }

    private OperationsExternalIssueVO toVO(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        OperationsExternalIssueVO vo = new OperationsExternalIssueVO();
        vo.setTaskKey(rs.getString("task_key"));
        vo.setProvider(rs.getString("provider"));
        vo.setIssueStatus(rs.getString("issue_status"));
        vo.setExternalIssueId(rs.getString("external_issue_id"));
        vo.setExternalIssueIid(rs.getString("external_issue_iid"));
        vo.setExternalIssueUrl(rs.getString("external_issue_url"));
        vo.setExternalIssueState(rs.getString("external_issue_state"));
        vo.setExternalIssueTitle(rs.getString("external_issue_title"));
        vo.setExternalIssueLabels(rs.getString("external_issue_labels"));
        vo.setExternalIssueAssignee(rs.getString("external_issue_assignee"));
        vo.setExternalIssueAuthor(rs.getString("external_issue_author"));
        Timestamp externalUpdatedAt = rs.getTimestamp("external_updated_at");
        vo.setExternalUpdatedAt(externalUpdatedAt == null ? null : externalUpdatedAt.toLocalDateTime());
        Timestamp externalClosedAt = rs.getTimestamp("external_closed_at");
        vo.setExternalClosedAt(externalClosedAt == null ? null : externalClosedAt.toLocalDateTime());
        vo.setRequestUrl(rs.getString("request_url"));
        vo.setErrorMessage(rs.getString("error_message"));
        Timestamp syncedAt = rs.getTimestamp("synced_at");
        vo.setSyncedAt(syncedAt == null ? null : syncedAt.toLocalDateTime());
        return vo;
    }

    private String truncate(String value) {
        return truncate(value, 1000);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
