package com.review.agent.service;

import com.review.agent.domain.dto.OperationBusinessImpactVO;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.dto.OperationOwnerLoadVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;

import java.util.List;

public interface OperationsRemediationQueueService {

    List<OperationFindingVO> listQueue(int limit);

    List<OperationOwnerLoadVO> listOwnerLoad();

    List<OperationRuleLearningCandidateVO> listRuleLearningCandidates(int limit);

    OperationBusinessImpactVO estimateBusinessImpact();

    void confirmFinding(Long findingId);

    void dismissFinding(Long findingId);
}
