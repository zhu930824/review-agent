package com.review.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertProjectMemberRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String role;
}
