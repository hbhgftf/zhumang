package zysy.iflytek.zhumang.user.dto;

import lombok.Data;

@Data
public class VolunteerRegisterDto {
    private String openid;    // openid
    private String nickname;  // 昵称
    private String avatarUrl; // 头像URL
    private String name;      // 姓名
    private String phone;     // 手机号码
    private String idCard;    // 身份证号
} 