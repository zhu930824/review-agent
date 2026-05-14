package com.review.agent.service;

import com.review.agent.domain.dto.CreateProjectRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.ProjectVO;
import com.review.agent.domain.dto.UpdateProjectRequest;

import java.util.List;

public interface ProjectService {

    ProjectVO createProject(CreateProjectRequest request);

    PageResult<ProjectVO> listProjects(PageRequest pageRequest);

    ProjectVO getProject(Long id);

    ProjectVO updateProject(Long id, UpdateProjectRequest request);

    void deleteProject(Long id);

    void retryClone(Long id);

    List<String> getBranches(Long id);
}
