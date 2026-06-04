package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.Project;
import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CiStatusPayloadFactory {

    private final String context;
    private final String targetUrlTemplate;
    private final ReviewMapper reviewMapper;
    private final ProjectMapper projectMapper;

    public CiStatusPayloadFactory(
            @Value("${review-agent.ci-status.context:review-agent/pre-pr}") String context,
            @Value("${review-agent.ci-status.target-url-template:}") String targetUrlTemplate,
            ReviewMapper reviewMapper,
            ProjectMapper projectMapper) {
        this.context = context == null || context.isBlank() ? "review-agent/pre-pr" : context;
        this.targetUrlTemplate = targetUrlTemplate == null ? "" : targetUrlTemplate;
        this.reviewMapper = reviewMapper;
        this.projectMapper = projectMapper;
    }

    CiStatusPayloadFactory(String context, String targetUrlTemplate) {
        this(context, targetUrlTemplate, null, null);
    }

    public CiStatusPayload pass(Long reviewId, String description) {
        return build(reviewId, CiStatusState.SUCCESS, description);
    }

    public CiStatusPayload block(Long reviewId, String description) {
        return build(reviewId, CiStatusState.FAILURE, description);
    }

    public CiStatusPayload running(Long reviewId, String description) {
        return build(reviewId, CiStatusState.PENDING, description);
    }

    public CiStatusPayload build(Long reviewId, CiStatusState state, String description) {
        Review review = reviewMapper == null ? null : reviewMapper.selectById(reviewId);
        Project project = review == null || projectMapper == null ? null : projectMapper.selectById(review.getProjectId());
        return new CiStatusPayload(
                reviewId,
                state,
                context,
                description == null ? "" : description,
                resolveTargetUrl(reviewId),
                project == null ? "" : nullToBlank(project.getRepoUrl()),
                review == null ? "" : nullToBlank(review.getSourceCommit()));
    }

    private String resolveTargetUrl(Long reviewId) {
        if (targetUrlTemplate.isBlank()) {
            return "";
        }
        return targetUrlTemplate.replace("{reviewId}", String.valueOf(reviewId));
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
