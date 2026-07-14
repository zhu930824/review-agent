package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.review.agent.common.exception.BizException;
import com.review.agent.domain.dto.CreateProjectRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.ProjectVO;
import com.review.agent.domain.dto.UpdateProjectRequest;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.domain.enums.ProjectStatus;
import com.review.agent.domain.exception.CommonExceptionEnum;
import com.review.agent.infrastructure.git.GitLabRepoUrlParser;
import com.review.agent.infrastructure.git.GitLabRepoUrlParser.GitLabRepoInfo;
import com.review.agent.infrastructure.git.GitLabDiffService;
import com.review.agent.infrastructure.git.GitService;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.service.ProjectService;
import com.review.agent.service.ProjectAccessService;
import com.review.agent.infrastructure.auth.ProjectPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final GitService gitService;
    private final ProjectGitLabConfigMapper gitLabConfigMapper;
    private final GitLabDiffService gitLabDiffService;
    private final ProjectAccessService projectAccessService;

    @Value("${review-agent.repo-base-path:./repos}")
    private String repoBasePath;

    @Override
    public ProjectVO createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setRepoUrl(request.getRepoUrl());
        project.setDefaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main");
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.PENDING);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        projectMapper.insert(project);
        projectAccessService.addCurrentUserAsOwner(project.getId());

        // 如果提供了 GitLab token，配置 API 模式
        if (request.getGitlabToken() != null && !request.getGitlabToken().isBlank()) {
            setupGitLabConfig(project, request.getGitlabToken());
            project.setStatus(ProjectStatus.READY);
            projectMapper.updateById(project);
            log.info("项目 {} 已配置 GitLab API 模式，跳过本地克隆", project.getId());
        } else {
            // 传统模式：本地克隆
            String localPath = repoBasePath + "/" + project.getId();
            project.setLocalPath(localPath);
            projectMapper.updateById(project);
            gitService.cloneRepository(project.getId());
        }

        return toVO(project);
    }

    /**
     * 解析仓库 URL 并保存 GitLab 集成配置。
     */
    private void setupGitLabConfig(Project project, String token) {
        GitLabRepoInfo repoInfo = GitLabRepoUrlParser.parse(project.getRepoUrl());
        if (repoInfo == null) {
            log.warn("无法解析 GitLab 仓库 URL: {}, 回退到克隆模式", project.getRepoUrl());
            project.setLocalPath(repoBasePath + "/" + project.getId());
            gitService.cloneRepository(project.getId());
            return;
        }

        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(project.getId());
        config.setGitlabHost(repoInfo.getHost());
        config.setGitlabToken(token);
        config.setProjectPath(repoInfo.getProjectPath());
        config.setEnabled(true);
        config.setAutoReviewEnabled(true);
        config.setReviewDrafts(false);
        config.setPublishSummaryEnabled(false);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        gitLabConfigMapper.insert(config);
    }

    @Override
    public PageResult<ProjectVO> listProjects(PageRequest pageRequest) {
        Page<Project> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (!projectAccessService.hasGlobalProjectAccess()) {
            List<Long> accessibleProjectIds = projectAccessService.listAccessibleProjectIds();
            if (accessibleProjectIds.isEmpty()) {
                return new PageResult<>(0L, pageRequest.getPageNum(), pageRequest.getPageSize(), List.of());
            }
            wrapper.in(Project::getId, accessibleProjectIds);
        }
        wrapper.orderByDesc(Project::getCreatedAt);

        Page<Project> result = projectMapper.selectPage(page, wrapper);
        List<ProjectVO> voList = result.getRecords().stream().map(this::toVO).toList();

        return new PageResult<>(result.getTotal(), pageRequest.getPageNum(),
                pageRequest.getPageSize(), voList);
    }

    @Override
    public ProjectVO getProject(Long id) {
        projectAccessService.require(id, ProjectPermission.VIEW);
        return toVO(requireProject(id));
    }

    @Override
    public ProjectVO updateProject(Long id, UpdateProjectRequest request) {
        projectAccessService.require(id, ProjectPermission.PROJECT_MANAGE);
        Project project = requireProject(id);
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getRepoUrl() != null) {
            project.setRepoUrl(request.getRepoUrl());
        }
        if (request.getDefaultBranch() != null) {
            project.setDefaultBranch(request.getDefaultBranch());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        return toVO(project);
    }

    @Override
    public void deleteProject(Long id) {
        projectAccessService.require(id, ProjectPermission.MEMBER_MANAGE);
        requireProject(id);
        projectMapper.deleteById(id);
    }

    @Override
    public void retryClone(Long id) {
        projectAccessService.require(id, ProjectPermission.PROJECT_MANAGE);
        Project project = requireProject(id);
        if (gitLabDiffService.isGitLabConfigured(id)) {
            // GitLab API 模式无需重试克隆，直接标记为就绪
            project.setStatus(ProjectStatus.READY);
            projectMapper.updateById(project);
            return;
        }
        if (project.getStatus() != ProjectStatus.ERROR) {
            throw new BizException(CommonExceptionEnum.PROJECT_RETRY_NOT_ALLOWED);
        }
        project.setStatus(ProjectStatus.PENDING);
        projectMapper.updateById(project);
        gitService.cloneRepository(id);
    }

    @Override
    public List<String> getBranches(Long id) {
        projectAccessService.require(id, ProjectPermission.VIEW);
        requireProject(id);
        if (gitLabDiffService.isGitLabConfigured(id)) {
            return gitLabDiffService.getBranches(id);
        }
        return gitService.getBranches(id);
    }

    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException(CommonExceptionEnum.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setRepoUrl(project.getRepoUrl());
        vo.setDefaultBranch(project.getDefaultBranch());
        vo.setLocalPath(project.getLocalPath());
        vo.setStatus(project.getStatus());
        vo.setDescription(project.getDescription());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setUpdatedAt(project.getUpdatedAt());
        return vo;
    }
}
