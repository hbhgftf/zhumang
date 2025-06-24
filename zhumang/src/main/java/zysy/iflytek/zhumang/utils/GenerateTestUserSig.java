package zysy.iflytek.zhumang.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenerateTestUserSig {
    
    private static long sdkAppId;
    private static String secretKey;
    
    @Value("${tencent.cloud.sdkAppId}")
    public void setSdkAppId(long sdkAppId) {
        GenerateTestUserSig.sdkAppId = sdkAppId;
    }
    
    @Value("${tencent.cloud.secretKey}")
    public void setSecretKey(String secretKey) {
        GenerateTestUserSig.secretKey = secretKey;
    }

    /**
     * 生成 UserSig
     * 使用腾讯云官方 TLSSigAPIv2 生成 UserSig
     * https://cloud.tencent.com/document/product/647/17275
     * 
     * @param userId 用户 ID
     * @param expireTime 有效期，单位秒
     * @return UserSig
     */
    public static String generateUserSig(String userId, long expireTime) {
        if (secretKey == null || secretKey.length() == 0) {
            throw new IllegalArgumentException("SecretKey 不能为空");
        }
        if (userId == null || userId.length() == 0) {
            throw new IllegalArgumentException("UserId 不能为空");
        }

        try {
            TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
            return api.genUserSig(userId, expireTime);
        } catch (Exception e) {
            throw new RuntimeException("生成 UserSig 失败: " + e.getMessage(), e);
        }
    }
} 