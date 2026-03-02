package com.pixiv.artwork.config;

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
                .description("请输入 JWT token，格式：Bearer {token}\n\n" +
                        "**获取 token 的步骤：**\n" +
                        "1. 访问用户服务 (http://localhost:8081/swagger-ui.html)\n" +
                        "2. 使用 POST /api/auth/login 接口登录\n" +
                        "3. 从响应中复制 accessToken\n" +
                        "4. 点击右上角的 🔒 Authorize 按钮\n" +
                        "5. 输入 token（不需要加 'Bearer ' 前缀）");

        // 定义安全需求
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("作品服务 API 文档")
                        .description("类 Pixiv 艺术作品分享和约稿平台 - 作品服务\n\n" +
                                "## 功能说明\n" +
                                "本服务提供作品发布、浏览、搜索、标签管理、点赞、收藏、评价等功能。\n\n" +
                                "## 认证说明\n" +
                                "大部分接口需要 JWT 认证。请先在用户服务中登录获取 token，" +
                                "然后点击右上角的 🔒 Authorize 按钮，输入 token 即可。\n\n" +
                                "**注意**：输入 token 时不需要加 'Bearer ' 前缀，系统会自动添加。\n\n" +
                                "## X-User-Id 请求头说明\n" +
                                "当前版本中，部分接口需要在请求头中传入 `X-User-Id` 参数来标识当前用户。\n" +
                                "这是一个临时方案，用于开发测试阶段。\n\n" +
                                "**如何获取 User ID：**\n" +
                                "1. 在用户服务中登录后，从响应的 `user.id` 字段获取\n" +
                                "2. 或者调用 GET /api/users/me 接口查看当前用户信息\n\n" +
                                "**生产环境说明：**\n" +
                                "在生产环境中，用户 ID 会从 JWT token 中自动解析，不需要手动传入。")
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
