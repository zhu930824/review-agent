package com.review.agent.service;

import com.review.agent.domain.dto.CredentialRotationRequest;
import com.review.agent.domain.dto.CredentialRotationResultVO;

public interface CredentialRotationService {

    CredentialRotationResultVO rotate(CredentialRotationRequest request);
}
