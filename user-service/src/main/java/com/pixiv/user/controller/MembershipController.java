package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.MembershipDTO;
import com.pixiv.user.service.MembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    /**
     * 管理员查看会员列表
     */
    @GetMapping("/api/admin/membership/list")
    public ResponseEntity<Result<Map<String, Object>>> listMembers(
            @RequestHeader(value = "X-User-Id", required = false) Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "level", required = false) String level) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        Map<String, Object> result = membershipService.listMembers(page, size, level);
        return ResponseEntity.ok(Result.success(result));
    }

    @GetMapping("/api/membership")
    public ResponseEntity<Result<MembershipDTO>> getCurrentMembership(
            @RequestHeader("X-User-Id") Long userId) {
        MembershipDTO membership = membershipService.getMembership(userId);
        return ResponseEntity.ok(Result.success(membership));
    }

    @GetMapping("/api/membership/{userId}")
    public ResponseEntity<Result<MembershipDTO>> getMembershipByUserId(
            @PathVariable("userId") Long userId) {
        MembershipDTO membership = membershipService.getMembership(userId);
        return ResponseEntity.ok(Result.success(membership));
    }

    @PostMapping("/api/admin/membership/upgrade")
    public ResponseEntity<Result<MembershipDTO>> upgradeMembership(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody UpgradeMembershipRequest request) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        MembershipDTO membership = membershipService.upgradeMembership(
                request.getUserId(), request.getLevel(), request.getDurationDays());
        return ResponseEntity.ok(Result.success(membership));
    }

    /**
     * 用户自行开通/续费会员（模拟支付）
     */
    @PostMapping("/api/membership/subscribe")
    public ResponseEntity<Result<MembershipDTO>> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpgradeMembershipRequest request) {
        MembershipDTO membership = membershipService.upgradeMembership(
                userId, request.getLevel(), request.getDurationDays());
        return ResponseEntity.ok(Result.success(membership));
    }

    /**
     * 内部接口：支付成功后由 commission-service 调用升级会员
     * 无需鉴权（服务间调用）
     */
    @PostMapping("/api/membership/internal/upgrade")
    public ResponseEntity<Result<MembershipDTO>> internalUpgrade(
            @RequestBody UpgradeMembershipRequest request) {
        MembershipDTO membership = membershipService.upgradeMembership(
                request.getUserId(), request.getLevel(), request.getDurationDays());
        return ResponseEntity.ok(Result.success(membership));
    }

    public static class UpgradeMembershipRequest {

        private Long userId;
        private String level;
        private int durationDays;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public int getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(int durationDays) {
            this.durationDays = durationDays;
        }
    }
}
