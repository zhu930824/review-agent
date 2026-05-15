package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 治理能力目录实体
 */
@Data
@TableName("governance_capability")
public class GovernanceCapability {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定能力标识 */
    private String capabilityKey;

    /** 能力名称 */
    private String name;

    /** 分类：AI_REVIEW / QUALITY / SECURITY / INTEGRATION / KNOWLEDGE / ANALYTICS */
    private String category;

    /** 状态：ENABLED / PARTIAL / PLANNED */
    private String status;

    /** 业务影响：HIGH / MEDIUM / LOW */
    private String businessImpact;

    /** 就绪度评分 0-100 */
    private Integer readiness;

    /** 观察到的市场期望 */
    private String marketSignal;

    /** 建议的平台行动 */
    private String platformMove;

    /** JSON 数组 —— 对标产品列表 */
    private String sourceProducts;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
