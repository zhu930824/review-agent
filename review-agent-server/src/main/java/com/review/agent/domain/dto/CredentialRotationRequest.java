package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredentialRotationRequest {

    @NotBlank
    private String confirmation;
}
