package com.pixiv.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * 
 * 功能：
 * - 配置允许跨域的来源、方法、请求头
 * - 支持前端应用跨域访问 API
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class CorsConfig {

    /**
     * 配置 CORS 过滤器
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的来源（开发环境允许所有来源，生产环境应该配置具体的前端域名）
        config.addAllowedOriginPattern("*");  // 使用 Pattern 支持通配符
        
        // 允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 是否允许携带凭证（Cookie）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 暴露给前端的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Total-Count");
        
        // 配置 URL 映射
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }

}
