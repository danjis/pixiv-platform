package com.pixiv.user.controller;

import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.ArtistApplicationDTO;
import com.pixiv.user.dto.ReviewApplicationRequest;
import com.pixiv.user.entity.*;
import com.pixiv.user.repository.AuditLogRepository;
import com.pixiv.user.repository.UserRepository;
import com.pixiv.user.repository.ArtistRepository;
import com.pixiv.user.repository.AdminRepository;
import com.pixiv.user.service.AdminService;
import com.pixiv.user.service.ArtistApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员相关的请求：画师审核、用户管理、审计日志、数据统计
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员", description = "管理员相关接口")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final ArtistApplicationService artistApplicationService;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final AuditLogRepository auditLogRepository;
    private final AdminRepository adminRepository;

    public AdminController(AdminService adminService,
            ArtistApplicationService artistApplicationService,
            UserRepository userRepository,
            ArtistRepository artistRepository,
            AuditLogRepository auditLogRepository,
            AdminRepository adminRepository) {
        this.adminService = adminService;
        this.artistApplicationService = artistApplicationService;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.auditLogRepository = auditLogRepository;
        this.adminRepository = adminRepository;
    }

    // ==================== 权限校验辅助 ====================
    private boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }

    // ==================== 画师申请审核 ====================

    @GetMapping("/applications/pending")
    @Operation(summary = "获取待审核申请列表")
    public ResponseEntity<Result<PageResult<ArtistApplicationDTO>>> getPendingApplications(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<ArtistApplicationDTO> applications = artistApplicationService.getPendingApplications(pageable);
        PageResult<ArtistApplicationDTO> result = new PageResult<>(
                applications.getContent(), applications.getTotalElements(),
                applications.getNumber() + 1, applications.getSize());
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/applications")
    @Operation(summary = "获取申请列表", description = "支持按状态筛选")
    public ResponseEntity<Result<PageResult<ArtistApplicationDTO>>> getApplications(
            @RequestParam(value = "status", required = false) ApplicationStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<ArtistApplicationDTO> applications = artistApplicationService.getApplications(status, pageable);
        PageResult<ArtistApplicationDTO> result = new PageResult<>(
                applications.getContent(), applications.getTotalElements(),
                applications.getNumber() + 1, applications.getSize());
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/applications/{applicationId}")
    @Operation(summary = "获取申请详情")
    public ResponseEntity<Result<ArtistApplicationDTO>> getApplication(
            @PathVariable("applicationId") Long applicationId) {
        ArtistApplicationDTO application = artistApplicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(Result.success(application));
    }

    @PostMapping("/applications/{applicationId}/review")
    @Operation(summary = "审核画师申请")
    public ResponseEntity<Result<ArtistApplicationDTO>> reviewApplication(
            @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody ReviewApplicationRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        ArtistApplicationDTO result = adminService.reviewApplication(
                adminId, applicationId, request.getApproved(), request.getReviewComment());
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/applications/pending/count")
    @Operation(summary = "获取待审核数量")
    public ResponseEntity<Result<Long>> getPendingCount() {
        long count = artistApplicationService.countPendingApplications();
        return ResponseEntity.ok(Result.success(count));
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "分页获取用户列表，支持搜索和角色筛选")
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role", required = false) String adminRole) {

        if (!isAdmin(adminRole)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        } else if (role != null && !role.isBlank()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                users = userRepository.findByRole(userRole, pageable);
            } catch (IllegalArgumentException e) {
                users = userRepository.findAll(pageable);
            }
        } else {
            users = userRepository.findAll(pageable);
        }

        List<Map<String, Object>> records = users.getContent().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("avatarUrl", u.getAvatarUrl());
            m.put("role", u.getRole().name());
            m.put("bio", u.getBio());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();

        PageResult<Map<String, Object>> result = new PageResult<>(
                records, users.getTotalElements(), users.getNumber() + 1, users.getSize());
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "用户详情")
    public ResponseEntity<Result<Map<String, Object>>> getUserDetail(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String adminRole) {

        if (!isAdmin(adminRole)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Result.error("用户不存在"));
        }

        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("email", user.getEmail());
        m.put("avatarUrl", user.getAvatarUrl());
        m.put("role", user.getRole().name());
        m.put("bio", user.getBio());
        m.put("createdAt", user.getCreatedAt());
        m.put("updatedAt", user.getUpdatedAt());
        m.put("hideFollowing", user.getHideFollowing());
        m.put("hideFavorites", user.getHideFavorites());

        // 如果是画师，附带画师信息
        if (user.getRole() == UserRole.ARTIST) {
            artistRepository.findByUserId(userId).ifPresent(artist -> {
                Map<String, Object> artistInfo = new HashMap<>();
                artistInfo.put("portfolioUrl", artist.getPortfolioUrl());
                artistInfo.put("description", artist.getDescription());
                artistInfo.put("acceptingCommissions", artist.getAcceptingCommissions());
                artistInfo.put("followerCount", artist.getFollowerCount());
                artistInfo.put("artworkCount", artist.getArtworkCount());
                m.put("artist", artistInfo);
            });
        }

        return ResponseEntity.ok(Result.success(m));
    }

    // ==================== 审计日志 ====================

    @GetMapping("/audit-logs")
    @Operation(summary = "审计日志", description = "分页获取审计日志，支持按类型筛选")
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getAuditLogs(
            @RequestParam(value = "actionType", required = false) String actionType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestHeader(value = "X-User-Role", required = false) String adminRole) {

        if (!isAdmin(adminRole)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logs;
        if (actionType != null && !actionType.isBlank()) {
            try {
                ActionType type = ActionType.valueOf(actionType);
                logs = auditLogRepository.findByActionType(type, pageable);
            } catch (IllegalArgumentException e) {
                logs = auditLogRepository.findAll(pageable);
            }
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        List<Map<String, Object>> records = logs.getContent().stream().map(log -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", log.getId());
            m.put("adminId", log.getAdminId());
            m.put("actionType", log.getActionType().name());
            m.put("description", log.getDescription());
            m.put("targetType", log.getTargetType());
            m.put("targetId", log.getTargetId());
            m.put("createdAt", log.getCreatedAt());
            // 附带管理员用户名
            adminRepository.findById(log.getAdminId()).ifPresent(admin -> m.put("adminUsername", admin.getUsername()));
            return m;
        }).toList();

        PageResult<Map<String, Object>> result = new PageResult<>(
                records, logs.getTotalElements(), logs.getNumber() + 1, logs.getSize());
        return ResponseEntity.ok(Result.success(result));
    }

    // ==================== 数据统计 ====================

    @GetMapping("/stats")
    @Operation(summary = "平台数据统计", description = "返回用户数、画师数、待审核数等基础统计")
    public ResponseEntity<Result<Map<String, Object>>> getStats(
            @RequestHeader(value = "X-User-Role", required = false) String adminRole) {

        if (!isAdmin(adminRole)) {
            return ResponseEntity.status(403).body(Result.error("无管理员权限"));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalArtists", artistRepository.count());
        stats.put("pendingApplications", artistApplicationService.countPendingApplications());
        stats.put("totalAuditLogs", auditLogRepository.count());

        return ResponseEntity.ok(Result.success(stats));
    }
}
