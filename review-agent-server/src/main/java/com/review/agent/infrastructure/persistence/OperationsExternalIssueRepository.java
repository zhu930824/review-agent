package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationsExternalIssueVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OperationsExternalIssueRepository {

    Optional<OperationsIssueSyncContext> findGitLabSyncContext(String taskKey);

    Optional<OperationsExternalIssueVO> findLatestByTaskKey(String taskKey);

    java.util.Map<String, OperationsExternalIssueVO> findLatestByTaskKeys(List<String> taskKeys);

    List<OperationsExternalIssueVO> findRecentGitLabRefreshCandidates(int limit);

    OperationsExternalIssueVO record(String taskKey,
                                     String provider,
                                     String issueStatus,
                                     String externalIssueId,
                                     String externalIssueIid,
                                     String externalIssueUrl,
                                     String externalIssueState,
                                     String requestUrl,
                                     String errorMessage);

    default OperationsExternalIssueVO recordDetailed(String taskKey,
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
        return record(
                taskKey,
                provider,
                issueStatus,
                externalIssueId,
                externalIssueIid,
                externalIssueUrl,
                externalIssueState,
                requestUrl,
                errorMessage);
    }
}
