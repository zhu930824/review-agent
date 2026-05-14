package com.review.agent.service;

import com.review.agent.domain.dto.AuthRequest;
import com.review.agent.domain.dto.AuthTokenVO;
import com.review.agent.domain.dto.RegisterRequest;

public interface AuthService {
    AuthTokenVO register(RegisterRequest request);

    AuthTokenVO login(AuthRequest request);
}
