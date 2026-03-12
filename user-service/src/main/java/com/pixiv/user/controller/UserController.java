package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.ArtistDTO;
import com.pixiv.user.dto.UpdateProfileRequest;
import com.pixiv.user.dto.UserDTO;
import com.pixiv.user.entity.WalletTransaction;
import com.pixiv.user.entity.WithdrawalRequest;
import com.pixiv.user.service.FollowService;
import com.pixiv.user.service.UserService;
import com.pixiv.user.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final WalletService walletService;

    public UserController(UserService userService, FollowService followService, WalletService walletService) {
        this.userService = userService;
        this.followService = followService;
        this.walletService = walletService;
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

    /**
     * 更新画师设置
     * PUT /api/users/me/artist-settings
     *
     * 包括：接稿状态、擅长风格、价格区间、联系方式、简介
     */
    @PutMapping("/me/artist-settings")
    @Operation(summary = "更新画师设置", description = "更新当前画师的接稿设置、擅长风格、价格区间、联系方式等")
    public ResponseEntity<Result<ArtistDTO>> updateArtistSettings(
            Authentication authentication,
            @RequestBody java.util.Map<String, Object> settings) {
        Long userId = Long.parseLong(authentication.getName());
        logger.info("更新画师设置: userId={}", userId);

        ArtistDTO artistDTO = userService.updateArtistSettings(userId, settings);
        return ResponseEntity.ok(Result.success(artistDTO));
    }

    /**
     * 获取当前用户的画师设置
     * GET /api/users/me/artist-settings
     */
    @GetMapping("/me/artist-settings")
    @Operation(summary = "获取画师设置", description = "获取当前画师的详细设置信息")
    public ResponseEntity<Result<ArtistDTO>> getMyArtistSettings(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        logger.info("获取画师设置: userId={}", userId);

        ArtistDTO artistDTO = userService.getArtistByUserId(userId);
        return ResponseEntity.ok(Result.success(artistDTO));
    }

    // =========== 画师钱包接口 ===========

    /**
     * 获取钱包概览
     * GET /api/users/me/wallet
     */
    @GetMapping("/me/wallet")
    @Operation(summary = "获取钱包概览", description = "获取当前画师的钱包余额和收入统计")
    public ResponseEntity<Result<Map<String, Object>>> getWalletOverview(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        logger.info("获取钱包概览: userId={}", userId);

        Map<String, Object> overview = walletService.getWalletOverview(userId);
        return ResponseEntity.ok(Result.success(overview));
    }

    /**
     * 获取钱包交易记录
     * GET /api/users/me/wallet/transactions
     */
    @GetMapping("/me/wallet/transactions")
    @Operation(summary = "获取交易记录", description = "获取画师钱包的交易明细")
    public ResponseEntity<Result<Page<WalletTransaction>>> getWalletTransactions(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long userId = Long.parseLong(authentication.getName());
        logger.info("获取交易记录: userId={}, page={}, size={}", userId, page, size);

        Page<WalletTransaction> transactions = walletService.getTransactions(userId, page, size);
        return ResponseEntity.ok(Result.success(transactions));
    }

    /**
     * 画师收入入账（内部服务调用）
     * POST /api/users/wallet/income
     */
    @PostMapping("/wallet/income")
    @Operation(summary = "收入入账（内部）", description = "约稿完成时，commission-service 调用此接口入账画师收入")
    public ResponseEntity<Result<String>> addWalletIncome(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        Long commissionId = body.get("commissionId") != null ? Long.valueOf(body.get("commissionId").toString()) : null;
        String orderNo = body.get("orderNo") != null ? body.get("orderNo").toString() : null;
        String description = body.get("description") != null ? body.get("description").toString() : "约稿收入";

        logger.info("画师收入入账: userId={}, amount={}, commissionId={}", userId, amount, commissionId);

        walletService.addIncome(userId, amount, commissionId, orderNo, description);
        return ResponseEntity.ok(Result.success("入账成功"));
    }

    /**
     * 冻结金额（内部服务调用）
     * POST /api/users/wallet/freeze
     */
    @PostMapping("/wallet/freeze")
    @Operation(summary = "冻结金额（内部）", description = "定金支付成功时，commission-service 调用此接口冻结画师金额")
    public ResponseEntity<Result<String>> freezeWalletAmount(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        Long commissionId = body.get("commissionId") != null ? Long.valueOf(body.get("commissionId").toString()) : null;
        String description = body.get("description") != null ? body.get("description").toString() : "约稿定金冻结";

        logger.info("冻结金额: userId={}, amount={}, commissionId={}", userId, amount, commissionId);
        walletService.freezeAmount(userId, amount, commissionId, description);
        return ResponseEntity.ok(Result.success("冻结成功"));
    }

    /**
     * 解冻并释放金额（内部服务调用）
     * POST /api/users/wallet/unfreeze
     */
    @PostMapping("/wallet/unfreeze")
    @Operation(summary = "解冻并释放（内部）", description = "约稿完成时，commission-service 调用此接口解冻并转入可用余额")
    public ResponseEntity<Result<String>> unfreezeWalletAmount(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        Long commissionId = body.get("commissionId") != null ? Long.valueOf(body.get("commissionId").toString()) : null;
        String description = body.get("description") != null ? body.get("description").toString() : "约稿完成解冻";

        logger.info("解冻金额: userId={}, amount={}, commissionId={}", userId, amount, commissionId);
        walletService.unfreezeAndRelease(userId, amount, commissionId, description);
        return ResponseEntity.ok(Result.success("解冻成功"));
    }

    /**
     * 取消冻结（退款时内部调用）
     * POST /api/users/wallet/cancel-freeze
     */
    @PostMapping("/wallet/cancel-freeze")
    @Operation(summary = "取消冻结（退款用）", description = "约稿退款时，取消冻结金额（不入可用余额，资金退还给委托方）")
    public ResponseEntity<Result<String>> cancelWalletFreeze(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        Long commissionId = body.get("commissionId") != null ? Long.valueOf(body.get("commissionId").toString()) : null;
        String description = body.get("description") != null ? body.get("description").toString() : "约稿退款解冻";

        logger.info("取消冻结(退款): userId={}, amount={}, commissionId={}", userId, amount, commissionId);
        walletService.cancelFreeze(userId, amount, commissionId, description);
        return ResponseEntity.ok(Result.success("取消冻结成功"));
    }

    /**
     * 画师提现
     * POST /api/users/me/wallet/withdraw
     */
    @PostMapping("/me/wallet/withdraw")
    @Operation(summary = "画师提现", description = "画师将可用余额提现到支付宝账户，需管理员审批")
    public ResponseEntity<Result<String>> withdraw(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(authentication.getName());
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        String alipayAccount = body.get("alipayAccount") != null ? body.get("alipayAccount").toString() : null;
        String alipayName = body.get("alipayName") != null ? body.get("alipayName").toString() : null;

        logger.info("画师提现: userId={}, amount={}", userId, amount);
        try {
            walletService.withdraw(userId, amount, alipayAccount, alipayName);
            return ResponseEntity.ok(Result.success("提现申请已提交，请等待管理员审批"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }

    /**
     * 获取我的提现记录
     * GET /api/users/me/wallet/withdrawals
     */
    @GetMapping("/me/wallet/withdrawals")
    @Operation(summary = "我的提现记录", description = "获取当前画师的提现申请记录")
    public ResponseEntity<Result<Page<WithdrawalRequest>>> getMyWithdrawals(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(Result.success(walletService.getMyWithdrawals(userId, page, size)));
    }

    // =========== 提现管理（管理端） ===========

    /**
     * 获取提现申请列表（管理端）
     * GET /api/users/admin/withdrawals
     */
    @GetMapping("/admin/withdrawals")
    @Operation(summary = "提现申请列表（管理端）", description = "获取所有提现申请，支持按状态筛选")
    public ResponseEntity<Result<Page<WithdrawalRequest>>> getWithdrawals(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(Result.success(walletService.getWithdrawals(status, page, size)));
    }

    /**
     * 审批通过提现（管理端）
     * PUT /api/users/admin/withdrawals/{id}/approve
     */
    @PutMapping("/admin/withdrawals/{id}/approve")
    @Operation(summary = "审批通过提现（管理端）")
    public ResponseEntity<Result<String>> approveWithdrawal(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String adminIdStr) {
        Long adminId = adminIdStr != null ? Long.parseLong(adminIdStr) : 0L;
        try {
            walletService.approveWithdrawal(id, adminId);
            return ResponseEntity.ok(Result.success("审批通过"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }

    /**
     * 拒绝提现（管理端）
     * PUT /api/users/admin/withdrawals/{id}/reject
     */
    @PutMapping("/admin/withdrawals/{id}/reject")
    @Operation(summary = "拒绝提现（管理端）")
    public ResponseEntity<Result<String>> rejectWithdrawal(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String adminIdStr,
            @RequestBody(required = false) Map<String, Object> body) {
        Long adminId = adminIdStr != null ? Long.parseLong(adminIdStr) : 0L;
        String remark = body != null && body.get("remark") != null ? body.get("remark").toString() : null;
        try {
            walletService.rejectWithdrawal(id, adminId, remark);
            return ResponseEntity.ok(Result.success("已拒绝"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }

    /**
     * 获取待审批提现数量（管理端）
     * GET /api/users/admin/withdrawals/pending-count
     */
    @GetMapping("/admin/withdrawals/pending-count")
    @Operation(summary = "待审批提现数量")
    public ResponseEntity<Result<Long>> getPendingWithdrawalCount() {
        return ResponseEntity.ok(Result.success(walletService.getPendingWithdrawalCount()));
    }
}
