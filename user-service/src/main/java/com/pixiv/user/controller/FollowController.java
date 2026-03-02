package com.pixiv.user.controller;

import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.FollowStatusDTO;
import com.pixiv.user.dto.UserDTO;
import com.pixiv.user.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注控制器
 * 处理用户关注相关的 HTTP 请求
 */
@RestController
@RequestMapping("/api/follows")
@Tag(name = "关注接口", description = "用户关注、取消关注、查询关注列表等相关接口")
public class FollowController {

    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * 关注用户/画师
     * POST /api/follows/{userId}
     *
     * @param userId         要关注的用户ID
     * @param authentication Spring Security 认证对象
     * @return 操作结果
     */
    @PostMapping("/{userId}")
    @Operation(summary = "关注用户", description = "关注指定的用户或画师")
    public ResponseEntity<Result<Void>> followUser(
            @Parameter(description = "要关注的用户ID", required = true) @PathVariable("userId") Long userId,
            Authentication authentication) {
        logger.info("关注用户: userId={}", userId);

        // 从认证对象中获取当前用户ID
        Long currentUserId = Long.parseLong(authentication.getName());

        followService.followUser(currentUserId, userId);

        return ResponseEntity.ok(Result.success("关注成功", null));
    }

    /**
     * 取消关注用户/画师
     * DELETE /api/follows/{userId}
     *
     * @param userId         要取消关注的用户ID
     * @param authentication Spring Security 认证对象
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "取消关注", description = "取消关注指定的用户或画师")
    public ResponseEntity<Result<Void>> unfollowUser(
            @Parameter(description = "要取消关注的用户ID", required = true) @PathVariable("userId") Long userId,
            Authentication authentication) {
        logger.info("取消关注用户: userId={}", userId);

        // 从认证对象中获取当前用户ID
        Long currentUserId = Long.parseLong(authentication.getName());

        followService.unfollowUser(currentUserId, userId);

        return ResponseEntity.ok(Result.success("取消关注成功", null));
    }

    /**
     * 获取我关注的用户列表
     * GET /api/follows/following
     *
     * @param page           页码（从0开始）
     * @param size           每页大小
     * @param authentication Spring Security 认证对象
     * @return 关注的用户列表
     */
    @GetMapping("/following")
    @Operation(summary = "获取我的关注列表", description = "获取当前用户关注的所有用户")
    public ResponseEntity<Result<PageResult<UserDTO>>> getFollowing(
            @Parameter(description = "页码，从1开始") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {
        logger.info("获取关注列表: page={}, size={}", page, size);
        logger.info("Authentication: {}", authentication);
        logger.info("Principal: {}", authentication.getPrincipal());
        logger.info("Name: {}", authentication.getName());

        // 从认证对象中获取当前用户ID
        Long currentUserId = (Long) authentication.getPrincipal();
        logger.info("Current User ID: {}", currentUserId);

        // 前端页码从1开始，转换为Spring Data的页码（从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserDTO> followingPage = followService.getFollowingWithStats(currentUserId, pageable);

        PageResult<UserDTO> pageResult = new PageResult<>(
                followingPage.getContent(),
                followingPage.getTotalElements(),
                page, // 返回前端传入的页码
                followingPage.getSize());

        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取我的粉丝列表
     * GET /api/follows/followers
     *
     * @param page           页码（从0开始）
     * @param size           每页大小
     * @param authentication Spring Security 认证对象
     * @return 粉丝列表
     */
    @GetMapping("/followers")
    @Operation(summary = "获取我的粉丝列表", description = "获取关注当前用户的所有粉丝")
    public ResponseEntity<Result<PageResult<UserDTO>>> getFollowers(
            @Parameter(description = "页码，从1开始") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {
        logger.info("获取粉丝列表: page={}, size={}", page, size);

        // 从认证对象中获取当前用户ID
        Long currentUserId = (Long) authentication.getPrincipal();

        // 前端页码从1开始，转换为Spring Data的页码（从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserDTO> followersPage = followService.getFollowersWithStats(currentUserId, pageable);

        PageResult<UserDTO> pageResult = new PageResult<>(
                followersPage.getContent(),
                followersPage.getTotalElements(),
                page, // 返回前端传入的页码
                followersPage.getSize());

        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 检查是否关注某个用户
     * GET /api/follows/{userId}/status
     *
     * @param userId         要检查的用户ID
     * @param authentication Spring Security 认证对象
     * @return 关注状态
     */
    @GetMapping("/{userId}/status")
    @Operation(summary = "检查关注状态", description = "检查当前用户是否关注了指定用户")
    public ResponseEntity<Result<FollowStatusDTO>> checkFollowStatus(
            @Parameter(description = "要检查的用户ID", required = true) @PathVariable("userId") Long userId,
            Authentication authentication) {
        logger.info("检查关注状态: userId={}", userId);

        // 从认证对象中获取当前用户ID
        Long currentUserId = Long.parseLong(authentication.getName());

        boolean isFollowing = followService.checkFollowStatus(currentUserId, userId);
        FollowStatusDTO statusDTO = new FollowStatusDTO(isFollowing);

        return ResponseEntity.ok(Result.success(statusDTO));
    }

    /**
     * 获取指定用户的关注列表（公开接口）
     * GET /api/follows/{userId}/following
     *
     * @param userId 用户ID
     * @param page   页码（从0开始）
     * @param size   每页大小
     * @return 关注的用户列表
     */
    @GetMapping("/{userId}/following")
    @Operation(summary = "获取用户的关注列表", description = "获取指定用户关注的所有用户（公开接口）")
    public ResponseEntity<Result<PageResult<UserDTO>>> getUserFollowing(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "页码，从1开始") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "20") int size) {
        logger.info("获取用户关注列表: userId={}, page={}, size={}", userId, page, size);

        // 隐私检查：如果用户隐藏了关注列表，返回空结果
        if (followService.isFollowingHidden(userId)) {
            PageResult<UserDTO> emptyResult = new PageResult<>(List.of(), 0L, page, size);
            return ResponseEntity.ok(Result.success(emptyResult));
        }

        // 前端页码从1开始，转换为Spring Data的页码（从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserDTO> followingPage = followService.getFollowingWithStats(userId, pageable);

        PageResult<UserDTO> pageResult = new PageResult<>(
                followingPage.getContent(),
                followingPage.getTotalElements(),
                page,
                followingPage.getSize());

        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取指定用户的粉丝列表（公开接口）
     * GET /api/follows/{userId}/followers
     *
     * @param userId 用户ID
     * @param page   页码（从0开始）
     * @param size   每页大小
     * @return 粉丝列表
     */
    @GetMapping("/{userId}/followers")
    @Operation(summary = "获取用户的粉丝列表", description = "获取关注指定用户的所有粉丝（公开接口）")
    public ResponseEntity<Result<PageResult<UserDTO>>> getUserFollowers(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "页码，从1开始") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(name = "size", defaultValue = "20") int size) {
        logger.info("获取用户粉丝列表: userId={}, page={}, size={}", userId, page, size);

        // 前端页码从1开始，转换为Spring Data的页码（从0开始）
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserDTO> followersPage = followService.getFollowersWithStats(userId, pageable);

        PageResult<UserDTO> pageResult = new PageResult<>(
                followersPage.getContent(),
                followersPage.getTotalElements(),
                page,
                followersPage.getSize());

        return ResponseEntity.ok(Result.success(pageResult));
    }
}
