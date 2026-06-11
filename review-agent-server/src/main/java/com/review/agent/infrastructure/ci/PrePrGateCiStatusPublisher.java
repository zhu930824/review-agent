package com.review.agent.infrastructure.ci;

import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.service.PrePrGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrePrGateCiStatusPublisher {

    private final PrePrGateService prePrGateService;
    private final CiStatusService ciStatusService;

    public void publishGateStatus(Long reviewId) {
        PrePrGateVO gate = prePrGateService.getGate(reviewId);
        String status = gate.getGateStatus();
        if ("PASSED".equals(status)) {
            ciStatusService.reportPass(reviewId, "Pre-PR review passed");
            return;
        }
        if ("BLOCKED".equals(status) || "NEEDS_HUMAN_REVIEW".equals(status)) {
            ciStatusService.reportBlock(reviewId, firstReasonOrDefault(gate.getBlockedReasons()));
            return;
        }
        ciStatusService.reportRunning(reviewId, "Pre-PR review is running");
    }

    private String firstReasonOrDefault(List<String> reasons) {
        if (reasons == null || reasons.isEmpty() || reasons.getFirst() == null || reasons.getFirst().isBlank()) {
            return "Pre-PR review did not pass";
        }
        return reasons.getFirst();
    }
}
