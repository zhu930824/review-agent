package com.review.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.review.agent.domain.enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目实体 —— 对应 Git 仓库的本地管理信息
 */
@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String name;

    /** Git 仓库地址 */
    private String repoUrl;

    /** 默认分支，默认 main */
    private String defaultBranch;

    /** 本地克隆路径 */
    private String localPath;

    /** 克隆/就绪状态 */
    private ProjectStatus status;

    /** 描述 */
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
