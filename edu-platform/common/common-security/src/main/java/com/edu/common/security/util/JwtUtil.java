package com.edu.common.security.util;

import com.edu.common.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析、验证
 */
@Slf4j
public class JwtUtil {

    /** 默认过期时间：24小时（毫秒） */
    public static final long DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L;
    /** 刷新 Token 过期时间：7天 */
    public static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(String secret, long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 Access Token
     */
    public String generateToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, loginUser.getUserId());
        claims.put(CLAIM_USERNAME, loginUser.getUsername());
        claims.put(CLAIM_ROLE, loginUser.getRole());
        claims.put(CLAIM_EMAIL, loginUser.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(loginUser.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token，返回用户信息
     */
    public LoginUser parseToken(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            return null;
        }
        return LoginUser.builder()
                .userId(claims.get(CLAIM_USER_ID, Long.class))
                .username(claims.get(CLAIM_USERNAME, String.class))
                .role(claims.get(CLAIM_ROLE, String.class))
                .email(claims.get(CLAIM_EMAIL, String.class))
                .build();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpiration(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 检查 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaims(token);
        return claims == null || isTokenExpired(claims);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
