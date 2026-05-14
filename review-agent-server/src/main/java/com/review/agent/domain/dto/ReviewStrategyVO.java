package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewStrategyVO {

    private Long id;

    private String strategyKey;

    private String name;

    private String reviewMode;

    private String description;

    private List<String> recommendedFor;

    private List<String> blockOn;

    private List<String> requireHumanReviewOn;

    private List<String> advisoryOn;

    private Boolean enabled;

    private List<RoleBindingVO> roleBindings;
}