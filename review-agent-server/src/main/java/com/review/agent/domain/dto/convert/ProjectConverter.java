package com.review.agent.domain.dto.convert;

import com.review.agent.domain.dto.CreateProjectRequest;
import com.review.agent.domain.dto.ProjectVO;
import com.review.agent.domain.dto.UpdateProjectRequest;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.enums.ProjectStatus;

import java.time.LocalDateTime;

/** 项目相关对象转换 */
public final class ProjectConverter {

    private ProjectConverter() {}

    public static Project toEntity(CreateProjectRequest req) {
        Project entity = new Project();
        entity.setName(req.getName());
        entity.setRepoUrl(req.getRepoUrl());
        entity.setDefaultBranch(req.getDefaultBranch());
        entity.setDescription(req.getDescription());
        entity.setStatus(ProjectStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /** 按更新请求中的非空字段覆盖实体属性 */
    public static void updateEntity(Project entity, UpdateProjectRequest req) {
        if (req.getName() != null) {
            entity.setName(req.getName());
        }
        if (req.getRepoUrl() != null) {
            entity.setRepoUrl(req.getRepoUrl());
        }
        if (req.getDefaultBranch() != null) {
            entity.setDefaultBranch(req.getDefaultBranch());
        }
        if (req.getDescription() != null) {
            entity.setDescription(req.getDescription());
        }
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public static ProjectVO toVO(Project entity) {
        ProjectVO vo = new ProjectVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setRepoUrl(entity.getRepoUrl());
        vo.setDefaultBranch(entity.getDefaultBranch());
        vo.setLocalPath(entity.getLocalPath());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCloneErrorMessage(entity.getCloneErrorMessage());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
