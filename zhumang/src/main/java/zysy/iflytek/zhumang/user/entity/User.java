package zysy.iflytek.zhumang.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * <p>
 * 统一用户表（整合视障用户、志愿者、管理员）
 * </p>
 *
 * @author YinDehua
 * @since 2025-04-18
 */
@Getter
@Setter
@ApiModel(value = "User对象", description = "统一用户表（整合视障用户、志愿者、管理员）")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("微信唯一标识，唯一索引")
    private String openid;

    @ApiModelProperty("微信会话密钥（加密存储）")
    private String sessionKey;

    @ApiModelProperty("创建时间")
    private Date createdTime;

    @ApiModelProperty("用户角色")
    private String role;

    @ApiModelProperty("手机号（唯一）")
    private String phone;

    @ApiModelProperty("常用地址")
    private String usualAddress;

    @ApiModelProperty("语音设置（如读屏软件偏好）")
    private String voiceSettings;

    @ApiModelProperty("累计服务时长（小时）")
    private Integer serviceHours;

    /**
     * 服务评分
     */
    private Double serviceRating;

    @ApiModelProperty("所属组织（如高校/公益平台）")
    private String organization;

    @ApiModelProperty("志愿者认证时间")
    private Date certificationTime;

    @ApiModelProperty("管理员用户名（唯一）")
    private String username;

    @ApiModelProperty("管理员权限")
    private String permission;

    @ApiModelProperty("用户昵称")
    @Column(length = 50)
    private String nickname;

    @ApiModelProperty("用户头像URL")
    @Column(length = 200)
    private String avatarUrl;

    @ApiModelProperty("身份证号")
    @Column(length = 18)
    private String idCard;

    @ApiModelProperty("邮箱（唯一）")
    @Column(length = 100)
    private String email;

    @ApiModelProperty("密码（加密存储）")
    @Column(length = 100)
    private String password;

    @ApiModelProperty("用户状态：1-正常，0-禁用")
    private Integer status;

    // 省略 getter/setter，确保有以下方法：
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    // Getters & Setters
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getSessionKey() { return sessionKey; }
    public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }


}
