package com.pixiv.gateway.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 健康检查配置
 * 
 * 功能：
 * - 提供自定义健康检查端点
 * - 用于 Nacos 服务健康检查
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class HealthCheckConfig {

    /**
     * 自定义健康检查指示器
     */
    @Bean
    public HealthIndicator gatewayHealthIndicator() {
        return () -> {
            // 检查网关服务是否正常运行
            // 这里可以添加更复杂的健康检查逻辑，如检查数据库连接、Redis 连接等
            
            try {
                // 简单的健康检查：服务正在运行
                return Health.up()
                        .withDetail("service", "gateway-service")
                        .withDetail("status", "运行中")
                        .withDetail("description", "API 网关服务正常运行")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "gateway-service")
                        .withDetail("status", "异常")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

}
