package com.pixiv.notification.feign;

import com.pixiv.common.dto.Result;
import com.pixiv.notification.dto.CommissionSnapshot;
import com.pixiv.notification.dto.PaymentSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "commission-service")
public interface CommissionServiceClient {

    @GetMapping("/api/commissions/{commissionId}")
    Result<CommissionSnapshot> getCommission(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("commissionId") Long commissionId);

    @GetMapping("/api/payments/commission/{commissionId}")
    Result<List<PaymentSnapshot>> getCommissionPayments(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("commissionId") Long commissionId);

    @PostMapping("/api/payments/admin/refund")
    Result<Void> adminRefund(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> body);

    @PutMapping("/api/commissions/admin/{commissionId}/cancel")
    Result<CommissionSnapshot> adminCancelCommission(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable("commissionId") Long commissionId,
            @RequestBody Map<String, Object> body);
}
