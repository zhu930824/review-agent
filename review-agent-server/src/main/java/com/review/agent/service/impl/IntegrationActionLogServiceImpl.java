package com.review.agent.service.impl;

import com.review.agent.domain.dto.IntegrationActionLogVO;
import com.review.agent.domain.entity.IntegrationActionLog;
import com.review.agent.infrastructure.persistence.IntegrationActionLogRepository;
import com.review.agent.service.IntegrationActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationActionLogServiceImpl implements IntegrationActionLogService {

    private final IntegrationActionLogRepository repository;

    @Override
    public List<IntegrationActionLogVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return repository.listRecent(safeLimit).stream()
                .map(this::toVO)
                .toList();
    }

    private IntegrationActionLogVO toVO(IntegrationActionLog log) {
        IntegrationActionLogVO vo = new IntegrationActionLogVO();
        vo.setId(log.getId());
        vo.setConnectorKey(log.getConnectorKey());
        vo.setProvider(log.getProvider());
        vo.setActionType(log.getActionType());
        vo.setActionStatus(log.getActionStatus());
        vo.setTargetKey(log.getTargetKey());
        vo.setCommitSha(log.getCommitSha());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());
        return vo;
    }
}
