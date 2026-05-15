package com.review.agent.domain.dto;

import com.review.agent.domain.enums.Severity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审查策略视图对象
 */
@Data
public class ReviewStrategyVO {

    private Long id;
    private String strategyKey;
    private String name;
    private String reviewMode;
    private String description;

    /** 适用场景，从 JSON 数组字符串解析而来 */
    private List<String> recommendedFor;

    /** 质量闸门策略 */
    private GatePolicy gatePolicy;

    /** 策略中的角色-模型绑定列表 */
    private List<StrategyRoleBindingVO> roleBindings;

    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 质量闸门策略 —— 定义不同严重级别的处理方式
     */
    @Data
    public static class GatePolicy {

        /** 需要阻断的严重级别列表 */
        private List<Severity> blockOn;

        /** 需要人工复核的严重级别列表 */
        private List<Severity> requireHumanReviewOn;

        /** 建议关注的严重级别列表 */
        private List<Severity> advisoryOn;
    }
}
