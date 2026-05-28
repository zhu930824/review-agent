package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.fix.FixDraft;
import com.review.agent.infrastructure.fix.FixDraftGenerator;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.domain.entity.ReviewFinding;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/{reviewId}/fix-drafts")
@RequiredArgsConstructor
public class FixDraftController {

    private final FixDraftGenerator fixDraftGenerator;
    private final ReviewFindingMapper reviewFindingMapper;

    @GetMapping
    public Result<List<FixDraft>> generateDrafts(@PathVariable("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        return Result.success(fixDraftGenerator.generate(findings));
    }
}
