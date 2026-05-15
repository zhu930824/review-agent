package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 治理规则包视图对象
 */
@Data
public class GovernanceRulePackVO {

    private Long id;
    private String rulePackKey;
    private String name;
    private String businessOutcome;
    private Boolean enabled;

    /** 控制项列表，由 Service 层从 JSON 字符串解析 */
    private List<String> controls;

    /** 所需能力 id 列表，由 Service 层从 JSON 字符串解析 */
    private List<String> capabilityIds;

    /** 建议策略 id 列表，由 Service 层从 JSON 字符串解析 */
    private List<String> suggestedStrategies;

    /** 人工检查点列表，由 Service 层从 JSON 字符串解析 */
    private List<String> humanCheckpoints;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
