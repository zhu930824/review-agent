package com.review.agent.infrastructure.auth;

import com.review.agent.domain.entity.Review;
import com.review.agent.infrastructure.persistence.ReviewMapper;
import com.review.agent.service.ProjectAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ProjectResourceInterceptor implements HandlerInterceptor {

    private static final String REVIEW_PREFIX = "/api/reviews/";

    private final ReviewMapper reviewMapper;
    private final ProjectAccessService projectAccessService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith(REVIEW_PREFIX)) {
            Long reviewId = leadingId(path.substring(REVIEW_PREFIX.length()));
            if (reviewId != null) {
                Review review = reviewMapper.selectById(reviewId);
                if (review != null) {
                    ProjectPermission permission = "GET".equalsIgnoreCase(request.getMethod())
                            ? ProjectPermission.VIEW
                            : ProjectPermission.REVIEW_EXECUTE;
                    projectAccessService.require(review.getProjectId(), permission);
                }
            }
        } else if ("/api/reviews".equals(path) && request.getParameter("projectId") != null) {
            projectAccessService.require(
                    Long.valueOf(request.getParameter("projectId")), ProjectPermission.VIEW);
        }
        return true;
    }

    private Long leadingId(String value) {
        String segment = value.contains("/") ? value.substring(0, value.indexOf('/')) : value;
        if (segment.isBlank() || !segment.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return Long.valueOf(segment);
    }
}
