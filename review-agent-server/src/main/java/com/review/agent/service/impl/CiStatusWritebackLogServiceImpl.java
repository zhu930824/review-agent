package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.CiStatusWritebackLogVO;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.persistence.CiStatusWritebackLogMapper;
import com.review.agent.service.CiStatusWritebackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CiStatusWritebackLogServiceImpl implements CiStatusWritebackLogService {

    private static final String CONNECTOR_KEY = "github-checks";
    private static final String PROVIDER = "GITHUB";

    private final CiStatusWritebackLogMapper ciStatusWritebackLogMapper;

    @Override
    public List<CiStatusWritebackLogVO> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return ciStatusWritebackLogMapper.selectList(
                new LambdaQueryWrapper<CiStatusWritebackLog>()
                        .orderByDesc(CiStatusWritebackLog::getCreatedAt)
                        .last("LIMIT " + safeLimit))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public void recordSuccess(Long reviewId, String commitSha, String state, String requestUrl) {
        insert(reviewId, commitSha, state, "SUCCESS", requestUrl, null, null);
    }

    @Override
    public void recordFailure(Long reviewId, String commitSha, String state, String requestUrl, String errorMessage) {
        insert(reviewId, commitSha, state, "FAILED", requestUrl, truncate(errorMessage), LocalDateTime.now().plusMinutes(5));
    }

    @Override
    public void recordSkipped(Long reviewId, String state, String reason) {
        insert(reviewId, null, state, "SKIPPED", null, truncate(reason), null);
    }

    private void insert(
            Long reviewId,
            String commitSha,
            String state,
            String writebackStatus,
            String requestUrl,
            String errorMessage,
            LocalDateTime nextRetryAt) {
        LocalDateTime now = LocalDateTime.now();
        CiStatusWritebackLog log = new CiStatusWritebackLog();
        log.setConnectorKey(CONNECTOR_KEY);
        log.setProvider(PROVIDER);
        log.setReviewId(reviewId);
        log.setCommitSha(commitSha);
        log.setState(state);
        log.setWritebackStatus(writebackStatus);
        log.setRequestUrl(requestUrl);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(0);
        log.setNextRetryAt(nextRetryAt);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        ciStatusWritebackLogMapper.insert(log);
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 2000) {
            return message;
        }
        return message.substring(0, 2000);
    }

    private CiStatusWritebackLogVO toVO(CiStatusWritebackLog log) {
        CiStatusWritebackLogVO vo = new CiStatusWritebackLogVO();
        vo.setId(log.getId());
        vo.setConnectorKey(log.getConnectorKey());
        vo.setProvider(log.getProvider());
        vo.setReviewId(log.getReviewId());
        vo.setCommitSha(log.getCommitSha());
        vo.setState(log.getState());
        vo.setWritebackStatus(log.getWritebackStatus());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setRetryCount(log.getRetryCount());
        vo.setNextRetryAt(log.getNextRetryAt());
        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());
        return vo;
    }
}
