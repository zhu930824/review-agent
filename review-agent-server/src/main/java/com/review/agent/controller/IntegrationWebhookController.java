package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryLogVO;
import com.review.agent.domain.dto.IntegrationWebhookDeliveryResultVO;
import com.review.agent.domain.dto.GitLabReviewTriggerRetryRequest;
import com.review.agent.domain.dto.GitLabReviewTriggerHealthVO;
import com.review.agent.domain.dto.JenkinsReviewTriggerHealthVO;
import com.review.agent.domain.dto.JenkinsReviewTriggerRequest;
import com.review.agent.domain.dto.JenkinsReviewTriggerResultVO;
import com.review.agent.domain.dto.JenkinsReviewTriggerRetryRequest;
import com.review.agent.domain.dto.PrePrGateVO;
import com.review.agent.infrastructure.webhook.GitLabMergeRequestReviewTriggerResult;
import com.review.agent.service.GitLabReviewTriggerRetryService;
import com.review.agent.service.GitLabReviewTriggerHealthService;
import com.review.agent.service.JenkinsReviewTriggerHealthService;
import com.review.agent.service.JenkinsReviewTriggerRetryService;
import com.review.agent.service.JenkinsReviewTriggerService;
import com.review.agent.service.IntegrationWebhookDeliveryLogService;
import com.review.agent.service.IntegrationWebhookDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/webhooks")
public class IntegrationWebhookController {

    private final IntegrationWebhookDeliveryService deliveryService;
    private final IntegrationWebhookDeliveryLogService deliveryLogService;
    private final GitLabReviewTriggerRetryService gitLabReviewTriggerRetryService;
    private final GitLabReviewTriggerHealthService gitLabReviewTriggerHealthService;
    private final JenkinsReviewTriggerService jenkinsReviewTriggerService;
    private final JenkinsReviewTriggerRetryService jenkinsReviewTriggerRetryService;
    private final JenkinsReviewTriggerHealthService jenkinsReviewTriggerHealthService;

    @Autowired
    public IntegrationWebhookController(
            IntegrationWebhookDeliveryService deliveryService,
            IntegrationWebhookDeliveryLogService deliveryLogService,
            GitLabReviewTriggerRetryService gitLabReviewTriggerRetryService,
            GitLabReviewTriggerHealthService gitLabReviewTriggerHealthService,
            JenkinsReviewTriggerService jenkinsReviewTriggerService,
            JenkinsReviewTriggerRetryService jenkinsReviewTriggerRetryService,
            JenkinsReviewTriggerHealthService jenkinsReviewTriggerHealthService) {
        this.deliveryService = deliveryService;
        this.deliveryLogService = deliveryLogService;
        this.gitLabReviewTriggerRetryService = gitLabReviewTriggerRetryService;
        this.gitLabReviewTriggerHealthService = gitLabReviewTriggerHealthService;
        this.jenkinsReviewTriggerService = jenkinsReviewTriggerService;
        this.jenkinsReviewTriggerRetryService = jenkinsReviewTriggerRetryService;
        this.jenkinsReviewTriggerHealthService = jenkinsReviewTriggerHealthService;
    }

    public IntegrationWebhookController(
            IntegrationWebhookDeliveryService deliveryService,
            IntegrationWebhookDeliveryLogService deliveryLogService,
            GitLabReviewTriggerRetryService gitLabReviewTriggerRetryService,
            GitLabReviewTriggerHealthService gitLabReviewTriggerHealthService) {
        this(deliveryService, deliveryLogService, gitLabReviewTriggerRetryService, gitLabReviewTriggerHealthService,
                null, null, null);
    }

    @GetMapping("/deliveries")
    public Result<List<IntegrationWebhookDeliveryLogVO>> listDeliveries(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.success(deliveryLogService.listRecent(limit));
    }

    @PostMapping("/gitlab/review-triggers/retry")
    public Result<GitLabMergeRequestReviewTriggerResult> retryGitLabReviewTrigger(
            @Valid @RequestBody GitLabReviewTriggerRetryRequest request) {
        return Result.success(gitLabReviewTriggerRetryService.retry(request.getTriggerKey()));
    }

    @GetMapping("/gitlab/review-triggers/health")
    public Result<GitLabReviewTriggerHealthVO> getGitLabReviewTriggerHealth() {
        return Result.success(gitLabReviewTriggerHealthService.getHealth());
    }

    @PostMapping("/jenkins/reviews")
    public Result<IntegrationWebhookDeliveryResultVO> receiveJenkinsReview(
            @RequestHeader(value = "X-Review-Agent-Connector", defaultValue = "jenkins-pipeline") String connectorKey,
            @RequestHeader(value = "X-Jenkins-Delivery", required = false) String deliveryId,
            @RequestHeader("X-Review-Agent-Token") String token,
            @Valid @RequestBody JenkinsReviewTriggerRequest request) {
        return Result.success(jenkinsReviewTriggerService.receive(connectorKey, deliveryId, token, request));
    }

    @GetMapping("/jenkins/reviews/{reviewId}/gate")
    public Result<PrePrGateVO> getJenkinsReviewGate(
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-Review-Agent-Connector", defaultValue = "jenkins-pipeline") String connectorKey,
            @RequestHeader("X-Review-Agent-Token") String token) {
        return Result.success(jenkinsReviewTriggerService.getGate(connectorKey, token, reviewId));
    }

    @PostMapping("/jenkins/review-triggers/retry")
    public Result<JenkinsReviewTriggerResultVO> retryJenkinsReviewTrigger(
            @Valid @RequestBody JenkinsReviewTriggerRetryRequest request) {
        return Result.success(jenkinsReviewTriggerRetryService.retry(
                request.getConnectorKey(), request.getTriggerKey()));
    }

    @GetMapping("/jenkins/review-triggers/health")
    public Result<JenkinsReviewTriggerHealthVO> getJenkinsReviewTriggerHealth(
            @RequestParam(value = "connectorKey", defaultValue = "jenkins-pipeline") String connectorKey) {
        return Result.success(jenkinsReviewTriggerHealthService.getHealth(connectorKey));
    }

    @PostMapping("/github")
    public Result<IntegrationWebhookDeliveryResultVO> receiveGitHub(
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {
        return Result.success(deliveryService.receiveGitHubDelivery(deliveryId, eventType, signature, payload));
    }

    @PostMapping("/gitlab")
    public Result<IntegrationWebhookDeliveryResultVO> receiveGitLab(
            @RequestHeader(value = "X-Gitlab-Delivery", required = false) String deliveryId,
            @RequestHeader(value = "X-Gitlab-Event", defaultValue = "unknown") String eventType,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestHeader(value = "X-GitLab-Token", required = false) String legacyToken,
            @RequestBody String payload) {
        String effectiveToken = token == null ? legacyToken : token;
        return Result.success(deliveryService.receiveGitLabDelivery(deliveryId, eventType, effectiveToken, payload));
    }
}
