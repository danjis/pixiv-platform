package com.pixiv.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 * 
 * 使用 STOMP 协议 + SockJS 回退方案，支持实时通知推送
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，前缀为 /topic（广播）和 /queue（点对点）
        config.enableSimpleBroker("/topic", "/queue");
        // 应用目的前缀（客户端发送消息时使用的前缀）
        config.setApplicationDestinationPrefixes("/app");
        // 用户目的前缀（用于点对点消息，如 /user/{userId}/queue/notifications）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点，客户端通过此端点建立 WebSocket 连接
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*") // 允许所有来源（开发环境）
                .withSockJS(); // 启用 SockJS 回退方案
    }
}
