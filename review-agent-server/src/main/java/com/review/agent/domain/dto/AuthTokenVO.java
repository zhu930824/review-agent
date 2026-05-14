package com.review.agent.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokenVO {
    private String token;
    private String tokenType;
    private Long expiresAt;
    private UserVO user;
}
