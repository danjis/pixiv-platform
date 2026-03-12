package com.pixiv.commission.controller;

import com.pixiv.commission.entity.CouponType;
import com.pixiv.commission.service.CouponService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 * 提供优惠券的管理和用户操作接口
 */
@Tag(name = "优惠券管理", description = "优惠券创建、领取、使用等接口")
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private CouponService couponService;

    // =========== 用户端接口 ===========

    /**
     * 获取可领取的优惠券列表
     */
    @Operation(summary = "获取可领取的优惠券", description = "查看当前可领取的有效优惠券")
    @GetMapping("/available")
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getAvailableCoupons(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            PageResult<Map<String, Object>> result = couponService.getAvailableCoupons(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取优惠券列表失败", e);
            return ResponseEntity.ok(Result.error("获取优惠券列表失败"));
        }
    }

    /**
     * 领取优惠券
     */
    @Operation(summary = "领取优惠券")
    @PostMapping("/{couponId}/claim")
    public ResponseEntity<Result<Void>> claimCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("couponId") Long couponId) {
        try {
            couponService.claimCoupon(userId, couponId);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("领取优惠券失败: userId={}, couponId={}", userId, couponId, e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 通过优惠码领取
     */
    @Operation(summary = "通过优惠码领取优惠券")
    @PostMapping("/claim-by-code")
    public ResponseEntity<Result<Void>> claimByCode(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error("请输入优惠券代码"));
            }
            couponService.claimByCode(userId, code);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("通过优惠码领取失败: userId={}", userId, e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取我的优惠券
     */
    @Operation(summary = "获取我的优惠券列表")
    @GetMapping("/mine")
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getMyCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            PageResult<Map<String, Object>> result = couponService.getUserCoupons(userId, status, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取用户优惠券失败: userId={}", userId, e);
            return ResponseEntity.ok(Result.error("获取优惠券失败"));
        }
    }

    /**
     * 获取可用于某笔订单的优惠券
     */
    @Operation(summary = "获取可用于订单的优惠券")
    @GetMapping("/available-for-order")
    public ResponseEntity<Result<List<Map<String, Object>>>> getAvailableForOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("orderAmount") BigDecimal orderAmount) {
        try {
            List<Map<String, Object>> result = couponService.getAvailableForOrder(userId, orderAmount);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取可用优惠券失败: userId={}, amount={}", userId, orderAmount, e);
            return ResponseEntity.ok(Result.error("获取可用优惠券失败"));
        }
    }

    // =========== 管理端接口 ===========

    /**
     * 创建优惠券（管理端）
     */
    @Operation(summary = "创建优惠券（管理端）")
    @PostMapping("/admin/create")
    public ResponseEntity<Result<Map<String, Object>>> createCoupon(
            @RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String description = (String) body.get("description");
            CouponType type = CouponType.valueOf((String) body.get("type"));
            BigDecimal discountValue = new BigDecimal(body.get("discountValue").toString());
            BigDecimal minOrderAmount = body.get("minOrderAmount") != null
                    ? new BigDecimal(body.get("minOrderAmount").toString())
                    : BigDecimal.ZERO;
            BigDecimal maxDiscountAmount = body.get("maxDiscountAmount") != null
                    ? new BigDecimal(body.get("maxDiscountAmount").toString())
                    : null;
            Integer totalQuantity = body.get("totalQuantity") != null
                    ? Integer.parseInt(body.get("totalQuantity").toString())
                    : 100;
            LocalDateTime startTime = LocalDateTime.parse((String) body.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse((String) body.get("endTime"));
            String distributionType = body.get("distributionType") != null
                    ? (String) body.get("distributionType")
                    : "PUBLIC";

            var coupon = couponService.createCoupon(name, description, type, discountValue,
                    minOrderAmount, maxDiscountAmount, totalQuantity, startTime, endTime, distributionType);

            Map<String, Object> result = Map.of(
                    "id", coupon.getId(),
                    "code", coupon.getCode(),
                    "name", coupon.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(result));
        } catch (Exception e) {
            logger.error("创建优惠券失败", e);
            return ResponseEntity.ok(Result.error("创建优惠券失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有优惠券（管理端）
     */
    @Operation(summary = "获取所有优惠券（管理端）")
    @GetMapping("/admin/list")
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getAllCoupons(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            PageResult<Map<String, Object>> result = couponService.getAllCoupons(page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取优惠券列表失败", e);
            return ResponseEntity.ok(Result.error("获取优惠券列表失败"));
        }
    }

    /**
     * 切换优惠券状态（管理端）
     */
    @Operation(summary = "启用/停用优惠券（管理端）")
    @PutMapping("/admin/{couponId}/toggle")
    public ResponseEntity<Result<Void>> toggleStatus(@PathVariable("couponId") Long couponId) {
        try {
            couponService.toggleCouponStatus(couponId);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("切换优惠券状态失败: couponId={}", couponId, e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    // =========== 内部接口（供其他服务调用） ===========

    /**
     * 积分兑换创建用户优惠券（内部接口，由 user-service 调用）
     */
    @Operation(summary = "积分兑换优惠券（内部接口）")
    @PostMapping("/internal/create-for-user")
    public ResponseEntity<Result<Map<String, Object>>> createCouponForUser(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.parseLong(body.get("userId").toString());
            String couponName = (String) body.get("couponName");
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            int validDays = body.get("validDays") != null ? Integer.parseInt(body.get("validDays").toString()) : 30;

            Map<String, Object> result = couponService.createCouponForUser(userId, couponName, amount, validDays);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("积分兑换创建优惠券失败", e);
            return ResponseEntity.ok(Result.error("创建优惠券失败: " + e.getMessage()));
        }
    }
}
