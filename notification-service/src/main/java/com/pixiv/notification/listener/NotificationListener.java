package com.pixiv.notification.listener;

import com.pixiv.notification.config.RabbitMQConfig;
import com.pixiv.notification.dto.CreateNotificationRequest;
import com.pixiv.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通知消息监听器
 * 
 * 监听 RabbitMQ 消息队列，自动创建通知
 */
@Component
public class NotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 监听通知创建队列
     * 
     * 当其他服务发送消息到 notification.create 队列时，
     * 此方法会自动被调用，创建通知
     * 
     * @param request 创建通知请求
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_CREATE_QUEUE)
    public void handleNotificationCreate(CreateNotificationRequest request) {
        logger.info("收到通知创建消息: userId={}, type={}, content={}", 
                request.getUserId(), request.getType(), request.getContent());
        
        try {
            // 调用服务创建通知
            notificationService.createNotification(request);
            logger.info("通知创建成功: userId={}, type={}", request.getUserId(), request.getType());
        } catch (Exception e) {
            logger.error("处理通知创建消息失败: userId={}, type={}, error={}", 
                    request.getUserId(), request.getType(), e.getMessage(), e);
            // 注意：这里不抛出异常，避免消息重复消费
            // 如果需要重试机制，可以配置 RabbitMQ 的死信队列
        }
    }
}
