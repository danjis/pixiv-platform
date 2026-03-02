package com.pixiv.artwork.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 
 * 配置消息队列、消息转换器和监听容器工厂
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 智能打标队列名称
     */
    public static final String TAGGING_QUEUE = "artwork.tagging";

    /**
     * 创建智能打标队列
     * 
     * @return 队列实例
     */
    @Bean
    public Queue taggingQueue() {
        // durable=true 表示队列持久化
        return new Queue(TAGGING_QUEUE, true);
    }

    /**
     * 配置消息转换器（使用 JSON 格式）
     * 
     * @return JSON 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate（发送端）
     * 
     * @param connectionFactory 连接工厂
     * @return RabbitTemplate 实例
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * 配置 @RabbitListener 监听容器工厂（消费端）
     * 
     * 显式配置以确保：
     * 1. 使用 Jackson2JsonMessageConverter 反序列化 JSON 消息
     * 2. 使用 AUTO 确认模式（覆盖 Nacos 共享配置中的 manual 模式）
     * 3. 避免因 manual ack + 无手动确认代码导致消息卡死
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // 使用自动确认模式：消息处理成功自动 ack，异常自动 nack
        // 这里显式设置，覆盖 Nacos shared-rabbitmq.yml 的 manual 模式
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(1);
        return factory;
    }
}
