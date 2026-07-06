package com.review.agent.service;

import com.review.agent.domain.dto.OperationsTaskVO;
import com.review.agent.domain.dto.OperationsExternalIssueVO;

import java.util.List;

public interface OperationsTaskService {

    List<OperationsTaskVO> listTasks(int limit);

    List<OperationsTaskVO> listSlaAlerts(int limit);

    List<OperationsTaskVO> syncTasks(int limit);

    void updateTask(String taskKey, String status, String ownerRole, Long slaHours);

    void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours);

    OperationsExternalIssueVO syncGitLabIssue(String taskKey);

    OperationsExternalIssueVO refreshGitLabIssue(String taskKey);

    int refreshRecentGitLabIssues(int limit);

    void closeTask(String taskKey, String closeReason);
}
