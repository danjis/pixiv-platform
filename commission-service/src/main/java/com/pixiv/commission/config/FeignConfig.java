package com.pixiv.commission.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端配置
 * 
 * 配置 Feign 调用的超时时间、重试策略、日志级别等
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class FeignConfig {

    @Autowired
    private FeignRequestInterceptor feignRequestInterceptor;

    /**
     * 配置 Feign 请求超时时间
     * 
     * @return Request.Options
     */
    @Bean
    public Request.Options requestOptions() {
        // 连接超时时间：5000 毫秒（5 秒）
        // 读取超时时间：10000 毫秒（10 秒）
        return new Request.Options(
            5000,   // 连接超时（毫秒）
            10000   // 读取超时（毫秒）
        );
    }

    /**
     * 配置 Feign 重试策略
     * 
     * @return Retryer
     */
    @Bean
    public Retryer retryer() {
        // 重试间隔：100ms
        // 最大重试间隔：1000ms
        // 最大重试次数：3 次
        return new Retryer.Default(
            100,    // 初始重试间隔（毫秒）
            1000,   // 最大重试间隔（毫秒）
            3       // 最大重试次数
        );
    }

    /**
     * 配置 Feign 日志级别
     * 
     * @return Logger.Level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        // NONE: 不记录日志（默认）
        // BASIC: 仅记录请求方法、URL、响应状态码和执行时间
        // HEADERS: 记录 BASIC 级别的信息以及请求和响应的头信息
        // FULL: 记录所有请求和响应的详细信息
        return Logger.Level.BASIC;
    }

    /**
     * 注册 Feign 请求拦截器
     * 
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return feignRequestInterceptor;
    }
}
