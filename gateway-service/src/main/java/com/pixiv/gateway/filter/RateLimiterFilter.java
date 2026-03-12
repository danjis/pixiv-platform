package com.pixiv.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixiv.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IP 限流过滤器
 *
 * 基于 Redis 的固定窗口限流，按 IP + API 路径分组限制请求速率。
 * 规则（每分钟）：
 * - 登录/注册接口：30 req/min
 * - 验证码接口：30 req/min
 * - 文件上传接口：60 req/min
 * - 搜索建议接口：120 req/min
 * - 作品列表接口：1000 req/min（首页/搜索/排行并发请求多）
 * - 其他每个 API 路径独立计数：1000 req/min
 */
@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 路径前缀 → 每分钟请求上限（有序，按前缀匹配，先匹配先命中）
     */
    private static final Map<String, Integer> PATH_LIMITS = new LinkedHashMap<>();

    static {
        PATH_LIMITS.put("/api/auth/login", 30);
        PATH_LIMITS.put("/api/auth/register", 30);
        PATH_LIMITS.put("/api/captcha", 30);
        PATH_LIMITS.put("/api/files/upload", 60);
        PATH_LIMITS.put("/api/artworks/suggestions", 120);
        // 作品前缀独立设置更高上限，首页/搜索/支流等并发请求很容易超标
        PATH_LIMITS.put("/api/artworks", 1000);
    }

    /** 每个 API 路径独立的默认限流：1000 req/min */
    private static final int DEFAULT_LIMIT = 1000;

    /** 限流窗口：60 秒 */
    private static final Duration WINDOW = Duration.ofSeconds(60);

    /** 开发环境可通过 rate-limit.enabled=false 关闭限流 */
    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!rateLimitEnabled) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String ip = getClientIp(request);
        String path = request.getURI().getPath();
        int limit = resolveLimit(path);
        String key = RATE_LIMIT_PREFIX + ip + ":" + resolveBucket(path);

        return Mono.fromCallable(() -> {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, WINDOW);
            }
            return count != null ? count : 0L;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(count -> {
                    if (count > limit) {
                        log.warn("限流触发: ip={}, path={}, count={}, limit={}", ip, path, count, limit);
                        return rejectRequest(exchange);
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    // Redis 不可用时放行，不影响正常业务
                    log.error("限流检查异常，放行请求: {}", ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之前执行（AuthenticationFilter order=-100）
        return -200;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    /**
     * 根据路径匹配限流桶名称。
     * 按照 API 路径的前两段分桶（例如 /api/artworks/123 → /api/artworks），
     * 使不同功能模块独立限流，避免所有请求共享一个桶。
     */
    private String resolveBucket(String path) {
        // 先检查是否匹配特定前缀
        for (Map.Entry<String, Integer> entry : PATH_LIMITS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getKey();
            }
        }
        // 按 API 路径的前两段分桶：/api/artworks/xxx → /api/artworks
        String[] segments = path.split("/");
        if (segments.length >= 3) {
            return "/" + segments[1] + "/" + segments[2]; // e.g. /api/artworks
        }
        return path;
    }

    /**
     * 根据路径匹配限流上限
     */
    private int resolveLimit(String path) {
        for (Map.Entry<String, Integer> entry : PATH_LIMITS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_LIMIT;
    }

    /**
     * 返回 429 Too Many Requests
     */
    private Mono<Void> rejectRequest(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            Result<?> result = Result.error("请求过于频繁，请稍后再试");
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] fallback = "{\"code\":429,\"message\":\"请求过于频繁\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
