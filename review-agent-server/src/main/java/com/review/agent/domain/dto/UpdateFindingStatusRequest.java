package com.review.agent.domain.dto;

import com.review.agent.domain.enums.HumanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 更新审查发现人工状态的请求 */
@Data
public class UpdateFindingStatusRequest {

    @NotNull
    private HumanStatus humanStatus;
}
