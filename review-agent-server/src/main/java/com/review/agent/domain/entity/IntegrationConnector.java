package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集成连接器路线图实体
 */
@Data
@TableName("integration_connector")
public class IntegrationConnector {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 稳定连接器标识 */
    private String connectorKey;

    /** 连接器名称 */
    private String name;

    /** 上线阶段：live / next / later */
    private String rolloutStage;

    /** 状态：ENABLED / PARTIAL / PLANNED */
    private String status;

    /** 业务价值描述 */
    private String businessValue;

    /** 实施提示 */
    private String implementationHint;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
