package com.pixiv.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 工具类
 * 
 * 功能：
 * - 验证 JWT 令牌
 * - 解析 JWT 令牌中的用户信息
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 验证 JWT 令牌并返回 Claims
     * 
     * @param token JWT 令牌
     * @return Claims 对象
     * @throws Exception 如果令牌无效或已过期
     */
    public Claims validateToken(String token) throws Exception {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("JWT 验证失败: {}", e.getMessage());
            throw new Exception("JWT 令牌无效或已过期", e);
        }
    }

    /**
     * 从 Claims 中提取用户 ID
     */
    public Long getUserIdFromClaims(Claims claims) {
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Claims 中提取用户名
     */
    public String getUsernameFromClaims(Claims claims) {
        return claims.getSubject();
    }

    /**
     * 从 Claims 中提取用户角色
     */
    public String getRoleFromClaims(Claims claims) {
        return claims.get("role", String.class);
    }

}
