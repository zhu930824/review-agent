package com.review.agent.service;

import com.review.agent.domain.dto.ModelInvocationRequest;
import com.review.agent.domain.dto.ModelInvocationResponse;

public interface ModelInvocationPort {

    ModelInvocationResponse invoke(ModelInvocationRequest request) throws Exception;
}
