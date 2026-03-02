package com.pixiv.artwork.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类
 * 
 * 配置 Feign 客户端的拦截器，用于服务间调用时添加认证信息
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class FeignConfig {

    @Value("${service.internal.token:pixiv-internal-service-secret-token-2024}")
    private String internalServiceToken;

    /**
     * Feign 请求拦截器
     * 
     * 在每个 Feign 请求中自动添加内部服务认证头
     * 用于 user-service 识别这是来自内部服务的调用
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 添加内部服务认证头
                template.header("X-Internal-Service-Token", internalServiceToken);
            }
        };
    }
}
