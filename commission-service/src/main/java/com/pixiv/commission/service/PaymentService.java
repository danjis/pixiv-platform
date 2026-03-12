package com.pixiv.commission.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.pixiv.commission.config.AlipayConfig;
import com.pixiv.commission.dto.PaymentOrderDTO;
import com.pixiv.commission.entity.*;
import com.pixiv.commission.feign.MembershipClient;
import com.pixiv.commission.feign.UserServiceClient;
import com.pixiv.commission.repository.CommissionRepository;
import com.pixiv.commission.repository.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * 支付服务
 *
 * 对接支付宝沙箱支付，处理定金和尾款的支付、回调、退款
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private MembershipPaymentService membershipPaymentService;

    @Autowired
    private MembershipClient membershipClient;

    @Autowired
    private UserServiceClient userServiceClient;

    /** 平台服务费率（5%） */
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");

    /** 订单超时时间（分钟） */
    private static final int ORDER_TIMEOUT_MINUTES = 30;

    @Transactional
    public String createPayment(Long userId, Long commissionId, PaymentType paymentType, Long userCouponId) {
        // 查找约稿
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException("约稿不存在: " + commissionId));

        // 校验权限：只有委托方才能付款
        if (!commission.getClientId().equals(userId)) {
            throw new IllegalArgumentException("只有委托方才能支付");
        }

        // 校验状态
        validatePaymentStatus(commission, paymentType);

        // 检查是否已有未超时的待支付订单 → 复用它
        LocalDateTime expireThreshold = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        var existingOpt = paymentOrderRepository.findByCommissionIdAndPaymentTypeAndStatus(
                commissionId, paymentType, PaymentStatus.PENDING);
        if (existingOpt.isPresent()) {
            PaymentOrder existing = existingOpt.get();
            if (existing.getCreatedAt().isAfter(expireThreshold)) {
                // 未超时，直接复用此订单重新生成支付表单
                logger.info("复用待支付订单: orderNo={}", existing.getOrderNo());
                return generateAlipayForm(existing);
            } else {
                // 已超时，关闭旧订单
                existing.setStatus(PaymentStatus.CLOSED);
                paymentOrderRepository.save(existing);
                logger.info("关闭超时订单: orderNo={}", existing.getOrderNo());
            }
        }

        // 计算支付金额
        BigDecimal originalAmount = calculateAmount(commission, paymentType);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal actualAmount = originalAmount;
        Long appliedUserCouponId = null;

        // 定金不可使用优惠券，优惠券仅限尾款
        if (userCouponId != null && paymentType == PaymentType.DEPOSIT) {
            throw new IllegalArgumentException("定金支付不可使用优惠券，优惠券仅在支付尾款时可用");
        }

        // 如果用户选择了优惠券，计算折扣
        if (userCouponId != null) {
            try {
                discountAmount = couponService.useCoupon(userId, userCouponId, commissionId, originalAmount);
                actualAmount = originalAmount.subtract(discountAmount);
                // 确保实际支付金额不低于 0.01
                if (actualAmount.compareTo(new BigDecimal("0.01")) < 0) {
                    actualAmount = new BigDecimal("0.01");
                }
                appliedUserCouponId = userCouponId;
                logger.info("使用优惠券: userCouponId={}, discount={}, original={}, actual={}",
                        userCouponId, discountAmount, originalAmount, actualAmount);
            } catch (Exception e) {
                logger.warn("优惠券使用失败，将以原价支付: {}", e.getMessage());
                discountAmount = BigDecimal.ZERO;
                actualAmount = originalAmount;
            }
        }

        // 计算平台服务费（VIP/SVIP享折扣）
        BigDecimal platformFee = actualAmount.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal feeDiscount = BigDecimal.ZERO;
        try {
            var memberResult = membershipClient.getMembership(userId);
            if (memberResult != null && memberResult.isSuccess() && memberResult.getData() != null) {
                String level = (String) memberResult.getData().get("level");
                boolean expired = memberResult.getData().get("expired") != null
                        && (Boolean) memberResult.getData().get("expired");
                if (!expired) {
                    if ("SVIP".equals(level)) {
                        // SVIP 9折手续费 → 减免10%的费用
                        feeDiscount = platformFee.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
                    } else if ("VIP".equals(level)) {
                        // VIP 95折手续费 → 减免5%的费用
                        feeDiscount = platformFee.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("获取会员等级失败，不享受手续费折扣: userId={}", userId);
        }

        BigDecimal actualFee = platformFee.subtract(feeDiscount);
        actualAmount = actualAmount.add(actualFee);
        // 确保实际支付金额不低于 0.01
        if (actualAmount.compareTo(new BigDecimal("0.01")) < 0) {
            actualAmount = new BigDecimal("0.01");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 构建支付订单标题
        String subject = paymentType == PaymentType.DEPOSIT
                ? "约稿定金 - " + commission.getTitle()
                : "约稿尾款 - " + commission.getTitle();

        // 创建支付订单
        PaymentOrder paymentOrder = PaymentOrder.builder()
                .orderNo(orderNo)
                .commissionId(commissionId)
                .payerId(userId)
                .payeeId(commission.getArtistId())
                .amount(actualAmount)
                .originalAmount(appliedUserCouponId != null ? originalAmount : null)
                .discountAmount(appliedUserCouponId != null ? discountAmount : null)
                .userCouponId(appliedUserCouponId)
                .platformFee(actualFee)
                .feeDiscount(feeDiscount.compareTo(BigDecimal.ZERO) > 0 ? feeDiscount : null)
                .paymentType(paymentType)
                .status(PaymentStatus.PENDING)
                .subject(subject)
                .build();

        paymentOrderRepository.save(paymentOrder);

        logger.info("创建支付订单: orderNo={}, commission={}, type={}, amount={}, discount={}, fee={}, feeDiscount={}",
                orderNo, commissionId, paymentType, actualAmount, discountAmount, actualFee, feeDiscount);

        return generateAlipayForm(paymentOrder);
    }

    /**
     * 继续支付已有的待支付订单
     */
    @Transactional
    public String continuePay(Long userId, String orderNo) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("支付订单不存在: " + orderNo));

        if (!order.getPayerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        if (order.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("订单状态不是待支付，无法继续支付");
        }

        // 检查是否超时
        LocalDateTime expireTime = order.getCreatedAt().plusMinutes(ORDER_TIMEOUT_MINUTES);
        if (LocalDateTime.now().isAfter(expireTime)) {
            order.setStatus(PaymentStatus.CLOSED);
            paymentOrderRepository.save(order);
            throw new IllegalArgumentException("订单已超时，请重新发起支付");
        }

        logger.info("继续支付订单: orderNo={}", orderNo);
        return generateAlipayForm(order);
    }

    /**
     * 关闭超时的待支付订单（定时任务调用）
     */
    @Transactional
    public int closeExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
        List<PaymentOrder> expired = paymentOrderRepository.findExpiredPendingOrders(expireTime);
        for (PaymentOrder order : expired) {
            order.setStatus(PaymentStatus.CLOSED);
            paymentOrderRepository.save(order);
            logger.info("自动关闭超时订单: orderNo={}", order.getOrderNo());
        }
        return expired.size();
    }

    /**
     * 生成支付宝支付表单 HTML
     */
    private String generateAlipayForm(PaymentOrder paymentOrder) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());

            // 设置30分钟超时
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + paymentOrder.getOrderNo() + "\"," +
                    "\"total_amount\":\"" + paymentOrder.getAmount().toPlainString() + "\"," +
                    "\"subject\":\"" + paymentOrder.getSubject() + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"timeout_express\":\"30m\"" +
                    "}");

            String form = alipayClient.pageExecute(request).getBody();
            logger.info("支付宝支付表单已生成: orderNo={}", paymentOrder.getOrderNo());
            return form;

        } catch (AlipayApiException e) {
            logger.error("调用支付宝API失败: orderNo={}", paymentOrder.getOrderNo(), e);
            throw new RuntimeException("生成支付页面失败: " + e.getMessage());
        }
    }

    /**
     * 处理支付宝异步通知（核心回调）
     *
     * @param params 支付宝回调的全部参数
     * @return "success" 或 "failure"
     */
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
        logger.info("收到支付宝异步通知: {}", params);

        // 1. 验签
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());
            if (!signVerified) {
                logger.warn("支付宝异步通知验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验签异常", e);
            return "failure";
        }

        // 2. 取出关键参数
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        logger.info("支付宝通知: outTradeNo={}, tradeNo={}, status={}", outTradeNo, tradeNo, tradeStatus);

        // 3. 查找支付订单
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderNo(outTradeNo)
                .orElse(null);
        if (paymentOrder == null) {
            logger.warn("支付订单不存在: {}", outTradeNo);
            return "failure";
        }

        // 4. 幂等判断：已处理过的订单直接返回成功
        if (paymentOrder.getStatus() == PaymentStatus.PAID) {
            logger.info("订单已处理，跳过: {}", outTradeNo);
            return "success";
        }

        // 5. 判断交易状态
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新约稿/会员状态（先升级再标记PAID，保证原子性）
            if (paymentOrder.getPaymentType() == PaymentType.MEMBERSHIP) {
                // 会员支付：先调用会员升级逻辑，成功后再标记PAID
                try {
                    membershipPaymentService.handleMembershipPaymentSuccess(paymentOrder);
                } catch (Exception e) {
                    logger.error("会员升级失败，支付宝将重试通知: orderNo={}", outTradeNo, e);
                    return "failure"; // 返回failure让支付宝重新通知
                }
            }

            // 升级成功后再标记为PAID
            paymentOrder.setStatus(PaymentStatus.PAID);
            paymentOrder.setAlipayTradeNo(tradeNo);
            paymentOrder.setPaidAt(LocalDateTime.now());
            paymentOrderRepository.save(paymentOrder);

            // 约稿支付：更新约稿状态
            if (paymentOrder.getPaymentType() != PaymentType.MEMBERSHIP) {
                updateCommissionAfterPayment(paymentOrder);
            }

            logger.info("支付成功: orderNo={}, commissionId={}, type={}",
                    outTradeNo, paymentOrder.getCommissionId(), paymentOrder.getPaymentType());
            return "success";
        }

        logger.warn("未处理的交易状态: {}", tradeStatus);
        return "failure";
    }

    /**
     * 查询支付订单状态
     * 如果本地状态为 PENDING，主动查询支付宝确认实际支付结果
     */
    @Transactional
    public PaymentOrderDTO getPaymentStatus(String orderNo) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("支付订单不存在: " + orderNo));

        // 如果本地还是 PENDING，主动查询支付宝获取真实状态
        if (order.getStatus() == PaymentStatus.PENDING) {
            try {
                AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
                queryRequest.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
                AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);

                if (response.isSuccess()) {
                    String tradeStatus = response.getTradeStatus();
                    logger.info("主动查询支付宝: orderNo={}, tradeStatus={}", orderNo, tradeStatus);

                    if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                        // 先处理业务逻辑（会员升级/约稿状态），成功后再标记PAID
                        if (order.getPaymentType() == PaymentType.MEMBERSHIP) {
                            membershipPaymentService.handleMembershipPaymentSuccess(order);
                        } else {
                            updateCommissionAfterPayment(order);
                        }
                        order.setStatus(PaymentStatus.PAID);
                        order.setAlipayTradeNo(response.getTradeNo());
                        order.setPaidAt(LocalDateTime.now());
                        paymentOrderRepository.save(order);
                        logger.info("主动查询确认支付成功: orderNo={}", orderNo);
                    }
                } else {
                    logger.warn("支付宝查询返回失败: orderNo={}, code={}, msg={}",
                            orderNo, response.getCode(), response.getMsg());
                }
            } catch (AlipayApiException e) {
                logger.error("主动查询支付宝失败: orderNo={}", orderNo, e);
            }
        }

        return toDTO(order);
    }

    /**
     * 获取约稿相关的所有支付记录
     */
    public List<PaymentOrderDTO> getPaymentsByCommission(Long userId, Long commissionId) {
        // 校验权限
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new IllegalArgumentException("约稿不存在"));
        if (!commission.getClientId().equals(userId) && !commission.getArtistId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此约稿的支付记录");
        }

        return paymentOrderRepository.findByCommissionId(commissionId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 退款（取消约稿时使用）
     * 根据退款规则选择性退款：
     * - 委托方取消：定金不退，尾款退
     * - 画师取消：定金和尾款都退
     *
     * @param commissionId  约稿 ID
     * @param reason        退款原因
     * @param refundDeposit 是否退定金
     */
    @Transactional
    public void refundPayment(Long commissionId, String reason, boolean refundDeposit) {
        List<PaymentOrder> paidOrders = paymentOrderRepository.findByCommissionId(commissionId)
                .stream()
                .filter(o -> o.getStatus() == PaymentStatus.PAID)
                .collect(Collectors.toList());

        // 获取约稿信息用于钱包操作
        Commission commission = commissionRepository.findById(commissionId).orElse(null);

        for (PaymentOrder order : paidOrders) {
            // 根据规则判断是否退款此笔支付
            if (order.getPaymentType() == PaymentType.DEPOSIT && !refundDeposit) {
                logger.info("根据退款规则，定金不予退还: orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
                continue;
            }

            try {
                AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
                refundRequest.setBizContent("{" +
                        "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                        "\"refund_amount\":\"" + order.getAmount().toPlainString() + "\"," +
                        "\"refund_reason\":\"" + (reason != null ? reason : "约稿取消退款") + "\"" +
                        "}");

                AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
                if (response.isSuccess()) {
                    order.setStatus(PaymentStatus.REFUNDED);
                    order.setRefundedAt(LocalDateTime.now());
                    order.setRefundReason(reason);
                    paymentOrderRepository.save(order);
                    logger.info("退款成功: orderNo={}, type={}, amount={}", order.getOrderNo(), order.getPaymentType(),
                            order.getAmount());

                    // 退定金时，取消画师钱包的冻结金额（资金退给委托方，不入画师可用余额）
                    if (order.getPaymentType() == PaymentType.DEPOSIT && commission != null) {
                        try {
                            java.util.Map<String, Object> cancelBody = new java.util.LinkedHashMap<>();
                            cancelBody.put("userId", commission.getArtistId());
                            cancelBody.put("amount", commission.getDepositAmount());
                            cancelBody.put("commissionId", commissionId);
                            cancelBody.put("description", "约稿「" + commission.getTitle() + "」定金退款，取消冻结");
                            userServiceClient.cancelWalletFreeze(cancelBody);
                            logger.info("取消冻结成功: artistId={}, amount={}", commission.getArtistId(),
                                    commission.getDepositAmount());
                        } catch (Exception e) {
                            logger.error("取消冻结失败: commissionId={}", commissionId, e);
                        }
                    }
                } else {
                    logger.error("退款失败: orderNo={}, code={}, msg={}",
                            order.getOrderNo(), response.getCode(), response.getMsg());
                }
            } catch (AlipayApiException e) {
                logger.error("调用退款API失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    /**
     * 退款 - 兼容旧调用（默认退所有）
     */
    @Transactional
    public void refundPayment(Long commissionId, String reason) {
        refundPayment(commissionId, reason, true);
    }

    /**
     * 管理员指定退款单笔支付
     */
    @Transactional
    public void refundSinglePayment(Long paymentId, String reason) {
        PaymentOrder order = paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + paymentId));

        if (order.getStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("该支付记录状态不是已支付，无法退款（当前: " + order.getStatus() + "）");
        }

        try {
            AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
            refundRequest.setBizContent("{" +
                    "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                    "\"refund_amount\":\"" + order.getAmount().toPlainString() + "\"," +
                    "\"refund_reason\":\"" + (reason != null ? reason : "管理员操作退款") + "\"" +
                    "}");

            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (response.isSuccess()) {
                order.setStatus(PaymentStatus.REFUNDED);
                order.setRefundedAt(LocalDateTime.now());
                order.setRefundReason(reason);
                paymentOrderRepository.save(order);
                logger.info("管理员退款成功: paymentId={}, orderNo={}, type={}, amount={}",
                        paymentId, order.getOrderNo(), order.getPaymentType(), order.getAmount());
            } else {
                throw new RuntimeException("支付宝退款失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            logger.error("调用退款API失败: paymentId={}", paymentId, e);
            throw new RuntimeException("退款接口调用失败: " + e.getMessage());
        }
    }

    // ===================== Private Helpers =====================

    private void validatePaymentStatus(Commission commission, PaymentType paymentType) {
        if (paymentType == PaymentType.DEPOSIT) {
            if (commission.getStatus() != CommissionStatus.QUOTED) {
                throw new IllegalArgumentException("约稿状态不正确，画师尚未报价（当前状态: " + commission.getStatus() + "）");
            }
            // 检查定金是否已支付
            if (paymentOrderRepository.existsByCommissionIdAndPaymentTypeAndStatus(
                    commission.getId(), PaymentType.DEPOSIT, PaymentStatus.PAID)) {
                throw new IllegalArgumentException("定金已支付，不能重复付款");
            }
        } else if (paymentType == PaymentType.FINAL_PAYMENT) {
            if (commission.getStatus() != CommissionStatus.DELIVERED) {
                throw new IllegalArgumentException("作品还未交付，无法支付尾款（当前状态: " + commission.getStatus() + "）");
            }
            if (paymentOrderRepository.existsByCommissionIdAndPaymentTypeAndStatus(
                    commission.getId(), PaymentType.FINAL_PAYMENT, PaymentStatus.PAID)) {
                throw new IllegalArgumentException("尾款已支付，不能重复付款");
            }
        }
    }

    private BigDecimal calculateAmount(Commission commission, PaymentType paymentType) {
        if (paymentType == PaymentType.DEPOSIT) {
            return commission.getDepositAmount();
        } else {
            // 尾款 = 总额 - 定金
            return commission.getTotalAmount().subtract(commission.getDepositAmount());
        }
    }

    /**
     * 获取所有支付记录（管理端）
     */
    public List<PaymentOrderDTO> getAllPayments(int page, int size) {
        Page<PaymentOrder> orderPage = paymentOrderRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return orderPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有约稿列表（管理端）
     */
    public List<Map<String, Object>> getAllCommissions(int page, int size) {
        Page<Commission> commissionPage = commissionRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return commissionPage.getContent().stream()
                .map(c -> {
                    Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("id", c.getId());
                    map.put("title", c.getTitle());
                    map.put("clientId", c.getClientId());
                    map.put("artistId", c.getArtistId());
                    map.put("status", c.getStatus().name());
                    map.put("totalAmount", c.getTotalAmount());
                    map.put("depositAmount", c.getDepositAmount());
                    map.put("depositPaid", c.getDepositPaid());
                    map.put("finalPaid", c.getFinalPaid());
                    map.put("createdAt", c.getCreatedAt());
                    map.put("updatedAt", c.getUpdatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private void updateCommissionAfterPayment(PaymentOrder paymentOrder) {
        Commission commission = commissionRepository.findById(paymentOrder.getCommissionId())
                .orElse(null);
        if (commission == null)
            return;

        if (paymentOrder.getPaymentType() == PaymentType.DEPOSIT) {
            commission.setDepositPaid(true);
            commission.setStatus(CommissionStatus.DEPOSIT_PAID);
            commissionRepository.save(commission);
            logger.info("约稿状态更新: {} → DEPOSIT_PAID", commission.getId());

            // 冻结定金金额到画师钱包（仅定金本身，不含手续费）
            try {
                java.util.Map<String, Object> freezeBody = new java.util.LinkedHashMap<>();
                freezeBody.put("userId", commission.getArtistId());
                freezeBody.put("amount", commission.getDepositAmount());
                freezeBody.put("commissionId", commission.getId());
                freezeBody.put("description", "约稿「" + commission.getTitle() + "」定金冻结");
                userServiceClient.freezeWalletAmount(freezeBody);
                logger.info("冻结定金成功: artistId={}, amount={}", commission.getArtistId(), commission.getDepositAmount());
            } catch (Exception e) {
                logger.error("冻结定金失败: commissionId={}", commission.getId(), e);
            }
        } else if (paymentOrder.getPaymentType() == PaymentType.FINAL_PAYMENT) {
            commission.setFinalPaid(true);
            commission.setStatus(CommissionStatus.COMPLETED);
            commissionRepository.save(commission);
            logger.info("约稿状态更新: {} → COMPLETED", commission.getId());

            // 解冻定金 + 尾款入账（画师收到全额，不受优惠券影响）
            BigDecimal depositAmount = commission.getDepositAmount();
            BigDecimal finalAmount = commission.getTotalAmount().subtract(depositAmount);

            try {
                // 1. 解冻定金 → 可用余额
                java.util.Map<String, Object> unfreezeBody = new java.util.LinkedHashMap<>();
                unfreezeBody.put("userId", commission.getArtistId());
                unfreezeBody.put("amount", depositAmount);
                unfreezeBody.put("commissionId", commission.getId());
                unfreezeBody.put("description", "约稿「" + commission.getTitle() + "」定金解冻");
                userServiceClient.unfreezeWalletAmount(unfreezeBody);
                logger.info("解冻定金成功: artistId={}, amount={}", commission.getArtistId(), depositAmount);

                // 2. 尾款入账（画师收到完整尾款，优惠券减免由平台承担）
                java.util.Map<String, Object> incomeBody = new java.util.LinkedHashMap<>();
                incomeBody.put("userId", commission.getArtistId());
                incomeBody.put("amount", finalAmount);
                incomeBody.put("commissionId", commission.getId());
                incomeBody.put("orderNo", paymentOrder.getOrderNo());
                incomeBody.put("description", "约稿「" + commission.getTitle() + "」尾款入账");
                userServiceClient.addWalletIncome(incomeBody);
                logger.info("尾款入账成功: artistId={}, amount={}", commission.getArtistId(), finalAmount);
            } catch (Exception e) {
                logger.error("钱包操作失败: commissionId={}", commission.getId(), e);
            }
        }
    }

    /**
     * 转账到支付宝账户（提现打款）
     */
    public String transferToAlipayAccount(String outBizNo, BigDecimal amount, String alipayAccount, String alipayName) {
        try {
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            request.setBizContent("{" +
                    "\"out_biz_no\":\"" + outBizNo + "\"," +
                    "\"trans_amount\":\"" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "\"," +
                    "\"product_code\":\"TRANS_ACCOUNT_NO_PWD\"," +
                    "\"biz_scene\":\"DIRECT_TRANSFER\"," +
                    "\"payee_info\":{" +
                    "\"identity\":\"" + alipayAccount + "\"," +
                    "\"identity_type\":\"ALIPAY_LOGON_ID\"," +
                    "\"name\":\"" + alipayName + "\"" +
                    "}" +
                    "}");

            AlipayFundTransUniTransferResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                logger.info("支付宝转账成功: outBizNo={}, amount={}, alipayAccount={}, orderId={}",
                        outBizNo, amount, alipayAccount, response.getOrderId());
                return response.getOrderId();
            } else {
                logger.error("支付宝转账失败: outBizNo={}, code={}, msg={}, subMsg={}",
                        outBizNo, response.getCode(), response.getMsg(), response.getSubMsg());
                throw new RuntimeException("支付宝转账失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            logger.error("调用支付宝转账API异常: outBizNo={}", outBizNo, e);
            throw new RuntimeException("支付宝转账接口调用失败: " + e.getMessage());
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PAY" + timestamp + uuid;
    }

    private PaymentOrderDTO toDTO(PaymentOrder order) {
        LocalDateTime expireAt = null;
        if (order.getStatus() == PaymentStatus.PENDING) {
            expireAt = order.getCreatedAt().plusMinutes(ORDER_TIMEOUT_MINUTES);
        }
        return PaymentOrderDTO.builder()
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
                .expireAt(expireAt)
                .userCouponId(order.getUserCouponId())
                .originalAmount(order.getOriginalAmount())
                .discountAmount(order.getDiscountAmount())
                .platformFee(order.getPlatformFee())
                .feeDiscount(order.getFeeDiscount())
                .build();
    }
}
