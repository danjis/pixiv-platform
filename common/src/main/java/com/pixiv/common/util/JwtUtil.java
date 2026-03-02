package com.pixiv.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类
 * 用于生成和验证 JWT 令牌
 */
public class JwtUtil {

    /**
     * 默认密钥（实际使用时应该从配置文件读取）
     */
    private static final String DEFAULT_SECRET = "pixiv-platform-secret-key-for-jwt-token-generation-2024";

    /**
     * 访问令牌有效期（2小时）
     */
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000L;

    /**
     * 刷新令牌有效期（7天）
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     */
    private static SecretKey getSecretKey(String secret) {
        String key = StringUtils.isNotBlank(secret) ? secret : DEFAULT_SECRET;
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT 令牌
     */
    public static String generateAccessToken(Long userId, String username, String role) {
        return generateAccessToken(userId, username, role, DEFAULT_SECRET);
    }

    /**
     * 生成访问令牌（自定义密钥）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @param secret   密钥
     * @return JWT 令牌
     */
    public static String generateAccessToken(Long userId, String username, String role, String secret) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSecretKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT 令牌
     */
    public static String generateRefreshToken(Long userId, String username) {
        return generateRefreshToken(userId, username, DEFAULT_SECRET);
    }

    /**
     * 生成刷新令牌（自定义密钥）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param secret   密钥
     * @return JWT 令牌
     */
    public static String generateRefreshToken(Long userId, String username, String secret) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSecretKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析令牌
     *
     * @param token 令牌
     * @return Claims
     */
    public static Claims parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET);
    }

    /**
     * 解析令牌（自定义密钥）
     *
     * @param token  令牌
     * @param secret 密钥
     * @return Claims
     */
    public static Claims parseToken(String token, String secret) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     *
     * @param token 令牌
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return validateToken(token, DEFAULT_SECRET);
    }

    /**
     * 验证令牌是否有效（自定义密钥）
     *
     * @param token  令牌
     * @param secret 密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断令牌是否过期
     *
     * @param claims Claims
     * @return 是否过期
     */
    private static boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public static Long getUserId(String token) {
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
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token 令牌
     * @return 用户角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 从令牌中获取令牌类型
     *
     * @param token 令牌
     * @return 令牌类型（access 或 refresh）
     */
    public static String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("type") : null;
    }
}
