package com.pixiv.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 配置类
 * 
 * 配置 API 文档的基本信息
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("通知服务 API 文档")
                        .description("通知服务提供系统通知管理功能，包括：\n\n" +
                                "- 通知创建和查询\n" +
                                "- 未读通知统计\n" +
                                "- 标记已读功能\n" +
                                "- RabbitMQ 消息队列集成\n\n" +
                                "**认证说明：**\n" +
                                "- 所有接口需要在请求头中携带 `X-User-Id` 表示当前用户 ID\n" +
                                "- 实际生产环境应该使用 JWT 令牌进行认证")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pixiv Platform Team")
                                .email("dev@pixiv-platform.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("本地开发环境"),
                        new Server().url("http://localhost:8080").description("API 网关")
                ));
    }
}
