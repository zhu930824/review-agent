package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.ProjectGitLabReviewPolicyVO;
import com.review.agent.domain.dto.UpdateProjectGitLabReviewPolicyRequest;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.service.ProjectGitLabReviewPolicyService;
import com.review.agent.service.ProjectAccessService;
import com.review.agent.infrastructure.auth.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectGitLabReviewPolicyServiceImpl implements ProjectGitLabReviewPolicyService {

    private final ProjectGitLabConfigMapper mapper;
    private final ProjectAccessService projectAccessService;

    @Override
    public ProjectGitLabReviewPolicyVO get(Long projectId) {
        projectAccessService.require(projectId, ProjectPermission.VIEW);
        ProjectGitLabConfig config = find(projectId);
        ProjectGitLabReviewPolicyVO vo = new ProjectGitLabReviewPolicyVO();
        vo.setProjectId(projectId);
        vo.setConfigured(config != null && Boolean.TRUE.equals(config.getEnabled()));
        vo.setAutoReviewEnabled(config == null || config.getAutoReviewEnabled() == null
                ? Boolean.TRUE : config.getAutoReviewEnabled());
        vo.setReviewDrafts(config != null && Boolean.TRUE.equals(config.getReviewDrafts()));
        vo.setPublishSummaryEnabled(config != null && Boolean.TRUE.equals(config.getPublishSummaryEnabled()));
        vo.setTargetBranchPattern(config == null ? null : config.getTargetBranchPattern());
        return vo;
    }

    @Override
    public ProjectGitLabReviewPolicyVO update(Long projectId, UpdateProjectGitLabReviewPolicyRequest request) {
        projectAccessService.require(projectId, ProjectPermission.INTEGRATION_MANAGE);
        ProjectGitLabConfig config = find(projectId);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            throw new IllegalStateException("Project does not have an enabled GitLab API configuration");
        }
        config.setAutoReviewEnabled(request.getAutoReviewEnabled());
        config.setReviewDrafts(request.getReviewDrafts());
        config.setPublishSummaryEnabled(request.getPublishSummaryEnabled());
        config.setTargetBranchPattern(trimToNull(request.getTargetBranchPattern()));
        config.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(config);
        return get(projectId);
    }

    private ProjectGitLabConfig find(Long projectId) {
        return mapper.selectOne(new LambdaQueryWrapper<ProjectGitLabConfig>()
                .eq(ProjectGitLabConfig::getProjectId, projectId));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
