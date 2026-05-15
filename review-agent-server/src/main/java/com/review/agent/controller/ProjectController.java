package com.review.agent.controller;

import com.review.agent.domain.dto.BaseResult;
import com.review.agent.domain.dto.CreateProjectRequest;
import com.review.agent.domain.dto.PageRequest;
import com.review.agent.domain.dto.PageResult;
import com.review.agent.domain.dto.ProjectVO;
import com.review.agent.domain.dto.UpdateProjectRequest;
import com.review.agent.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public BaseResult<ProjectVO> createProject(@Validated @RequestBody CreateProjectRequest request) {
        return BaseResult.success(projectService.createProject(request));
    }

    @GetMapping
    public BaseResult<PageResult<ProjectVO>> listProjects(PageRequest pageRequest) {
        return BaseResult.success(projectService.listProjects(pageRequest));
    }

    @GetMapping("/{id}")
    public BaseResult<ProjectVO> getProject(@PathVariable Long id) {
        return BaseResult.success(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    public BaseResult<ProjectVO> updateProject(@PathVariable Long id,
                                                @RequestBody UpdateProjectRequest request) {
        return BaseResult.success(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResult<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return BaseResult.success();
    }
}
