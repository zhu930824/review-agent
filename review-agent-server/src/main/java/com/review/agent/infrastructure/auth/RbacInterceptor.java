package com.review.agent.infrastructure.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.review.agent.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RbacInterceptor implements HandlerInterceptor {

    private final AccessDecisionService accessDecisionService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AccessDecisionService.AccessIdentity identity = accessDecisionService.currentIdentity();
        if (identity == null || !identity.isActive()) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "账号不存在或已停用");
            return false;
        }

        PlatformPermission required = requiredPermission(request.getMethod(), request.getRequestURI());
        if (required != null && !identity.role().allows(required)) {
            write(response, HttpServletResponse.SC_FORBIDDEN, 403,
                    "当前角色 " + identity.role().name() + " 无权执行该操作");
            return false;
        }
        return true;
    }

    private PlatformPermission requiredPermission(String method, String path) {
        boolean read = "GET".equalsIgnoreCase(method);
        if (matches(path, "/api/access/**")) {
            return PlatformPermission.ACCESS_MANAGE;
        }
        if (matches(path, "/api/integration/ci-config/credential-rotation")) {
            return PlatformPermission.CREDENTIAL_ROTATE;
        }
        if (matches(path, "/api/integration/ci-config/**") || matches(path, "/api/integration/ci-config")) {
            return read ? PlatformPermission.INTEGRATION_VIEW : PlatformPermission.INTEGRATION_MANAGE;
        }
        if (matches(path, "/api/integration/actions/**")
                || matches(path, "/api/integration/webhooks/deliveries")
                || matches(path, "/api/integration/webhooks/gitlab/review-triggers/health")
                || matches(path, "/api/integration/webhooks/jenkins/review-triggers/health")) {
            return PlatformPermission.INTEGRATION_VIEW;
        }
        if (matches(path, "/api/integration/webhooks/gitlab/review-triggers/retry")) {
            return PlatformPermission.INTEGRATION_MANAGE;
        }
        if (matches(path, "/api/integration/webhooks/jenkins/review-triggers/retry")) {
            return PlatformPermission.INTEGRATION_MANAGE;
        }
        if (matches(path, "/api/governance/**")) {
            if (read || path.endsWith("/dry-run")) {
                return PlatformPermission.GOVERNANCE_VIEW;
            }
            return PlatformPermission.GOVERNANCE_MANAGE;
        }
        if (matches(path, "/api/model-config/**")) {
            return read ? PlatformPermission.MODEL_VIEW : PlatformPermission.MODEL_MANAGE;
        }
        if (matches(path, "/api/model-telemetry/**")) {
            return read ? PlatformPermission.MODEL_VIEW : PlatformPermission.MODEL_MANAGE;
        }
        if (matches(path, "/api/operations/**")) {
            return read ? PlatformPermission.OPERATIONS_VIEW : PlatformPermission.OPERATIONS_MANAGE;
        }
        if ("PUT".equalsIgnoreCase(method) && matches(path, "/api/projects/*/gitlab-review-policy")) {
            return PlatformPermission.INTEGRATION_MANAGE;
        }
        return null;
    }

    private boolean matches(String path, String pattern) {
        return pathMatcher.match(pattern, path);
    }

    private void write(HttpServletResponse response, int httpStatus, int code, String message) throws Exception {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
