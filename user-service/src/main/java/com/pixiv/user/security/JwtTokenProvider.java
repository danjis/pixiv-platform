package com.pixiv.user.security;

import com.pixiv.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * JWT 令牌提供者
 * 负责生成、验证和管理 JWT 令牌
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:pixiv-platform-secret-key-for-jwt-token-generation-2024}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration; // 默认 2 小时

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration; // 默认 7 天

    private final RedisTemplate<String, Object> redisTemplate;

    public JwtTokenProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return 访问令牌
     */
    public String generateAccessToken(Long userId, String username, String role) {
        return JwtUtil.generateAccessToken(userId, username, role, jwtSecret);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        String refreshToken = JwtUtil.generateRefreshToken(userId, username, jwtSecret);
        
        // 将刷新令牌存储到 Redis，用于令牌撤销和验证
        String redisKey = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
        
        return refreshToken;
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token, jwtSecret);
    }

    /**
     * 解析令牌
     *
     * @param token 令牌
     * @return Claims
     */
    public Claims parseToken(String token) {
        return JwtUtil.parseToken(token, jwtSecret);
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token 令牌
     * @return 用户角色
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 验证刷新令牌
     *
     * @param userId       用户ID
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        if (!validateToken(refreshToken)) {
            return false;
        }

        // 从 Redis 中获取存储的刷新令牌
        String redisKey = "refresh_token:" + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(redisKey);

        return refreshToken.equals(storedToken);
    }

    /**
     * 撤销刷新令牌
     *
     * @param userId 用户ID
     */
    public void revokeRefreshToken(Long userId) {
        String redisKey = "refresh_token:" + userId;
        redisTemplate.delete(redisKey);
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            return null;
        }

        Claims claims = parseToken(refreshToken);
        if (claims == null) {
            return null;
        }

        Long userId = getUserId(refreshToken);
        String username = getUsername(refreshToken);

        // 验证刷新令牌是否在 Redis 中
        if (!validateRefreshToken(userId, refreshToken)) {
            return null;
        }

        // 从 claims 中获取角色（如果存在）
        String role = (String) claims.get("role");
        if (role == null) {
            role = "USER"; // 默认角色
        }

        return generateAccessToken(userId, username, role);
    }
}
