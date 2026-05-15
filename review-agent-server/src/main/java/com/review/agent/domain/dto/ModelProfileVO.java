package com.review.agent.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ModelProfileVO {

    private Long id;

    private Long providerId;

    private String providerName;

    private String profileKey;

    private String modelName;

    private String displayName;

    private List<String> capabilityTags;

    private BigDecimal defaultTemperature;

    private Integer timeoutSeconds;

    private Boolean enabled;
}