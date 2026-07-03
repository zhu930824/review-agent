package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.GovernanceRulePackChangeVO;
import com.review.agent.domain.dto.OperationRuleLearningCandidateVO;

import java.util.List;

public interface GovernanceRulePackChangeRepository {

    List<GovernanceRulePackChangeVO> listRecent(int limit);

    void proposeFromRuleLearningCandidate(OperationRuleLearningCandidateVO candidate);

    void updateStatus(Long id, String status);
}
