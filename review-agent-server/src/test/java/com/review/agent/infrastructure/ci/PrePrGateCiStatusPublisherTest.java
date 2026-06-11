package com.review.agent.infrastructure.ci;

import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.service.PrePrGateService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrePrGateCiStatusPublisherTest {

    private final FakePrePrGateService gateService = new FakePrePrGateService();
    private final RecordingCiStatusService ciStatusService = new RecordingCiStatusService();
    private final PrePrGateCiStatusPublisher publisher = new PrePrGateCiStatusPublisher(gateService, ciStatusService);

    @Test
    void publishesPassedGateAsSuccessfulCiStatus() {
        gateService.gate = gate("PASSED", List.of());

        publisher.publishGateStatus(42L);

        assertEquals(List.of("pass:42:Pre-PR review passed"), ciStatusService.calls);
    }

    @Test
    void publishesBlockedGateAsFailedCiStatusWithReason() {
        gateService.gate = gate("BLOCKED", List.of("存在 1 个 BLOCKER 级别问题，Pre-PR 暂不可通过。"));

        publisher.publishGateStatus(42L);

        assertEquals(List.of("block:42:存在 1 个 BLOCKER 级别问题，Pre-PR 暂不可通过。"), ciStatusService.calls);
    }

    @Test
    void publishesHumanReviewGateAsFailedCiStatus() {
        gateService.gate = gate("NEEDS_HUMAN_REVIEW", List.of("存在 1 个 MAJOR 问题等待人工复核。"));

        publisher.publishGateStatus(42L);

        assertEquals(List.of("block:42:存在 1 个 MAJOR 问题等待人工复核。"), ciStatusService.calls);
    }

    @Test
    void publishesRunningGateAsPendingCiStatus() {
        gateService.gate = gate("RUNNING", List.of());

        publisher.publishGateStatus(42L);

        assertEquals(List.of("running:42:Pre-PR review is running"), ciStatusService.calls);
    }

    private PrePrGateVO gate(String status, List<String> reasons) {
        PrePrGateVO gate = new PrePrGateVO();
        gate.setReviewId(42L);
        gate.setGateStatus(status);
        gate.setBlockedReasons(reasons);
        return gate;
    }

    private static class FakePrePrGateService implements PrePrGateService {
        private PrePrGateVO gate;

        @Override
        public PrePrGateVO getGate(Long reviewId) {
            return gate;
        }

        @Override
        public PrePrGateVO refreshGate(Long reviewId) {
            return gate;
        }

        @Override
        public PrePrGateVO initializeGate(Long reviewId) {
            return gate;
        }

        @Override
        public PrePrGateVO decideGate(Long reviewId, PrePrGateDecisionRequest request) {
            return gate;
        }
    }

    private static class RecordingCiStatusService implements CiStatusService {
        private final List<String> calls = new ArrayList<>();

        @Override
        public void reportPass(Long reviewId, String description) {
            calls.add("pass:" + reviewId + ":" + description);
        }

        @Override
        public void reportBlock(Long reviewId, String description) {
            calls.add("block:" + reviewId + ":" + description);
        }

        @Override
        public void reportRunning(Long reviewId, String description) {
            calls.add("running:" + reviewId + ":" + description);
        }
    }
}
