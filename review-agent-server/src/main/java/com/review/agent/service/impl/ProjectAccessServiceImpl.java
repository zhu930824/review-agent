package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.ProjectMemberVO;
import com.review.agent.domain.dto.UpsertProjectMemberRequest;
import com.review.agent.domain.entity.ProjectMember;
import com.review.agent.domain.entity.UserAccount;
import com.review.agent.infrastructure.auth.AccessDecisionService;
import com.review.agent.infrastructure.auth.PlatformRole;
import com.review.agent.infrastructure.auth.ProjectPermission;
import com.review.agent.infrastructure.auth.ProjectRole;
import com.review.agent.infrastructure.persistence.ProjectMemberMapper;
import com.review.agent.infrastructure.persistence.UserAccountMapper;
import com.review.agent.service.ProjectAccessService;
import com.review.agent.service.AccessAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAccessServiceImpl implements ProjectAccessService {

    private final AccessDecisionService accessDecisionService;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserAccountMapper userAccountMapper;
    private final AccessAuditLogService accessAuditLogService;

    @Override
    public void require(Long projectId, ProjectPermission permission) {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (identity == null) {
            return;
        }
        if (!identity.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is disabled");
        }
        if (identity.role() == PlatformRole.ADMIN) {
            return;
        }
        ProjectMember membership = find(projectId, identity.userId());
        if (membership == null || !ProjectRole.parse(membership.getRole()).allows(permission)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No " + permission + " permission for project " + projectId);
        }
    }

    @Override
    public boolean hasGlobalProjectAccess() {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        return identity != null && identity.role() == PlatformRole.ADMIN;
    }

    @Override
    public List<Long> listAccessibleProjectIds() {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (identity == null) {
            return List.of();
        }
        return projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getUserId, identity.userId()))
                .stream()
                .map(ProjectMember::getProjectId)
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void addCurrentUserAsOwner(Long projectId) {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (identity == null) {
            return;
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(identity.userId());
        member.setRole(ProjectRole.OWNER.name());
        member.setCreatedBy(identity.userId());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        projectMemberMapper.insert(member);
        accessAuditLogService.record(
                "PROJECT_OWNER_ASSIGNED", identity, identity.userId(), identity.username(), projectId,
                null, ProjectRole.OWNER.name(), "Project creator assigned as owner");
    }

    @Override
    public List<ProjectMemberVO> listMembers(Long projectId) {
        require(projectId, ProjectPermission.VIEW);
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        Long currentUserId = identity == null ? null : identity.userId();
        return projectMemberMapper.selectList(new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .orderByAsc(ProjectMember::getId))
                .stream()
                .map(member -> toVO(member, currentUserId))
                .toList();
    }

    @Override
    @Transactional
    public ProjectMemberVO upsertMember(Long projectId, UpsertProjectMemberRequest request) {
        require(projectId, ProjectPermission.MEMBER_MANAGE);
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, request.getUsername().trim()));
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Active user not found: " + request.getUsername());
        }
        ProjectRole role = parseRole(request.getRole());
        ProjectMember member = find(projectId, user.getId());
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (member == null) {
            member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(user.getId());
            member.setCreatedBy(identity == null ? null : identity.userId());
            member.setCreatedAt(LocalDateTime.now());
            member.setRole(role.name());
            member.setUpdatedAt(LocalDateTime.now());
            projectMemberMapper.insert(member);
            accessAuditLogService.record(
                    "PROJECT_MEMBER_ADDED", identity, user.getId(), user.getUsername(), projectId,
                    null, role.name(), "Project member added");
        } else {
            preventLastOwnerRemoval(projectId, member, role);
            ProjectRole previousRole = ProjectRole.parse(member.getRole());
            if (previousRole == role) {
                return toVO(member, identity == null ? null : identity.userId());
            }
            member.setRole(role.name());
            member.setUpdatedAt(LocalDateTime.now());
            projectMemberMapper.updateById(member);
            accessAuditLogService.record(
                    "PROJECT_MEMBER_ROLE_CHANGED", identity, user.getId(), user.getUsername(), projectId,
                    previousRole.name(), role.name(), "Project member role changed");
        }
        return toVO(member, identity == null ? null : identity.userId());
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        require(projectId, ProjectPermission.MEMBER_MANAGE);
        ProjectMember member = find(projectId, userId);
        if (member == null) {
            return;
        }
        if (ProjectRole.parse(member.getRole()) == ProjectRole.OWNER && countOwners(projectId) <= 1) {
            throw new IllegalStateException("The project must keep at least one owner");
        }
        UserAccount user = userAccountMapper.selectById(userId);
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        projectMemberMapper.deleteById(member.getId());
        accessAuditLogService.record(
                "PROJECT_MEMBER_REMOVED",
                identity,
                userId,
                user == null ? "deleted-user-" + userId : user.getUsername(),
                projectId,
                ProjectRole.parse(member.getRole()).name(),
                null,
                "Project member removed");
    }

    private void preventLastOwnerRemoval(Long projectId, ProjectMember member, ProjectRole nextRole) {
        if (ProjectRole.parse(member.getRole()) == ProjectRole.OWNER
                && nextRole != ProjectRole.OWNER
                && countOwners(projectId) <= 1) {
            throw new IllegalStateException("The project must keep at least one owner");
        }
    }

    private long countOwners(Long projectId) {
        Long count = projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getRole, ProjectRole.OWNER.name()));
        return count == null ? 0 : count;
    }

    private ProjectMember find(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private ProjectRole parseRole(String role) {
        try {
            return ProjectRole.parse(role);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Project role must be OWNER, MAINTAINER or REVIEWER", ex);
        }
    }

    private ProjectMemberVO toVO(ProjectMember member, Long currentUserId) {
        UserAccount user = userAccountMapper.selectById(member.getUserId());
        ProjectRole role = ProjectRole.parse(member.getRole());
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setUserId(member.getUserId());
        vo.setUsername(user == null ? "deleted-user-" + member.getUserId() : user.getUsername());
        vo.setDisplayName(user == null ? null : user.getDisplayName());
        vo.setEmail(user == null ? null : user.getEmail());
        vo.setStatus(user == null ? "DELETED" : user.getStatus());
        vo.setRole(role.name());
        vo.setCurrentUser(member.getUserId().equals(currentUserId));
        vo.setPermissions(role.permissions().stream().map(Enum::name).sorted().toList());
        vo.setUpdatedAt(member.getUpdatedAt());
        return vo;
    }
}
