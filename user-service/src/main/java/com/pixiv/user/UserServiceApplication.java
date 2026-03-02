package com.pixiv.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 * 
 * 职责：
 * - 用户注册、登录、认证
 * - 个人信息管理
 * - 画师申请与审核
 * - 用户关注功能
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务注册与发现
@EnableFeignClients     // 启用 Feign 客户端
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("用户服务启动成功！");
        System.out.println("服务端口: 8081");
        System.out.println("Swagger 文档: http://localhost:8081/swagger-ui.html");
        System.out.println("========================================");
    }

}
