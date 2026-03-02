package com.pixiv.artwork.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 配置类
 * 
 * 配置 HTTP 客户端，用于调用外部服务（如 AI 服务）
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建 RestTemplate Bean
     * 
     * 配置超时时间，避免长时间等待
     * 
     * @param builder RestTemplate 构建器
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))  // 连接超时 10 秒
                .setReadTimeout(Duration.ofSeconds(30))     // 读取超时 30 秒（AI 服务需要时间处理）
                .build();
    }
}
