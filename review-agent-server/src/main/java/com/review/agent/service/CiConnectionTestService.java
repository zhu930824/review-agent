package com.review.agent.service;

import com.review.agent.domain.dto.CiConnectionTestResultVO;

public interface CiConnectionTestService {

    CiConnectionTestResultVO test(String connectorKey);
}
