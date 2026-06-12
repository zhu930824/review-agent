package com.review.agent.service.impl;

import com.review.agent.domain.dto.PrePrGateDecisionRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.domain.entity.CiStatusWritebackLog;
import com.review.agent.infrastructure.ci.CiStatusService;
import com.review.agent.infrastructure.ci.PrePrGateCiStatusPublisher;
import com.review.agent.infrastructure.persistence.CiStatusWritebackRetryRepository;
import com.review.agent.service.PrePrGateService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CiStatusWritebackRetryServiceImplTest {

    private final FakeRetryRepository retryRepository = new FakeRetryRepository();
    private final FakePrePrGateService gateService = new FakePrePrGateService();
    private final RecordingCiStatusService ciStatusService = new RecordingCiStatusService();
    private final CiStatusWritebackRetryServiceImpl service = new CiStatusWritebackRetryServiceImpl(
            retryRepository,
            new PrePrGateCiStatusPublisher(gateService, ciStatusService));

    @Test
    void retriesFailedWritebackByRepublishingPersistedGate() {
        retryRepository.log = log("FAILED", 2);
        gateService.gate = gate("BLOCKED", List.of("人工决策：继续阻断"));

        service.retry(7L);

        assertEquals(3, retryRepository.log.getRetryCount());
        assertEquals(null, retryRepository.log.getNextRetryAt());
        assertEquals(List.of("block:42:人工决策：继续阻断"), ciStatusService.calls);
    }

    @Test
    void rejectsRetryForSuccessfulWriteback() {
        retryRepository.log = log("SUCCESS", 0);

        assertThrows(IllegalStateException.class, () -> service.retry(7L));
    }

    @Test
    void retriesDueFailedWritebacksOnly() {
        CiStatusWritebackLog due = log("FAILED", 0);
        due.setId(8L);
        due.setReviewId(43L);
        due.setNextRetryAt(LocalDateTime.now().minusMinutes(1));
        retryRepository.dueLogs = List.of(due);
        gateService.gate = gate("PASSED", List.of());

        int retried = service.retryDueWritebacks();

        assertEquals(1, retried);
        assertEquals(1, retryRepository.log.getRetryCount());
        assertEquals(null, retryRepository.log.getNextRetryAt());
        assertEquals(List.of("pass:43:Pre-PR review passed"), ciStatusService.calls);
    }

    @Test
    void continuesDueRetryBatchWhenOneLogCannotBeRetried() {
        CiStatusWritebackLog missingReview = log("FAILED", 0);
        missingReview.setId(8L);
        missingReview.setReviewId(null);
        CiStatusWritebackLog retryable = log("FAILED", 0);
        retryable.setId(9L);
        retryable.setReviewId(44L);
        retryRepository.dueLogs = List.of(missingReview, retryable);
        gateService.gate = gate("PASSED", List.of());

        int retried = service.retryDueWritebacks();

        assertEquals(1, retried);
        assertEquals(9L, retryRepository.log.getId());
        assertEquals(List.of("pass:44:Pre-PR review passed"), ciStatusService.calls);
    }

    private CiStatusWritebackLog log(String status, int retryCount) {
        CiStatusWritebackLog log = new CiStatusWritebackLog();
        log.setId(7L);
        log.setReviewId(42L);
        log.setWritebackStatus(status);
        log.setRetryCount(retryCount);
        log.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
        return log;
    }

    private PrePrGateVO gate(String status, List<String> reasons) {
        PrePrGateVO gate = new PrePrGateVO();
        gate.setReviewId(42L);
        gate.setGateStatus(status);
        gate.setBlockedReasons(reasons);
        return gate;
    }

    private static class FakeRetryRepository implements CiStatusWritebackRetryRepository {
        private CiStatusWritebackLog log;
        private List<CiStatusWritebackLog> dueLogs = List.of();

        @Override
        public Optional<CiStatusWritebackLog> findById(Long id) {
            return Optional.ofNullable(log);
        }

        @Override
        public List<CiStatusWritebackLog> findDueFailedWritebacks(LocalDateTime now, int limit) {
            return dueLogs;
        }

        @Override
        public void markRetrying(CiStatusWritebackLog log) {
            this.log = log;
        }
    }

    private static class FakePrePrGateService implements PrePrGateService {
        private PrePrGateVO gate;

        @Override
        public PrePrGateVO getGate(Long reviewId) {
            return gate;
        }

        @Override
        public PrePrGateVO initializeGate(Long reviewId) {
            return gate;
        }

        @Override
        public PrePrGateVO refreshGate(Long reviewId) {
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
