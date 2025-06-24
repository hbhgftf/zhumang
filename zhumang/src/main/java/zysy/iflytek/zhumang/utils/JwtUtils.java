package zysy.iflytek.zhumang.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static String secret;  // 密钥
    private static long expireSeconds;   // 过期时间（秒）
    private static long refreshExpireSeconds;   // 刷新token过期时间（秒）

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret;
    }

    @Value("${jwt.expire}")
    public void setExpire(long expireSeconds) {
        JwtUtils.expireSeconds = expireSeconds;
    }

    @Value("${jwt.refresh-expire}")
    public void setRefreshExpire(long refreshExpireSeconds) {
        JwtUtils.refreshExpireSeconds = refreshExpireSeconds;
    }

    /**
     * 生成 JWT 访问令牌
     */
    public static String generateToken(Long userId) {
        return generateToken(userId, expireSeconds);
    }

    /**
     * 生成 JWT 刷新令牌
     */
    public static String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshExpireSeconds);
    }

    /**
     * 生成 JWT 令牌
     */
    private static String generateToken(Long userId, long expireSeconds) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 JWT 令牌中获取用户 ID
     */
    public static Long getUserIdFromToken(String token) throws JwtException {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证令牌是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌是否有效（未过期且签名正确）
     */
    public static boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}