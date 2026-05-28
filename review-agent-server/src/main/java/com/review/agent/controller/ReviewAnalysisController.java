package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.infrastructure.refactor.RefactorPlan;
import com.review.agent.infrastructure.refactor.RefactorPlannerService;
import com.review.agent.infrastructure.risk.ChangeRiskPredictor;
import com.review.agent.infrastructure.risk.RiskAssessment;
import com.review.agent.infrastructure.testgen.TestCoveragePlan;
import com.review.agent.infrastructure.testgen.TestCaseGenerationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/{reviewId}")
@RequiredArgsConstructor
public class ReviewAnalysisController {

    private final TestCaseGenerationService testGenService;
    private final RefactorPlannerService refactorPlanner;
    private final ChangeRiskPredictor riskPredictor;
    private final ReviewFindingMapper reviewFindingMapper;
    private final ReviewMapper reviewMapper;

    @PostMapping("/generate-tests")
    public Result<TestCoveragePlan> generateTests(@PathVariable("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        List<String> changedFiles = findings.stream()
                .map(ReviewFinding::getFilePath)
                .distinct()
                .toList();
        return Result.success(testGenService.generate(reviewId, findings, changedFiles));
    }

    @PostMapping("/refactor-plan")
    public Result<RefactorPlan> generateRefactorPlan(@PathVariable("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        return Result.success(refactorPlanner.generatePlan(findings));
    }

    @GetMapping("/risk")
    public Result<RiskAssessment> predictRisk(@PathVariable("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        List<String> changedFiles = findings.stream()
                .map(ReviewFinding::getFilePath)
                .distinct()
                .toList();
        return Result.success(riskPredictor.predict(findings, changedFiles));
    }
}
