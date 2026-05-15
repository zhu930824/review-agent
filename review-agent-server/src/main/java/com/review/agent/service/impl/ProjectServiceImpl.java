package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.review.agent.domain.dto.CreateProjectRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.ProjectVO;
import com.review.agent.domain.dto.UpdateProjectRequest;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.enums.ProjectStatus;
import com.review.agent.domain.exception.BusinessException;
import com.review.agent.infrastructure.git.GitService;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final GitService gitService;

    @Value("${review-agent.repo-base-path:./repos}")
    private String repoBasePath;

    @Override
    public ProjectVO createProject(CreateProjectRequest request) {
        Project project = ProjectConverter.toEntity(request);
        projectMapper.insert(project);

        String localPath = repoBasePath + "/" + project.getId();
        project.setLocalPath(localPath);
        projectMapper.updateById(project);

        gitService.cloneRepository(project.getId());

        return ProjectConverter.toVO(project);
    }

    @Override
    public PageResult<ProjectVO> listProjects(PageRequest pageRequest) {
        Page<Project> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getCreatedAt);

        Page<Project> result = projectMapper.selectPage(page, wrapper);
        List<ProjectVO> voList = result.getRecords().stream().map(ProjectConverter::toVO).toList();

        return new PageResult<>(result.getTotal(), pageRequest.getPageNum(),
                pageRequest.getPageSize(), voList);
    }

    @Override
    public ProjectVO getProject(Long id) {
        return ProjectConverter.toVO(requireProject(id));
    }

    @Override
    public ProjectVO updateProject(Long id, UpdateProjectRequest request) {
        Project project = requireProject(id);
        ProjectConverter.updateEntity(project, request);
        projectMapper.updateById(project);
        return ProjectConverter.toVO(project);
    }

    @Override
    public void deleteProject(Long id) {
        requireProject(id);
        projectMapper.deleteById(id);
    }

    /** 按ID查询项目，不存在则抛出业务异常 */
    private Project requireProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("404", "项目不存在: " + id);
        }
        return project;
    }
}
