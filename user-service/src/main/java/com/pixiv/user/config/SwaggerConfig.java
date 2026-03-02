package com.pixiv.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置类
 * 
 * 配置 API 文档信息和 JWT 认证
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 定义 JWT 安全方案
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("请输入 JWT token，格式：Bearer {token}");

        // 定义安全需求
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("用户服务 API 文档")
                        .description("类 Pixiv 艺术作品分享和约稿平台 - 用户服务\n\n" +
                                "## 认证说明\n" +
                                "大部分接口需要 JWT 认证。请先调用注册或登录接口获取 token，" +
                                "然后点击右上角的 🔒 Authorize 按钮，输入 token 即可。\n\n" +
                                "**注意**：输入 token 时不需要加 'Bearer ' 前缀，系统会自动添加。")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pixiv Platform Team")
                                .email("support@pixiv-platform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // 添加安全方案
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                // 全局应用安全需求（所有接口默认需要认证，除非特别标注）
                .addSecurityItem(securityRequirement);
    }

}
