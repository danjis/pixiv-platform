package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.entity.Admin;
import com.pixiv.user.repository.AdminRepository;
import com.pixiv.user.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 * 独立于普通用户认证，使用 admins 表
 */
@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "管理员认证", description = "管理员登录接口")
public class AdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuthController(AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用管理员账号密码登录，返回 JWT token")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Result.error("用户名和密码不能为空"));
        }

        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin == null || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            logger.warn("管理员登录失败: username={}", username);
            return ResponseEntity.badRequest().body(Result.error("用户名或密码错误"));
        }

        // 生成 JWT，role 设为 ADMIN
        String accessToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getUsername(), "ADMIN");

        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        data.put("admin", adminInfo);

        logger.info("管理员登录成功: username={}", username);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前管理员信息")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentAdmin(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        Admin admin = adminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body(Result.error("管理员不存在"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("createdAt", admin.getCreatedAt());

        return ResponseEntity.ok(Result.success(data));
    }
}
