package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsExternalIssueVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.entity.ProjectGitLabConfig;
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
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationsTaskServiceImplTest {

    private final FakeRemediationQueueService remediationQueueService = new FakeRemediationQueueService();
    private final FakeCiHealthActionService ciHealthActionService = new FakeCiHealthActionService();
    private final FakeOperationsTaskRepository taskRepository = new FakeOperationsTaskRepository();
    private final FakeOperationsExternalIssueRepository externalIssueRepository = new FakeOperationsExternalIssueRepository();
    private final FakeGitLabApiClient gitLabApiClient = new FakeGitLabApiClient();
    private final OperationsTaskServiceImpl service = new OperationsTaskServiceImpl(
            remediationQueueService,
            ciHealthActionService,
            taskRepository,
            externalIssueRepository,
            gitLabApiClient);

    @Test
    void mergesFindingAndCiHealthActionsIntoUnifiedTasks() {
        remediationQueueService.findings = List.of(
                finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true),
                finding(8L, 4L, "Minor cleanup", Severity.MINOR, FindingCategory.CODE_STYLE, HumanStatus.PENDING, false));
        ciHealthActionService.actions = List.of(ciAction("jenkins-pipeline", "JENKINS", "CRITICAL"));

        List<OperationsTaskVO> tasks = service.listTasks(10);

        assertEquals(3, tasks.size());
        assertEquals("FINDING-7", tasks.get(0).getTaskKey());
        assertEquals("FINDING", tasks.get(0).getSourceType());
        assertEquals("Security Owner", tasks.get(0).getOwnerRole());
        assertEquals(4L, tasks.get(0).getSlaHours());
        assertEquals("CI_HEALTH", tasks.get(1).getSourceType());
        assertEquals("CI Owner", tasks.get(1).getOwnerRole());
        assertEquals("CI-jenkins-pipeline-UNHEALTHY", tasks.get(1).getTaskKey());
    }

    @Test
    void normalizesLimitForUnifiedTasks() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));

        List<OperationsTaskVO> tasks = service.listTasks(0);

        assertEquals(50, remediationQueueService.lastLimit);
        assertEquals(1, tasks.size());
    }

    @Test
    void overlaysPersistedStatusAndCloseReason() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));
        OperationsTaskVO persisted = new OperationsTaskVO();
        persisted.setTaskKey("FINDING-7");
        persisted.setStatus("RESOLVED");
        persisted.setOwnerRole("Security Owner");
        persisted.setSlaHours(4L);
        persisted.setCloseReason("Fixed in release branch.");
        taskRepository.persisted = Map.of("FINDING-7", persisted);

        List<OperationsTaskVO> tasks = service.listTasks(10);

        assertEquals("RESOLVED", tasks.get(0).getStatus());
        assertEquals("Fixed in release branch.", tasks.get(0).getCloseReason());
    }

    @Test
    void marksPersistedTasksAsSlaAlerts() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));
        OperationsTaskVO persisted = new OperationsTaskVO();
        persisted.setTaskKey("FINDING-7");
        persisted.setStatus("OPEN");
        persisted.setOwnerRole("Security Owner");
        persisted.setSlaHours(4L);
        persisted.setCreatedAt(LocalDateTime.now().minusHours(8));
        taskRepository.persisted = Map.of("FINDING-7", persisted);

        List<OperationsTaskVO> tasks = service.listTasks(10);
        List<OperationsTaskVO> alerts = service.listSlaAlerts(10);

        assertEquals("OVERDUE", tasks.get(0).getSlaState());
        assertEquals("FINDING-7", alerts.get(0).getTaskKey());
        assertEquals("OVERDUE", alerts.get(0).getSlaState());
    }

    @Test
    void syncPersistsDerivedTasksAndCloseUpdatesRepository() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));

        service.syncTasks(10);
        service.updateTask("FINDING-7", "in_progress", "Platform Owner", 8L);
        service.updateTasks(List.of("FINDING-7", "FINDING-7"), "open", "Security Desk", 12L);
        service.closeTask("FINDING-7", "Done");

        assertEquals("FINDING-7", taskRepository.upserted.get(0).getTaskKey());
        assertEquals("FINDING-7", taskRepository.updatedTaskKey);
        assertEquals("IN_PROGRESS", taskRepository.updatedStatus);
        assertEquals("Platform Owner", taskRepository.updatedOwnerRole);
        assertEquals(8L, taskRepository.updatedSlaHours);
        assertEquals(List.of("FINDING-7"), taskRepository.batchUpdatedTaskKeys);
        assertEquals("OPEN", taskRepository.batchUpdatedStatus);
        assertEquals("Security Desk", taskRepository.batchUpdatedOwnerRole);
        assertEquals(12L, taskRepository.batchUpdatedSlaHours);
        assertEquals("FINDING-7", taskRepository.closedTaskKey);
        assertEquals("Done", taskRepository.closeReason);
    }

    @Test
    void syncGitLabIssueRecordsSkippedAndSuccessStates() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));

        OperationsExternalIssueVO skipped = service.syncGitLabIssue("FINDING-7");

        assertEquals("SKIPPED", skipped.getIssueStatus());
        assertEquals("No enabled GitLab API configuration found for task project.", skipped.getErrorMessage());

        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(12L);
        config.setGitlabHost("https://gitlab.example.com");
        config.setGitlabToken("token");
        config.setProjectPath("team%2Freview-agent");
        config.setEnabled(true);
        externalIssueRepository.context = new OperationsIssueSyncContext(
                "FINDING-7",
                "FINDING",
                "7",
                "Review #3",
                "Payment service blocker",
                "BLOCKER",
                "Security Owner",
                "review-agent",
                "Confirm, dismiss, or open the review detail to drive this finding to closure.",
                3L,
                12L,
                "review-agent",
                config);

        OperationsExternalIssueVO synced = service.syncGitLabIssue("FINDING-7");

        assertEquals("SYNCED", synced.getIssueStatus());
        assertEquals("https://gitlab.example.com/team/review-agent/-/issues/9", synced.getExternalIssueUrl());
        assertEquals("[Review Agent] Payment service blocker", gitLabApiClient.title);
    }

    @Test
    void refreshGitLabIssueRecordsStateAndClosesTaskWhenIssueClosed() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));
        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(12L);
        config.setGitlabHost("https://gitlab.example.com");
        config.setGitlabToken("token");
        config.setProjectPath("team%2Freview-agent");
        config.setEnabled(true);
        externalIssueRepository.context = new OperationsIssueSyncContext(
                "FINDING-7",
                "FINDING",
                "7",
                "Review #3",
                "Payment service blocker",
                "BLOCKER",
                "Security Owner",
                "review-agent",
                "Confirm, dismiss, or open the review detail to drive this finding to closure.",
                3L,
                12L,
                "review-agent",
                config);
        OperationsExternalIssueVO latest = new OperationsExternalIssueVO();
        latest.setTaskKey("FINDING-7");
        latest.setProvider("GITLAB");
        latest.setIssueStatus("SYNCED");
        latest.setExternalIssueId("99");
        latest.setExternalIssueIid("9");
        latest.setExternalIssueUrl("https://gitlab.example.com/team/review-agent/-/issues/9");
        externalIssueRepository.latest = Map.of("FINDING-7", latest);
        gitLabApiClient.issueState = "closed";

        OperationsExternalIssueVO refreshed = service.refreshGitLabIssue("FINDING-7");

        assertEquals("SYNCED", refreshed.getIssueStatus());
        assertEquals("closed", refreshed.getExternalIssueState());
        assertEquals("Payment service blocker", refreshed.getExternalIssueTitle());
        assertEquals("review-agent,blocker", refreshed.getExternalIssueLabels());
        assertEquals("security-owner", refreshed.getExternalIssueAssignee());
        assertEquals("review-bot", refreshed.getExternalIssueAuthor());
        assertEquals("FINDING-7", taskRepository.closedTaskKey);
        assertEquals("Closed from GitLab issue #9.", taskRepository.closeReason);
    }

    @Test
    void refreshRecentGitLabIssuesRefreshesRepositoryCandidates() {
        remediationQueueService.findings = List.of(finding(7L, 3L, "Payment service blocker", Severity.BLOCKER, FindingCategory.SECURITY, HumanStatus.PENDING, true));
        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(12L);
        config.setGitlabHost("https://gitlab.example.com");
        config.setGitlabToken("token");
        config.setProjectPath("team%2Freview-agent");
        config.setEnabled(true);
        externalIssueRepository.context = new OperationsIssueSyncContext(
                "FINDING-7",
                "FINDING",
                "7",
                "Review #3",
                "Payment service blocker",
                "BLOCKER",
                "Security Owner",
                "review-agent",
                "Confirm, dismiss, or open the review detail to drive this finding to closure.",
                3L,
                12L,
                "review-agent",
                config);
        OperationsExternalIssueVO latest = new OperationsExternalIssueVO();
        latest.setTaskKey("FINDING-7");
        latest.setProvider("GITLAB");
        latest.setIssueStatus("SYNCED");
        latest.setExternalIssueId("99");
        latest.setExternalIssueIid("9");
        latest.setExternalIssueUrl("https://gitlab.example.com/team/review-agent/-/issues/9");
        externalIssueRepository.latest = Map.of("FINDING-7", latest);
        externalIssueRepository.refreshCandidates = List.of(latest);
        gitLabApiClient.issueState = "opened";

        int refreshed = service.refreshRecentGitLabIssues(10);

        assertEquals(1, refreshed);
        assertEquals(10, externalIssueRepository.lastRefreshCandidateLimit);
        assertEquals("opened", externalIssueRepository.latest.get("FINDING-7").getExternalIssueState());
        assertEquals("security-owner", externalIssueRepository.latest.get("FINDING-7").getExternalIssueAssignee());
    }

    @Test
    void rejectsUnsupportedTaskStatusAndInvalidSla() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTask("FINDING-7", "UNKNOWN", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTask("FINDING-7", "OPEN", null, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateTasks(List.of(), "OPEN", null, null));
    }

    private OperationFindingVO finding(
            Long id,
            Long reviewId,
            String title,
            Severity severity,
            FindingCategory category,
            HumanStatus humanStatus,
            boolean crossHit) {
        OperationFindingVO vo = new OperationFindingVO();
        vo.setId(id);
        vo.setReviewId(reviewId);
        vo.setProjectName("review-agent");
        vo.setTitle(title);
        vo.setSeverity(severity);
        vo.setCategory(category);
        vo.setHumanStatus(humanStatus);
        vo.setIsCrossHit(crossHit);
        return vo;
    }

    private OperationsCiHealthActionVO ciAction(String connectorKey, String provider, String severity) {
        OperationsCiHealthActionVO vo = new OperationsCiHealthActionVO();
        vo.setKey(connectorKey + "-UNHEALTHY");
        vo.setConnectorKey(connectorKey);
        vo.setProvider(provider);
        vo.setHealthStatus("UNHEALTHY");
        vo.setSeverity(severity);
        vo.setOwnerRole("CI Owner");
        vo.setSlaHours(4L);
        vo.setLatestSignal("FAILED");
        vo.setRecommendation("Check Jenkins credentials.");
        return vo;
    }

    private static class FakeRemediationQueueService implements OperationsRemediationQueueService {
        private int lastLimit;
        private List<OperationFindingVO> findings = List.of();

        @Override
        public List<OperationFindingVO> listQueue(int limit) {
            lastLimit = limit;
            return findings;
        }

        @Override
        public List<OperationOwnerLoadVO> listOwnerLoad() {
            return List.of();
        }

        @Override
        public List<OperationRuleLearningCandidateVO> listRuleLearningCandidates(int limit) {
            return List.of();
        }

        @Override
        public OperationBusinessImpactVO estimateBusinessImpact() {
            return new OperationBusinessImpactVO();
        }

        @Override
        public void confirmFinding(Long findingId) {
        }

        @Override
        public void dismissFinding(Long findingId) {
        }

        @Override
        public void acceptRuleLearningCandidate(Long findingId) {
        }

        @Override
        public void rejectRuleLearningCandidate(Long findingId) {
        }
    }

    private static class FakeCiHealthActionService implements OperationsCiHealthActionService {
        private List<OperationsCiHealthActionVO> actions = List.of();

        @Override
        public List<OperationsCiHealthActionVO> listActions() {
            return actions;
        }
    }

    private static class FakeOperationsTaskRepository implements OperationsTaskRepository {
        private Map<String, OperationsTaskVO> persisted = Map.of();
        private List<OperationsTaskVO> upserted = List.of();
        private String updatedTaskKey;
        private String updatedStatus;
        private String updatedOwnerRole;
        private Long updatedSlaHours;
        private List<String> batchUpdatedTaskKeys = List.of();
        private String batchUpdatedStatus;
        private String batchUpdatedOwnerRole;
        private Long batchUpdatedSlaHours;
        private String closedTaskKey;
        private String closeReason;

        @Override
        public Map<String, OperationsTaskVO> listByTaskKeys(List<String> taskKeys) {
            return taskKeys.stream()
                    .filter(persisted::containsKey)
                    .map(persisted::get)
                    .collect(Collectors.toMap(OperationsTaskVO::getTaskKey, Function.identity()));
        }

        @Override
        public List<OperationsTaskVO> listRecent(int limit) {
            return List.copyOf(persisted.values());
        }

        @Override
        public void upsertTasks(List<OperationsTaskVO> tasks) {
            upserted = tasks;
        }

        @Override
        public void updateTask(String taskKey, String status, String ownerRole, Long slaHours) {
            updatedTaskKey = taskKey;
            updatedStatus = status;
            updatedOwnerRole = ownerRole;
            updatedSlaHours = slaHours;
        }

        @Override
        public void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours) {
            batchUpdatedTaskKeys = taskKeys;
            batchUpdatedStatus = status;
            batchUpdatedOwnerRole = ownerRole;
            batchUpdatedSlaHours = slaHours;
        }

        @Override
        public void closeTask(String taskKey, String closeReason) {
            this.closedTaskKey = taskKey;
            this.closeReason = closeReason;
        }
    }

    private static class FakeOperationsExternalIssueRepository implements OperationsExternalIssueRepository {
        private OperationsIssueSyncContext context;
        private Map<String, OperationsExternalIssueVO> latest = Map.of();
        private List<OperationsExternalIssueVO> refreshCandidates = List.of();
        private int lastRefreshCandidateLimit;

        @Override
        public Optional<OperationsIssueSyncContext> findGitLabSyncContext(String taskKey) {
            if (context != null) {
                return Optional.of(context);
            }
            return Optional.of(new OperationsIssueSyncContext(
                    taskKey,
                    "FINDING",
                    "7",
                    "Review #3",
                    "Payment service blocker",
                    "BLOCKER",
                    "Security Owner",
                    "review-agent",
                    "Confirm, dismiss, or open the review detail to drive this finding to closure.",
                    3L,
                    12L,
                    "review-agent",
                    null));
        }

        @Override
        public Optional<OperationsExternalIssueVO> findLatestByTaskKey(String taskKey) {
            return Optional.ofNullable(latest.get(taskKey));
        }

        @Override
        public Map<String, OperationsExternalIssueVO> findLatestByTaskKeys(List<String> taskKeys) {
            return latest;
        }

        @Override
        public List<OperationsExternalIssueVO> findRecentGitLabRefreshCandidates(int limit) {
            lastRefreshCandidateLimit = limit;
            return refreshCandidates;
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
                LocalDateTime externalUpdatedAt,
                LocalDateTime externalClosedAt,
                String requestUrl,
                String errorMessage) {
            OperationsExternalIssueVO vo = new OperationsExternalIssueVO();
            vo.setTaskKey(taskKey);
            vo.setProvider(provider);
            vo.setIssueStatus(issueStatus);
            vo.setExternalIssueId(externalIssueId);
            vo.setExternalIssueIid(externalIssueIid);
            vo.setExternalIssueUrl(externalIssueUrl);
            vo.setExternalIssueState(externalIssueState);
            vo.setExternalIssueTitle(externalIssueTitle);
            vo.setExternalIssueLabels(externalIssueLabels);
            vo.setExternalIssueAssignee(externalIssueAssignee);
            vo.setExternalIssueAuthor(externalIssueAuthor);
            vo.setExternalUpdatedAt(externalUpdatedAt);
            vo.setExternalClosedAt(externalClosedAt);
            vo.setRequestUrl(requestUrl);
            vo.setErrorMessage(errorMessage);
            latest = Map.of(taskKey, vo);
            return vo;
        }
    }

    private static class FakeGitLabApiClient extends GitLabApiClient {
        private String title;
        private String issueState = "opened";

        @Override
        public GitLabIssueCreateResult createIssue(ProjectGitLabConfig config, String title, String description, String labels) {
            this.title = title;
            return new GitLabIssueCreateResult(
                    "99",
                    "9",
                    "https://gitlab.example.com/team/review-agent/-/issues/9",
                    "https://gitlab.example.com/api/v4/projects/team%2Freview-agent/issues");
        }

        @Override
        public GitLabIssueStateResult getIssue(ProjectGitLabConfig config, String issueIid) {
            return new GitLabIssueStateResult(
                    "99",
                    issueIid,
                    "https://gitlab.example.com/team/review-agent/-/issues/" + issueIid,
                    issueState,
                    "Payment service blocker",
                    "review-agent,blocker",
                    "security-owner",
                    "review-bot",
                    LocalDateTime.of(2026, 7, 6, 10, 30),
                    "closed".equals(issueState) ? LocalDateTime.of(2026, 7, 6, 11, 0) : null,
                    "https://gitlab.example.com/api/v4/projects/team%2Freview-agent/issues/" + issueIid);
        }
    }
}
