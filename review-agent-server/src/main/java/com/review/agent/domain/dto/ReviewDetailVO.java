package com.review.agent.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewDetailVO {

    private ReviewVO review;
    private List<ReviewFindingVO> findings;
    private List<ReviewModelResultVO> modelResults;
    private int totalFindings;
    private int blockerCount;
    private int majorCount;
    private int minorCount;
    private int infoCount;

    /** Pre-PR 状态：PASSED/BLOCKED，非 Pre-PR 审查为 null */
    private String prePrStatus;
    /** 阻塞原因列表，仅 Pre-PR 审查且状态为 BLOCKED 时有值 */
    private List<String> blockedReasons;
}
