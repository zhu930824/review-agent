package com.review.agent.infrastructure.auth;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum ProjectRole {
    OWNER(EnumSet.allOf(ProjectPermission.class)),
    MAINTAINER(EnumSet.of(
            ProjectPermission.VIEW,
            ProjectPermission.REVIEW_EXECUTE,
            ProjectPermission.PROJECT_MANAGE)),
    REVIEWER(EnumSet.of(
            ProjectPermission.VIEW,
            ProjectPermission.REVIEW_EXECUTE));

    private final Set<ProjectPermission> permissions;

    ProjectRole(Set<ProjectPermission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public boolean allows(ProjectPermission permission) {
        return permissions.contains(permission);
    }

    public Set<ProjectPermission> permissions() {
        return permissions;
    }

    public static ProjectRole parse(String role) {
        return valueOf(role.trim().toUpperCase(Locale.ROOT));
    }
}
