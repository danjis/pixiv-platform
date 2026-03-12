package com.pixiv.commission.controller;

import com.pixiv.commission.dto.*;
import com.pixiv.commission.entity.CommissionStatus;
import com.pixiv.commission.service.CommissionService;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 约稿控制器
 *
 * 新流程（用户发起）:
 * 用户提交请求(PENDING) → 画师报价(QUOTED) → 用户支付定金(DEPOSIT_PAID)
 * → 画师创作(IN_PROGRESS) → 画师交付(DELIVERED) → 用户确认并支付尾款(COMPLETED)
 */
@Tag(name = "约稿管理", description = "约稿请求、报价、状态变更、交付确认等接口")
@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private static final Logger logger = LoggerFactory.getLogger(CommissionController.class);

    @Autowired
    private CommissionService commissionService;

    // ===================== 创建 =====================

    @Operation(summary = "发起约稿请求", description = "用户向画师发起约稿请求")
    @PostMapping
    public ResponseEntity<Result<CommissionDTO>> createCommission(
            @Valid @RequestBody CreateCommissionRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.createCommission(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建约稿失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("创建约稿失败: " + e.getMessage()));
        }
    }

    // ===================== 查询 =====================

    @Operation(summary = "获取约稿详情")
    @GetMapping("/{commissionId}")
    public ResponseEntity<Result<CommissionDTO>> getCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.getCommission(userId, commissionId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "获取我的约稿列表", description = "role=client 查委托，role=artist 查接稿")
    @GetMapping
    public ResponseEntity<Result<Page<CommissionDTO>>> getMyCommissions(
            @RequestParam(value = "role", defaultValue = "client") String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = CommissionStatus.valueOf(status.toUpperCase());
            }
            Page<CommissionDTO> result = commissionService.getMyCommissions(userId, role, statusEnum, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    // ===================== 画师报价 =====================

    @Operation(summary = "画师报价", description = "画师操作: PENDING → QUOTED")
    @PutMapping("/{commissionId}/quote")
    public ResponseEntity<Result<CommissionDTO>> quoteCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @Valid @RequestBody QuoteCommissionRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.quoteCommission(userId, commissionId, request);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    // ===================== 状态变更 =====================

    @Operation(summary = "接受报价（兼容接口，实际支付走 /api/payments）", description = "委托方操作")
    @PutMapping("/{commissionId}/accept")
    public ResponseEntity<Result<CommissionDTO>> acceptAndPayDeposit(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.acceptAndPayDeposit(userId, commissionId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "拒绝约稿请求", description = "画师操作: PENDING/QUOTED → REJECTED")
    @PutMapping("/{commissionId}/reject")
    public ResponseEntity<Result<CommissionDTO>> rejectCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String reason = body != null ? body.get("reason") : null;
            CommissionDTO dto = commissionService.rejectCommission(userId, commissionId, reason);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "开始创作", description = "画师操作: DEPOSIT_PAID → IN_PROGRESS")
    @PutMapping("/{commissionId}/start")
    public ResponseEntity<Result<CommissionDTO>> startWork(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.startWork(userId, commissionId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "交付作品", description = "画师操作: IN_PROGRESS → DELIVERED")
    @PutMapping("/{commissionId}/deliver")
    public ResponseEntity<Result<CommissionDTO>> deliverWork(
            @PathVariable(value = "commissionId") Long commissionId,
            @Valid @RequestBody DeliverWorkRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.deliverWork(userId, commissionId, request);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "确认收货", description = "委托方操作: DELIVERED → COMPLETED（释放尾款）")
    @PutMapping("/{commissionId}/confirm")
    public ResponseEntity<Result<CommissionDTO>> confirmDelivery(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.confirmDelivery(userId, commissionId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "请求修改", description = "委托方操作: DELIVERED → IN_PROGRESS（消耗修改次数）")
    @PutMapping("/{commissionId}/revision")
    public ResponseEntity<Result<CommissionDTO>> requestRevision(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionDTO dto = commissionService.requestRevision(userId, commissionId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "取消约稿", description = "双方均可操作 → CANCELLED")
    @PutMapping("/{commissionId}/cancel")
    public ResponseEntity<Result<CommissionDTO>> cancelCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String reason = body != null ? body.get("reason") : null;
            CommissionDTO dto = commissionService.cancelCommission(userId, commissionId, reason);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    // ===================== 约稿内消息 =====================

    @Operation(summary = "获取约稿消息列表")
    @GetMapping("/{commissionId}/messages")
    public ResponseEntity<Result<List<CommissionMessageDTO>>> getMessages(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            List<CommissionMessageDTO> messages = commissionService.getMessages(userId, commissionId);
            return ResponseEntity.ok(Result.success(messages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "发送约稿消息")
    @PostMapping("/{commissionId}/messages")
    public ResponseEntity<Result<CommissionMessageDTO>> sendMessage(
            @PathVariable(value = "commissionId") Long commissionId,
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionMessageDTO msg = commissionService.sendMessage(userId, commissionId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(msg));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    // ===================== 删除 =====================

    @Operation(summary = "删除约稿记录", description = "仅允许删除已完成/已取消/已拒绝的约稿")
    @DeleteMapping("/{commissionId}")
    public ResponseEntity<Result<Void>> deleteCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            commissionService.deleteCommission(userId, commissionId);
            return ResponseEntity.ok(Result.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("删除约稿失败: commissionId={}, userId={}", commissionId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("删除约稿失败: " + e.getMessage()));
        }
    }

    // ===================== 管理员操作 =====================

    @Operation(summary = "管理员取消约稿", description = "管理员介入取消约稿并处理退款")
    @PutMapping("/admin/{commissionId}/cancel")
    public ResponseEntity<Result<CommissionDTO>> adminCancelCommission(
            @PathVariable(value = "commissionId") Long commissionId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : null;
            boolean refundDeposit = body != null && body.containsKey("refundDeposit")
                    ? Boolean.parseBoolean(body.get("refundDeposit").toString())
                    : false;
            CommissionDTO dto = commissionService.adminCancelCommission(adminId, commissionId, reason, refundDeposit);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            logger.error("管理员取消约稿失败", e);
            return ResponseEntity.ok(Result.error("操作失败: " + e.getMessage()));
        }
    }
}
