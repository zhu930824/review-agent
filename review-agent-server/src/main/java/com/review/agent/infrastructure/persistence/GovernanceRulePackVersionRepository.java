package com.review.agent.infrastructure.persistence;

import com.review.agent.domain.dto.GovernanceRulePackDryRunVO;
import com.review.agent.domain.dto.GovernanceRulePackVersionVO;

import java.util.List;

public interface GovernanceRulePackVersionRepository {

    List<GovernanceRulePackVersionVO> listRecent(int limit);

    GovernanceRulePackDryRunVO dryRunChange(Long changeId);

    void applyChange(Long changeId);

    void rollbackChange(Long changeId);
}
