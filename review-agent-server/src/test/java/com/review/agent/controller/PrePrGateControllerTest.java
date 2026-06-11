package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.infrastructure.ci.CiStatusService;
import com.review.agent.infrastructure.ci.PrePrGateCiStatusPublisher;
import com.review.agent.service.PrePrGateService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrePrGateControllerTest {

    private final FakePrePrGateService gateService = new FakePrePrGateService();
    private final RecordingCiStatusService ciStatusService = new RecordingCiStatusService();
    private final PrePrGateController controller = new PrePrGateController(
            gateService,
            new PrePrGateCiStatusPublisher(gateService, ciStatusService));

    @Test
    void publishCiStatusUsesPersistedGateStatus() {
        gateService.gate = gate("BLOCKED", List.of("存在 1 个 BLOCKER 级别问题，Pre-PR 暂不可通过。"));

        Result<Void> result = controller.publishCiStatus(42L);

        assertTrue(result.isSuccess());
        assertEquals(List.of("block:42:存在 1 个 BLOCKER 级别问题，Pre-PR 暂不可通过。"), ciStatusService.calls);
    }

    @Test
    void refreshGatePublishesLatestGateToCiStatus() {
        gateService.gate = gate("PASSED", List.of());

        Result<PrePrGateVO> result = controller.refreshGate(42L);

        assertTrue(result.isSuccess());
        assertEquals("PASSED", result.getData().getGateStatus());
        assertEquals(List.of("pass:42:Pre-PR review passed"), ciStatusService.calls);
    }

    @Test
    void initializeGateReturnsPersistedInitialGateWithoutPublishingCi() {
        gateService.gate = gate("RUNNING", List.of());

        Result<PrePrGateVO> result = controller.initializeGate(42L);

        assertTrue(result.isSuccess());
        assertEquals("RUNNING", result.getData().getGateStatus());
        assertTrue(ciStatusService.calls.isEmpty());
    }

    @Test
    void manualDecisionPublishesUpdatedGateToCiStatus() {
        gateService.gate = gate("BLOCKED", List.of("人工决策：继续阻断"));
        PrePrGateDecisionRequest request = new PrePrGateDecisionRequest();
        request.setGateStatus("BLOCKED");
        request.setReason("继续阻断");
        request.setDecidedBy("alice");

        Result<PrePrGateVO> result = controller.decideGate(42L, request);

        assertTrue(result.isSuccess());
        assertEquals("BLOCKED", result.getData().getGateStatus());
        assertEquals(List.of("block:42:人工决策：继续阻断"), ciStatusService.calls);
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
