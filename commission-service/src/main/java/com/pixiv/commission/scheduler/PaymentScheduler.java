package com.pixiv.commission.scheduler;

import com.pixiv.commission.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付订单定时任务
 * 
 * 定期关闭超时的待支付订单
 */
@Component
public class PaymentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduler.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * 每5分钟执行一次，关闭超时的待支付订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void closeExpiredOrders() {
        try {
            int count = paymentService.closeExpiredOrders();
            if (count > 0) {
                logger.info("定时任务: 已关闭 {} 个超时订单", count);
            }
        } catch (Exception e) {
            logger.error("定时任务: 关闭超时订单异常", e);
        }
    }
}
