package com.pixiv.commission.controller;

import com.pixiv.commission.dto.PaymentOrderDTO;
import com.pixiv.commission.entity.PaymentOrder;
import com.pixiv.commission.entity.PaymentType;
import com.pixiv.commission.entity.PaymentStatus;
import com.pixiv.commission.service.MembershipPaymentService;
import com.pixiv.commission.service.PaymentService;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 *
 * 处理支付宝沙箱支付相关操作
 */
@Tag(name = "支付管理", description = "支付宝沙箱支付接口")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MembershipPaymentService membershipPaymentService;

    @Autowired
    private com.pixiv.commission.repository.PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private com.pixiv.commission.service.CouponService couponService;

    /**
     * 创建支付订单，返回支付宝支付页面 HTML
     */
    @Operation(summary = "创建支付", description = "生成支付宝支付表单，前端提交表单跳转支付宝")
    @PostMapping("/create")
    public ResponseEntity<Result<String>> createPayment(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            Long commissionId = Long.valueOf(body.get("commissionId").toString());
            String typeStr = body.get("paymentType").toString();
            PaymentType paymentType = PaymentType.valueOf(typeStr.toUpperCase());
            Long userCouponId = body.get("userCouponId") != null
                    ? Long.valueOf(body.get("userCouponId").toString())
                    : null;

            String payForm = paymentService.createPayment(userId, commissionId, paymentType, userCouponId);
            return ResponseEntity.ok(Result.success(payForm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建支付失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("创建支付失败: " + e.getMessage()));
        }
    }

    /**
     * 支付宝异步通知回调（非常重要！）
     * 支付宝服务器直接调用此接口，无需 X-User-Id
     */
    @Operation(summary = "支付宝异步通知", description = "支付宝回调接口，处理支付结果")
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 将 request 中的参数转成 Map
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String[] values = entry.getValue();
                StringBuilder valueStr = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
                }
                params.put(entry.getKey(), valueStr.toString());
            }

            return paymentService.handleAlipayNotify(params);
        } catch (Exception e) {
            logger.error("处理支付宝通知异常", e);
            return "failure";
        }
    }

    /**
     * 查询支付订单状态
     */
    @Operation(summary = "查询支付状态")
    @GetMapping("/status")
    public ResponseEntity<Result<PaymentOrderDTO>> getPaymentStatus(
            @RequestParam("orderNo") String orderNo) {
        try {
            PaymentOrderDTO dto = paymentService.getPaymentStatus(orderNo);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 继续支付已有的待支付订单
     */
    @Operation(summary = "继续支付", description = "对已有的待支付订单重新生成支付宝表单")
    @PostMapping("/continue")
    public ResponseEntity<Result<String>> continuePay(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String orderNo = body.get("orderNo").toString();
            String payForm = paymentService.continuePay(userId, orderNo);
            return ResponseEntity.ok(Result.success(payForm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("继续支付失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("继续支付失败: " + e.getMessage()));
        }
    }

    /**
     * 获取约稿的所有支付记录
     */
    @Operation(summary = "约稿支付记录")
    @GetMapping("/commission/{commissionId}")
    public ResponseEntity<Result<List<PaymentOrderDTO>>> getCommissionPayments(
            @PathVariable("commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            List<PaymentOrderDTO> payments = paymentService.getPaymentsByCommission(userId, commissionId);
            return ResponseEntity.ok(Result.success(payments));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 用户直退已废弃，统一改为提交售后/申诉后由管理员审核处理
     */
    @Operation(summary = "申请退款（已废弃）", description = "用户端不再支持直接退款，需提交售后/申诉后由管理员审核")
    @PostMapping("/refund")
    public ResponseEntity<Result<Void>> refund(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        Long commissionId = body.get("commissionId") != null
                ? Long.valueOf(body.get("commissionId").toString())
                : null;
        logger.warn("拦截用户直退请求: userId={}, commissionId={}", userId, commissionId);
        return ResponseEntity.badRequest()
                .body(Result.error("用户端不支持直接退款，请先提交售后/申诉申请，由管理员审核后处理"));
    }

    // =========== 会员支付接口 ===========

    /**
     * 创建会员支付订单（返回支付宝支付表单）
     */
    @Operation(summary = "创建会员支付", description = "VIP/SVIP 会员开通支付")
    @PostMapping("/membership")
    public ResponseEntity<Result<String>> createMembershipPayment(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String level = body.get("level").toString();
            int durationDays = Integer.parseInt(body.get("durationDays").toString());
            String payForm = membershipPaymentService.createMembershipPayment(userId, level, durationDays);
            return ResponseEntity.ok(Result.success(payForm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建会员支付失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("创建会员支付失败: " + e.getMessage()));
        }
    }

    /**
     * 查询会员支付订单状态
     */
    @Operation(summary = "查询会员支付状态")
    @GetMapping("/membership/status")
    public ResponseEntity<Result<PaymentOrderDTO>> getMembershipPaymentStatus(
            @RequestParam("orderNo") String orderNo,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            var order = membershipPaymentService.getPaymentStatus(orderNo, userId);
            if (order == null) {
                return ResponseEntity.ok(Result.error("订单不存在"));
            }
            // 转成 DTO 返回
            PaymentOrderDTO dto = PaymentOrderDTO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .commissionId(order.getCommissionId())
                    .payerId(order.getPayerId())
                    .payeeId(order.getPayeeId())
                    .amount(order.getAmount())
                    .paymentType(order.getPaymentType())
                    .alipayTradeNo(order.getAlipayTradeNo())
                    .status(order.getStatus())
                    .subject(order.getSubject())
                    .paidAt(order.getPaidAt())
                    .createdAt(order.getCreatedAt())
                    .build();
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            logger.error("查询会员支付状态失败", e);
            return ResponseEntity.ok(Result.error("查询失败"));
        }
    }

    // =========== 我的订单接口 ===========

    /**
     * 取消待支付订单（关闭订单）
     */
    @Operation(summary = "取消订单", description = "取消待支付的订单")
    @PostMapping("/cancel")
    public ResponseEntity<Result<Void>> cancelOrder(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String orderNo = body.get("orderNo").toString();
            com.pixiv.commission.entity.PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
            if (!order.getPayerId().equals(userId)) {
                throw new IllegalArgumentException("无权操作此订单");
            }
            if (order.getStatus() != PaymentStatus.PENDING) {
                throw new IllegalArgumentException("只能取消待支付的订单");
            }
            order.setStatus(PaymentStatus.CLOSED);
            paymentOrderRepository.save(order);
            // 如果使用了优惠券，归还优惠券
            if (order.getUserCouponId() != null) {
                try {
                    couponService.returnCoupon(order.getUserCouponId());
                } catch (Exception e) {
                    logger.warn("归还优惠券失败: {}", e.getMessage());
                }
            }
            logger.info("用户取消订单: orderNo={}, userId={}", orderNo, userId);
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("取消订单失败", e);
            return ResponseEntity.ok(Result.error("取消订单失败: " + e.getMessage()));
        }
    }

    /**
     * 删除订单记录（待支付订单不可删除）
     */
    @Operation(summary = "删除订单", description = "删除非待支付状态的订单记录")
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<Result<Void>> deleteOrder(
            @PathVariable("orderNo") String orderNo,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            com.pixiv.commission.entity.PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
            if (!order.getPayerId().equals(userId)) {
                throw new IllegalArgumentException("无权操作此订单");
            }
            if (order.getStatus() == PaymentStatus.PENDING) {
                throw new IllegalArgumentException("待支付订单不可删除，请先取消");
            }
            paymentOrderRepository.delete(order);
            logger.info("用户删除订单: orderNo={}, userId={}", orderNo, userId);
            return ResponseEntity.ok(Result.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("删除订单失败", e);
            return ResponseEntity.ok(Result.error("删除订单失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户的支付订单列表（分页）
     */
    @Operation(summary = "我的订单", description = "获取当前用户的所有支付订单记录")
    @GetMapping("/my")
    public ResponseEntity<Result<Map<String, Object>>> getMyOrders(
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentType", required = false) String paymentType) {
        try {
            Page<com.pixiv.commission.entity.PaymentOrder> pageResult;
            PageRequest pageable = PageRequest.of(page - 1, size);

            if (status != null && !status.isEmpty()) {
                pageResult = paymentOrderRepository.findByPayerIdAndStatusOrderByCreatedAtDesc(
                        userId, PaymentStatus.valueOf(status), pageable);
            } else if (paymentType != null && !paymentType.isEmpty()) {
                pageResult = paymentOrderRepository.findByPayerIdAndPaymentTypeOrderByCreatedAtDesc(
                        userId, PaymentType.valueOf(paymentType), pageable);
            } else {
                pageResult = paymentOrderRepository.findByPayerIdOrderByCreatedAtDesc(userId, pageable);
            }

            List<PaymentOrderDTO> dtoList = pageResult.getContent().stream().map(order -> PaymentOrderDTO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .commissionId(order.getCommissionId())
                    .payerId(order.getPayerId())
                    .payeeId(order.getPayeeId())
                    .amount(order.getAmount())
                    .paymentType(order.getPaymentType())
                    .status(order.getStatus())
                    .subject(order.getSubject())
                    .paidAt(order.getPaidAt())
                    .refundedAt(order.getRefundedAt())
                    .refundReason(order.getRefundReason())
                    .createdAt(order.getCreatedAt())
                    .originalAmount(order.getOriginalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .platformFee(order.getPlatformFee())
                    .feeDiscount(order.getFeeDiscount())
                    .build()).collect(java.util.stream.Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", dtoList);
            result.put("total", pageResult.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", pageResult.getTotalPages());

            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取我的订单失败", e);
            return ResponseEntity.ok(Result.error("获取订单列表失败: " + e.getMessage()));
        }
    }

    // =========== 管理端接口 ===========

    /**
     * 获取所有支付记录（管理端）
     */
    @Operation(summary = "获取所有支付记录（管理端）")
    @GetMapping("/admin/list")
    public ResponseEntity<Result<List<PaymentOrderDTO>>> adminListPayments(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            List<PaymentOrderDTO> payments = paymentService.getAllPayments(page, size);
            return ResponseEntity.ok(Result.success(payments));
        } catch (Exception e) {
            logger.error("获取支付列表失败", e);
            return ResponseEntity.ok(Result.error("获取支付列表失败"));
        }
    }

    /**
     * 管理员手动退款（指定单笔支付）
     */
    @Operation(summary = "管理员手动退款")
    @PostMapping("/admin/refund")
    public ResponseEntity<Result<Void>> adminRefund(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId,
            @RequestBody Map<String, Object> body) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            String reason = body.get("reason") != null ? body.get("reason").toString() : "管理员操作退款";
            logger.info("管理员退款操作: adminId={}, reason={}", adminId, reason);

            // 支持两种模式：按paymentId退单笔，或按commissionId退全部
            if (body.containsKey("paymentId")) {
                Long paymentId = Long.valueOf(body.get("paymentId").toString());
                paymentService.refundSinglePayment(paymentId, reason);
                return ResponseEntity.ok(Result.success());
            } else if (body.containsKey("commissionId")) {
                Long commissionId = Long.valueOf(body.get("commissionId").toString());
                boolean refundDeposit = body.containsKey("refundDeposit")
                        ? Boolean.parseBoolean(body.get("refundDeposit").toString())
                        : false; // 默认不退定金
                paymentService.refundPayment(commissionId, reason, refundDeposit);
                return ResponseEntity.ok(Result.success());
            } else {
                return ResponseEntity.badRequest().body(Result.error("请提供 paymentId 或 commissionId"));
            }
        } catch (Exception e) {
            logger.error("管理员退款失败", e);
            return ResponseEntity.ok(Result.error("退款失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有约稿列表（管理端）
     */
    @Operation(summary = "获取所有约稿（管理端）")
    @GetMapping("/admin/commissions")
    public ResponseEntity<Result<List<Map<String, Object>>>> adminListCommissions(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            List<Map<String, Object>> list = paymentService.getAllCommissions(page, size);
            return ResponseEntity.ok(Result.success(list));
        } catch (Exception e) {
            logger.error("获取约稿列表失败", e);
            return ResponseEntity.ok(Result.error("获取约稿列表失败"));
        }
    }

    // =========== 财务统计接口（管理端） ===========

    /**
     * 获取财务概览
     * 包含平台总收入、画师总收入、会员收入、约稿手续费收入、退款总额等
     */
    @Operation(summary = "财务概览（管理端）", description = "获取平台收入统计概览数据")
    @GetMapping("/admin/finance/overview")
    public ResponseEntity<Result<Map<String, Object>>> getFinanceOverview(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            Map<String, Object> overview = new HashMap<>();

            // 1. 总交易额（所有已支付订单金额）
            BigDecimal totalPaidAmount = paymentOrderRepository.sumAmountByStatus(PaymentStatus.PAID);

            // 2. 会员充值收入（全部属于平台）
            BigDecimal membershipIncome = paymentOrderRepository.sumAmountByStatusAndType(
                    PaymentStatus.PAID, PaymentType.MEMBERSHIP);

            // 3. 约稿交易额（定金 + 尾款，属于画师）
            BigDecimal commissionDeposit = paymentOrderRepository.sumAmountByStatusAndType(
                    PaymentStatus.PAID, PaymentType.DEPOSIT);
            BigDecimal commissionFinal = paymentOrderRepository.sumAmountByStatusAndType(
                    PaymentStatus.PAID, PaymentType.FINAL_PAYMENT);
            BigDecimal commissionTotal = commissionDeposit.add(commissionFinal);

            // 4. 平台服务费收入（从约稿订单收取的手续费）
            BigDecimal platformFeeIncome = paymentOrderRepository.sumPlatformFeeByStatus(PaymentStatus.PAID);

            // 5. 服务费优惠减免
            BigDecimal feeDiscountTotal = paymentOrderRepository.sumFeeDiscountByStatus(PaymentStatus.PAID);

            // 6. 退款总额
            BigDecimal refundedAmount = paymentOrderRepository.sumAmountByStatus(PaymentStatus.REFUNDED);

            // 6.5 优惠券补贴总额（平台承担）
            BigDecimal couponSubsidy = paymentOrderRepository.sumDiscountAmountByStatus(PaymentStatus.PAID);

            // 7. 平台总收入 = 会员收入 + 服务费收入 - 优惠券补贴
            BigDecimal platformTotalIncome = membershipIncome.add(platformFeeIncome).subtract(couponSubsidy);

            // 8. 画师总收入 = 基础约稿金额（不含服务费，不受优惠券影响）
            BigDecimal artistTotalIncome = paymentOrderRepository.sumArtistIncomeByStatus(PaymentStatus.PAID);

            // 9. 订单统计
            long totalPaidOrders = paymentOrderRepository.countByStatus(PaymentStatus.PAID);
            long totalRefundedOrders = paymentOrderRepository.countByStatus(PaymentStatus.REFUNDED);
            long membershipOrders = paymentOrderRepository.countByStatusAndPaymentType(
                    PaymentStatus.PAID, PaymentType.MEMBERSHIP);
            long commissionOrders = paymentOrderRepository.countByStatusAndPaymentType(
                    PaymentStatus.PAID, PaymentType.DEPOSIT)
                    + paymentOrderRepository.countByStatusAndPaymentType(
                            PaymentStatus.PAID, PaymentType.FINAL_PAYMENT);

            // 10. 今日数据
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = todayStart.plusDays(1);
            BigDecimal todayIncome = paymentOrderRepository.sumAmountByStatusAndPaidAtBetween(
                    PaymentStatus.PAID, todayStart, todayEnd);
            BigDecimal todayPlatformFee = paymentOrderRepository.sumPlatformFeeByStatusAndPaidAtBetween(
                    PaymentStatus.PAID, todayStart, todayEnd);
            BigDecimal todayMembership = paymentOrderRepository.sumAmountByStatusAndTypeAndPaidAtBetween(
                    PaymentStatus.PAID, PaymentType.MEMBERSHIP, todayStart, todayEnd);
            long todayOrders = paymentOrderRepository.countByStatusAndPaidAtBetween(
                    PaymentStatus.PAID, todayStart, todayEnd);

            overview.put("totalPaidAmount", totalPaidAmount);
            overview.put("membershipIncome", membershipIncome);
            overview.put("commissionTotal", commissionTotal);
            overview.put("platformFeeIncome", platformFeeIncome);
            overview.put("feeDiscountTotal", feeDiscountTotal);
            overview.put("refundedAmount", refundedAmount);
            overview.put("platformTotalIncome", platformTotalIncome);
            overview.put("artistTotalIncome", artistTotalIncome);
            overview.put("couponSubsidy", couponSubsidy);
            overview.put("totalPaidOrders", totalPaidOrders);
            overview.put("totalRefundedOrders", totalRefundedOrders);
            overview.put("membershipOrders", membershipOrders);
            overview.put("commissionOrders", commissionOrders);
            overview.put("todayIncome", todayIncome);
            overview.put("todayPlatformFee", todayPlatformFee);
            overview.put("todayMembership", todayMembership);
            overview.put("todayOrders", todayOrders);

            return ResponseEntity.ok(Result.success(overview));
        } catch (Exception e) {
            logger.error("获取财务概览失败", e);
            return ResponseEntity.ok(Result.error("获取财务概览失败"));
        }
    }

    /**
     * 获取收入趋势（按天/周/月）
     * 
     * @param period day / week / month
     * @param days   查询最近多少天（默认30）
     */
    @Operation(summary = "收入趋势（管理端）", description = "获取指定时间段的收入趋势数据")
    @GetMapping("/admin/finance/trend")
    public ResponseEntity<Result<List<Map<String, Object>>>> getFinanceTrend(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "period", defaultValue = "day") String period,
            @RequestParam(value = "days", defaultValue = "30") int days) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            List<Map<String, Object>> trend = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = start.plusDays(1);

                BigDecimal totalAmount = paymentOrderRepository.sumAmountByStatusAndPaidAtBetween(
                        PaymentStatus.PAID, start, end);
                BigDecimal membershipAmount = paymentOrderRepository.sumAmountByStatusAndTypeAndPaidAtBetween(
                        PaymentStatus.PAID, PaymentType.MEMBERSHIP, start, end);
                BigDecimal platformFee = paymentOrderRepository.sumPlatformFeeByStatusAndPaidAtBetween(
                        PaymentStatus.PAID, start, end);
                BigDecimal commissionAmount = totalAmount.subtract(membershipAmount);
                long orderCount = paymentOrderRepository.countByStatusAndPaidAtBetween(
                        PaymentStatus.PAID, start, end);

                // 平台收入 = 会员收入 + 服务费
                BigDecimal platformIncome = membershipAmount.add(platformFee);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());
                dayData.put("totalAmount", totalAmount);
                dayData.put("membershipAmount", membershipAmount);
                dayData.put("commissionAmount", commissionAmount);
                dayData.put("platformFee", platformFee);
                dayData.put("platformIncome", platformIncome);
                dayData.put("artistIncome", commissionAmount);
                dayData.put("orderCount", orderCount);
                trend.add(dayData);
            }

            return ResponseEntity.ok(Result.success(trend));
        } catch (Exception e) {
            logger.error("获取收入趋势失败", e);
            return ResponseEntity.ok(Result.error("获取收入趋势失败"));
        }
    }

    /**
     * 获取最近交易明细（管理端）
     */
    @Operation(summary = "最近交易明细（管理端）")
    @GetMapping("/admin/finance/recent")
    public ResponseEntity<Result<List<PaymentOrderDTO>>> getRecentTransactions(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限操作"));
        }
        try {
            var pageable = PageRequest.of(0, size);
            List<PaymentOrder> orders = paymentOrderRepository.findPaidCommissionOrders(pageable);

            List<PaymentOrderDTO> dtoList = orders.stream().map(order -> PaymentOrderDTO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .commissionId(order.getCommissionId())
                    .payerId(order.getPayerId())
                    .payeeId(order.getPayeeId())
                    .amount(order.getAmount())
                    .paymentType(order.getPaymentType())
                    .status(order.getStatus())
                    .subject(order.getSubject())
                    .paidAt(order.getPaidAt())
                    .refundedAt(order.getRefundedAt())
                    .refundReason(order.getRefundReason())
                    .createdAt(order.getCreatedAt())
                    .originalAmount(order.getOriginalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .platformFee(order.getPlatformFee())
                    .feeDiscount(order.getFeeDiscount())
                    .build()).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(Result.success(dtoList));
        } catch (Exception e) {
            logger.error("获取最近交易失败", e);
            return ResponseEntity.ok(Result.error("获取最近交易失败"));
        }
    }

    /**
     * 内部接口：转账到支付宝账户（提现打款）
     */
    @PostMapping("/internal/transfer")
    @Operation(summary = "转账到支付宝账户（内部服务调用）")
    public ResponseEntity<Result<String>> transferToAlipay(@RequestBody Map<String, String> body) {
        String outBizNo = body.get("outBizNo");
        String amount = body.get("amount");
        String alipayAccount = body.get("alipayAccount");
        String alipayName = body.get("alipayName");

        if (outBizNo == null || amount == null || alipayAccount == null || alipayName == null) {
            return ResponseEntity.badRequest().body(Result.error(400, "参数不完整"));
        }

        try {
            String orderId = paymentService.transferToAlipayAccount(
                    outBizNo, new java.math.BigDecimal(amount), alipayAccount, alipayName);
            return ResponseEntity.ok(Result.success(orderId));
        } catch (Exception e) {
            logger.error("提现转账失败: outBizNo={}", outBizNo, e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
}
