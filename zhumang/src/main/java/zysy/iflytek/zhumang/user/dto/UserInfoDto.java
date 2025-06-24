package zysy.iflytek.zhumang.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户信息DTO
 * 用于接收前端传递的用户信息
 */
@Data
@ApiModel("用户信息DTO")
public class UserInfoDto {
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Long id;

    /**
     * 用户昵称
     * 可选字段，允许为空
     */
    @ApiModelProperty("用户昵称")
    private String nickname;

    /**
     * 用户头像URL
     * 可选字段，允许为空
     */
    @ApiModelProperty("用户头像URL")
    private String avatarUrl;

    /**
     * 用户openid
     * 必填字段，用于标识用户
     */
    @ApiModelProperty("用户openid")
    private String openid;

    /**
     * 用户邮箱
     */
    @ApiModelProperty("用户邮箱")
    private String email;

    /**
     * 用户手机号
     */
    @ApiModelProperty("用户手机号")
    private String phone;

    /**
     * 用户角色
     */
    @ApiModelProperty("用户角色")
    private String role;
}