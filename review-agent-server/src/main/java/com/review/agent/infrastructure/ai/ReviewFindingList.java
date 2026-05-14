package com.review.agent.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewFindingList {
    private List<ReviewFindingResult> findings;
}
