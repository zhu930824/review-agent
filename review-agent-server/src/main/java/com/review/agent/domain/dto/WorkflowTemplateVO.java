package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务工作流模板视图对象
 */
@Data
public class WorkflowTemplateVO {

    private Long id;
    private String templateKey;
    private String name;
    private String scenario;
    private String strategyId;
    private String successMetric;
    private Boolean enabled;

    /** 规则包 id 列表，由 Service 层从 JSON 字符串解析 */
    private List<String> rulePackIds;

    /** 集成连接器 id 列表，由 Service 层从 JSON 字符串解析 */
    private List<String> integrationIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
