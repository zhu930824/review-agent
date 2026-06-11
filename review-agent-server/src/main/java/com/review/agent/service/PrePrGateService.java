package com.review.agent.service;

import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateVO;

public interface PrePrGateService {

    PrePrGateVO getGate(Long reviewId);

    PrePrGateVO initializeGate(Long reviewId);

    PrePrGateVO refreshGate(Long reviewId);

    PrePrGateVO decideGate(Long reviewId, PrePrGateDecisionRequest request);
}
