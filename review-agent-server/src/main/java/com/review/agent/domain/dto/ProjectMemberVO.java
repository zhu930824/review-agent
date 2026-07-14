package com.review.agent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectMemberVO {

    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String status;
    private Boolean currentUser;
    private List<String> permissions;
    private LocalDateTime updatedAt;
}
