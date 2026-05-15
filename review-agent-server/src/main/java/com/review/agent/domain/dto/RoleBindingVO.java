package com.review.agent.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoleBindingVO {

    private Long id;

    private String role;

    private Long modelProfileId;

    private String modelProfileName;

    private List<String> skills;

    private BigDecimal temperature;
}