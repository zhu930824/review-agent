package com.review.agent.infrastructure.auth;

import com.review.agent.domain.entity.UserAccount;
import com.review.agent.infrastructure.persistence.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessDecisionService {

    private final UserAccountMapper userAccountMapper;

    public AccessIdentity currentIdentity() {
        JwtClaims claims = AuthContext.get();
        if (claims == null || claims.userId() == null) {
            return null;
        }
        UserAccount user = userAccountMapper.selectById(claims.userId());
        if (user == null) {
            return null;
        }
        return new AccessIdentity(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                PlatformRole.fromStoredRole(user.getRole()));
    }

    public record AccessIdentity(
            Long userId,
            String username,
            String displayName,
            String storedRole,
            String status,
            PlatformRole role) {

        public boolean isActive() {
            return "ACTIVE".equalsIgnoreCase(status);
        }
    }
}
