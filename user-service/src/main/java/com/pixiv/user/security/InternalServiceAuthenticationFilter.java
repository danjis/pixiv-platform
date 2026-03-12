package com.pixiv.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 内部服务认证过滤器
 * 
 * 用于验证服务间调用的请求，通过检查 X-Internal-Service-Token 头来识别内部服务
 * 
 * @author Pixiv Platform Team
 */
@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalServiceAuthenticationFilter.class);

    private static final String INTERNAL_SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";

    @Value("${service.internal.token:pixiv-internal-service-secret-token-2024}")
    private String internalServiceToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        logger.debug("InternalServiceAuthenticationFilter - URI: {}", requestURI);

        // 检查是否为服务间调用的接口
        if (isInternalServiceEndpoint(requestURI)) {
            String token = request.getHeader(INTERNAL_SERVICE_TOKEN_HEADER);

            if (token != null && token.equals(internalServiceToken)) {
                // 验证成功，设置内部服务认证
                logger.debug("内部服务认证成功: URI={}", requestURI);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE")));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 没有内部服务 token，放行给后续 JWT 过滤器处理（支持前端 JWT 访问公开主页）
                logger.debug("内部服务 token 不存在，放行给 JWT 过滤器: URI={}", requestURI);
            }
        } else {
            // 不是内部服务接口，直接放行
            logger.debug("非内部服务接口，直接放行: URI={}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否为内部服务调用的接口
     * 
     * @param requestURI 请求 URI
     * @return true 表示是内部服务接口
     */
    private boolean isInternalServiceEndpoint(String requestURI) {
        // 需要内部服务认证的接口：
        // - /api/users/{userId} - 获取用户信息（仅数字ID）
        // - /api/users/{userId}/artist - 获取画师信息
        //
        // 排除的接口：
        // - /api/users/me - 获取当前用户信息
        // - /api/auth/** - 认证相关接口
        // - /api/captcha/** - 验证码接口

        // 使用更精确的正则表达式，确保只匹配数字ID、批量查询接口 和 内部服务接口
        boolean isInternal = requestURI.matches("^/api/users/(\\d+(/artist|/following-ids|/follower-ids)?|batch)$")
                || requestURI.startsWith("/api/membership/internal/")
                || requestURI.startsWith("/api/users/wallet/");

        logger.debug("isInternalServiceEndpoint: URI={}, isInternal={}", requestURI, isInternal);

        return isInternal;
    }
}
