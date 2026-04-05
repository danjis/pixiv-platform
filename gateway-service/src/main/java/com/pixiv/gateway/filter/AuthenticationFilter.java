package com.pixiv.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixiv.common.dto.Result;
import com.pixiv.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 
 * 功能：
 * - 验证 JWT 令牌的有效性
 * - 将用户信息添加到请求头中传递给下游服务
 * - 对白名单路径放行（如登录、注册等）
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 白名单路径（不需要认证的接口）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login", // 登录
            "/api/auth/login-by-email", // 邮箱验证码登录
            "/api/auth/register", // 注册
            "/api/auth/refresh", // 刷新令牌
            "/api/admin/auth/**", // 管理员登录（公开接口）
            "/api/captcha/**", // 验证码（公开接口）
            "/api/artworks", // 作品列表（游客可访问）
            "/api/artworks/ranking", // 排行榜（游客可访问）
            "/api/artworks/suggestions", // 搜索联想补全（公开）
            "/api/artworks/search-by-image", // 以图搜图（公开）
            "/api/artworks/*", // 作品详情（游客可访问）
            "/api/tags/**", // 标签（游客可访问）
            "/api/contests", // 比赛列表（游客可访问）
            "/api/contests/*", // 比赛详情（游客可访问）
            "/api/contests/*/entries", // 比赛参赛作品列表（游客可查看）
            "/api/contests/featured-entries", // 精选参赛作品（游客可查看）
            "/api/payments/notify", // 支付宝异步通知回调（无JWT）
            "/api/payments/status", // 支付状态查询（支付宝回跳时无JWT可能性）
            "/api/commission-plans/artist/**", // 画师约稿方案（游客可查看）
            "/api/membership/*", // 查看用户会员状态（VIP徽章显示）
            "/api/membership/internal/**", // 服务间内部调用（会员升级）
            "/api/payments/membership/status", // 会员支付状态查询（支付宝回跳）
            "/actuator/**", // 健康检查
            "/ws/**", // WebSocket 连接（通过 token 查询参数认证）
            "/v3/api-docs/**", // API 文档
            "/swagger-ui/**", // Swagger UI
            "/swagger-ui.html", // Swagger UI 入口
            "/webjars/**", // Swagger 静态资源
            "/api/*/v3/api-docs" // 各服务 API 文档代理
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("请求路径: {}", path);

        // 检查是否在白名单中
        if (isWhiteListed(path)) {
            log.debug("白名单路径，放行: {}", path);
            // 白名单路径：如果携带了有效 JWT，仍然解析用户信息传递给下游
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    Claims claims = jwtUtil.validateToken(authHeader.substring(7));
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);
                    log.debug("白名单路径但携带有效JWT - 用户ID: {}, 角色: {}", userId, role);
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-Username", username)
                            .header("X-User-Role", role)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } catch (Exception e) {
                    log.debug("白名单路径JWT解析失败，按游客放行: {}", e.getMessage());
                }
            }
            return chain.filter(exchange);
        }

        // 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 检查 Authorization 请求头是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少 Authorization 请求头或格式错误: {}", path);
            return unauthorizedResponse(exchange, "未提供认证令牌");
        }

        // 提取 JWT 令牌
        String token = authHeader.substring(7);

        try {
            // 验证 JWT 令牌
            Claims claims = jwtUtil.validateToken(token);

            // 提取用户信息
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            log.debug("JWT 验证成功 - 用户ID: {}, 用户名: {}, 角色: {}", userId, username, role);

            // 将用户信息添加到请求头中，传递给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            // 继续执行过滤器链
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("JWT 验证失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "认证令牌无效或已过期");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                // 匹配所有子路径
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            } else if (pattern.endsWith("/*")) {
                // 匹配一级子路径
                String prefix = pattern.substring(0, pattern.length() - 2);
                return path.startsWith(prefix) && path.indexOf('/', prefix.length() + 1) == -1;
            } else if (pattern.contains("*")) {
                // 匹配单个路径段中的通配符
                String regex = pattern.replace("*", "[^/]+");
                return path.matches(regex);
            } else {
                // 精确匹配
                return path.equals(pattern);
            }
        });
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(HttpStatus.UNAUTHORIZED.value(), message);

        try {
            byte[] bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 设置过滤器优先级（数值越小优先级越高）
        return -100;
    }

}
