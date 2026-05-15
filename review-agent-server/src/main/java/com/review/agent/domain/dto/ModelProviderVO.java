package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelProviderVO {

    private Long id;

    private String providerKey;

    private String name;

    private String providerType;

    private String endpoint;

    private String apiKeyEnv;

    private Boolean enabled;

    private String description;

    private List<ModelProfileVO> profiles;
}