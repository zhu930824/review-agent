package com.review.agent.service.impl;

import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.enums.FindingCategory;
import com.review.agent.domain.enums.HumanStatus;
import com.review.agent.domain.enums.Severity;
import com.review.agent.service.OperationsCiHealthActionService;
import com.review.agent.service.OperationsRemediationQueueService;
import com.review.agent.service.OperationsTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OperationsTaskServiceImpl implements OperationsTaskService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final OperationsRemediationQueueService remediationQueueService;
    private final OperationsCiHealthActionService ciHealthActionService;

    @Override
    public List<OperationsTaskVO> listTasks(int limit) {
        int safeLimit = normalizeLimit(limit);
        Stream<OperationsTaskVO> findingTasks = remediationQueueService.listQueue(safeLimit).stream()
                .map(this::fromFinding);
        Stream<OperationsTaskVO> ciTasks = ciHealthActionService.listActions().stream()
                .map(this::fromCiHealthAction);

        return Stream.concat(findingTasks, ciTasks)
                .sorted(Comparator
                        .comparing(OperationsTaskVO::getPriorityScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OperationsTaskVO::getSlaHours, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(OperationsTaskVO::getTaskKey))
                .limit(safeLimit)
                .toList();
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
