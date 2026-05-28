package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.infrastructure.agent.role.ArchitectureCommitteeOrchestrator;
import com.review.agent.infrastructure.agent.role.RefactorPlannerAgent;
import com.review.agent.infrastructure.knowledge.ArchitectureAdvice;
import com.review.agent.infrastructure.knowledge.ArchitectureBrain;
import com.review.agent.infrastructure.knowledge.KnowledgeGraph;
import com.review.agent.infrastructure.knowledge.KnowledgeGraphEngine;
import com.review.agent.infrastructure.knowledge.KnowledgeNode;
import com.review.agent.infrastructure.refactor.RefactorPlan;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.domain.entity.ReviewFinding;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeGraphEngine graphEngine;
    private final ArchitectureBrain architectureBrain;
    private final ArchitectureCommitteeOrchestrator committeeOrchestrator;
    private final RefactorPlannerAgent refactorPlannerAgent;
    private final ReviewFindingMapper reviewFindingMapper;

    @GetMapping("/graph")
    public Result<KnowledgeGraph> getGraph(
            @RequestParam(value = "reviewId", required = false) Long reviewId) {
        if (reviewId != null) {
            List<ReviewFinding> findings = reviewFindingMapper.selectList(
                    new LambdaQueryWrapper<ReviewFinding>()
                            .eq(ReviewFinding::getReviewId, reviewId));
            return Result.success(graphEngine.buildFullGraph(findings));
        }
        return Result.success(graphEngine.buildFullGraph(null));
    }

    @GetMapping("/query")
    public Result<List<KnowledgeNode>> query(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(graphEngine.query(keyword));
    }

    @GetMapping("/related/{nodeId}")
    public Result<List<KnowledgeNode>> findRelated(@PathVariable("nodeId") String nodeId) {
        return Result.success(graphEngine.findRelated(nodeId));
    }

    @GetMapping("/brain/analyze")
    public Result<ArchitectureAdvice> analyzeReview(
            @RequestParam("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        return Result.success(architectureBrain.analyze(findings));
    }

    @PostMapping("/committee/review")
    public Result<ArchitectureCommitteeOrchestrator.CommitteeVerdict> committeeReview(
            @RequestParam("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        List<String> changedFiles = findings.stream()
                .map(ReviewFinding::getFilePath)
                .distinct()
                .toList();
        ArchitectureAdvice advice = architectureBrain.analyze(findings);
        return Result.success(committeeOrchestrator.review(changedFiles, advice));
    }

    @GetMapping("/refactor-plan")
    public Result<RefactorPlan> getRefactorPlan(
            @RequestParam("reviewId") Long reviewId) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));
        return Result.success(refactorPlannerAgent.planWithContext(findings));
    }
}
