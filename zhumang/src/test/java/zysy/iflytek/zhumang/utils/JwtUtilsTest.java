package zysy.iflytek.zhumang.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=zhumang-jwt-secret-key-2024-please-keep-it-safe-and-secure-and-long-enough-for-hs512-algorithm",
    "jwt.expire=3600",
    "jwt.refresh-expire=604800"
})
public class JwtUtilsTest {

    @Test
    public void testTokenGenerationAndValidation() {
        // 测试用户ID
        Long userId = 123L;

        // 生成访问令牌
        String accessToken = JwtUtils.generateToken(userId);
        assertNotNull(accessToken, "生成的访问令牌不应为空");
        System.out.println("生成的访问令牌: " + accessToken);

        // 生成刷新令牌
        String refreshToken = JwtUtils.generateRefreshToken(userId);
        assertNotNull(refreshToken, "生成的刷新令牌不应为空");
        System.out.println("生成的刷新令牌: " + refreshToken);

        // 验证访问令牌
        try {
            Long extractedUserId = JwtUtils.getUserIdFromToken(accessToken);
            assertEquals(userId, extractedUserId, "从令牌中提取的用户ID应该匹配");
            System.out.println("成功从访问令牌中提取用户ID: " + extractedUserId);

            boolean isExpired = JwtUtils.isTokenExpired(accessToken);
            assertFalse(isExpired, "新生成的令牌不应过期");
            System.out.println("访问令牌未过期");
        } catch (Exception e) {
            fail("访问令牌验证失败: " + e.getMessage());
        }

        // 验证刷新令牌
        try {
            Long extractedUserId = JwtUtils.getUserIdFromToken(refreshToken);
            assertEquals(userId, extractedUserId, "从刷新令牌中提取的用户ID应该匹配");
            System.out.println("成功从刷新令牌中提取用户ID: " + extractedUserId);

            boolean isExpired = JwtUtils.isTokenExpired(refreshToken);
            assertFalse(isExpired, "新生成的刷新令牌不应过期");
            System.out.println("刷新令牌未过期");
        } catch (Exception e) {
            fail("刷新令牌验证失败: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidToken() {
        // 测试无效令牌
        String invalidToken = "invalid.token.here";
        assertThrows(Exception.class, () -> {
            JwtUtils.getUserIdFromToken(invalidToken);
        }, "无效令牌应该抛出异常");

        // 测试过期令牌（通过修改过期时间为过去的时间）
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE3MDk5MjQwMDAsImV4cCI6MTcwOTkyNDAwMH0.invalid_signature";
        assertTrue(JwtUtils.isTokenExpired(expiredToken), "过期令牌应该被识别为过期");
    }

    @Test
    public void testTokenWithDifferentUserIds() {
        // 测试不同用户ID的令牌
        Long userId1 = 123L;
        Long userId2 = 456L;

        String token1 = JwtUtils.generateToken(userId1);
        String token2 = JwtUtils.generateToken(userId2);

        assertNotEquals(token1, token2, "不同用户ID的令牌应该不同");

        try {
            Long extractedUserId1 = JwtUtils.getUserIdFromToken(token1);
            Long extractedUserId2 = JwtUtils.getUserIdFromToken(token2);

            assertEquals(userId1, extractedUserId1, "第一个令牌的用户ID应该匹配");
            assertEquals(userId2, extractedUserId2, "第二个令牌的用户ID应该匹配");
            assertNotEquals(extractedUserId1, extractedUserId2, "两个令牌的用户ID应该不同");

            System.out.println("成功验证不同用户ID的令牌");
        } catch (Exception e) {
            fail("令牌验证失败: " + e.getMessage());
        }
    }
} 