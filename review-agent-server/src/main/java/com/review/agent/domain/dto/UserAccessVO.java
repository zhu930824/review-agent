package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAccessVO {

    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String normalizedRole;
    private String status;
    private List<String> permissions;
}
