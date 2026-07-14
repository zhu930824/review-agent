package com.review.agent.domain.dto;

import lombok.Data;

@Data
public class CredentialSecurityHealthVO {

    private String status;
    private Boolean encryptionEnabled;
    private Boolean developmentKey;
    private Boolean previousKeysConfigured;
    private Boolean rotationRequired;
    private Long encryptedCredentialCount;
    private Long plaintextCredentialCount;
    private Long activeKeyCredentialCount;
    private Long previousKeyCredentialCount;
    private Long unreadableCredentialCount;
    private String recommendation;
}
