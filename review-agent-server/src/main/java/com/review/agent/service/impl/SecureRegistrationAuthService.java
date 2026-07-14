package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.AuthRequest;
import com.review.agent.domain.dto.AuthTokenVO;
import com.review.agent.domain.dto.RegisterRequest;
import com.review.agent.domain.entity.UserAccount;
import com.review.agent.infrastructure.auth.PlatformRole;
import com.review.agent.infrastructure.persistence.UserAccountMapper;
import com.review.agent.service.AuthService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Service
public class SecureRegistrationAuthService implements AuthService {

    private final AuthService delegate;
    private final UserAccountMapper userAccountMapper;

    public SecureRegistrationAuthService(
            @Qualifier("authServiceImpl") AuthService delegate,
            UserAccountMapper userAccountMapper) {
        this.delegate = delegate;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    @Transactional
    public synchronized AuthTokenVO register(RegisterRequest request) {
        boolean firstAccount = userAccountMapper.selectCount(new LambdaQueryWrapper<>()) == 0;
        AuthTokenVO token = delegate.register(request);
        PlatformRole assignedRole = firstAccount ? PlatformRole.ADMIN : PlatformRole.REVIEWER;

        UserAccount update = new UserAccount();
        update.setId(token.getUser().getId());
        update.setRole(assignedRole.name());
        userAccountMapper.updateById(update);
        token.getUser().setRole(assignedRole.name());
        return token;
    }

    @Override
    public AuthTokenVO login(AuthRequest request) {
        return delegate.login(request);
    }
}
