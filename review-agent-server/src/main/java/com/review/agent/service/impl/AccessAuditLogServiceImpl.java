package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.AccessAuditLogVO;
import com.review.agent.domain.entity.AccessAuditLog;
import com.review.agent.infrastructure.auth.AccessDecisionService;
import com.review.agent.infrastructure.persistence.AccessAuditLogMapper;
import com.review.agent.service.AccessAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessAuditLogServiceImpl implements AccessAuditLogService {

    private final AccessAuditLogMapper accessAuditLogMapper;

    @Override
    public List<AccessAuditLogVO> listRecent(int limit, String actionType, Long projectId) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        LambdaQueryWrapper<AccessAuditLog> query = new LambdaQueryWrapper<AccessAuditLog>()
                .eq(actionType != null && !actionType.isBlank(), AccessAuditLog::getActionType, normalize(actionType))
                .eq(projectId != null, AccessAuditLog::getProjectId, projectId)
                .orderByDesc(AccessAuditLog::getCreatedAt)
                .orderByDesc(AccessAuditLog::getId)
                .last("LIMIT " + safeLimit);
        return accessAuditLogMapper.selectList(query)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public void record(
            String actionType,
            AccessDecisionService.AccessIdentity actor,
            Long targetUserId,
            String targetUsername,
            Long projectId,
            String previousRole,
            String newRole,
            String detail) {
        AccessAuditLog log = new AccessAuditLog();
        log.setActorUserId(actor == null ? null : actor.userId());
        log.setActorUsername(actor == null ? "SYSTEM" : actor.username());
        log.setActionType(normalize(actionType));
        log.setActionStatus("SUCCESS");
        log.setTargetUserId(targetUserId);
        log.setTargetUsername(targetUsername);
        log.setProjectId(projectId);
        log.setPreviousRole(previousRole);
        log.setNewRole(newRole);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        accessAuditLogMapper.insert(log);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private AccessAuditLogVO toVO(AccessAuditLog log) {
        AccessAuditLogVO vo = new AccessAuditLogVO();
        vo.setId(log.getId());
        vo.setActorUserId(log.getActorUserId());
        vo.setActorUsername(log.getActorUsername());
        vo.setActionType(log.getActionType());
        vo.setActionStatus(log.getActionStatus());
        vo.setTargetUserId(log.getTargetUserId());
        vo.setTargetUsername(log.getTargetUsername());
        vo.setProjectId(log.getProjectId());
        vo.setPreviousRole(log.getPreviousRole());
        vo.setNewRole(log.getNewRole());
        vo.setDetail(log.getDetail());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
