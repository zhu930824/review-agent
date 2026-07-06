package com.review.agent.service;

import com.review.agent.domain.dto.OperationsCiHealthActionVO;
import com.review.agent.domain.dto.IntegrationActionLogVO;

import java.util.List;

public interface OperationsCiHealthActionService {

    List<OperationsCiHealthActionVO> listActions();

    IntegrationActionLogVO notifyAction(String actionKey);
}
