package com.review.agent.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String role;
}
