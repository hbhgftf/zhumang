package zysy.iflytek.zhumang.user.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel("用户信息更新DTO")
public class UserUpdateDto {
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty("用户昵称")
    private String nickname;

    /**
     * 用户头像URL
     */
    @ApiModelProperty("用户头像URL")
    private String avatarUrl;

    /**
     * 用户手机号
     */
    @ApiModelProperty("用户手机号")
    private String phone;

    /**
     * 常用地址
     */
    @ApiModelProperty("常用地址")
    private String usualAddress;

    /**
     * 语音设置
     */
    @ApiModelProperty("语音设置")
    private String voiceSettings;

    /**
     * 服务时长
     */
    @ApiModelProperty("服务时长")
    private Integer serviceHours;

    /**
     * 服务评分
     */
    @ApiModelProperty("服务评分")
    private Double serviceRating;

    /**
     * 所属组织
     */
    @ApiModelProperty("所属组织")
    private String organization;

    /**
     * 认证时间
     */
    @ApiModelProperty("认证时间")
    private Date certificationTime;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;
    // 不允许 role 和 permission 修改
} 