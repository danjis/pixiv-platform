package com.pixiv.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 约稿服务启动类
 * 
 * 功能：
 * - 约稿订单管理（创建、接受、拒绝、完成）
 * - 企划管理（创建、参与、投稿）
 * - 约稿消息沟通
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication
@EnableDiscoveryClient // 启用 Nacos 服务注册与发现
@EnableFeignClients // 启用 Feign 客户端（调用用户服务和作品服务）
@org.springframework.scheduling.annotation.EnableScheduling // 启用定时任务
public class CommissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("约稿服务启动成功！");
        System.out.println("端口: 8083");
        System.out.println("Swagger 文档: http://localhost:8083/swagger-ui.html");
        System.out.println("========================================");
    }
}
