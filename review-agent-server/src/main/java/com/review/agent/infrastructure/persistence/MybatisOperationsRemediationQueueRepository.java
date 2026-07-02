package com.review.agent.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.OperationFindingVO;
import com.review.agent.domain.entity.Project;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.enums.HumanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MybatisOperationsRemediationQueueRepository implements OperationsRemediationQueueRepository {

    private final ReviewFindingMapper reviewFindingMapper;
    private final ReviewMapper reviewMapper;
    private final ProjectMapper projectMapper;

    @Override
    public List<OperationFindingVO> listOpenFindings(int limit) {
        return listFindings(limit).stream()
                .filter(finding -> finding.getHumanStatus() != HumanStatus.DISMISSED)
                .toList();
    }

    @Override
    public List<OperationFindingVO> listFindings(int limit) {
        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new LambdaQueryWrapper<ReviewFinding>()
                        .orderByDesc(ReviewFinding::getCreatedAt)
                        .last("LIMIT " + Math.max(1, limit)));
        if (findings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Review> reviewsById = reviewsById(findings);
        Map<Long, Project> projectsById = projectsById(reviewsById);
        return findings.stream()
                .map(finding -> toVO(finding, reviewsById, projectsById))
                .toList();
    }

    @Override
    public void updateHumanStatus(Long findingId, HumanStatus humanStatus) {
        ReviewFinding finding = new ReviewFinding();
        finding.setId(findingId);
        finding.setHumanStatus(humanStatus);
        reviewFindingMapper.updateById(finding);
    }

    private Map<Long, Review> reviewsById(List<ReviewFinding> findings) {
        List<Long> reviewIds = findings.stream()
                .map(ReviewFinding::getReviewId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewMapper.selectBatchIds(reviewIds).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Project> projectsById(Map<Long, Review> reviewsById) {
        List<Long> projectIds = reviewsById.values().stream()
                .map(Review::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity(), (left, right) -> left));
    }

    private OperationFindingVO toVO(ReviewFinding finding, Map<Long, Review> reviewsById, Map<Long, Project> projectsById) {
        Review review = reviewsById.get(finding.getReviewId());
        Project project = review == null ? null : projectsById.get(review.getProjectId());

        OperationFindingVO vo = new OperationFindingVO();
        vo.setId(finding.getId());
        vo.setReviewId(finding.getReviewId());
        vo.setProjectName(project == null ? "unknown-project" : project.getName());
        vo.setSeverity(finding.getSeverity());
        vo.setCategory(finding.getCategory());
        vo.setTitle(finding.getTitle());
        vo.setHumanStatus(finding.getHumanStatus());
        vo.setConfidence(finding.getConfidence());
        vo.setIsCrossHit(finding.getIsCrossHit());
        return vo;
    }
}
