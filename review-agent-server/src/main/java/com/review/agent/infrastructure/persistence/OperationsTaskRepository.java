package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationsTaskVO;

import java.util.List;
import java.util.Map;

public interface OperationsTaskRepository {

    Map<String, OperationsTaskVO> listByTaskKeys(List<String> taskKeys);

    List<OperationsTaskVO> listRecent(int limit);

    void upsertTasks(List<OperationsTaskVO> tasks);

    void updateTask(String taskKey, String status, String ownerRole, Long slaHours);

    void updateTasks(List<String> taskKeys, String status, String ownerRole, Long slaHours);

    void closeTask(String taskKey, String closeReason);
}
