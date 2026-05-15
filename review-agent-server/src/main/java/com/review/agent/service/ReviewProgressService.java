package com.review.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.domain.entity.Review;
import com.review.agent.domain.entity.ReviewFinding;
import com.review.agent.domain.entity.ReviewModelResult;
import com.review.agent.domain.enums.ReviewStatus;
import com.review.agent.infrastructure.persistence.ReviewFindingMapper;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.infrastructure.persistence.ReviewModelResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewProgressService {

    private final ObjectMapper objectMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewModelResultMapper reviewModelResultMapper;
    private final ReviewFindingMapper reviewFindingMapper;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long reviewId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(reviewId, emitter);

        emitter.onCompletion(() -> emitters.remove(reviewId));
        emitter.onTimeout(() -> emitters.remove(reviewId));
        emitter.onError(e -> emitters.remove(reviewId));

        sendCurrentProgress(reviewId, emitter);
        return emitter;
    }

    public void notifyAgentStarted(Long reviewId, String agentRole, String modelName) {
        AgentProgressEvent event = new AgentProgressEvent();
        event.setType("agent_started");
        event.setAgentRole(agentRole);
        event.setModelName(modelName);
        event.setStatus("RUNNING");
        broadcast(reviewId, event);
    }

    public void notifyAgentProgress(Long reviewId, String agentRole, String message) {
        AgentProgressEvent event = new AgentProgressEvent();
        event.setType("agent_progress");
        event.setAgentRole(agentRole);
        event.setMessage(message);
        broadcast(reviewId, event);
    }

    public void notifyAgentCompleted(Long reviewId, String agentRole, String modelName, int findingCount) {
        AgentProgressEvent event = new AgentProgressEvent();
        event.setType("agent_completed");
        event.setAgentRole(agentRole);
        event.setModelName(modelName);
        event.setStatus("COMPLETED");
        event.setFindingCount(findingCount);
        broadcast(reviewId, event);
    }

    public void notifyAgentFailed(Long reviewId, String agentRole, String errorMessage) {
        AgentProgressEvent event = new AgentProgressEvent();
        event.setType("agent_failed");
        event.setAgentRole(agentRole);
        event.setStatus("FAILED");
        event.setMessage(errorMessage);
        broadcast(reviewId, event);
    }

    public void notifyReviewCompleted(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) return;

        List<ReviewModelResult> modelResults = reviewModelResultMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewModelResult>()
                        .eq(ReviewModelResult::getReviewId, reviewId));

        List<ReviewFinding> findings = reviewFindingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewFinding>()
                        .eq(ReviewFinding::getReviewId, reviewId));

        ReviewCompleteEvent event = new ReviewCompleteEvent();
        event.setType("review_completed");
        event.setStatus(review.getStatus().name());
        event.setTotalFindings(findings.size());
        event.setModelResults(modelResults);
        event.setSummary(review.getSummary());
        broadcast(reviewId, event);
    }

    private void sendCurrentProgress(Long reviewId, SseEmitter emitter) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) return;

        List<ReviewModelResult> modelResults = reviewModelResultMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReviewModelResult>()
                        .eq(ReviewModelResult::getReviewId, reviewId));

        InitialProgressEvent event = new InitialProgressEvent();
        event.setType("initial_state");
        event.setReviewStatus(review.getStatus().name());
        event.setModelResults(modelResults);
        broadcast(reviewId, event);
    }

    private void broadcast(Long reviewId, Object event) {
        SseEmitter emitter = emitters.get(reviewId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(objectMapper.writeValueAsString(event)));
        } catch (IOException e) {
            log.warn("SSE 发送失败: reviewId={}", reviewId, e);
            emitters.remove(reviewId);
        }
    }

    public static class AgentProgressEvent {
        private String type;
        private String agentRole;
        private String modelName;
        private String status;
        private String message;
        private Integer findingCount;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAgentRole() { return agentRole; }
        public void setAgentRole(String agentRole) { this.agentRole = agentRole; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getFindingCount() { return findingCount; }
        public void setFindingCount(Integer findingCount) { this.findingCount = findingCount; }
    }

    public static class ReviewCompleteEvent {
        private String type;
        private String status;
        private int totalFindings;
        private List<ReviewModelResult> modelResults;
        private String summary;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getTotalFindings() { return totalFindings; }
        public void setTotalFindings(int totalFindings) { this.totalFindings = totalFindings; }
        public List<ReviewModelResult> getModelResults() { return modelResults; }
        public void setModelResults(List<ReviewModelResult> modelResults) { this.modelResults = modelResults; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    public static class InitialProgressEvent {
        private String type;
        private String reviewStatus;
        private List<ReviewModelResult> modelResults;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getReviewStatus() { return reviewStatus; }
        public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
        public List<ReviewModelResult> getModelResults() { return modelResults; }
        public void setModelResults(List<ReviewModelResult> modelResults) { this.modelResults = modelResults; }
    }
}
