package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.OperationFindingVO;

import java.util.List;

public interface OperationsRemediationQueueRepository {

    List<OperationFindingVO> listOpenFindings(int limit);

    List<OperationFindingVO> listFindings(int limit);
}
