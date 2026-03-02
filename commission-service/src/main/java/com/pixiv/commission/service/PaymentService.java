package com.pixiv.commission.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.pixiv.commission.config.AlipayConfig;
import com.pixiv.commission.dto.PaymentOrderDTO;
import com.pixiv.commission.entity.*;
import com.pixiv.commission.repository.CommissionRepository;
import com.pixiv.commission.repository.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * 创建支付订单并生成支付宝支付页面 HTML
     *
     * @param userId       当前用户 ID（付款方）
     * @param commissionId 约稿 ID
     * @param paymentType  支付类型（DEPOSIT / FINAL_PAYMENT）
     * @return 支付宝的支付页面 HTML 表单
     */
    /** 订单超时时间（分钟） */
    private static final int ORDER_TIMEOUT_MINUTES = 30;

    @Transactional
    public String createPayment(Long userId, Long commissionId, PaymentType paymentType) {
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
        BigDecimal amount = calculateAmount(commission, paymentType);

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
                .amount(amount)
                .paymentType(paymentType)
                .status(PaymentStatus.PENDING)
                .subject(subject)
                .build();

        paymentOrderRepository.save(paymentOrder);

        logger.info("创建支付订单: orderNo={}, commission={}, type={}, amount={}",
                orderNo, commissionId, paymentType, amount);

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
            // 更新支付订单
            paymentOrder.setStatus(PaymentStatus.PAID);
            paymentOrder.setAlipayTradeNo(tradeNo);
            paymentOrder.setPaidAt(LocalDateTime.now());
            paymentOrderRepository.save(paymentOrder);

            // 更新约稿状态
            updateCommissionAfterPayment(paymentOrder);

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
                        order.setStatus(PaymentStatus.PAID);
                        order.setAlipayTradeNo(response.getTradeNo());
                        order.setPaidAt(LocalDateTime.now());
                        paymentOrderRepository.save(order);

                        // 同步更新约稿状态
                        updateCommissionAfterPayment(order);
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
     *
     * @param commissionId 约稿 ID
     * @param reason       退款原因
     */
    @Transactional
    public void refundPayment(Long commissionId, String reason) {
        List<PaymentOrder> paidOrders = paymentOrderRepository.findByCommissionId(commissionId)
                .stream()
                .filter(o -> o.getStatus() == PaymentStatus.PAID)
                .collect(Collectors.toList());

        for (PaymentOrder order : paidOrders) {
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
                    logger.info("退款成功: orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
                } else {
                    logger.error("退款失败: orderNo={}, code={}, msg={}",
                            order.getOrderNo(), response.getCode(), response.getMsg());
                }
            } catch (AlipayApiException e) {
                logger.error("调用退款API失败: orderNo={}", order.getOrderNo(), e);
            }
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
        } else if (paymentOrder.getPaymentType() == PaymentType.FINAL_PAYMENT) {
            commission.setFinalPaid(true);
            commission.setStatus(CommissionStatus.COMPLETED);
            commissionRepository.save(commission);
            logger.info("约稿状态更新: {} → COMPLETED", commission.getId());
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
                .build();
    }
}
