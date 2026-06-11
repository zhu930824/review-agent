package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.PrePrGateFindingInput;
import com.review.agent.domain.dto.PrePrGateVO;

import java.util.List;
import java.util.Optional;

public interface PrePrGateRepository {

    Optional<String> findReviewStatus(Long reviewId);

    List<PrePrGateFindingInput> listFindings(Long reviewId);

    Optional<PrePrGateVO> findGate(Long reviewId);

    PrePrGateVO upsertGate(PrePrGateVO gate);

    void appendGateHistory(Long reviewId, String gateStatus, String eventType, String reason, String operator);
}
