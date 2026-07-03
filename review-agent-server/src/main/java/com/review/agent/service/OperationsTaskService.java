package com.review.agent.service;

import com.review.agent.domain.dto.OperationsTaskVO;

import java.util.List;

public interface OperationsTaskService {

    List<OperationsTaskVO> listTasks(int limit);
}
