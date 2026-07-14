package com.review.agent.common.config;

import com.review.agent.infrastructure.auth.AuthInterceptor;
import com.review.agent.infrastructure.auth.RbacInterceptor;
import com.review.agent.infrastructure.auth.ProjectResourceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuthWebMvcConfiguration implements WebMvcConfigurer {

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/integration/webhooks/github",
            "/api/integration/webhooks/gitlab",
            "/api/integration/webhooks/jenkins/reviews",
            "/api/integration/webhooks/jenkins/reviews/*/gate",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private final AuthInterceptor authInterceptor;
    private final RbacInterceptor rbacInterceptor;
    private final ProjectResourceInterceptor projectResourceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS)
                .order(0);
        registry.addInterceptor(rbacInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS)
                .order(1);
        registry.addInterceptor(projectResourceInterceptor)
                .addPathPatterns("/api/projects/**", "/api/reviews/**", "/api/reviews")
                .order(2);
    }
}
