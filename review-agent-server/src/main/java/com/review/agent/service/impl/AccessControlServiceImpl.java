package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.AccessProfileVO;
import com.review.agent.domain.dto.AccessAuditLogVO;
import com.review.agent.domain.dto.UpdateUserRoleRequest;
import com.review.agent.domain.dto.UserAccessVO;
import com.review.agent.domain.entity.UserAccount;
import com.review.agent.infrastructure.auth.AccessDecisionService;
import com.review.agent.infrastructure.auth.PlatformRole;
import com.review.agent.infrastructure.persistence.UserAccountMapper;
import com.review.agent.service.AccessControlService;
import com.review.agent.service.AccessAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessControlServiceImpl implements AccessControlService {

    private final AccessDecisionService accessDecisionService;
    private final UserAccountMapper userAccountMapper;
    private final AccessAuditLogService accessAuditLogService;

    @Override
    public AccessProfileVO currentProfile() {
        AccessDecisionService.AccessIdentity identity = requireIdentity();
        AccessProfileVO profile = new AccessProfileVO();
        profile.setUserId(identity.userId());
        profile.setUsername(identity.username());
        profile.setDisplayName(identity.displayName());
        profile.setRole(identity.storedRole());
        profile.setNormalizedRole(identity.role().name());
        profile.setPermissions(permissionNames(identity.role()));
        return profile;
    }

    @Override
    public List<UserAccessVO> listUsers() {
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                        .orderByAsc(UserAccount::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<AccessAuditLogVO> listAuditLogs(int limit, String actionType, Long projectId) {
        return accessAuditLogService.listRecent(limit, actionType, projectId);
    }

    @Override
    @Transactional
    public UserAccessVO updateRole(Long userId, UpdateUserRoleRequest request) {
        PlatformRole role = parseRole(request.getRole());
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User account not found: " + userId);
        }

        PlatformRole currentRole = PlatformRole.fromStoredRole(user.getRole());
        if (currentRole == PlatformRole.ADMIN && role != PlatformRole.ADMIN && countAdmins() <= 1) {
            throw new IllegalStateException("The platform must keep at least one administrator");
        }

        if (currentRole == role) {
            return toVO(user);
        }

        AccessDecisionService.AccessIdentity actor = requireIdentity();
        UserAccount update = new UserAccount();
        update.setId(userId);
        update.setRole(role.name());
        userAccountMapper.updateById(update);
        accessAuditLogService.record(
                "PLATFORM_ROLE_CHANGED",
                actor,
                user.getId(),
                user.getUsername(),
                null,
                currentRole.name(),
                role.name(),
                "Platform role changed");
        user.setRole(role.name());
        return toVO(user);
    }

    private long countAdmins() {
        return userAccountMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                        .select(UserAccount::getRole))
                .stream()
                .filter(user -> PlatformRole.fromStoredRole(user.getRole()) == PlatformRole.ADMIN)
                .count();
    }

    private AccessDecisionService.AccessIdentity requireIdentity() {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (identity == null || !identity.isActive()) {
            throw new IllegalStateException("Authenticated user is unavailable");
        }
        return identity;
    }

    private PlatformRole parseRole(String role) {
        try {
            return PlatformRole.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Role must be ADMIN, GOVERNANCE_MANAGER, OPERATOR or REVIEWER", ex);
        }
    }

    private UserAccessVO toVO(UserAccount user) {
        PlatformRole role = PlatformRole.fromStoredRole(user.getRole());
        UserAccessVO vo = new UserAccessVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setNormalizedRole(role.name());
        vo.setStatus(user.getStatus());
        vo.setPermissions(permissionNames(role));
        return vo;
    }

    private List<String> permissionNames(PlatformRole role) {
        return role.permissions().stream().map(Enum::name).sorted().toList();
    }
}
