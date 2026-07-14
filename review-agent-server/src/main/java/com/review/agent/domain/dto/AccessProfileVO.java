package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AccessProfileVO {

    private Long userId;
    private String username;
    private String displayName;
    private String role;
    private String normalizedRole;
    private List<String> permissions;
}
