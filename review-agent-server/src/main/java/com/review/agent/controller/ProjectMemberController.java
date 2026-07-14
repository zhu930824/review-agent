package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.ProjectMemberVO;
import com.review.agent.domain.dto.UpsertProjectMemberRequest;
import com.review.agent.service.ProjectAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectAccessService projectAccessService;

    @GetMapping
    public Result<List<ProjectMemberVO>> list(@PathVariable Long projectId) {
        return Result.success(projectAccessService.listMembers(projectId));
    }

    @PutMapping
    public Result<ProjectMemberVO> upsert(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertProjectMemberRequest request) {
        return Result.success(projectAccessService.upsertMember(projectId, request));
    }

    @DeleteMapping("/{userId}")
    public Result<Void> remove(@PathVariable Long projectId, @PathVariable Long userId) {
        projectAccessService.removeMember(projectId, userId);
        return Result.success();
    }
}
