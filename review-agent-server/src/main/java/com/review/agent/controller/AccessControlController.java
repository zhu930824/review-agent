package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.AccessProfileVO;
import com.review.agent.domain.dto.AccessAuditLogVO;
import com.review.agent.domain.dto.UpdateUserRoleRequest;
import com.review.agent.domain.dto.UserAccessVO;
import com.review.agent.service.AccessControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccessControlController {

    private final AccessControlService accessControlService;

    @GetMapping("/auth/access-profile")
    public Result<AccessProfileVO> currentProfile() {
        return Result.success(accessControlService.currentProfile());
    }

    @GetMapping("/access/users")
    public Result<List<UserAccessVO>> listUsers() {
        return Result.success(accessControlService.listUsers());
    }

    @GetMapping("/access/audit-logs")
    public Result<List<AccessAuditLogVO>> listAuditLogs(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Long projectId) {
        return Result.success(accessControlService.listAuditLogs(limit, actionType, projectId));
    }

    @PatchMapping("/access/users/{userId}/role")
    public Result<UserAccessVO> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return Result.success(accessControlService.updateRole(userId, request));
    }
}
