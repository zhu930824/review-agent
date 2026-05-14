package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.review.agent.domain.enums.ModelRole;
import com.review.agent.domain.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单个模型的审查执行结果实体
 */
@Data
@TableName("review_model_result")
public class ReviewModelResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的审查任务 ID */
    private Long reviewId;

    /** 模型名称 */
    private String modelName;

    /** 模型角色：WORKER / JUDGE */
    private ModelRole role;

    /** 执行状态 */
    private ReviewStatus status;

    /** 模型返回的原始结果 JSON */
    private String rawResult;

    /** 错误信息 */
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
