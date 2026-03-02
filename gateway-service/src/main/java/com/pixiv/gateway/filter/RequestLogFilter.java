package com.pixiv.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 * 
 * 功能：
 * - 记录所有请求的基本信息
 * - 记录请求处理时间
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        
        // 记录请求信息
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = getClientIp(request);
        
        log.info("请求开始 - 方法: {}, 路径: {}, 查询参数: {}, 客户端IP: {}", 
                method, path, query, clientIp);
        
        // 继续执行过滤器链，并在完成后记录响应信息
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() 
                    : 0;
            
            log.info("请求完成 - 方法: {}, 路径: {}, 状态码: {}, 耗时: {}ms", 
                    method, path, statusCode, duration);
        }));
    }

    /**
     * 获取客户端真实 IP 地址
     */
    private String getClientIp(ServerHttpRequest request) {
        // 尝试从 X-Forwarded-For 获取（经过代理的情况）
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null 
                    ? request.getRemoteAddress().getAddress().getHostAddress() 
                    : "unknown";
        }
        
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    @Override
    public int getOrder() {
        // 设置为最高优先级，最先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
