package com.review.agent.infrastructure.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SimpleWorkflowEngine implements WorkflowEngine {

    private final Map<String, WorkflowStatus> instances = new ConcurrentHashMap<>();

    @Override
    public String startWorkflow(String workflowKey, Map<String, Object> inputs) {
        String workflowId = UUID.randomUUID().toString().substring(0, 8);
        WorkflowStatus status = WorkflowStatus.builder()
                .workflowId(workflowId)
                .workflowKey(workflowKey)
                .status("RUNNING")
                .startedAt(java.time.LocalDateTime.now())
                .tasks(java.util.List.of())
                .build();
        instances.put(workflowId, status);
        log.info("启动工作流: key={}, id={}", workflowKey, workflowId);
        return workflowId;
    }

    @Override
    public WorkflowStatus getStatus(String workflowId) {
        return instances.get(workflowId);
    }

    @Override
    public void completeTask(String workflowId, String taskId, Map<String, Object> outputs) {
        log.info("完成任务: workflow={}, task={}", workflowId, taskId);
    }
}
