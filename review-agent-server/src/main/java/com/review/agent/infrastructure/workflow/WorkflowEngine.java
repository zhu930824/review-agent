package com.review.agent.infrastructure.workflow;

import java.util.Map;

public interface WorkflowEngine {

    String startWorkflow(String workflowKey, Map<String, Object> inputs);

    WorkflowStatus getStatus(String workflowId);

    void completeTask(String workflowId, String taskId, Map<String, Object> outputs);
}
