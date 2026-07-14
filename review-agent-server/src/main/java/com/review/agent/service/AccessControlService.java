package com.review.agent.service;

import com.review.agent.domain.dto.AccessProfileVO;
import com.review.agent.domain.dto.AccessAuditLogVO;
import com.review.agent.domain.dto.UpdateUserRoleRequest;
import com.review.agent.domain.dto.UserAccessVO;

import java.util.List;

public interface AccessControlService {

    AccessProfileVO currentProfile();

    List<UserAccessVO> listUsers();

    List<AccessAuditLogVO> listAuditLogs(int limit, String actionType, Long projectId);

    UserAccessVO updateRole(Long userId, UpdateUserRoleRequest request);
}
