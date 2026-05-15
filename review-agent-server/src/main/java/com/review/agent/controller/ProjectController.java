package com.review.agent.controller;

import com.review.agent.common.result.Result;
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

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public Result<ProjectVO> createProject(@Validated @RequestBody CreateProjectRequest request) {
        return Result.success(projectService.createProject(request));
    }

    @GetMapping
    public Result<PageResult<ProjectVO>> listProjects(PageRequest pageRequest) {
        return Result.success(projectService.listProjects(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getProject(@PathVariable("id") Long id) {
        return Result.success(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(@PathVariable("id") Long id,
                                           @RequestBody UpdateProjectRequest request) {
        return Result.success(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @PostMapping("/{id}/retry-clone")
    public Result<Void> retryClone(@PathVariable("id") Long id) {
        projectService.retryClone(id);
        return Result.success();
    }

    @GetMapping("/{id}/branches")
    public Result<List<String>> getBranches(@PathVariable("id") Long id) {
        return Result.success(projectService.getBranches(id));
    }
}
