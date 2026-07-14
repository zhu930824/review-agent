package com.review.agent.service;

import com.review.agent.domain.dto.AccessAuditLogVO;
import com.review.agent.infrastructure.auth.AccessDecisionService;

import java.util.List;

public interface AccessAuditLogService {

    List<AccessAuditLogVO> listRecent(int limit, String actionType, Long projectId);

    void record(
            String actionType,
            AccessDecisionService.AccessIdentity actor,
            Long targetUserId,
            String targetUsername,
            Long projectId,
            String previousRole,
            String newRole,
            String detail);
}
