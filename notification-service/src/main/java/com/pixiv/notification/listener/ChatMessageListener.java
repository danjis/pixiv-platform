package com.pixiv.notification.listener;

import com.pixiv.notification.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 私信消息监听器
 *
 * 监听 RabbitMQ chat.message 队列，收到私信事件后通过 WebSocket 推送给接收方
 */
@Component
public class ChatMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.CHAT_MESSAGE_QUEUE)
    public void handleChatMessage(Map<String, Object> event) {
        Object recipientIdObj = event.get("recipientId");
        if (recipientIdObj == null) {
            logger.warn("收到私信事件但缺少 recipientId，忽略");
            return;
        }

        Long recipientId = Long.valueOf(recipientIdObj.toString());
        logger.info("收到私信推送事件: recipientId={}, senderId={}, convId={}",
                recipientId, event.get("senderId"), event.get("conversationId"));

        try {
            // 移除 recipientId，前端不需要
            event.remove("recipientId");
            String destination = "/topic/chat/" + recipientId;
            messagingTemplate.convertAndSend(destination, event);
            logger.debug("WebSocket 私信推送成功: recipientId={}, destination={}", recipientId, destination);
        } catch (Exception e) {
            logger.warn("WebSocket 私信推送失败: recipientId={}, error={}", recipientId, e.getMessage());
        }
    }
}
