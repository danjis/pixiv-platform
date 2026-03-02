package com.pixiv.file.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置类
 * 
 * 配置 API 文档信息
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("文件服务 API")
                        .version("1.0.0")
                        .description("类 Pixiv 平台 - 文件服务 API 文档")
                        .contact(new Contact()
                                .name("Pixiv Platform Team")
                                .email("support@pixiv-platform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
