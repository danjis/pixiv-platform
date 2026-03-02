package com.pixiv.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关服务启动类
 * 
 * 功能：
 * - 统一入口：所有前端请求通过网关
 * - 路由转发：根据路径将请求转发到对应的微服务
 * - 认证鉴权：JWT 令牌验证
 * - 限流保护：防止恶意请求
 * - 跨域处理：CORS 配置
 * - 负载均衡：自动选择健康的服务实例
 * 
 * 注意：网关服务不需要数据库，因此排除了 DataSource 和 JPA 的自动配置
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,           // 排除数据源自动配置
        HibernateJpaAutoConfiguration.class          // 排除 JPA 自动配置
})
@EnableDiscoveryClient  // 启用服务注册与发现
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("========================================");
        System.out.println("API 网关服务启动成功！");
        System.out.println("端口: 8080");
        System.out.println("========================================");
    }

}
