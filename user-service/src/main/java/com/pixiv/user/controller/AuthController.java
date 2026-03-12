package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.AuthResponse;
import com.pixiv.user.dto.EmailLoginRequest;
import com.pixiv.user.dto.LoginRequest;
import com.pixiv.user.dto.RefreshTokenRequest;
import com.pixiv.user.dto.RegisterRequest;
import com.pixiv.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册、登录、令牌刷新等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "用户注册、登录、令牌刷新等认证相关接口")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户并返回访问令牌")
    @SecurityRequirement(name = "") // 不需要认证
    public ResponseEntity<Result<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名或邮箱登录并返回访问令牌")
    @SecurityRequirement(name = "") // 不需要认证
    public ResponseEntity<Result<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request.getUsernameOrEmail(), request.getPassword());
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 邮箱验证码登录
     *
     * @param request 邮箱登录请求
     * @return 认证响应
     */
    @PostMapping("/login-by-email")
    @Operation(summary = "邮箱验证码登录", description = "使用邮箱和验证码登录并返回访问令牌")
    @SecurityRequirement(name = "") // 不需要认证
    public ResponseEntity<Result<AuthResponse>> loginByEmail(@Valid @RequestBody EmailLoginRequest request) {
        AuthResponse response = userService.loginByEmail(request.getEmail(), request.getEmailCode());
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 刷新访问令牌
     *
     * @param request 刷新令牌请求
     * @return 认证响应
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    @SecurityRequirement(name = "") // 不需要认证
    public ResponseEntity<Result<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = userService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 用户登出
     *
     * @param authentication 认证信息
     * @return 成功响应
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "撤销刷新令牌并登出")
    public ResponseEntity<Result<Void>> logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.logout(userId);
        return ResponseEntity.ok(Result.success("登出成功", null));
    }
}
