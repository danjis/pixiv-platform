package com.pixiv.commission.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feign 请求拦截器
 * 
 * 自动为所有 Feign 请求添加内部服务认证头
 * 
 * @author Pixiv Platform Team
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    private static final String INTERNAL_SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";

    @Value("${service.internal.token:pixiv-internal-service-secret-token-2024}")
    private String internalServiceToken;

    @Override
    public void apply(RequestTemplate template) {
        // 为所有 Feign 请求添加内部服务认证头
        template.header(INTERNAL_SERVICE_TOKEN_HEADER, internalServiceToken);
        
        logger.debug("添加内部服务认证头: URL={}", template.url());
    }
}
