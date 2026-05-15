package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务工作流模板实体
 */
@Data
@TableName("workflow_template")
public class WorkflowTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定模板标识 */
    private String templateKey;

    /** 模板名称 */
    private String name;

    /** 适用场景 */
    private String scenario;

    /** 审查策略 key */
    private String strategyKey;

    /** JSON 数组 —— 规则包 key 列表 */
    private String rulePackKeys;

    /** JSON 数组 —— 连接器 key 列表 */
    private String connectorKeys;

    /** 预期成功指标 */
    private String successMetric;

    /** 启用标志 */
    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
