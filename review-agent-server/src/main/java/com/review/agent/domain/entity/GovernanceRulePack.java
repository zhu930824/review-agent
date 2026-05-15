package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 治理规则包实体
 */
@Data
@TableName("governance_rule_pack")
public class GovernanceRulePack {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定规则包标识 */
    private String rulePackKey;

    /** 规则包名称 */
    private String name;

    /** 预期业务成果 */
    private String businessOutcome;

    /** JSON 数组 —— 控制项列表 */
    private String controls;

    /** JSON 数组 —— 所需能力 key 列表 */
    private String capabilityKeys;

    /** JSON 数组 —— 建议策略 key 列表 */
    private String strategyKeys;

    /** JSON 数组 —— 人工检查点列表 */
    private String humanCheckpoints;

    /** 启用标志 */
    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
