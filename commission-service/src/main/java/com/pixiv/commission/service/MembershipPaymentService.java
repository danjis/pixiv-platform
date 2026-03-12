package com.pixiv.commission.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pixiv.commission.config.AlipayConfig;
import com.pixiv.commission.entity.*;
import com.pixiv.commission.feign.MembershipClient;
import com.pixiv.commission.repository.PaymentOrderRepository;
import com.pixiv.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会员支付服务
 * 
 * 处理VIP/SVIP会员开通的支付宝沙箱支付
 */
@Service
public class MembershipPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipPaymentService.class);

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private MembershipClient membershipClient;

    /** 会员价格表 */
    private static final Map<String, BigDecimal> PRICE_MAP = new HashMap<>();
    static {
        PRICE_MAP.put("VIP_30", new BigDecimal("15.00"));
        PRICE_MAP.put("VIP_90", new BigDecimal("40.00"));
        PRICE_MAP.put("VIP_180", new BigDecimal("68.00"));
        PRICE_MAP.put("VIP_365", new BigDecimal("118.00"));
        PRICE_MAP.put("SVIP_30", new BigDecimal("30.00"));
        PRICE_MAP.put("SVIP_90", new BigDecimal("80.00"));
        PRICE_MAP.put("SVIP_180", new BigDecimal("138.00"));
        PRICE_MAP.put("SVIP_365", new BigDecimal("238.00"));
    }

    /**
     * 创建会员支付订单
     *
     * @param userId       用户ID
     * @param level        会员等级 VIP/SVIP
     * @param durationDays 时长(天)
     * @return 支付宝表单 HTML
     */
    @Transactional
    public String createMembershipPayment(Long userId, String level, int durationDays) {
        // 校验参数
        if (!"VIP".equals(level) && !"SVIP".equals(level)) {
            throw new IllegalArgumentException("无效的会员等级: " + level);
        }

        String priceKey = level + "_" + durationDays;
        BigDecimal amount = PRICE_MAP.get(priceKey);
        if (amount == null) {
            throw new IllegalArgumentException("不支持的会员时长: " + durationDays + "天");
        }

        // 生成订单号 (VIP + 时间戳 + UUID)
        String orderNo = "VIP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);

        // 构建支付订单标题
        String durationLabel = durationDays + "天";
        if (durationDays == 30)
            durationLabel = "1个月";
        else if (durationDays == 90)
            durationLabel = "3个月";
        else if (durationDays == 180)
            durationLabel = "半年";
        else if (durationDays == 365)
            durationLabel = "1年";

        String subject = "Pixiv " + level + "会员 - " + durationLabel;

        // 创建支付订单 (commissionId=0 表示会员订单)
        PaymentOrder paymentOrder = PaymentOrder.builder()
                .orderNo(orderNo)
                .commissionId(0L)
                .payerId(userId)
                .payeeId(0L)
                .amount(amount)
                .paymentType(PaymentType.MEMBERSHIP)
                .status(PaymentStatus.PENDING)
                .subject(subject)
                .build();

        paymentOrderRepository.save(paymentOrder);

        logger.info("创建会员支付订单: orderNo={}, userId={}, level={}, duration={}, amount={}",
                orderNo, userId, level, durationDays, amount);

        // 生成支付宝表单
        return generateAlipayForm(paymentOrder);
    }

    /**
     * 处理会员支付回调（支付成功后调用user-service升级会员）
     */
    @Transactional
    public void handleMembershipPaymentSuccess(PaymentOrder paymentOrder) {
        logger.info("处理会员支付成功: orderNo={}, subject={}", paymentOrder.getOrderNo(), paymentOrder.getSubject());

        // 从subject解析会员等级和时长
        String subject = paymentOrder.getSubject();
        String level = "VIP";
        int durationDays = 30;

        if (subject.contains("SVIP")) {
            level = "SVIP";
        }
        if (subject.contains("1个月"))
            durationDays = 30;
        else if (subject.contains("3个月"))
            durationDays = 90;
        else if (subject.contains("半年"))
            durationDays = 180;
        else if (subject.contains("1年"))
            durationDays = 365;

        // 通过 Feign 调用 user-service 升级会员（走 Nacos 服务发现）
        Map<String, Object> body = new HashMap<>();
        body.put("userId", paymentOrder.getPayerId());
        body.put("level", level);
        body.put("durationDays", durationDays);

        try {
            Result<Map<String, Object>> result = membershipClient.upgradeMembership(body);
            if (result != null && result.getCode() == 200) {
                logger.info("调用user-service升级会员成功: userId={}, level={}, duration={}",
                        paymentOrder.getPayerId(), level, durationDays);
            } else {
                String msg = result != null ? result.getMessage() : "返回结果为空";
                logger.error("调用user-service升级会员失败(业务错误): userId={}, orderNo={}, msg={}",
                        paymentOrder.getPayerId(), paymentOrder.getOrderNo(), msg);
                throw new RuntimeException("会员升级失败: " + msg);
            }
        } catch (RuntimeException e) {
            throw e; // 已包装的异常继续抛出
        } catch (Exception e) {
            logger.error("调用user-service升级会员失败(通信异常): userId={}, orderNo={}",
                    paymentOrder.getPayerId(), paymentOrder.getOrderNo(), e);
            throw new RuntimeException("会员升级通信失败", e);
        }
    }

    /**
     * 查询会员支付状态
     */
    public PaymentOrder getPaymentStatus(String orderNo, Long userId) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null || !order.getPayerId().equals(userId))
            return null;
        if (order.getPaymentType() != PaymentType.MEMBERSHIP)
            return null;

        // 如果是PENDING状态，主动查询支付宝
        if (order.getStatus() == PaymentStatus.PENDING) {
            try {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                String tradeStatus = response.getTradeStatus();
                logger.info("主动查询支付宝会员订单: orderNo={}, tradeStatus={}, isSuccess={}",
                        orderNo, tradeStatus, response.isSuccess());
                if (response.isSuccess()
                        && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
                    // 先尝试升级会员，升级成功后才标记PAID
                    try {
                        handleMembershipPaymentSuccess(order);
                        // 升级成功，标记为PAID
                        order.setStatus(PaymentStatus.PAID);
                        order.setAlipayTradeNo(response.getTradeNo());
                        order.setPaidAt(LocalDateTime.now());
                        paymentOrderRepository.save(order);
                        logger.info("会员升级+支付标记PAID成功: orderNo={}", orderNo);
                    } catch (Exception upgradeEx) {
                        logger.error("会员升级失败，保留PENDING以便下次轮询重试: orderNo={}",
                                orderNo, upgradeEx);
                        // 不标记PAID，保留PENDING让下次查询重试
                    }
                }
            } catch (AlipayApiException e) {
                logger.error("查询支付宝订单失败: {}", orderNo, e);
            }
        }
        return order;
    }

    /**
     * 生成支付宝支付表单HTML
     */
    private String generateAlipayForm(PaymentOrder paymentOrder) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            // 会员支付回跳到会员中心
            request.setReturnUrl(
                    alipayConfig.getReturnUrl().replace("/commission/pay-result", "/membership/pay-result"));

            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + paymentOrder.getOrderNo() + "\"," +
                    "\"total_amount\":\"" + paymentOrder.getAmount().toPlainString() + "\"," +
                    "\"subject\":\"" + paymentOrder.getSubject() + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"timeout_express\":\"30m\"" +
                    "}");

            String form = alipayClient.pageExecute(request).getBody();
            logger.info("会员支付宝表单已生成: orderNo={}", paymentOrder.getOrderNo());
            return form;

        } catch (AlipayApiException e) {
            logger.error("调用支付宝API失败: orderNo={}", paymentOrder.getOrderNo(), e);
            throw new RuntimeException("生成支付页面失败: " + e.getMessage());
        }
    }
}
