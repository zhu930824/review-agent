package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.enums.HumanStatus;

import java.util.List;

public interface OperationsRemediationQueueRepository {

    List<OperationFindingVO> listOpenFindings(int limit);

    List<OperationFindingVO> listFindings(int limit);

    void updateHumanStatus(Long findingId, HumanStatus humanStatus);
}
