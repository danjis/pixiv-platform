package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.ArtistDTO;
import com.pixiv.user.dto.UpdateProfileRequest;
import com.pixiv.user.dto.UserDTO;
import com.pixiv.user.service.FollowService;
import com.pixiv.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 处理用户个人信息相关的 HTTP 请求
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户个人信息查询和更新相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final FollowService followService;

    public UserController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
    }

    /**
     * 获取当前用户信息
     * GET /api/users/me
     *
     * @param authentication Spring Security 认证对象
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    public ResponseEntity<Result<UserDTO>> getCurrentUser(Authentication authentication) {
        logger.info("获取当前用户信息");

        // 从认证对象中获取用户ID
        Long userId = Long.parseLong(authentication.getName());

        UserDTO userDTO = userService.getCurrentUser(userId);

        return ResponseEntity.ok(Result.success(userDTO));
    }

    /**
     * 更新当前用户个人信息
     * PUT /api/users/me
     *
     * @param authentication Spring Security 认证对象
     * @param request        更新个人信息请求
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    @Operation(summary = "更新个人信息", description = "更新当前用户的头像和个人简介")
    public ResponseEntity<Result<UserDTO>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        logger.info("更新当前用户个人信息");

        // 从认证对象中获取用户ID
        Long userId = Long.parseLong(authentication.getName());

        UserDTO userDTO = userService.updateProfile(
                userId,
                request.getAvatarUrl(),
                request.getBio());

        return ResponseEntity.ok(Result.success(userDTO));
    }

    /**
     * 获取指定用户的公开信息
     * GET /api/users/{userId}
     *
     * @param userId 用户ID
     * @return 用户公开信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户主页", description = "获取指定用户的公开信息")
    public ResponseEntity<Result<UserDTO>> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        logger.info("获取用户公开信息: userId={}", userId);

        UserDTO userDTO = userService.getCurrentUser(userId);

        return ResponseEntity.ok(Result.success(userDTO));
    }

    /**
     * 获取指定用户的画师信息（内部服务调用）
     * GET /api/users/{userId}/artist
     *
     * @param userId 用户ID
     * @return 画师信息
     */
    @GetMapping("/{userId}/artist")
    @Operation(summary = "获取画师信息", description = "获取指定用户的画师信息（内部服务调用）")
    public ResponseEntity<Result<ArtistDTO>> getArtistByUserId(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        logger.info("获取画师信息: userId={}", userId);

        ArtistDTO artistDTO = userService.getArtistByUserId(userId);

        return ResponseEntity.ok(Result.success(artistDTO));
    }

    /**
     * 获取用户关注的所有人的 ID 列表（内部服务调用）
     * GET /api/users/{userId}/following-ids
     *
     * @param userId 用户ID
     * @return 关注的用户 ID 列表
     */
    @GetMapping("/{userId}/following-ids")
    @Operation(summary = "获取用户关注ID列表", description = "获取指定用户关注的所有用户ID列表（内部服务调用）")
    public ResponseEntity<Result<List<Long>>> getFollowingIds(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        logger.info("获取用户关注ID列表: userId={}", userId);

        List<Long> followingIds = followService.getFollowingIds(userId);
        logger.info("用户关注ID列表: userId={}, count={}", userId, followingIds.size());

        return ResponseEntity.ok(Result.success(followingIds));
    }

    /**
     * 获取用户的所有粉丝 ID 列表（内部服务调用）
     * GET /api/users/{userId}/follower-ids
     *
     * @param userId 用户ID
     * @return 粉丝 ID 列表
     */
    @GetMapping("/{userId}/follower-ids")
    @Operation(summary = "获取用户粉丝ID列表", description = "获取指定用户的所有粉丝ID列表（内部服务调用，用于 Feed 推送）")
    public ResponseEntity<Result<List<Long>>> getFollowerIds(
            @Parameter(description = "用户ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        logger.info("获取用户粉丝ID列表: userId={}", userId);

        List<Long> followerIds = followService.getFollowerIds(userId);
        logger.info("用户粉丝ID列表: userId={}, count={}", userId, followerIds.size());

        return ResponseEntity.ok(Result.success(followerIds));
    }

    /**
     * 批量获取用户基本信息（内部服务调用）
     * POST /api/users/batch
     *
     * @param userIds 用户 ID 列表
     * @return 用户基本信息列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户基本信息（内部服务调用，解决 N+1 查询问题）")
    public ResponseEntity<Result<List<com.pixiv.common.dto.UserDTO>>> getUsersByIds(
            @RequestBody List<Long> userIds) {
        logger.info("批量获取用户基本信息: count={}", userIds != null ? userIds.size() : 0);
        List<com.pixiv.common.dto.UserDTO> users = userService.getUsersByIds(userIds);
        return ResponseEntity.ok(Result.success(users));
    }

    /**
     * 更新隐私设置
     * PUT /api/users/me/privacy
     */
    @PutMapping("/me/privacy")
    @Operation(summary = "更新隐私设置", description = "更新当前用户的隐私设置（隐藏关注/收藏列表）")
    public ResponseEntity<Result<UserDTO>> updatePrivacySettings(
            Authentication authentication,
            @RequestBody java.util.Map<String, Boolean> settings) {
        Long userId = Long.parseLong(authentication.getName());
        logger.info("更新隐私设置: userId={}, settings={}", userId, settings);

        UserDTO userDTO = userService.updatePrivacySettings(userId, settings);
        return ResponseEntity.ok(Result.success(userDTO));
    }
}
