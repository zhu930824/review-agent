package com.review.agent.controller;

import com.review.agent.common.result.Result;
import com.review.agent.domain.dto.AuthRequest;
import com.review.agent.domain.dto.AuthTokenVO;
import com.review.agent.domain.dto.RegisterRequest;
import com.review.agent.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthTokenVO> register(@Validated @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthTokenVO> login(@Validated @RequestBody AuthRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
