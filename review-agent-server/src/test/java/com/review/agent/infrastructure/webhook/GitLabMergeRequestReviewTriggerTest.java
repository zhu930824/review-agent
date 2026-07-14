package com.review.agent.infrastructure.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.dto.CreatePrePrRequest;
import com.review.agent.domain.dto.ReviewDetailVO;
import com.review.agent.domain.dto.ReviewVO;
import com.review.agent.domain.entity.IntegrationWebhookReviewTrigger;
import com.review.agent.domain.entity.ProjectGitLabConfig;
import com.review.agent.infrastructure.persistence.IntegrationWebhookReviewTriggerRepository;
import com.review.agent.infrastructure.persistence.ProjectGitLabConfigMapper;
import com.review.agent.service.ReviewService;
import com.review.agent.service.GitLabMergeRequestNoteService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class GitLabMergeRequestReviewTriggerTest {

    private final ProjectGitLabConfigMapper configMapper = mock(ProjectGitLabConfigMapper.class);
    private final IntegrationWebhookReviewTriggerRepository triggerRepository = mock(IntegrationWebhookReviewTriggerRepository.class);
    private final ReviewService reviewService = mock(ReviewService.class);
    private final GitLabMergeRequestNoteService noteService = mock(GitLabMergeRequestNoteService.class);
    private final GitLabMergeRequestReviewTrigger trigger = new GitLabMergeRequestReviewTrigger(
            new ObjectMapper(), configMapper, triggerRepository, reviewService, noteService);

    @Test
    void createsPrePrReviewForOpenedMergeRequestOfConfiguredProject() {
        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(88L);
        when(configMapper.selectOne(any())).thenReturn(config);
        when(triggerRepository.tryReserve(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        ReviewVO review = new ReviewVO();
        review.setId(42L);
        ReviewDetailVO detail = new ReviewDetailVO();
        detail.setReview(review);
        when(reviewService.createPrePrReview(any())).thenReturn(detail);

        GitLabMergeRequestReviewTriggerResult result = trigger.trigger("Merge Request Hook", """
                {"object_kind":"merge_request","project":{"path_with_namespace":"platform/review-agent"},
                 "object_attributes":{"action":"open","state":"opened","iid":17,"last_commit":{"id":"abc123"},
                 "source_branch":"feature/quality","target_branch":"main"}}
                """);

        ArgumentCaptor<CreatePrePrRequest> request = ArgumentCaptor.forClass(CreatePrePrRequest.class);
        verify(reviewService).createPrePrReview(request.capture());
        assertEquals(88L, request.getValue().getProjectId());
        assertEquals("feature/quality", request.getValue().getSourceBranch());
        assertEquals("main", request.getValue().getTargetBranch());
        assertEquals("PROCESSED", result.status());
        assertEquals("platform/review-agent:mr:17:commit:abc123", result.triggerKey());
        assertEquals(42L, result.reviewId());
        assertNull(result.message());
        verify(triggerRepository).markProcessed(
                "gitlab-merge-request", "platform/review-agent:mr:17:commit:abc123", 42L);
    }

    @Test
    void skipsClosedMergeRequestWithoutCallingReviewService() {
        GitLabMergeRequestReviewTriggerResult result = trigger.trigger("Merge Request Hook", """
                {"object_kind":"merge_request","project":{"path_with_namespace":"platform/review-agent"},
                 "object_attributes":{"action":"close","state":"closed","source_branch":"feature/quality","target_branch":"main"}}
                """);

        assertEquals("SKIPPED", result.status());
        verifyNoInteractions(configMapper, triggerRepository, reviewService, noteService);
    }

    @Test
    void skipsOpenedMergeRequestWhenProjectIsNotConfigured() {
        when(configMapper.selectOne(any())).thenReturn(null);

        GitLabMergeRequestReviewTriggerResult result = trigger.trigger("Merge Request Hook", """
                {"object_kind":"merge_request","project":{"path_with_namespace":"platform/review-agent"},
                 "object_attributes":{"action":"reopen","state":"opened","source_branch":"feature/quality","target_branch":"main"}}
                """);

        assertEquals("SKIPPED", result.status());
        verifyNoInteractions(triggerRepository, reviewService, noteService);
    }

    @Test
    void deduplicatesAnExistingReviewForTheSameMergeRequestCommit() {
        ProjectGitLabConfig config = new ProjectGitLabConfig();
        config.setProjectId(88L);
        when(configMapper.selectOne(any())).thenReturn(config);
        when(triggerRepository.tryReserve(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);
        IntegrationWebhookReviewTrigger existing = new IntegrationWebhookReviewTrigger();
        existing.setTriggerStatus("PROCESSED");
        existing.setReviewId(42L);
        when(triggerRepository.find(any(), any())).thenReturn(Optional.of(existing));

        GitLabMergeRequestReviewTriggerResult result = trigger.trigger("Merge Request Hook", """
                {"object_kind":"merge_request","project":{"path_with_namespace":"platform/review-agent"},
                 "object_attributes":{"action":"update","state":"opened","iid":17,"last_commit":{"id":"abc123"},
                 "source_branch":"feature/quality","target_branch":"main"}}
                """);

        assertEquals("DEDUPLICATED", result.status());
        assertEquals("platform/review-agent:mr:17:commit:abc123", result.triggerKey());
        assertEquals(42L, result.reviewId());
        verify(reviewService, never()).createPrePrReview(any());
    }
}
