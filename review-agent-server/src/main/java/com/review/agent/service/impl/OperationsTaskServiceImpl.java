package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsExternalIssueVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.infrastructure.git.GitLabApiClient;
import com.review.agent.infrastructure.git.GitLabIssueCreateResult;
import com.review.agent.infrastructure.git.GitLabIssueStateResult;
import com.review.agent.infrastructure.persistence.OperationsExternalIssueRepository;
import com.review.agent.infrastructure.persistence.OperationsIssueSyncContext;
import com.review.agent.infrastructure.persistence.OperationsTaskRepository;
import com.review.agent.service.OperationsCiHealthActionService;
import com.review.agent.service.OperationsRemediationQueueService;
import com.review.agent.service.OperationsTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OperationsTaskServiceImpl implements OperationsTaskService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final List<String> ALLOWED_TASK_STATUSES = List.of(
            "OPEN", "IN_PROGRESS", "CONFIRMED", "RESOLVED", "ACCEPTED_RISK");

    private final OperationsRemediationQueueService remediationQueueService;
    private final OperationsCiHealthActionService ciHealthActionService;
    private final OperationsTaskRepository operationsTaskRepository;
    private final OperationsExternalIssueRepository operationsExternalIssueRepository;
    private final GitLabApiClient gitLabApiClient;

    @Override
    public List<OperationsTaskVO> listTasks(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<OperationsTaskVO> derivedTasks = deriveTasks(safeLimit);
        Map<String, OperationsTaskVO> persistedTasks = operationsTaskRepository.listByTaskKeys(
                derivedTasks.stream().map(OperationsTaskVO::getTaskKey).toList());
        Map<String, OperationsExternalIssueVO> externalIssues = operationsExternalIssueRepository.findLatestByTaskKeys(
                derivedTasks.stream().map(OperationsTaskVO::getTaskKey).toList());
        return sortAndLimit(derivedTasks.stream()
                .map(task -> enrichSlaState(mergeExternalIssue(
                        mergePersistedState(task, persistedTasks.get(task.getTaskKey())),
                        externalIssues.get(task.getTaskKey())))), safeLimit);
    }

    @Override
    public List<OperationsTaskVO> listSlaAlerts(int limit) {
        int safeLimit = normalizeLimit(limit);
        return listTasks(MAX_LIMIT).stream()
                .filter(task -> "OVERDUE".equals(task.getSlaState()) || "DUE_SOON".equals(task.getSlaState()))
                .sorted(Comparator
                        .comparing(OperationsTaskVO::getSlaState, Comparator.nullsLast(this::compareSlaState))
                        .thenComparing(OperationsTaskVO::getRemainingHours, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OperationsTaskVO::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public List<OperationsTaskVO> syncTasks(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<OperationsTaskVO> tasks = deriveTasks(safeLimit);
        operationsTaskRepository.upsertTasks(tasks);
        return listTasks(safeLimit);
    }

    @Override
    public void updateTask(String taskKey, String status, String ownerRole, Long slaHours) {
        String normalizedStatus = normalizeTaskStatus(status);
        String normalizedOwnerRole = normalizeBlank(ownerRole);
        Long normalizedSlaHours = normalizeSlaHours(slaHours);
        ensureTaskPersisted(taskKey);
        operationsTaskRepository.updateTask(
                taskKey,
                normalizedStatus,
                normalizedOwnerRole,
                normalizedSlaHours);
    }

    @Override
    public void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours) {
        List<String> normalizedTaskKeys = normalizeTaskKeys(taskKeys);
        String normalizedStatus = normalizeTaskStatus(status);
        String normalizedOwnerRole = normalizeBlank(ownerRole);
        Long normalizedSlaHours = normalizeSlaHours(slaHours);
        ensureTasksPersisted(normalizedTaskKeys);
        operationsTaskRepository.updateTasks(
                normalizedTaskKeys,
                normalizedStatus,
                normalizedOwnerRole,
                normalizedSlaHours);
    }

    @Override
    public OperationsExternalIssueVO syncGitLabIssue(String taskKey) {
        ensureTaskPersisted(taskKey);
        OperationsIssueSyncContext context = operationsExternalIssueRepository.findGitLabSyncContext(taskKey)
                .orElse(null);
        if (context == null) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Operations task is not persisted yet.");
        }
        if (!"FINDING".equals(context.sourceType())) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Only FINDING operations tasks can be synced to GitLab issues.");
        }
        if (context.gitLabConfig() == null || context.gitLabConfig().getGitlabToken() == null) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "No enabled GitLab API configuration found for task project.");
        }

        try {
            GitLabIssueCreateResult result = gitLabApiClient.createIssue(
                    context.gitLabConfig(),
                    gitLabIssueTitle(context),
                    gitLabIssueDescription(context),
                    gitLabIssueLabels(context));
            return operationsExternalIssueRepository.recordDetailed(
                    taskKey,
                    "GITLAB",
                    "SYNCED",
                    result.id(),
                    result.iid(),
                    result.webUrl(),
                    result.state(),
                    result.title(),
                    result.labels(),
                    result.assigneeUsername(),
                    result.authorUsername(),
                    result.updatedAt(),
                    result.closedAt(),
                    result.requestUrl(),
                    null);
        } catch (RuntimeException e) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "FAILED",
                    null,
                    null,
                    null,
                    null,
                    null,
                    e.getMessage());
        }
    }

    @Override
    public OperationsExternalIssueVO refreshGitLabIssue(String taskKey) {
        ensureTaskPersisted(taskKey);
        OperationsExternalIssueVO latest = operationsExternalIssueRepository.findLatestByTaskKey(taskKey)
                .orElse(null);
        if (latest == null || !"GITLAB".equals(latest.getProvider())) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "No GitLab issue link found for this operations task.");
        }
        if (latest.getExternalIssueIid() == null || latest.getExternalIssueIid().isBlank()) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    latest.getExternalIssueId(),
                    latest.getExternalIssueIid(),
                    latest.getExternalIssueUrl(),
                    latest.getExternalIssueState(),
                    latest.getRequestUrl(),
                    "GitLab issue iid is missing.");
        }
        OperationsIssueSyncContext context = operationsExternalIssueRepository.findGitLabSyncContext(taskKey)
                .orElse(null);
        if (context == null || context.gitLabConfig() == null || context.gitLabConfig().getGitlabToken() == null) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "SKIPPED",
                    latest.getExternalIssueId(),
                    latest.getExternalIssueIid(),
                    latest.getExternalIssueUrl(),
                    latest.getExternalIssueState(),
                    latest.getRequestUrl(),
                    "No enabled GitLab API configuration found for task project.");
        }

        try {
            GitLabIssueStateResult result = gitLabApiClient.getIssue(
                    context.gitLabConfig(),
                    latest.getExternalIssueIid());
            String issueState = nullToDefault(result.state(), latest.getExternalIssueState());
            if ("closed".equalsIgnoreCase(issueState)) {
                operationsTaskRepository.closeTask(
                        taskKey,
                        "Closed from GitLab issue #" + latest.getExternalIssueIid() + ".");
            }
            return operationsExternalIssueRepository.recordDetailed(
                    taskKey,
                    "GITLAB",
                    "SYNCED",
                    nullToDefault(result.id(), latest.getExternalIssueId()),
                    nullToDefault(result.iid(), latest.getExternalIssueIid()),
                    nullToDefault(result.webUrl(), latest.getExternalIssueUrl()),
                    issueState,
                    nullToDefault(result.title(), latest.getExternalIssueTitle()),
                    nullToDefault(result.labels(), latest.getExternalIssueLabels()),
                    nullToDefault(result.assigneeUsername(), latest.getExternalIssueAssignee()),
                    nullToDefault(result.authorUsername(), latest.getExternalIssueAuthor()),
                    result.updatedAt() == null ? latest.getExternalUpdatedAt() : result.updatedAt(),
                    result.closedAt() == null ? latest.getExternalClosedAt() : result.closedAt(),
                    result.requestUrl(),
                    null);
        } catch (RuntimeException e) {
            return operationsExternalIssueRepository.record(
                    taskKey,
                    "GITLAB",
                    "FAILED",
                    latest.getExternalIssueId(),
                    latest.getExternalIssueIid(),
                    latest.getExternalIssueUrl(),
                    latest.getExternalIssueState(),
                    latest.getRequestUrl(),
                    e.getMessage());
        }
    }

    @Override
    public int refreshRecentGitLabIssues(int limit) {
        int safeLimit = normalizeLimit(limit);
        int refreshed = 0;
        for (OperationsExternalIssueVO candidate : operationsExternalIssueRepository.findRecentGitLabRefreshCandidates(safeLimit)) {
            try {
                OperationsExternalIssueVO result = refreshGitLabIssue(candidate.getTaskKey());
                if ("SYNCED".equals(result.getIssueStatus())) {
                    refreshed++;
                }
            } catch (RuntimeException ignored) {
                // The per-task refresh records FAILED where possible; keep the batch moving.
            }
        }
        return refreshed;
    }

    @Override
    public void closeTask(String taskKey, String closeReason) {
        ensureTaskPersisted(taskKey);
        operationsTaskRepository.closeTask(taskKey, closeReason == null || closeReason.isBlank()
                ? "Closed from operations workbench."
                : closeReason);
    }

    private List<OperationsTaskVO> deriveTasks(int safeLimit) {
        Stream<OperationsTaskVO> findingTasks = remediationQueueService.listQueue(safeLimit).stream()
                .map(this::fromFinding);
        Stream<OperationsTaskVO> ciTasks = ciHealthActionService.listActions().stream()
                .map(this::fromCiHealthAction);

        return sortAndLimit(Stream.concat(findingTasks, ciTasks), safeLimit);
    }

    private void ensureTaskPersisted(String taskKey) {
        if (taskKey == null || taskKey.isBlank()) {
            throw new IllegalArgumentException("Task key is required.");
        }
        ensureTasksPersisted(List.of(taskKey));
    }

    private void ensureTasksPersisted(List<String> taskKeys) {
        List<OperationsTaskVO> matchingTasks = deriveTasks(MAX_LIMIT).stream()
                .filter(task -> taskKeys.contains(task.getTaskKey()))
                .toList();
        operationsTaskRepository.upsertTasks(matchingTasks);
    }

    private List<OperationsTaskVO> sortAndLimit(Stream<OperationsTaskVO> tasks, int safeLimit) {
        return tasks
                .sorted(Comparator
                        .comparing(OperationsTaskVO::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OperationsTaskVO::getSlaHours, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OperationsTaskVO::getTaskKey))
                .limit(safeLimit)
                .toList();
    }

    private OperationsTaskVO mergePersistedState(OperationsTaskVO task, OperationsTaskVO persisted) {
        if (persisted == null) {
            return task;
        }
        task.setStatus(persisted.getStatus());
        task.setOwnerRole(persisted.getOwnerRole());
        task.setSlaHours(persisted.getSlaHours());
        task.setCloseReason(persisted.getCloseReason());
        task.setCreatedAt(persisted.getCreatedAt());
        task.setClosedAt(persisted.getClosedAt());
        task.setUpdatedAt(persisted.getUpdatedAt());
        return task;
    }

    private OperationsTaskVO mergeExternalIssue(OperationsTaskVO task, OperationsExternalIssueVO externalIssue) {
        task.setExternalIssue(externalIssue);
        return task;
    }

    private OperationsTaskVO enrichSlaState(OperationsTaskVO task) {
        if (task.getSlaHours() == null || task.getSlaHours() <= 0) {
            task.setSlaState("NO_SLA");
            return task;
        }
        if ("RESOLVED".equals(task.getStatus()) || "ACCEPTED_RISK".equals(task.getStatus())) {
            task.setSlaState("CLOSED");
            return task;
        }
        LocalDateTime anchor = task.getCreatedAt();
        if (anchor == null) {
            task.setSlaState("UNTRACKED");
            return task;
        }
        LocalDateTime dueAt = anchor.plusHours(task.getSlaHours());
        long remainingHours = Duration.between(LocalDateTime.now(), dueAt).toHours();
        task.setSlaDueAt(dueAt);
        task.setRemainingHours(remainingHours);
        if (remainingHours < 0) {
            task.setSlaState("OVERDUE");
            return task;
        }
        long dueSoonThreshold = Math.max(4L, Math.round(task.getSlaHours() * 0.25));
        task.setSlaState(remainingHours <= dueSoonThreshold ? "DUE_SOON" : "ON_TRACK");
        return task;
    }

    private int compareSlaState(String left, String right) {
        return Integer.compare(slaStateRank(left), slaStateRank(right));
    }

    private int slaStateRank(String state) {
        if ("OVERDUE".equals(state)) {
            return 0;
        }
        if ("DUE_SOON".equals(state)) {
            return 1;
        }
        return 2;
    }

    private String gitLabIssueTitle(OperationsIssueSyncContext context) {
        return "[Review Agent] " + nullToDefault(context.title(), context.taskKey());
    }

    private String gitLabIssueDescription(OperationsIssueSyncContext context) {
        return """
                ## Review Agent Operations Task

                - Task: `%s`
                - Source: `%s`
                - Review: `%s`
                - Project: `%s`
                - Severity: `%s`
                - Owner: `%s`
                - Latest signal: `%s`

                %s
                """.formatted(
                context.taskKey(),
                context.sourceRef(),
                context.reviewId() == null ? "-" : "#" + context.reviewId(),
                nullToDefault(context.projectName(), "-"),
                nullToDefault(context.severity(), "-"),
                nullToDefault(context.ownerRole(), "-"),
                nullToDefault(context.latestSignal(), "-"),
                nullToDefault(context.recommendation(), "Please triage and close this task from Review Agent."));
    }

    private String gitLabIssueLabels(OperationsIssueSyncContext context) {
        return "review-agent,code-review," + nullToDefault(context.severity(), "UNKNOWN").toLowerCase();
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private OperationsTaskVO fromFinding(OperationFindingVO finding) {
        OperationsTaskVO task = new OperationsTaskVO();
        task.setTaskKey("FINDING-" + finding.getId());
        task.setSourceType("FINDING");
        task.setSourceId(String.valueOf(finding.getId()));
        task.setSourceRef("Review #" + finding.getReviewId());
        task.setTitle(finding.getTitle());
        task.setStatus(status(finding.getHumanStatus()));
        task.setSeverity(name(finding.getSeverity()));
        task.setOwnerRole(ownerRole(finding.getCategory()));
        task.setSlaHours(slaHours(finding.getSeverity()));
        task.setPriorityScore(priorityScore(finding));
        task.setLatestSignal(finding.getProjectName());
        task.setRecommendation("Confirm, dismiss, or open the review detail to drive this finding to closure.");
        return task;
    }

    private OperationsTaskVO fromCiHealthAction(OperationsCiHealthActionVO action) {
        OperationsTaskVO task = new OperationsTaskVO();
        task.setTaskKey("CI-" + action.getKey());
        task.setSourceType("CI_HEALTH");
        task.setSourceId(action.getConnectorKey());
        task.setSourceRef(action.getProvider());
        task.setTitle(action.getProvider() + " integration health: " + action.getHealthStatus());
        task.setStatus("OPEN");
        task.setSeverity(action.getSeverity());
        task.setOwnerRole(action.getOwnerRole());
        task.setSlaHours(action.getSlaHours());
        task.setPriorityScore(ciPriorityScore(action.getSeverity()));
        task.setLatestSignal(action.getLatestSignal());
        task.setRecommendation(action.getRecommendation());
        return task;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeTaskStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_TASK_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported operations task status: " + status);
        }
        return normalized;
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Long normalizeSlaHours(Long slaHours) {
        if (slaHours == null) {
            return null;
        }
        if (slaHours <= 0) {
            throw new IllegalArgumentException("SLA hours must be positive.");
        }
        return Math.min(slaHours, 720L);
    }

    private List<String> normalizeTaskKeys(List<String> taskKeys) {
        if (taskKeys == null || taskKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one task key is required.");
        }
        List<String> normalized = taskKeys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(taskKey -> !taskKey.isBlank())
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("At least one task key is required.");
        }
        if (normalized.size() > 50) {
            throw new IllegalArgumentException("Batch task update supports at most 50 tasks.");
        }
        return normalized;
    }

    private String status(HumanStatus humanStatus) {
        if (humanStatus == HumanStatus.CONFIRMED) {
            return "CONFIRMED";
        }
        return "OPEN";
    }

    private String name(Object value) {
        return value == null ? "UNKNOWN" : value.toString();
    }

    private String ownerRole(FindingCategory category) {
        if (category == FindingCategory.SECURITY) {
            return "Security Owner";
        }
        if (category == FindingCategory.PERFORMANCE) {
            return "Performance Owner";
        }
        if (category == FindingCategory.BUG || category == FindingCategory.EXCEPTION_HANDLING) {
            return "Tech Lead";
        }
        return "Code Owner";
    }

    private Long slaHours(Severity severity) {
        if (severity == Severity.BLOCKER) {
            return 4L;
        }
        if (severity == Severity.MAJOR) {
            return 24L;
        }
        return 72L;
    }

    private Integer priorityScore(OperationFindingVO finding) {
        int score = severityScore(finding.getSeverity());
        if (finding.getHumanStatus() == HumanStatus.PENDING) {
            score += 12;
        }
        if (Boolean.TRUE.equals(finding.getIsCrossHit())) {
            score += 8;
        }
        return score;
    }

    private int severityScore(Severity severity) {
        if (severity == Severity.BLOCKER) {
            return 100;
        }
        if (severity == Severity.MAJOR) {
            return 70;
        }
        if (severity == Severity.MINOR) {
            return 30;
        }
        return 10;
    }

    private Integer ciPriorityScore(String severity) {
        if ("CRITICAL".equals(severity)) {
            return 95;
        }
        if ("WARNING".equals(severity)) {
            return 65;
        }
        return 25;
    }
}
