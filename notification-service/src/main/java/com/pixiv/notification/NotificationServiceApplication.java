package com.pixiv.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 通知服务启动类
 * 
 * 功能：
 * - 系统通知管理（创建、查询、标记已读）
 * - 邮件通知发送
 * - 监听 RabbitMQ 消息队列，自动创建通知
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用 Nacos 服务注册与发现
@EnableFeignClients     // 启用 Feign 客户端（调用用户服务）
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("通知服务启动成功！");
        System.out.println("端口: 8084");
        System.out.println("Swagger 文档: http://localhost:8084/swagger-ui.html");
        System.out.println("========================================");
    }
}
