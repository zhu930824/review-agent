package com.review.agent.infrastructure.auth;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum PlatformRole {
    ADMIN(EnumSet.allOf(PlatformPermission.class)),
    GOVERNANCE_MANAGER(EnumSet.of(
            PlatformPermission.GOVERNANCE_VIEW,
            PlatformPermission.GOVERNANCE_MANAGE,
            PlatformPermission.INTEGRATION_VIEW,
            PlatformPermission.INTEGRATION_MANAGE,
            PlatformPermission.CREDENTIAL_ROTATE,
            PlatformPermission.MODEL_VIEW,
            PlatformPermission.MODEL_MANAGE,
            PlatformPermission.OPERATIONS_VIEW)),
    OPERATOR(EnumSet.of(
            PlatformPermission.GOVERNANCE_VIEW,
            PlatformPermission.INTEGRATION_VIEW,
            PlatformPermission.MODEL_VIEW,
            PlatformPermission.OPERATIONS_VIEW,
            PlatformPermission.OPERATIONS_MANAGE)),
    REVIEWER(EnumSet.of(PlatformPermission.MODEL_VIEW));

    private final Set<PlatformPermission> permissions;

    PlatformRole(Set<PlatformPermission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public Set<PlatformPermission> permissions() {
        return permissions;
    }

    public boolean allows(PlatformPermission permission) {
        return permissions.contains(permission);
    }

    public static PlatformRole fromStoredRole(String role) {
        if (role == null || role.isBlank()) {
            return REVIEWER;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "ADMIN", "ADMINISTRATOR", "管理员", "平台管理员" -> ADMIN;
            case "GOVERNANCE_MANAGER", "治理管理员", "治理负责人" -> GOVERNANCE_MANAGER;
            case "OPERATOR", "运营人员", "运营管理员" -> OPERATOR;
            case "REVIEWER", "审查员", "开发人员" -> REVIEWER;
            default -> REVIEWER;
        };
    }
}
