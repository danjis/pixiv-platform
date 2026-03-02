package com.pixiv.artwork;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 作品服务启动类
 * 
 * 功能：
 * - 作品发布、浏览、搜索
 * - 标签管理（自动打标和手动打标）
 * - 点赞、收藏、评价等社交互动
 * - RabbitMQ 消息队列（智能打标）
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication
@EnableDiscoveryClient // 启用 Nacos 服务注册与发现
@EnableFeignClients // 启用 Feign 客户端（调用用户服务）
@EnableAsync // 启用异步方法支持
@EnableScheduling // 启用定时任务（排行榜刷新）
@EnableRabbit // 启用 RabbitMQ 监听器（智能打标消费者）
public class ArtworkServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtworkServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("作品服务启动成功！");
        System.out.println("端口: 8082");
        System.out.println("Swagger 文档: http://localhost:8082/swagger-ui.html");
        System.out.println("========================================");
    }
}
