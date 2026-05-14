package com.review.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.review.agent.domain.dto.AuthRequest;
import com.review.agent.domain.dto.AuthTokenVO;
import com.review.agent.domain.dto.RegisterRequest;
import com.review.agent.domain.dto.UserVO;
import com.review.agent.domain.dto.convert.UserConverter;
import com.review.agent.domain.entity.UserAccount;
import com.review.agent.domain.exception.BusinessException;
import com.review.agent.infrastructure.auth.JwtService;
import com.review.agent.infrastructure.persistence.UserAccountMapper;
import com.review.agent.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DEFAULT_ROLE = "管理员";

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthTokenVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        assertUsernameAvailable(username);

        if (StringUtils.hasText(request.getEmail())) {
            assertEmailAvailable(request.getEmail().trim());
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName().trim());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        user.setRole(DEFAULT_ROLE);
        user.setStatus(ACTIVE);
        userAccountMapper.insert(user);
        return buildToken(user);
    }

    @Override
    public AuthTokenVO login(AuthRequest request) {
        UserAccount user = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, request.getUsername().trim()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "用户名或密码错误");
        }
        if (!ACTIVE.equals(user.getStatus())) {
            throw new BusinessException("AUTH_USER_DISABLED", "账号已停用");
        }
        return buildToken(user);
    }

    private void assertUsernameAvailable(String username) {
        Long count = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException("AUTH_USERNAME_EXISTS", "用户名已存在");
        }
    }

    private void assertEmailAvailable(String email) {
        Long count = userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getEmail, email));
        if (count != null && count > 0) {
            throw new BusinessException("AUTH_EMAIL_EXISTS", "邮箱已存在");
        }
    }

    private AuthTokenVO buildToken(UserAccount user) {
        UserVO userVO = UserConverter.toVO(user);
        JwtService.TokenResult token = jwtService.createToken(user.getId(), user.getUsername());
        return AuthTokenVO.builder()
                .token(token.token())
                .tokenType("Bearer")
                .expiresAt(token.expiresAt())
                .user(userVO)
                .build();
    }
}
