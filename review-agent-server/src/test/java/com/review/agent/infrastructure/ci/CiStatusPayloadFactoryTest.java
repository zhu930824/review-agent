package com.review.agent.infrastructure.ci;

import com.review.agent.domain.entity.Project;
import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.persistence.ProjectMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CiStatusPayloadFactoryTest {

    @Test
    void buildsProviderNeutralFailurePayloadWithReviewTargetUrl() {
        CiStatusPayloadFactory factory = new CiStatusPayloadFactory(
                "review-agent/pre-pr",
                "https://review-agent.local/reviews/{reviewId}");

        CiStatusPayload payload = factory.build(
                42L,
                CiStatusState.FAILURE,
                "BLOCKER findings found");

        assertEquals(42L, payload.reviewId());
        assertEquals(CiStatusState.FAILURE, payload.state());
        assertEquals("review-agent/pre-pr", payload.context());
        assertEquals("BLOCKER findings found", payload.description());
        assertEquals("https://review-agent.local/reviews/42", payload.targetUrl());
    }

    @Test
    void mapsGateOutcomesToProviderNeutralStates() {
        CiStatusPayloadFactory factory = new CiStatusPayloadFactory("review-agent/pre-pr", "");

        assertEquals(CiStatusState.SUCCESS, factory.pass(1L, "passed").state());
        assertEquals(CiStatusState.FAILURE, factory.block(1L, "blocked").state());
        assertEquals(CiStatusState.PENDING, factory.running(1L, "running").state());
    }

    @Test
    void enrichesPayloadWithRepositoryAndSourceCommitForProviderEndpoint() {
        ReviewMapper reviewMapper = mock(ReviewMapper.class);
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        Review review = new Review();
        review.setProjectId(7L);
        review.setSourceCommit("abc123");
        Project project = new Project();
        project.setRepoUrl("https://github.com/acme/review-agent.git");
        when(reviewMapper.selectById(42L)).thenReturn(review);
        when(projectMapper.selectById(7L)).thenReturn(project);

        CiStatusPayloadFactory factory = new CiStatusPayloadFactory(
                "review-agent/pre-pr",
                "https://review-agent.local/reviews/{reviewId}",
                reviewMapper,
                projectMapper);

        CiStatusPayload payload = factory.pass(42L, "passed");

        assertEquals("https://github.com/acme/review-agent.git", payload.repoUrl());
        assertEquals("abc123", payload.statusSha());
    }
}
