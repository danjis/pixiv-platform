package com.pixiv.notification.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 
 * 配置消息队列、消息转换器等
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 通知创建队列名称
     */
    public static final String NOTIFICATION_CREATE_QUEUE = "notification.create";

    /**
     * 私信消息推送队列名称
     */
    public static final String CHAT_MESSAGE_QUEUE = "chat.message";

    /**
     * 创建通知队列
     */
    @Bean
    public Queue notificationCreateQueue() {
        return new Queue(NOTIFICATION_CREATE_QUEUE, true);
    }

    /**
     * 创建私信消息推送队列
     */
    @Bean
    public Queue chatMessageQueue() {
        return new Queue(CHAT_MESSAGE_QUEUE, true);
    }

    /**
     * 配置消息转换器
     * 使用 Jackson2JsonMessageConverter 将消息转换为 JSON 格式
     * 
     * @return 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitMQ 监听器容器工厂
     * 
     * @param connectionFactory 连接工厂
     * @return 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 设置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
